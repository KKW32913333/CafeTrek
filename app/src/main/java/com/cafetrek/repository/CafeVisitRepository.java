package com.cafetrek.repository;

import com.cafetrek.domain.CafeVisit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CafeVisitRepository extends JpaRepository<CafeVisit, Long> {
    List<CafeVisit> findByUserIdOrderByVisitedAtDesc(Long userId);
    List<CafeVisit> findByUserIdAndCafeIdOrderByVisitedAtDesc(Long userId, Long cafeId);
    long countByUserId(Long userId);

    @org.springframework.data.jpa.repository.Query(
        "select v.coffee.country as country, count(v) as cnt " +
        "from CafeVisit v where v.user.id = :userId and v.coffee is not null " +
        "group by v.coffee.country order by cnt desc")
    List<CountryCount> countByUserGroupedByCountry(Long userId);

    interface CountryCount {
        String getCountry();
        Long getCnt();
    }
}
