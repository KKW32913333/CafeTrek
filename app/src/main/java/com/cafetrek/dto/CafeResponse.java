package com.cafetrek.dto;

import com.cafetrek.domain.Cafe;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CafeResponse {
    private Long id;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private Double rating;
    /** Straight-line distance from the query point, in meters. Null if no location was supplied. */
    private Double distanceMeters;
    private boolean favorite;
    private int visitCount;

    public static CafeResponse from(Cafe c) {
        CafeResponse r = new CafeResponse();
        r.setId(c.getId());
        r.setName(c.getName());
        r.setAddress(c.getAddress());
        r.setLatitude(c.getLatitude());
        r.setLongitude(c.getLongitude());
        r.setRating(c.getRating());
        return r;
    }
}
