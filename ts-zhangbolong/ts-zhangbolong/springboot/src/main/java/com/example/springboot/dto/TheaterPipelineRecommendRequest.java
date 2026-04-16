package com.example.springboot.dto;

import lombok.Data;

@Data
public class TheaterPipelineRecommendRequest {

    /**
     * 用户自然语言需求
     */
    private String query;

    /**
     * 可选：mock 用户画像
     */
    private Long userId;

    /**
     * 最终方案数量下限（3~5）
     */
    private Integer planCountMin = 3;

    /**
     * 最终方案数量上限（3~5）
     */
    private Integer planCountMax = 5;

    /**
     * 是否调用大模型做软性特征与 JSON 统筹（false 时全程规则+数据，便于答辩离线）
     */
    private boolean useLlm = true;
}
