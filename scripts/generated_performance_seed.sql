USE theater_db;
SET NAMES utf8mb4;

-- 由 crawled_pages.json 自动生成，请导入前先人工校验 play_id / venue_id / 时间 / 票价
-- 1. 豆瓣音乐剧
INSERT INTO performance (play_id, venue_id, venue_name, city, start_time, end_time, min_price, max_price, status, created_at) VALUES (/* TODO: play_id */ 0, /* TODO: venue_id */ 0, '', '上海', '2026-01-01 19:30:00', '2026-01-01 22:00:00', 0.0, 8177.0, 'ON_SALE', NOW());

-- 2. - 大麦搜索
INSERT INTO performance (play_id, venue_id, venue_name, city, start_time, end_time, min_price, max_price, status, created_at) VALUES (/* TODO: play_id */ 0, /* TODO: venue_id */ 0, '', '北京', '2026-01-01 19:30:00', '2026-01-01 22:00:00', 20.0, 8294.0, 'ON_SALE', NOW());
