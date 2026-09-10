package com.cafetrek.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    /** BCrypt hash. Never expose this via a DTO. */
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role = "USER";

    private String profileImage;

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
