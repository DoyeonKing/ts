USE theater_db;
SET NAMES utf8mb4;

-- 1) 补充术语节点
INSERT INTO knowledge_node (name, node_type, description, extra_data, created_at)
SELECT '旁白', 'TERMINOLOGY', '角色直接对观众说的话，舞台上其他角色通常听不见。',
       '{"category":"表演术语","usageScene":"剧情分析","noviceTip":"常与独白一起出现"}', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM knowledge_node WHERE name = '旁白' AND node_type = 'TERMINOLOGY'
);

INSERT INTO knowledge_node (name, node_type, description, extra_data, created_at)
SELECT '走位', 'TERMINOLOGY', '演员在舞台空间中的位置变化与移动路径。',
       '{"category":"舞台术语","usageScene":"排练/剧评","noviceTip":"常用于评价舞台调度"}', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM knowledge_node WHERE name = '走位' AND node_type = 'TERMINOLOGY'
);

INSERT INTO knowledge_node (name, node_type, description, extra_data, created_at)
SELECT '调度', 'TERMINOLOGY', '导演对演员、空间和节奏的整体安排。',
       '{"category":"舞台术语","usageScene":"导演分析","noviceTip":"常用于评价场面层次"}', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM knowledge_node WHERE name = '调度' AND node_type = 'TERMINOLOGY'
);

INSERT INTO knowledge_node (name, node_type, description, extra_data, created_at)
SELECT '对手戏', 'TERMINOLOGY', '角色之间强互动、强冲突的表演段落。',
       '{"category":"表演术语","usageScene":"演员评价","noviceTip":"常用来评价演员之间能否接住戏"}', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM knowledge_node WHERE name = '对手戏' AND node_type = 'TERMINOLOGY'
);

INSERT INTO knowledge_node (name, node_type, description, extra_data, created_at)
SELECT '爆发戏', 'TERMINOLOGY', '角色情绪集中释放、戏剧冲突陡然升级的关键段落。',
       '{"category":"表演术语","usageScene":"剧评/演员分析","noviceTip":"常对应用户说的演技炸裂"}', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM knowledge_node WHERE name = '爆发戏' AND node_type = 'TERMINOLOGY'
);

INSERT INTO knowledge_node (name, node_type, description, extra_data, created_at)
SELECT '收放', 'TERMINOLOGY', '演员控制情绪力度的能力，既能克制也能外放。',
       '{"category":"表演术语","usageScene":"演员评价","noviceTip":"常用来判断表演层次感"}', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM knowledge_node WHERE name = '收放' AND node_type = 'TERMINOLOGY'
);

INSERT INTO knowledge_node (name, node_type, description, extra_data, created_at)
SELECT '台词节奏', 'TERMINOLOGY', '对白推进的速度、停顿和呼吸处理方式。',
       '{"category":"表演术语","usageScene":"剧评","noviceTip":"节奏稳通常代表表演成熟"}', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM knowledge_node WHERE name = '台词节奏' AND node_type = 'TERMINOLOGY'
);

INSERT INTO knowledge_node (name, node_type, description, extra_data, created_at)
SELECT '舞美', 'TERMINOLOGY', '舞台视觉整体设计，包括布景、空间、道具等。',
       '{"category":"舞台术语","usageScene":"观后感/剧评","noviceTip":"画面感强通常在夸舞美"}', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM knowledge_node WHERE name = '舞美' AND node_type = 'TERMINOLOGY'
);

INSERT INTO knowledge_node (name, node_type, description, extra_data, created_at)
SELECT '转台', 'TERMINOLOGY', '通过旋转舞台完成场景切换的舞台机械方式。',
       '{"category":"舞台术语","usageScene":"舞台技术","noviceTip":"常见于大型音乐剧和商业制作"}', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM knowledge_node WHERE name = '转台' AND node_type = 'TERMINOLOGY'
);

