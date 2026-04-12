package com.example.springboot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiPipelineService {

    private final SmartRecommendationService smartRecommendationService;
    private final VerticalFeatureService verticalFeatureService;

    public Map<String, Object> buildPipelineResult(String query, Long userId) {
        Map<String, Object> recommendation = smartRecommendationService.buildPlan(query, userId);
        Map<String, Object> verticalFeatures = verticalFeatureService.generateTextFeatures(query);
        List<Map<String, Object>> plans = castPlanList(recommendation.get("plans"));
        Map<String, Object> intent = castMap(recommendation.get("intent"));

        Map<String, Object> result = new LinkedHashMap<>(recommendation);
        result.put("verticalFeatures", verticalFeatures);
        result.put("retrievalSummary", buildRetrievalSummary(plans, intent, verticalFeatures));
        result.put("algorithmMetrics", buildAlgorithmMetrics(plans, intent, verticalFeatures));
        result.put("ragContext", buildRagContext(query, intent, verticalFeatures, plans));
        result.put("strategy", List.of(
                "NLU条件解析",
                "垂直特征生成",
                "知识图谱与偏好多路召回",
                "预算/城市/时间硬过滤",
                "RAG上下文拼装",
                "结构化推荐输出"
        ));
        result.put("summary", plans.isEmpty()
                ? "当前没有满足约束的可售演出，系统已完成垂直特征抽取、多路召回与事实约束过滤。"
                : "系统已完成意图解析、垂直特征生成、多路召回、RAG上下文拼装与结构化输出，生成 " + plans.size() + " 个推荐方案。");
        return result;
    }

    public Map<String, Object> generateVerticalText(String query) {
        Map<String, Object> features = new LinkedHashMap<>(verticalFeatureService.generateTextFeatures(query));
        features.put("service", "/api/ai/generate-text");
        features.put("status", "implemented");
        return features;
    }

    private Map<String, Object> buildRetrievalSummary(List<Map<String, Object>> plans,
                                                      Map<String, Object> intent,
                                                      Map<String, Object> verticalFeatures) {
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

    private Map<String, Object> buildAlgorithmMetrics(List<Map<String, Object>> plans,
                                                      Map<String, Object> intent,
                                                      Map<String, Object> verticalFeatures) {
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("intentConfidence", computeIntentConfidence(intent));
        metrics.put("featureRichness", computeFeatureRichness(verticalFeatures));
        metrics.put("factConstraintLevel", computeConstraintLevel(intent));
        metrics.put("planDiversity", plans.size());
        metrics.put("explainabilityReady", !plans.isEmpty());
        return metrics;
    }

    private Map<String, Object> buildRagContext(String query,
                                                Map<String, Object> intent,
                                                Map<String, Object> verticalFeatures,
                                                List<Map<String, Object>> plans) {
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
        if (tags instanceof List<?> list && !list.isEmpty()) {
            parts.add("偏好剧种/风格：" + String.join("、", list.stream().map(String::valueOf).toList()));
        }
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
        int total = sizeOf(verticalFeatures.get("softTags"))
                + sizeOf(verticalFeatures.get("actorTraits"))
                + sizeOf(verticalFeatures.get("reviewKeywords"))
                + sizeOf(verticalFeatures.get("analysisPoints"));
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
