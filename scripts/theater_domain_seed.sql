-- 剧场垂直领域补充种子数据（UTF-8）
-- 用途：
-- 1. 给 terminology / tag 页面补足“行话”和推荐标签
-- 2. 让知识图谱和推荐系统有更多垂直语义节点可用
-- 3. 不依赖大规模训练，先做高质量小样本知识库

USE theater_db;
SET NAMES utf8mb4;

INSERT INTO knowledge_node (name, node_type, description, extra_data, created_at) VALUES
('旁白','TERMINOLOGY','角色直接对观众说话，舞台上其他人物通常“听不见”。','{"category":"表演术语","aliases":["aside"],"usageScene":"剧评/剧情分析","noviceTip":"常用于区分角色对观众说话和正常对白"}',NOW()),
('走位','TERMINOLOGY','演员在舞台空间中的位置变化与移动路径。','{"category":"舞台术语","aliases":["位移"],"usageScene":"排练/剧评","noviceTip":"评价舞台调度时经常会提到"}',NOW()),
('调度','TERMINOLOGY','导演对演员、空间、节奏和场面关系的整体安排。','{"category":"舞台术语","aliases":["舞台调度"],"usageScene":"剧评/导演分析","noviceTip":"常用来评价场面是否流畅有层次"}',NOW()),
('对手戏','TERMINOLOGY','两个或多个角色之间强互动、强冲突的表演段落。','{"category":"表演术语","aliases":["搭戏"],"usageScene":"演员评价","noviceTip":"常用于评价演员之间是否接得住戏"}',NOW()),
('爆发戏','TERMINOLOGY','角色情绪集中释放、戏剧冲突急剧拉高的关键段落。','{"category":"表演术语","aliases":["情绪爆发段"],"usageScene":"剧评/演员分析","noviceTip":"用户说“演技炸裂”时常对应这类段落"}',NOW()),
('收放','TERMINOLOGY','演员控制情绪和表演力度的能力，既能外放也能克制。','{"category":"表演术语","aliases":["收与放"],"usageScene":"演员评价","noviceTip":"常用于判断演员是否有层次"}',NOW()),
('台词节奏','TERMINOLOGY','对白推进的速度、停顿和呼吸组织方式。','{"category":"表演术语","aliases":["节奏处理"],"usageScene":"剧评/表演分析","noviceTip":"节奏稳通常代表表演成熟度较高"}',NOW()),
('舞美','TERMINOLOGY','舞台视觉整体设计，包括布景、道具、空间风格等。','{"category":"舞台术语","aliases":["舞台美术"],"usageScene":"观后感/剧评","noviceTip":"用户评价“画面感强”时常在说舞美"}',NOW()),
('转台','TERMINOLOGY','通过旋转舞台完成场景切换的一种舞台机械手段。','{"category":"舞台术语","aliases":["旋转舞台"],"usageScene":"舞台技术/观后感","noviceTip":"常出现在大型商业制作和音乐剧中"}',NOW()),
('升降台','TERMINOLOGY','舞台可上下运动的装置，用于人物或场景的动态呈现。','{"category":"舞台术语","aliases":["升降装置"],"usageScene":"舞台技术","noviceTip":"常用于强化视觉奇观感"}',NOW()),
('景片','TERMINOLOGY','舞台布景中用于构成环境视觉的平面或立体部件。','{"category":"舞台术语","aliases":["布景片"],"usageScene":"舞美设计","noviceTip":"描述舞台还原度时会用到"}',NOW()),
('灯光切点','TERMINOLOGY','灯光变化与情绪或剧情节点精确对应的时刻。','{"category":"舞台术语","aliases":["切光点"],"usageScene":"舞台技术/剧评","noviceTip":"灯光卡得准会显得演出非常完整"}',NOW()),
('咏叹调','TERMINOLOGY','歌剧中突出人物情感表达的独唱段落。','{"category":"歌剧术语","aliases":["aria"],"usageScene":"歌剧赏析","noviceTip":"相当于歌剧里最容易出“名场面”的唱段"}',NOW()),
('宣叙调','TERMINOLOGY','以叙述推进剧情为主的歌唱段落。','{"category":"歌剧术语","aliases":["recitative"],"usageScene":"歌剧赏析","noviceTip":"比起抒情，更偏剧情推进"}',NOW()),
('重唱','TERMINOLOGY','两位或以上角色共同演唱的唱段。','{"category":"音乐术语","aliases":["合唱性唱段"],"usageScene":"音乐剧/歌剧分析","noviceTip":"多人情绪关系常在这里集中爆发"}',NOW()),
('动机旋律','TERMINOLOGY','反复出现、代表人物或情绪的核心旋律材料。','{"category":"音乐术语","aliases":["主题动机"],"usageScene":"音乐剧/歌剧分析","noviceTip":"经常用来判断作品的音乐记忆点"}',NOW()),
('足尖','TERMINOLOGY','芭蕾演员以足尖支撑完成动作的技巧。','{"category":"芭蕾术语","aliases":["足尖技术"],"usageScene":"芭蕾赏析","noviceTip":"是古典芭蕾的重要辨识点"}',NOW()),
('托举','TERMINOLOGY','双人舞中由一方支撑另一方完成腾空或定格动作。','{"category":"芭蕾术语","aliases":["lift"],"usageScene":"芭蕾/舞段分析","noviceTip":"常用于评价舞段难度和默契度"}',NOW()),
('变奏','TERMINOLOGY','在主题基础上变化发展的独立舞段或音乐段落。','{"category":"芭蕾术语","aliases":["variation"],"usageScene":"芭蕾赏析","noviceTip":"常是展示个人技巧的重点段落"}',NOW()),
('肢体线条','TERMINOLOGY','演员或舞者在动作延展中呈现出的形体美感。','{"category":"肢体术语","aliases":["线条感"],"usageScene":"芭蕾/肢体剧评价","noviceTip":"常用于评价舞蹈完成度和审美表现"}',NOW()),
('二刷','TERMINOLOGY','同一部戏第二次观看。','{"category":"观演黑话","aliases":["二次观看"],"usageScene":"观众交流","noviceTip":"通常意味着作品值得重复观看"}',NOW()),
('官摄','TERMINOLOGY','官方拍摄并发布的演出影像资料。','{"category":"观演黑话","aliases":["官方录像"],"usageScene":"粉圈/观众交流","noviceTip":"和偷拍、路透不同，通常更正式"}',NOW()),
('返图','TERMINOLOGY','观演后分享的照片或二次整理图。','{"category":"观演黑话","aliases":["返场图"],"usageScene":"社交平台交流","noviceTip":"常见于粉丝或观众社群"}',NOW()),
('神卡司','TERMINOLOGY','观众对特别满意或梦幻演员阵容的口语化称呼。','{"category":"观演黑话","aliases":["梦幻卡司"],"usageScene":"观众交流","noviceTip":"属于主观评价，适合展示社区氛围"}',NOW()),
('首排','TERMINOLOGY','剧场观众区最前排座位。','{"category":"购票黑话","aliases":["前排"],"usageScene":"购票讨论","noviceTip":"通常视野近、价格也更高"}',NOW()),
('视角遮挡','TERMINOLOGY','由于座位位置、舞美或设备导致部分舞台看不全。','{"category":"购票术语","aliases":["遮挡位"],"usageScene":"选座/购票","noviceTip":"推荐系统可据此做避坑提示"}',NOW()),
('性价比位','TERMINOLOGY','在价格和观演效果之间比较均衡的座位区域。','{"category":"购票术语","aliases":["高性价比座位"],"usageScene":"购票建议","noviceTip":"特别适合预算型推荐"}',NOW());

