package com.example.springboot.repository;

import com.example.springboot.entity.PlayRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayRatingRepository extends JpaRepository<PlayRating, Long> {

    Optional<PlayRating> findByUserIdAndPlayId(Long userId, Long playId);

    List<PlayRating> findByPlayIdOrderByCreatedAtDesc(Long playId, org.springframework.data.domain.Pageable pageable);

    List<PlayRating> findByUserIdOrderByCreatedAtDesc(Long userId, org.springframework.data.domain.Pageable pageable);

    @Query("SELECT AVG(r.rating) FROM PlayRating r WHERE r.playId = :playId")
    Double getAverageRatingByPlayId(@Param("playId") Long playId);

    @Query("SELECT COUNT(r) FROM PlayRating r WHERE r.playId = :playId")
    long countByPlayId(@Param("playId") Long playId);
}
