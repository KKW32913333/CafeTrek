package com.cafetrek.service;

import com.cafetrek.domain.Cafe;
import com.cafetrek.dto.CafeResponse;
import com.cafetrek.dto.CafeUpsertRequest;
import com.cafetrek.repository.CafeRepository;
import com.cafetrek.repository.CafeVisitRepository;
import com.cafetrek.repository.FavoriteRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CafeService {

    private final CafeRepository cafeRepository;
    private final FavoriteRepository favoriteRepository;
    private final CafeVisitRepository visitRepository;
    private final PlacesService placesService;

    /** Cafes farther than this from the given location are hidden — keeps old demo/seed data from a different city out of the list. */
    private static final double MAX_DISTANCE_METERS = 5000;

    public List<CafeResponse> search(Double lat, Double lng, String keyword, Long userId) {
        if (lat != null && lng != null) {
            // Refreshes the local DB with live Google Places results near (lat, lng).
            // Safe to call even without a configured key: it just returns silently.
            placesService.searchNearby(lat, lng);
        }

        List<Cafe> cafes = (keyword == null || keyword.isBlank())
                ? cafeRepository.findAll()
                : cafeRepository.findByNameContainingIgnoreCase(keyword);

        List<CafeResponse> result = cafes.stream().map(c -> toResponse(c, lat, lng, userId)).collect(Collectors.toList());

        if (lat != null && lng != null) {
            result = result.stream()
                    .filter(r -> r.getDistanceMeters() == null || r.getDistanceMeters() <= MAX_DISTANCE_METERS)
                    .collect(Collectors.toList());
            result.sort(Comparator.comparing(
                    CafeResponse::getDistanceMeters,
                    Comparator.nullsLast(Comparator.naturalOrder())));
        }
        return result;
    }

    public CafeResponse getById(Long id, Long userId) {
        Cafe c = cafeRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("cafe not found: " + id));
        return toResponse(c, null, null, userId);
    }

    public CafeResponse upsertFromGooglePlace(CafeUpsertRequest req) {
        Cafe cafe = cafeRepository.findByGooglePlaceId(req.getGooglePlaceId()).orElseGet(Cafe::new);
        cafe.setGooglePlaceId(req.getGooglePlaceId());
        cafe.setName(req.getName());
        cafe.setAddress(req.getAddress());
        cafe.setLatitude(req.getLatitude());
        cafe.setLongitude(req.getLongitude());
        cafe.setRating(req.getRating());
        Cafe saved = cafeRepository.save(cafe);
        return CafeResponse.from(saved);
    }

    private CafeResponse toResponse(Cafe c, Double lat, Double lng, Long userId) {
        CafeResponse r = CafeResponse.from(c);
        if (lat != null && lng != null && c.getLatitude() != null && c.getLongitude() != null) {
            r.setDistanceMeters(haversineMeters(lat, lng, c.getLatitude(), c.getLongitude()));
        }
        if (userId != null) {
            Optional<?> fav = favoriteRepository.findByUserIdAndCafeId(userId, c.getId());
            r.setFavorite(fav.isPresent());
        }
        r.setVisitCount((int) visitRepository.findByUserIdAndCafeIdOrderByVisitedAtDesc(
                userId == null ? -1L : userId, c.getId()).size());
        return r;
    }

    /** Great-circle distance between two lat/lng points, in meters. */
    public static double haversineMeters(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371000; // Earth radius, meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
