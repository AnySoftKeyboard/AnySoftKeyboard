"""Tests for the include-based Boshiamy variant generator."""

from __future__ import annotations

import os
import tempfile
import unittest
from typing import Set

import boshiamy_variants as bv


# Sample codepoints (verified against Unihan_IRGSources.txt).
CP_CHUANG = 0x5E8A  # 床 -- trad+simp+jp
CP_CHUANG_FORMAL = 0x7240  # 牀 -- trad (via H) only in our scheme
CP_DUI_TRAD = 0x5C0D  # 對
CP_DUI_SIMP = 0x5BF9  # 对
CP_DUI_JP = 0x5BFE   # 対
CP_QIN_TRAD = 0x89AA  # 親
CP_QIN_SIMP = 0x4EB2  # 亲
CP_DUO_SIMP = 0x593A  # 夺
CP_NEUTRAL = 0x55BF   # 喿 (no controversial classification)
CP_HIRAGANA_A = 0x3042  # あ
CP_KATAKANA_A = 0x30A2  # ア


def _set(*cps: int) -> Set[int]:
    return set(cps)


class IsCjkTest(unittest.TestCase):
    def test_bmp_unified_block(self) -> None:
        self.assertTrue(bv._is_cjk_cp(0x4E00))
        self.assertTrue(bv._is_cjk_cp(0x9FFF))

    def test_extension_a(self) -> None:
        self.assertTrue(bv._is_cjk_cp(0x3400))

    def test_kana_not_cjk(self) -> None:
        self.assertFalse(bv._is_cjk_cp(CP_HIRAGANA_A))
        self.assertFalse(bv._is_cjk_cp(CP_KATAKANA_A))

    def test_ascii_not_cjk(self) -> None:
        self.assertFalse(bv._is_cjk_cp(ord("a")))
        self.assertFalse(bv._is_cjk_cp(ord("(")))


class IsKanaTest(unittest.TestCase):
    def test_hiragana(self) -> None:
        self.assertTrue(bv._is_kana_cp(CP_HIRAGANA_A))

    def test_katakana(self) -> None:
        self.assertTrue(bv._is_kana_cp(CP_KATAKANA_A))

    def test_cjk_not_kana(self) -> None:
        self.assertFalse(bv._is_kana_cp(CP_CHUANG))


class KanaOnlyCodeTest(unittest.TestCase):
    def test_comma_prefix_is_kana(self) -> None:
        self.assertTrue(bv._kana_only_code(",foo"))

    def test_period_prefix_is_kana(self) -> None:
        self.assertTrue(bv._kana_only_code(".foo"))

    def test_alpha_prefix_is_regular(self) -> None:
        self.assertFalse(bv._kana_only_code("a"))
        self.assertFalse(bv._kana_only_code("ltn"))
        # `a,` -- code is "a," but starts with `a`, NOT a kana prefix
        # row.  (It would still be classified by value: `a,\tあ` has a
        # kana value so trad/simp reject via the value predicate.)
        self.assertFalse(bv._kana_only_code("a,"))

    def test_empty_code_is_not_kana(self) -> None:
        self.assertFalse(bv._kana_only_code(""))


class IncludeForTradTest(unittest.TestCase):
    def setUp(self) -> None:
        # Set contains 床 對 親 喿 -- the chars we expect on trad.
        self.trad = _set(CP_CHUANG, CP_DUI_TRAD, CP_QIN_TRAD, CP_NEUTRAL)

    def test_床_is_included(self) -> None:
        self.assertTrue(bv.include_for_trad("ltn", "床", self.trad))

    def test_對_is_included(self) -> None:
        self.assertTrue(bv.include_for_trad("a", "對", self.trad))

    def test_对_simp_is_rejected(self) -> None:
        self.assertFalse(bv.include_for_trad("a", "对", self.trad))

    def test_対_jp_is_rejected(self) -> None:
        self.assertFalse(bv.include_for_trad("a", "対", self.trad))

    def test_hiragana_value_rejected(self) -> None:
        # Even if code is "a", value あ contains kana -> reject on trad.
        self.assertFalse(bv.include_for_trad("a,", "あ", self.trad))

    def test_kana_value_with_kana_code_rejected(self) -> None:
        # Comma/period codes are the JP-input convention; on trad they are
        # rejected ONLY if the value is itself kana (`,foo` -> `あ` here).
        self.assertFalse(bv.include_for_trad(",foo", "あ", self.trad))

    def test_symbol_value_with_kana_code_accepted(self) -> None:
        # Pure-symbol values (fullwidth comma/period etc.) under a
        # comma/period code are script-neutral and must survive on trad
        # so `,` -> `，` and `.` -> `。` are usable on TW boshiamy.
        self.assertTrue(bv.include_for_trad(",", "，", self.trad))
        self.assertTrue(bv.include_for_trad(".", "。", self.trad))
        self.assertTrue(bv.include_for_trad(",'", "‘", self.trad))

    def test_neutral_phrase_passes(self) -> None:
        self.assertTrue(bv.include_for_trad("liu", "嘸蝦米", _set(0x5638, 0x8766, 0x7C73)))

    def test_kaomoji_passes(self) -> None:
        # All non-CJK, non-kana characters -> always passes
        self.assertTrue(bv.include_for_trad("b.", "(´▽`)", self.trad))

    def test_emoji_passes(self) -> None:
        self.assertTrue(bv.include_for_trad("e.", "😂", self.trad))

    def test_partial_phrase_rejected(self) -> None:
        # Phrase mixing trad + simp -> reject (we want pure script).
        self.assertFalse(bv.include_for_trad("xx", "對对", self.trad))


