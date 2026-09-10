package com.cafetrek.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Body for POST /api/cafes. Used to import a cafe found via Google Places
 * (client-side Places SDK call, per spec section 17) into CafeTrek's own
 * table, keyed on googlePlaceId so re-importing the same place is a no-op.
 */
@Getter
@Setter
public class CafeUpsertRequest {
    @NotBlank
    private String googlePlaceId;
    @NotBlank
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private Double rating;
}
