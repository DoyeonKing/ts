package com.example.springboot.dto;

import lombok.Data;

@Data
public class RecommendRequest {
    /**
     * 用户自然语言需求，例如“想看梦幻一点、预算 300 内、偏爱音乐剧”。
     */
    private String query;

    /**
     * 可选：使用 Day3 mock 用户画像。
     */
    private Long userId;

    /**
     * 返回数量，默认 5，最大 10。
     */
    private Integer topK;
}
