package com.example.springboot.repository;

import com.example.springboot.entity.Performance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PerformanceRepository extends JpaRepository<Performance, Long> {

    List<Performance> findByPlayId(Long playId);

    List<Performance> findByStatusOrderByStartTimeAsc(String status);

    boolean existsByPlayId(Long playId);

    @Query("""
        SELECT p FROM Performance p
        WHERE p.playId IN :playIds
          AND (:maxPrice IS NULL OR p.minPrice <= :maxPrice)
          AND (
                :city IS NULL
                OR function('replace', p.city, '市', '') = function('replace', :city, '市', '')
              )
          AND (:startTime IS NULL OR p.startTime >= :startTime)
          AND p.status = 'ON_SALE'
        ORDER BY p.startTime ASC, p.minPrice ASC
    """)
    List<Performance> searchAvailablePerformances(
            @Param("playIds") List<Long> playIds,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("city") String city,
            @Param("startTime") LocalDateTime startTime
    );
}
