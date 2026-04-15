package com.example.springboot.service;

import com.example.springboot.config.AiLlmProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ConstrainedRecommendationLlmService {

    private final AiLlmProperties llmProperties;
    private final ObjectMapper objectMapper;

    public Map<String, Object> selectFromCandidates(String query,
                                                    Map<String, Object> intent,
                                                    List<Map<String, Object>> candidatePlans) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("llmEnabled", false);
        result.put("selectedPlayIds", List.of());
        result.put("summary", "");
        result.put("reasoning", List.of());

        if (!isConfigured() || candidatePlans == null || candidatePlans.isEmpty()) {
            return result;
        }

        try {
            String system = """
                    你是剧场推荐系统中的“受约束候选选择器”。
                    你只能在给定候选里做选择、排序和解释，禁止编造不存在的剧目、场次、票价、城市、场馆。
                    你必须严格依据候选事实数据作答。
                    你只输出 JSON，不要输出 markdown，不要解释规则。
                    """;

            String user = buildUserPrompt(query, intent, candidatePlans);
            String content = callChatCompletions(system, user);
            String json = normalizeJson(content);
            JsonNode root = objectMapper.readTree(json);

            List<Long> selectedPlayIds = readLongList(root.path("selectedPlayIds"));
            Set<Long> validIds = new LinkedHashSet<>();
            for (Map<String, Object> plan : candidatePlans) {
                Object playId = plan.get("playId");
                if (playId instanceof Number number) {
                    validIds.add(number.longValue());
                }
            }

            List<Long> filteredIds = new ArrayList<>();
            for (Long id : selectedPlayIds) {
                if (id != null && validIds.contains(id) && !filteredIds.contains(id)) {
                    filteredIds.add(id);
                }
            }

            result.put("llmEnabled", true);
            result.put("selectedPlayIds", filteredIds);
            result.put("summary", root.path("summary").asText(""));
            result.put("reasoning", readStringList(root.path("reasoning")));
            return result;
        } catch (Exception e) {
            result.put("llmEnabled", false);
            result.put("llmError", e.getMessage());
            return result;
        }
    }

    public boolean isConfigured() {
        return llmProperties.isEnabled()
                && StringUtils.hasText(llmProperties.getApiKey())
                && StringUtils.hasText(llmProperties.getBaseUrl())
                && StringUtils.hasText(llmProperties.getModel());
    }

    private String buildUserPrompt(String query,
                                   Map<String, Object> intent,
                                   List<Map<String, Object>> candidatePlans) throws Exception {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("query", safe(query));
        payload.set("intent", objectMapper.valueToTree(intent == null ? Map.of() : intent));
        ArrayNode candidates = payload.putArray("candidates");

        int limit = Math.min(candidatePlans.size(), 8);
        for (int i = 0; i < limit; i++) {
            Map<String, Object> plan = candidatePlans.get(i);
            ObjectNode node = candidates.addObject();
            node.put("playId", toLong(plan.get("playId")));
            node.put("playName", safe(String.valueOf(plan.getOrDefault("playName", ""))));
            node.put("description", safe(String.valueOf(plan.getOrDefault("description", ""))));
            node.put("city", safe(String.valueOf(plan.getOrDefault("city", ""))));
            node.put("venue", safe(String.valueOf(plan.getOrDefault("venue", ""))));
            node.put("showTime", safe(String.valueOf(plan.getOrDefault("showTime", ""))));
            node.put("priceRange", safe(String.valueOf(plan.getOrDefault("priceRange", ""))));
            node.put("reason", safe(String.valueOf(plan.getOrDefault("reason", ""))));
            node.put("analysis", safe(String.valueOf(plan.getOrDefault("analysis", ""))));
            node.put("matchScore", safe(String.valueOf(plan.getOrDefault("matchScore", ""))));
        }

        return """
                请从下面给定候选中，选择最符合用户需求的前 3-5 个候选并排序。
                规则：
                1. 只能从 candidates 中选择。
                2. 禁止编造新剧目、新场次、新票价、新场馆。
                3. 优先考虑用户需求、城市/预算/时间约束、用户历史偏好，以及候选中的真实事实。
                4. 如果候选本身都可行，就按“更适合用户”的顺序排序。
                输出 JSON，字段必须包含：
                selectedPlayIds: number[]
                summary: string
                reasoning: string[]

                数据如下：
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

    private List<Long> readLongList(JsonNode node) {
        List<Long> list = new ArrayList<>();
        if (node != null && node.isArray()) {
            for (JsonNode item : node) {
                if (item != null && item.isNumber()) {
                    list.add(item.asLong());
                } else if (item != null && item.isTextual()) {
                    try {
                        list.add(Long.parseLong(item.asText().trim()));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        return list;
    }

    private List<String> readStringList(JsonNode node) {
        List<String> list = new ArrayList<>();
        if (node != null && node.isArray()) {
            for (JsonNode item : node) {
                if (item != null && item.isTextual() && StringUtils.hasText(item.asText())) {
                    list.add(item.asText().trim());
                }
            }
        }
        return list;
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

    private long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (Exception e) {
            return 0L;
        }
    }

    private String safe(String text) {
        return text == null ? "" : text.trim();
    }
}
