package com.cafetrek.service;

import com.cafetrek.dto.StatsResponse;
import com.cafetrek.repository.CafeVisitRepository;
import com.cafetrek.repository.FavoriteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final CafeVisitRepository visitRepository;
    private final FavoriteRepository favoriteRepository;

    // Badge thresholds on visit count, matching spec section 11 (カフェ関連実績).
    private static final int[] BADGE_THRESHOLDS = {1, 10, 50, 100};

    public StatsResponse get(Long userId) {
        StatsResponse s = new StatsResponse();
        long visits = visitRepository.countByUserId(userId);
        s.setVisitCount(visits);
        s.setFavoriteCount(favoriteRepository.countByUserId(userId));

        long badges = 0;
        for (int t : BADGE_THRESHOLDS) if (visits >= t) badges++;
        s.setBadgeCount(badges);

        Map<String, Long> breakdown = new LinkedHashMap<>();
        for (CafeVisitRepository.CountryCount cc : visitRepository.countByUserGroupedByCountry(userId)) {
            if (cc.getCountry() != null) breakdown.put(cc.getCountry(), cc.getCnt());
        }
        s.setCoffeeCountryBreakdown(breakdown);
        return s;
    }
}
