#!/usr/bin/env python3
"""
大麦详情页专用解析器。

输入支持两种来源：
1. 详情页 URL
2. 本地保存的详情页 HTML 路径

输出为结构化 JSON，重点区分页级事实与场次级事实。
"""

from __future__ import annotations

import argparse
import json
import re
import sys
from dataclasses import asdict, dataclass, field
from datetime import datetime, timedelta
from html import unescape
from pathlib import Path
from typing import Any, Optional
from urllib.request import Request, urlopen


USER_AGENT = (
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
    "AppleWebKit/537.36 (KHTML, like Gecko) "
    "Chrome/124.0.0.0 Safari/537.36"
)


@dataclass
class CrawledPerformance:
    perform_id: Optional[int]
    perform_name: str
    perform_date_text: str
    start_time: str
    end_time: str
    min_price: Optional[float]
    max_price: Optional[float]
    status: str
    tags: list[str] = field(default_factory=list)


@dataclass
class CrawledPlayPage:
    source_url: str
    source_type: str
    item_id: Optional[int]
    title: str
    description: str
    genre: str
    city: str
    venue_name: str
    venue_address: str
    show_time_text: str
    duration_text: str
    status: str
    sell_start_time: str
    price_range_text: str
    start_time: str
    end_time: str
    min_price: Optional[float]
    max_price: Optional[float]
    cast_text: str
    poster: str
    raw_snippet: str
    performances: list[CrawledPerformance] = field(default_factory=list)
    debug_data_presence: dict[str, bool] = field(default_factory=dict)


def fetch_html(source: str) -> str:
    if source.startswith(("http://", "https://")):
        req = Request(source, headers={"User-Agent": USER_AGENT})
        with urlopen(req, timeout=20) as resp:
            return resp.read().decode("utf-8", errors="ignore")

    path = Path(source)
    if not path.is_absolute():
        path = Path.cwd() / path
    return path.read_text(encoding="utf-8", errors="ignore")


def strip_tags(text: str) -> str:
    text = re.sub(r"<script[\s\S]*?</script>", " ", text, flags=re.I)
    text = re.sub(r"<style[\s\S]*?</style>", " ", text, flags=re.I)
    text = re.sub(r"<[^>]+>", " ", text)
    return re.sub(r"\s+", " ", unescape(text)).strip()


def extract_hidden_json(html: str, block_id: str) -> dict[str, Any]:
    pattern = rf'<div id="{re.escape(block_id)}"[^>]*>(.*?)</div>'
    match = re.search(pattern, html, flags=re.S | re.I)
    if not match:
        return {}
    raw = unescape(match.group(1).strip())
    try:
        return json.loads(raw)
    except json.JSONDecodeError:
        return {}


def extract_text(patterns: list[str], text: str) -> str:
    for pattern in patterns:
        match = re.search(pattern, text, flags=re.I | re.S)
        if match:
            return unescape(match.group(1)).strip()
    return ""


def clean_title(title: str) -> str:
    title = re.sub(r"【网上订票】.*$", "", title).strip()
    title = re.sub(r"-\s*大麦网$", "", title).strip()
    return title


def parse_datetime_text(value: str) -> str:
    value = value.strip()
    for fmt in ("%Y-%m-%d %H:%M", "%Y.%m.%d %H:%M", "%Y-%m-%d", "%Y.%m.%d"):
        try:
            dt = datetime.strptime(value, fmt)
            if "H" not in fmt:
                dt = dt.replace(hour=19, minute=30)
            return dt.strftime("%Y-%m-%d %H:%M:%S")
        except ValueError:
            pass
    return ""


def format_timestamp_ms(ts: Any) -> str:
    try:
        return datetime.fromtimestamp(int(ts) / 1000).strftime("%Y-%m-%d %H:%M:%S")
    except Exception:
        return ""


def parse_price_range_text(value: str) -> tuple[Optional[float], Optional[float]]:
    prices = [float(x) for x in re.findall(r"(\d+(?:\.\d+)?)", value or "")]
    if not prices:
        return None, None
    return min(prices), max(prices)


def extract_sku_prices(html: str) -> list[float]:
    prices = [float(x) for x in re.findall(r'<div class="skuname">\s*.*?(\d+(?:\.\d+)?)元', html, flags=re.S)]
    return sorted(set(prices))


def infer_end_time(start_time: str, duration_text: str) -> str:
    if not start_time:
        return ""
    try:
        base = datetime.strptime(start_time, "%Y-%m-%d %H:%M:%S")
    except ValueError:
        return ""

    minutes = 150
    nums = [int(x) for x in re.findall(r"(\d+)", duration_text or "")]
    if nums:
        if "小时" in duration_text and "分钟" in duration_text and len(nums) >= 2:
            minutes = nums[0] * 60 + nums[1]
        else:
            minutes = nums[0]
    return (base + timedelta(minutes=minutes)).strftime("%Y-%m-%d %H:%M:%S")


def normalize_status(data_default: dict[str, Any], html: str) -> str:
    buy_btn_text = str(data_default.get("buyBtnText") or "")
    buy_btn_status = data_default.get("buyBtnStatus")
    plain_text = strip_tags(html)

    if "该渠道不支持购买" in buy_btn_text or buy_btn_status == 100:
        return "UNSUPPORTED"
    if any(word in plain_text for word in ["无票", "售罄", "缺货"]):
        return "SOLD_OUT"
    return "ON_SALE"


