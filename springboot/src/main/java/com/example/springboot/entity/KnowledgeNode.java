package com.example.springboot.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "knowledge_node")
public class KnowledgeNode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "node_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private NodeType nodeType;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "extra_data", columnDefinition = "TEXT")
    private String extraData;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public enum NodeType {
        PLAY, ACTOR, TERMINOLOGY, TAG, VENUE
    }
}
