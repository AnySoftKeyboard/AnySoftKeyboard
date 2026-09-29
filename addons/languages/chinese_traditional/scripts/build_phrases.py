"""Build the bundled next-word phrase table from McBopomofo's phrase data.

Source
======
`phrase.occ` from the McBopomofo project (小麥注音), an MIT-licensed
Traditional Chinese input method:

    https://github.com/openvanilla/McBopomofo/blob/master/Source/Data/phrase.occ

The file is a plain ``<phrase> <occurrence-count>`` list. McBopomofo's own
data README notes its phrase material derives from ``tsi.src`` of libtabe
(BSD licensed) with modifications, so the whole chain is permissive and
Apache-2.0 compatible.

Output
======
``pack/src/main/res/raw/chinese_phrases`` -- ``<trigger><TAB><next-chars>``
rows, where the trigger is the first character of a phrase and the value is
the remainder. The runtime looks the trigger up after a character is
committed and offers the remainders as next-word suggestions, so rows are
emitted most-frequent-first and capped per trigger to keep the table small.

Only phrases whose characters all appear in ``chinese_chars_trad`` are kept,
matching the rest of the pack's Traditional-only policy.

Run::

    python build_phrases.py path/to/phrase.occ
"""

from __future__ import annotations

import argparse
import os
import sys
from typing import Dict, List, Tuple

ROOT = os.path.dirname(os.path.abspath(__file__))
RAW_DIR = os.path.join(ROOT, "..", "pack", "src", "main", "res", "raw")
TRAD_CHARS = os.path.join(RAW_DIR, "chinese_chars_trad")
OUT = os.path.join(RAW_DIR, "chinese_phrases")

# Suggestions past the first handful are never seen in the candidate strip,
# and keeping every phrase would add ~140k lines for no user benefit.
MAX_PER_TRIGGER = 8
# Long phrases are rarely useful as a single next-word suggestion.
MAX_PHRASE_LEN = 4


def load_trad_chars(path: str) -> set:
    with open(path, encoding="utf-8") as fh:
        return {c for c in fh.read() if c.strip()}


def read_occurrences(path: str) -> List[Tuple[str, int]]:
    rows: List[Tuple[str, int]] = []
    with open(path, encoding="utf-8") as fh:
        for line in fh:
            parts = line.split()
            if len(parts) != 2 or not parts[1].isdigit():
                continue
            rows.append((parts[0], int(parts[1])))
    return rows


def build(
    rows: List[Tuple[str, int]], trad: set
) -> Dict[str, List[str]]:
    # Most frequent first; ties fall back to the phrase itself so the output
    # is deterministic regardless of input ordering.
    rows = sorted(rows, key=lambda t: (-t[1], t[0]))
    out: Dict[str, List[str]] = {}
    for phrase, _count in rows:
        if not (2 <= len(phrase) <= MAX_PHRASE_LEN):
            continue
        if not all(ch in trad for ch in phrase):
            continue
        trigger, rest = phrase[0], phrase[1:]
        bucket = out.setdefault(trigger, [])
        if len(bucket) >= MAX_PER_TRIGGER or rest in bucket:
            continue
        bucket.append(rest)
    return out


def write(path: str, table: Dict[str, List[str]]) -> int:
    tmp = path + ".tmp"
    written = 0
    try:
        with open(tmp, "w", encoding="utf-8", newline="\n") as fh:
            for trigger in sorted(table):
                for rest in table[trigger]:
                    fh.write(f"{trigger}\t{rest}\n")
                    written += 1
        os.replace(tmp, path)
    except OSError:
        if os.path.exists(tmp):
            os.remove(tmp)
        raise
    return written


def main(argv: List[str]) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("source", help="McBopomofo phrase.occ")
    parser.add_argument("-o", "--output", default=OUT)
    args = parser.parse_args(argv[1:])

    if not os.path.exists(TRAD_CHARS):
        print(
            f"error: {TRAD_CHARS} not found; run boshiamy_variants.py first",
            file=sys.stderr,
        )
        return 1
    trad = load_trad_chars(TRAD_CHARS)
    rows = read_occurrences(args.source)
    if not rows:
        print("error: no '<phrase> <count>' rows found", file=sys.stderr)
        return 1
    table = build(rows, trad)
    written = write(args.output, table)
    print(
        f"read {len(rows)} entries -> {len(table)} triggers, "
        f"wrote {written} rows to {args.output}"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv))
