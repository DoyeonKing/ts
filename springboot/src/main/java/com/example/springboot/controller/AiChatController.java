package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.service.AiPipelineService;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Validated
public class AiChatController {

    private final AiPipelineService aiPipelineService;

    @PostMapping("/chat")
    public Result chat(@RequestBody ChatRequest request) {
        return Result.success(aiPipelineService.buildPipelineResult(request.getQuery(), request.getUserId()));
    }

    @PostMapping("/generate-text")
    public Result generateText(@RequestBody ChatRequest request) {
        return Result.success(aiPipelineService.generateVerticalText(request.getQuery()));
    }

    @Data
    public static class ChatRequest {
        @NotBlank(message = "query 不能为空")
        private String query;
        private Long userId;
    }
}
