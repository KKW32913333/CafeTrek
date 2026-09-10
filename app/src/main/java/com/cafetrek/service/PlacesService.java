package com.cafetrek.service;

import com.cafetrek.domain.Cafe;
import com.cafetrek.repository.CafeRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Talks to the Google Places API (server-side, via GOOGLE_PLACES_SERVER_API_KEY —
 * a separate, HTTP-referrer-unrestricted key from the browser's Maps key, see
 * README). Nearby Search results are upserted into the local `cafes` table,
 * keyed on google_place_id, so favorites/visits/records attach to them like
 * any other cafe.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlacesService {

    private static final String NEARBY_SEARCH_URL =
            "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=%s,%s&radius=1500&type=cafe&key=%s";
    private static final String PHOTO_URL =
            "https://maps.googleapis.com/maps/api/place/photo?maxwidth=640&photoreference=%s&key=%s";
    private static final String DETAILS_URL =
            "https://maps.googleapis.com/maps/api/place/details/json?place_id=%s&fields=website&key=%s";

    private final CafeRepository cafeRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .build();

    @Value("${google.places.server-api-key}")
    private String serverApiKey;

    public boolean isConfigured() {
        return serverApiKey != null && !serverApiKey.isBlank();
    }

    /**
     * Searches Google Places for cafes near (lat, lng), upserts each result
     * into the local DB, and returns the (now-persisted) Cafe entities.
     * Returns an empty list, without error, if no server key is configured.
     */
    // Remembers when we last hit the real Google API for a given (rounded) location,
    // so repeatedly opening ホーム/地図 near the same spot doesn't re-call Places every
    // time — this was making page loads feel slow. Rounding to 3 decimal places is
    // roughly 100m of "same place" tolerance. In-memory only (resets on redeploy),
    // which is fine: worst case is one extra live search after a restart.
    private final java.util.Map<String, java.time.Instant> lastSearchedAt = new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.time.Duration SEARCH_COOLDOWN = java.time.Duration.ofMinutes(10);

    public List<Cafe> searchNearby(double lat, double lng) {
        if (!isConfigured()) {
            log.warn("GOOGLE_PLACES_SERVER_API_KEY not configured — skipping live Places search.");
            return List.of();
        }

        String key = String.format("%.3f,%.3f", lat, lng);
        java.time.Instant last = lastSearchedAt.get(key);
        if (last != null && java.time.Duration.between(last, java.time.Instant.now()).compareTo(SEARCH_COOLDOWN) < 0) {
            log.debug("Skipping Places search for {} — searched within the last {} minutes.", key, SEARCH_COOLDOWN.toMinutes());
            return List.of();
        }
        lastSearchedAt.put(key, java.time.Instant.now());

        List<Cafe> upserted = new ArrayList<>();
        try {
            String url = String.format(NEARBY_SEARCH_URL, lat, lng, serverApiKey);
            HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            JsonNode root = objectMapper.readTree(response.body());
            String status = root.path("status").asText();
            if (!"OK".equals(status) && !"ZERO_RESULTS".equals(status)) {
                log.warn("Places Nearby Search returned status={} body={}", status, response.body());
                return List.of();
            }

            for (JsonNode result : root.path("results")) {
                upserted.add(upsertFromPlaceResult(result));
            }
        } catch (IOException | InterruptedException e) {
            log.warn("Places Nearby Search failed: {}", e.getMessage());
            Thread.currentThread().interrupt();
        }
        return upserted;
    }

    /** Fetches the actual image bytes for a photo_reference — kept server-side so the API key never reaches the browser. */
    public byte[] fetchPhotoBytes(String photoReference) throws IOException, InterruptedException {
        String url = String.format(PHOTO_URL, photoReference, serverApiKey);
        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        return response.body();
    }

    /**
     * Fetches just the "website" field via Place Details, using a field mask
     * so we're not billed for fields we don't need. Called lazily — once per
     * cafe, ever — from CafeService.getById, and the result is cached on the
     * Cafe row (website + websiteChecked) so repeat visits never call this again.
     */
    public String fetchWebsite(String googlePlaceId) {
        if (!isConfigured() || googlePlaceId == null) {
            return null;
        }
        try {
            String url = String.format(DETAILS_URL, googlePlaceId, serverApiKey);
            HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            JsonNode root = objectMapper.readTree(response.body());
            if (!"OK".equals(root.path("status").asText())) {
                log.warn("Place Details returned status={} for place_id={}", root.path("status").asText(), googlePlaceId);
                return null;
            }
            return root.path("result").path("website").asText(null);
        } catch (IOException | InterruptedException e) {
            log.warn("Place Details (website) failed for {}: {}", googlePlaceId, e.getMessage());
            Thread.currentThread().interrupt();
            return null;
        }
    }

    private Cafe upsertFromPlaceResult(JsonNode result) {
        String placeId = result.path("place_id").asText();
        Cafe cafe = cafeRepository.findByGooglePlaceId(placeId).orElseGet(Cafe::new);

        cafe.setGooglePlaceId(placeId);
        cafe.setName(result.path("name").asText());
        cafe.setAddress(result.path("vicinity").asText(null));
        if (result.has("geometry")) {
            JsonNode loc = result.path("geometry").path("location");
            cafe.setLatitude(loc.path("lat").asDouble());
            cafe.setLongitude(loc.path("lng").asDouble());
        }
        if (result.has("rating")) {
            cafe.setRating(result.path("rating").asDouble());
        }
        if (result.has("photos") && result.path("photos").isArray() && result.path("photos").size() > 0) {
            cafe.setPhotoReference(result.path("photos").get(0).path("photo_reference").asText(null));
        }

        boolean isNew = cafe.getId() == null;
        Cafe saved = cafeRepository.save(cafe);
        if (isNew) {
            log.info("Imported new cafe from Places: {} ({})", saved.getName(), placeId);
        }
        return saved;
    }
}
