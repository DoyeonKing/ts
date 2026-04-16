package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.service.EnhancedAdvancedTheaterRecommendationService;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/advanced-recommendation")
@RequiredArgsConstructor
@Validated
public class AdvancedRecommendationController {

    private final EnhancedAdvancedTheaterRecommendationService advancedTheaterRecommendationService;

    @PostMapping("/plan")
    public Result buildPlan(@RequestBody AdvancedRequest request) {
        return Result.success(advancedTheaterRecommendationService.buildAdvancedPlan(
                request == null ? null : request.getQuery(),
                request == null ? null : request.getUserId()
        ));
    }

    @Data
    public static class AdvancedRequest {
        @NotBlank(message = "query 不能为空")
        private String query;
        private Long userId;
    }
}