INSERT INTO knowledge_node (name, node_type, description, extra_data, created_at)
SELECT '灯光切点', 'TERMINOLOGY', '灯光变化与剧情节奏精准咬合的时刻。',
       '{"category":"舞台术语","usageScene":"技术评价","noviceTip":"切点越准，完成度越高"}', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM knowledge_node WHERE name = '灯光切点' AND node_type = 'TERMINOLOGY'
);

INSERT INTO knowledge_node (name, node_type, description, extra_data, created_at)
SELECT '宣叙调', 'TERMINOLOGY', '歌剧中以叙述推进剧情为主的唱段。',
       '{"category":"歌剧术语","usageScene":"歌剧分析","noviceTip":"比咏叹调更偏叙事"}', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM knowledge_node WHERE name = '宣叙调' AND node_type = 'TERMINOLOGY'
);

INSERT INTO knowledge_node (name, node_type, description, extra_data, created_at)
SELECT '重唱', 'TERMINOLOGY', '两位或以上角色共同演唱的段落。',
       '{"category":"音乐术语","usageScene":"音乐剧/歌剧分析","noviceTip":"常是多人关系和情绪爆发点"}', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM knowledge_node WHERE name = '重唱' AND node_type = 'TERMINOLOGY'
);

INSERT INTO knowledge_node (name, node_type, description, extra_data, created_at)
SELECT '托举', 'TERMINOLOGY', '双人舞中一方支撑另一方完成腾空或定格动作。',
       '{"category":"芭蕾术语","usageScene":"舞段分析","noviceTip":"常用来评价技巧和默契"}', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM knowledge_node WHERE name = '托举' AND node_type = 'TERMINOLOGY'
);

INSERT INTO knowledge_node (name, node_type, description, extra_data, created_at)
SELECT '二刷', 'TERMINOLOGY', '同一部戏第二次观看。',
       '{"category":"观演黑话","usageScene":"观众交流","noviceTip":"通常说明作品值得重复观看"}', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM knowledge_node WHERE name = '二刷' AND node_type = 'TERMINOLOGY'
);

INSERT INTO knowledge_node (name, node_type, description, extra_data, created_at)
SELECT '官摄', 'TERMINOLOGY', '官方拍摄并发布的演出影像资料。',
       '{"category":"观演黑话","usageScene":"社群交流","noviceTip":"与路透和偷拍不同"}', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM knowledge_node WHERE name = '官摄' AND node_type = 'TERMINOLOGY'
);

INSERT INTO knowledge_node (name, node_type, description, extra_data, created_at)
SELECT '神卡司', 'TERMINOLOGY', '观众对特别满意或梦幻演员阵容的口语化称呼。',
       '{"category":"观演黑话","usageScene":"社群交流","noviceTip":"属于强主观但很常见的表达"}', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM knowledge_node WHERE name = '神卡司' AND node_type = 'TERMINOLOGY'
);

INSERT INTO knowledge_node (name, node_type, description, extra_data, created_at)
SELECT '视角遮挡', 'TERMINOLOGY', '由于位置、设备或舞美导致部分舞台看不全。',
       '{"category":"购票术语","usageScene":"选座/购票","noviceTip":"推荐系统可据此做避坑提示"}', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM knowledge_node WHERE name = '视角遮挡' AND node_type = 'TERMINOLOGY'
);

INSERT INTO knowledge_node (name, node_type, description, extra_data, created_at)
SELECT '性价比位', 'TERMINOLOGY', '价格和观演效果比较均衡的座位区域。',
       '{"category":"购票术语","usageScene":"购票建议","noviceTip":"适合预算型推荐"}', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM knowledge_node WHERE name = '性价比位' AND node_type = 'TERMINOLOGY'
);

-- 2) 补充推荐标签
INSERT INTO knowledge_node (name, node_type, description, extra_data, created_at)
SELECT '演技爆发力强', 'TAG', '适合情绪冲突大、关键场面爆发明显的作品。',
       '{"category":"推荐标签","dimension":"表演风格","scoreWeight":1.4}', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM knowledge_node WHERE name = '演技爆发力强' AND node_type = 'TAG'
);

