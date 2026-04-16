-- ============================================================
-- 知识图谱种子数据
-- 用途：数据库准备好后直接执行，导入剧目、演员、术语、标签、场馆及关系
-- 说明：若使用 SpringBoot + JPA，表会由 Hibernate 自动创建，本脚本仅负责 INSERT 数据
--       若表不存在，请先执行 knowledge-graph-schema.sql 或启动 SpringBoot
-- ============================================================

USE theater_db;

-- 清空旧数据（可选，重复执行时先清空）
DELETE FROM knowledge_edge;
DELETE FROM knowledge_node;

-- 重置自增 ID（可选，使 ID 从 1 开始）
ALTER TABLE knowledge_node AUTO_INCREMENT = 1;
ALTER TABLE knowledge_edge AUTO_INCREMENT = 1;

-- ========== 节点 (28 个) ==========
-- 剧目 1-6
INSERT INTO knowledge_node (name, node_type, description, extra_data, created_at) VALUES
('哈姆雷特', 'PLAY', '莎士比亚四大悲剧之一，丹麦王子复仇与人性抉择', '{"genre":"悲剧","rating":9.5}', NOW()),
('罗密欧与朱丽叶', 'PLAY', '莎士比亚经典爱情故事，两大家族下的禁忌之恋', '{"genre":"爱情悲剧","rating":9.3}', NOW()),
('天鹅湖', 'PLAY', '柴可夫斯基芭蕾舞剧，王子与天鹅公主的童话', '{"genre":"芭蕾","rating":9.0}', NOW()),
('茶花女', 'PLAY', '威尔第歌剧，巴黎名妓与青年贵族的爱情悲剧', '{"genre":"歌剧","rating":9.4}', NOW()),
('李尔王', 'PLAY', '莎士比亚四大悲剧，权力、亲情与背叛', '{"genre":"悲剧","rating":9.1}', NOW()),
('茶馆', 'PLAY', '老舍经典话剧，三幕写尽半个世纪的沧桑变迁', '{"genre":"话剧","rating":9.6}', NOW());

-- 演员 7-9
INSERT INTO knowledge_node (name, node_type, description, extra_data, created_at) VALUES
('濮存昕', 'ACTOR', '著名话剧演员，北京人艺', '{"awards":["梅花奖","文华奖"],"specialty":"话剧、莎士比亚"}', NOW()),
('袁泉', 'ACTOR', '演员，话剧与影视', '{"awards":["梅花奖","金狮奖"],"specialty":"话剧、音乐剧"}', NOW()),
('何冰', 'ACTOR', '北京人艺演员', '{"awards":["梅花奖"],"specialty":"话剧、喜剧"}', NOW());

-- 标签 10-16
INSERT INTO knowledge_node (name, node_type, description, extra_data, created_at) VALUES
('莎士比亚', 'TAG', '英国文艺复兴时期剧作家', NULL, NOW()),
('悲剧', 'TAG', '以主人公的不幸结局引发观众的悲悯', NULL, NOW()),
('爱情', 'TAG', '以爱情为主题的剧目', NULL, NOW()),
('经典', 'TAG', '经久不衰的经典剧目', NULL, NOW()),
('芭蕾', 'TAG', '以芭蕾舞蹈为表现形式', NULL, NOW()),
('歌剧', 'TAG', '以歌唱和音乐为主要表现手段', NULL, NOW()),
('话剧', 'TAG', '以对话为主要表现手段的戏剧', NULL, NOW());

-- 术语 17-26
INSERT INTO knowledge_node (name, node_type, description, extra_data, created_at) VALUES
('独白', 'TERMINOLOGY', '角色在舞台上独自说出内心想法，观众可闻但剧中其他角色不知', NULL, NOW()),
('旁白', 'TERMINOLOGY', '角色对观众说的话，不被舞台上其他角色听到', NULL, NOW()),
('台步', 'TERMINOLOGY', '演员在舞台上的行走方式与步法', NULL, NOW()),
('谢幕', 'TERMINOLOGY', '演出结束后演员向观众致谢的仪式', NULL, NOW()),
('幕间', 'TERMINOLOGY', '演出中两幕之间的休息时间', NULL, NOW()),
('SD', 'TERMINOLOGY', 'Special Day，指特别场次（如首场、末场、生日场等）', NULL, NOW()),
('返场', 'TERMINOLOGY', '演出结束后应观众掌声再次登台表演的环节，常见于音乐会、歌剧等', NULL, NOW()),
('卡司', 'TERMINOLOGY', 'Cast 音译，指一场演出的演员阵容、角色分配', NULL, NOW()),
('末场', 'TERMINOLOGY', '一部戏在某地或某一轮演出的最后一场', NULL, NOW()),
('首演', 'TERMINOLOGY', '一部戏第一次公开演出的场次', NULL, NOW());

-- 场馆 27-28
INSERT INTO knowledge_node (name, node_type, description, extra_data, created_at) VALUES
('国家大剧院', 'VENUE', '位于北京天安门广场西侧，国家级表演艺术中心', '{"city":"北京","capacity":5452}', NOW()),
('北京人艺', 'VENUE', '北京人民艺术剧院，中国话剧艺术殿堂', '{"city":"北京","capacity":1200}', NOW());

