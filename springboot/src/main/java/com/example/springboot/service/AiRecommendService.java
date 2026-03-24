package com.example.springboot.service;

import com.example.springboot.dto.MockPlayProfile;
import com.example.springboot.dto.MockUserPreference;
import com.example.springboot.dto.RecommendItem;
import com.example.springboot.dto.RecommendRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Day4 推荐算法 MVP（混合策略）
 * 1) 内容匹配（类型/标签/演员）
 * 2) 近邻用户协同信号（基于 Day3 mock 用户偏好）
 * 3) 热度与评分质量
 * 4) 预算匹配
 * 5) MMR 多样性重排
 */
@Service
public class AiRecommendService {

    private final MockRecommendationDataService mockDataService;

    public AiRecommendService(MockRecommendationDataService mockDataService) {
        this.mockDataService = mockDataService;
    }

    public Map<String, Object> recommend(RecommendRequest req) {
        List<MockPlayProfile> plays = mockDataService.getPlays();
        List<MockUserPreference> users = mockDataService.getUserPreferences();
        if (plays.isEmpty()) {
            return Map.of("items", List.of(), "strategy", "hybrid-v1", "message", "暂无可推荐数据");
        }

        int topK = normalizeTopK(req.getTopK());
        QueryIntent intent = parseIntent(req.getQuery(), plays);
        MockUserPreference currentUser = resolveUser(req.getUserId(), users);

        // 1) 先计算基础分：内容匹配 + 协同信号 + 质量热度 + 预算匹配
        List<Candidate> candidates = new ArrayList<>();
        for (MockPlayProfile p : plays) {
            ScoreParts sp = scorePlay(p, intent, currentUser, users, plays);
            candidates.add(new Candidate(p, sp));
        }

        // 2) MMR 多样性重排：避免 topN 全是同一风格
        List<Candidate> reranked = mmrRerank(candidates, topK);

        List<RecommendItem> items = reranked.stream()
                .map(c -> toRecommendItem(c.play, c.parts))
                .toList();

        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("userId", currentUser == null ? null : currentUser.getUserId());
        profile.put("query", StringUtils.hasText(req.getQuery()) ? req.getQuery() : "");
        profile.put("targetGenres", intent.targetGenres);
        profile.put("targetTags", intent.targetTags);
        profile.put("targetActor", intent.targetActor);
        profile.put("budget", Map.of("min", intent.budgetMin, "max", intent.budgetMax));

        return Map.of(
                "strategy", "hybrid-content-cf-mmr-v1",
                "items", items,
                "profile", profile
        );
    }

    private MockUserPreference resolveUser(Long userId, List<MockUserPreference> users) {
        if (userId == null) return null;
        return users.stream().filter(u -> Objects.equals(u.getUserId(), userId)).findFirst().orElse(null);
    }

    private int normalizeTopK(Integer topK) {
        if (topK == null) return 5;
        return Math.max(1, Math.min(10, topK));
    }

    private QueryIntent parseIntent(String query, List<MockPlayProfile> plays) {
        QueryIntent intent = new QueryIntent();
        String q = StringUtils.hasText(query) ? query.trim() : "";

        Set<String> genres = plays.stream().map(MockPlayProfile::getGenre).collect(Collectors.toSet());
        Set<String> tags = plays.stream().flatMap(p -> p.getTags().stream()).collect(Collectors.toSet());
        Set<String> actors = plays.stream().map(MockPlayProfile::getLeadActor).collect(Collectors.toSet());

        for (String g : genres) {
            if (q.contains(g)) intent.targetGenres.add(g);
        }
        for (String t : tags) {
            if (q.contains(t)) intent.targetTags.add(t);
        }
        for (String a : actors) {
            if (q.contains(a)) {
                intent.targetActor = a;
                break;
            }
        }

        // 常见表达映射
        if (q.contains("梦幻")) intent.targetTags.add("梦幻");
        if (q.contains("魔法")) intent.targetTags.add("奇幻");
        if (q.contains("沉浸")) intent.targetGenres.add("沉浸式戏剧");

        int[] budget = extractBudget(q);
        intent.budgetMin = budget[0];
        intent.budgetMax = budget[1];
        return intent;
    }

    private int[] extractBudget(String query) {
        if (!StringUtils.hasText(query)) return new int[]{0, 2000};
        Matcher m = Pattern.compile("(\\d{2,4})").matcher(query);
        List<Integer> nums = new ArrayList<>();
        while (m.find()) nums.add(Integer.parseInt(m.group(1)));
        if (nums.isEmpty()) return new int[]{0, 2000};
        if (nums.size() == 1) return new int[]{0, nums.get(0)};
        int a = Math.min(nums.get(0), nums.get(1));
        int b = Math.max(nums.get(0), nums.get(1));
        return new int[]{a, b};
    }

