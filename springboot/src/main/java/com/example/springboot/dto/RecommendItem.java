package com.example.springboot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendItem {
    private Long playId;
    private String playName;
    private String genre;
    private List<String> tags;
    private String leadActor;
    private double rating;
    private int heat;
    private int priceMin;
    private int priceMax;

    /**
     * 最终分（0-100）
     */
    private double score;

    /**
     * 分项得分，便于解释与答辩展示。
     */
    private Map<String, Double> scoreBreakdown;

    /**
     * 推荐理由（可直接给前端展示）
     */
    private List<String> reasons;
}
