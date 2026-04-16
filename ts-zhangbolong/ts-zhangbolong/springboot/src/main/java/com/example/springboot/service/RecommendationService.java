package com.example.springboot.service;

import com.example.springboot.entity.KnowledgeEdge;
import com.example.springboot.entity.KnowledgeNode;
import com.example.springboot.entity.KnowledgeNode.NodeType;
import com.example.springboot.repository.KnowledgeEdgeRepository;
import com.example.springboot.repository.KnowledgeNodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 基于知识图谱的智能推荐：根据用户当前查看的节点（剧目/演员等）推荐相关内容。
 */
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final KnowledgeNodeRepository nodeRepository;
    private final KnowledgeEdgeRepository edgeRepository;

    /** 关系类型权重：权重越高推荐优先级越高 */
    private static final Map<String, Integer> RELATION_WEIGHT = Map.of(
            "SIMILAR_TO", 10,
            "PERFORMS_IN", 8,
            "HAS_TAG", 6,
            "HAS_TERMINOLOGY", 5,
            "PERFORMED_AT", 5,
            "WORKS_AT", 4,
            "RELATED_TO", 3
    );

    private static int weightOf(String relationType) {
        return RELATION_WEIGHT.getOrDefault(relationType, 1);
    }

    /** 推荐理由文案 */
    private static final Map<String, String> RELATION_REASON = Map.of(
            "SIMILAR_TO", "相似剧目",
            "PERFORMS_IN", "参演该剧",
            "HAS_TAG", "同标签",
            "HAS_TERMINOLOGY", "相关术语",
            "PERFORMED_AT", "同场馆演出",
            "WORKS_AT", "同场馆",
            "RELATED_TO", "相关"
    );

    private static String reasonOf(String relationType, String label) {
        if (label != null && !label.isBlank()) return label;
        return RELATION_REASON.getOrDefault(relationType, "相关推荐");
    }

    /**
     * 根据剧目 ID 推荐：相似剧目 + 相关演员等
     */
    public Map<String, Object> recommendForPlay(Long playId) {
        return recommendForNode(playId, List.of(NodeType.PLAY, NodeType.ACTOR, NodeType.TAG, NodeType.TERMINOLOGY), 6, 4);
    }

    /**
     * 根据演员 ID 推荐：其参演/相关剧目 + 合作演员
     */
    public Map<String, Object> recommendForActor(Long actorId) {
        return recommendForNode(actorId, List.of(NodeType.PLAY, NodeType.ACTOR, NodeType.VENUE), 6, 4);
    }

    /**
     * 通用：根据任意节点 ID 推荐相关节点，按类型分组并带推荐理由
     *
     * @param nodeId      当前节点 ID
     * @param allowTypes  允许推荐的节点类型
     * @param maxPerType  每种类型最多返回数量
     * @param maxOther    其他类型合计最多数量
     */
    public Map<String, Object> recommendForNode(Long nodeId, List<NodeType> allowTypes, int maxPerType, int maxOther) {
        KnowledgeNode source = nodeRepository.findById(nodeId).orElse(null);
        if (source == null) {
            return Map.of("plays", List.of(), "actors", List.of(), "others", List.of());
        }

        Set<NodeType> allowSet = new HashSet<>(allowTypes);
        // 1-hop: (nodeId, neighbor, relationType, label) -> score
        List<KnowledgeEdge> edges = edgeRepository.findEdgesByNodeId(nodeId);
        Map<Long, ScoreReason> scores = new HashMap<>();

        for (KnowledgeEdge e : edges) {
            Long otherId = e.getSourceNodeId().equals(nodeId) ? e.getTargetNodeId() : e.getSourceNodeId();
            if (otherId.equals(nodeId)) continue;
            int w = weightOf(e.getRelationType());
            String reason = reasonOf(e.getRelationType(), e.getLabel());
            scores.merge(otherId, new ScoreReason(w, reason), ScoreReason::add);
        }

        // 2-hop: 邻居的邻居（降权）
        Set<Long> oneHopIds = new HashSet<>(scores.keySet());
        for (Long mid : oneHopIds) {
            for (KnowledgeEdge e2 : edgeRepository.findEdgesByNodeId(mid)) {
                Long otherId = e2.getSourceNodeId().equals(mid) ? e2.getTargetNodeId() : e2.getSourceNodeId();
                if (otherId.equals(nodeId) || oneHopIds.contains(otherId)) continue;
                int w = weightOf(e2.getRelationType()) / 2;
                scores.merge(otherId, new ScoreReason(w, "间接相关"), ScoreReason::add);
            }
        }

        // 按类型分组并排序
        List<Long> sortedIds = scores.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue().score, a.getValue().score))
                .map(Map.Entry::getKey)
                .toList();

        Map<Long, KnowledgeNode> nodeMap = new HashMap<>();
        nodeRepository.findAllById(sortedIds).forEach(n -> nodeMap.put(n.getId(), n));

        List<Map<String, Object>> plays = new ArrayList<>();
        List<Map<String, Object>> actors = new ArrayList<>();
        List<Map<String, Object>> others = new ArrayList<>();

        for (Long id : sortedIds) {
            KnowledgeNode n = nodeMap.get(id);
            if (n == null || !allowSet.contains(n.getNodeType())) continue;

            ScoreReason sr = scores.get(id);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", n.getId());
            item.put("name", n.getName());
            item.put("nodeType", n.getNodeType().name());
            item.put("description", n.getDescription());
            item.put("reason", sr.reason);
            item.put("score", sr.score);

            if (n.getNodeType() == NodeType.PLAY && plays.size() < maxPerType) plays.add(item);
            else if (n.getNodeType() == NodeType.ACTOR && actors.size() < maxPerType) actors.add(item);
            else if (others.size() < maxOther) others.add(item);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("plays", plays);
        result.put("actors", actors);
        result.put("others", others);
        result.put("source", Map.of("id", source.getId(), "name", source.getName(), "nodeType", source.getNodeType().name()));
        return result;
    }

    /**
     * 按名称模糊匹配节点 ID（用于剧目详情页、演员详情页用 name 查图谱节点）
     */
    public Optional<Long> findNodeIdByNameAndType(String name, NodeType type) {
        return nodeRepository.findByNameAndNodeType(name, type).map(KnowledgeNode::getId);
    }

    private static class ScoreReason {
        int score;
        String reason;

        ScoreReason(int score, String reason) {
            this.score = score;
            this.reason = reason;
        }

        ScoreReason add(ScoreReason other) {
            this.score += other.score;
            if (other.reason != null && !other.reason.equals(this.reason)) this.reason = this.reason + " / " + other.reason;
            return this;
        }
    }
}
