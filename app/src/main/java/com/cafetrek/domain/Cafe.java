package com.cafetrek.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * A cafe as known to CafeTrek. `googlePlaceId` is how we de-duplicate
 * against Google Places once that integration (spec section 17) is wired
 * up on the frontend; for now cafes are seeded locally (see data.sql).
 */
@Entity
@Table(name = "cafes")
@Getter
@Setter
@NoArgsConstructor
public class Cafe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "google_place_id", unique = true)
    private String googlePlaceId;

    @Column(nullable = false)
    private String name;

    private String address;

    private Double latitude;

    private Double longitude;

    /** Google's aggregate rating (0-5). CafeTrek's own per-user ratings live on CafeVisit. */
    private Double rating;

    /** Places API photo_reference — used to build a proxied image URL (see PlacePhotoController). */
    @Column(length = 500)
    private String photoReference;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now(ZoneId.of("Asia/Tokyo"));
        updatedAt = LocalDateTime.now(ZoneId.of("Asia/Tokyo"));
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now(ZoneId.of("Asia/Tokyo"));
    }
}