class IncludeForSimpTest(unittest.TestCase):
    def setUp(self) -> None:
        self.simp = _set(CP_CHUANG, CP_DUI_SIMP, CP_QIN_SIMP, CP_DUO_SIMP, CP_NEUTRAL)

    def test_对_included(self) -> None:
        self.assertTrue(bv.include_for_simp("a", "对", self.simp))

    def test_對_rejected(self) -> None:
        self.assertFalse(bv.include_for_simp("a", "對", self.simp))

    def test_床_universal_included(self) -> None:
        self.assertTrue(bv.include_for_simp("ltn", "床", self.simp))

    def test_kana_code_rejected(self) -> None:
        self.assertFalse(bv.include_for_simp(",foo", "あ", self.simp))

    def test_symbol_value_with_kana_code_accepted_on_simp(self) -> None:
        self.assertTrue(bv.include_for_simp(",", "，", self.simp))
        self.assertTrue(bv.include_for_simp(".", "。", self.simp))


class BuildSimpRowsTest(unittest.TestCase):
    """The simplified table must reorder to authentic 台式簡體 priority.

    The trad-ordered source lists the trad primary first (e.g. iwn -> 這);
    after trad->simp conversion + de-dup, the simplified glyph of that primary
    must take the primary slot (iwn -> 这 first, not the secondary 汞).
    """

    def setUp(self) -> None:
        # 這=0x9019 這-only(trad), 这=0x8FD9 simp, 汞=0x6C5E both, 逋=0x900B both.
        self.simp = _set(0x8FD9, 0x6C5E, 0x900B)  # 这 汞 逋 (not 這)
        self.t2s = {"這": "这"}  # 汞/逋/这 unchanged

    def test_simplified_primary_takes_first_slot(self) -> None:
        # Source order mirrors the trad table: 這(dropped-as-trad), 汞, 逋, 这.
        raw = [
            ("iwn", "這"),
            ("iwn", "汞"),
            ("iwn", "逋"),
            ("iwn", "这"),
        ]
        rows = bv.build_simp_rows(raw, self.simp, self.t2s)
        self.assertEqual(rows, [("iwn", "这"), ("iwn", "汞"), ("iwn", "逋")])

    def test_dedup_keeps_first_occurrence(self) -> None:
        # 這->这 converts to the same glyph as a later 这 row; keep one, first.
        raw = [("iwn", "這"), ("iwn", "这")]
        rows = bv.build_simp_rows(raw, self.simp, self.t2s)
        self.assertEqual(rows, [("iwn", "这")])

    def test_original_kept_when_conversion_not_valid_simp(self) -> None:
        # A char with no t2s entry that is itself valid simp survives as-is.
        raw = [("a", "汞")]
        rows = bv.build_simp_rows(raw, self.simp, self.t2s)
        self.assertEqual(rows, [("a", "汞")])

    def test_row_dropped_when_neither_form_is_simp(self) -> None:
        # 對 (trad-only, no t2s entry here) is not in simp_set -> dropped.
        raw = [("a", "對")]
        rows = bv.build_simp_rows(raw, self.simp, self.t2s)
        self.assertEqual(rows, [])


class IncludeForJpTest(unittest.TestCase):
    def setUp(self) -> None:
        self.jp = _set(CP_CHUANG, CP_DUI_TRAD, CP_DUI_JP)

    def test_対_shinjitai_included(self) -> None:
        self.assertTrue(bv.include_for_jp("a", "対", self.jp))

    def test_對_kyujitai_included(self) -> None:
        # JP keyboard accepts kyujitai too (both in jp_set per IRG).
        self.assertTrue(bv.include_for_jp("a", "對", self.jp))

    def test_对_simp_only_rejected(self) -> None:
        self.assertFalse(bv.include_for_jp("a", "对", self.jp))

    def test_hiragana_value_accepted_on_jp(self) -> None:
        self.assertTrue(bv.include_for_jp("a,", "あ", self.jp))

    def test_katakana_value_accepted_on_jp(self) -> None:
        self.assertTrue(bv.include_for_jp("a.", "ア", self.jp))

    def test_kana_code_prefix_accepted_on_jp(self) -> None:
        # Comma/period code rows belong to JP regardless of value.
        self.assertTrue(bv.include_for_jp(",foo", "あ", self.jp))


