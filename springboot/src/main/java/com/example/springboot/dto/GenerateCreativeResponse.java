package com.example.springboot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerateCreativeResponse {

    /**
     * 图片地址（可能为远端 URL 或 data URL）。
     */
    private String imageUrl;

    /**
     * 本次实际使用的生成提示词，便于调参与复现。
     */
    private String prompt;

    private boolean mock;

    private String mockReason;
}