    private ScoreParts scorePlay(MockPlayProfile p,
                                 QueryIntent intent,
                                 MockUserPreference currentUser,
                                 List<MockUserPreference> users,
                                 List<MockPlayProfile> plays) {
        ScoreParts parts = new ScoreParts();

        // A. 内容匹配（0-45）
        if (!intent.targetGenres.isEmpty() && intent.targetGenres.contains(p.getGenre())) {
            parts.content += 20.0;
            parts.reasons.add("类型匹配：" + p.getGenre());
        }

        long tagMatchByQuery = p.getTags().stream().filter(intent.targetTags::contains).count();
        parts.content += Math.min(15.0, tagMatchByQuery * 5.0);
        if (tagMatchByQuery > 0) {
            parts.reasons.add("风格标签贴合");
        }

        if (StringUtils.hasText(intent.targetActor) && intent.targetActor.equals(p.getLeadActor())) {
            parts.content += 10.0;
            parts.reasons.add("主演偏好匹配");
        }

        // 用户画像加权
        if (currentUser != null) {
            if (currentUser.getLikedGenres().contains(p.getGenre())) {
                parts.content += 8.0;
                parts.reasons.add("命中用户偏好类型");
            }
            long userTagMatch = p.getTags().stream().filter(currentUser.getLikedTags()::contains).count();
            parts.content += Math.min(12.0, userTagMatch * 4.0);
            if (userTagMatch > 0) {
                parts.reasons.add("命中用户偏好标签");
            }
            if (StringUtils.hasText(currentUser.getFavoriteActor()) && currentUser.getFavoriteActor().equals(p.getLeadActor())) {
                parts.content += 8.0;
                parts.reasons.add("命中用户喜爱演员");
            }
        }
        parts.content = Math.min(45.0, parts.content);

        // B. 协同信号（0-20）：基于相似用户近期浏览
        parts.collaborative = collaborativeScore(p, currentUser, users);
        if (parts.collaborative >= 6) {
            parts.reasons.add("相似用户近期关注");
        }

        // C. 质量热度（0-20）
        double ratingNorm = Math.max(0, Math.min(1, (p.getRating() - 8.0) / 1.5)); // 8.0~9.5
        double heatNorm = Math.max(0, Math.min(1, (p.getHeat() - 70.0) / 30.0));   // 70~100
        parts.qualityPopularity = 12.0 * ratingNorm + 8.0 * heatNorm;
        if (parts.qualityPopularity >= 14) {
            parts.reasons.add("口碑与热度较高");
        }

        // D. 预算匹配（0-10）
        int bMin = currentUser == null ? intent.budgetMin : currentUser.getBudgetMin();
        int bMax = currentUser == null ? intent.budgetMax : currentUser.getBudgetMax();
        parts.budget = budgetFitScore(p, bMin, bMax);
        if (parts.budget >= 7) {
            parts.reasons.add("价格区间匹配");
        }

        parts.baseScore = parts.content + parts.collaborative + parts.qualityPopularity + parts.budget;
        return parts;
    }

    private double collaborativeScore(MockPlayProfile play, MockUserPreference currentUser, List<MockUserPreference> users) {
        if (currentUser == null) return 0;
        List<UserSim> sims = new ArrayList<>();
        for (MockUserPreference u : users) {
            if (Objects.equals(u.getUserId(), currentUser.getUserId())) continue;
            double sim = userSimilarity(currentUser, u);
            if (sim > 0.05) sims.add(new UserSim(u, sim));
        }
        sims.sort((a, b) -> Double.compare(b.sim, a.sim));
        if (sims.isEmpty()) return 0;

        double numerator = 0;
        double denominator = 0;
        for (int i = 0; i < Math.min(12, sims.size()); i++) {
            UserSim s = sims.get(i);
            double implicit = s.user.getRecentlyViewedPlayIds().contains(play.getPlayId()) ? 1.0 : 0.0;
            // 若不是直接看过，也给同类型一点弱信号
            if (implicit == 0 && s.user.getLikedGenres().contains(play.getGenre())) {
                implicit = 0.45;
            }
            numerator += s.sim * implicit;
            denominator += s.sim;
        }
        if (denominator == 0) return 0;
        return 20.0 * (numerator / denominator);
    }

    private double userSimilarity(MockUserPreference a, MockUserPreference b) {
        double genreSim = jaccard(a.getLikedGenres(), b.getLikedGenres());
        double tagSim = jaccard(a.getLikedTags(), b.getLikedTags());
        double recentSim = jaccardLong(a.getRecentlyViewedPlayIds(), b.getRecentlyViewedPlayIds());
        return 0.45 * genreSim + 0.35 * tagSim + 0.20 * recentSim;
    }

