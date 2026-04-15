package com.example.springboot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.ai.llm")
public class AiLlmProperties {

    private boolean enabled = false;
    private String apiKey = "";
    private String baseUrl = "";
    private String model = "";
    private int timeoutSeconds = 120;
    private int connectTimeoutSeconds = 90;
    private String imageApiKey = "";
    private String imageBaseUrl = "";
    private String imageModel = "";
}
