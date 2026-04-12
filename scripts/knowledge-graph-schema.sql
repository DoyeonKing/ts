-- ============================================================
-- 知识图谱表结构（仅在表不存在时使用）
-- 若已用 SpringBoot 启动过，Hibernate 会自动建表，可跳过本脚本
-- ============================================================

CREATE DATABASE IF NOT EXISTS theater_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE theater_db;

CREATE TABLE IF NOT EXISTS knowledge_node (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    node_type VARCHAR(50) NOT NULL,
    description TEXT,
    extra_data TEXT,
    created_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS knowledge_edge (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    source_node_id BIGINT NOT NULL,
    target_node_id BIGINT NOT NULL,
    relation_type VARCHAR(100) NOT NULL,
    label VARCHAR(200),
    weight DOUBLE DEFAULT 1.0,
    created_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
