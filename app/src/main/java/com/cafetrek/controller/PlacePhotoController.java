package com.cafetrek.controller;

import com.cafetrek.domain.Cafe;
import com.cafetrek.repository.CafeRepository;
import com.cafetrek.service.PlacesService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * Proxies a cafe's Google Places photo through our own server, so the
 * server-side Places API key (GOOGLE_PLACES_SERVER_API_KEY) never appears
 * in a URL the browser sends directly to Google.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class PlacePhotoController {

    private final CafeRepository cafeRepository;
    private final PlacesService placesService;

    @GetMapping("/api/places/photo/{cafeId}")
    public ResponseEntity<byte[]> photo(@PathVariable Long cafeId) {
        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new EntityNotFoundException("cafe not found: " + cafeId));

        if (cafe.getPhotoReference() == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            byte[] bytes = placesService.fetchPhotoBytes(cafe.getPhotoReference());
            return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(bytes);
        } catch (Exception e) {
            log.warn("Failed to fetch Places photo for cafe {}: {}", cafeId, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}