def extract_cast_text(static_data: dict[str, Any]) -> str:
    notice_list = (((static_data.get("noticeMatter") or {}).get("noticeList")) or [])
    for block in notice_list:
        for note in block.get("ticketNoteList") or []:
            if note.get("title") == "主要演员":
                return str(note.get("content") or "")
    return ""


def extract_display_performances(html: str, duration_text: str, status: str) -> list[CrawledPerformance]:
    items = re.findall(
        r'<div class="select_right_list_item[^"]*">\s*<span>\s*(20\d{2}-\d{2}-\d{2}\s+星期.\s+\d{2}:\d{2})(.*?)</span>',
        html,
        flags=re.S,
    )
    result: list[CrawledPerformance] = []
    for label, extra in items:
        date_part = label.split(" 星期")[0]
        time_part = label.rsplit(" ", 1)[-1]
        start_time = parse_datetime_text(f"{date_part} {time_part}")
        tags = [strip_tags(x) for x in re.findall(r"<span class=\"presell\">\s*(.*?)\s*</span>", extra, flags=re.S)]
        result.append(
            CrawledPerformance(
                perform_id=None,
                perform_name=strip_tags(label),
                perform_date_text=strip_tags(label),
                start_time=start_time,
                end_time=infer_end_time(start_time, duration_text),
                min_price=None,
                max_price=None,
                status=status,
                tags=[x for x in tags if x],
            )
        )
    return result


def parse_page(source: str, html: str) -> CrawledPlayPage:
    data_default = extract_hidden_json(html, "dataDefault")
    static_data = extract_hidden_json(html, "staticDataDefault")
    item_base = static_data.get("itemBase") or {}
    venue = static_data.get("venue") or {}

    title = clean_title(
        str(item_base.get("itemName") or "")
        or extract_text([r"<title>(.*?)</title>"], html)
    )
    description = extract_text(
        [r'<meta name="description" content="(.*?)"', r'<meta property="og:description" content="(.*?)"'],
        html,
    )
    genre = str(item_base.get("guideCat") or "")
    city = str(item_base.get("cityName") or venue.get("venueCityName") or "")
    venue_name = str(venue.get("venueName") or "")
    venue_address = str(venue.get("venueAddr") or "")
    show_time_text = str(item_base.get("showTime") or "")
    duration_text = str(item_base.get("showDuration") or "")
    poster = (
        str(item_base.get("itemPic") or "")
        or ((item_base.get("itemPics") or {}).get("itemPicList") or [{}])[0].get("picUrl", "")
    )
    cast_text = extract_cast_text(static_data)
    status = normalize_status(data_default, html)
    price_range_text = str(data_default.get("priceRange") or "")
    min_price, max_price = parse_price_range_text(price_range_text)

    sku_prices = extract_sku_prices(html)
    if sku_prices:
        min_price = min_price if min_price is not None else sku_prices[0]
        max_price = max_price if max_price is not None else sku_prices[-1]

    performances = extract_display_performances(html, duration_text, status)
    if performances and sku_prices:
        for perf in performances:
            perf.min_price = sku_prices[0]
            perf.max_price = sku_prices[-1]

    start_time = performances[0].start_time if performances else ""
    end_time = performances[0].end_time if performances else ""

    if not start_time:
        first_date_text = extract_text([r'<div class="time">时间：([^<]+)</div>'], html)
        if first_date_text:
            left = first_date_text.split("-")[0]
            if re.fullmatch(r"\d{4}\.\d{2}\.\d{2}", left):
                start_time = parse_datetime_text(left)
                end_time = infer_end_time(start_time, duration_text)

    plain_text = strip_tags(html)
    sell_start_time = format_timestamp_ms(data_default.get("sellStartTime")) or str(data_default.get("sellStartTimeStr") or "")

    return CrawledPlayPage(
        source_url=source,
        source_type="url" if source.startswith(("http://", "https://")) else "file",
        item_id=item_base.get("itemId"),
        title=title,
        description=description,
        genre=genre,
        city=city,
        venue_name=venue_name,
        venue_address=venue_address,
        show_time_text=show_time_text,
        duration_text=duration_text,
        status=status,
        sell_start_time=sell_start_time,
        price_range_text=price_range_text,
        start_time=start_time,
        end_time=end_time,
        min_price=min_price,
        max_price=max_price,
        cast_text=cast_text,
        poster=poster,
        raw_snippet=plain_text[:500],
        performances=performances,
        debug_data_presence={
            "has_data_default": bool(data_default),
            "has_static_data_default": bool(static_data),
            "has_performances": bool(performances),
            "has_sku_prices": bool(sku_prices),
        },
    )


def load_sources(path: Path) -> list[str]:
    sources: list[str] = []
    for line in path.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if not line or line.startswith("#"):
            continue
        sources.append(line)
    return sources


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--input", required=True, help="包含 URL 或本地 HTML 路径的列表文件")
    parser.add_argument("--output", required=True, help="输出 JSON 文件")
    args = parser.parse_args()

    sources = load_sources(Path(args.input))
    results = []
    for source in sources:
        try:
            html = fetch_html(source)
            results.append(asdict(parse_page(source, html)))
            print(f"[ok] {source}")
        except Exception as exc:
            print(f"[fail] {source} -> {exc}", file=sys.stderr)

    Path(args.output).write_text(json.dumps(results, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"written: {args.output}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
