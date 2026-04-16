# 剧场数据导入运行顺序

这套顺序按“先基础库，再补垂直语义”的思路来，适合你们现在做答辩版。

## 推荐顺序

### 方案 A：你们现在最稳的跑法

1. 先执行基础建库脚本

```sql
source scripts/theater_db_complete.sql;
```

这一步会创建：

- `user`
- `knowledge_node`
- `knowledge_edge`
- `performance`
- `play_rating`
- `comment`
- `actor_like`
- `terminology_feedback`

并导入一批基础剧目、场次、评分和术语。

2. 再执行增强版垂直数据脚本

```sql
source scripts/theater_domain_seed_expanded.sql;
```

这一步会补充：

- 更多戏剧行话
- 更多推荐标签
- 术语之间的关联
- 剧目和标签的 `HAS_TAG`
- 剧目和术语的 `HAS_TERMINOLOGY`

3. 如果你们前端术语页 / 图谱页还是老数据，再重启后端

Spring Boot 启动后会直接从 MySQL 读取这些数据。

## 不建议的顺序

不要把下面几份混着都跑：

- `scripts/theater_db_complete.sql`
- `scripts/knowledge-graph-seed.sql`
- `scripts/knowledge-graph-seed-expanded.sql`

原因：

- 这几份里有重复建图数据
- 有些脚本会 `DELETE FROM knowledge_node`
- 混跑容易把你刚补的标签和术语冲掉

## 最稳的执行命令

如果你是在 MySQL 命令行里：

```sql
source D:/Desktop/thearter/version_01/ts/scripts/theater_db_complete.sql;
source D:/Desktop/thearter/version_01/ts/scripts/theater_domain_seed_expanded.sql;
```

如果你是在 PowerShell 里：

```powershell
mysql -u root -p < scripts/theater_db_complete.sql
mysql -u root -p theater_db < scripts/theater_domain_seed_expanded.sql
```

第二条命令里显式指定 `theater_db`，更稳一些。

## 导入后建议检查

```sql
SELECT COUNT(*) FROM knowledge_node;
SELECT COUNT(*) FROM knowledge_edge;
SELECT node_type, COUNT(*) FROM knowledge_node GROUP BY node_type;
SELECT relation_type, COUNT(*) FROM knowledge_edge GROUP BY relation_type;
SELECT name, node_type FROM knowledge_node WHERE name IN ('走位', '调度', '演技爆发力强', '新手友好');
```

## 如果要重新来一遍

最简单：

1. 重新执行 `scripts/theater_db_complete.sql`
2. 再执行 `scripts/theater_domain_seed_expanded.sql`

因为第一份脚本会先删表再建表，相当于重置。

## 你们答辩时可以怎么说

“我们先通过基础业务库保证真实场次和票价，再通过补充的领域术语、标签体系和知识图谱关系增强推荐的专业性。这样既避免了大模型幻觉，也在小样本条件下建立了垂直剧场语义能力。”
