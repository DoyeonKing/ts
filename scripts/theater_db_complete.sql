CREATE DATABASE IF NOT EXISTS theater_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE theater_db;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS=0;
DROP TABLE IF EXISTS comment;
DROP TABLE IF EXISTS play_rating;
DROP TABLE IF EXISTS actor_like;
DROP TABLE IF EXISTS terminology_feedback;
DROP TABLE IF EXISTS performance;
DROP TABLE IF EXISTS knowledge_edge;
DROP TABLE IF EXISTS knowledge_node;
DROP TABLE IF EXISTS user;
SET FOREIGN_KEY_CHECKS=1;

CREATE TABLE user (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(64) NOT NULL UNIQUE,
  password_hash VARCHAR(128),
  nickname VARCHAR(64),
  avatar VARCHAR(128),
  created_at DATETIME,
  updated_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE knowledge_node (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(200) NOT NULL,
  node_type ENUM('PLAY','ACTOR','TERMINOLOGY','TAG','VENUE') NOT NULL,
  description TEXT,
  extra_data TEXT,
  created_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE knowledge_edge (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  source_node_id BIGINT NOT NULL,
  target_node_id BIGINT NOT NULL,
  relation_type VARCHAR(100) NOT NULL,
  label VARCHAR(200),
  weight DOUBLE DEFAULT 1.0,
  created_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE performance (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  play_id BIGINT NOT NULL,
  venue_id BIGINT NOT NULL,
  venue_name VARCHAR(200) NOT NULL,
  city VARCHAR(50) NOT NULL,
  start_time DATETIME NOT NULL,
  end_time DATETIME NOT NULL,
  min_price DECIMAL(10,2) NOT NULL,
  max_price DECIMAL(10,2) NOT NULL,
  status VARCHAR(50) NOT NULL,
  created_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE play_rating (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  play_id BIGINT NOT NULL,
  rating INT NOT NULL,
  content TEXT,
  created_at DATETIME,
  updated_at DATETIME,
  UNIQUE KEY uk_play_rating_user_play (user_id, play_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE comment (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  target_type VARCHAR(20) NOT NULL,
  target_id BIGINT NOT NULL,
  parent_id BIGINT NULL,
  content TEXT NOT NULL,
  created_at DATETIME,
  updated_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE actor_like (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  actor_id BIGINT NOT NULL,
  created_at DATETIME,
  UNIQUE KEY uk_actor_like_user_actor (user_id, actor_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE terminology_feedback (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  terminology_id BIGINT NOT NULL,
  rating INT NOT NULL,
  created_at DATETIME,
  UNIQUE KEY uk_terminology_feedback_user_term (user_id, terminology_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_knowledge_node_type_name ON knowledge_node(node_type, name);
CREATE INDEX idx_knowledge_edge_source ON knowledge_edge(source_node_id);
CREATE INDEX idx_knowledge_edge_target ON knowledge_edge(target_node_id);
CREATE INDEX idx_knowledge_edge_relation ON knowledge_edge(relation_type);
CREATE INDEX idx_performance_play_city_time ON performance(play_id, city, start_time);
CREATE INDEX idx_performance_status ON performance(status);
CREATE INDEX idx_comment_target_parent ON comment(target_type, target_id, parent_id);

INSERT INTO user (id,username,password_hash,nickname,avatar,created_at,updated_at) VALUES
(1,'demo1','hash1','剧场小白','/static/a.png',NOW(),NOW()),
(2,'demo2','hash2','莎剧迷','/static/b.png',NOW(),NOW()),
(3,'demo3','hash3','歌剧控','/static/c.png',NOW(),NOW()),
(4,'demo4','hash4','音乐剧党','/static/d.png',NOW(),NOW());

INSERT INTO knowledge_node (id,name,node_type,description,extra_data,created_at) VALUES
(1,'哈姆雷特','PLAY','莎士比亚四大悲剧之一','{"genre":"悲剧","rating":9.5}',NOW()),
(2,'李尔王','PLAY','权力、亲情与背叛','{"genre":"悲剧","rating":9.1}',NOW()),
(3,'罗密欧与朱丽叶','PLAY','经典爱情悲剧','{"genre":"爱情悲剧","rating":9.3}',NOW()),
(4,'茶馆','PLAY','老舍经典话剧','{"genre":"话剧","rating":9.6}',NOW()),
(5,'天鹅湖','PLAY','古典芭蕾代表作','{"genre":"芭蕾","rating":9.0}',NOW()),
(6,'茶花女','PLAY','威尔第经典歌剧','{"genre":"歌剧","rating":9.4}',NOW()),
(7,'歌剧魅影','PLAY','全球知名音乐剧','{"genre":"音乐剧","rating":9.5}',NOW()),
(8,'恋爱的犀牛','PLAY','中国当代爱情话剧代表作','{"genre":"话剧","rating":9.2}',NOW()),
(9,'雷雨','PLAY','中国现代戏剧经典','{"genre":"话剧","rating":9.4}',NOW()),
(10,'胡桃夹子','PLAY','圣诞档高人气芭蕾','{"genre":"芭蕾","rating":8.9}',NOW()),
(11,'图兰朵','PLAY','经典大型歌剧','{"genre":"歌剧","rating":9.3}',NOW()),
(12,'猫','PLAY','经典英文音乐剧','{"genre":"音乐剧","rating":9.1}',NOW()),
(13,'濮存昕','ACTOR','著名话剧演员','{"specialty":"话剧、莎剧"}',NOW()),
(14,'何冰','ACTOR','北京人艺代表演员','{"specialty":"话剧、京味戏"}',NOW()),
(15,'袁泉','ACTOR','兼具舞台与影视经验','{"specialty":"话剧、音乐剧"}',NOW()),
(16,'廖昌永','ACTOR','著名男中音歌唱家','{"specialty":"歌剧"}',NOW()),
(17,'朱洁静','ACTOR','知名舞剧与芭蕾演员','{"specialty":"舞剧、芭蕾"}',NOW()),
(18,'阿云嘎','ACTOR','音乐剧演员与歌手','{"specialty":"音乐剧"}',NOW()),
(19,'莎士比亚','TAG','莎剧相关标签',NULL,NOW()),
(20,'老舍','TAG','老舍作品标签',NULL,NOW()),
(21,'悲剧','TAG','悲剧类型标签',NULL,NOW()),
(22,'爱情','TAG','爱情主题标签',NULL,NOW()),
(23,'经典','TAG','经典常演作品标签',NULL,NOW()),
(24,'话剧','TAG','话剧类标签',NULL,NOW()),
(25,'芭蕾','TAG','芭蕾类标签',NULL,NOW()),
(26,'歌剧','TAG','歌剧类标签',NULL,NOW()),
(27,'音乐剧','TAG','音乐剧类标签',NULL,NOW()),
(28,'独白','TERMINOLOGY','角色独自吐露内心',NULL,NOW()),
(29,'谢幕','TERMINOLOGY','演出结束致意',NULL,NOW()),
(30,'卡司','TERMINOLOGY','演出阵容',NULL,NOW()),
(31,'返场','TERMINOLOGY','谢幕后再次登台',NULL,NOW()),
(32,'幕间','TERMINOLOGY','两幕之间休息',NULL,NOW()),
(33,'咏叹调','TERMINOLOGY','歌剧中展现人物情感的独唱段落',NULL,NOW()),
(34,'舞台调度','TERMINOLOGY','舞台空间中的人物运动与布局',NULL,NOW()),
(35,'国家大剧院','VENUE','国家级表演艺术中心','{"city":"北京"}',NOW()),
(36,'北京人民艺术剧院','VENUE','中国话剧重镇','{"city":"北京"}',NOW()),
(37,'中央歌剧院','VENUE','重要歌剧演出机构','{"city":"北京"}',NOW()),
(38,'天桥艺术中心','VENUE','音乐剧热点场馆','{"city":"北京"}',NOW()),
(39,'保利剧院','VENUE','北京热门商业演出场馆','{"city":"北京"}',NOW()),
(40,'上海大剧院','VENUE','上海地标性综合表演艺术剧院','{"city":"上海"}',NOW()),
(41,'上海文化广场','VENUE','上海热门音乐剧演出场馆','{"city":"上海"}',NOW()),
(42,'上剧场','VENUE','以上海话剧与实验戏剧见长','{"city":"上海"}',NOW());

INSERT INTO knowledge_edge (source_node_id,target_node_id,relation_type,label,weight,created_at) VALUES
(13,1,'PERFORMS_IN','饰 哈姆雷特',1,NOW()),
(13,2,'PERFORMS_IN','饰 李尔王',1,NOW()),
(14,4,'PERFORMS_IN','饰 松二爷',1,NOW()),
(15,1,'PERFORMS_IN','饰 奥菲利亚',1,NOW()),
(16,6,'PERFORMS_IN','主演',1,NOW()),
(17,5,'PERFORMS_IN','主演',1,NOW()),
(18,7,'PERFORMS_IN','主演',1,NOW()),
(18,12,'PERFORMS_IN','主演',1,NOW()),
(1,19,'HAS_TAG',NULL,1,NOW()),(1,21,'HAS_TAG',NULL,1,NOW()),(1,23,'HAS_TAG',NULL,1,NOW()),
(2,19,'HAS_TAG',NULL,1,NOW()),(2,21,'HAS_TAG',NULL,1,NOW()),(2,23,'HAS_TAG',NULL,1,NOW()),
(3,19,'HAS_TAG',NULL,1,NOW()),(3,22,'HAS_TAG',NULL,1,NOW()),(3,23,'HAS_TAG',NULL,1,NOW()),
(4,20,'HAS_TAG',NULL,1,NOW()),(4,24,'HAS_TAG',NULL,1,NOW()),(4,23,'HAS_TAG',NULL,1,NOW()),
(5,25,'HAS_TAG',NULL,1,NOW()),(5,23,'HAS_TAG',NULL,1,NOW()),
(6,26,'HAS_TAG',NULL,1,NOW()),(6,22,'HAS_TAG',NULL,1,NOW()),(6,23,'HAS_TAG',NULL,1,NOW()),
(7,27,'HAS_TAG',NULL,1,NOW()),(7,23,'HAS_TAG',NULL,1,NOW()),
(8,24,'HAS_TAG',NULL,1,NOW()),(8,22,'HAS_TAG',NULL,1,NOW()),
(9,24,'HAS_TAG',NULL,1,NOW()),(9,21,'HAS_TAG',NULL,1,NOW()),(9,23,'HAS_TAG',NULL,1,NOW()),
(10,25,'HAS_TAG',NULL,1,NOW()),(10,23,'HAS_TAG',NULL,1,NOW()),
(11,26,'HAS_TAG',NULL,1,NOW()),(11,23,'HAS_TAG',NULL,1,NOW()),
(12,27,'HAS_TAG',NULL,1,NOW()),(12,23,'HAS_TAG',NULL,1,NOW()),
(1,28,'HAS_TERMINOLOGY','生存还是毁灭',1,NOW()),
(1,30,'HAS_TERMINOLOGY',NULL,1,NOW()),
(4,32,'HAS_TERMINOLOGY','三幕结构',1,NOW()),
(4,34,'HAS_TERMINOLOGY','群像戏调度',1,NOW()),
(6,31,'HAS_TERMINOLOGY','歌剧常见返场',1,NOW()),
(6,33,'HAS_TERMINOLOGY','经典咏叹调',1,NOW()),
(11,33,'HAS_TERMINOLOGY','公主独唱段落',1,NOW()),
(1,35,'PERFORMED_AT','演出季常见剧目',1,NOW()),
(2,36,'PERFORMED_AT',NULL,1,NOW()),
(3,35,'PERFORMED_AT',NULL,1,NOW()),
(4,36,'PERFORMED_AT','驻场代表作',1,NOW()),
(5,35,'PERFORMED_AT',NULL,1,NOW()),
(6,37,'PERFORMED_AT',NULL,1,NOW()),
(7,38,'PERFORMED_AT','音乐剧热点场馆',1,NOW()),
(8,39,'PERFORMED_AT',NULL,1,NOW()),
(9,36,'PERFORMED_AT',NULL,1,NOW()),
(10,35,'PERFORMED_AT',NULL,1,NOW()),
(11,37,'PERFORMED_AT',NULL,1,NOW()),
(12,38,'PERFORMED_AT',NULL,1,NOW()),
(8,42,'PERFORMED_AT','上海口碑场馆',1,NOW()),
(9,42,'PERFORMED_AT',NULL,1,NOW()),
(7,41,'PERFORMED_AT','上海音乐剧热门场馆',1,NOW()),
(10,40,'PERFORMED_AT',NULL,1,NOW()),
(13,36,'WORKS_AT','长期合作',1,NOW()),
(14,36,'WORKS_AT','长期合作',1,NOW()),
(16,37,'WORKS_AT','歌剧合作',1,NOW()),
(18,38,'WORKS_AT','音乐剧合作',1,NOW()),
(1,2,'SIMILAR_TO','同为莎翁悲剧',1,NOW()),
(1,3,'SIMILAR_TO','同为莎翁作品',1,NOW()),
(4,9,'SIMILAR_TO','同属经典话剧',1,NOW()),
(5,10,'SIMILAR_TO','同属古典芭蕾',1,NOW()),
(6,11,'SIMILAR_TO','同属经典歌剧',1,NOW()),
(7,12,'SIMILAR_TO','同属经典音乐剧',1,NOW()),
(28,29,'RELATED_TO','演出表达相关',1,NOW()),
(30,31,'RELATED_TO','演后互动流程',1,NOW());

INSERT INTO performance (id,play_id,venue_id,venue_name,city,start_time,end_time,min_price,max_price,status,created_at) VALUES
(1,1,35,'国家大剧院','北京','2026-04-11 19:30:00','2026-04-11 22:00:00',180,480,'ON_SALE',NOW()),
(2,1,35,'国家大剧院','北京','2026-04-12 14:30:00','2026-04-12 17:00:00',220,580,'ON_SALE',NOW()),
(3,2,36,'北京人民艺术剧院','北京','2026-04-12 19:30:00','2026-04-12 22:00:00',160,320,'ON_SALE',NOW()),
(4,3,35,'国家大剧院','北京','2026-04-18 19:30:00','2026-04-18 22:00:00',260,680,'ON_SALE',NOW()),
(5,4,36,'北京人民艺术剧院','北京','2026-04-13 19:30:00','2026-04-13 22:00:00',120,280,'ON_SALE',NOW()),
(6,5,35,'国家大剧院','北京','2026-04-19 19:30:00','2026-04-19 21:40:00',200,560,'ON_SALE',NOW()),
(7,6,37,'中央歌剧院','北京','2026-04-20 19:30:00','2026-04-20 22:10:00',240,520,'ON_SALE',NOW()),
(8,7,38,'天桥艺术中心','北京','2026-04-25 19:30:00','2026-04-25 22:20:00',380,980,'ON_SALE',NOW()),
(9,8,39,'保利剧院','北京','2026-04-26 19:30:00','2026-04-26 22:00:00',180,380,'ON_SALE',NOW()),
(10,9,36,'北京人民艺术剧院','北京','2026-04-17 19:30:00','2026-04-17 22:00:00',150,360,'ON_SALE',NOW()),
(11,10,35,'国家大剧院','北京','2026-04-24 19:30:00','2026-04-24 21:30:00',180,460,'ON_SALE',NOW()),
(12,11,37,'中央歌剧院','北京','2026-04-27 19:30:00','2026-04-27 22:20:00',260,620,'ON_SALE',NOW()),
(13,12,38,'天桥艺术中心','北京','2026-04-30 19:30:00','2026-04-30 22:00:00',320,880,'ON_SALE',NOW()),
(14,8,42,'上剧场','上海','2026-04-25 19:30:00','2026-04-25 22:00:00',180,300,'ON_SALE',NOW()),
(15,9,42,'上剧场','上海','2026-04-26 19:30:00','2026-04-26 22:00:00',220,300,'ON_SALE',NOW()),
(16,7,41,'上海文化广场','上海','2026-04-25 19:30:00','2026-04-25 22:20:00',260,300,'ON_SALE',NOW()),
(17,10,40,'上海大剧院','上海','2026-04-26 14:30:00','2026-04-26 16:40:00',200,300,'ON_SALE',NOW());

INSERT INTO play_rating (id,user_id,play_id,rating,content,created_at,updated_at) VALUES
(1,1,1,5,'第一次现场看莎剧，氛围很强。',NOW(),NOW()),
(2,2,1,5,'独白处理很细腻。',NOW(),NOW()),
(3,3,6,5,'唱段很稳，舞美完整。',NOW(),NOW()),
(4,4,7,5,'很适合音乐剧新手。',NOW(),NOW()),
(5,1,5,4,'双人舞确实惊艳。',NOW(),NOW()),
(6,2,4,5,'群像戏很扎实。',NOW(),NOW()),
(7,1,8,5,'爱情表达很直接，适合新观众。',NOW(),NOW()),
(8,2,9,5,'经典冲突很强，台词很有力量。',NOW(),NOW()),
(9,3,11,4,'大歌剧氛围很足。',NOW(),NOW()),
(10,4,12,5,'旋律熟悉，舞台节奏很快。',NOW(),NOW());

INSERT INTO comment (id,user_id,target_type,target_id,parent_id,content,created_at,updated_at) VALUES
(1,1,'PLAY',1,NULL,'哈姆雷特适合第一次进剧场看吗？',NOW(),NOW()),
(2,2,'PLAY',1,1,'适合，先了解人物关系会更好。',NOW(),NOW()),
(3,3,'PLAY',4,NULL,'茶馆这版节奏怎么样？',NOW(),NOW()),
(4,4,'PLAY',4,3,'不闷，群像很精彩。',NOW(),NOW()),
(5,2,'PLAY',6,NULL,'茶花女适合带长辈一起看。',NOW(),NOW()),
(6,1,'PLAY',8,NULL,'恋爱的犀牛是不是更适合情侣看？',NOW(),NOW()),
(7,2,'PLAY',9,NULL,'雷雨这版冲突感很强。',NOW(),NOW());

INSERT INTO actor_like (id,user_id,actor_id,created_at) VALUES
(1,1,13,NOW()),(2,1,15,NOW()),(3,2,13,NOW()),(4,2,14,NOW()),(5,3,16,NOW()),(6,4,15,NOW()),(7,4,18,NOW());

INSERT INTO terminology_feedback (id,user_id,terminology_id,rating,created_at) VALUES
(1,1,28,1,NOW()),(2,1,30,1,NOW()),(3,2,32,1,NOW()),(4,3,31,1,NOW()),(5,4,29,1,NOW()),(6,3,33,1,NOW()),(7,2,34,1,NOW());

ALTER TABLE user AUTO_INCREMENT=5;
ALTER TABLE knowledge_node AUTO_INCREMENT=43;
ALTER TABLE performance AUTO_INCREMENT=18;
ALTER TABLE play_rating AUTO_INCREMENT=11;
ALTER TABLE comment AUTO_INCREMENT=8;
ALTER TABLE actor_like AUTO_INCREMENT=8;
ALTER TABLE terminology_feedback AUTO_INCREMENT=8;
