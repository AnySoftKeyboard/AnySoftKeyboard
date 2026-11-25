"""Tests for build_zhuyin_phrases.py (run: python3 -m unittest build_zhuyin_phrases_test)."""

from __future__ import annotations

import math
import os
import struct
import tempfile
import unittest
from typing import Dict, List, Tuple

import build_zhuyin_phrases as bz


def _write(path: str, text: str) -> None:
    with open(path, "w", encoding="utf-8") as f:
        f.write(text)


def _read_table(data: bytes) -> Dict[Tuple[int, ...], List[Tuple[int, str, int]]]:
    """Decodes a table the way PhraseTable.java reads it: key -> [(tones, text, score)], in file
    order (dicts keep insertion order, so the keys come out in file order too)."""
    keys, records, chars = struct.unpack(">III", data[4:16])
    pos = 16
    key_offsets = struct.unpack(f">{keys + 1}I", data[pos:pos + 4 * (keys + 1)])
    pos += 4 * (keys + 1)
    entry_starts = struct.unpack(f">{keys + 1}I", data[pos:pos + 4 * (keys + 1)])
    pos += 4 * (keys + 1)
    key_blob = struct.unpack(f">{key_offsets[-1]}H", data[pos:pos + 2 * key_offsets[-1]])
    pos += 2 * key_offsets[-1]
    entries_pos = pos
    pool = data[entries_pos + 10 * records:].decode("utf-16-be")
    assert len(pool) == chars
    table: Dict[Tuple[int, ...], List[Tuple[int, str, int]]] = {}
    for k in range(keys):
        bucket = []
        for e in range(entry_starts[k], entry_starts[k + 1]):
            tones, offset, length, score = struct.unpack_from(">IIBB", data, entries_pos + 10 * e)
            bucket.append((tones, pool[offset:offset + length], score))
        table[tuple(key_blob[key_offsets[k]:key_offsets[k + 1]])] = bucket
    return table


class SyllableTest(unittest.TestCase):

    def test_base_matches_java_encoding(self):
        # Mirrors SyllableTest.testBaseEncodingMatchesDataScript.
        self.assertEqual(bz.Syllable.parse("ㄋㄧˇ"), ((7 * 4 + 1) * 14, 3))
        self.assertEqual(bz.Syllable.parse("ㄏㄠˇ"), ((11 * 4) * 14 + 7, 3))

    def test_no_mark_is_first_tone(self):
        self.assertEqual(bz.Syllable.parse("ㄇㄚ")[1], 1)
        self.assertEqual(bz.Syllable.parse("ㄇㄚ˙")[1], 5)

    def test_rejects_invalid(self):
        self.assertIsNone(bz.Syllable.parse(""))
        self.assertIsNone(bz.Syllable.parse("ㄧㄋ"))
        self.assertIsNone(bz.Syllable.parse("x"))


