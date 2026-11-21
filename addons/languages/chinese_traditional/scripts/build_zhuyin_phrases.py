#!/usr/bin/env python3
"""Builds the binary Zhuyin phrase table used by the phonetic engine (ime/phonetic).

Inputs are McBopomofo's data files (MIT, see NOTICE.md), downloaded into a scratch folder. The
bundled table was built from the commit below; with these inputs the output is byte-identical:

  rev=745e8641a95d958532f0820c3ee679c2a585d8e3
  for f in BPMFMappings.txt BPMFBase.txt phrase.occ \
           heterophony1.list heterophony2.list heterophony3.list; do
    curl -sfLO https://raw.githubusercontent.com/openvanilla/McBopomofo/$rev/Source/Data/$f
  done

Usage:
  build_zhuyin_phrases.py <mcbopomofo-data-dir> <output-file> \
      [--eval-sentences <txt> --eval-out <tsv>]

The output format is documented in ime/phonetic/.../PhraseTable.java and mirrored by
`write_table` below. Syllable codes MUST match ime/phonetic/.../Syllable.java.
"""

from __future__ import annotations

import argparse
import math
import os
import struct
import sys
from typing import Dict, Iterable, List, Optional, Tuple

INITIALS = "ㄅㄆㄇㄈㄉㄊㄋㄌㄍㄎㄏㄐㄑㄒㄓㄔㄕㄖㄗㄘㄙ"
MEDIALS = "ㄧㄨㄩ"
FINALS = "ㄚㄛㄜㄝㄞㄟㄠㄡㄢㄣㄤㄥㄦ"
TONE_MARKS = {"ˊ": 2, "ˇ": 3, "ˋ": 4, "˙": 5}

MAGIC = b"ZYP1"
MAX_PHRASE_SYLLABLES = 6
SCORE_SCALE = 8.0  # stored byte = round(-ln(p) * SCORE_SCALE)
SECONDARY_PHRASE_READING_FACTOR = 0.3
SECONDARY_CHAR_READING_FACTOR = 0.05

# Textbook readings for particles; McBopomofo's heterophony lists are IME-oriented.
PRIMARY_READING_OVERRIDES = {
    "嗎": "ㄇㄚ˙", "呢": "ㄋㄜ˙", "吧": "ㄅㄚ˙", "啊": "ㄚ˙", "麼": "ㄇㄜ˙",
    "嘛": "ㄇㄚ˙", "喔": "ㄛ˙", "了": "ㄌㄜ˙", "的": "ㄉㄜ˙", "著": "ㄓㄜ˙",
}


class Syllable:
    """Encodes a Bopomofo syllable string to (base, tone). Mirrors Syllable.java."""

    @staticmethod
    def parse(text: str) -> Optional[Tuple[int, int]]:
        initial = medial = final = 0
        tone = 1
        for ch in text:
            if ch in TONE_MARKS:
                tone = TONE_MARKS[ch]
            elif ch in INITIALS and initial == 0 and medial == 0 and final == 0:
                initial = INITIALS.index(ch) + 1
            elif ch in MEDIALS and medial == 0 and final == 0:
                medial = MEDIALS.index(ch) + 1
            elif ch in FINALS and final == 0:
                final = FINALS.index(ch) + 1
            else:
                return None
        if initial == 0 and medial == 0 and final == 0:
            return None
        return (initial * 4 + medial) * 14 + final, tone


def _is_bmp_text(text: str) -> bool:
    """BMP only (one UTF-16 unit per syllable). Bare Bopomofo symbols (ㄋ read as ㄋ) are
    dropped: they would make a lone initial look like a complete syllable, and the engine
    already falls back to Bopomofo text for unknown syllables."""
    return all(ord(c) <= 0xFFFF and not c.isspace() and not c.isascii()
               and not 0x3100 <= ord(c) <= 0x312F for c in text)


def load_occurrences(path: str) -> Dict[str, int]:
    occ: Dict[str, int] = {}
    with open(path, encoding="utf-8") as f:
        for line in f:
            parts = line.split()
            if len(parts) == 2 and parts[1].isdigit():
                occ[parts[0]] = int(parts[1])
    return occ


