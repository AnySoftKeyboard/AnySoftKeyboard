"""Tests for build_zhuyin_phrases.py (run: python3 -m unittest build_zhuyin_phrases_test)."""

from __future__ import annotations

import os
import struct
import tempfile
import unittest

import build_zhuyin_phrases as bz


def _write(path: str, text: str) -> None:
    with open(path, "w", encoding="utf-8") as f:
        f.write(text)


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
        self.dir = tempfile.mkdtemp()
        _write(os.path.join(self.dir, "BPMFBase.txt"),
               "你 ㄋㄧˇ ni3 su3 big5\n"
               "妳 ㄋㄧˇ ni3 su3 big5\n"
               "好 ㄏㄠˇ hao3 cl3 big5\n"
               "好 ㄏㄠˋ hao4 cl4 big5\n"
               "嗎 ㄇㄚ ma a8 big5\n"
               "嗎 ㄇㄚ˙ ma5 a87 big5\n"
               "ㄋ ㄋ n s big5\n"
               "𠀀 ㄑㄧㄡ qiu u9 utf8\n")
        _write(os.path.join(self.dir, "BPMFMappings.txt"),
               "你好 ㄋㄧˇ ㄏㄠˇ\n"
               "好嗎 ㄏㄠˇ ㄇㄚ˙\n"
               "好好好 ㄏㄠˇ ㄏㄠˇ\n")  # length mismatch: dropped
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
        # Bare Bopomofo symbols, non-BMP chars and length mismatches are dropped.
        all_texts = {text for bucket in entries.values() for (_, text) in bucket}
        self.assertNotIn("ㄋ", all_texts)
        self.assertNotIn("𠀀", all_texts)
        self.assertNotIn("好好好", all_texts)

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
