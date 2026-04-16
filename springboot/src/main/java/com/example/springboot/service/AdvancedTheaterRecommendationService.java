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
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdvancedTheaterRecommendationService {

    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("MM月dd日 HH:mm", Locale.CHINA);
    private static final Set<String> GENRE_TAGS = Set.of("话剧", "音乐剧");
    private static final Map<String, String> CITY_NORMALIZATION = Map.ofEntries(
            Map.entry("北京", "北京市"),
            Map.entry("上海", "上海市"),
            Map.entry("广州", "广州市"),
            Map.entry("深圳", "深圳市"),
            Map.entry("杭州", "杭州市"),
            Map.entry("南京", "南京市"),
            Map.entry("成都", "成都市"),
            Map.entry("武汉", "武汉市"),
            Map.entry("西安", "西安市")
    );

    private static final Map<String, List<String>> TAG_KEYWORDS = Map.ofEntries(
            Map.entry("话剧", List.of("话剧", "对白", "台词", "戏剧")),
            Map.entry("音乐剧", List.of("音乐剧", "musical", "唱段", "返场")),
            Map.entry("爱情", List.of("爱情", "恋爱", "浪漫", "约会", "情感")),
            Map.entry("经典", List.of("经典", "名作", "改编")),
            Map.entry("演员爆发力强", List.of("爆发力", "演技炸裂", "张力演员")),
            Map.entry("情绪张力强", List.of("张力", "压迫感", "冲突", "对抗")),
            Map.entry("台词密度高", List.of("台词密", "对白多", "文本密度")),
            Map.entry("新手友好", List.of("新手", "入坑", "第一次看", "容易看懂")),
            Map.entry("情侣适合", List.of("情侣", "约会", "适合约会")),
            Map.entry("适合带长辈", List.of("长辈", "爸妈", "老人")),
            Map.entry("烧脑", List.of("烧脑", "复杂", "反转", "悬疑")),
            Map.entry("催泪", List.of("催泪", "好哭", "虐", "虐一点")),
            Map.entry("群像戏", List.of("群像", "人物关系多")),
            Map.entry("现实主义", List.of("现实主义", "现实题材")),
            Map.entry("唱段突出", List.of("唱段", "歌好听", "名曲")),
            Map.entry("卡司强", List.of("卡司", "阵容", "演员阵容")),
            Map.entry("舞段突出", List.of("舞段", "舞蹈", "编舞")),
            Map.entry("适合二刷", List.of("二刷", "值得再看")),
            Map.entry("300元内可选", List.of("300内", "300以内", "预算300", "三百内")),
            Map.entry("性价比高", List.of("性价比", "划算", "预算友好"))
    );

    private static final Map<String, List<String>> TERM_KEYWORDS = Map.ofEntries(
            Map.entry("独白", List.of("独白", "内心戏")),
            Map.entry("台词节奏", List.of("台词", "对白", "节奏")),
            Map.entry("爆发戏", List.of("爆发", "情绪爆点")),
            Map.entry("对手戏", List.of("对手戏", "飙戏")),
            Map.entry("调度", List.of("调度", "场面调度")),
            Map.entry("走位", List.of("走位", "舞台移动")),
            Map.entry("卡司", List.of("卡司", "阵容")),
            Map.entry("返场", List.of("返场", "安可")),
            Map.entry("重唱", List.of("重唱", "合唱段")),
            Map.entry("舞段", List.of("舞段", "编舞"))
    );

    private static final Map<String, Double> TAG_BOOST = Map.ofEntries(
            Map.entry("演员爆发力强", 1.2),
            Map.entry("情绪张力强", 1.0),
            Map.entry("台词密度高", 0.8),
            Map.entry("新手友好", 0.8),
            Map.entry("情侣适合", 0.6),
            Map.entry("适合带长辈", 0.6),
            Map.entry("烧脑", 0.7),
            Map.entry("催泪", 0.7),
            Map.entry("唱段突出", 1.0),
            Map.entry("卡司强", 1.0),
            Map.entry("舞段突出", 0.8)
    );

    private final KnowledgeNodeRepository nodeRepository;
    private final KnowledgeEdgeRepository edgeRepository;
    private final PlayRatingRepository playRatingRepository;
    private final ActorLikeRepository actorLikeRepository;
    private final PerformanceRepository performanceRepository;

    public Map<String, Object> buildAdvancedPlan(String query, Long userId) {
        return buildAdvancedPlan(query, userId, null);
    }

    public Map<String, Object> buildAdvancedPlan(String query, Long userId, QueryIntent externalIntent) {
        QueryIntent intent = mergeIntent(query, externalIntent);
        RecallTrace trace = new RecallTrace();
        Map<Long, CandidatePlay> candidateMap = recallCandidates(intent, userId, trace);
        List<CandidatePlay> ranked = filterAndRank(candidateMap, intent);

        List<Map<String, Object>> plans = ranked.stream()
                .limit(5)
                .map(candidate -> toPlan(candidate, intent))
                .toList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("query", query);
        result.put("intent", intent.toMap());
        result.put("plans", plans);
        result.put("trace", trace.toMap());
        result.put("strategy", List.of(
                "前置意图解析",
                "标签与术语双通道召回",
                "用户偏好增强",
                "真实场次与票价过滤",
                "多因子加权排序",
                "结构化可解释输出"
        ));
        result.put("summary", plans.isEmpty()
                ? "当前没有满足条件的在售话剧或音乐剧。"
                : "高级推荐链路已输出 " + plans.size() + " 个候选方案。");
        return result;
    }

    public QueryIntent parseIntent(String query) {
        String text = query == null ? "" : query.trim();
        QueryIntent intent = new QueryIntent();
        intent.setQuery(text);
        intent.setNormalizedQuery(text);
        intent.setMaxPrice(extractBudget(text));
        intent.setCity(detectCity(text));
        intent.setWeekendOnly(containsAny(text, List.of("周末", "周六", "周日")));
        intent.setNeedActorFocus(containsAny(text, List.of("演员", "演技", "卡司")));

        int graphTagHits = matchGraphNodes(text, NodeType.TAG, intent.getTags());
        int graphTermHits = matchGraphNodes(text, NodeType.TERMINOLOGY, intent.getTerms());
        int aliasTagHits = matchAliasDictionary(text, TAG_KEYWORDS, intent.getTags());
        int aliasTermHits = matchAliasDictionary(text, TERM_KEYWORDS, intent.getTerms());

        if (text.contains("爱情剧")) {
            intent.getInferredTags().add("爱情");
            intent.getTags().add("爱情");
            if (!containsAny(text, List.of("话剧", "音乐剧"))) {
                intent.getInferredTags().add("话剧");
                intent.getTags().add("话剧");
            }
        }
        if (containsAny(text, List.of("适合约会", "约会看", "情侣看"))) {
            intent.getInferredTags().add("情侣适合");
            intent.getInferredTags().add("爱情");
            intent.getTags().add("情侣适合");
            intent.getTags().add("爱情");
        }
        if (containsAny(text, List.of("好哭", "虐一点", "虐", "催泪"))) {
            intent.getInferredTags().add("催泪");
            intent.getTags().add("催泪");
        }
        if (text.contains("悬疑")) {
            intent.getInferredTags().add("烧脑");
            intent.getTags().add("烧脑");
        }

        intent.getInferredTags().addAll(intent.getTags());
        intent.getInferredTerms().addAll(intent.getTerms());
        intent.setSummary(String.format(Locale.CHINA,
                "本地解析：真实图谱匹配标签 %d 个、术语 %d 个，别名补充标签 %d 个、术语 %d 个",
                graphTagHits, graphTermHits, aliasTagHits, aliasTermHits));
        return intent;
    }

    private int matchGraphNodes(String text, NodeType nodeType, Set<String> sink) {
        if (!StringUtils.hasText(text)) {
            return 0;
        }
        int before = sink.size();
        for (KnowledgeNode node : nodeRepository.findByNodeType(nodeType)) {
            if (matchesNodeText(text, node)) {
                sink.add(node.getName());
            }
        }
        return Math.max(0, sink.size() - before);
    }

    private boolean matchesNodeText(String text, KnowledgeNode node) {
        if (node == null || !StringUtils.hasText(node.getName())) {
            return false;
        }
        if (text.contains(node.getName())) {
            return true;
        }
        return extractNodeAliases(node).stream().anyMatch(alias -> text.contains(alias));
    }

    private Set<String> extractNodeAliases(KnowledgeNode node) {
        Set<String> aliases = new LinkedHashSet<>();
        collectAliasTokens(aliases, node.getExtraData());
        collectAliasTokens(aliases, node.getDescription());
        aliases.remove(node.getName());
        return aliases;
    }

    private void collectAliasTokens(Set<String> sink, String rawText) {
        if (!StringUtils.hasText(rawText)) {
            return;
        }
        for (String token : rawText.split("[,，;；/|、\\s]+")) {
            String normalized = token == null ? "" : token.trim();
            if (normalized.length() >= 2 && normalized.length() <= 20) {
                sink.add(normalized);
            }
        }
    }

    private int matchAliasDictionary(String text, Map<String, List<String>> dictionary, Set<String> sink) {
        int before = sink.size();
        for (Map.Entry<String, List<String>> entry : dictionary.entrySet()) {
            if (containsAny(text, entry.getValue())) {
                sink.add(entry.getKey());
            }
        }
        return Math.max(0, sink.size() - before);
    }

    private boolean containsAny(String text, Collection<String> words) {
        for (String word : words) {
            if (text.contains(word)) {
                return true;
            }
        }
        return false;
    }

    private BigDecimal extractBudget(String text) {
        StringBuilder digits = new StringBuilder();
        for (char ch : text.toCharArray()) {
            if (Character.isDigit(ch)) {
                digits.append(ch);
            } else if (digits.length() > 0) {
                break;
            }
        }
        return digits.isEmpty() ? null : new BigDecimal(digits.toString());
    }

    private String detectCity(String text) {
        for (String city : CITY_NORMALIZATION.keySet()) {
            if (text.contains(city)) {
                return city;
            }
        }
        return null;
    }

    private Map<Long, CandidatePlay> recallCandidates(QueryIntent intent, Long userId, RecallTrace trace) {
        Map<Long, CandidatePlay> map = new LinkedHashMap<>();
        collectByNodeNames(intent.getTags(), NodeType.TAG, 3.5, "命中标签：", map, trace.getTagHits());
        collectByNodeNames(intent.getTerms(), NodeType.TERMINOLOGY, 2.2, "相关术语：", map, trace.getTermHits());
        collectByUser(userId, map, trace);
        collectByRating(map);

        if (map.isEmpty()) {
            nodeRepository.findByNodeType(NodeType.PLAY).forEach(play -> map.put(play.getId(), CandidatePlay.from(play)));
        }
        trace.setCandidateCountBeforeFilter(map.size());
        return map;
    }

    private void collectByNodeNames(Set<String> names,
                                    NodeType type,
                                    double score,
                                    String reasonPrefix,
                                    Map<Long, CandidatePlay> map,
                                    Set<String> traceHitSink) {
        for (String name : names) {
            Optional<KnowledgeNode> sourceOpt = nodeRepository.findByNameAndNodeType(name, type);
            if (sourceOpt.isEmpty()) {
                continue;
            }
            traceHitSink.add(name);
            KnowledgeNode source = sourceOpt.get();
            for (KnowledgeEdge edge : edgeRepository.findEdgesByNodeId(source.getId())) {
                Long otherId = edge.getSourceNodeId().equals(source.getId()) ? edge.getTargetNodeId() : edge.getSourceNodeId();
                nodeRepository.findById(otherId).ifPresent(node -> {
                    if (node.getNodeType() != NodeType.PLAY) {
                        return;
                    }
                    CandidatePlay candidate = map.computeIfAbsent(node.getId(), id -> CandidatePlay.from(node));
                    candidate.graphScore += score;
                    candidate.reasons.add(reasonPrefix + name);
                    if (type == NodeType.TAG) {
                        candidate.matchedTags.add(name);
                    }
                    if (type == NodeType.TERMINOLOGY) {
                        candidate.matchedTerms.add(name);
                    }
                });
            }
        }
    }

    private void collectByUser(Long userId, Map<Long, CandidatePlay> map, RecallTrace trace) {
        if (userId == null) {
            return;
        }

        for (PlayRating rating : playRatingRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, 20))) {
            nodeRepository.findById(rating.getPlayId()).ifPresent(node -> {
                if (node.getNodeType() != NodeType.PLAY) {
                    return;
                }
                CandidatePlay candidate = map.computeIfAbsent(node.getId(), id -> CandidatePlay.from(node));
                candidate.preferenceScore += Math.max(0, rating.getRating()) * 0.8;
                candidate.reasons.add("历史高分偏好");
                trace.getUserSignals().add("历史评分");
            });
        }

        for (ActorLike like : actorLikeRepository.findByUserIdOrderByCreatedAtDesc(userId)) {
            for (KnowledgeEdge edge : edgeRepository.findEdgesByNodeId(like.getActorId())) {
                Long otherId = edge.getSourceNodeId().equals(like.getActorId()) ? edge.getTargetNodeId() : edge.getSourceNodeId();
                nodeRepository.findById(otherId).ifPresent(node -> {
                    if (node.getNodeType() != NodeType.PLAY) {
                        return;
                    }
                    CandidatePlay candidate = map.computeIfAbsent(node.getId(), id -> CandidatePlay.from(node));
                    candidate.preferenceScore += 3.0;
                    candidate.reasons.add("命中喜欢演员");
                    candidate.matchedActors.add(String.valueOf(like.getActorId()));
                    trace.getUserSignals().add("演员偏好");
                });
            }
        }
    }

    private void collectByRating(Map<Long, CandidatePlay> map) {
        nodeRepository.findByNodeType(NodeType.PLAY).forEach(node -> {
            CandidatePlay candidate = map.computeIfAbsent(node.getId(), id -> CandidatePlay.from(node));
            Double avg = playRatingRepository.getAverageRatingByPlayId(node.getId());
            if (avg != null) {
                candidate.ratingScore += avg;
            }
        });
    }

    private List<CandidatePlay> filterAndRank(Map<Long, CandidatePlay> map, QueryIntent intent) {
        List<Long> playIds = new ArrayList<>(map.keySet());
        if (playIds.isEmpty()) {
            return List.of();
        }

        List<Performance> performances = performanceRepository.searchAvailablePerformances(
                playIds,
                intent.getMaxPrice(),
                normalizeCity(intent.getCity()),
                intent.isWeekendOnly() ? weekendStart() : null
        );

        Map<Long, Performance> bestPerformance = performances.stream().collect(Collectors.toMap(
                Performance::getPlayId,
                performance -> performance,
                (a, b) -> a.getMinPrice().compareTo(b.getMinPrice()) <= 0 ? a : b,
                LinkedHashMap::new
        ));

        return map.values().stream()
                .peek(candidate -> candidate.performance = bestPerformance.get(candidate.playId))
                .filter(candidate -> candidate.performance != null)
                .filter(candidate -> !hasStrongGenreDemand(intent) || matchesGenreDemand(candidate, intent.getTags()))
                .peek(candidate -> score(candidate, intent))
                .sorted(Comparator.comparingDouble(CandidatePlay::totalScore).reversed())
                .toList();
    }

    private boolean hasStrongGenreDemand(QueryIntent intent) {
        return intent.getTags().stream().anyMatch(GENRE_TAGS::contains);
    }

    private boolean matchesGenreDemand(CandidatePlay candidate, Set<String> requestedTags) {
        Set<String> requestedGenres = requestedTags.stream().filter(GENRE_TAGS::contains).collect(Collectors.toSet());
        if (requestedGenres.isEmpty()) {
            return true;
        }

        String text = String.join(" ",
                candidate.playName == null ? "" : candidate.playName,
                candidate.description == null ? "" : candidate.description,
                String.join(" ", candidate.reasons)
        );
        return requestedGenres.stream().anyMatch(text::contains);
    }

    private void score(CandidatePlay candidate, QueryIntent intent) {
        if (intent.isNeedActorFocus()) {
            candidate.preferenceScore += 1.2;
        }
        if (intent.getCity() != null && candidate.performance != null
                && normalizeCity(intent.getCity()).equals(normalizeCity(candidate.performance.getCity()))) {
            candidate.ruleScore += 1.0;
        }
        if (intent.isWeekendOnly()) {
            candidate.ruleScore += 0.5;
        }
        if (intent.getMaxPrice() != null && candidate.performance != null) {
            candidate.ruleScore += candidate.performance.getMinPrice().compareTo(intent.getMaxPrice()) <= 0 ? 1.2 : -1.5;
        }
        for (String tag : candidate.matchedTags) {
            candidate.preferenceScore += TAG_BOOST.getOrDefault(tag, 0.0);
        }
        if (candidate.matchedTerms.contains("台词节奏") && intent.getTags().contains("台词密度高")) {
            candidate.preferenceScore += 0.4;
        }
        if (candidate.matchedTerms.contains("爆发戏") && intent.getTags().contains("演员爆发力强")) {
            candidate.preferenceScore += 0.4;
        }
        candidate.popularityScore += Math.min(2.5, candidate.reasons.size() * 0.4);
    }

    private Map<String, Object> toPlan(CandidatePlay candidate, QueryIntent intent) {
        Map<String, Object> result = new LinkedHashMap<>();
        Performance performance = candidate.performance;
        result.put("playId", candidate.playId);
        result.put("playName", candidate.playName);
        result.put("description", candidate.description);
        result.put("genreFocus", inferGenreFocus(intent));
        result.put("matchScore", String.format(Locale.CHINA, "%.1f", candidate.totalScore() * 10));
        result.put("venue", performance == null ? "待定" : performance.getVenueName());
        result.put("city", performance == null ? "待定" : performance.getCity());
        result.put("showTime", performance == null ? "待定" : performance.getStartTime().format(DF));
        result.put("priceRange", performance == null ? "待定" : "¥" + performance.getMinPrice() + "-" + performance.getMaxPrice());
        result.put("matchedTags", List.copyOf(candidate.matchedTags));
        result.put("matchedTerms", List.copyOf(candidate.matchedTerms));
        result.put("reason", candidate.reasons.isEmpty() ? "综合推荐" : String.join("；", candidate.reasons.stream().distinct().limit(5).toList()));
        result.put("analysis", buildAnalysis(candidate));
        result.put("scoreBreakdown", Map.of(
                "graphScore", round1(candidate.graphScore),
                "preferenceScore", round1(candidate.preferenceScore),
                "ratingScore", round1(candidate.ratingScore),
                "popularityScore", round1(candidate.popularityScore),
                "ruleScore", round1(candidate.ruleScore)
        ));
        return result;
    }

    private String inferGenreFocus(QueryIntent intent) {
        if (intent.getTags().contains("音乐剧")) {
            return "音乐剧";
        }
        if (intent.getTags().contains("话剧")) {
            return "话剧";
        }
        return "话剧/音乐剧";
    }

    private String buildAnalysis(CandidatePlay candidate) {
        List<String> parts = new ArrayList<>();
        if (!candidate.matchedTags.isEmpty()) {
            parts.add("命中标签 " + String.join("、", candidate.matchedTags));
        }
        if (!candidate.matchedTerms.isEmpty()) {
            parts.add("命中术语 " + String.join("、", candidate.matchedTerms));
        }
        if (candidate.performance != null) {
            parts.add("存在真实在售场次");
        }
        if (candidate.ratingScore > 0) {
            parts.add("结合了评分信号");
        }
        if (parts.isEmpty()) {
            parts.add("综合规则与语义召回得到");
        }
        return String.join("；", parts) + "。";
    }

    private LocalDateTime weekendStart() {
        LocalDate today = LocalDate.now();
        int delta = DayOfWeek.SATURDAY.getValue() - today.getDayOfWeek().getValue();
        if (delta < 0) {
            delta += 7;
        }
        return today.plusDays(delta).atStartOfDay();
    }

    private double round1(double value) {
        return Math.round(value * 10) / 10.0;
    }

    private String normalizeCity(String city) {
        if (!StringUtils.hasText(city)) {
            return city;
        }
        return CITY_NORMALIZATION.getOrDefault(city, city);
    }

    private static class CandidatePlay {
        Long playId;
        String playName;
        String description;
        Performance performance;
        double graphScore;
        double preferenceScore;
        double ratingScore;
        double popularityScore;
        double ruleScore;
        List<String> reasons = new ArrayList<>();
        Set<String> matchedTags = new LinkedHashSet<>();
        Set<String> matchedTerms = new LinkedHashSet<>();
        Set<String> matchedActors = new LinkedHashSet<>();

        static CandidatePlay from(KnowledgeNode node) {
            CandidatePlay candidate = new CandidatePlay();
            candidate.playId = node.getId();
            candidate.playName = node.getName();
            candidate.description = node.getDescription();
            return candidate;
        }

        double totalScore() {
            return graphScore + preferenceScore + ratingScore + popularityScore + ruleScore;
        }
    }

    @Data
    public static class QueryIntent {
        private String query;
        private String normalizedQuery;
        private BigDecimal maxPrice;
        private String city;
        private boolean weekendOnly;
        private boolean needActorFocus;
        private String summary;
        private boolean llmEnabled;
        private boolean fallbackUsed;
        private Set<String> tags = new LinkedHashSet<>();
        private Set<String> terms = new LinkedHashSet<>();
        private Set<String> inferredTags = new LinkedHashSet<>();
        private Set<String> inferredTerms = new LinkedHashSet<>();

        public Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("query", query);
            map.put("normalizedQuery", normalizedQuery);
            map.put("maxPrice", maxPrice);
            map.put("city", city);
            map.put("weekendOnly", weekendOnly);
            map.put("needActorFocus", needActorFocus);
            map.put("summary", summary);
            map.put("llmEnabled", llmEnabled);
            map.put("fallbackUsed", fallbackUsed);
            map.put("tags", List.copyOf(tags));
            map.put("terms", List.copyOf(terms));
            map.put("inferredTags", List.copyOf(inferredTags));
            map.put("inferredTerms", List.copyOf(inferredTerms));
            return map;
        }
    }

    @Data
    private static class RecallTrace {
        private Set<String> tagHits = new LinkedHashSet<>();
        private Set<String> termHits = new LinkedHashSet<>();
        private Set<String> userSignals = new LinkedHashSet<>();
        private int candidateCountBeforeFilter;

        Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("tagHits", List.copyOf(tagHits));
            map.put("termHits", List.copyOf(termHits));
            map.put("userSignals", List.copyOf(userSignals));
            map.put("candidateCountBeforeFilter", candidateCountBeforeFilter);
            return map;
        }
    }
}
