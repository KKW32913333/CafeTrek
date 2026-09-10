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
    @Column(length = 2000)
    private String photoReference;

    /** The cafe's own homepage URL, from Places "Place Details" (fetched lazily — see PlacesService.fetchWebsite). */
    @Column(length = 2000)
    private String website;

    /**
     * True once we've asked Places for the website (even if it came back null) — avoids repeat Place Details calls.
     * columnDefinition provides a default so Postgres can ALTER TABLE ADD COLUMN ... NOT NULL
     * on a table that already has rows (otherwise the add-column migration fails silently).
     */
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean websiteChecked = false;

    /** Comma-separated Google Places "types" (e.g. "cafe,bakery,food,point_of_interest") — used by the map screen's category filter. */
    @Column(length = 500)
    private String types;

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