class BuildTest(unittest.TestCase):

    def setUp(self):
        tmp = tempfile.TemporaryDirectory()
        self.addCleanup(tmp.cleanup)
        self.dir = tmp.name
        _write(os.path.join(self.dir, "BPMFBase.txt"),
               "你 ㄋㄧˇ ni3 su3 big5\n"
               "妳 ㄋㄧˇ ni3 su3 big5\n"
               "好 ㄏㄠˇ hao3 cl3 big5\n"
               "好 ㄏㄠˋ hao4 cl4 big5\n"
               "嗎 ㄇㄚ ma a8 big5\n"
               "嗎 ㄇㄚ˙ ma5 a87 big5\n"
               "ㄋ ㄋ n s big5\n"
               "𠀀 ㄑㄧㄡ qiu u9 big5\n"  # outside the BMP: dropped
               "㐀 ㄑㄧㄡ qiu u9 utf8\n")  # an extension char nothing uses: dropped
        _write(os.path.join(self.dir, "BPMFMappings.txt"),
               "你好 ㄋㄧˇ ㄏㄠˇ\n"
               "好嗎 ㄏㄠˇ ㄇㄚ˙\n"
               "好好好 ㄏㄠˇ ㄏㄠˇ\n"  # length mismatch: dropped
               "你好你好你好你 ㄋㄧˇ ㄏㄠˇ ㄋㄧˇ ㄏㄠˇ ㄋㄧˇ ㄏㄠˇ ㄋㄧˇ\n")  # 7 syllables: dropped
        _write(os.path.join(self.dir, "phrase.occ"),
               "你 100\n妳 10\n好 200\n嗎 50\n你好 80\n")
        for i in (1, 2, 3):
            _write(os.path.join(self.dir, f"heterophony{i}.list"), "")

    def test_entries(self):
        entries = bz.collect_entries(self.dir)
        ni = bz.Syllable.parse("ㄋㄧˇ")[0]
        hao = bz.Syllable.parse("ㄏㄠˇ")[0]
        ma = bz.Syllable.parse("ㄇㄚ")[0]
        texts = {text for (_, text) in entries[(ni,)]}
        self.assertEqual(texts, {"你", "妳"})
        self.assertIn((ni, hao), entries)
        self.assertIn((hao, ma), entries)
        # The particle override puts ㄇㄚ˙ first, so 嗎 ˙ outweighs 嗎 ㄇㄚ.
        weights = {tones: w for (tones, text), w in entries[(ma,)].items() if text == "嗎"}
        self.assertGreater(weights[5], weights[1])
        # Bare Bopomofo symbols, non-BMP chars, unused extension chars, length mismatches and
        # phrases over the syllable cap are dropped.
        all_texts = {text for bucket in entries.values() for (_, text) in bucket}
        self.assertNotIn("ㄋ", all_texts)
        self.assertNotIn("𠀀", all_texts)
        self.assertNotIn("㐀", all_texts)
        self.assertNotIn("好好好", all_texts)
        self.assertNotIn("你好你好你好你", all_texts)
        self.assertTrue(all(len(key) <= bz.MAX_PHRASE_SYLLABLES for key in entries))

    def test_secondary_reading_weighs_less(self):
        entries = bz.collect_entries(self.dir)
        hao = bz.Syllable.parse("ㄏㄠˇ")[0]
        weights = {tones: w for (tones, text), w in entries[(hao,)].items() if text == "好"}
        self.assertEqual(weights[3], 201)  # occurrences + 1
        self.assertEqual(weights[4], 201 * bz.SECONDARY_CHAR_READING_FACTOR)

    def test_heterophony_list_picks_the_primary_reading(self):
        _write(os.path.join(self.dir, "heterophony1.list"), "好 ㄏㄠˋ\n")
        entries = bz.collect_entries(self.dir)
        hao = bz.Syllable.parse("ㄏㄠˇ")[0]
        weights = {tones: w for (tones, text), w in entries[(hao,)].items() if text == "好"}
        self.assertGreater(weights[4], weights[3])

    def test_table_is_what_the_reader_expects(self):
        out = os.path.join(self.dir, "table")
        entries = bz.collect_entries(self.dir)
        total = 440.0
        bz.write_table(entries, total, out)
        with open(out, "rb") as f:
            table = _read_table(f.read())
        # Every entry is there, with its tones, text and score.
        self.assertEqual(set(table), set(entries))
        for key, bucket in entries.items():
            expected = {(tones, text, min(255, round(-math.log(weight / total) * bz.SCORE_SCALE)))
                        for (tones, text), weight in bucket.items()}
            self.assertEqual(set(table[key]), expected)
        # Keys are sorted by (length, bases): lookups binary-search them, and the phrases a
        # half-typed syllable can become are one run of keys.
        keys = list(table)
        self.assertEqual(keys, sorted(keys, key=lambda k: (len(k), k)))
        # Entries of a key are best first (the smaller score is the likelier phrase).
        for bucket in table.values():
            scores = [score for (_, _, score) in bucket]
            self.assertEqual(scores, sorted(scores))
        # Tones are 3 bits per syllable, the first syllable in the lowest bits.
        ni, hao, ma = (bz.Syllable.parse(s)[0] for s in ("ㄋㄧˇ", "ㄏㄠˇ", "ㄇㄚ"))
        self.assertEqual([(t, text) for (t, text, _) in table[(ni, hao)]], [(3 | 3 << 3, "你好")])
        self.assertEqual([(t, text) for (t, text, _) in table[(hao, ma)]], [(3 | 5 << 3, "好嗎")])

    def test_text_is_stored_once(self):
        out = os.path.join(self.dir, "table")
        entries = bz.collect_entries(self.dir)
        _, _, chars = bz.write_table(entries, 440.0, out)
        texts = {text for bucket in entries.values() for (_, text) in bucket}
        # 好 and 嗎 have two readings each, but one copy of their text.
        self.assertEqual(chars, sum(len(text) for text in texts))

    def test_written_header_is_consistent(self):
        out = os.path.join(self.dir, "table")
        entries = bz.collect_entries(self.dir)
        keys, records, chars = bz.write_table(entries, 440.0, out)
        with open(out, "rb") as f:
            data = f.read()
        self.assertEqual(data[:4], bz.MAGIC)
        self.assertEqual(struct.unpack(">III", data[4:16]), (keys, records, chars))
        key_offsets = struct.unpack(f">{keys + 1}I", data[16:16 + 4 * (keys + 1)])
        size = 16 + 8 * (keys + 1) + 2 * key_offsets[-1] + 10 * records + 2 * chars
        self.assertEqual(len(data), size)

    def test_sentence_to_reading(self):
        entries = bz.collect_entries(self.dir)
        words = bz.best_readings(entries, self.dir)
        self.assertEqual(bz.sentence_to_reading("你好嗎", words, 440.0), "ㄋㄧˇ ㄏㄠˇ ㄇㄚ˙")
        self.assertIsNone(bz.sentence_to_reading("你x", words, 440.0))


if __name__ == "__main__":
    unittest.main()