INSERT INTO knowledge_node (name, node_type, description, extra_data, created_at)
SELECT '情绪张力强', 'TAG', '整体冲突感和压迫感较强，适合追求戏剧张力的观众。',
       '{"category":"推荐标签","dimension":"表演风格","scoreWeight":1.3}', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM knowledge_node WHERE name = '情绪张力强' AND node_type = 'TAG'
);

INSERT INTO knowledge_node (name, node_type, description, extra_data, created_at)
SELECT '台词密度高', 'TAG', '对白信息量大、语言表达强度高。',
       '{"category":"推荐标签","dimension":"表演风格","scoreWeight":1.2}', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM knowledge_node WHERE name = '台词密度高' AND node_type = 'TAG'
);

INSERT INTO knowledge_node (name, node_type, description, extra_data, created_at)
SELECT '群像戏', 'TAG', '多角色共同推进剧情，人物关系网复杂。',
       '{"category":"推荐标签","dimension":"结构特征","scoreWeight":1.1}', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM knowledge_node WHERE name = '群像戏' AND node_type = 'TAG'
);

INSERT INTO knowledge_node (name, node_type, description, extra_data, created_at)
SELECT '唱段突出', 'TAG', '核心看点在经典唱段和音乐表达。',
       '{"category":"推荐标签","dimension":"音乐特征","scoreWeight":1.2}', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM knowledge_node WHERE name = '唱段突出' AND node_type = 'TAG'
);

INSERT INTO knowledge_node (name, node_type, description, extra_data, created_at)
SELECT '舞段突出', 'TAG', '核心看点在舞段技巧、编排和肢体表现。',
       '{"category":"推荐标签","dimension":"舞台特征","scoreWeight":1.2}', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM knowledge_node WHERE name = '舞段突出' AND node_type = 'TAG'
);

INSERT INTO knowledge_node (name, node_type, description, extra_data, created_at)
SELECT '肢体表达强', 'TAG', '作品高度依赖身体动作传递情绪和叙事。',
       '{"category":"推荐标签","dimension":"表演风格","scoreWeight":1.2}', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM knowledge_node WHERE name = '肢体表达强' AND node_type = 'TAG'
);

INSERT INTO knowledge_node (name, node_type, description, extra_data, created_at)
SELECT '新手友好', 'TAG', '没有戏剧基础也容易看懂和代入。',
       '{"category":"推荐标签","dimension":"受众体验","scoreWeight":1.5}', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM knowledge_node WHERE name = '新手友好' AND node_type = 'TAG'
);

INSERT INTO knowledge_node (name, node_type, description, extra_data, created_at)
SELECT '戏剧迷必看', 'TAG', '更适合有一定观剧经验、追求完成度和表达深度的观众。',
       '{"category":"推荐标签","dimension":"受众体验","scoreWeight":1.3}', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM knowledge_node WHERE name = '戏剧迷必看' AND node_type = 'TAG'
);

INSERT INTO knowledge_node (name, node_type, description, extra_data, created_at)
SELECT '情侣适合', 'TAG', '适合双人观演，氛围感和情感共鸣较强。',
       '{"category":"推荐标签","dimension":"受众体验","scoreWeight":1.1}', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM knowledge_node WHERE name = '情侣适合' AND node_type = 'TAG'
);

INSERT INTO knowledge_node (name, node_type, description, extra_data, created_at)
SELECT '适合带长辈', 'TAG', '题材接受度较高、观演门槛较低。',
       '{"category":"推荐标签","dimension":"受众体验","scoreWeight":1.1}', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM knowledge_node WHERE name = '适合带长辈' AND node_type = 'TAG'
);

INSERT INTO knowledge_node (name, node_type, description, extra_data, created_at)
SELECT '适合二刷', 'TAG', '细节丰富，重复观看仍有新的发现。',
       '{"category":"推荐标签","dimension":"受众体验","scoreWeight":1.0}', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM knowledge_node WHERE name = '适合二刷' AND node_type = 'TAG'
);

