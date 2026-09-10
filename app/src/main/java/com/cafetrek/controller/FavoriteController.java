package com.cafetrek.controller;

import com.cafetrek.dto.CafeResponse;
import com.cafetrek.dto.FavoriteRequest;
import com.cafetrek.service.FavoriteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @GetMapping
    public List<CafeResponse> list(@RequestParam Long userId) {
        return favoriteService.list(userId);
    }

    @PostMapping
    public void add(@Valid @RequestBody FavoriteRequest req) {
        favoriteService.add(req.getUserId(), req.getCafeId());
    }

    @DeleteMapping
    public void remove(@RequestParam Long userId, @RequestParam Long cafeId) {
        favoriteService.remove(userId, cafeId);
    }
}
