package com.example.springboot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EnhancedAdvancedTheaterRecommendationService {

    private final AdvancedTheaterRecommendationService advancedTheaterRecommendationService;
    private final AdvancedRecommendationIntentLlmService advancedRecommendationIntentLlmService;
    private final ConstrainedRecommendationLlmService constrainedRecommendationLlmService;

    public Map<String, Object> buildAdvancedPlan(String query, Long userId) {
        AdvancedTheaterRecommendationService.QueryIntent intent =
                advancedRecommendationIntentLlmService.parseIntent(query);

        String normalizedQuery = normalizeQuery(intent);
        intent.setNormalizedQuery(normalizedQuery);

        Map<String, Object> result = new LinkedHashMap<>(
                advancedTheaterRecommendationService.buildAdvancedPlan(query, userId, intent)
        );

        List<Map<String, Object>> plans = castPlanList(result.get("plans"));
        Map<String, Object> intentMap = castMap(result.get("intent"));
        Map<String, Object> llmSelection = constrainedRecommendationLlmService
                .selectFromCandidates(normalizedQuery, intentMap, plans);
        result.put("llmSelection", llmSelection);
        result.put("normalizedQuery", normalizedQuery);
        result.put("plans", applyLlmSelection(plans, llmSelection));

        Object strategy = result.get("strategy");
        if (strategy instanceof List<?> list) {
            List<Object> updated = new ArrayList<>(list);
            updated.add(intent.isLlmEnabled() ? "前置 LLM 意图理解" : "前置意图解析回退本地规则");
            updated.add("后置 LLM 在真实候选中做受约束选择");
            result.put("strategy", updated);
        }
        return result;
    }

    private String normalizeQuery(AdvancedTheaterRecommendationService.QueryIntent intent) {
        String text = intent == null || intent.getNormalizedQuery() == null ? "" : intent.getNormalizedQuery().trim();
        if (text.contains("爱情剧") && !text.contains("话剧") && !text.contains("音乐剧")) {
            return text.replace("爱情剧", "爱情 话剧");
        }
        return text;
    }

    private List<Map<String, Object>> applyLlmSelection(List<Map<String, Object>> plans, Map<String, Object> llmSelection) {
        if (!(llmSelection.get("selectedPlayIds") instanceof List<?> selectedIds) || selectedIds.isEmpty()) {
            return plans;
        }

        Map<Long, Map<String, Object>> byPlayId = new LinkedHashMap<>();
        for (Map<String, Object> plan : plans) {
            Object playId = plan.get("playId");
            if (playId instanceof Number number) {
                byPlayId.put(number.longValue(), plan);
            }
        }

        List<Map<String, Object>> reordered = new ArrayList<>();
        for (Object selectedId : selectedIds) {
            Long id = toLong(selectedId);
            if (id != null && byPlayId.containsKey(id)) {
                reordered.add(byPlayId.remove(id));
            }
        }
        reordered.addAll(byPlayId.values());
        return reordered;
    }

    private Long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
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
