USE theater_db;
SET NAMES utf8mb4;

DELETE FROM knowledge_edge;
DELETE FROM knowledge_node;
ALTER TABLE knowledge_node AUTO_INCREMENT = 1;
ALTER TABLE knowledge_edge AUTO_INCREMENT = 1;

INSERT INTO knowledge_node (name,node_type,description,extra_data,created_at) VALUES
('哈姆雷特','PLAY','莎士比亚四大悲剧之一。','{"genre":"悲剧","rating":9.5}',NOW()),
('李尔王','PLAY','权力、亲情与背叛。','{"genre":"悲剧","rating":9.1}',NOW()),
('麦克白','PLAY','野心与预言的悲剧。','{"genre":"悲剧","rating":9.2}',NOW()),
('奥赛罗','PLAY','嫉妒引发的毁灭。','{"genre":"悲剧","rating":9.0}',NOW()),
('罗密欧与朱丽叶','PLAY','经典爱情悲剧。','{"genre":"爱情悲剧","rating":9.3}',NOW()),
('仲夏夜之梦','PLAY','奇幻浪漫喜剧。','{"genre":"喜剧","rating":8.8}',NOW()),
('茶馆','PLAY','老舍三幕话剧代表作。','{"genre":"话剧","rating":9.6}',NOW()),
('雷雨','PLAY','曹禺现实主义话剧经典。','{"genre":"话剧","rating":9.4}',NOW()),
('天下第一楼','PLAY','老字号兴衰与群像人生。','{"genre":"京味话剧","rating":9.2}',NOW()),
('天鹅湖','PLAY','最具代表性的古典芭蕾。','{"genre":"芭蕾","rating":9.0}',NOW()),
('茶花女','PLAY','威尔第经典歌剧。','{"genre":"歌剧","rating":9.4}',NOW()),
('歌剧魅影','PLAY','全球知名音乐剧。','{"genre":"音乐剧","rating":9.5}',NOW()),
('悲惨世界','PLAY','改编自雨果名著的音乐剧。','{"genre":"音乐剧","rating":9.6}',NOW()),
('濮存昕','ACTOR','著名话剧演员。','{"specialty":"话剧、莎剧"}',NOW()),
('何冰','ACTOR','北京人艺代表演员。','{"specialty":"话剧、京味戏"}',NOW()),
('袁泉','ACTOR','兼具舞台与影视经验。','{"specialty":"话剧、音乐剧"}',NOW()),
('冯远征','ACTOR','擅长复杂人物塑造。','{"specialty":"现实主义话剧"}',NOW()),
('廖昌永','ACTOR','著名男中音歌唱家。','{"specialty":"歌剧"}',NOW()),
('和慧','ACTOR','国际活跃女高音。','{"specialty":"歌剧"}',NOW()),
('莎士比亚','TAG','莎剧相关标签。',NULL,NOW()),
('曹禺','TAG','曹禺作品标签。',NULL,NOW()),
('老舍','TAG','老舍作品标签。',NULL,NOW()),
('悲剧','TAG','悲剧类型标签。',NULL,NOW()),
('喜剧','TAG','喜剧类型标签。',NULL,NOW()),
('爱情','TAG','爱情主题标签。',NULL,NOW()),
('经典','TAG','经典常演作品标签。',NULL,NOW()),
('京味','TAG','北京地域文化气质标签。',NULL,NOW()),
('芭蕾','TAG','芭蕾类标签。',NULL,NOW()),
('歌剧','TAG','歌剧类标签。',NULL,NOW()),
('音乐剧','TAG','音乐剧类标签。',NULL,NOW()),
('独白','TERMINOLOGY','角色独自吐露内心。',NULL,NOW()),
('旁白','TERMINOLOGY','直接对观众说的话。',NULL,NOW()),
('台步','TERMINOLOGY','舞台上的步法节奏。',NULL,NOW()),
('谢幕','TERMINOLOGY','演出结束致意。',NULL,NOW()),
('幕间','TERMINOLOGY','两幕之间休息。',NULL,NOW()),
('返场','TERMINOLOGY','谢幕后再次登台。',NULL,NOW()),
('卡司','TERMINOLOGY','演出阵容。',NULL,NOW()),
('首演','TERMINOLOGY','首次公开演出。',NULL,NOW()),
('末场','TERMINOLOGY','一轮演出的最后一场。',NULL,NOW()),
('走位','TERMINOLOGY','演员舞台空间移动。',NULL,NOW()),
('调度','TERMINOLOGY','导演对舞台整体安排。',NULL,NOW()),
('咏叹调','TERMINOLOGY','歌剧重要抒情唱段。',NULL,NOW()),
('宣叙调','TERMINOLOGY','推进剧情的叙述唱段。',NULL,NOW()),
('足尖','TERMINOLOGY','芭蕾高难技巧。',NULL,NOW()),
('双人舞','TERMINOLOGY','重要双人舞段。',NULL,NOW()),
('国家大剧院','VENUE','国家级表演艺术中心。','{"city":"北京"}',NOW()),
('北京人民艺术剧院','VENUE','中国话剧重镇。','{"city":"北京"}',NOW()),
('上海大剧院','VENUE','华东重要演艺场馆。','{"city":"上海"}',NOW()),
('中央歌剧院','VENUE','重要歌剧制作与演出机构。','{"city":"北京"}',NOW()),
('天桥艺术中心','VENUE','音乐剧演出热点场馆。','{"city":"北京"}',NOW());

