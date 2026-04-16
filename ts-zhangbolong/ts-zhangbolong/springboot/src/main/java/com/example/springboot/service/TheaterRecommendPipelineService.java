package com.example.springboot.service;

import com.example.springboot.dto.MockPlayProfile;
import com.example.springboot.dto.RecommendItem;
import com.example.springboot.dto.RecommendRequest;
import com.example.springboot.dto.TheaterPipelineRecommendRequest;
import com.example.springboot.entity.KnowledgeNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 高级推荐流水线（答辩向）：NLU → 图谱召回 → mock 结构化过滤 → 软性特征（云端 LLM，可替换为 LoRA 服务）→ RAG 事实拼装 → JSON 方案统筹。
 * <p>
 * 说明：当前仓库无独立 /api/performances，票价事实统一来自 {@link MockRecommendationDataService}；
 * 图谱为成员 A 的 MySQL 知识图谱 {@link KnowledgeGraphService}，非 Neo4j 时仍走同一 HTTP 对内调用。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TheaterRecommendPipelineService {

    private final AiRecommendService aiRecommendService;
    private final MockRecommendationDataService mockDataService;
    private final KnowledgeGraphService knowledgeGraphService;
    private final RecommendationService recommendationService;
    private final AiGenerationService aiGenerationService;
    private final ObjectMapper objectMapper;

    public Map<String, Object> run(TheaterPipelineRecommendRequest req) {
        String query = StringUtils.hasText(req.getQuery()) ? req.getQuery().trim() : "";
        Map<String, Object> nlu = aiRecommendService.extractIntentProfile(query);

        @SuppressWarnings("unchecked")
        List<String> genres = (List<String>) nlu.getOrDefault("targetGenres", List.of());
        @SuppressWarnings("unchecked")
        List<String> tags = (List<String>) nlu.getOrDefault("targetTags", List.of());
        @SuppressWarnings("unchecked")
        Map<String, Object> budgetMap = (Map<String, Object>) nlu.getOrDefault("budget", Map.of());
        int budgetMin = budgetMap.get("min") instanceof Number n ? n.intValue() : 0;
        int budgetMax = budgetMap.get("max") instanceof Number n ? n.intValue() : 2000;

        List<String> graphKeywords = buildGraphKeywords(query, genres, tags);
        List<Map<String, Object>> graphHits = graphRecall(graphKeywords);

        RecommendRequest rr = new RecommendRequest();
        rr.setQuery(query);
        rr.setUserId(req.getUserId());
        rr.setTopK(12);
        Map<String, Object> hybrid = aiRecommendService.recommend(rr);
        @SuppressWarnings("unchecked")
        List<RecommendItem> hybridItems = (List<RecommendItem>) hybrid.getOrDefault("items", List.of());

        List<MockPlayProfile> allPlays = mockDataService.getPlays();
        LinkedHashMap<Long, MockPlayProfile> merged = new LinkedHashMap<>();

        for (RecommendItem item : hybridItems) {
            allPlays.stream()
                    .filter(p -> Objects.equals(p.getPlayId(), item.getPlayId()))
                    .findFirst()
                    .ifPresent(p -> merged.putIfAbsent(p.getPlayId(), p));
        }
        for (Map<String, Object> hit : graphHits) {
            matchMockByGraphHit(hit, allPlays).forEach(p -> merged.putIfAbsent(p.getPlayId(), p));
        }

        List<MockPlayProfile> budgetFiltered = merged.values().stream()
                .filter(p -> budgetOverlap(p, budgetMin, budgetMax))
                .limit(8)
                .toList();

        if (budgetFiltered.isEmpty()) {
            budgetFiltered = hybridItems.stream()
                    .map(RecommendItem::getPlayId)
                    .map(id -> allPlays.stream().filter(p -> Objects.equals(p.getPlayId(), id)).findFirst().orElse(null))
                    .filter(Objects::nonNull)
                    .distinct()
                    .limit(5)
                    .toList();
        }

        String ragFacts = buildRagFactsBlock(budgetFiltered);
        boolean useLlm = req.isUseLlm();

        String expert = useLlm
                ? aiGenerationService.pipelineExtractSoftFeatures(query)
                : "【离线模式】未调用大模型；软性说明由规则意图与数据字段拼接。";

        int lo = req.getPlanCountMin() == null ? 3 : req.getPlanCountMin();
        int hi = req.getPlanCountMax() == null ? 5 : req.getPlanCountMax();

        String rawJson = useLlm
                ? aiGenerationService.pipelineSynthesizePlansJson(ragFacts, expert, query, lo, hi)
                : "[]";

        List<Map<String, Object>> plans = parsePlansJson(rawJson, budgetFiltered, graphHits);

        Map<String, Object> steps = new LinkedHashMap<>();
        steps.put("nlu", nlu);
        steps.put("graphKeywords", graphKeywords);
        steps.put("graphRecall", graphHits);
        steps.put("structuredSource", "mock_hybrid_plus_graph_name_match");
        steps.put("candidateCount", merged.size());
        steps.put("afterBudgetFilterCount", budgetFiltered.size());
        steps.put("expertFeatures", Map.of(
                "text", expert,
                "source", useLlm ? "cloud_llm_placeholder_for_lora" : "offline"
        ));
        steps.put("ragFactsPreview", ragFacts.length() > 500 ? ragFacts.substring(0, 500) + "..." : ragFacts);
        steps.put("finalPlans", plans);
        steps.put("parsedFromLlm", useLlm && !plans.isEmpty());

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("strategy", "pipeline-nlu-graph-mock-rag-json-v1");
        out.put("steps", steps);
        out.put("disclaimer", "剧目与价格区间来自演示数据集与知识图谱节点名称匹配，非实时票务；生产环境应对接真实排期/票价接口。LoRA 可替换 pipelineExtractSoftFeatures 的实现。");
        return out;
    }

    private List<String> buildGraphKeywords(String query, List<String> genres, List<String> tags) {
        LinkedHashSet<String> kws = new LinkedHashSet<>();
        genres.forEach(kws::add);
        tags.stream().limit(4).forEach(kws::add);
        if (StringUtils.hasText(query) && query.length() >= 2) {
            kws.add(query.length() > 12 ? query.substring(0, 12) : query);
        }
        return new ArrayList<>(kws);
    }

    private List<Map<String, Object>> graphRecall(List<String> keywords) {
        List<Map<String, Object>> hits = new ArrayList<>();
        Set<Long> seen = new HashSet<>();
        for (String kw : keywords) {
            if (!StringUtils.hasText(kw)) continue;
            try {
                List<KnowledgeNode> nodes = knowledgeGraphService.searchNodes(kw);
                for (KnowledgeNode n : nodes) {
                    if (!seen.add(n.getId())) continue;
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("nodeId", n.getId());
                    row.put("name", n.getName());
                    row.put("nodeType", n.getNodeType().name());
                    row.put("description", n.getDescription());
                    row.put("keyword", kw);
                    hits.add(row);
                    expandGraphNeighbors(n, hits, seen);
                }
            } catch (Exception e) {
                log.debug("graph search skip kw={} : {}", kw, e.getMessage());
            }
        }
        return hits.stream().limit(24).toList();
    }

    private void expandGraphNeighbors(KnowledgeNode seed, List<Map<String, Object>> hits, Set<Long> seen) {
        if (seed.getNodeType() != KnowledgeNode.NodeType.PLAY && seed.getNodeType() != KnowledgeNode.NodeType.ACTOR) {
            return;
        }
        try {
            Map<String, Object> rec = seed.getNodeType() == KnowledgeNode.NodeType.PLAY
                    ? recommendationService.recommendForPlay(seed.getId())
                    : recommendationService.recommendForActor(seed.getId());
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> plays = (List<Map<String, Object>>) rec.getOrDefault("plays", List.of());
            for (Map<String, Object> p : plays.stream().limit(4).toList()) {
                Object id = p.get("id");
                if (id instanceof Number num && seen.add(num.longValue())) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("nodeId", num.longValue());
                    row.put("name", p.get("name"));
                    row.put("nodeType", "PLAY");
                    row.put("description", p.get("description"));
                    row.put("keyword", "neighbor_of_" + seed.getId());
                    hits.add(row);
                }
            }
        } catch (Exception e) {
            log.debug("expandGraphNeighbors skip: {}", e.getMessage());
        }
    }

    private List<MockPlayProfile> matchMockByGraphHit(Map<String, Object> hit, List<MockPlayProfile> all) {
        Object nameObj = hit.get("name");
        if (!(nameObj instanceof String gn) || !StringUtils.hasText(gn)) {
            return List.of();
        }
        String g = stripTitle(gn);
        List<MockPlayProfile> list = new ArrayList<>();
        for (MockPlayProfile p : all) {
            String pn = stripTitle(p.getPlayName());
            if (pn.contains(g) || g.contains(pn)) {
                list.add(p);
            }
        }
        return list.stream().limit(3).toList();
    }

    private static String stripTitle(String s) {
        return s.replace("《", "").replace("》", "").trim();
    }

    private static boolean budgetOverlap(MockPlayProfile p, int bMin, int bMax) {
        int left = Math.max(p.getPriceMin(), bMin);
        int right = Math.min(p.getPriceMax(), bMax);
        return left <= right;
    }

    private String buildRagFactsBlock(List<MockPlayProfile> plays) {
        if (plays.isEmpty()) {
            return "（事实区为空：无匹配剧目）";
        }
        StringBuilder sb = new StringBuilder();
        int i = 1;
        for (MockPlayProfile p : plays.stream().limit(6).toList()) {
            sb.append(i++).append(") 剧目：").append(p.getPlayName())
                    .append("；类型：").append(p.getGenre())
                    .append("；标签：").append(String.join("、", p.getTags()))
                    .append("；价格区间：").append(p.getPriceMin()).append("-").append(p.getPriceMax()).append(" 元")
                    .append("；评分：").append(p.getRating()).append("；热度：").append(p.getHeat())
                    .append("\n");
        }
        return sb.toString();
    }

    private List<Map<String, Object>> parsePlansJson(String raw, List<MockPlayProfile> fallbackPlays, List<Map<String, Object>> graphHits) {
        if (!StringUtils.hasText(raw)) {
            return fallbackPlans(fallbackPlays, graphHits);
        }
        String cleaned = raw.trim();
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceFirst("^```(?:json)?\\s*", "").replaceFirst("\\s*```$", "").trim();
        }
        int lb = cleaned.indexOf('[');
        int rb = cleaned.lastIndexOf(']');
        if (lb >= 0 && rb > lb) {
            cleaned = cleaned.substring(lb, rb + 1);
        }
        try {
            JsonNode arr = objectMapper.readTree(cleaned);
            if (!arr.isArray()) {
                return fallbackPlans(fallbackPlays, graphHits);
            }
            List<Map<String, Object>> out = new ArrayList<>();
            for (JsonNode el : arr) {
                if (!el.isObject()) continue;
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("playName", textOr(el, "playName"));
                m.put("priceNote", textOr(el, "priceNote"));
                m.put("graphReason", textOr(el, "graphReason"));
                m.put("softAngle", textOr(el, "softAngle"));
                m.put("foodHint", textOr(el, "foodHint"));
                m.put("transitHint", textOr(el, "transitHint"));
                out.add(m);
            }
            if (!out.isEmpty()) {
                return out;
            }
        } catch (Exception e) {
            log.warn("parsePlansJson failed: {}", e.getMessage());
        }
        return fallbackPlans(fallbackPlays, graphHits);
    }

    private static String textOr(JsonNode el, String field) {
        JsonNode n = el.get(field);
        return n != null && n.isTextual() ? n.asText() : "";
    }

    private List<Map<String, Object>> fallbackPlans(List<MockPlayProfile> plays, List<Map<String, Object>> graphHits) {
        String graphNote = graphHits.stream()
                .filter(h -> "PLAY".equals(String.valueOf(h.get("nodeType"))))
                .map(h -> String.valueOf(h.get("name")))
                .distinct()
                .limit(3)
                .collect(Collectors.joining("、"));
        List<Map<String, Object>> list = new ArrayList<>();
        int n = 0;
        for (MockPlayProfile p : plays.stream().limit(5).toList()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("playName", p.getPlayName());
            m.put("priceNote", p.getPriceMin() + "-" + p.getPriceMax() + " 元（演示数据）");
            m.put("graphReason", StringUtils.hasText(graphNote) ? "图谱关键词命中相关节点：" + graphNote : "图谱未命中或待补数据");
            m.put("softAngle", "结合类型/标签与用户预算做结构化兜底方案");
            m.put("foodHint", "未提供：建议剧场周边简餐，避免迟到");
            m.put("transitHint", "未提供：建议用地图 App 查询剧场最近地铁站");
            list.add(m);
            if (++n >= 3) break;
        }
        return list;
    }
}
