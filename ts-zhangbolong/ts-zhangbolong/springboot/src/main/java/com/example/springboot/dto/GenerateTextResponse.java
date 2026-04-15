package com.example.springboot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerateTextResponse {

    private String text;

    /**
     * 为 true 表示未调用大模型（无密钥或关闭 enabled），便于联调区分。
     */
    private boolean mock;

    /**
     * mock 原因（仅当 mock=true 时有值），用于快速定位配置问题。
     */
    private String mockReason;
}
