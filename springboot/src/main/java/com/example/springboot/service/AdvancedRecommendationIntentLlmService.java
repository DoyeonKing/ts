package com.example.springboot.service;

import com.example.springboot.config.AiLlmProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdvancedRecommendationIntentLlmService {

    private final AiLlmProperties llmProperties;
    private final ObjectMapper objectMapper;
    private final AdvancedTheaterRecommendationService advancedTheaterRecommendationService;

    public AdvancedTheaterRecommendationService.QueryIntent parseIntent(String query) {
        AdvancedTheaterRecommendationService.QueryIntent fallback = advancedTheaterRecommendationService.parseIntent(query);
        fallback.setLlmEnabled(false);
        fallback.setFallbackUsed(true);

        if (!isConfigured()) {
            return fallback;
        }

        try {
            String content = callChatCompletions(buildSystemPrompt(), buildUserPrompt(query));
            JsonNode root = objectMapper.readTree(normalizeJson(content));
            AdvancedTheaterRecommendationService.QueryIntent intent = buildIntentFromJson(query, root);
            intent.setLlmEnabled(true);
            intent.setFallbackUsed(false);
            return intent;
        } catch (Exception ex) {
            fallback.setSummary("LLM 意图解析失败，已回退本地规则：" + ex.getMessage());
            return fallback;
        }
    }

    public boolean isConfigured() {
        return llmProperties.isEnabled()
                && StringUtils.hasText(llmProperties.getApiKey())
                && StringUtils.hasText(llmProperties.getBaseUrl())
                && StringUtils.hasText(llmProperties.getModel());
    }

    private AdvancedTheaterRecommendationService.QueryIntent buildIntentFromJson(String query, JsonNode root) {
        AdvancedTheaterRecommendationService.QueryIntent intent = new AdvancedTheaterRecommendationService.QueryIntent();
        intent.setQuery(query == null ? "" : query.trim());
        intent.setNormalizedQuery(readText(root, "normalizedQuery", intent.getQuery()));
        intent.setCity(readNullableText(root, "city"));
        intent.setSummary(readText(root, "summary", "LLM 前置意图解析"));
        intent.setWeekendOnly(root.path("weekendOnly").asBoolean(false));
        intent.setNeedActorFocus(root.path("needActorFocus").asBoolean(false));
        intent.setMaxPrice(readBudget(root.path("budget")));

        Set<String> inferredTags = new LinkedHashSet<>();
        inferredTags.addAll(readStringSet(root.path("inferredTags")));
        inferredTags.addAll(readStringSet(root.path("genreHints")));
        inferredTags.addAll(readStringSet(root.path("themeTags")));

        Set<String> inferredTerms = new LinkedHashSet<>();
        inferredTerms.addAll(readStringSet(root.path("inferredTerms")));
        inferredTerms.addAll(readStringSet(root.path("softConstraints")));
        inferredTerms.addAll(readStringSet(root.path("actorFocus")));

        intent.getTags().addAll(inferredTags);
        intent.getTerms().addAll(inferredTerms);
        intent.getInferredTags().addAll(inferredTags);
        intent.getInferredTerms().addAll(inferredTerms);
        return intent;
    }

    private String buildSystemPrompt() {
        return """
                你是剧场垂直推荐系统中的前置意图理解器。
                你的任务是把用户自然语言需求解析成结构化意图，帮助后续数据库召回真实在售候选。
                你可以做口语归一化、弱条件补全、主题理解，但不能编造真实场次、票价、剧目或演员事实。
                输出必须是 JSON，不要输出 markdown，不要补充额外解释。
                尽量把口语化表达归一到现有剧场推荐标签，例如：话剧、音乐剧、爱情、经典、新手友好、情侣适合、催泪、烧脑、卡司强、台词密度高、情绪张力强、唱段突出、舞段突出、适合二刷、性价比高、300元内可选。
                术语尽量归一到：独白、台词节奏、爆发戏、对手戏、调度、走位、卡司、返场、重唱、舞段。
                """;
    }

    private String buildUserPrompt(String query) throws Exception {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("query", query == null ? "" : query.trim());

        ArrayNode tagExamples = payload.putArray("validTagExamples");
        for (String value : List.of("话剧", "音乐剧", "爱情", "经典", "新手友好", "情侣适合", "催泪",
                "烧脑", "卡司强", "台词密度高", "情绪张力强", "唱段突出", "舞段突出", "适合二刷", "性价比高", "300元内可选")) {
            tagExamples.add(value);
        }

        ArrayNode termExamples = payload.putArray("validTermExamples");
        for (String value : List.of("独白", "台词节奏", "爆发戏", "对手戏", "调度", "走位", "卡司", "返场", "重唱", "舞段")) {
            termExamples.add(value);
        }

        return """
                请解析这个用户需求，并只输出 JSON。
                需要包含这些字段：
                normalizedQuery: string
                inferredTags: string[]
                inferredTerms: string[]
                city: string|null
                budget: number|null
                weekendOnly: boolean
                needActorFocus: boolean
                summary: string

                如果你能明确判断，也可以额外输出：
                genreHints: string[]
                themeTags: string[]
                softConstraints: string[]
                actorFocus: string[]

                用户输入与可参考标签如下：
                """ + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(payload);
    }

    private String callChatCompletions(String system, String user) throws Exception {
        String base = llmProperties.getBaseUrl().replaceAll("/+$", "");
        String url = base + "/chat/completions";

        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", llmProperties.getModel());
        ArrayNode messages = body.putArray("messages");
        ObjectNode m0 = messages.addObject();
        m0.put("role", "system");
        m0.put("content", system);
        ObjectNode m1 = messages.addObject();
        m1.put("role", "user");
        m1.put("content", user);

        int connectSec = Math.max(15, llmProperties.getConnectTimeoutSeconds());
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(connectSec))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(Math.max(connectSec, llmProperties.getTimeoutSeconds())))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + llmProperties.getApiKey())
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("HTTP " + response.statusCode() + ": " + response.body());
        }

        JsonNode root = objectMapper.readTree(response.body());
        JsonNode err = root.path("error");
        if (!err.isMissingNode() && err.path("message").isTextual()) {
            throw new IllegalStateException(err.path("message").asText());
        }
        return root.path("choices").path(0).path("message").path("content").asText(null);
    }

    private String normalizeJson(String content) {
        if (!StringUtils.hasText(content)) {
            return "{}";
        }
        String text = content.trim();
        if (text.startsWith("```") && text.endsWith("```")) {
            text = text.replaceFirst("^```[a-zA-Z]*", "").replaceFirst("```$", "").trim();
        }
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return "{}";
    }

    private Set<String> readStringSet(JsonNode node) {
        Set<String> values = new LinkedHashSet<>();
        if (node != null && node.isArray()) {
            for (JsonNode item : node) {
                if (item != null && item.isTextual() && StringUtils.hasText(item.asText())) {
                    values.add(item.asText().trim());
                }
            }
        }
        return values;
    }

    private BigDecimal readBudget(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isNumber()) {
            return node.decimalValue();
        }
        if (node.isTextual() && StringUtils.hasText(node.asText())) {
            try {
                return new BigDecimal(node.asText().trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private String readText(JsonNode root, String fieldName, String defaultValue) {
        String value = readNullableText(root, fieldName);
        return StringUtils.hasText(value) ? value : defaultValue;
    }

    private String readNullableText(JsonNode root, String fieldName) {
        JsonNode node = root.path(fieldName);
        if (node.isTextual() && StringUtils.hasText(node.asText())) {
            return node.asText().trim();
        }
        return null;
    }
}
