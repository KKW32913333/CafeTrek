package com.cafetrek.repository;

import com.cafetrek.domain.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    List<Favorite> findByUserId(Long userId);
    Optional<Favorite> findByUserIdAndCafeId(Long userId, Long cafeId);
    long countByUserId(Long userId);
    void deleteByUserIdAndCafeId(Long userId, Long cafeId);
}
