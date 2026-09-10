package com.cafetrek.controller;

import com.cafetrek.dto.StatsResponse;
import com.cafetrek.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    /** Backs the マイページ screen: 訪問数 / お気に入り / 獲得バッジ / コーヒー図鑑. */
    @GetMapping
    public StatsResponse get(@RequestParam Long userId) {
        return statsService.get(userId);
    }
}