INSERT INTO knowledge_node (name, node_type, description, extra_data, created_at)
SELECT '烧脑', 'TAG', '剧情理解和人物关系需要更强的跟进。',
       '{"category":"推荐标签","dimension":"体验强度","scoreWeight":1.2}', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM knowledge_node WHERE name = '烧脑' AND node_type = 'TAG'
);

INSERT INTO knowledge_node (name, node_type, description, extra_data, created_at)
SELECT '催泪', 'TAG', '情感触发明显，容易引发共情。',
       '{"category":"推荐标签","dimension":"体验强度","scoreWeight":1.1}', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM knowledge_node WHERE name = '催泪' AND node_type = 'TAG'
);

INSERT INTO knowledge_node (name, node_type, description, extra_data, created_at)
SELECT '高能冲突', 'TAG', '剧情矛盾密集、冲突强烈。',
       '{"category":"推荐标签","dimension":"戏剧冲突","scoreWeight":1.3}', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM knowledge_node WHERE name = '高能冲突' AND node_type = 'TAG'
);

INSERT INTO knowledge_node (name, node_type, description, extra_data, created_at)
SELECT '经典改编', 'TAG', '基于经典文学或知名文本改编。',
       '{"category":"推荐标签","dimension":"内容来源","scoreWeight":1.1}', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM knowledge_node WHERE name = '经典改编' AND node_type = 'TAG'
);

INSERT INTO knowledge_node (name, node_type, description, extra_data, created_at)
SELECT '现实主义', 'TAG', '更强调真实生活质感与社会关系。',
       '{"category":"推荐标签","dimension":"美学风格","scoreWeight":1.1}', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM knowledge_node WHERE name = '现实主义' AND node_type = 'TAG'
);

INSERT INTO knowledge_node (name, node_type, description, extra_data, created_at)
SELECT '300元内可选', 'TAG', '存在 300 元及以下的可购票档位。',
       '{"category":"推荐标签","dimension":"预算条件","scoreWeight":1.4}', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM knowledge_node WHERE name = '300元内可选' AND node_type = 'TAG'
);

INSERT INTO knowledge_node (name, node_type, description, extra_data, created_at)
SELECT '性价比高', 'TAG', '在价格和观演体验之间较均衡。',
       '{"category":"推荐标签","dimension":"预算条件","scoreWeight":1.3}', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM knowledge_node WHERE name = '性价比高' AND node_type = 'TAG'
);

INSERT INTO knowledge_node (name, node_type, description, extra_data, created_at)
SELECT '冷门口碑好', 'TAG', '热度不一定最高，但观众评价稳定。',
       '{"category":"推荐标签","dimension":"热度特征","scoreWeight":1.1}', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM knowledge_node WHERE name = '冷门口碑好' AND node_type = 'TAG'
);

INSERT INTO knowledge_node (name, node_type, description, extra_data, created_at)
SELECT '周末友好', 'TAG', '档期安排适合周末观演。',
       '{"category":"推荐标签","dimension":"档期条件","scoreWeight":1.0}', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM knowledge_node WHERE name = '周末友好' AND node_type = 'TAG'
);

-- 3) 补充术语之间的关联
INSERT INTO knowledge_edge (source_node_id, target_node_id, relation_type, label, weight, created_at)
SELECT s.id, t.id, 'RELATED_TO', '表演表达相关', 1.0, NOW()
FROM knowledge_node s, knowledge_node t
WHERE s.name = '独白' AND s.node_type = 'TERMINOLOGY'
  AND t.name = '旁白' AND t.node_type = 'TERMINOLOGY'
  AND NOT EXISTS (
      SELECT 1 FROM knowledge_edge e
      WHERE e.source_node_id = s.id AND e.target_node_id = t.id AND e.relation_type = 'RELATED_TO'
  );

