package com.cafetrek.repository;

import com.cafetrek.domain.Photo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PhotoRepository extends JpaRepository<Photo, Long> {
    List<Photo> findByVisitId(Long visitId);
    List<Photo> findByCafeId(Long cafeId);
}
