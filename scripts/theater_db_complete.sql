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
(8,'濮存昕','ACTOR','著名话剧演员','{"specialty":"话剧、莎剧"}',NOW()),
(9,'何冰','ACTOR','北京人艺代表演员','{"specialty":"话剧、京味戏"}',NOW()),
(10,'袁泉','ACTOR','兼具舞台与影视经验','{"specialty":"话剧、音乐剧"}',NOW()),
(11,'廖昌永','ACTOR','著名男中音歌唱家','{"specialty":"歌剧"}',NOW()),
(12,'莎士比亚','TAG','莎剧相关标签',NULL,NOW()),
(13,'老舍','TAG','老舍作品标签',NULL,NOW()),
(14,'悲剧','TAG','悲剧类型标签',NULL,NOW()),
(15,'爱情','TAG','爱情主题标签',NULL,NOW()),
(16,'经典','TAG','经典常演作品标签',NULL,NOW()),
(17,'话剧','TAG','话剧类标签',NULL,NOW()),
(18,'芭蕾','TAG','芭蕾类标签',NULL,NOW()),
(19,'歌剧','TAG','歌剧类标签',NULL,NOW()),
(20,'音乐剧','TAG','音乐剧类标签',NULL,NOW()),
(21,'独白','TERMINOLOGY','角色独自吐露内心',NULL,NOW()),
(22,'谢幕','TERMINOLOGY','演出结束致意',NULL,NOW()),
(23,'卡司','TERMINOLOGY','演出阵容',NULL,NOW()),
(24,'返场','TERMINOLOGY','谢幕后再次登台',NULL,NOW()),
(25,'幕间','TERMINOLOGY','两幕之间休息',NULL,NOW()),
(26,'国家大剧院','VENUE','国家级表演艺术中心','{"city":"北京"}',NOW()),
(27,'北京人民艺术剧院','VENUE','中国话剧重镇','{"city":"北京"}',NOW()),
(28,'中央歌剧院','VENUE','重要歌剧演出机构','{"city":"北京"}',NOW()),
(29,'天桥艺术中心','VENUE','音乐剧热点场馆','{"city":"北京"}',NOW());

INSERT INTO knowledge_edge (source_node_id,target_node_id,relation_type,label,weight,created_at) VALUES
(8,1,'PERFORMS_IN','饰 哈姆雷特',1,NOW()),
(8,2,'PERFORMS_IN','饰 李尔王',1,NOW()),
(9,4,'PERFORMS_IN','饰 松二爷',1,NOW()),
(10,1,'PERFORMS_IN','饰 奥菲利亚',1,NOW()),
(11,6,'PERFORMS_IN','主演',1,NOW()),
(1,12,'HAS_TAG',NULL,1,NOW()),(1,14,'HAS_TAG',NULL,1,NOW()),(1,16,'HAS_TAG',NULL,1,NOW()),
(2,12,'HAS_TAG',NULL,1,NOW()),(2,14,'HAS_TAG',NULL,1,NOW()),
(3,12,'HAS_TAG',NULL,1,NOW()),(3,15,'HAS_TAG',NULL,1,NOW()),
(4,13,'HAS_TAG',NULL,1,NOW()),(4,17,'HAS_TAG',NULL,1,NOW()),(4,16,'HAS_TAG',NULL,1,NOW()),
(5,18,'HAS_TAG',NULL,1,NOW()),(5,16,'HAS_TAG',NULL,1,NOW()),
(6,19,'HAS_TAG',NULL,1,NOW()),(6,15,'HAS_TAG',NULL,1,NOW()),
(7,20,'HAS_TAG',NULL,1,NOW()),(7,16,'HAS_TAG',NULL,1,NOW()),
(1,21,'HAS_TERMINOLOGY','生存还是毁灭',1,NOW()),
(1,23,'HAS_TERMINOLOGY',NULL,1,NOW()),
(4,25,'HAS_TERMINOLOGY','三幕结构',1,NOW()),
(6,24,'HAS_TERMINOLOGY','歌剧常见返场',1,NOW()),
(1,26,'PERFORMED_AT','演出季常见剧目',1,NOW()),
(2,27,'PERFORMED_AT',NULL,1,NOW()),
(4,27,'PERFORMED_AT','驻场代表作',1,NOW()),
(5,26,'PERFORMED_AT',NULL,1,NOW()),
(6,28,'PERFORMED_AT',NULL,1,NOW()),
(7,29,'PERFORMED_AT','音乐剧热点场馆',1,NOW()),
(8,27,'WORKS_AT','长期合作',1,NOW()),
(9,27,'WORKS_AT','长期合作',1,NOW()),
(11,28,'WORKS_AT','歌剧合作',1,NOW()),
(1,2,'SIMILAR_TO','同为莎翁悲剧',1,NOW()),
(1,3,'SIMILAR_TO','同为莎翁作品',1,NOW()),
(21,22,'RELATED_TO','演出表达相关',1,NOW()),
(23,24,'RELATED_TO','演后互动流程',1,NOW());

