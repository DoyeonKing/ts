package com.example.springboot.service;

import com.example.springboot.entity.ActorLike;
import com.example.springboot.entity.KnowledgeEdge;
import com.example.springboot.entity.KnowledgeNode;
import com.example.springboot.entity.KnowledgeNode.NodeType;
import com.example.springboot.entity.Performance;
import com.example.springboot.entity.PlayRating;
import com.example.springboot.repository.ActorLikeRepository;
import com.example.springboot.repository.KnowledgeEdgeRepository;
import com.example.springboot.repository.KnowledgeNodeRepository;
import com.example.springboot.repository.PerformanceRepository;
import com.example.springboot.repository.PlayRatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SmartRecommendationService {
    private final KnowledgeNodeRepository nodeRepository;
    private final KnowledgeEdgeRepository edgeRepository;
    private final PlayRatingRepository playRatingRepository;
    private final ActorLikeRepository actorLikeRepository;
    private final PerformanceRepository performanceRepository;
    private final VerticalFeatureService verticalFeatureService;
    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("MM月dd日 HH:mm", Locale.CHINA);

    public Map<String, Object> buildPlan(String query, Long userId) {
        QueryIntent intent = parseIntent(query);
        mergeVerticalFeatures(intent, verticalFeatureService.generateTextFeatures(query));
        RecallResult recall = recall(intent, userId);
        List<Map<String, Object>> plans = recall.candidates.stream().limit(5).map(this::toPlan).toList();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("query", query);
        result.put("intent", intent.toMap());
        result.put("plans", plans);
        result.put("strategy", List.of("NLU条件解析", "垂直特征融合", "知识图谱与偏好多路召回", "预算/城市/时间硬过滤", "融合排序", "结构化推荐输出"));
        result.put("algorithmTrace", recall.toMap());
        result.put("summary", plans.isEmpty() ? "当前没有满足约束的可售演出，建议放宽筛选条件后重试。" : "系统已生成 " + plans.size() + " 个推荐方案。");
        return result;
    }

    private QueryIntent parseIntent(String query) {
        String text = query == null ? "" : query.trim();
        QueryIntent i = new QueryIntent();
        i.query = text;
        i.maxPrice = extractBudget(text);
        i.city = text.contains("北京") ? "北京" : null;
        i.weekendOnly = text.contains("周末");
        if (text.contains("话剧")) i.tags.add("话剧");
        if (text.contains("歌剧")) i.tags.add("歌剧");
        if (text.contains("芭蕾")) i.tags.add("芭蕾");
        if (text.contains("爱情") || text.contains("浪漫")) i.tags.add("爱情");
        if (text.contains("经典")) i.tags.add("经典");
        if (text.contains("张力") || text.contains("爆发力") || text.contains("悲剧")) { i.tags.add("悲剧"); i.terms.add("独白"); }
        i.needActorFocus = text.contains("演员") || text.contains("演技");
        return i;
    }

    private void mergeVerticalFeatures(QueryIntent i, Map<String, Object> f) {
        i.actorTraits.addAll(toStringSet(f.get("actorTraits")));
        i.reviewKeywords.addAll(toStringSet(f.get("reviewKeywords")));
        i.tags.addAll(toStringSet(f.get("softTags")));
        if (!i.actorTraits.isEmpty()) i.needActorFocus = true;
        if (i.reviewKeywords.stream().anyMatch(v -> v.contains("台词") || v.contains("戏剧") || v.contains("冲突"))) i.terms.add("独白");
    }

    private Set<String> toStringSet(Object value) {
        if (!(value instanceof List<?> list)) return Set.of();
        LinkedHashSet<String> r = new LinkedHashSet<>();
        for (Object item : list) if (item != null) r.add(String.valueOf(item));
        return r;
    }

    private BigDecimal extractBudget(String text) {
        StringBuilder digits = new StringBuilder();
        for (char ch : text.toCharArray()) { if (Character.isDigit(ch)) digits.append(ch); else if (digits.length() > 0) break; }
        return digits.isEmpty() ? null : new BigDecimal(digits.toString());
    }

    private RecallResult recall(QueryIntent intent, Long userId) {
        Map<Long, CandidatePlay> map = new LinkedHashMap<>();
        int beforeTag = map.size(); collectByTag(intent.tags, map); int afterTag = map.size();
        int beforeTerm = map.size(); collectByTerm(intent.terms, map); int afterTerm = map.size();
        int beforeUser = map.size(); collectByUser(userId, map); int afterUser = map.size();
        collectByRating(map);
        if (map.isEmpty()) nodeRepository.findByNodeType(NodeType.PLAY).forEach(p -> map.put(p.getId(), CandidatePlay.from(p)));
        int beforeFilter = map.size();
        List<Performance> ps = performanceRepository.searchAvailablePerformances(new ArrayList<>(map.keySet()), intent.maxPrice, intent.city, intent.weekendOnly ? weekendStart() : null);
        Map<Long, Performance> best = ps.stream().collect(Collectors.toMap(Performance::getPlayId, p -> p, (a, b) -> a.getMinPrice().compareTo(b.getMinPrice()) <= 0 ? a : b, LinkedHashMap::new));
        List<CandidatePlay> candidates = map.values().stream().peek(c -> c.performance = best.get(c.playId)).filter(c -> c.performance != null).peek(c -> score(c, intent)).sorted(Comparator.comparingDouble(CandidatePlay::total).reversed()).toList();
        RecallResult r = new RecallResult();
        r.candidates = candidates; r.tagRecallHits = Math.max(0, afterTag - beforeTag); r.termRecallHits = Math.max(0, afterTerm - beforeTerm); r.userRecallHits = Math.max(0, afterUser - beforeUser); r.candidateCountBeforeFilter = beforeFilter; r.availablePerformanceCount = best.size(); r.filteredOutCount = Math.max(0, beforeFilter - candidates.size());
        return r;
    }

    private void collectByTag(Set<String> tags, Map<Long, CandidatePlay> map) { collectByNodeNames(tags, NodeType.TAG, 3.5, "命中标签：", map); }
    private void collectByTerm(Set<String> terms, Map<Long, CandidatePlay> map) { collectByNodeNames(terms, NodeType.TERMINOLOGY, 2.0, "相关术语：", map); }

    private void collectByNodeNames(Set<String> names, NodeType type, double score, String reason, Map<Long, CandidatePlay> map) {
        for (String name : names) nodeRepository.findByNameAndNodeType(name, type).ifPresent(src -> {
            for (KnowledgeEdge e : edgeRepository.findEdgesByNodeId(src.getId())) {
                Long otherId = e.getSourceNodeId().equals(src.getId()) ? e.getTargetNodeId() : e.getSourceNodeId();
                nodeRepository.findById(otherId).ifPresent(n -> {
                    if (n.getNodeType() == NodeType.PLAY) {
                        CandidatePlay c = map.computeIfAbsent(n.getId(), id -> CandidatePlay.from(n));
                        c.graph += score; c.reasons.add(reason + name);
                    }
                });
            }
        });
    }

    private void collectByUser(Long userId, Map<Long, CandidatePlay> map) {
        if (userId == null) return;
        for (PlayRating rating : playRatingRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, 20))) {
            nodeRepository.findById(rating.getPlayId()).ifPresent(n -> { if (n.getNodeType() == NodeType.PLAY) { CandidatePlay c = map.computeIfAbsent(n.getId(), id -> CandidatePlay.from(n)); c.preference += Math.max(0, rating.getRating()) * 0.8; c.reasons.add("历史高分剧目偏好"); } });
        }
        for (ActorLike like : actorLikeRepository.findByUserIdOrderByCreatedAtDesc(userId)) {
            for (KnowledgeEdge e : edgeRepository.findEdgesByNodeId(like.getActorId())) {
                Long otherId = e.getSourceNodeId().equals(like.getActorId()) ? e.getTargetNodeId() : e.getSourceNodeId();
                nodeRepository.findById(otherId).ifPresent(n -> { if (n.getNodeType() == NodeType.PLAY) { CandidatePlay c = map.computeIfAbsent(n.getId(), id -> CandidatePlay.from(n)); c.preference += 3.0; c.reasons.add("命中喜爱演员"); } });
            }
        }
    }

    private void collectByRating(Map<Long, CandidatePlay> map) {
        nodeRepository.findByNodeType(NodeType.PLAY).forEach(n -> { CandidatePlay c = map.computeIfAbsent(n.getId(), id -> CandidatePlay.from(n)); Double avg = playRatingRepository.getAverageRatingByPlayId(n.getId()); if (avg != null) c.ratingScore += avg; });
    }

    private void score(CandidatePlay c, QueryIntent i) {
        if (i.needActorFocus) c.preference += 1.5;
        if (!i.actorTraits.isEmpty()) c.preference += 0.8;
        if (!i.reviewKeywords.isEmpty()) c.rule += 0.6;
        if (i.weekendOnly) c.rule += 0.5;
        if (i.city != null && c.performance != null && i.city.equals(c.performance.getCity())) c.rule += 1.0;
        if (i.maxPrice != null && c.performance != null) c.rule += c.performance.getMinPrice().compareTo(i.maxPrice) <= 0 ? 1.2 : -1.5;
        c.popularity += Math.min(2.5, c.reasons.size() * 0.4);
    }

    private Map<String, Object> toPlan(CandidatePlay c) {
        Performance p = c.performance;
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("playId", c.playId); m.put("playName", c.playName); m.put("matchScore", String.format(Locale.CHINA, "%.1f", c.total() * 10)); m.put("description", c.description);
        m.put("venue", p != null ? p.getVenueName() : "待定"); m.put("city", p != null ? p.getCity() : "待定"); m.put("showTime", p != null ? p.getStartTime().format(DF) : "待定"); m.put("priceRange", p != null ? "¥" + p.getMinPrice() + "-" + p.getMaxPrice() : "待定");
        m.put("reason", c.reasons.isEmpty() ? "综合偏好推荐" : String.join("；", c.reasons.stream().distinct().limit(4).toList()));
        m.put("analysis", "图谱关联、用户偏好、口碑和规则约束综合排序后得出，适合希望快速锁定可售场次的用户。");
        m.put("scoreBreakdown", Map.of("graphScore", round1(c.graph), "preferenceScore", round1(c.preference), "ratingScore", round1(c.ratingScore), "popularityScore", round1(c.popularity), "ruleScore", round1(c.rule)));
        return m;
    }

    private LocalDateTime weekendStart() {
        LocalDate today = LocalDate.now(); int delta = DayOfWeek.SATURDAY.getValue() - today.getDayOfWeek().getValue(); if (delta < 0) delta += 7; return today.plusDays(delta).atStartOfDay();
    }
    private double round1(double v) { return Math.round(v * 10) / 10.0; }

    private static class CandidatePlay {
        Long playId; String playName; String description; Performance performance; double graph; double preference; double ratingScore; double popularity; double rule; List<String> reasons = new ArrayList<>();
        static CandidatePlay from(KnowledgeNode n) { CandidatePlay c = new CandidatePlay(); c.playId = n.getId(); c.playName = n.getName(); c.description = n.getDescription(); return c; }
        double total() { return graph + preference + ratingScore + popularity + rule; }
    }
    private static class RecallResult {
        List<CandidatePlay> candidates = List.of(); int tagRecallHits; int termRecallHits; int userRecallHits; int candidateCountBeforeFilter; int availablePerformanceCount; int filteredOutCount;
        Map<String, Object> toMap() { return Map.of("tagRecallHits", tagRecallHits, "termRecallHits", termRecallHits, "userRecallHits", userRecallHits, "candidateCountBeforeFilter", candidateCountBeforeFilter, "availablePerformanceCount", availablePerformanceCount, "filteredOutCount", filteredOutCount); }
    }
    private static class QueryIntent {
        String query; BigDecimal maxPrice; String city; boolean weekendOnly; boolean needActorFocus; Set<String> tags = new LinkedHashSet<>(); Set<String> terms = new LinkedHashSet<>(); Set<String> actorTraits = new LinkedHashSet<>(); Set<String> reviewKeywords = new LinkedHashSet<>();
        Map<String, Object> toMap() { Map<String, Object> m = new LinkedHashMap<>(); m.put("query", query); m.put("maxPrice", maxPrice); m.put("city", city); m.put("weekendOnly", weekendOnly); m.put("needActorFocus", needActorFocus); m.put("preferredTags", List.copyOf(tags)); m.put("terms", List.copyOf(terms)); m.put("actorTraits", List.copyOf(actorTraits)); m.put("reviewKeywords", List.copyOf(reviewKeywords)); return m; }
    }
}
