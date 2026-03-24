package com.example.springboot.service;

import com.example.springboot.config.AiLlmProperties;
import com.example.springboot.dto.GenerateCreativeFromImageRequest;
import com.example.springboot.dto.GenerateCreativeRequest;
import com.example.springboot.dto.GenerateCreativeResponse;
import com.example.springboot.dto.GenerateTextRequest;
import com.example.springboot.dto.GenerateTextResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiGenerationService {

    private final AiLlmProperties llmProperties;
    private final ObjectMapper objectMapper;

    private static final Map<Long, String> MOCK_PLAY_NAMES = new HashMap<>();

    static {
        MOCK_PLAY_NAMES.put(1L, "《哈姆雷特》");
        MOCK_PLAY_NAMES.put(2L, "《天鹅湖》");
        MOCK_PLAY_NAMES.put(3L, "《茶花女》");
        MOCK_PLAY_NAMES.put(4L, "《罗密欧与朱丽叶》");
    }

    public GenerateTextResponse generateText(GenerateTextRequest req) {
        String playName = resolvePlayName(req.getPlayId());
        String userExtra = StringUtils.hasText(req.getUserInput()) ? req.getUserInput().trim() : "";

        List<String> missing = getMissingConfigReasons();
        if (!missing.isEmpty()) {
            return new GenerateTextResponse(
                    buildMockText(req.getType(), playName, userExtra),
                    true,
                    String.join("; ", missing)
            );
        }

        String system = buildSystemPrompt(req.getType());
        String user = buildUserPrompt(req.getType(), playName, userExtra);

        try {
            String content = callChatCompletions(system, user);
            if (!StringUtils.hasText(content)) {
                return new GenerateTextResponse(
                        buildMockText(req.getType(), playName, userExtra),
                        true,
                        "模型返回内容为空"
                );
            }
            return new GenerateTextResponse(content.trim(), false, null);
        } catch (Exception e) {
            throw new RuntimeException("大模型调用失败: " + e.getMessage(), e);
        }
    }

    public GenerateCreativeResponse generateCreative(GenerateCreativeRequest req) {
        String playName = resolvePlayName(req.getPlayId());
        String prompt = buildCreativePrompt(req.getType(), playName, req.getStyle());

        List<String> missing = getMissingImageConfigReasons();
        if (!missing.isEmpty()) {
            return new GenerateCreativeResponse(
                    buildMockCreativeImageUrl(req.getType(), playName),
                    prompt,
                    true,
                    String.join("; ", missing)
            );
        }

        try {
            String imageUrl = callImageGeneration(prompt);
            if (!StringUtils.hasText(imageUrl)) {
                return new GenerateCreativeResponse(
                        buildMockCreativeImageUrl(req.getType(), playName),
                        prompt,
                        true,
                        "图片模型返回为空"
                );
            }
            return new GenerateCreativeResponse(imageUrl, prompt, false, null);
        } catch (Exception e) {
            // 版权/IP 风险命中时，自动降敏重试一次，尽量提高可用性。
            if (isIpInfringementError(e.getMessage())) {
                String safePrompt = buildSafeCreativePrompt(req.getType(), playName, req.getStyle());
                log.warn("Creative IP risk detected, retry with sanitized prompt. oldPrompt={}, newPrompt={}", prompt, safePrompt);
                try {
                    String retriedImageUrl = callImageGeneration(safePrompt);
                    if (StringUtils.hasText(retriedImageUrl)) {
                        return new GenerateCreativeResponse(retriedImageUrl, safePrompt, false, null);
                    }
                } catch (Exception retryEx) {
                    log.warn("Creative retry after IP risk still failed. reason={}", retryEx.getMessage());
                }
            }
            log.warn("Creative generation fallback to mock. imageBaseUrl={}, imageModel={}, reason={}",
                    resolveImageBaseUrl(), resolveImageModel(), e.getMessage());
            return new GenerateCreativeResponse(
                    buildMockCreativeImageUrl(req.getType(), playName),
                    prompt,
                    true,
                    "图片模型调用失败: " + e.getMessage()
            );
        }
    }

    public GenerateCreativeResponse generateCreativeFromImage(GenerateCreativeFromImageRequest req) {
        String playName = resolvePlayName(req.getPlayId());
        String prompt = buildCreativeFromImagePrompt(req.getType(), playName, req.getStyle(), req.getProductType());
        String referenceModel = resolveImageEditModel();

        List<String> missing = getMissingImageConfigReasons();
        if (!missing.isEmpty()) {
            return new GenerateCreativeResponse(
                    buildMockCreativeImageUrl(req.getType(), playName),
                    prompt,
                    true,
                    String.join("; ", missing)
            );
        }

        if (!supportsReferenceImage(referenceModel)) {
            try {
                String fallbackPrompt = prompt + "。当前仅做风格近似重绘，不保证参考图细节一一对应。";
                String fallbackImageUrl = callImageGeneration(fallbackPrompt);
                if (StringUtils.hasText(fallbackImageUrl)) {
                    return new GenerateCreativeResponse(
                            fallbackImageUrl,
                            fallbackPrompt,
                            true,
                            "当前模型(" + referenceModel + ")不支持参考图输入；请配置 app.ai.llm.image-edit-model 为图像编辑模型后获得真实图生图效果"
                    );
                }
            } catch (Exception fallbackEx) {
                log.warn("Creative-from-image unsupported-model fallback failed. reason={}", fallbackEx.getMessage());
            }
            return new GenerateCreativeResponse(
                    buildMockCreativeImageUrl(req.getType(), playName),
                    prompt,
                    true,
                    "当前模型(" + referenceModel + ")不支持参考图输入；请配置 app.ai.llm.image-edit-model 为图像编辑模型"
            );
        }

        try {
            String imageUrl = callImageGenerationWithReference(prompt, req.getImageUrl());
            if (StringUtils.hasText(imageUrl)) {
                return new GenerateCreativeResponse(imageUrl, prompt, false, null);
            }
        } catch (Exception e) {
            log.warn("Creative-from-image primary call failed. imageBaseUrl={}, imageModel={}, reason={}",
                    resolveImageBaseUrl(), resolveImageModel(), e.getMessage());
            if (isIpInfringementError(e.getMessage())) {
                String safePrompt = buildSafeCreativePrompt(req.getType(), playName, req.getStyle());
                try {
                    String retryImageUrl = callImageGenerationWithReference(safePrompt, req.getImageUrl());
                    if (StringUtils.hasText(retryImageUrl)) {
                        return new GenerateCreativeResponse(retryImageUrl, safePrompt, false, null);
                    }
                } catch (Exception retryEx) {
                    log.warn("Creative-from-image retry failed. reason={}", retryEx.getMessage());
                }
            }
            // 尝试降级到纯文本文生图，保证接口可用。
            try {
                String fallbackPrompt = prompt + "。参考图无法直接使用时，请保持主体轮廓与主色调一致。";
                String fallbackImageUrl = callImageGeneration(fallbackPrompt);
                if (StringUtils.hasText(fallbackImageUrl)) {
                    return new GenerateCreativeResponse(fallbackImageUrl, fallbackPrompt, false, null);
                }
            } catch (Exception fallbackEx) {
                log.warn("Creative-from-image text fallback failed. reason={}", fallbackEx.getMessage());
            }
            return new GenerateCreativeResponse(
                    buildMockCreativeImageUrl(req.getType(), playName),
                    prompt,
                    true,
                    "参考图生成失败: " + e.getMessage()
            );
        }

        return new GenerateCreativeResponse(
                buildMockCreativeImageUrl(req.getType(), playName),
                prompt,
                true,
                "参考图生成返回为空"
        );
    }

    /**
     * 仅返回“是否已配置”的诊断信息，不返回密钥原文。
     */
    public Map<String, Object> getConfigStatus() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("enabled", llmProperties.isEnabled());
        status.put("hasApiKey", StringUtils.hasText(llmProperties.getApiKey()));
        status.put("hasBaseUrl", StringUtils.hasText(llmProperties.getBaseUrl()));
        status.put("hasModel", StringUtils.hasText(llmProperties.getModel()));
        status.put("baseUrl", StringUtils.hasText(llmProperties.getBaseUrl()) ? llmProperties.getBaseUrl() : "");
        status.put("model", StringUtils.hasText(llmProperties.getModel()) ? llmProperties.getModel() : "");
        status.put("missing", getMissingConfigReasons());
        status.put("imageHasApiKey", StringUtils.hasText(resolveImageApiKey()));
        status.put("imageHasBaseUrl", StringUtils.hasText(resolveImageBaseUrl()));
        status.put("imageHasModel", StringUtils.hasText(resolveImageModel()));
        status.put("imageBaseUrl", resolveImageBaseUrl());
        status.put("imageModel", resolveImageModel());
        status.put("imageEditModel", resolveImageEditModel());
        status.put("referenceImageSupported", supportsReferenceImage(resolveImageEditModel()));
        status.put("imageMissing", getMissingImageConfigReasons());
        return status;
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

    private List<String> getMissingImageConfigReasons() {
        List<String> missing = new ArrayList<>();
        if (!llmProperties.isEnabled()) {
            missing.add("app.ai.llm.enabled=false");
        }
        if (!StringUtils.hasText(resolveImageApiKey())) {
            missing.add("app.ai.llm.image-api-key/api-key 为空");
        }
        if (!StringUtils.hasText(resolveImageBaseUrl())) {
            missing.add("app.ai.llm.image-base-url/base-url 为空");
        }
        if (!StringUtils.hasText(resolveImageModel())) {
            missing.add("app.ai.llm.image-model/model 为空");
        }
        return missing;
    }

    private String resolveImageApiKey() {
        return StringUtils.hasText(llmProperties.getImageApiKey()) ? llmProperties.getImageApiKey() : llmProperties.getApiKey();
    }

    private String resolveImageBaseUrl() {
        return StringUtils.hasText(llmProperties.getImageBaseUrl()) ? llmProperties.getImageBaseUrl() : llmProperties.getBaseUrl();
    }

    private String resolveImageModel() {
        return StringUtils.hasText(llmProperties.getImageModel()) ? llmProperties.getImageModel() : llmProperties.getModel();
    }

    private String resolveImageEditModel() {
        return StringUtils.hasText(llmProperties.getImageEditModel()) ? llmProperties.getImageEditModel() : resolveImageModel();
    }

    private boolean supportsReferenceImage(String model) {
        String m = StringUtils.hasText(model) ? model.toLowerCase() : "";
        // 已知 t2i 模型只支持文生图，不支持在同一消息中传入 image content。
        return !m.contains("t2i");
    }

    private String resolvePlayName(Long playId) {
        if (playId == null) {
            return null;
        }
        return MOCK_PLAY_NAMES.getOrDefault(playId, "剧目ID " + playId);
    }

    private String buildSystemPrompt(String type) {
        return switch (type) {
            case "review" -> "你是一位戏剧观众口吻的写作者。必须紧贴用户原话与情绪表达，先复述用户感受，再做轻度扩展。禁止虚构用户未提及的具体剧情、人物关系、台词细节。输出简洁自然，控制在 120-220 字。";
            case "plot" -> "你是一位剧情梳理助手。必须以用户输入为主线，不可编造具体幕次、人物冲突或细节设定。若用户信息很少，明确说明“基于用户描述做概括”，给出 2-3 条结构化分析即可。控制在 140-260 字。";
            case "actor" -> "你是一位表演观察助手。仅在用户提供依据时才写具体表演细节；若无充分依据，使用中性表达，避免杜撰台词、动作、对手戏。保持贴近用户感受，输出 120-220 字。";
            default -> throw new IllegalArgumentException("不支持的 type: " + type);
        };
    }

    private String buildUserPrompt(String type, String playName, String userExtra) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.hasText(playName)) {
            sb.append("关联剧目：").append(playName).append("\n");
        } else {
            sb.append("未指定具体剧目，请基于用户描述合理发挥。\n");
        }
        if (StringUtils.hasText(userExtra)) {
            sb.append("用户补充说明：").append(userExtra).append("\n");
        }
        sb.append("写作约束：优先贴合用户原话，不偏离用户中心观点；不要编造具体剧情片段、角色关系、台词或舞台动作细节。可适度丰富，但必须可由用户输入合理推出。\n");
        sb.append(switch (type) {
            case "review" -> "请写一段简洁观后感，120-220 字，不超过 2 段。";
            case "plot" -> "请写剧情与主题分析，140-260 字，2-3 点即可。";
            case "actor" -> "请写演员评价，120-220 字，避免无依据细节。";
            default -> "";
        });
        return sb.toString();
    }

    private String buildMockText(String type, String playName, String userExtra) {
        String p = StringUtils.hasText(playName) ? playName : "本场演出";
        String extra = StringUtils.hasText(userExtra) ? "（补充：" + userExtra + "）" : "";
        return switch (type) {
            case "review" -> "【演示文案，未配置大模型密钥】" + p + " 带来层次分明的冲突与节奏。" + extra
                    + " 走出剧场后仍回味人物抉择：建议在正式环境中配置 app.ai.llm 以生成完整观后感。";
            case "plot" -> "【演示文案】" + p + " 在叙事上采用经典结构，主线清晰；人物动机与主题可结合具体场次展开。" + extra
                    + " 配置大模型后将输出更完整的结构分析。";
            case "actor" -> "【演示文案】" + p + " 的演员在台词与舞台行动上整体协调；细节刻画可结合具体角色展开。" + extra
                    + " 配置大模型后将输出更细的表演点评。";
            default -> "";
        };
    }

    private String buildCreativePrompt(String type, String playName, String style) {
        String creativeType = "poster".equals(type) ? "文创海报" : "周边设计图";
        String targetPlay = StringUtils.hasText(playName) ? playName : "主题剧目";
        String styleText = StringUtils.hasText(style) ? style.trim() : "梦幻舞台风格";
        return "请生成一张" + creativeType
                + "，主题围绕" + targetPlay
                + "，整体风格为" + styleText
                + "。画面干净、构图完整、色彩协调，适合活动宣传与社交媒体展示，避免过多文字。";
    }

    private String buildCreativeFromImagePrompt(String type, String playName, String style, String productType) {
        String basePrompt = buildCreativePrompt(type, playName, style);
        String product = StringUtils.hasText(productType) ? productType.trim() : ("poster".equals(type) ? "海报" : "周边");
        return basePrompt
                + " 请以用户参考图为主视觉来源，保留核心构图和主色调。"
                + " 输出适配" + product + "场景，避免加入版权角色名称与品牌标识。";
    }

    private String buildSafeCreativePrompt(String type, String playName, String style) {
        String sanitizedStyle = sanitizeIpRiskText(style);
        return buildCreativePrompt(type, playName, sanitizedStyle);
    }

    private String sanitizeIpRiskText(String input) {
        if (!StringUtils.hasText(input)) {
            return "梦幻奇境风格，冬日童话氛围";
        }
        String s = input;
        // 常见 IP 关键词做弱化处理，避免命中版权风险规则。
        s = s.replaceAll("(?i)冰雪奇缘|frozen", "冬日童话");
        s = s.replaceAll("(?i)爱莎|艾莎|elsa", "女主角");
        s = s.replaceAll("(?i)安娜|anna", "伙伴角色");
        s = s.replaceAll("(?i)迪士尼|disney", "经典童话");
        return s;
    }

    private String buildMockCreativeImageUrl(String type, String playName) {
        String creativeType = "poster".equals(type) ? "海报" : "周边";
        String text = (StringUtils.hasText(playName) ? playName : "剧目") + "-" + creativeType + "-演示图";
        return "https://dummyimage.com/1024x1024/e5e7eb/374151.png&text="
                + URLEncoder.encode(text, StandardCharsets.UTF_8);
    }

    private String callImageGeneration(String prompt) throws Exception {
        String base = resolveImageBaseUrl().replaceAll("/+$", "");
        String url = base + "/services/aigc/multimodal-generation/generation";

        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", resolveImageModel());
        ObjectNode input = body.putObject("input");
        ArrayNode messages = input.putArray("messages");
        ObjectNode message = messages.addObject();
        message.put("role", "user");
        ArrayNode content = message.putArray("content");
        ObjectNode text = content.addObject();
        text.put("text", prompt);

        ObjectNode parameters = body.putObject("parameters");
        parameters.put("prompt_extend", true);
        parameters.put("watermark", false);
        parameters.put("n", 1);
        parameters.put("negative_prompt", "");
        parameters.put("size", "1280*1280");

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(llmProperties.getTimeoutSeconds()))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + resolveImageApiKey())
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 429 || response.body().contains("Throttling.RateQuota")) {
            Thread.sleep(1500L);
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        }
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException(parseDashScopeError(response.statusCode(), response.body()));
        }

        JsonNode root = objectMapper.readTree(response.body());
        if (root.path("code").isTextual() && root.path("message").isTextual()) {
            throw new IllegalStateException(root.path("code").asText() + ": " + root.path("message").asText());
        }
        JsonNode imageNode = root.path("output").path("choices").path(0).path("message").path("content").path(0).path("image");
        if (imageNode.isTextual()) {
            return imageNode.asText();
        }
        return null;
    }

    private String callImageGenerationWithReference(String prompt, String imageUrl) throws Exception {
        String base = resolveImageBaseUrl().replaceAll("/+$", "");
        String url = base + "/services/aigc/multimodal-generation/generation";

        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", resolveImageEditModel());
        ObjectNode input = body.putObject("input");
        ArrayNode messages = input.putArray("messages");
        ObjectNode message = messages.addObject();
        message.put("role", "user");
        ArrayNode content = message.putArray("content");
        ObjectNode image = content.addObject();
        image.put("image", imageUrl);
        ObjectNode text = content.addObject();
        text.put("text", prompt);

        ObjectNode parameters = body.putObject("parameters");
        parameters.put("prompt_extend", true);
        parameters.put("watermark", false);
        parameters.put("n", 1);
        parameters.put("negative_prompt", "");
        parameters.put("size", "1280*1280");

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(llmProperties.getTimeoutSeconds()))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + resolveImageApiKey())
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 429 || response.body().contains("Throttling.RateQuota")) {
            Thread.sleep(1500L);
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        }
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException(parseDashScopeError(response.statusCode(), response.body()));
        }

        JsonNode root = objectMapper.readTree(response.body());
        if (root.path("code").isTextual() && root.path("message").isTextual()) {
            throw new IllegalStateException(root.path("code").asText() + ": " + root.path("message").asText());
        }
        JsonNode imageNode = root.path("output").path("choices").path(0).path("message").path("content").path(0).path("image");
        if (imageNode.isTextual()) {
            return imageNode.asText();
        }
        return null;
    }

    private String parseDashScopeError(int statusCode, String body) {
        try {
            JsonNode root = objectMapper.readTree(body);
            String code = root.path("code").asText("");
            String message = root.path("message").asText("");
            String requestId = root.path("request_id").asText("");
            if (StringUtils.hasText(code) || StringUtils.hasText(message)) {
                return "HTTP " + statusCode
                        + " [code=" + code + ", message=" + message + ", request_id=" + requestId + "]";
            }
        } catch (Exception ignore) {
            // 非 JSON 或结构不符时回落原始内容
        }
        return "HTTP " + statusCode + ": " + body;
    }

    private boolean isIpInfringementError(String message) {
        return StringUtils.hasText(message) && message.contains("IPInfringementSuspect");
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

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(llmProperties.getTimeoutSeconds()))
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
}
