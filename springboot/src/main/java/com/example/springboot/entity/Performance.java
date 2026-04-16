package com.example.springboot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "performance")
public class Performance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "play_id", nullable = false)
    private Long playId;

    @Column(name = "venue_id", nullable = false)
    private Long venueId;

    @Column(name = "venue_name", nullable = false, length = 200)
    private String venueName;

    @Column(nullable = false, length = 50)
    private String city;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "min_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal minPrice;

    @Column(name = "max_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal maxPrice;

    @Column(nullable = false, length = 50)
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
