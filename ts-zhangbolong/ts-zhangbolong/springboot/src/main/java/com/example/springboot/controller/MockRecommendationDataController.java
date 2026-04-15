package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.service.MockRecommendationDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai/mock-data")
@RequiredArgsConstructor
public class MockRecommendationDataController {

    private final MockRecommendationDataService mockDataService;

    @GetMapping("/summary")
    public Result summary() {
        return Result.success(mockDataService.getSummary());
    }

    @GetMapping("/plays")
    public Result plays(@RequestParam(defaultValue = "20") int limit) {
        List<?> data = mockDataService.getPlays().stream()
                .limit(Math.max(1, Math.min(limit, 100)))
                .toList();
        return Result.success(Map.of("count", data.size(), "items", data));
    }

    @GetMapping("/users")
    public Result users(@RequestParam(defaultValue = "50") int limit) {
        List<?> data = mockDataService.getUserPreferences().stream()
                .limit(Math.max(1, Math.min(limit, 200)))
                .toList();
        return Result.success(Map.of("count", data.size(), "items", data));
    }
}
