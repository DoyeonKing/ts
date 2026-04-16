package com.example.springboot.service;

import com.example.springboot.config.AiLlmProperties;
import com.example.springboot.dto.AiChatRequest;
import com.example.springboot.dto.AiChatResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiChatService {

    private static final int MAX_SESSION_ROUNDS = 6;
    private static final int MAX_SESSION_MESSAGES = MAX_SESSION_ROUNDS * 2;

    private final AiLlmProperties llmProperties;
    private final ObjectMapper objectMapper;
    private final ConcurrentMap<String, Deque<ChatMessage>> sessionMemory = new ConcurrentHashMap<>();

    public AiChatResponse chat(AiChatRequest req) {
        String sessionId = normalizeSessionId(req.getSessionId());
        if (sessionId != null && Boolean.TRUE.equals(req.getResetSession())) {
            sessionMemory.remove(sessionId);
        }

        List<String> missing = getMissingConfigReasons();
        if (!missing.isEmpty()) {
            return new AiChatResponse(buildMockReply(req), true, String.join("; ", missing));
        }

        try {
            String content = callChatCompletions(req, sessionId);
            if (!StringUtils.hasText(content)) {
                return new AiChatResponse(buildMockReply(req), true, "模型返回内容为空");
            }
            String normalized = normalizeReply(content);
            saveSessionTurn(sessionId, req.getMessage().trim(), normalized);
            return new AiChatResponse(normalized, false, null);
        } catch (Exception e) {
            log.warn("AI chat failed, fallback to mock. reason={}", e.getMessage());
            return new AiChatResponse(buildMockReply(req), true, "对话模型调用失败: " + e.getMessage());
        }
    }

    private List<String> getMissingConfigReasons() {
        List<String> missing = new ArrayList<>();
        if (!llmProperties.isEnabled()) {
            missing.add("app.ai.llm.enabled=false");
        }
        if (!StringUtils.hasText(llmProperties.getApiKey())) {
            missing.add("app.ai.llm.api-key 为空");
        }
        if (!StringUtils.hasText(llmProperties.getBaseUrl())) {
            missing.add("app.ai.llm.base-url 为空");
        }
        if (!StringUtils.hasText(llmProperties.getModel())) {
            missing.add("app.ai.llm.model 为空");
        }
        return missing;
    }

    private String buildSystemPrompt(AiChatRequest req) {
        String scene = StringUtils.hasText(req.getScene()) ? req.getScene().trim() : "通用剧场咨询";
        return "你是“餐剧盒”剧场助手。"
                + "目标：帮助用户选剧、规划观演体验，并给出简洁可执行建议。"
                + "风格：友好、专业、简洁，优先中文回答。"
                + "约束：不编造不存在的具体场次/票价/地址；信息不足时先说明假设并给可落地建议。"
                + "输出要求：只输出纯文本，不使用 Markdown 符号（如 **、#、```）；控制在 180-260 字；"
                + "按固定结构输出：结论：... 方案1：... 方案2：... 下一步：...。"
                + "当前对话场景：" + scene + "。";
    }

    private String buildMockReply(AiChatRequest req) {
        String scene = StringUtils.hasText(req.getScene()) ? req.getScene().trim() : "通用咨询";
        return "【剧场助手演示回复】你正在进行“" + scene + "”场景咨询。\n"
                + "你可以告诉我预算、偏好类型、同行人数和时间段，我会给你 2-3 个可执行建议。"
                + (StringUtils.hasText(req.getMessage()) ? "\n你刚才的问题是：" + req.getMessage().trim() : "");
    }

    private String callChatCompletions(AiChatRequest req, String sessionId) throws Exception {
        String base = llmProperties.getBaseUrl().replaceAll("/+$", "");
        String url = base + "/chat/completions";

        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", llmProperties.getModel());
        ArrayNode messages = body.putArray("messages");

        ObjectNode system = messages.addObject();
        system.put("role", "system");
        system.put("content", buildSystemPrompt(req));

        appendSessionHistory(messages, sessionId);

        if (req.getHistory() != null) {
            for (AiChatRequest.HistoryMessage history : req.getHistory()) {
                if (!StringUtils.hasText(history.getRole()) || !StringUtils.hasText(history.getContent())) {
                    continue;
                }
                String role = normalizeRole(history.getRole());
                if (role == null) {
                    continue;
                }
                appendMessage(messages, role, history.getContent().trim());
            }
        }

        appendMessage(messages, "user", req.getMessage().trim());

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

        return sendChatWithRetry(client, request);
    }

    private String sendChatWithRetry(HttpClient client, HttpRequest request) throws Exception {
        Exception last = null;
        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
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
            } catch (HttpConnectTimeoutException e) {
                last = e;
                log.warn("LLM connect timeout (attempt {}/2), baseUrl may be unreachable or need proxy", attempt);
                if (attempt < 2) {
                    Thread.sleep(1000L);
                }
            } catch (Exception e) {
                if (attempt < 2 && isLikelyConnectTimeout(e)) {
                    last = e;
                    log.warn("LLM connect issue (attempt {}/2): {}", attempt, e.getMessage());
                    Thread.sleep(1000L);
                    continue;
                }
                throw e;
            }
        }
        throw last != null ? last : new IllegalStateException("LLM 请求失败");
    }

    private static boolean isLikelyConnectTimeout(Throwable e) {
        if (e instanceof HttpConnectTimeoutException) {
            return true;
        }
        String m = e.getMessage();
        return m != null && (m.contains("connect timed out") || m.contains("Connect timed out") || m.contains("Connection timed out"));
    }

    private String normalizeSessionId(String rawSessionId) {
        if (!StringUtils.hasText(rawSessionId)) {
            return null;
        }
        String s = rawSessionId.trim();
        if (s.length() > 64) {
            s = s.substring(0, 64);
        }
        return s;
    }

    private void appendSessionHistory(ArrayNode messages, String sessionId) {
        if (!StringUtils.hasText(sessionId)) {
            return;
        }
        Deque<ChatMessage> queue = sessionMemory.get(sessionId);
        if (queue == null || queue.isEmpty()) {
            return;
        }
        synchronized (queue) {
            for (ChatMessage chatMessage : queue) {
                appendMessage(messages, chatMessage.role(), chatMessage.content());
            }
        }
    }

    private void saveSessionTurn(String sessionId, String userMessage, String assistantMessage) {
        if (!StringUtils.hasText(sessionId)) {
            return;
        }
        Deque<ChatMessage> queue = sessionMemory.computeIfAbsent(sessionId, key -> new ArrayDeque<>());
        synchronized (queue) {
            queue.addLast(new ChatMessage("user", userMessage));
            queue.addLast(new ChatMessage("assistant", assistantMessage));
            while (queue.size() > MAX_SESSION_MESSAGES) {
                queue.pollFirst();
            }
        }
    }

    private void appendMessage(ArrayNode messages, String role, String content) {
        ObjectNode message = messages.addObject();
        message.put("role", role);
        message.put("content", content);
    }

    private String normalizeRole(String rawRole) {
        if (!StringUtils.hasText(rawRole)) return null;
        String role = rawRole.trim().toLowerCase();
        return switch (role) {
            case "user", "assistant", "system" -> role;
            default -> null;
        };
    }

    private String normalizeReply(String raw) {
        if (!StringUtils.hasText(raw)) {
            return "";
        }
        String s = raw.trim();
        // Swagger 中换行会以转义形式显示，压成单行更易读。
        s = s.replace("\r", " ").replace("\n", " ");
        // 去掉常见 markdown 噪音符号，避免展示杂乱。
        s = s.replace("```", " ").replace("**", " ").replace("##", " ").replace("#", " ");
        s = s.replace("`", " ").replace("*", " ");
        s = s.replaceAll("\\s{2,}", " ").trim();
        if (s.length() > 320) {
            s = s.substring(0, 320) + "...";
        }
        return s;
    }

    private record ChatMessage(String role, String content) {
    }
}
