package com.example.springboot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiPipelineService {

    private static final String ANALYSIS_HEADER = "📋 需求分析";
    private static final String DESIGN_HEADER = "🎨 平面设计图提示词";
    private static final String PRODUCT_HEADER = "📦 成品展示图提示词";
    private static final String ADVICE_HEADER = "💡 设计建议";

    private final SmartRecommendationService smartRecommendationService;
    private final VerticalFeatureService verticalFeatureService;
    private final AiTextGenerationService aiTextGenerationService;
    private final AiImageGenerationService aiImageGenerationService;

    public Map<String, Object> buildPipelineResult(String query, Long userId) {
        Map<String, Object> recommendation = smartRecommendationService.buildPlan(query, userId);
        Map<String, Object> verticalFeatures = verticalFeatureService.generateTextFeatures(query);
        Map<String, Object> creativeSuggestions = verticalFeatureService.generateCreativeSuggestions(query);
        List<Map<String, Object>> plans = castPlanList(recommendation.get("plans"));
        Map<String, Object> intent = castMap(recommendation.get("intent"));

        Map<String, Object> result = new LinkedHashMap<>(recommendation);
        result.put("verticalFeatures", verticalFeatures);
        result.put("creativeSuggestions", creativeSuggestions);
        result.put("retrievalSummary", buildRetrievalSummary(plans, intent, verticalFeatures));
        result.put("algorithmMetrics", buildAlgorithmMetrics(plans, intent, verticalFeatures));
        result.put("ragContext", buildRagContext(query, intent, verticalFeatures, plans));
        result.put("strategy", List.of("NLU条件解析", "垂直特征生成", "知识图谱与偏好多路召回", "预算/城市/时间硬过滤", "RAG上下文拼装", "文创周边建议生成", "结构化推荐输出"));
        result.put("summary", plans.isEmpty()
                ? "当前没有满足约束的可售演出，系统已完成垂直特征抽取、多路召回与事实约束过滤。"
                : "系统已完成意图解析、垂直特征生成、多路召回、RAG上下文拼装与文创周边建议生成，输出 " + plans.size() + " 个推荐方案。");
        return result;
    }

    public Map<String, Object> generateVerticalText(String query) {
        Map<String, Object> features = new LinkedHashMap<>(verticalFeatureService.generateTextFeatures(query));
        features.put("service", "/api/ai/generate-text");
        features.put("status", "implemented");
        return features;
    }

    public Map<String, Object> generateCreative(String type, Long playId, String style) {
        Map<String, Object> promptPayload = generateCreativePrompt(type, playId, style);
        Map<String, Object> result = new LinkedHashMap<>(promptPayload);
        result.put("mock", true);
        result.put("mockReason", "当前阶段先生成创意方向说明，再调用图片接口出图。");
        result.put("imageUrl", "");
        return result;
    }

    public Map<String, Object> generateCreativePrompt(String type, Long playId, String style) {
        String topic = buildCreativeTopic(type, playId, style);
        Map<String, Object> suggestions = verticalFeatureService.generateCreativeSuggestions(topic);
        Map<String, Object> textFeatures = generateVerticalText(topic);
        List<Map<String, Object>> items = castPlanList(suggestions.get("items"));
        List<String> itemSummaries = items.stream()
                .map(item -> String.valueOf(item.getOrDefault("type", "周边")) + "：" + String.valueOf(item.getOrDefault("designIdea", "")))
                .collect(Collectors.toList());

        Map<String, Object> llmPromptResult = aiTextGenerationService.rewriteCreativePrompt(
                topic,
                type,
                String.valueOf(suggestions.getOrDefault("theme", "剧场")),
                String.valueOf(suggestions.getOrDefault("mood", "沉浸叙事")),
                String.valueOf(suggestions.getOrDefault("audience", "剧场兴趣人群")),
                itemSummaries,
                String.valueOf(textFeatures.getOrDefault("generatedReview", ""))
        );

        String rawPrompt = String.valueOf(llmPromptResult.getOrDefault("prompt", ""));
        Map<String, String> parsed = parseStructuredPrompt(rawPrompt);
        String mergedImagePrompt = aiTextGenerationService.mergePromptModulesForImage(
                parsed.get("analysis"),
                parsed.get("designPrompt"),
                parsed.get("productPrompt"),
                parsed.get("designAdvice"),
                type
        );

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("type", type);
        result.put("playId", playId);
        result.put("topic", topic);
        result.put("prompt", mergedImagePrompt);
        result.put("rawPrompt", rawPrompt);
        result.put("analysis", parsed.get("analysis"));
        result.put("designPrompt", parsed.get("designPrompt"));
        result.put("productPrompt", parsed.get("productPrompt"));
        result.put("designAdvice", parsed.get("designAdvice"));
        result.put("mergedImagePrompt", mergedImagePrompt);
        result.put("items", suggestions.get("items"));
        result.put("theme", suggestions.get("theme"));
        result.put("mood", suggestions.get("mood"));
        result.put("audience", suggestions.get("audience"));
        result.put("creativeReview", textFeatures.get("generatedReview"));
        result.put("llmEnabled", llmPromptResult.get("llmEnabled"));
        result.put("llmMissing", llmPromptResult.get("llmMissing"));
        result.put("llmError", llmPromptResult.get("llmError"));
        return result;
    }

    public Map<String, Object> generateCreativeImage(String prompt, String type) {
        return aiImageGenerationService.generateCreativeImage(prompt, type);
    }

    private Map<String, String> parseStructuredPrompt(String rawPrompt) {
        Map<String, String> parsed = new LinkedHashMap<>();
        parsed.put("analysis", extractSection(rawPrompt, ANALYSIS_HEADER, DESIGN_HEADER));
        parsed.put("designPrompt", extractSection(rawPrompt, DESIGN_HEADER, PRODUCT_HEADER));
        parsed.put("productPrompt", extractSection(rawPrompt, PRODUCT_HEADER, ADVICE_HEADER));
        parsed.put("designAdvice", extractSection(rawPrompt, ADVICE_HEADER, ""));
        return parsed;
    }

    private String extractSection(String text, String startMarker, String endMarker) {
        if (text == null || text.isBlank()) return "";
        int start = text.indexOf(startMarker);
        if (start < 0) return "";
        start += startMarker.length();
        int end = endMarker == null || endMarker.isBlank() ? text.length() : text.indexOf(endMarker, start);
        if (end < 0) end = text.length();
        return text.substring(start, end).replace("━━━━━━━━━━━━━━━━━━━━━━", "").replace("【对用户需求的简要分析】", "").replace("【完整的中文提示词，可直接复制使用】", "").replace("【配色建议、构图建议、材质建议等】", "").trim();
    }

    private String buildCreativeTopic(String type, Long playId, String style) {
        StringBuilder sb = new StringBuilder();
        if ("poster".equals(type)) sb.append("海报 "); else sb.append("周边 ");
        if (playId != null) sb.append("剧目").append(playId).append(" ");
        if (style != null && !style.isBlank()) sb.append(style.trim());
        return sb.toString().trim();
    }

    private Map<String, Object> buildRetrievalSummary(List<Map<String, Object>> plans, Map<String, Object> intent, Map<String, Object> verticalFeatures) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("finalPlanCount", plans.size());
        summary.put("hasBudgetConstraint", intent.get("maxPrice") != null);
        summary.put("hasCityConstraint", intent.get("city") != null);
        summary.put("weekendOnly", Boolean.TRUE.equals(intent.get("weekendOnly")));
        summary.put("actorFocus", Boolean.TRUE.equals(intent.get("needActorFocus")));
        summary.put("softTagCount", sizeOf(verticalFeatures.get("softTags")));
        summary.put("actorTraitCount", sizeOf(verticalFeatures.get("actorTraits")));
        summary.put("keywordCount", sizeOf(verticalFeatures.get("reviewKeywords")));
        summary.put("retrievalMode", "knowledge-graph + preference + performance-filter");
        return summary;
    }

    private Map<String, Object> buildAlgorithmMetrics(List<Map<String, Object>> plans, Map<String, Object> intent, Map<String, Object> verticalFeatures) {
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("intentConfidence", computeIntentConfidence(intent));
        metrics.put("featureRichness", computeFeatureRichness(verticalFeatures));
        metrics.put("factConstraintLevel", computeConstraintLevel(intent));
        metrics.put("planDiversity", plans.size());
        metrics.put("explainabilityReady", !plans.isEmpty());
        return metrics;
    }

    private Map<String, Object> buildRagContext(String query, Map<String, Object> intent, Map<String, Object> verticalFeatures, List<Map<String, Object>> plans) {
        Map<String, Object> ragContext = new LinkedHashMap<>();
        ragContext.put("userNeed", query);
        ragContext.put("constraints", buildConstraintSummary(intent));
        ragContext.put("professionalReview", verticalFeatures.get("generatedReview"));
        ragContext.put("analysisPoints", verticalFeatures.get("analysisPoints"));
        ragContext.put("candidateFacts", buildCandidateFacts(plans));
        ragContext.put("promptTemplate", "请严格依据真实票价、真实场次、真实场馆与专业赏析要点生成推荐方案，不得编造不存在的事实信息。");
        return ragContext;
    }

    private List<Map<String, Object>> buildCandidateFacts(List<Map<String, Object>> plans) {
        List<Map<String, Object>> facts = new ArrayList<>();
        for (Map<String, Object> plan : plans) {
            Map<String, Object> fact = new LinkedHashMap<>();
            fact.put("playName", plan.get("playName"));
            fact.put("venue", plan.get("venue"));
            fact.put("city", plan.get("city"));
            fact.put("showTime", plan.get("showTime"));
            fact.put("priceRange", plan.get("priceRange"));
            fact.put("reason", plan.get("reason"));
            fact.put("analysis", plan.get("analysis"));
            fact.put("scoreBreakdown", plan.get("scoreBreakdown"));
            facts.add(fact);
        }
        return facts;
    }

    private String buildConstraintSummary(Map<String, Object> intent) {
        List<String> parts = new ArrayList<>();
        Object maxPrice = intent.get("maxPrice");
        Object city = intent.get("city");
        if (maxPrice != null) parts.add("预算不超过 " + maxPrice + " 元");
        if (city != null) parts.add("城市为 " + city);
        if (Boolean.TRUE.equals(intent.get("weekendOnly"))) parts.add("优先周末场次");
        if (Boolean.TRUE.equals(intent.get("needActorFocus"))) parts.add("关注演员表现力与舞台张力");
        Object tags = intent.get("preferredTags");
        if (tags instanceof List<?> list && !list.isEmpty()) parts.add("偏好剧种/风格：" + String.join("、", list.stream().map(String::valueOf).toList()));
        return parts.isEmpty() ? "暂无额外硬性约束" : String.join("；", parts);
    }

    private double computeIntentConfidence(Map<String, Object> intent) {
        int score = 0;
        if (intent.get("maxPrice") != null) score++;
        if (intent.get("city") != null) score++;
        if (Boolean.TRUE.equals(intent.get("weekendOnly"))) score++;
        if (Boolean.TRUE.equals(intent.get("needActorFocus"))) score++;
        Object tags = intent.get("preferredTags");
        if (tags instanceof List<?> list && !list.isEmpty()) score++;
        return Math.min(1.0, score / 5.0);
    }

    private double computeFeatureRichness(Map<String, Object> verticalFeatures) {
        int total = sizeOf(verticalFeatures.get("softTags")) + sizeOf(verticalFeatures.get("actorTraits")) + sizeOf(verticalFeatures.get("reviewKeywords")) + sizeOf(verticalFeatures.get("analysisPoints"));
        return Math.min(1.0, total / 10.0);
    }

    private double computeConstraintLevel(Map<String, Object> intent) {
        int count = 0;
        if (intent.get("maxPrice") != null) count++;
        if (intent.get("city") != null) count++;
        if (Boolean.TRUE.equals(intent.get("weekendOnly"))) count++;
        return Math.min(1.0, count / 3.0);
    }

    private int sizeOf(Object value) {
        return value instanceof List<?> list ? list.size() : 0;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> castMap(Object value) {
        return value instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> castPlanList(Object value) {
        return value instanceof List<?> list ? (List<Map<String, Object>>) list : List.of();
    }
}
