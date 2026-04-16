package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.dto.GenerateCreativeFromImageRequest;
import com.example.springboot.dto.GenerateCreativeRequest;
import com.example.springboot.dto.GenerateCreativeResponse;
import com.example.springboot.dto.RecommendRequest;
import com.example.springboot.dto.TheaterPipelineRecommendRequest;
import com.example.springboot.dto.GenerateTextRequest;
import com.example.springboot.dto.GenerateTextResponse;
import com.example.springboot.service.AiGenerationService;
import com.example.springboot.service.AiRecommendService;
import com.example.springboot.service.TheaterRecommendPipelineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Tag(name = "AI 生成", description = "观后感/剧情分析/演员评价等文字生成")
public class AiGenerationController {

    private final AiGenerationService aiGenerationService;
    private final AiRecommendService aiRecommendService;
    private final TheaterRecommendPipelineService theaterRecommendPipelineService;

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

    /**
     * 基于参考图生成文创图：海报/周边设计。
     * body: { "type": "poster"|"merch", "imageUrl": 必填, "playId": 可选, "style": 可选, "productType": 可选 }
     */
    @PostMapping("/creative-from-image")
    @Operation(summary = "参考图生成文创图片", description = "上传图转公网URL后调用；返回 imageUrl + prompt + mock 信息")
    public Result creativeFromImage(@Valid @RequestBody GenerateCreativeFromImageRequest request) {
        GenerateCreativeResponse data = aiGenerationService.generateCreativeFromImage(request);
        return Result.success(data);
    }

    @GetMapping("/config-status")
    @Operation(summary = "AI 配置诊断", description = "仅返回是否已读到 api-key/base-url/model，不返回密钥原文")
    public Result configStatus() {
        return Result.success(aiGenerationService.getConfigStatus());
    }

    /**
     * Day4: 推荐算法 MVP（基于 Day3 mock 数据）
     * body: { "query": 可选, "userId": 可选, "topK": 可选(1~10) }
     */
    @PostMapping("/recommend")
    @Operation(summary = "推荐结果", description = "混合推荐：内容匹配 + 协同信号 + 热度质量 + 预算匹配 + 多样性重排")
    public Result recommend(@RequestBody(required = false) RecommendRequest request) {
        RecommendRequest req = request == null ? new RecommendRequest() : request;
        return Result.success(aiRecommendService.recommend(req));
    }

    /**
     * 社交候选：可能与你一起看该剧的用户（基于兴趣画像与该剧偏好匹配）
     */
    @GetMapping("/recommend/companions")
    @Operation(summary = "推荐同行用户", description = "参数：userId、playId、topK；用于社交入口点击后展示候选同行")
    public Result companions(@RequestParam Long userId,
                             @RequestParam Long playId,
                             @RequestParam(required = false) Integer topK) {
        return Result.success(aiRecommendService.recommendCompanions(userId, playId, topK));
    }

    /**
     * 高级流水线：NLU + 知识图谱召回 + mock 结构化事实 +（可选）LLM 软性特征与 JSON 方案统筹。
     * 与 Day4 的 /recommend 并存，便于对比与答辩展示。
     */
    @PostMapping("/recommend/pipeline")
    @Operation(summary = "剧场推荐流水线", description = "图谱+mock+RAG+JSON；useLlm=false 可完全离线演示")
    public Result recommendPipeline(@RequestBody(required = false) TheaterPipelineRecommendRequest request) {
        TheaterPipelineRecommendRequest req = request == null ? new TheaterPipelineRecommendRequest() : request;
        return Result.success(theaterRecommendPipelineService.run(req));
    }
}