-- ========== 边 (节点 ID: 剧目1-6, 演员7-9, 标签10-16, 术语17-26, 场馆27-28) ==========

-- 演员-剧目
INSERT INTO knowledge_edge (source_node_id, target_node_id, relation_type, label, weight, created_at) VALUES
(7, 1, 'PERFORMS_IN', '饰 哈姆雷特', 1.0, NOW()),
(7, 5, 'PERFORMS_IN', '饰 李尔王', 1.0, NOW()),
(7, 6, 'PERFORMS_IN', '饰 常四爷', 1.0, NOW()),
(8, 1, 'PERFORMS_IN', '饰 奥菲利亚', 1.0, NOW()),
(9, 5, 'PERFORMS_IN', '饰 葛罗斯特', 1.0, NOW()),
(9, 6, 'PERFORMS_IN', '饰 松二爷', 1.0, NOW());

-- 剧目-标签
INSERT INTO knowledge_edge (source_node_id, target_node_id, relation_type, label, weight, created_at) VALUES
(1, 10, 'HAS_TAG', NULL, 1.0, NOW()),
(1, 11, 'HAS_TAG', NULL, 1.0, NOW()),
(1, 13, 'HAS_TAG', NULL, 1.0, NOW()),
(2, 10, 'HAS_TAG', NULL, 1.0, NOW()),
(2, 12, 'HAS_TAG', NULL, 1.0, NOW()),
(2, 11, 'HAS_TAG', NULL, 1.0, NOW()),
(3, 14, 'HAS_TAG', NULL, 1.0, NOW()),
(3, 13, 'HAS_TAG', NULL, 1.0, NOW()),
(3, 12, 'HAS_TAG', NULL, 1.0, NOW()),
(4, 15, 'HAS_TAG', NULL, 1.0, NOW()),
(4, 12, 'HAS_TAG', NULL, 1.0, NOW()),
(5, 10, 'HAS_TAG', NULL, 1.0, NOW()),
(5, 11, 'HAS_TAG', NULL, 1.0, NOW()),
(6, 16, 'HAS_TAG', NULL, 1.0, NOW()),
(6, 13, 'HAS_TAG', NULL, 1.0, NOW());

-- 剧目-术语
INSERT INTO knowledge_edge (source_node_id, target_node_id, relation_type, label, weight, created_at) VALUES
(1, 17, 'HAS_TERMINOLOGY', '「生存还是毁灭」经典独白', 1.0, NOW()),
(1, 18, 'HAS_TERMINOLOGY', NULL, 1.0, NOW()),
(5, 17, 'HAS_TERMINOLOGY', NULL, 1.0, NOW()),
(6, 21, 'HAS_TERMINOLOGY', '三幕话剧', 1.0, NOW()),
(4, 21, 'HAS_TERMINOLOGY', NULL, 1.0, NOW()),
(4, 23, 'HAS_TERMINOLOGY', '歌剧常见返场', 1.0, NOW()),
(3, 23, 'HAS_TERMINOLOGY', NULL, 1.0, NOW()),
(1, 24, 'HAS_TERMINOLOGY', NULL, 1.0, NOW()),
(6, 24, 'HAS_TERMINOLOGY', NULL, 1.0, NOW()),
(1, 26, 'HAS_TERMINOLOGY', NULL, 1.0, NOW()),
(5, 25, 'HAS_TERMINOLOGY', NULL, 1.0, NOW());

-- 剧目-场馆
INSERT INTO knowledge_edge (source_node_id, target_node_id, relation_type, label, weight, created_at) VALUES
(1, 27, 'PERFORMED_AT', '2024年演出季', 1.0, NOW()),
(6, 28, 'PERFORMED_AT', '驻场演出', 1.0, NOW()),
(5, 28, 'PERFORMED_AT', NULL, 1.0, NOW()),
(3, 27, 'PERFORMED_AT', NULL, 1.0, NOW());

-- 剧目-剧目（相似）
INSERT INTO knowledge_edge (source_node_id, target_node_id, relation_type, label, weight, created_at) VALUES
(1, 5, 'SIMILAR_TO', '同为莎翁四大悲剧', 1.0, NOW()),
(1, 2, 'SIMILAR_TO', '同为莎翁作品', 1.0, NOW()),
(2, 5, 'SIMILAR_TO', '同为莎翁作品', 1.0, NOW());

-- 演员-场馆
INSERT INTO knowledge_edge (source_node_id, target_node_id, relation_type, label, weight, created_at) VALUES
(7, 27, 'WORKS_AT', NULL, 1.0, NOW()),
(7, 28, 'WORKS_AT', NULL, 1.0, NOW()),
(9, 28, 'WORKS_AT', NULL, 1.0, NOW());

-- 术语-术语（相关）
INSERT INTO knowledge_edge (source_node_id, target_node_id, relation_type, label, weight, created_at) VALUES
(19, 20, 'RELATED_TO', '舞台表演基础', 1.0, NOW()),
(17, 18, 'RELATED_TO', '均为角色独立发声', 1.0, NOW()),
(22, 20, 'RELATED_TO', '特殊场次谢幕', 1.0, NOW());

-- 完成
SELECT '知识图谱种子数据导入完成' AS message;
SELECT COUNT(*) AS node_count FROM knowledge_node;
SELECT COUNT(*) AS edge_count FROM knowledge_edge;
