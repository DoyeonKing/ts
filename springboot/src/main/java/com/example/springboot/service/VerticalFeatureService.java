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
        if (softTags.isEmpty()) {
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
        result.put("engine", "local-lora-style-engine");
        result.put("query", text);
        result.put("softTags", List.copyOf(softTags));
        result.put("actorTraits", List.copyOf(actorTraits));
        result.put("reviewKeywords", List.copyOf(reviewKeywords));
        result.put("analysisPoints", analysisPoints);
        result.put("generatedReview", buildReview(text, softTags, actorTraits, reviewKeywords));
        result.put("summary", "垂直特征模块已提取演员特征、剧种偏好和专业评论关键词，可用于后续图谱召回与RAG上下文拼装。");
        return result;
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
