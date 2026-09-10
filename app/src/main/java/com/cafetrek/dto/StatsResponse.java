package com.cafetrek.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class StatsResponse {
    private long visitCount;
    private long favoriteCount;
    private long badgeCount;
    /** 国名 -> 杯数, ordered by count desc (drives コーヒー図鑑). */
    private Map<String, Long> coffeeCountryBreakdown;
}
