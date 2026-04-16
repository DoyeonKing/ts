package com.example.springboot.service;

import com.example.springboot.entity.KnowledgeNode;
import com.example.springboot.entity.KnowledgeNode.NodeType;
import com.example.springboot.entity.Performance;
import com.example.springboot.repository.KnowledgeNodeRepository;
import com.example.springboot.repository.PerformanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PerformanceService {

    private final PerformanceRepository performanceRepository;
    private final KnowledgeNodeRepository nodeRepository;

    public List<Performance> findByPlayId(Long playId) {
        return performanceRepository.findByPlayId(playId);
    }

    public List<Performance> findOnSale() {
        return performanceRepository.findByStatusOrderByStartTimeAsc("ON_SALE");
    }

    public List<Performance> findByPlayIds(List<Long> playIds, BigDecimal maxPrice, String city, LocalDateTime startTime) {
        if (playIds == null || playIds.isEmpty()) {
            return List.of();
        }
        return performanceRepository.searchAvailablePerformances(playIds, maxPrice, city, startTime);
    }

    @Transactional
    public String initSeedData() {
        if (performanceRepository.count() > 0) {
            return "演出场次数据已存在，跳过初始化。";
        }

        createPerformance(1L, "国家大剧院", "国家大剧院", "北京",
                LocalDateTime.of(2026, 4, 11, 19, 30),
                LocalDateTime.of(2026, 4, 11, 22, 0),
                new BigDecimal("180"), new BigDecimal("480"), "ON_SALE");
        createPerformance(1L, "国家大剧院", "国家大剧院", "北京",
                LocalDateTime.of(2026, 4, 12, 14, 30),
                LocalDateTime.of(2026, 4, 12, 17, 0),
                new BigDecimal("220"), new BigDecimal("580"), "ON_SALE");
        createPerformance(5L, "北京人艺", "北京人艺", "北京",
                LocalDateTime.of(2026, 4, 12, 19, 30),
                LocalDateTime.of(2026, 4, 12, 22, 0),
                new BigDecimal("160"), new BigDecimal("320"), "ON_SALE");
        createPerformance(6L, "北京人艺", "北京人艺", "北京",
                LocalDateTime.of(2026, 4, 13, 19, 30),
                LocalDateTime.of(2026, 4, 13, 22, 0),
                new BigDecimal("120"), new BigDecimal("280"), "ON_SALE");
        createPerformance(2L, "国家大剧院", "国家大剧院", "北京",
                LocalDateTime.of(2026, 4, 18, 19, 30),
                LocalDateTime.of(2026, 4, 18, 22, 0),
                new BigDecimal("260"), new BigDecimal("680"), "ON_SALE");
        createPerformance(3L, "国家大剧院", "国家大剧院", "北京",
                LocalDateTime.of(2026, 4, 19, 19, 30),
                LocalDateTime.of(2026, 4, 19, 21, 40),
                new BigDecimal("200"), new BigDecimal("560"), "ON_SALE");
        createPerformance(4L, "国家大剧院", "国家大剧院", "北京",
                LocalDateTime.of(2026, 4, 20, 19, 30),
                LocalDateTime.of(2026, 4, 20, 22, 10),
                new BigDecimal("240"), new BigDecimal("520"), "ON_SALE");

        return "演出场次种子数据初始化成功";
    }

    private void createPerformance(Long playId, String venueLookupName, String venueName, String city,
                                   LocalDateTime startTime, LocalDateTime endTime,
                                   BigDecimal minPrice, BigDecimal maxPrice, String status) {
        KnowledgeNode play = nodeRepository.findById(playId).orElse(null);
        if (play == null || play.getNodeType() != NodeType.PLAY) {
            return;
        }
        Long venueId = nodeRepository.findByNameAndNodeType(venueLookupName, NodeType.VENUE)
                .map(KnowledgeNode::getId)
                .orElse(null);
        if (venueId == null) {
            return;
        }
        Performance performance = new Performance();
        performance.setPlayId(playId);
        performance.setVenueId(venueId);
        performance.setVenueName(venueName);
        performance.setCity(city);
        performance.setStartTime(startTime);
        performance.setEndTime(endTime);
        performance.setMinPrice(minPrice);
        performance.setMaxPrice(maxPrice);
        performance.setStatus(status);
        performanceRepository.save(performance);
    }
}
