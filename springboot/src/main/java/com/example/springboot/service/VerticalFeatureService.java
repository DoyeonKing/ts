package com.example.springboot.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class VerticalFeatureService {

    public Map<String, Object> generateTextFeatures(String query) {
        String text = query == null ? "" : query.trim();
        Set<String> softTags = new LinkedHashSet<>();
        Set<String> actorTraits = new LinkedHashSet<>();
        Set<String> reviewKeywords = new LinkedHashSet<>();
        List<String> analysisPoints = new ArrayList<>();

        if (containsAny(text, "演技", "演员", "爆发力", "张力")) {
            actorTraits.add("舞台张力强");
            actorTraits.add("情绪爆发力突出");
            reviewKeywords.add("人物情绪推进");
            reviewKeywords.add("高强度表演段落");
            analysisPoints.add("优先关注具备高情绪张力和戏剧冲突的演员表演。");
        }
        if (containsAny(text, "话剧", "台词", "对白")) {
            softTags.add("话剧");
            reviewKeywords.add("台词控制力");
            analysisPoints.add("重点强调台词节奏、人物关系与舞台调度。");
        }
        if (containsAny(text, "悲剧", "莎士比亚", "经典")) {
            softTags.add("悲剧");
            softTags.add("经典");
            reviewKeywords.add("戏剧冲突密度");
            analysisPoints.add("从经典文本与戏剧结构角度补充赏析。");
        }
        if (containsAny(text, "爱情", "浪漫")) {
            softTags.add("爱情");
            reviewKeywords.add("情感表达层次");
            analysisPoints.add("优先描述人物情感递进与关系张力。");
        }
        if (containsAny(text, "音乐剧")) {
            softTags.add("音乐剧");
            reviewKeywords.add("唱段表现");
            analysisPoints.add("优先关注唱演结合、叙事节奏与舞台调度。");
        }
        if (containsAny(text, "歌剧")) {
            softTags.add("歌剧");
            reviewKeywords.add("咏叹调表现");
            analysisPoints.add("增加声乐表现与音乐戏剧性的分析。");
        }
        if (containsAny(text, "芭蕾")) {
            softTags.add("芭蕾");
            reviewKeywords.add("肢体叙事");
            analysisPoints.add("突出身体表达、动作线条与舞台美学。");
        }
        if (softTags.isEmpty() && !containsAny(text, "北京", "上海", "广州", "深圳", "杭州", "南京", "成都", "武汉", "西安", "重庆", "天津", "苏州")) {
            softTags.add("经典");
        }
        if (actorTraits.isEmpty()) {
            actorTraits.add("舞台完成度高");
        }
        if (reviewKeywords.isEmpty()) {
            reviewKeywords.add("角色塑造");
            reviewKeywords.add("戏剧节奏");
        }
        if (analysisPoints.isEmpty()) {
            analysisPoints.add("结合用户需求补充剧目主题、表演亮点与观演价值分析。");
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("engine", "vertical-feature-engine");
        result.put("query", text);
        result.put("softTags", List.copyOf(softTags));
        result.put("actorTraits", List.copyOf(actorTraits));
        result.put("reviewKeywords", List.copyOf(reviewKeywords));
        result.put("analysisPoints", analysisPoints);
        result.put("generatedReview", buildReview(text, softTags, actorTraits, reviewKeywords));
        result.put("summary", "垂直特征模块已提取演员特征、剧种偏好和专业评论关键词，可用于后续图谱召回与RAG上下文拼装。");
        return result;
    }

    public Map<String, Object> generateCreativeSuggestions(String query) {
        String text = query == null ? "" : query.trim();
        String theme = detectTheme(text);
        String mood = detectMood(text);
        String audience = detectAudience(text);

        List<Map<String, Object>> items = new ArrayList<>();
        items.add(buildCreativeItem("T恤", theme + "台词感图形T恤", "胸前主图使用剧目意象 + 两行短句排版，背面用小字号印角色关键词。", "便于日常穿着传播，适合年轻观众二次分享。", "把情绪穿在身上"));
        items.add(buildCreativeItem("帆布袋", theme + "场景帆布袋", "正面做舞台透视线稿，侧边加场馆坐标与演出日期。", "低门槛、高实用，适合剧场出行和通勤。", "把剧场带回生活"));
        items.add(buildCreativeItem("票根卡", theme + "纪念票根卡", "保留票根信息样式，叠加角色关系图和一句金句。", "强化观演纪念属性，适合打卡收藏。", "这一场值得留档"));
        items.add(buildCreativeItem("明信片", theme + "情绪明信片", "正面为角色剪影与灯光色块，背面提供手写寄语区。", "适合作为周边礼包或社群活动物料。", "把戏寄给未来的自己"));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("theme", theme);
        result.put("mood", mood);
        result.put("audience", audience);
        result.put("items", items);
        result.put("creativeBrief", "建议以“" + theme + "”为主线，视觉语气偏“" + mood + "”，优先覆盖“" + audience + "”人群。");
        return result;
    }

    private Map<String, Object> buildCreativeItem(String type, String name, String designIdea, String reason, String slogan) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("type", type);
        item.put("name", name);
        item.put("designIdea", designIdea);
        item.put("reason", reason);
        item.put("slogan", slogan);
        return item;
    }

    private String detectTheme(String text) {
        if (containsAny(text, "话剧")) return "话剧";
        if (containsAny(text, "歌剧")) return "歌剧";
        if (containsAny(text, "芭蕾")) return "芭蕾";
        if (containsAny(text, "音乐剧")) return "音乐剧";
        if (containsAny(text, "悲剧")) return "悲剧";
        return "剧场";
    }

    private String detectMood(String text) {
        if (containsAny(text, "张力", "爆发力", "冲突")) return "高张力";
        if (containsAny(text, "爱情", "浪漫")) return "情感细腻";
        if (containsAny(text, "经典")) return "经典沉稳";
        return "沉浸叙事";
    }

    private String detectAudience(String text) {
        if (containsAny(text, "第一次", "入坑", "新手")) return "新观众";
        if (containsAny(text, "演员", "演技")) return "表演细节爱好者";
        return "剧场兴趣人群";
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String buildReview(String text, Set<String> softTags, Set<String> actorTraits, Set<String> reviewKeywords) {
        return "针对用户需求“" + text + "”，系统优先抽取出“"
                + String.join("、", softTags)
                + "”等剧目软标签，并将“"
                + String.join("、", actorTraits)
                + "”作为演员表演侧重点；在评论维度上重点关注“"
                + String.join("、", reviewKeywords)
                + "”，用于生成更贴近剧场语境的专业赏析。";
    }
}
