package com.example.springboot.repository;

import com.example.springboot.entity.ActorLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActorLikeRepository extends JpaRepository<ActorLike, Long> {

    Optional<ActorLike> findByUserIdAndActorId(Long userId, Long actorId);

    List<ActorLike> findByUserIdOrderByCreatedAtDesc(Long userId);

    boolean existsByUserIdAndActorId(Long userId, Long actorId);
}
