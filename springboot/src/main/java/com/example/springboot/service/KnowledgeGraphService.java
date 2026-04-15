package com.example.springboot.service;

import com.example.springboot.entity.KnowledgeEdge;
import com.example.springboot.entity.KnowledgeNode;
import com.example.springboot.entity.KnowledgeNode.NodeType;
import com.example.springboot.repository.KnowledgeEdgeRepository;
import com.example.springboot.repository.KnowledgeNodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KnowledgeGraphService {

    private final KnowledgeNodeRepository nodeRepository;
    private final KnowledgeEdgeRepository edgeRepository;

    private static final String EMPTY_JSON_OBJECT = "{}";

    private void ensureSeedDataInitialized() {
        // 严格数据库模式：不再自动初始化或自动补数据
        // 数据由数据库现有内容与显式初始化接口控制
    }

    /**
     * 获取完整的知识图谱（节点 + 边）
     */
    public Map<String, Object> getFullGraph() {
        ensureSeedDataInitialized();
        List<KnowledgeNode> nodes = nodeRepository.findAll();
        List<KnowledgeEdge> edges = edgeRepository.findAll();
        return buildGraphResponse(nodes, edges);
    }

    /**
     * 获取某个节点及其一度邻居组成的子图
     */
    public Map<String, Object> getNodeNeighborhood(Long nodeId) {
        ensureSeedDataInitialized();
        KnowledgeNode center = nodeRepository.findById(nodeId)
                .orElseThrow(() -> new RuntimeException("节点不存在: " + nodeId));

        List<KnowledgeNode> neighbors = nodeRepository.findNeighbors(nodeId);
        List<KnowledgeNode> allNodes = new ArrayList<>(neighbors);
        allNodes.add(center);

        List<Long> nodeIds = allNodes.stream().map(KnowledgeNode::getId).toList();
        List<KnowledgeEdge> edges = edgeRepository.findEdgesAmongNodes(nodeIds);

        return buildGraphResponse(allNodes, edges);
    }

    /**
     * 按关键词搜索节点
     */
    public List<KnowledgeNode> searchNodes(String keyword) {
        ensureSeedDataInitialized();
        return nodeRepository.findByNameContaining(keyword);
    }

    /**
     * 按类型查询节点
     */
    public List<KnowledgeNode> getNodesByType(NodeType type) {
        ensureSeedDataInitialized();
        return nodeRepository.findByNodeType(type);
    }

    /**
     * 获取节点详情（含关联边数）
     */
    public Map<String, Object> getNodeDetail(Long nodeId) {
        ensureSeedDataInitialized();
        KnowledgeNode node = nodeRepository.findById(nodeId)
                .orElseThrow(() -> new RuntimeException("节点不存在: " + nodeId));
        List<KnowledgeEdge> edges = edgeRepository.findEdgesByNodeId(nodeId);

        Map<String, Object> result = new HashMap<>();
        result.put("node", node);
        result.put("edges", edges);
        result.put("linkCount", edges.size());
        return result;
    }

    /**
     * 按类型筛选的子图
     */
    public Map<String, Object> getGraphByTypes(List<String> types) {
        ensureSeedDataInitialized();
        List<NodeType> nodeTypes = types.stream()
                .map(t -> NodeType.valueOf(t.toUpperCase()))
                .toList();

        List<KnowledgeNode> nodes = new ArrayList<>();
        for (NodeType type : nodeTypes) {
            nodes.addAll(nodeRepository.findByNodeType(type));
        }

        List<Long> nodeIds = nodes.stream().map(KnowledgeNode::getId).toList();
        List<KnowledgeEdge> edges = edgeRepository.findEdgesAmongNodes(nodeIds);

        return buildGraphResponse(nodes, edges);
    }

    private Map<String, Object> buildGraphResponse(List<KnowledgeNode> nodes, List<KnowledgeEdge> edges) {
        Set<Long> nodeIdSet = nodes.stream().map(KnowledgeNode::getId).collect(Collectors.toSet());

        Map<Long, Long> linkCountMap = new HashMap<>();
        for (KnowledgeEdge edge : edges) {
            linkCountMap.merge(edge.getSourceNodeId(), 1L, Long::sum);
            linkCountMap.merge(edge.getTargetNodeId(), 1L, Long::sum);
        }

        List<Map<String, Object>> nodeList = nodes.stream().map(n -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", n.getId());
            map.put("name", n.getName());
            map.put("nodeType", n.getNodeType().name());
            map.put("description", n.getDescription());
            map.put("extraData", n.getExtraData());
            map.put("linkCount", linkCountMap.getOrDefault(n.getId(), 0L));
            return map;
        }).toList();

        List<Map<String, Object>> edgeList = edges.stream()
                .filter(e -> nodeIdSet.contains(e.getSourceNodeId()) && nodeIdSet.contains(e.getTargetNodeId()))
                .map(e -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", e.getId());
                    map.put("source", e.getSourceNodeId());
                    map.put("target", e.getTargetNodeId());
                    map.put("relationType", e.getRelationType());
                    map.put("label", e.getLabel());
                    map.put("weight", e.getWeight());
                    return map;
                }).toList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("nodes", nodeList);
        result.put("edges", edgeList);
        return result;
    }

    @Transactional
    public void enrichExperienceTags() {
        KnowledgeNode tagWeekend = createOrUpdateTag("周末友好", "周末排期稳定、时间友好，适合周末观演");
        KnowledgeNode tagBrain = createOrUpdateTag("烧脑", "剧情信息量大、结构复杂，适合喜欢推理和思辨的观众");
        KnowledgeNode tagRewatch = createOrUpdateTag("适合二刷", "细节丰富、复看仍有新发现，二刷体验更佳");

        KnowledgeNode hamlet = nodeRepository.findByNameAndNodeType("哈姆雷特", NodeType.PLAY).orElse(null);
        KnowledgeNode lear = nodeRepository.findByNameAndNodeType("李尔王", NodeType.PLAY).orElse(null);
        KnowledgeNode teahouse = nodeRepository.findByNameAndNodeType("茶馆", NodeType.PLAY).orElse(null);
        KnowledgeNode romeo = nodeRepository.findByNameAndNodeType("罗密欧与朱丽叶", NodeType.PLAY).orElse(null);
        KnowledgeNode swan = nodeRepository.findByNameAndNodeType("天鹅湖", NodeType.PLAY).orElse(null);
        KnowledgeNode camellia = nodeRepository.findByNameAndNodeType("茶花女", NodeType.PLAY).orElse(null);

        linkIfMissing(hamlet, tagBrain, "HAS_TAG", "高信息密度");
        linkIfMissing(lear, tagBrain, "HAS_TAG", "结构复杂");
        linkIfMissing(teahouse, tagRewatch, "HAS_TAG", "人物层次丰富");
        linkIfMissing(hamlet, tagRewatch, "HAS_TAG", "经典台词细节多");

        linkIfMissing(romeo, tagWeekend, "HAS_TAG", "周末热门场");
        linkIfMissing(swan, tagWeekend, "HAS_TAG", "周末观演友好");
        linkIfMissing(camellia, tagWeekend, "HAS_TAG", "周末场次稳定");
    }

    private KnowledgeNode createOrUpdateTag(String name, String description) {
        KnowledgeNode node = nodeRepository.findByNameAndNodeType(name, NodeType.TAG)
                .orElseGet(() -> createNode(name, NodeType.TAG, description, EMPTY_JSON_OBJECT));
        boolean changed = false;
        if (node.getDescription() == null || node.getDescription().isBlank()) {
            node.setDescription(description);
            changed = true;
        }
        if (node.getExtraData() == null || node.getExtraData().isBlank()) {
            node.setExtraData(EMPTY_JSON_OBJECT);
            changed = true;
        }
        return changed ? nodeRepository.save(node) : node;
    }

    private void linkIfMissing(KnowledgeNode source, KnowledgeNode target, String relationType, String label) {
        if (source == null || target == null) return;
        if (edgeRepository.existsBySourceNodeIdAndTargetNodeIdAndRelationType(source.getId(), target.getId(), relationType)
                || edgeRepository.existsBySourceNodeIdAndTargetNodeIdAndRelationType(target.getId(), source.getId(), relationType)) {
            return;
        }
        createEdge(source, target, relationType, label);
    }

    // ========== 初始化种子数据 ==========

    @Transactional
    public String initSeedData() {
        if (nodeRepository.count() > 0) {
            return "数据已存在，跳过初始化。如需重新初始化，请先清空数据。";
        }

        // --- 剧目 ---
        KnowledgeNode hamlet = createNode("哈姆雷特", NodeType.PLAY,
                "莎士比亚四大悲剧之一，丹麦王子复仇与人性抉择",
                "{\"genre\":\"悲剧\",\"rating\":9.5}");
        KnowledgeNode romeo = createNode("罗密欧与朱丽叶", NodeType.PLAY,
                "莎士比亚经典爱情故事，两大家族下的禁忌之恋",
                "{\"genre\":\"爱情悲剧\",\"rating\":9.3}");
        KnowledgeNode swan = createNode("天鹅湖", NodeType.PLAY,
                "柴可夫斯基芭蕾舞剧，王子与天鹅公主的童话",
                "{\"genre\":\"芭蕾\",\"rating\":9.0}");
        KnowledgeNode camellia = createNode("茶花女", NodeType.PLAY,
                "威尔第歌剧，巴黎名妓与青年贵族的爱情悲剧",
                "{\"genre\":\"歌剧\",\"rating\":9.4}");
        KnowledgeNode lear = createNode("李尔王", NodeType.PLAY,
                "莎士比亚四大悲剧，权力、亲情与背叛",
                "{\"genre\":\"悲剧\",\"rating\":9.1}");
        KnowledgeNode teahouse = createNode("茶馆", NodeType.PLAY,
                "老舍经典话剧，三幕写尽半个世纪的沧桑变迁",
                "{\"genre\":\"话剧\",\"rating\":9.6}");

        // --- 演员 ---
        KnowledgeNode pu = createNode("濮存昕", NodeType.ACTOR,
                "著名话剧演员，北京人艺",
                "{\"awards\":[\"梅花奖\",\"文华奖\"],\"specialty\":\"话剧、莎士比亚\"}");
        KnowledgeNode yuan = createNode("袁泉", NodeType.ACTOR,
                "演员，话剧与影视",
                "{\"awards\":[\"梅花奖\",\"金狮奖\"],\"specialty\":\"话剧、音乐剧\"}");
        KnowledgeNode he = createNode("何冰", NodeType.ACTOR,
                "北京人艺演员",
                "{\"awards\":[\"梅花奖\"],\"specialty\":\"话剧、喜剧\"}");

        // --- 标签 ---
        KnowledgeNode tagShakespeare = createNode("莎士比亚", NodeType.TAG, "英国文艺复兴时期剧作家", null);
        KnowledgeNode tagTragedy = createNode("悲剧", NodeType.TAG, "以主人公的不幸结局引发观众的悲悯", null);
        KnowledgeNode tagLove = createNode("爱情", NodeType.TAG, "以爱情为主题的剧目", null);
        KnowledgeNode tagClassic = createNode("经典", NodeType.TAG, "经久不衰的经典剧目", null);
        KnowledgeNode tagBallet = createNode("芭蕾", NodeType.TAG, "以芭蕾舞蹈为表现形式", null);
        KnowledgeNode tagOpera = createNode("歌剧", NodeType.TAG, "以歌唱和音乐为主要表现手段", null);
        KnowledgeNode tagDrama = createNode("话剧", NodeType.TAG, "以对话为主要表现手段的戏剧", null);

        // --- 术语 ---
        KnowledgeNode termSoliloquy = createNode("独白", NodeType.TERMINOLOGY,
                "角色在舞台上独自说出内心想法，观众可闻但剧中其他角色不知", null);
        KnowledgeNode termAside = createNode("旁白", NodeType.TERMINOLOGY,
                "角色对观众说的话，不被舞台上其他角色听到", null);
        KnowledgeNode termStageWalk = createNode("台步", NodeType.TERMINOLOGY,
                "演员在舞台上的行走方式与步法", null);
        KnowledgeNode termCurtainCall = createNode("谢幕", NodeType.TERMINOLOGY,
                "演出结束后演员向观众致谢的仪式", null);
        KnowledgeNode termIntermission = createNode("幕间", NodeType.TERMINOLOGY,
                "演出中两幕之间的休息时间", null);
        KnowledgeNode termSD = createNode("SD", NodeType.TERMINOLOGY,
                "Special Day，指特别场次（如首场、末场、生日场等）", null);
        KnowledgeNode termEncore = createNode("返场", NodeType.TERMINOLOGY,
                "演出结束后应观众掌声再次登台表演的环节，常见于音乐会、歌剧等", null);
        KnowledgeNode termCast = createNode("卡司", NodeType.TERMINOLOGY,
                "Cast 音译，指一场演出的演员阵容、角色分配", null);
        KnowledgeNode termLastShow = createNode("末场", NodeType.TERMINOLOGY,
                "一部戏在某地或某一轮演出的最后一场", null);
        KnowledgeNode termPremiere = createNode("首演", NodeType.TERMINOLOGY,
                "一部戏第一次公开演出的场次", null);

        // --- 场馆 ---
        KnowledgeNode venueNational = createNode("国家大剧院", NodeType.VENUE,
                "位于北京天安门广场西侧，国家级表演艺术中心",
                "{\"city\":\"北京\",\"capacity\":5452}");
        KnowledgeNode venueRenyi = createNode("北京人艺", NodeType.VENUE,
                "北京人民艺术剧院，中国话剧艺术殿堂",
                "{\"city\":\"北京\",\"capacity\":1200}");

        // ========== 创建关系 ==========

        // 演员-剧目
        createEdge(pu, hamlet, "PERFORMS_IN", "饰 哈姆雷特");
        createEdge(pu, lear, "PERFORMS_IN", "饰 李尔王");
        createEdge(pu, teahouse, "PERFORMS_IN", "饰 常四爷");
        createEdge(yuan, hamlet, "PERFORMS_IN", "饰 奥菲利亚");
        createEdge(he, lear, "PERFORMS_IN", "饰 葛罗斯特");
        createEdge(he, teahouse, "PERFORMS_IN", "饰 松二爷");

        // 剧目-标签
        createEdge(hamlet, tagShakespeare, "HAS_TAG", null);
        createEdge(hamlet, tagTragedy, "HAS_TAG", null);
        createEdge(hamlet, tagClassic, "HAS_TAG", null);
        createEdge(romeo, tagShakespeare, "HAS_TAG", null);
        createEdge(romeo, tagLove, "HAS_TAG", null);
        createEdge(romeo, tagTragedy, "HAS_TAG", null);
        createEdge(swan, tagBallet, "HAS_TAG", null);
        createEdge(swan, tagClassic, "HAS_TAG", null);
        createEdge(swan, tagLove, "HAS_TAG", null);
        createEdge(camellia, tagOpera, "HAS_TAG", null);
        createEdge(camellia, tagLove, "HAS_TAG", null);
        createEdge(lear, tagShakespeare, "HAS_TAG", null);
        createEdge(lear, tagTragedy, "HAS_TAG", null);
        createEdge(teahouse, tagDrama, "HAS_TAG", null);
        createEdge(teahouse, tagClassic, "HAS_TAG", null);

        // 剧目-术语
        createEdge(hamlet, termSoliloquy, "HAS_TERMINOLOGY", "「生存还是毁灭」经典独白");
        createEdge(hamlet, termAside, "HAS_TERMINOLOGY", null);
        createEdge(lear, termSoliloquy, "HAS_TERMINOLOGY", null);
        createEdge(teahouse, termIntermission, "HAS_TERMINOLOGY", "三幕话剧");
        createEdge(camellia, termIntermission, "HAS_TERMINOLOGY", null);

        // 更多剧目-术语（返场、卡司、首演等）
        createEdge(camellia, termEncore, "HAS_TERMINOLOGY", "歌剧常见返场");
        createEdge(swan, termEncore, "HAS_TERMINOLOGY", null);
        createEdge(hamlet, termCast, "HAS_TERMINOLOGY", null);
        createEdge(teahouse, termCast, "HAS_TERMINOLOGY", null);
        createEdge(hamlet, termPremiere, "HAS_TERMINOLOGY", null);
        createEdge(lear, termLastShow, "HAS_TERMINOLOGY", null);

        // 剧目-场馆
        createEdge(hamlet, venueNational, "PERFORMED_AT", "2024年演出季");
        createEdge(teahouse, venueRenyi, "PERFORMED_AT", "驻场演出");
        createEdge(lear, venueRenyi, "PERFORMED_AT", null);
        createEdge(swan, venueNational, "PERFORMED_AT", null);

        // 剧目-剧目（相似）
        createEdge(hamlet, lear, "SIMILAR_TO", "同为莎翁四大悲剧");
        createEdge(hamlet, romeo, "SIMILAR_TO", "同为莎翁作品");
        createEdge(romeo, lear, "SIMILAR_TO", "同为莎翁作品");

        // 演员-场馆
        createEdge(pu, venueNational, "WORKS_AT", null);
        createEdge(pu, venueRenyi, "WORKS_AT", null);
        createEdge(he, venueRenyi, "WORKS_AT", null);

        // 术语通用关系
        createEdge(termStageWalk, termCurtainCall, "RELATED_TO", "舞台表演基础");
        createEdge(termSoliloquy, termAside, "RELATED_TO", "均为角色独立发声");
        createEdge(termSD, termCurtainCall, "RELATED_TO", "特殊场次谢幕");

        return "初始化完成！共创建 " + nodeRepository.count() + " 个节点，" + edgeRepository.count() + " 条关系。";
    }

    @Transactional
    public String clearAllData() {
        edgeRepository.deleteAll();
        nodeRepository.deleteAll();
        return "已清空所有知识图谱数据。";
    }

    private KnowledgeNode createNode(String name, NodeType type, String desc, String extra) {
        KnowledgeNode node = new KnowledgeNode();
        node.setName(name);
        node.setNodeType(type);
        node.setDescription(desc);
        node.setExtraData(extra);
        return nodeRepository.save(node);
    }

    private void createEdge(KnowledgeNode source, KnowledgeNode target, String relationType, String label) {
        KnowledgeEdge edge = new KnowledgeEdge();
        edge.setSourceNodeId(source.getId());
        edge.setTargetNodeId(target.getId());
        edge.setRelationType(relationType);
        edge.setLabel(label);
        edge.setWeight(1.0);
        edgeRepository.save(edge);
    }
}
