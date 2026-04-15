package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.dto.AiChatRequest;
import com.example.springboot.dto.AiChatResponse;
import com.example.springboot.service.AiChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Tag(name = "AI 对话", description = "剧场助手对话接口")
public class AiChatController {

    private final AiChatService aiChatService;

    @PostMapping("/chat")
    @Operation(summary = "剧场助手对话", description = "支持 sessionId 自动记忆最近 N 轮；前端仅传 sessionId+message 即可多轮对话")
    public Result chat(@Valid @RequestBody AiChatRequest request) {
        AiChatResponse data = aiChatService.chat(request);
        return Result.success(data);
    }
}
