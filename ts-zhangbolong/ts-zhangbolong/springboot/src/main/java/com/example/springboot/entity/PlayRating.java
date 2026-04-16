package com.example.springboot.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 剧目评分/短评：用户对某剧目的打分 + 可选短评（豆瓣式）。
 * play_id 对应 knowledge_node.id（node_type=PLAY），与知识图谱一致。
 * 有该表后即可为 RecBole 提供 user_id, item_id(play_id), rating 的交互数据。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "play_rating", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "play_id"}))
public class PlayRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 剧目 ID，对应 knowledge_node.id（node_type=PLAY） */
    @Column(name = "play_id", nullable = false)
    private Long playId;

    /** 1～5 星或 1～10，按业务定 */
    @Column(nullable = false)
    private Integer rating;

    /** 短评内容，可选 */
    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
