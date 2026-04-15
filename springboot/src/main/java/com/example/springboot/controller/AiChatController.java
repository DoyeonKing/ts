package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.service.AiPipelineService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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

    @PostMapping("/creative")
    public Result creative(@Valid @RequestBody CreativeRequest request) {
        return Result.success(aiPipelineService.generateCreative(request.getType(), request.getPlayId(), request.getStyle()));
    }

    @PostMapping("/creative-prompt")
    public Result creativePrompt(@Valid @RequestBody CreativeRequest request) {
        return Result.success(aiPipelineService.generateCreativePrompt(request.getType(), request.getPlayId(), request.getStyle()));
    }

    @PostMapping("/creative-image")
    public Result creativeImage(@RequestBody CreativeImageRequest request) {
        return Result.success(aiPipelineService.generateCreativeImage(
                request == null ? null : request.getPrompt(),
                request == null ? null : request.getType()
        ));
    }

    @Data
    public static class ChatRequest {
        @NotBlank(message = "query 不能为空")
        private String query;
        private Long userId;
    }

    @Data
    public static class CreativeRequest {
        @NotBlank(message = "type 不能为空")
        @Pattern(regexp = "poster|merch", message = "type 须为 poster 或 merch")
        private String type;
        private Long playId;
        private String style;
    }

    @Data
    public static class CreativeImageRequest {
        private String prompt;
        private String type;
    }
}
