package com.example.springboot.controller;

import com.example.springboot.entity.KnowledgeNode.NodeType;
import com.example.springboot.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    /**
     * 根据剧目推荐：相似剧目、相关演员等
     * GET /api/recommendations/play/{playId}
     */
    @GetMapping("/play/{playId}")
    public Map<String, Object> recommendForPlay(@PathVariable Long playId) {
        return recommendationService.recommendForPlay(playId);
    }

    /**
     * 根据演员推荐：其剧目、合作演员等
     * GET /api/recommendations/actor/{actorId}
     */
    @GetMapping("/actor/{actorId}")
    public Map<String, Object> recommendForActor(@PathVariable Long actorId) {
        return recommendationService.recommendForActor(actorId);
    }

    /**
     * 根据任意节点 ID 推荐（图谱内节点）
     * GET /api/recommendations/node/{nodeId}
     */
    @GetMapping("/node/{nodeId}")
    public Map<String, Object> recommendForNode(@PathVariable Long nodeId) {
        return recommendationService.recommendForNode(
                nodeId,
                List.of(NodeType.PLAY, NodeType.ACTOR, NodeType.TAG, NodeType.TERMINOLOGY, NodeType.VENUE),
                6, 6
        );
    }

    /**
     * 根据剧目名称查找图谱节点 ID（用于详情页未传 nodeId 时）
     * GET /api/recommendations/lookup?name=哈姆雷特&type=PLAY
     */
    @GetMapping("/lookup")
    public Map<String, Object> lookupNode(@RequestParam String name, @RequestParam String type) {
        NodeType nodeType = NodeType.valueOf(type.toUpperCase());
        Optional<Long> id = recommendationService.findNodeIdByNameAndType(name.trim(), nodeType);
        return Map.of("found", id.isPresent(), "nodeId", id.orElse(-1L));
    }
}
