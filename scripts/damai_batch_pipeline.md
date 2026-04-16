# 大麦批量解析流程

## 目标

把本地保存的 `大麦搜索页 HTML` 转成：

1. 详情页 URL 清单
2. 详情页结构化事实 JSON
3. `performance` 导入 SQL

## 第一步：从搜索页 HTML 批量抽详情页链接

```bash
python scripts/extract_damai_detail_urls.py ^
  --input "scripts/- 大麦搜索.html" ^
  --output-urls scripts/damai_detail_urls_from_search.txt ^
  --output-json scripts/damai_detail_urls_from_search.json
```

输出：

- `scripts/damai_detail_urls_from_search.txt`
- `scripts/damai_detail_urls_from_search.json`

说明：

- `txt` 适合后续抓取详情页
- `json` 适合人工校验标题、城市提示、`item_id`

## 第二步：准备详情页输入

当前有两种方式：

### 方式 A：直接用本地保存的详情页 HTML

编辑：

- `scripts/damai_detail_html_inputs.txt`

一行一个本地 HTML 路径，例如：

```text
scripts/【北京】开心麻花年度最疯悬疑喜剧《谁杀了罗伯特》沉浸版【网上订票】- 大麦网.html
scripts/【北京】音乐剧《基督山伯爵》中文版【网上订票】- 大麦网.html
scripts/【上海】环境式驻演原创音乐剧《开膛手杰克》【网上订票】- 大麦网.html
```

### 方式 B：后续扩展为直接抓详情页 URL

可直接复用：

- `scripts/damai_detail_urls_from_search.txt`

当前仓库里已具备“吃 URL”的解析能力，但前提是网络可访问详情页。

## 第三步：解析详情页

```bash
python scripts/parse_damai_detail_pages.py ^
  --input scripts/damai_detail_html_inputs.txt ^
  --output scripts/damai_crawled_pages.json
```

输出：

- `scripts/damai_crawled_pages.json`

当前会重点抽取：

- `item_id`
- `title`
- `genre`
- `city`
- `venue_name`
- `venue_address`
- `show_time_text`
- `duration_text`
- `status`
- `sell_start_time`
- `price_range_text`
- `poster`
- `performances`

## 第四步：生成 performance SQL

```bash
python scripts/build_performance_sql_from_damai_json.py ^
  --input scripts/damai_crawled_pages.json ^
  --output scripts/generated_performance_seed_from_damai.sql
```

输出：

- `scripts/generated_performance_seed_from_damai.sql`

说明：

- 有 `performances` 时会展开为多条场次记录
- 没有明确场次时回落为页面级一条记录
- 导入前仍需人工补 `play_id` / `venue_id`

## 当前已验证的文件

- `scripts/- 大麦搜索.html`
- `scripts/【北京】开心麻花年度最疯悬疑喜剧《谁杀了罗伯特》沉浸版【网上订票】- 大麦网.html`
- `scripts/【北京】音乐剧《基督山伯爵》中文版【网上订票】- 大麦网.html`
- `scripts/【上海】环境式驻演原创音乐剧《开膛手杰克》【网上订票】- 大麦网.html`

## 当前结论

- 搜索页适合“发现详情页链接”
- 详情页适合“抽事实”
- 不要把搜索页直接当事实源
