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
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiTextGenerationService {

    private final AiLlmProperties llmProperties;
    private final ObjectMapper objectMapper;

    public Map<String, Object> rewriteCreativePrompt(String userNeed,
                                                     String creativeType,
                                                     String theme,
                                                     String mood,
                                                     String audience,
                                                     List<String> itemSummaries,
                                                     String creativeReview) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (!isConfigured()) {
            result.put("prompt", buildFallbackPrompt(userNeed, creativeType, theme, mood, audience, itemSummaries, creativeReview));
            result.put("llmEnabled", false);
            result.put("llmMissing", getMissingReasons());
            result.put("llmError", "未读取到完整大模型配置，已回退到本地提示词模板");
            return result;
        }

        String system = "你是一位资深的文创产品设计师和AI提示词工程师。"
                + "你的任务是将用户的文创产品描述转化为专业、详细、可直接复制使用的AI图像生成提示词。"
                + "你必须严格理解用户真正想要的产品与风格，不要擅自改题，不要把单一产品扩展成无关的整套周边。"
                + "请始终输出4个部分：需求分析、平面设计图提示词、成品展示图提示词、设计建议。"
                + "其中平面设计图提示词用于生成平面主视觉/印花设计稿；成品展示图提示词用于生成产品落地后的真实展示效果图。"
                + "平面设计图提示词要重点描述主体视觉、风格、配色、版式、细节元素、背景与技术参数；成品展示图提示词要重点描述产品类型、材质、场景、光影、角度、氛围与技术参数。"
                + "输出必须是中文，必须清晰、专业、具体、可直接复制到即梦、Midjourney、Stable Diffusion 等工具中使用。";

        String user = "请根据以下输入生成结果。"
                + "\n用户原始描述：" + safe(userNeed)
                + "\n创意类型：" + safe(creativeType)
                + "\n主题参考：" + safe(theme)
                + "\n情绪参考：" + safe(mood)
                + "\n目标人群：" + safe(audience)
                + "\n辅助灵感：" + String.join("；", itemSummaries == null ? List.of() : itemSummaries)
                + "\n专业赏析参考：" + safe(creativeReview)
                + "\n请严格按照以下格式返回："
                + "\n━━━━━━━━━━━━━━━━━━━━━━"
                + "\n📋 需求分析"
                + "\n━━━━━━━━━━━━━━━━━━━━━━"
                + "\n【对用户需求的简要分析】"
                + "\n━━━━━━━━━━━━━━━━━━━━━━"
                + "\n🎨 平面设计图提示词"
                + "\n━━━━━━━━━━━━━━━━━━━━━━"
                + "\n【完整的中文提示词，可直接复制使用】"
                + "\n━━━━━━━━━━━━━━━━━━━━━━"
                + "\n📦 成品展示图提示词"
                + "\n━━━━━━━━━━━━━━━━━━━━━━"
                + "\n【完整的中文提示词，可直接复制使用】"
                + "\n━━━━━━━━━━━━━━━━━━━━━━"
                + "\n💡 设计建议"
                + "\n━━━━━━━━━━━━━━━━━━━━━━"
                + "\n【配色建议、构图建议、材质建议等】"
                + "\n额外要求："
                + "\n1. 平面设计图提示词请按“主体描述、风格定位、色彩方案、构图布局、细节元素、背景处理、技术参数”这条思路组织。"
                + "\n2. 成品展示图提示词请按“产品类型、应用场景、材质质感、光影效果、展示角度、环境氛围、技术参数”这条思路组织。"
                + "\n3. 如果用户明确指定某个产品，就围绕这个产品展开，不要擅自增加其他产品。"
                + "\n4. 平面设计图是平面主视觉设计稿，不是工艺结构图。"
                + "\n5. 成品展示图要体现真实落地后的高级质感。";

        try {
            String content = callChatCompletions(system, user);
            if (StringUtils.hasText(content)) {
                result.put("prompt", content.trim());
                result.put("llmEnabled", true);
                result.put("llmMissing", List.of());
                result.put("llmError", "");
                return result;
            }
            result.put("prompt", buildFallbackPrompt(userNeed, creativeType, theme, mood, audience, itemSummaries, creativeReview));
            result.put("llmEnabled", false);
            result.put("llmMissing", List.of());
            result.put("llmError", "大模型返回为空，已回退到本地提示词模板");
            return result;
        } catch (Exception e) {
            result.put("prompt", buildFallbackPrompt(userNeed, creativeType, theme, mood, audience, itemSummaries, creativeReview));
            result.put("llmEnabled", false);
            result.put("llmMissing", List.of());
            result.put("llmError", e.getMessage());
            return result;
        }
    }

    public String mergePromptModulesForImage(String analysis,
                                             String designPrompt,
                                             String productPrompt,
                                             String designAdvice,
                                             String creativeType) {
        String fallback = fallbackMergedPrompt(analysis, designPrompt, productPrompt, designAdvice, creativeType);
        if (!isConfigured()) {
            return fallback;
        }

        String system = "你是千问图片生成前置提示词整理器。"
                + "你要把多个模块的中文方案整合成一条适合图片生成模型直接使用的最终提示词。"
                + "必须保留用户真正想要的主体、风格、材质、构图、配色、氛围。"
                + "不要输出分析，不要分段，不要标题，不要emoji，不要解释。"
                + "输出只保留一条适合图片模型的最终中文提示词，尽量精炼，信息密度高，长度控制在220字以内。";

        String user = "请把以下几个模块合并成一条最终出图提示词。"
                + "\n创意类型：" + safe(creativeType)
                + "\n需求分析：" + safe(analysis)
                + "\n平面设计图提示词：" + safe(designPrompt)
                + "\n成品展示图提示词：" + safe(productPrompt)
                + "\n设计建议：" + safe(designAdvice)
                + "\n要求：如果是周边，请优先保留商品主体、印花主视觉、材质、陈列方式、拍摄视角与高级感；如果是海报，请优先保留主视觉、构图、灯光与版式气质。只输出一条最终提示词。";

        try {
            String merged = callChatCompletions(system, user);
            return StringUtils.hasText(merged) ? merged.trim() : fallback;
        } catch (Exception e) {
            return fallback;
        }
    }

    public Map<String, Object> generateRecommendationFeatures(String query) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (!isConfigured()) {
            result.put("llmEnabled", false);
            result.put("query", safe(query));
            return result;
        }

        String system = "你是剧场推荐系统的垂直NLU与评论特征抽取器。"
                + "请根据用户输入，提取结构化特征并只输出JSON。"
                + "禁止输出解释、禁止输出Markdown、禁止输出代码块。";

        String user = "用户需求：" + safe(query)
                + "\n请输出JSON，字段必须包含："
                + "\nsoftTags: string[]（如 话剧/音乐剧/歌剧/芭蕾/舞剧/儿童剧/脱口秀/爱情/经典/悲剧）"
                + "\nactorTraits: string[]"
                + "\nreviewKeywords: string[]"
                + "\nanalysisPoints: string[]"
                + "\ngeneratedReview: string"
                + "\nsummary: string";

        try {
            String content = callChatCompletions(system, user);
            String json = normalizeJson(content);
            JsonNode root = objectMapper.readTree(json);

            result.put("engine", "llm-feature-engine");
            result.put("query", safe(query));
            result.put("softTags", readStringList(root.path("softTags")));
            result.put("actorTraits", readStringList(root.path("actorTraits")));
            result.put("reviewKeywords", readStringList(root.path("reviewKeywords")));
            result.put("analysisPoints", readStringList(root.path("analysisPoints")));
            result.put("generatedReview", root.path("generatedReview").asText(""));
            result.put("summary", root.path("summary").asText(""));
            result.put("llmEnabled", true);
            return result;
        } catch (Exception e) {
            result.put("llmEnabled", false);
            result.put("query", safe(query));
            return result;
        }
    }

    public boolean isConfigured() {
        return llmProperties.isEnabled()
                && StringUtils.hasText(llmProperties.getApiKey())
                && StringUtils.hasText(llmProperties.getBaseUrl())
                && StringUtils.hasText(llmProperties.getModel());
    }

    public List<String> getMissingReasons() {
        List<String> missing = new ArrayList<>();
        if (!llmProperties.isEnabled()) missing.add("app.ai.llm.enabled=false");
        if (!StringUtils.hasText(llmProperties.getApiKey())) missing.add("app.ai.llm.api-key 为空");
        if (!StringUtils.hasText(llmProperties.getBaseUrl())) missing.add("app.ai.llm.base-url 为空");
        if (!StringUtils.hasText(llmProperties.getModel())) missing.add("app.ai.llm.model 为空");
        return missing;
    }

    private String fallbackMergedPrompt(String analysis,
                                        String designPrompt,
                                        String productPrompt,
                                        String designAdvice,
                                        String creativeType) {
        String merged = (safe(productPrompt) + "，" + safe(designPrompt) + "，" + safe(designAdvice) + "，" + safe(analysis))
                .replaceAll("\\s+", " ")
                .trim();
        if (merged.length() > 220) {
            merged = merged.substring(0, 220);
        }
        if (!StringUtils.hasText(merged)) {
            return "poster".equals(creativeType)
                    ? "剧场主视觉海报，电影感构图，戏剧灯光，舞台空间，版式高级，纯净背景"
                    : "剧场文创商品展示图，主体明确，印花清晰，真实商品摄影风格，高级陈列，纯净背景";
        }
        return merged;
    }

    private String buildFallbackPrompt(String userNeed,
                                       String creativeType,
                                       String theme,
                                       String mood,
                                       String audience,
                                       List<String> itemSummaries,
                                       String creativeReview) {
        String typeText = "poster".equals(creativeType) ? "海报" : "文创产品";
        return "━━━━━━━━━━━━━━━━━━━━━━\n"
                + "📋 需求分析\n"
                + "━━━━━━━━━━━━━━━━━━━━━━\n"
                + "用户希望围绕“" + safe(userNeed) + "”生成一组可直接用于AI绘图的文创视觉提示词，重点产品类型为“" + typeText + "”，整体气质参考“"
                + safe(theme) + " / " + safe(mood) + "”，面向“" + safe(audience) + "”。\n\n"
                + "━━━━━━━━━━━━━━━━━━━━━━\n"
                + "🎨 平面设计图提示词\n"
                + "━━━━━━━━━━━━━━━━━━━━━━\n"
                + "围绕“" + safe(userNeed) + "”的核心视觉元素，生成高级平面主视觉设计稿，风格克制且有文创品牌感，配色结合“"
                + safe(theme) + "”与“" + safe(mood) + "”，构图完整、层次清晰，加入适量装饰纹样与版式留白，纯净背景，超清细节，适合用于AI生成平面设计图。\n\n"
                + "━━━━━━━━━━━━━━━━━━━━━━\n"
                + "📦 成品展示图提示词\n"
                + "━━━━━━━━━━━━━━━━━━━━━━\n"
                + "将同一套视觉语言应用到真实" + typeText + "成品展示图中，突出材质质感、真实光影、完整构图与高级陈列氛围，体现可落地的文创成品效果，超清画质，适合用于AI生成产品展示图。\n\n"
                + "━━━━━━━━━━━━━━━━━━━━━━\n"
                + "💡 设计建议\n"
                + "━━━━━━━━━━━━━━━━━━━━━━\n"
                + "建议优先保证用户原始需求为主体，辅助灵感可参考“" + String.join("、", itemSummaries == null ? List.of() : itemSummaries)
                + "”，风格气质可参考“" + safe(creativeReview) + "”。";
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
        if (!StringUtils.hasText(content)) return "{}";
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

    private String safe(String text) {
        return text == null ? "" : text.trim();
    }
}
