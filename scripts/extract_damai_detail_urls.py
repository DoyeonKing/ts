#!/usr/bin/env python3
"""
从大麦搜索结果 HTML 中批量抽取详情页链接。

输出两份文件：
1. 纯 URL 列表，便于后续抓取详情页
2. 带标题的 JSON，便于人工校验
"""

from __future__ import annotations

import argparse
import json
import re
from dataclasses import asdict, dataclass
from html import unescape
from pathlib import Path


@dataclass
class DetailUrlRecord:
    title: str
    city_hint: str
    detail_url: str
    item_id: str


def normalize_url(url: str) -> str:
    url = unescape(url.strip())
    url = url.replace("&amp;", "&")
    if url.startswith("//"):
        url = "https:" + url
    return url


def extract_records(html: str) -> list[DetailUrlRecord]:
    pattern = re.compile(
        r'<div[^>]+class="items__txt__title"[^>]*>\s*'
        r'<span[^>]*>【([^】]+)】</span>\s*'
        r'<a[^>]+href="(https://detail\.damai\.cn/item\.htm[^"]+)"[^>]*>(.*?)</a>',
        flags=re.S | re.I,
    )

    records: list[DetailUrlRecord] = []
    seen: set[str] = set()
    for city_hint, url, raw_title in pattern.findall(html):
        url = normalize_url(url)
        item_id_match = re.search(r"[?&]id=(\d+)", url)
        item_id = item_id_match.group(1) if item_id_match else ""
        if not item_id or item_id in seen:
            continue
        seen.add(item_id)
        title = re.sub(r"\s+", " ", unescape(raw_title)).strip()
        records.append(
            DetailUrlRecord(
                title=title,
                city_hint=city_hint.strip(),
                detail_url=url,
                item_id=item_id,
            )
        )
    return records


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--input", required=True, help="搜索结果 HTML")
    parser.add_argument("--output-urls", required=True, help="输出详情页 URL 列表")
    parser.add_argument("--output-json", required=True, help="输出带标题的 JSON")
    args = parser.parse_args()

    html = Path(args.input).read_text(encoding="utf-8", errors="ignore")
    records = extract_records(html)

    Path(args.output_urls).write_text(
        "\n".join(record.detail_url for record in records) + ("\n" if records else ""),
        encoding="utf-8",
    )
    Path(args.output_json).write_text(
        json.dumps([asdict(record) for record in records], ensure_ascii=False, indent=2),
        encoding="utf-8",
    )

    print(f"records: {len(records)}")
    print(f"written urls: {args.output_urls}")
    print(f"written json: {args.output_json}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
