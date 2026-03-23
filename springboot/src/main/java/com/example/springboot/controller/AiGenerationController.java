package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.dto.GenerateCreativeRequest;
import com.example.springboot.dto.GenerateCreativeResponse;
import com.example.springboot.dto.GenerateTextRequest;
import com.example.springboot.dto.GenerateTextResponse;
import com.example.springboot.service.AiGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Tag(name = "AI 生成", description = "观后感/剧情分析/演员评价等文字生成")
public class AiGenerationController {

    private final AiGenerationService aiGenerationService;

    /**
     * 文字类 AI 生成：观后感、剧情分析、演员评价。
     * body: { "type": "review"|"plot"|"actor", "playId": 可选, "userInput": 可选 }
     */
    @PostMapping("/generate-text")
    @Operation(summary = "生成文本", description = "type=review 观后感，plot 剧情分析，actor 演员评价")
    public Result generateText(@Valid @RequestBody GenerateTextRequest request) {
        GenerateTextResponse data = aiGenerationService.generateText(request);
        return Result.success(data);
    }

    /**
     * 文创图片生成：海报/周边设计。
     * body: { "type": "poster"|"merch", "playId": 可选, "style": 可选 }
     */
    @PostMapping("/creative")
    @Operation(summary = "生成文创图片", description = "type=poster 海报，merch 周边；返回 imageUrl + prompt + mock 信息")
    public Result creative(@Valid @RequestBody GenerateCreativeRequest request) {
        GenerateCreativeResponse data = aiGenerationService.generateCreative(request);
        return Result.success(data);
    }

    @GetMapping("/config-status")
    @Operation(summary = "AI 配置诊断", description = "仅返回是否已读到 api-key/base-url/model，不返回密钥原文")
    public Result configStatus() {
        return Result.success(aiGenerationService.getConfigStatus());
    }
}
