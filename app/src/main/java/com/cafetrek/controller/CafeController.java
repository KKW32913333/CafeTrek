package com.cafetrek.controller;

import com.cafetrek.dto.CafeResponse;
import com.cafetrek.dto.CafeUpsertRequest;
import com.cafetrek.service.CafeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cafes")
@RequiredArgsConstructor
public class CafeController {

    private final CafeService cafeService;

    /**
     * GET /api/cafes?lat=&lng=&keyword=&userId=
     * Backs both the ホーム "近くのカフェ" list and the 地図 screen's list/pins.
     * `lat`/`lng` sort results by distance; omit them to just list everything.
     * TODO(Phase 3): once a Google Maps API key is configured, this can proxy
     * to Places Nearby Search instead of / in addition to querying local cafes.
     */
    @GetMapping
    public List<CafeResponse> search(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long userId) {
        return cafeService.search(lat, lng, keyword, userId);
    }

    @GetMapping("/{id}")
    public CafeResponse getById(@PathVariable Long id, @RequestParam(required = false) Long userId) {
        return cafeService.getById(id, userId);
    }

    /** Imports/updates a cafe found via the client-side Google Places SDK. */
    @PostMapping
    public CafeResponse upsert(@Valid @RequestBody CafeUpsertRequest req) {
        return cafeService.upsertFromGooglePlace(req);
    }
}
