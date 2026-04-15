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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AiImageGenerationService {

    private static final Pattern IMAGE_URL_PATTERN = Pattern.compile("https://[^\\s'\"),]+", Pattern.CASE_INSENSITIVE);

    private final AiLlmProperties llmProperties;
    private final ObjectMapper objectMapper;

    public Map<String, Object> generateCreativeImage(String prompt, String type) {
        String normalizedType = "poster".equals(type) ? "poster" : "merch";
        String sourcePrompt = StringUtils.hasText(prompt) ? prompt.trim() : defaultPrompt(normalizedType);
        String compactPrompt = compactPromptForImageModel(sourcePrompt, normalizedType);
        String finalPrompt = strengthenPromptForImage(compactPrompt, normalizedType);

        try {
            String imageUrl = callWanTextToImage(finalPrompt);
            if (StringUtils.hasText(imageUrl)) {
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("prompt", finalPrompt);
                result.put("imageUrl", imageUrl);
                result.put("provider", "wan2.6-t2i");
                result.put("mock", false);
                result.put("imageType", normalizedType);
                result.put("imageError", "");
                return result;
            }
            return buildErrorResult(finalPrompt, normalizedType, "万相图片接口返回为空，未获取到可展示图片地址");
        } catch (Exception e) {
            return buildErrorResult(finalPrompt, normalizedType, e.getMessage());
        }
    }

    private Map<String, Object> buildErrorResult(String prompt, String type, String error) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("prompt", prompt);
        result.put("imageUrl", "");
        result.put("provider", "wan2.6-t2i");
        result.put("mock", true);
        result.put("imageType", type);
        result.put("imageError", error);
        return result;
    }

    private String compactPromptForImageModel(String prompt, String type) {
        String compact = prompt == null ? "" : prompt;
        compact = compact.replace("\r", " ")
                .replace("\n", " ")
                .replace("━━━━━━━━━━━━━━━━━━━━━━", " ")
                .replace("📋", " ")
                .replace("🎨", " ")
                .replace("📦", " ")
                .replace("💡", " ")
                .replace("【", " ")
                .replace("】", " ")
                .replace("需求分析", " ")
                .replace("平面设计图提示词", " ")
                .replace("成品展示图提示词", " ")
                .replace("设计建议", " ")
                .replace("主体描述：", "")
                .replace("风格定位：", "")
                .replace("色彩方案：", "")
                .replace("构图布局：", "")
                .replace("细节元素：", "")
                .replace("背景处理：", "")
                .replace("技术参数：", "")
                .replace("产品类型：", "")
                .replace("应用场景：", "")
                .replace("材质质感：", "")
                .replace("光影效果：", "")
                .replace("展示角度：", "")
                .replace("环境氛围：", "")
                .replaceAll("\\s+", " ")
                .trim();

        int maxLength = "poster".equals(type) ? 220 : 180;
        if (compact.length() > maxLength) {
            compact = compact.substring(0, maxLength);
        }
        return compact;
    }

    private String strengthenPromptForImage(String prompt, String type) {
        if ("poster".equals(type)) {
            return prompt + "，完整剧场主视觉海报，主体是海报本身，戏剧灯光，舞台空间，电影感构图，纯净背景，无水印，无多余文字";
        }
        return prompt + "，输出一张左右分栏合成图：左侧为文创成品展示图（真实商品摄影风格，正面展示）；右侧必须是服装平面设计图纸，不是海报版式。右侧内容要求：同一件T恤的正面平铺图 + 背面平铺图（2D正投影、无透视、无模特、无场景），图案位置清晰，轮廓线干净，白底或浅灰底，可带简洁标注线。左右两栏的图案必须完全一致（同图案、同配色、同构图元素）。无水印，无多余文字。";
    }

    private String callWanTextToImage(String prompt) throws Exception {
        String baseUrl = resolveImageBaseUrl();
        String apiKey = resolveImageApiKey();
        String model = resolveImageModel();
        if (!StringUtils.hasText(baseUrl) || !StringUtils.hasText(apiKey) || !StringUtils.hasText(model)) {
            throw new IllegalStateException("未读取到完整图片模型配置，请检查 image-base-url、image-api-key/api-key、image-model");
        }

        String url = baseUrl.replaceAll("/+$", "") + "/services/aigc/multimodal-generation/generation";

        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", model);
        ObjectNode input = body.putObject("input");
        ArrayNode messages = input.putArray("messages");
        ObjectNode message = messages.addObject();
        message.put("role", "user");
        ArrayNode content = message.putArray("content");
        ObjectNode textContent = content.addObject();
        textContent.put("text", prompt);

        ObjectNode parameters = body.putObject("parameters");
        parameters.put("negative_prompt", "");
        parameters.put("prompt_extend", true);
        parameters.put("watermark", false);
        parameters.put("n", 1);
        parameters.put("size", "1280*1280");

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(Math.max(15, llmProperties.getConnectTimeoutSeconds())))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(Math.max(30, llmProperties.getTimeoutSeconds())))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("HTTP " + response.statusCode() + ": " + response.body());
        }

        JsonNode root = objectMapper.readTree(response.body());
        if (root.path("code").isTextual() && root.path("message").isTextual()) {
            throw new IllegalStateException(root.path("code").asText() + ": " + root.path("message").asText());
        }

        JsonNode output = root.path("output");
        JsonNode resultUrl = output.path("results").path(0).path("url");
        if (resultUrl.isTextual() && StringUtils.hasText(resultUrl.asText())) {
            return resultUrl.asText();
        }

        String parsedFromOutput = extractImageUrlFromNode(output);
        if (StringUtils.hasText(parsedFromOutput)) {
            return parsedFromOutput;
        }

        String parsedFromRaw = extractImageUrlFromContent(response.body());
        if (StringUtils.hasText(parsedFromRaw)) {
            return parsedFromRaw;
        }

        JsonNode taskIdNode = output.path("task_id");
        if (taskIdNode.isTextual() && StringUtils.hasText(taskIdNode.asText())) {
            return pollTaskResult(client, baseUrl, apiKey, taskIdNode.asText());
        }

        throw new IllegalStateException("万相接口未返回可用图片地址：" + response.body());
    }

    private String pollTaskResult(HttpClient client, String baseUrl, String apiKey, String taskId) throws Exception {
        String taskUrl = baseUrl.replaceAll("/+$", "") + "/tasks/" + taskId;
        for (int i = 0; i < 20; i++) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(taskUrl))
                    .timeout(Duration.ofSeconds(30))
                    .header("Authorization", "Bearer " + apiKey)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("任务查询失败 HTTP " + response.statusCode() + ": " + response.body());
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode output = root.path("output");
            String status = output.path("task_status").asText("");
            if ("SUCCEEDED".equalsIgnoreCase(status)) {
                JsonNode resultUrl = output.path("results").path(0).path("url");
                if (resultUrl.isTextual() && StringUtils.hasText(resultUrl.asText())) {
                    return resultUrl.asText();
                }

                String parsedFromOutput = extractImageUrlFromNode(output);
                if (StringUtils.hasText(parsedFromOutput)) {
                    return parsedFromOutput;
                }

                String parsedFromRaw = extractImageUrlFromContent(response.body());
                if (StringUtils.hasText(parsedFromRaw)) {
                    return parsedFromRaw;
                }

                throw new IllegalStateException("任务成功但未返回图片地址：" + response.body());
            }
            if ("FAILED".equalsIgnoreCase(status) || "CANCELED".equalsIgnoreCase(status)) {
                throw new IllegalStateException("图片任务失败：" + output.path("message").asText(response.body()));
            }
            Thread.sleep(2000L);
        }
        throw new IllegalStateException("图片任务轮询超时，task_id=" + taskId);
    }

    private String extractImageUrlFromNode(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) return "";
        if (node.isTextual()) return extractImageUrlFromContent(node.asText());
        if (node.isArray()) {
            for (JsonNode item : node) {
                String url = extractImageUrlFromNode(item);
                if (StringUtils.hasText(url)) return url;
            }
            return "";
        }
        if (node.isObject()) {
            if (node.path("url").isTextual() && StringUtils.hasText(node.path("url").asText())) {
                return node.path("url").asText();
            }
            if (node.path("text").isTextual()) {
                String url = extractImageUrlFromContent(node.path("text").asText());
                if (StringUtils.hasText(url)) return url;
            }
            if (node.path("content").isTextual()) {
                String url = extractImageUrlFromContent(node.path("content").asText());
                if (StringUtils.hasText(url)) return url;
            }
            String url = extractImageUrlFromNode(node.path("content"));
            if (StringUtils.hasText(url)) return url;
            url = extractImageUrlFromNode(node.path("message"));
            if (StringUtils.hasText(url)) return url;
            url = extractImageUrlFromNode(node.path("choices"));
            if (StringUtils.hasText(url)) return url;
        }
        return "";
    }

    private String extractImageUrlFromContent(String content) {
        if (!StringUtils.hasText(content)) return "";
        Matcher matcher = IMAGE_URL_PATTERN.matcher(content);
        return matcher.find() ? matcher.group() : "";
    }

    private String resolveImageApiKey() {
        return StringUtils.hasText(llmProperties.getImageApiKey()) ? llmProperties.getImageApiKey() : llmProperties.getApiKey();
    }

    private String resolveImageBaseUrl() {
        return StringUtils.hasText(llmProperties.getImageBaseUrl()) ? llmProperties.getImageBaseUrl() : "https://dashscope.aliyuncs.com/api/v1";
    }

    private String resolveImageModel() {
        return StringUtils.hasText(llmProperties.getImageModel()) ? llmProperties.getImageModel() : "wan2.6-t2i";
    }

    private String defaultPrompt(String type) {
        if ("poster".equals(type)) {
            return "剧场主视觉海报，戏剧灯光，舞台氛围，电影感构图";
        }
        return "剧场文创商品图，一件正面可见的T恤 mockup，电商级商品展示";
    }
}
