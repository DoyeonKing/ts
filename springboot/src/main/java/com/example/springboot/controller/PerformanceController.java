package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.Performance;
import com.example.springboot.service.PerformanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/performances")
@RequiredArgsConstructor
public class PerformanceController {

    private final PerformanceService performanceService;

    @GetMapping("/play/{playId}")
    public Result listByPlayId(@PathVariable Long playId) {
        List<Performance> performances = performanceService.findByPlayId(playId);
        return Result.success(performances);
    }

    @PostMapping("/init")
    public Result initSeedData() {
        return Result.success(performanceService.initSeedData());
    }
}
