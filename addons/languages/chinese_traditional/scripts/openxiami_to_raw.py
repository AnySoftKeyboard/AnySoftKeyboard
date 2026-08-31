"""Convert the Open Xiami (開放蝦米) Google-Sheet export into the raw
Boshiamy-compatible radical table consumed by ``boshiamy_variants.py``.

Source
======
Open Xiami is a community project that re-created a Boshiamy-compatible
radical table from scratch on Google Sheets, specifically to avoid the
copyright problems of the tables that circulate online (the vendor still
holds copyright on the table shipped inside its commercial product).
Its 版權宣告 (copyright declaration) sheet states that contributors
license their encodings unconditionally to anyone, for any purpose,
commercial or not.

    https://docs.google.com/spreadsheets/d/1_j_O7aS6mew96gYwG58f82i3LCsbc0MmBAX-vxZmbE4/

Input format
============
The sheet is exported as TSV/CSV and holds a RIME-style dictionary: a
short YAML preamble terminated by a line containing only ``...``, then
one ``<character>\\t<code>\\t<weight>`` row per entry. Higher weight sorts
first (``sort:by_weight``); blank weights are treated as 0.

Output
======
``boshiamy_radicals_raw.tab`` -- ``<code>\\t<value>`` rows, grouped by code
and ordered so the highest-weighted character of each code comes first.
This is the same shape the downstream build expects, so
``boshiamy_variants.py`` needs no changes.

Run::

    python openxiami_to_raw.py <exported.tsv> [-o boshiamy_radicals_raw.tab]
"""

from __future__ import annotations

import argparse
import csv
import os
import sys
from typing import Dict, List, Tuple

ROOT = os.path.dirname(os.path.abspath(__file__))
DEFAULT_OUT = os.path.join(ROOT, "openxiami_radicals_raw.tab")
FREQUENCY = os.path.join(
    ROOT, "..", "pack", "src", "main", "res", "raw", "chinese_char_frequency"
)

# Rows before this marker are the YAML preamble (name/version/sort/...).
PREAMBLE_END = "..."


def load_frequency_rank(path: str) -> Dict[str, int]:
    """Load ``character -> rank`` (0 = most common) from the shipped table.

    Used only to break ties between entries the sheet gives equal weight;
    characters missing from the table sort last.
    """
    try:
        with open(path, encoding="utf-8") as fh:
            chars = [c for c in fh.read() if c.strip()]
    except OSError:
        return {}
    return {c: i for i, c in enumerate(chars)}


def _sniff_delimiter(sample: str) -> str:
    # The sheet can be exported as either CSV or TSV; pick whichever
    # separator actually appears on a data-looking line.
    return "\t" if sample.count("\t") >= sample.count(",") else ","


def read_openxiami(path: str) -> List[Tuple[str, str, float]]:
    """Return ``[(code, character, weight), ...]`` in sheet order."""
    with open(path, "r", encoding="utf-8-sig", newline="") as fh:
        sample = fh.read(8192)
        fh.seek(0)
        reader = csv.reader(fh, delimiter=_sniff_delimiter(sample))
        rows: List[Tuple[str, str, float]] = []
        started = False
        for raw in reader:
            if not raw:
                continue
            first = (raw[0] or "").strip()
            if not started:
                # Everything up to and including the "..." line is header.
                if first == PREAMBLE_END:
                    started = True
                continue
            if first.startswith("#") or len(raw) < 2:
                continue
            char = first
            code = (raw[1] or "").strip()
            if not char or not code:
                continue
            weight_cell = (raw[2] or "").strip() if len(raw) > 2 else ""
            try:
                weight = float(weight_cell) if weight_cell else 0.0
            except ValueError:
                weight = 0.0
            rows.append((code, char, weight))
    return rows


def group_by_code(
    rows: List[Tuple[str, str, float]],
    frequency: Dict[str, int] = None,
) -> Dict[str, List[str]]:
    """Group into ``code -> [chars]`` ordered by descending weight.

    The sheet only assigns a weight where contributors deliberately ranked an
    entry, so most candidates for a code tie at 0. Sheet order cannot break
    those ties usefully because the sheet is ordered by Unicode codepoint: for
    ``pri`` that would put the rare 佽 (U+4F7D) ahead of the very common 到
    (U+5230). Ties are therefore broken by character frequency, falling back to
    sheet order for characters the frequency table does not rank.
    """
    frequency = frequency or {}
    unranked = len(frequency)
    buckets: Dict[str, List[Tuple[float, int, int, str]]] = {}
    for index, (code, char, weight) in enumerate(rows):
        rank = min(
            (frequency.get(c, unranked) for c in char), default=unranked
        )
        buckets.setdefault(code, []).append((weight, rank, index, char))
    out: Dict[str, List[str]] = {}
    for code, entries in buckets.items():
        # Explicit weight first, then commonness, then sheet order.
        entries.sort(key=lambda t: (-t[0], t[1], t[2]))
        seen = set()
        ordered = []
        for _, _, _, char in entries:
            if char not in seen:
                seen.add(char)
                ordered.append(char)
        out[code] = ordered
    return out


def write_raw(path: str, table: Dict[str, List[str]]) -> int:
    tmp = path + ".tmp"
    count = 0
    try:
        with open(tmp, "w", encoding="utf-8", newline="\n") as fh:
            # Sorted by code for a deterministic, diff-friendly file.
            for code in sorted(table):
                for char in table[code]:
                    fh.write(f"{code}\t{char}\n")
                    count += 1
        os.replace(tmp, path)
    except OSError:
        if os.path.exists(tmp):
            os.remove(tmp)
        raise
    return count


def main(argv: List[str]) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("source", help="Open Xiami sheet exported as TSV/CSV")
    parser.add_argument("-o", "--output", default=DEFAULT_OUT)
    args = parser.parse_args(argv[1:])

    rows = read_openxiami(args.source)
    if not rows:
        print(
            "error: no data rows found -- is this the 輸入法碼表 sheet "
            "(it must contain the '...' preamble terminator)?",
            file=sys.stderr,
        )
        return 1
    table = group_by_code(rows, load_frequency_rank(FREQUENCY))
    written = write_raw(args.output, table)
    chars = len({c for v in table.values() for c in v})
    print(
        f"read {len(rows)} rows -> {len(table)} codes, {chars} unique "
        f"characters; wrote {written} lines to {args.output}"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv))