INSERT INTO knowledge_node (name, node_type, description, extra_data, created_at) VALUES
('演技爆发力强','TAG','适合情绪冲突大、关键场面爆发明显的作品。','{"category":"推荐标签","dimension":"表演风格","scoreWeight":1.4}',NOW()),
('情绪张力强','TAG','整体冲突感和压迫感较强，适合追求戏剧张力的观众。','{"category":"推荐标签","dimension":"表演风格","scoreWeight":1.3}',NOW()),
('台词密度高','TAG','对白信息量大、语言表达强度高。','{"category":"推荐标签","dimension":"表演风格","scoreWeight":1.2}',NOW()),
('群像戏','TAG','多角色共同推进剧情，人物关系网复杂。','{"category":"推荐标签","dimension":"结构特征","scoreWeight":1.1}',NOW()),
('独角戏气质','TAG','高度依赖单一角色或核心角色的舞台掌控力。','{"category":"推荐标签","dimension":"结构特征","scoreWeight":1.1}',NOW()),
('唱段突出','TAG','核心看点在经典唱段和音乐表达。','{"category":"推荐标签","dimension":"音乐特征","scoreWeight":1.2}',NOW()),
('舞段突出','TAG','核心看点在舞蹈编排、技巧和肢体表现。','{"category":"推荐标签","dimension":"舞台特征","scoreWeight":1.2}',NOW()),
('肢体表达强','TAG','作品高度依赖身体动作传递情绪与叙事。','{"category":"推荐标签","dimension":"表演风格","scoreWeight":1.2}',NOW()),
('新手友好','TAG','即使没有戏剧基础也容易看懂、代入和接受。','{"category":"推荐标签","dimension":"受众体验","scoreWeight":1.5}',NOW()),
('戏剧迷必看','TAG','更适合有一定剧场经验、追求完成度和表达深度的观众。','{"category":"推荐标签","dimension":"受众体验","scoreWeight":1.3}',NOW()),
('情侣适合','TAG','适合双人观演，情绪共鸣或氛围体验较强。','{"category":"推荐标签","dimension":"受众体验","scoreWeight":1.1}',NOW()),
('适合带长辈','TAG','题材接受度较高、观演门槛低。','{"category":"推荐标签","dimension":"受众体验","scoreWeight":1.1}',NOW()),
('适合二刷','TAG','细节丰富，重复观看仍有新发现。','{"category":"推荐标签","dimension":"受众体验","scoreWeight":1.0}',NOW()),
('烧脑','TAG','剧情理解和人物关系需要较强跟进。','{"category":"推荐标签","dimension":"体验强度","scoreWeight":1.2}',NOW()),
('轻松解压','TAG','情绪负担低，适合休闲观演。','{"category":"推荐标签","dimension":"体验强度","scoreWeight":1.0}',NOW()),
('催泪','TAG','情感触发明显，容易引发共情。','{"category":"推荐标签","dimension":"体验强度","scoreWeight":1.1}',NOW()),
('高能冲突','TAG','剧情矛盾密集、冲突强烈。','{"category":"推荐标签","dimension":"戏剧冲突","scoreWeight":1.3}',NOW()),
('经典改编','TAG','基于经典文学、历史文本或知名 IP 改编。','{"category":"推荐标签","dimension":"内容来源","scoreWeight":1.1}',NOW()),
('现实主义','TAG','更强调真实生活质感与社会关系。','{"category":"推荐标签","dimension":"美学风格","scoreWeight":1.1}',NOW()),
('300元内可选','TAG','存在 300 元及以下可购票档位。','{"category":"推荐标签","dimension":"预算条件","scoreWeight":1.4}',NOW()),
('性价比高','TAG','在价格与观演体验之间较均衡。','{"category":"推荐标签","dimension":"预算条件","scoreWeight":1.3}',NOW()),
('冷门口碑好','TAG','热度不一定最高，但观众评价较稳定。','{"category":"推荐标签","dimension":"热度特征","scoreWeight":1.1}',NOW()),
('周末友好','TAG','档期安排适合周末观演。','{"category":"推荐标签","dimension":"档期条件","scoreWeight":1.0}',NOW()),
('交通便利','TAG','剧场位置和到达便利性较好。','{"category":"推荐标签","dimension":"场馆条件","scoreWeight":1.0}',NOW());