INSERT INTO knowledge_edge (source_node_id, target_node_id, relation_type, label, weight, created_at)
SELECT s.id, t.id, 'RELATED_TO', '舞台空间相关', 1.0, NOW()
FROM knowledge_node s, knowledge_node t
WHERE s.name = '走位' AND s.node_type = 'TERMINOLOGY'
  AND t.name = '调度' AND t.node_type = 'TERMINOLOGY'
  AND NOT EXISTS (
      SELECT 1 FROM knowledge_edge e
      WHERE e.source_node_id = s.id AND e.target_node_id = t.id AND e.relation_type = 'RELATED_TO'
  );

INSERT INTO knowledge_edge (source_node_id, target_node_id, relation_type, label, weight, created_at)
SELECT s.id, t.id, 'RELATED_TO', '歌剧唱段相关', 1.0, NOW()
FROM knowledge_node s, knowledge_node t
WHERE s.name = '咏叹调' AND s.node_type = 'TERMINOLOGY'
  AND t.name = '宣叙调' AND t.node_type = 'TERMINOLOGY'
  AND NOT EXISTS (
      SELECT 1 FROM knowledge_edge e
      WHERE e.source_node_id = s.id AND e.target_node_id = t.id AND e.relation_type = 'RELATED_TO'
  );

-- 4) 给现有剧目补标签
INSERT INTO knowledge_edge (source_node_id, target_node_id, relation_type, label, weight, created_at)
SELECT p.id, t.id, 'HAS_TAG', NULL, 1.0, NOW()
FROM knowledge_node p
JOIN knowledge_node t ON t.name IN ('经典改编', '演技爆发力强', '情绪张力强', '台词密度高')
WHERE p.node_type = 'PLAY' AND p.name IN ('哈姆雷特', '李尔王', '雷雨')
  AND NOT EXISTS (
      SELECT 1 FROM knowledge_edge e
      WHERE e.source_node_id = p.id AND e.target_node_id = t.id AND e.relation_type = 'HAS_TAG'
  );

INSERT INTO knowledge_edge (source_node_id, target_node_id, relation_type, label, weight, created_at)
SELECT p.id, t.id, 'HAS_TAG', NULL, 1.0, NOW()
FROM knowledge_node p
JOIN knowledge_node t ON t.name IN ('新手友好', '适合带长辈', '性价比高', '300元内可选', '群像戏')
WHERE p.node_type = 'PLAY' AND p.name IN ('茶馆')
  AND NOT EXISTS (
      SELECT 1 FROM knowledge_edge e
      WHERE e.source_node_id = p.id AND e.target_node_id = t.id AND e.relation_type = 'HAS_TAG'
  );

INSERT INTO knowledge_edge (source_node_id, target_node_id, relation_type, label, weight, created_at)
SELECT p.id, t.id, 'HAS_TAG', NULL, 1.0, NOW()
FROM knowledge_node p
JOIN knowledge_node t ON t.name IN ('经典改编', '唱段突出', '情侣适合', '催泪')
WHERE p.node_type = 'PLAY' AND p.name IN ('茶花女', '图兰朵')
  AND NOT EXISTS (
      SELECT 1 FROM knowledge_edge e
      WHERE e.source_node_id = p.id AND e.target_node_id = t.id AND e.relation_type = 'HAS_TAG'
  );

INSERT INTO knowledge_edge (source_node_id, target_node_id, relation_type, label, weight, created_at)
SELECT p.id, t.id, 'HAS_TAG', NULL, 1.0, NOW()
FROM knowledge_node p
JOIN knowledge_node t ON t.name IN ('舞段突出', '肢体表达强', '适合二刷')
WHERE p.node_type = 'PLAY' AND p.name IN ('天鹅湖', '胡桃夹子')
  AND NOT EXISTS (
      SELECT 1 FROM knowledge_edge e
      WHERE e.source_node_id = p.id AND e.target_node_id = t.id AND e.relation_type = 'HAS_TAG'
  );

