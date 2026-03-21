package com.example.springboot.controller;

import com.example.springboot.service.PlayRatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 剧目评分/短评接口（豆瓣式）。
 * 未接登录时可用固定 userId（如 1）测试；接入登录后从 token 取 userId。
 */
@RestController
@RequestMapping("/api/play-rating")
@RequiredArgsConstructor
public class PlayRatingController {

    private final PlayRatingService ratingService;

    /** 提交或更新评分（短评可选） */
    @PostMapping
    public Map<String, Object> save(
            @RequestParam Long userId,
            @RequestParam Long playId,
            @RequestParam Integer rating,
            @RequestParam(required = false) String content) {
        return Map.of(
                "code", "200",
                "msg", "成功",
                "data", ratingService.saveOrUpdate(userId, playId, rating, content != null ? content : "")
        );
    }

    /** 某剧目的评分汇总：均分、人数 */
    @GetMapping("/summary/{playId}")
    public Map<String, Object> summary(@PathVariable Long playId) {
        return Map.of("code", "200", "msg", "成功", "data", ratingService.getPlayScoreSummary(playId));
    }

    /** 某剧目的评分列表（分页，含短评） */
    @GetMapping("/list/{playId}")
    public Map<String, Object> list(
            @PathVariable Long playId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<Map<String, Object>> list = ratingService.getByPlayId(playId, page, size);
        return Map.of("code", "200", "msg", "成功", "data", list);
    }
}
