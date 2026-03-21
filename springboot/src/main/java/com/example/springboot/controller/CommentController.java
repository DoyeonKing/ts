package com.example.springboot.controller;

import com.example.springboot.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 评论区接口（豆瓣式）：支持对剧目、对某条短评的评论与楼中楼。
 */
@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /** 发表评论：targetType=PLAY 对剧目，targetType=RATING 对某条短评；parentId 为空为一级，非空为回复 */
    @PostMapping
    public Map<String, Object> create(
            @RequestParam Long userId,
            @RequestParam String targetType,
            @RequestParam Long targetId,
            @RequestParam(required = false) Long parentId,
            @RequestParam String content) {
        return Map.of(
                "code", "200",
                "msg", "成功",
                "data", commentService.create(userId, targetType, targetId, parentId, content)
        );
    }

    /** 某剧目/某条短评下的评论列表（分页，一级 + 楼中楼） */
    @GetMapping("/list")
    public Map<String, Object> list(
            @RequestParam String targetType,
            @RequestParam Long targetId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<Map<String, Object>> list = commentService.getByTarget(targetType, targetId, page, size);
        long total = commentService.countByTarget(targetType, targetId);
        return Map.of(
                "code", "200",
                "msg", "成功",
                "data", Map.of("list", list, "total", total)
        );
    }
}
