package com.example.springboot.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 行话/术语反馈：用户对某词条点「有用/无用」或简单评分。
 * terminology_id 对应 knowledge_node.id（node_type=TERMINOLOGY）。
 * 算法可据此优化「推荐哪些术语给用户看」或做知识图谱上的反馈增强。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "terminology_feedback", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "terminology_id"}))
public class TerminologyFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "terminology_id", nullable = false)
    private Long terminologyId;

    /** 1=有用/喜欢, 0=无用/一般，或 1～5 分 */
    @Column(nullable = false)
    private Integer rating;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
