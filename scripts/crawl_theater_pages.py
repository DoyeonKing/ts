#!/usr/bin/env python3
"""
最小可用的剧场公开页面采集脚手架。

设计目标：
1. 不依赖第三方库，开箱可跑
2. 先采公开 HTML 页面
3. 输出统一 JSON，方便人工校验和后续入库

用法：
python scripts/crawl_theater_pages.py --input scripts/crawler_input_urls.example.txt --output scripts/crawled_pages.json
"""

from __future__ import annotations

import argparse
import json
import re
import sys
from dataclasses import dataclass, asdict
from html import unescape
from pathlib import Path
from typing import List, Optional
from urllib.request import Request, urlopen


USER_AGENT = (
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
    "AppleWebKit/537.36 (KHTML, like Gecko) "
    "Chrome/124.0.0.0 Safari/537.36"
)


@dataclass
class CrawledPlayPage:
    source_url: str
    title: str
    description: str
    city: str
    venue_name: str
    start_time: str
    end_time: str
    min_price: Optional[float]
    max_price: Optional[float]
    status: str
    cast_text: str
    poster: str
    raw_snippet: str


def fetch_html(url: str) -> str:
    req = Request(url, headers={"User-Agent": USER_AGENT})
    with urlopen(req, timeout=20) as resp:
        return resp.read().decode("utf-8", errors="ignore")


def strip_tags(text: str) -> str:
    text = re.sub(r"<script[\s\S]*?</script>", " ", text, flags=re.I)
    text = re.sub(r"<style[\s\S]*?</style>", " ", text, flags=re.I)
    text = re.sub(r"<[^>]+>", " ", text)
    text = unescape(text)
    text = re.sub(r"\s+", " ", text).strip()
    return text


def find_first(patterns: List[str], text: str) -> str:
    for pattern in patterns:
        m = re.search(pattern, text, flags=re.I)
        if m:
            return m.group(1).strip()
    return ""


def parse_price_range(plain_text: str) -> tuple[Optional[float], Optional[float]]:
    matches = re.findall(r"(?:¥|￥)?\s*(\d{2,4})(?:\s*[-~到至]\s*(\d{2,4}))?", plain_text)
    prices: List[float] = []
    for low, high in matches:
        if low:
            prices.append(float(low))
        if high:
            prices.append(float(high))
    if not prices:
        return None, None
    return min(prices), max(prices)


def parse_page(url: str, html: str) -> CrawledPlayPage:
    plain_text = strip_tags(html)

    title = find_first(
        [
            r"<title>(.*?)</title>",
            r'property="og:title"\s+content="(.*?)"',
            r'name="title"\s+content="(.*?)"',
        ],
        html,
    )

    description = find_first(
        [
            r'property="og:description"\s+content="(.*?)"',
            r'name="description"\s+content="(.*?)"',
        ],
        html,
    )

    poster = find_first(
        [
            r'property="og:image"\s+content="(.*?)"',
            r'<img[^>]+src="(https?://[^"]+)"',
        ],
        html,
    )

    min_price, max_price = parse_price_range(plain_text)

    city = find_first(
        [
            r"(北京|上海|广州|深圳|杭州|南京|成都|武汉|西安|重庆|天津|苏州)",
        ],
        plain_text,
    )

    status = "ON_SALE"
    if any(word in plain_text for word in ["售罄", "缺货", "无票"]):
        status = "SOLD_OUT"
    elif any(word in plain_text for word in ["预售", "开售"]):
        status = "ON_SALE"

    time_matches = re.findall(
        r"(20\d{2}[-/.年]\d{1,2}[-/.月]\d{1,2}(?:日)?\s*\d{1,2}:\d{2})",
        plain_text,
    )
    start_time = time_matches[0] if time_matches else ""
    end_time = time_matches[1] if len(time_matches) > 1 else ""

    venue_name = find_first(
        [
            r"(国家大剧院|北京人民艺术剧院|天桥艺术中心|保利剧院|上剧场|上海大剧院|中央歌剧院)",
        ],
        plain_text,
    )

    cast_text = find_first(
        [
            r"(主演[:：].{0,80})",
            r"(演员[:：].{0,80})",
            r"(卡司[:：].{0,80})",
        ],
        plain_text,
    )

    raw_snippet = plain_text[:500]

    return CrawledPlayPage(
        source_url=url,
        title=title,
        description=description,
        city=city,
        venue_name=venue_name,
        start_time=start_time,
        end_time=end_time,
        min_price=min_price,
        max_price=max_price,
        status=status,
        cast_text=cast_text,
        poster=poster,
        raw_snippet=raw_snippet,
    )


def load_urls(path: Path) -> List[str]:
    urls = []
    for line in path.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if not line or line.startswith("#"):
            continue
        urls.append(line)
    return urls


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--input", required=True, help="包含 URL 列表的文本文件")
    parser.add_argument("--output", required=True, help="输出 JSON 文件")
    args = parser.parse_args()

    input_path = Path(args.input)
    output_path = Path(args.output)
    urls = load_urls(input_path)

    results = []
    for url in urls:
        try:
            html = fetch_html(url)
            results.append(asdict(parse_page(url, html)))
            print(f"[ok] {url}")
        except Exception as exc:
            print(f"[fail] {url} -> {exc}", file=sys.stderr)

    output_path.write_text(
        json.dumps(results, ensure_ascii=False, indent=2),
        encoding="utf-8",
    )
    print(f"written: {output_path}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
