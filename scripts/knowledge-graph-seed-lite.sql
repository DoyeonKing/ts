USE theater_db;

DELETE FROM knowledge_edge;
DELETE FROM knowledge_node;

ALTER TABLE knowledge_node AUTO_INCREMENT = 1;
ALTER TABLE knowledge_edge AUTO_INCREMENT = 1;

INSERT INTO knowledge_node (name, node_type, description, extra_data, created_at) VALUES
('哈姆雷特', 'PLAY', '莎士比亚四大悲剧之一，丹麦王子复仇与人性抉择', '{"genre":"悲剧","rating":9.5}', NOW()),
('李尔王', 'PLAY', '莎士比亚四大悲剧之一，权力、亲情与背叛', '{"genre":"悲剧","rating":9.1}', NOW()),
('茶馆', 'PLAY', '老舍经典话剧，三幕写尽时代变迁', '{"genre":"话剧","rating":9.6}', NOW()),
('罗密欧与朱丽叶', 'PLAY', '莎士比亚经典爱情悲剧', '{"genre":"爱情悲剧","rating":9.3}', NOW()),
('濮存昕', 'ACTOR', '著名话剧演员，北京人艺', '{"specialty":"话剧、莎剧"}', NOW()),
('何冰', 'ACTOR', '北京人艺代表演员', '{"specialty":"话剧、京味戏"}', NOW()),
('袁泉', 'ACTOR', '兼具舞台与影视经验', '{"specialty":"话剧、音乐剧"}', NOW()),
('莎士比亚', 'TAG', '英国文艺复兴时期剧作家', NULL, NOW()),
('悲剧', 'TAG', '以不幸结局引发观众悲悯', NULL, NOW()),
('经典', 'TAG', '经久不衰的经典作品', NULL, NOW()),
('话剧', 'TAG', '以对白为主要表现方式', NULL, NOW()),
('爱情', 'TAG', '以爱情为主题的作品', NULL, NOW()),
('独白', 'TERMINOLOGY', '角色独自说出内心想法', NULL, NOW()),
('卡司', 'TERMINOLOGY', '一场演出的演员阵容', NULL, NOW()),
('谢幕', 'TERMINOLOGY', '演出结束后演员向观众致意', NULL, NOW()),
('北京人民艺术剧院', 'VENUE', '中国话剧艺术重镇', '{"city":"北京"}', NOW()),
('国家大剧院', 'VENUE', '国家级表演艺术中心', '{"city":"北京"}', NOW());

INSERT INTO knowledge_edge (source_node_id, target_node_id, relation_type, label, weight, created_at) VALUES
(5, 1, 'PERFORMS_IN', '饰 哈姆雷特', 1.0, NOW()),
(5, 2, 'PERFORMS_IN', '饰 李尔王', 1.0, NOW()),
(6, 3, 'PERFORMS_IN', '饰 重要角色', 1.0, NOW()),
(7, 4, 'PERFORMS_IN', '饰 朱丽叶', 1.0, NOW()),
(1, 8, 'HAS_TAG', NULL, 1.0, NOW()),
(1, 9, 'HAS_TAG', NULL, 1.0, NOW()),
(1, 10, 'HAS_TAG', NULL, 1.0, NOW()),
(2, 8, 'HAS_TAG', NULL, 1.0, NOW()),
(2, 9, 'HAS_TAG', NULL, 1.0, NOW()),
(2, 10, 'HAS_TAG', NULL, 1.0, NOW()),
(3, 10, 'HAS_TAG', NULL, 1.0, NOW()),
(3, 11, 'HAS_TAG', NULL, 1.0, NOW()),
(4, 8, 'HAS_TAG', NULL, 1.0, NOW()),
(4, 9, 'HAS_TAG', NULL, 1.0, NOW()),
(4, 12, 'HAS_TAG', NULL, 1.0, NOW()),
(1, 13, 'HAS_TERMINOLOGY', '生存还是毁灭', 1.0, NOW()),
(1, 14, 'HAS_TERMINOLOGY', NULL, 1.0, NOW()),
(3, 14, 'HAS_TERMINOLOGY', NULL, 1.0, NOW()),
(3, 15, 'HAS_TERMINOLOGY', NULL, 1.0, NOW()),
(3, 16, 'PERFORMED_AT', '驻场演出', 1.0, NOW()),
(1, 17, 'PERFORMED_AT', '演出季常见剧目', 1.0, NOW()),
(4, 17, 'PERFORMED_AT', NULL, 1.0, NOW()),
(1, 2, 'SIMILAR_TO', '同为莎翁悲剧', 1.0, NOW()),
(1, 4, 'SIMILAR_TO', '同为莎翁作品', 1.0, NOW()),
(13, 15, 'RELATED_TO', '均与表演呈现有关', 1.0, NOW());

SELECT 'lite graph seed imported' AS message;
SELECT COUNT(*) AS node_count FROM knowledge_node;
SELECT COUNT(*) AS edge_count FROM knowledge_edge;
