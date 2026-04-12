package com.example.springboot.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AiChatRequest {

    /**
     * 用户当前输入
     */
    @NotBlank(message = "message 不能为空")
    private String message;

    /**
     * 对话场景（可选），例如：选剧建议/出行规划/观剧礼仪
     */
    private String scene;

    /**
     * 可选：用户 ID，便于后续做个性化
     */
    private Long userId;

    /**
     * 会话 ID（可选）。传入后将自动记忆该会话最近 N 轮对话。
     */
    private String sessionId;

    /**
     * 是否清空当前 sessionId 的历史（可选，默认 false）
     */
    private Boolean resetSession = false;

    /**
     * 可选：历史消息，基础版可不传
     */
    @Valid
    private List<HistoryMessage> history = new ArrayList<>();

    @Data
    public static class HistoryMessage {
        /**
         * user 或 assistant
         */
        @NotBlank(message = "history.role 不能为空")
        private String role;

        /**
         * 消息内容
         */
        @NotBlank(message = "history.content 不能为空")
        private String content;
    }
}