INSERT INTO performance (id,play_id,venue_id,venue_name,city,start_time,end_time,min_price,max_price,status,created_at) VALUES
(1,1,26,'国家大剧院','北京','2026-04-11 19:30:00','2026-04-11 22:00:00',180,480,'ON_SALE',NOW()),
(2,1,26,'国家大剧院','北京','2026-04-12 14:30:00','2026-04-12 17:00:00',220,580,'ON_SALE',NOW()),
(3,2,27,'北京人民艺术剧院','北京','2026-04-12 19:30:00','2026-04-12 22:00:00',160,320,'ON_SALE',NOW()),
(4,3,26,'国家大剧院','北京','2026-04-18 19:30:00','2026-04-18 22:00:00',260,680,'ON_SALE',NOW()),
(5,4,27,'北京人民艺术剧院','北京','2026-04-13 19:30:00','2026-04-13 22:00:00',120,280,'ON_SALE',NOW()),
(6,5,26,'国家大剧院','北京','2026-04-19 19:30:00','2026-04-19 21:40:00',200,560,'ON_SALE',NOW()),
(7,6,28,'中央歌剧院','北京','2026-04-20 19:30:00','2026-04-20 22:10:00',240,520,'ON_SALE',NOW()),
(8,7,29,'天桥艺术中心','北京','2026-04-25 19:30:00','2026-04-25 22:20:00',380,980,'ON_SALE',NOW());

INSERT INTO play_rating (id,user_id,play_id,rating,content,created_at,updated_at) VALUES
(1,1,1,5,'第一次现场看莎剧，氛围很强。',NOW(),NOW()),
(2,2,1,5,'独白处理很细腻。',NOW(),NOW()),
(3,3,6,5,'唱段很稳，舞美完整。',NOW(),NOW()),
(4,4,7,5,'很适合音乐剧新手。',NOW(),NOW()),
(5,1,5,4,'双人舞确实惊艳。',NOW(),NOW()),
(6,2,4,5,'群像戏很扎实。',NOW(),NOW());

INSERT INTO comment (id,user_id,target_type,target_id,parent_id,content,created_at,updated_at) VALUES
(1,1,'PLAY',1,NULL,'哈姆雷特适合第一次进剧场看吗？',NOW(),NOW()),
(2,2,'PLAY',1,1,'适合，先了解人物关系会更好。',NOW(),NOW()),
(3,3,'PLAY',4,NULL,'茶馆这版节奏怎么样？',NOW(),NOW()),
(4,4,'PLAY',4,3,'不闷，群像很精彩。',NOW(),NOW()),
(5,2,'PLAY',6,NULL,'茶花女适合带长辈一起看。',NOW(),NOW());

INSERT INTO actor_like (id,user_id,actor_id,created_at) VALUES
(1,1,8,NOW()),(2,1,10,NOW()),(3,2,8,NOW()),(4,2,9,NOW()),(5,3,11,NOW()),(6,4,10,NOW());

INSERT INTO terminology_feedback (id,user_id,terminology_id,rating,created_at) VALUES
(1,1,21,1,NOW()),(2,1,23,1,NOW()),(3,2,25,1,NOW()),(4,3,24,1,NOW()),(5,4,22,1,NOW());

ALTER TABLE user AUTO_INCREMENT=5;
ALTER TABLE knowledge_node AUTO_INCREMENT=30;
ALTER TABLE performance AUTO_INCREMENT=9;
ALTER TABLE play_rating AUTO_INCREMENT=7;
ALTER TABLE comment AUTO_INCREMENT=6;
ALTER TABLE actor_like AUTO_INCREMENT=7;
ALTER TABLE terminology_feedback AUTO_INCREMENT=6;