    private double budgetFitScore(MockPlayProfile p, int budgetMin, int budgetMax) {
        int left = Math.max(p.getPriceMin(), budgetMin);
        int right = Math.min(p.getPriceMax(), budgetMax);
        if (left <= right) return 10.0;

        // 不重叠时按距离衰减
        int distance = Math.min(Math.abs(p.getPriceMin() - budgetMax), Math.abs(p.getPriceMax() - budgetMin));
        return Math.max(0, 10.0 - distance / 100.0);
    }

    private List<Candidate> mmrRerank(List<Candidate> candidates, int topK) {
        List<Candidate> pool = new ArrayList<>(candidates);
        pool.sort((a, b) -> Double.compare(b.parts.baseScore, a.parts.baseScore));

        List<Candidate> selected = new ArrayList<>();
        while (!pool.isEmpty() && selected.size() < topK) {
            Candidate best = null;
            double bestMmr = -1e9;
            for (Candidate c : pool) {
                double simMax = 0;
                for (Candidate s : selected) {
                    simMax = Math.max(simMax, playSimilarity(c.play, s.play));
                }
                double mmr = 0.82 * c.parts.baseScore - 0.18 * (simMax * 40.0);
                if (mmr > bestMmr) {
                    bestMmr = mmr;
                    best = c;
                }
            }
            if (best == null) break;
            best.parts.diversityPenalty = Math.max(0, best.parts.baseScore - bestMmr);
            best.parts.finalScore = Math.max(0, bestMmr);
            selected.add(best);
            pool.remove(best);
        }

        // 没进入 mmr 的也补 finalScore（保底）
        for (Candidate c : selected) {
            if (c.parts.finalScore == 0) c.parts.finalScore = c.parts.baseScore;
        }
        return selected;
    }

    private double playSimilarity(MockPlayProfile a, MockPlayProfile b) {
        double genre = a.getGenre().equals(b.getGenre()) ? 1.0 : 0.0;
        double tags = jaccard(a.getTags(), b.getTags());
        return 0.6 * genre + 0.4 * tags;
    }

    private double jaccard(List<String> a, List<String> b) {
        Set<String> sa = new HashSet<>(a);
        Set<String> sb = new HashSet<>(b);
        if (sa.isEmpty() && sb.isEmpty()) return 0;
        Set<String> inter = new HashSet<>(sa);
        inter.retainAll(sb);
        Set<String> union = new HashSet<>(sa);
        union.addAll(sb);
        return union.isEmpty() ? 0 : (double) inter.size() / union.size();
    }

    private double jaccardLong(List<Long> a, List<Long> b) {
        Set<Long> sa = new HashSet<>(a);
        Set<Long> sb = new HashSet<>(b);
        if (sa.isEmpty() && sb.isEmpty()) return 0;
        Set<Long> inter = new HashSet<>(sa);
        inter.retainAll(sb);
        Set<Long> union = new HashSet<>(sa);
        union.addAll(sb);
        return union.isEmpty() ? 0 : (double) inter.size() / union.size();
    }

    private RecommendItem toRecommendItem(MockPlayProfile p, ScoreParts sp) {
        Map<String, Double> breakdown = new LinkedHashMap<>();
        breakdown.put("content", round2(sp.content));
        breakdown.put("collaborative", round2(sp.collaborative));
        breakdown.put("qualityPopularity", round2(sp.qualityPopularity));
        breakdown.put("budget", round2(sp.budget));
        breakdown.put("diversityPenalty", round2(sp.diversityPenalty));
        breakdown.put("baseScore", round2(sp.baseScore));
        breakdown.put("finalScore", round2(sp.finalScore));

        // 去重推荐理由
        List<String> reasons = sp.reasons.stream().distinct().limit(4).toList();
        if (reasons.isEmpty()) reasons = List.of("综合匹配度较高");

        return new RecommendItem(
                p.getPlayId(),
                p.getPlayName(),
                p.getGenre(),
                p.getTags(),
                p.getLeadActor(),
                p.getRating(),
                p.getHeat(),
                p.getPriceMin(),
                p.getPriceMax(),
                round2(sp.finalScore > 0 ? sp.finalScore : sp.baseScore),
                breakdown,
                reasons
        );
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private static class QueryIntent {
        Set<String> targetGenres = new LinkedHashSet<>();
        Set<String> targetTags = new LinkedHashSet<>();
        String targetActor;
        int budgetMin = 0;
        int budgetMax = 2000;
    }

    private static class ScoreParts {
        double content;
        double collaborative;
        double qualityPopularity;
        double budget;
        double diversityPenalty;
        double baseScore;
        double finalScore;
        List<String> reasons = new ArrayList<>();
    }

    private record Candidate(MockPlayProfile play, ScoreParts parts) {}

    private record UserSim(MockUserPreference user, double sim) {}
}
