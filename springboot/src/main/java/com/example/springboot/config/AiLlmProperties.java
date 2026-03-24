package com.example.springboot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 大模型调用配置（OpenAI 兼容 Chat Completions，如豆包 Ark、通义等）。
 */
@Data
@ConfigurationProperties(prefix = "app.ai.llm")
public class AiLlmProperties {

    /**
     * 是否启用真实 HTTP 调用；为 false 或未配置 key 时使用本地占位文案。
     */
    private boolean enabled = true;

    /**
     * API Key，建议通过环境变量注入，勿提交仓库。
     */
    private String apiKey = "";

    /**
     * 基础地址，不含路径，例如 https://ark.cn-beijing.volces.com/api/v3
     */
    private String baseUrl = "";

    /**
     * 模型 ID 或接入点 ID。
     */
    private String model = "";

    /**
     * 请求超时（秒）。
     */
    private int timeoutSeconds = 120;

    /**
     * 图像生成专用 API Key；为空时回退使用 apiKey。
     */
    private String imageApiKey = "";

    /**
     * 图像生成专用基础地址；为空时回退使用 baseUrl。
     * 示例：https://dashscope.aliyuncs.com/api/v1
     */
    private String imageBaseUrl = "";

    /**
     * 图像生成专用模型；为空时回退使用 model。
     * 示例：wan2.6-t2i
     */
    private String imageModel = "";

    /**
     * 参考图生成专用模型（图生图/编辑）；为空时回退使用 imageModel。
     * 示例：wanx2.1-imageedit
     */
    private String imageEditModel = "";
}
