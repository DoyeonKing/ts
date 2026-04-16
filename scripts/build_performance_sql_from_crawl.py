#!/usr/bin/env python3
"""
把 crawl_theater_pages.py 产出的 JSON 转成 performance 插入 SQL。

用法：
python scripts/build_performance_sql_from_crawl.py ^
  --input scripts/crawled_pages.json ^
  --output scripts/generated_performance_seed.sql
"""

from __future__ import annotations

import argparse
import json
from pathlib import Path


def sql_escape(value: str) -> str:
    return value.replace("\\", "\\\\").replace("'", "\\'")


def normalize_status(status: str) -> str:
    text = (status or "").upper()
    if text in {"ON_SALE", "SOLD_OUT"}:
        return text
    return "ON_SALE"


def build_sql(records: list[dict]) -> str:
    lines = [
        "USE theater_db;",
        "SET NAMES utf8mb4;",
        "",
        "-- 由 crawled_pages.json 自动生成，请导入前先人工校验 play_id / venue_id / 时间 / 票价",
    ]

    for idx, item in enumerate(records, start=1):
        title = item.get("title") or ""
        city = item.get("city") or ""
        venue_name = item.get("venue_name") or ""
        start_time = item.get("start_time") or "2026-01-01 19:30:00"
        end_time = item.get("end_time") or "2026-01-01 22:00:00"
        min_price = item.get("min_price") if item.get("min_price") is not None else 0
        max_price = item.get("max_price") if item.get("max_price") is not None else min_price
        status = normalize_status(item.get("status") or "ON_SALE")

        lines.append(f"-- {idx}. {title}")
        lines.append(
            "INSERT INTO performance "
            "(play_id, venue_id, venue_name, city, start_time, end_time, min_price, max_price, status, created_at) "
            "VALUES "
            f"(/* TODO: play_id */ 0, /* TODO: venue_id */ 0, '{sql_escape(venue_name)}', '{sql_escape(city)}', "
            f"'{sql_escape(start_time)}', '{sql_escape(end_time)}', {min_price}, {max_price}, '{status}', NOW());"
        )
        lines.append("")

    return "\n".join(lines)


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--input", required=True)
    parser.add_argument("--output", required=True)
    args = parser.parse_args()

    input_path = Path(args.input)
    output_path = Path(args.output)
    records = json.loads(input_path.read_text(encoding="utf-8"))
    output_path.write_text(build_sql(records), encoding="utf-8")
    print(f"written: {output_path}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
