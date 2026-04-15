#!/usr/bin/env python3
"""
把 parse_damai_detail_pages.py 的输出转成 performance SQL。

特点：
1. 优先按 performances 数组展开为多条场次记录。
2. 如果页面没有明确场次，则回落到页面级一条记录。
3. 保留 TODO play_id / venue_id，便于人工校验后入库。
"""

from __future__ import annotations

import argparse
import json
from pathlib import Path


def sql_escape(value: str) -> str:
    return value.replace("\\", "\\\\").replace("'", "\\'")


def normalize_status(status: str) -> str:
    text = (status or "").upper()
    if text in {"ON_SALE", "SOLD_OUT", "UNSUPPORTED"}:
        return text
    return "ON_SALE"


def build_insert(item: dict, perf: dict, index: int) -> list[str]:
    title = item.get("title") or ""
    city = item.get("city") or ""
    venue_name = item.get("venue_name") or ""
    start_time = perf.get("start_time") or item.get("start_time") or "2026-01-01 19:30:00"
    end_time = perf.get("end_time") or item.get("end_time") or "2026-01-01 22:00:00"
    min_price = perf.get("min_price")
    max_price = perf.get("max_price")
    if min_price is None:
        min_price = item.get("min_price")
    if max_price is None:
        max_price = item.get("max_price")
    if min_price is None:
        min_price = 0
    if max_price is None:
        max_price = min_price
    status = normalize_status(perf.get("status") or item.get("status") or "ON_SALE")
    perform_name = perf.get("perform_name") or ""
    perform_id = perf.get("perform_id")

    lines = [
        f"-- {index}. {title}" + (f" | {perform_name}" if perform_name else ""),
        "INSERT INTO performance "
        "(play_id, venue_id, venue_name, city, start_time, end_time, min_price, max_price, status, created_at) "
        "VALUES "
        f"(/* TODO: play_id */ 0, /* TODO: venue_id */ 0, '{sql_escape(venue_name)}', '{sql_escape(city)}', "
        f"'{sql_escape(start_time)}', '{sql_escape(end_time)}', {min_price}, {max_price}, '{status}', NOW());",
    ]
    if perform_id is not None:
        lines.append(f"-- source perform_id: {perform_id}")
    return lines


def build_sql(records: list[dict]) -> str:
    lines = [
        "USE theater_db;",
        "SET NAMES utf8mb4;",
        "",
        "-- 由大麦详情页专用解析结果自动生成",
        "-- 导入前请人工校验 play_id / venue_id / start_time / end_time / 票价 / 状态",
        "",
    ]

    index = 1
    for item in records:
        performances = item.get("performances") or []
        if performances:
            for perf in performances:
                lines.extend(build_insert(item, perf, index))
                lines.append("")
                index += 1
        else:
            lines.extend(build_insert(item, {}, index))
            lines.append("")
            index += 1

    return "\n".join(lines)


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--input", required=True)
    parser.add_argument("--output", required=True)
    args = parser.parse_args()

    records = json.loads(Path(args.input).read_text(encoding="utf-8"))
    Path(args.output).write_text(build_sql(records), encoding="utf-8")
    print(f"written: {args.output}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
