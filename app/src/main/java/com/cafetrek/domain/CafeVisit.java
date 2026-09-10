package com.cafetrek.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * One "record" from the 記録 screen: a single visit to a cafe, the coffee
 * ordered, its tasting tags, the cafe's atmosphere on that visit, and the
 * user's comment/rating. This intentionally folds the spec's separate
 * coffee_records table (section 22) into cafe_visits, since the UI captures
 * both in one form/save action. Split them back out if per-coffee history
 * independent of a visit is ever needed.
 */
@Entity
@Table(name = "cafe_visits")
@Getter
@Setter
@NoArgsConstructor
public class CafeVisit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cafe_id", nullable = false)
    private Cafe cafe;

    /** The coffee ordered on this visit. Nullable: a visit can be logged without a coffee record. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coffee_id")
    private Coffee coffee;

    @Column(nullable = false)
    private LocalDate visitedAt;

    /** 1-5 star rating for this visit. */
    private Integer rating;

    /** Price paid for the coffee, in yen. */
    private Integer price;

    // ---- 味の特徴 (multi-select tags in the UI) ----
    private boolean acidity;
    private boolean bitterness;
    private boolean sweetness;
    private boolean body;
    private boolean fruity;
    private boolean nutty;

    /** 雰囲気: おしゃれ / 静か / にぎやか / 作業向き */
    private String atmosphere;

    @Column(length = 1000)
    private String comment;

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