def load_char_readings(base_path: str, heterophony_paths: Iterable[str],
                       occ: Dict[str, int]) -> Dict[str, List[str]]:
    """char -> readings, primary first (BPMFBase order, then heterophony lists, then overrides)."""
    readings: Dict[str, List[str]] = {}
    with open(base_path, encoding="utf-8") as f:
        for line in f:
            parts = line.split()
            if len(parts) < 2 or len(parts[0]) != 1:
                continue
            ch, reading = parts[0], parts[1]
            # utf8-only rows are rare extension chars: keep them only if they are actually used.
            if parts[-1] != "big5" and occ.get(ch, 0) == 0:
                continue
            lst = readings.setdefault(ch, [])
            if reading not in lst:
                lst.append(reading)
    # heterophony1 is applied last so it ends up in front.
    for path in heterophony_paths:
        with open(path, encoding="utf-8") as f:
            for line in f:
                parts = line.split()
                if len(parts) == 2 and parts[0] in readings and parts[1] in readings[parts[0]]:
                    readings[parts[0]].remove(parts[1])
                    readings[parts[0]].insert(0, parts[1])
    for ch, reading in PRIMARY_READING_OVERRIDES.items():
        if ch in readings and reading in readings[ch]:
            readings[ch].remove(reading)
            readings[ch].insert(0, reading)
    return readings


def collect_entries(data_dir: str) -> Dict[Tuple[int, ...], Dict[Tuple[int, str], float]]:
    """Returns key(bases) -> {(packed tones, text): weight}."""
    occ = load_occurrences(os.path.join(data_dir, "phrase.occ"))
    char_readings = load_char_readings(
        os.path.join(data_dir, "BPMFBase.txt"),
        [os.path.join(data_dir, f"heterophony{i}.list") for i in (3, 2, 1)],
        occ)

    entries: Dict[Tuple[int, ...], Dict[Tuple[int, str], float]] = {}

    def add(text: str, reading: List[str], weight: float) -> None:
        if len(reading) != len(text) or len(reading) > MAX_PHRASE_SYLLABLES:
            return
        if not _is_bmp_text(text):
            return
        parsed = [Syllable.parse(s) for s in reading]
        if any(p is None for p in parsed):
            return
        key = tuple(p[0] for p in parsed)
        tones = 0
        for i, p in enumerate(parsed):
            tones |= p[1] << (3 * i)
        bucket = entries.setdefault(key, {})
        bucket[(tones, text)] = max(bucket.get((tones, text), 0.0), weight)

    for ch, readings in char_readings.items():
        base = occ.get(ch, 0) + 1
        for i, reading in enumerate(readings):
            add(ch, [reading], base if i == 0 else base * SECONDARY_CHAR_READING_FACTOR)

    seen_phrases: Dict[str, int] = {}
    with open(os.path.join(data_dir, "BPMFMappings.txt"), encoding="utf-8") as f:
        for line in f:
            parts = line.split()
            if len(parts) < 3:
                continue
            text, reading = parts[0], parts[1:]
            nth = seen_phrases.get(text, 0)
            seen_phrases[text] = nth + 1
            base = occ.get(text, 0) + 1
            add(text, reading, base if nth == 0 else base * SECONDARY_PHRASE_READING_FACTOR)
    return entries


def write_table(entries: Dict[Tuple[int, ...], Dict[Tuple[int, str], float]], total: float,
                out_path: str) -> Tuple[int, int, int]:
    keys = sorted(entries.keys(), key=lambda k: (len(k), k))
    key_blob: List[int] = []
    key_offsets: List[int] = [0]
    entry_starts: List[int] = [0]
    entry_records: List[bytes] = []
    text_pool: List[str] = []
    text_index: Dict[str, int] = {}
    pool_len = 0
    for key in keys:
        key_blob.extend(key)
        key_offsets.append(len(key_blob))
        bucket = sorted(entries[key].items(), key=lambda kv: (-kv[1], kv[0][1]))
        for (tones, text), weight in bucket:
            offset = text_index.get(text)
            if offset is None:
                offset = pool_len
                text_index[text] = offset
                text_pool.append(text)
                pool_len += len(text)
            score = min(255, max(0, round(-math.log(weight / total) * SCORE_SCALE)))
            entry_records.append(struct.pack(">IIBB", tones, offset, len(text), score))
        entry_starts.append(len(entry_records))

    with open(out_path, "wb") as out:
        out.write(MAGIC)
        out.write(struct.pack(">III", len(keys), len(entry_records), pool_len))
        out.write(struct.pack(f">{len(key_offsets)}I", *key_offsets))
        out.write(struct.pack(f">{len(entry_starts)}I", *entry_starts))
        out.write(struct.pack(f">{len(key_blob)}H", *key_blob))
        for rec in entry_records:
            out.write(rec)
        out.write("".join(text_pool).encode("utf-16-be"))
    return len(keys), len(entry_records), pool_len