-- 可选：给已有经典剧目补一批推荐标签关系
INSERT INTO knowledge_edge (source_node_id, target_node_id, relation_type, label, weight, created_at)
SELECT p.id, t.id, 'HAS_TAG', NULL, 1.0, NOW()
FROM knowledge_node p
JOIN knowledge_node t ON t.name IN ('经典改编','情绪张力强','演技爆发力强')
WHERE p.node_type = 'PLAY' AND p.name IN ('哈姆雷特','李尔王','雷雨');

INSERT INTO knowledge_edge (source_node_id, target_node_id, relation_type, label, weight, created_at)
SELECT p.id, t.id, 'HAS_TAG', NULL, 1.0, NOW()
FROM knowledge_node p
JOIN knowledge_node t ON t.name IN ('新手友好','性价比高','300元内可选')
WHERE p.node_type = 'PLAY' AND p.name IN ('茶馆','恋爱的犀牛');

INSERT INTO knowledge_edge (source_node_id, target_node_id, relation_type, label, weight, created_at)
SELECT p.id, t.id, 'HAS_TAG', NULL, 1.0, NOW()
FROM knowledge_node p
JOIN knowledge_node t ON t.name IN ('唱段突出','情侣适合','经典改编')
WHERE p.node_type = 'PLAY' AND p.name IN ('茶花女','图兰朵');

INSERT INTO knowledge_edge (source_node_id, target_node_id, relation_type, label, weight, created_at)
SELECT p.id, t.id, 'HAS_TAG', NULL, 1.0, NOW()
FROM knowledge_node p
JOIN knowledge_node t ON t.name IN ('舞段突出','肢体表达强','适合二刷')
WHERE p.node_type = 'PLAY' AND p.name IN ('天鹅湖','胡桃夹子');