INSERT INTO knowledge_edge (source_node_id,target_node_id,relation_type,label,weight,created_at) VALUES
(14,1,'PERFORMS_IN','饰 哈姆雷特',1,NOW()),(14,2,'PERFORMS_IN','饰 李尔王',1,NOW()),
(15,7,'PERFORMS_IN','饰 松二爷',1,NOW()),(15,9,'PERFORMS_IN','饰 卢孟实',1,NOW()),
(16,1,'PERFORMS_IN','饰 奥菲利亚',1,NOW()),(17,8,'PERFORMS_IN','饰 周朴园',1,NOW()),
(18,11,'PERFORMS_IN','主演',1,NOW()),(19,11,'PERFORMS_IN','主演',1,NOW()),
(1,20,'HAS_TAG',NULL,1,NOW()),(1,23,'HAS_TAG',NULL,1,NOW()),(1,26,'HAS_TAG',NULL,1,NOW()),
(2,20,'HAS_TAG',NULL,1,NOW()),(2,23,'HAS_TAG',NULL,1,NOW()),(3,20,'HAS_TAG',NULL,1,NOW()),(3,23,'HAS_TAG',NULL,1,NOW()),
(4,20,'HAS_TAG',NULL,1,NOW()),(4,23,'HAS_TAG',NULL,1,NOW()),(5,20,'HAS_TAG',NULL,1,NOW()),(5,23,'HAS_TAG',NULL,1,NOW()),(5,25,'HAS_TAG',NULL,1,NOW()),
(6,20,'HAS_TAG',NULL,1,NOW()),(6,24,'HAS_TAG',NULL,1,NOW()),(7,22,'HAS_TAG',NULL,1,NOW()),(7,26,'HAS_TAG',NULL,1,NOW()),(7,27,'HAS_TAG',NULL,1,NOW()),
(8,21,'HAS_TAG',NULL,1,NOW()),(8,23,'HAS_TAG',NULL,1,NOW()),(9,27,'HAS_TAG',NULL,1,NOW()),(9,26,'HAS_TAG',NULL,1,NOW()),
(10,28,'HAS_TAG',NULL,1,NOW()),(10,26,'HAS_TAG',NULL,1,NOW()),(11,29,'HAS_TAG',NULL,1,NOW()),(11,25,'HAS_TAG',NULL,1,NOW()),
(12,30,'HAS_TAG',NULL,1,NOW()),(12,25,'HAS_TAG',NULL,1,NOW()),(13,30,'HAS_TAG',NULL,1,NOW()),(13,26,'HAS_TAG',NULL,1,NOW()),
(1,31,'HAS_TERMINOLOGY','生存还是毁灭',1,NOW()),(1,32,'HAS_TERMINOLOGY',NULL,1,NOW()),(1,37,'HAS_TERMINOLOGY',NULL,1,NOW()),(1,38,'HAS_TERMINOLOGY',NULL,1,NOW()),
(2,31,'HAS_TERMINOLOGY',NULL,1,NOW()),(2,39,'HAS_TERMINOLOGY',NULL,1,NOW()),(7,35,'HAS_TERMINOLOGY','三幕结构',1,NOW()),(7,37,'HAS_TERMINOLOGY',NULL,1,NOW()),
(8,40,'HAS_TERMINOLOGY',NULL,1,NOW()),(8,41,'HAS_TERMINOLOGY',NULL,1,NOW()),(10,44,'HAS_TERMINOLOGY',NULL,1,NOW()),(10,45,'HAS_TERMINOLOGY',NULL,1,NOW()),
(11,42,'HAS_TERMINOLOGY',NULL,1,NOW()),(11,43,'HAS_TERMINOLOGY',NULL,1,NOW()),(11,36,'HAS_TERMINOLOGY','歌剧常见返场',1,NOW()),
(12,37,'HAS_TERMINOLOGY',NULL,1,NOW()),(13,37,'HAS_TERMINOLOGY',NULL,1,NOW()),
(1,46,'PERFORMED_AT','演出季常见剧目',1,NOW()),(2,47,'PERFORMED_AT',NULL,1,NOW()),(7,47,'PERFORMED_AT','驻场代表作',1,NOW()),(8,47,'PERFORMED_AT',NULL,1,NOW()),
(10,46,'PERFORMED_AT',NULL,1,NOW()),(11,49,'PERFORMED_AT',NULL,1,NOW()),(12,50,'PERFORMED_AT','音乐剧热点场馆',1,NOW()),(13,48,'PERFORMED_AT',NULL,1,NOW()),
(14,47,'WORKS_AT','长期合作',1,NOW()),(15,47,'WORKS_AT','长期合作',1,NOW()),(17,47,'WORKS_AT','长期合作',1,NOW()),(18,49,'WORKS_AT','歌剧合作',1,NOW()),(19,46,'WORKS_AT','歌剧合作',1,NOW()),
(1,2,'SIMILAR_TO','同为莎翁悲剧',1,NOW()),(1,3,'SIMILAR_TO','同为莎翁悲剧',1,NOW()),(1,4,'SIMILAR_TO','同为莎翁悲剧',1,NOW()),(5,6,'SIMILAR_TO','同为莎翁戏剧',1,NOW()),
(7,9,'SIMILAR_TO','同具京味群像气质',1,NOW()),(10,11,'SIMILAR_TO','同为舞台音乐性强的经典作品',1,NOW()),(12,13,'SIMILAR_TO','同为国际经典音乐剧',1,NOW()),
(31,32,'RELATED_TO','均为角色独立发声方式',1,NOW()),(33,40,'RELATED_TO','演员基础空间控制',1,NOW()),(40,41,'RELATED_TO','导演排练核心概念',1,NOW()),
(34,36,'RELATED_TO','演后互动流程',1,NOW()),(42,43,'RELATED_TO','歌剧两类重要唱段',1,NOW()),(44,45,'RELATED_TO','芭蕾高频术语',1,NOW()),(38,39,'RELATED_TO','演出周期关键节点',1,NOW());

SELECT 'expanded seed imported' AS message;
SELECT COUNT(*) AS node_count FROM knowledge_node;
SELECT COUNT(*) AS edge_count FROM knowledge_edge;
