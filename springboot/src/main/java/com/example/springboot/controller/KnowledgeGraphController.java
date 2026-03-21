package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.KnowledgeNode;
import com.example.springboot.service.KnowledgeGraphService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/knowledge-graph")
@RequiredArgsConstructor
public class KnowledgeGraphController {

    private final KnowledgeGraphService graphService;

    /**
     * 获取完整知识图谱（所有节点 + 所有边）
     */
    @GetMapping("/full")
    public Result getFullGraph() {
        return Result.success(graphService.getFullGraph());
    }

    /**
     * 获取某个节点及其邻居组成的子图
     */
    @GetMapping("/node/{id}/neighborhood")
    public Result getNodeNeighborhood(@PathVariable Long id) {
        return Result.success(graphService.getNodeNeighborhood(id));
    }

    /**
     * 获取节点详情
     */
    @GetMapping("/node/{id}")
    public Result getNodeDetail(@PathVariable Long id) {
        return Result.success(graphService.getNodeDetail(id));
    }

    /**
     * 搜索节点
     */
    @GetMapping("/search")
    public Result searchNodes(@RequestParam String keyword) {
        return Result.success(graphService.searchNodes(keyword));
    }

    /**
     * 按类型筛选子图，types 以逗号分隔，如 PLAY,ACTOR
     */
    @GetMapping("/filter")
    public Result getGraphByTypes(@RequestParam String types) {
        List<String> typeList = List.of(types.split(","));
        return Result.success(graphService.getGraphByTypes(typeList));
    }

    /**
     * 按类型查询节点列表
     */
    @GetMapping("/nodes")
    public Result getNodesByType(@RequestParam String type) {
        KnowledgeNode.NodeType nodeType = KnowledgeNode.NodeType.valueOf(type.toUpperCase());
        return Result.success(graphService.getNodesByType(nodeType));
    }

    /**
     * 初始化种子数据（仅首次调用生效）
     */
    @PostMapping("/init")
    public Result initSeedData() {
        return Result.success(graphService.initSeedData());
    }

    /**
     * 清空所有数据（危险操作）
     */
    @DeleteMapping("/clear")
    public Result clearAllData() {
        return Result.success(graphService.clearAllData());
    }
}