INSERT INTO knowledge_edge (source_node_id, target_node_id, relation_type, label, weight, created_at)
SELECT p.id, t.id, 'HAS_TAG', NULL, 1.0, NOW()
FROM knowledge_node p
JOIN knowledge_node t ON t.name IN ('情侣适合', '新手友好', '冷门口碑好')
WHERE p.node_type = 'PLAY' AND p.name IN ('恋爱的犀牛')
  AND NOT EXISTS (
      SELECT 1 FROM knowledge_edge e
      WHERE e.source_node_id = p.id AND e.target_node_id = t.id AND e.relation_type = 'HAS_TAG'
  );

-- 5) 给现有剧目补术语
INSERT INTO knowledge_edge (source_node_id, target_node_id, relation_type, label, weight, created_at)
SELECT p.id, x.id, 'HAS_TERMINOLOGY', '经典独白段落', 1.0, NOW()
FROM knowledge_node p, knowledge_node x
WHERE p.name = '哈姆雷特' AND p.node_type = 'PLAY'
  AND x.name = '爆发戏' AND x.node_type = 'TERMINOLOGY'
  AND NOT EXISTS (
      SELECT 1 FROM knowledge_edge e
      WHERE e.source_node_id = p.id AND e.target_node_id = x.id AND e.relation_type = 'HAS_TERMINOLOGY'
  );

INSERT INTO knowledge_edge (source_node_id, target_node_id, relation_type, label, weight, created_at)
SELECT p.id, x.id, 'HAS_TERMINOLOGY', '群像戏调度', 1.0, NOW()
FROM knowledge_node p, knowledge_node x
WHERE p.name = '茶馆' AND p.node_type = 'PLAY'
  AND x.name = '调度' AND x.node_type = 'TERMINOLOGY'
  AND NOT EXISTS (
      SELECT 1 FROM knowledge_edge e
      WHERE e.source_node_id = p.id AND e.target_node_id = x.id AND e.relation_type = 'HAS_TERMINOLOGY'
  );

INSERT INTO knowledge_edge (source_node_id, target_node_id, relation_type, label, weight, created_at)
SELECT p.id, x.id, 'HAS_TERMINOLOGY', '强冲突对手戏', 1.0, NOW()
FROM knowledge_node p, knowledge_node x
WHERE p.name = '雷雨' AND p.node_type = 'PLAY'
  AND x.name = '对手戏' AND x.node_type = 'TERMINOLOGY'
  AND NOT EXISTS (
      SELECT 1 FROM knowledge_edge e
      WHERE e.source_node_id = p.id AND e.target_node_id = x.id AND e.relation_type = 'HAS_TERMINOLOGY'
  );

INSERT INTO knowledge_edge (source_node_id, target_node_id, relation_type, label, weight, created_at)
SELECT p.id, x.id, 'HAS_TERMINOLOGY', '经典歌剧唱段', 1.0, NOW()
FROM knowledge_node p, knowledge_node x
WHERE p.name = '茶花女' AND p.node_type = 'PLAY'
  AND x.name = '宣叙调' AND x.node_type = 'TERMINOLOGY'
  AND NOT EXISTS (
      SELECT 1 FROM knowledge_edge e
      WHERE e.source_node_id = p.id AND e.target_node_id = x.id AND e.relation_type = 'HAS_TERMINOLOGY'
  );

INSERT INTO knowledge_edge (source_node_id, target_node_id, relation_type, label, weight, created_at)
SELECT p.id, x.id, 'HAS_TERMINOLOGY', '高难双人舞段', 1.0, NOW()
FROM knowledge_node p, knowledge_node x
WHERE p.name = '天鹅湖' AND p.node_type = 'PLAY'
  AND x.name = '托举' AND x.node_type = 'TERMINOLOGY'
  AND NOT EXISTS (
      SELECT 1 FROM knowledge_edge e
      WHERE e.source_node_id = p.id AND e.target_node_id = x.id AND e.relation_type = 'HAS_TERMINOLOGY'
  );

SELECT COUNT(*) AS total_nodes FROM knowledge_node;
SELECT COUNT(*) AS total_edges FROM knowledge_edge;
