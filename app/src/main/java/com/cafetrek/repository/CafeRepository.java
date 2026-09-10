package com.cafetrek.repository;

import com.cafetrek.domain.Cafe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CafeRepository extends JpaRepository<Cafe, Long> {
    List<Cafe> findByNameContainingIgnoreCase(String keyword);
    Optional<Cafe> findByGooglePlaceId(String googlePlaceId);
}
