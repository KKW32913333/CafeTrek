package com.cafetrek.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Master data for a coffee (spec section 12: コーヒー図鑑). */
@Entity
@Table(name = "coffees")
@Getter
@Setter
@NoArgsConstructor
public class Coffee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String country;

    private String region;

    private String roastLevel;

    private String processingMethod;
}