class ReadIrgSourcesTest(unittest.TestCase):
    """Parse a small synthetic Unihan extract."""

    SAMPLE = (
        "# header\n"
        "U+5E8A\tkIRG_GSource\tG0-3432\n"
        "U+5E8A\tkIRG_HSource\tHB1-A7C9\n"
        "U+5E8A\tkIRG_JSource\tJ0-3E32\n"
        "U+5E8A\tkIRG_TSource\tT1-4A2B\n"
        "U+7240\tkIRG_HSource\tH-FE48\n"
        "U+7240\tkIRG_TSource\tT3-297C\n"  # T3 -> EXCLUDED from TSource
        "U+5C0D\tkIRG_GSource\tG1-3654\n"  # G1 -> EXCLUDED from simp
        "U+5C0D\tkIRG_TSource\tT1-6857\n"
        "U+5C0D\tkIRG_JSource\tJ0-5574\n"
        "U+5BF9\tkIRG_GSource\tG0-3654\n"
        "U+5BF9\tkIRG_TSource\tT3-223E\n"  # T3 -> not trad
        "U+5BF9\tkIRG_JSource\tJMJ-010254\n"  # JM -> NOT jp
        "U+5BFE\tkIRG_JSource\tJ0-4250\n"
        "U+5BFE\tkIRG_TSource\tT3-255C\n"  # T3 -> not trad
        "U+5BFE\tkIRG_GSource\tGE-3674\n"  # GE -> NOT simp
        "U+89AA\tkIRG_GSource\tG1-4757\n"  # G1 -> NOT simp
        "U+89AA\tkIRG_TSource\tT1-7235\n"
        "U+4EB2\tkIRG_GSource\tG0-4757\n"
        "U+4EB2\tkIRG_TSource\tT3-2B24\n"  # T3 -> not trad
        "U+593A\tkIRG_GSource\tG0-3661\n"
    )

    def test_full_parse(self) -> None:
        with tempfile.NamedTemporaryFile(
            "w", suffix=".txt", delete=False, encoding="utf-8"
        ) as fh:
            fh.write(self.SAMPLE)
            path = fh.name
        try:
            trad, simp, jp, trad_core, simp_core = bv._read_irg_sources(path)
        finally:
            os.unlink(path)
        # 床 trad via T1
        self.assertIn(CP_CHUANG, trad)
        # 牀 trad via HSource only (TSource is T3-)
        self.assertIn(CP_CHUANG_FORMAL, trad)
        # 對 trad via T1
        self.assertIn(CP_DUI_TRAD, trad)
        # 对 NOT trad (TSource T3-, no HSource)
        self.assertNotIn(CP_DUI_SIMP, trad)
        # 対 NOT trad (TSource T3-, no HSource)
        self.assertNotIn(CP_DUI_JP, trad)
        # 亲 NOT trad
        self.assertNotIn(CP_QIN_SIMP, trad)

        # simp_set
        self.assertIn(CP_CHUANG, simp)
        self.assertIn(CP_DUI_SIMP, simp)
        # 対 GE -> NOT in simp; 親 G1 -> NOT in simp
        self.assertNotIn(CP_DUI_JP, simp)
        self.assertNotIn(CP_QIN_TRAD, simp)
        self.assertIn(CP_QIN_SIMP, simp)
        self.assertIn(CP_DUO_SIMP, simp)
        self.assertNotIn(CP_DUI_TRAD, simp)

        # jp_set: 対 via J0, 對 via J0; 对 NOT jp (only JMJ)
        self.assertIn(CP_CHUANG, jp)
        self.assertIn(CP_DUI_TRAD, jp)
        self.assertIn(CP_DUI_JP, jp)
        self.assertNotIn(CP_DUI_SIMP, jp)

        # trad_core (T1 or HB1): 床 (T1+HB1), 對 (T1 only), 親 (T1).
        # 牀 has HSource H-FE48 (not HB1), TSource T3 -> NOT core.
        self.assertIn(CP_CHUANG, trad_core)
        self.assertIn(CP_DUI_TRAD, trad_core)
        self.assertIn(CP_QIN_TRAD, trad_core)
        self.assertNotIn(CP_CHUANG_FORMAL, trad_core)

        # simp_core (G0-): 床, 对, 亲, 夺 -> G0; 對 G1 -> NOT core
        self.assertIn(CP_CHUANG, simp_core)
        self.assertIn(CP_DUI_SIMP, simp_core)
        self.assertIn(CP_QIN_SIMP, simp_core)
        self.assertIn(CP_DUO_SIMP, simp_core)
        self.assertNotIn(CP_DUI_TRAD, simp_core)


class WriteCharsetTest(unittest.TestCase):
    def test_writes_sorted_one_per_line(self) -> None:
        with tempfile.TemporaryDirectory() as d:
            path = os.path.join(d, "out")
            n = bv._write_charset(path, _set(0x5C0D, 0x4E00, 0x5E8A))
            self.assertEqual(n, 3)
            with open(path, encoding="utf-8") as fh:
                content = fh.read()
            # Sorted ascending: U+4E00 (一), U+5C0D (對), U+5E8A (床)
            # Wait sort by codepoint: 0x4E00 < 0x5C0D < 0x5E8A
            self.assertEqual(content, "\u4E00\n\u5C0D\n\u5E8A\n")


if __name__ == "__main__":
    unittest.main()