def best_readings(entries: Dict[Tuple[int, ...], Dict[Tuple[int, str], float]],
                  data_dir: str) -> Dict[str, Tuple[str, float]]:
    """text -> (reading string, weight) of its most weighted reading, for eval generation."""
    del entries
    occ = load_occurrences(os.path.join(data_dir, "phrase.occ"))
    char_readings = load_char_readings(
        os.path.join(data_dir, "BPMFBase.txt"),
        [os.path.join(data_dir, f"heterophony{i}.list") for i in (3, 2, 1)],
        occ)
    out: Dict[str, Tuple[str, float]] = {}
    for ch, readings in char_readings.items():
        out[ch] = (readings[0], occ.get(ch, 0) + 1.0)
    with open(os.path.join(data_dir, "BPMFMappings.txt"), encoding="utf-8") as f:
        for line in f:
            parts = line.split()
            if len(parts) >= 3 and parts[0] not in out and len(parts) - 1 == len(parts[0]):
                out[parts[0]] = (" ".join(parts[1:]), occ.get(parts[0], 0) + 1.0)
    return out


def sentence_to_reading(sentence: str, words: Dict[str, Tuple[str, float]],
                        total: float) -> Optional[str]:
    """Unigram max-likelihood segmentation (text -> reading), as in yt-zhuyin's segmenter."""
    n = len(sentence)
    best: List[Tuple[float, int, str]] = [(0.0, -1, "")] + [(-math.inf, -1, "")] * n
    for end in range(1, n + 1):
        for length in range(1, min(MAX_PHRASE_SYLLABLES, end) + 1):
            word = sentence[end - length:end]
            hit = words.get(word)
            if hit is None or best[end - length][0] == -math.inf:
                continue
            score = best[end - length][0] + math.log(hit[1] / total)
            if score > best[end][0]:
                best[end] = (score, end - length, hit[0])
    if best[n][0] == -math.inf:
        return None
    parts: List[str] = []
    pos = n
    while pos > 0:
        _, prev, reading = best[pos]
        parts.append(reading)
        pos = prev
    return " ".join(reversed(parts))


def write_eval(sentences_path: str, out_path: str, data_dir: str,
               entries: Dict[Tuple[int, ...], Dict[Tuple[int, str], float]],
               total: float) -> int:
    words = best_readings(entries, data_dir)
    count = 0
    with open(sentences_path, encoding="utf-8") as f, \
            open(out_path, "w", encoding="utf-8") as out:
        out.write("# reading<TAB>expected text. Generated by build_zhuyin_phrases.py\n")
        for line in f:
            sentence = line.strip()
            if not sentence or sentence.startswith("#"):
                continue
            reading = sentence_to_reading(sentence, words, total)
            if reading is None:
                print(f"skipping (no reading): {sentence}", file=sys.stderr)
                continue
            out.write(f"{reading}\t{sentence}\n")
            count += 1
    return count


def main(argv: List[str]) -> int:
    parser = argparse.ArgumentParser(description=__doc__,
                                     formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("data_dir")
    parser.add_argument("output")
    parser.add_argument("--eval-sentences")
    parser.add_argument("--eval-out")
    args = parser.parse_args(argv)

    total = float(sum(load_occurrences(os.path.join(args.data_dir, "phrase.occ")).values()))
    entries = collect_entries(args.data_dir)
    keys, records, pool = write_table(entries, total, args.output)
    print(f"keys={keys} entries={records} text_chars={pool} "
          f"bytes={os.path.getsize(args.output)}")
    if args.eval_sentences and args.eval_out:
        n = write_eval(args.eval_sentences, args.eval_out, args.data_dir, entries, total)
        print(f"eval sentences={n}")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
