USE theater_db;
SET NAMES utf8mb4;

-- 由大麦详情页专用解析结果自动生成
-- 导入前请人工校验 play_id / venue_id / start_time / end_time / 票价 / 状态

-- 1. 【北京】开心麻花年度最疯悬疑喜剧《谁杀了罗伯特》沉浸版 | 2026-05-03 星期日 14:30
INSERT INTO performance (play_id, venue_id, venue_name, city, start_time, end_time, min_price, max_price, status, created_at) VALUES (/* TODO: play_id */ 0, /* TODO: venue_id */ 0, '北京·隆福寺A99剧场', '北京市', '2026-05-03 14:30:00', '2026-05-03 16:30:00', 100.0, 480.0, 'SOLD_OUT', NOW());

-- 2. 【北京】开心麻花年度最疯悬疑喜剧《谁杀了罗伯特》沉浸版 | 2026-05-03 星期日 19:30
INSERT INTO performance (play_id, venue_id, venue_name, city, start_time, end_time, min_price, max_price, status, created_at) VALUES (/* TODO: play_id */ 0, /* TODO: venue_id */ 0, '北京·隆福寺A99剧场', '北京市', '2026-05-03 19:30:00', '2026-05-03 21:30:00', 100.0, 480.0, 'SOLD_OUT', NOW());

-- 3. 【北京】音乐剧《基督山伯爵》中文版
INSERT INTO performance (play_id, venue_id, venue_name, city, start_time, end_time, min_price, max_price, status, created_at) VALUES (/* TODO: play_id */ 0, /* TODO: venue_id */ 0, '天桥艺术中心-大剧场', '北京市', '2026-05-06 19:30:00', '2026-05-06 22:00:00', 80.0, 1080.0, 'UNSUPPORTED', NOW());

-- 4. 【上海】环境式驻演原创音乐剧《开膛手杰克》
INSERT INTO performance (play_id, venue_id, venue_name, city, start_time, end_time, min_price, max_price, status, created_at) VALUES (/* TODO: play_id */ 0, /* TODO: venue_id */ 0, '星空间82号·好剧场', '上海市', '2026-05-08 19:30:00', '2026-05-08 21:20:00', 179.1, 399.0, 'UNSUPPORTED', NOW());
