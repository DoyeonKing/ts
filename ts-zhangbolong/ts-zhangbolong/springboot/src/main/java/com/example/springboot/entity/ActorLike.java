package com.example.springboot.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户「喜欢/关注」演员：不做打分，只记录是否喜欢，更符合常理。
 * 推荐算法可视为隐式正反馈（user_id, actor_id）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "actor_like", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "actor_id"}))
public class ActorLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "actor_id", nullable = false)
    private Long actorId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
