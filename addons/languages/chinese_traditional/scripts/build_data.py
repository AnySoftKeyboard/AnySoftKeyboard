"""
Build Apache-2.0-compatible data tables for the Chinese Traditional input pack.

All output is derived solely from the Unicode Unihan database (Unicode
License, BSD-style, Apache-2.0 compatible). No GPL/LGPL/AGPL upstream
is used.

All four output tables are FILTERED at build time to only contain
characters in the trad include-set (TSource T1-/T2- ∪ HSource HB*/HD-);
see ``boshiamy_variants.py`` for the zone rationale. This is a
Traditional Chinese IME pack, so Cangjie / Zhuyin / homophone lookups
must never surface simplified-only glyphs.

Outputs (UTF-8, tab-separated unless noted) written directly to
``pack/src/main/res/raw/``:
  chinese_char_to_zhuyin   <char> <TAB> <zhuyin syllable>
  chinese_homophones       <syllable> <TAB> <chars...>
  cangjie_radicals         <code> <TAB> <chars (in code-point order)>
  zhuyin_radicals          <bopomofo+tone> <TAB> <chars>
"""
import os
import re
import sys

import boshiamy_variants as bv

ROOT = os.path.dirname(os.path.abspath(__file__))

# Unihan source dir lookup mirrors boshiamy_variants.UNIHAN_CANDIDATES so
# both scripts find the same archive on every dev machine.
_REPO_ROOT = os.path.abspath(os.path.join(ROOT, "..", "..", "..", ".."))
_UNIHAN_CANDIDATES = [
    os.path.join(_REPO_ROOT, "scratch", "Unihan"),
    os.path.join(_REPO_ROOT, "Unihan"),
    os.path.join(ROOT, "Unihan"),
]
UNIHAN = next((p for p in _UNIHAN_CANDIDATES if os.path.isdir(p)), None)
if UNIHAN is None:
    raise FileNotFoundError(
        "Unihan/ folder not found. Searched:\n  "
        + "\n  ".join(_UNIHAN_CANDIDATES)
        + "\nDownload from https://www.unicode.org/Public/UCD/latest/ucd/"
    )

# Write directly to the runtime res/raw dir; the trad-include filter
# applied here makes the on-disk tables the authoritative trad-only
# source for Cangjie / Zhuyin / homophone lookups -- no runtime trad
# filtering needed for these tables.
OUT = os.path.join(ROOT, "..", "pack", "src", "main", "res", "raw")
os.makedirs(OUT, exist_ok=True)

# trad_set is the SAME core-trad codepoint set used by boshiamy_variants
# (TSource T1-/T2- ∪ HSource HB*/HD-); see that module's docstring for the
# zone rationale. This guarantees every trad-side keyboard shares one
# authoritative include set.
_trad_unihan = bv._find_unihan()
print(f"loading trad/simp/jp char-sets from {_trad_unihan}")
_, _, _, TRAD_SET, _ = bv._read_irg_sources(_trad_unihan)
print(f"trad_set size: {len(TRAD_SET)}")


def _all_chars_in_trad(text: str) -> bool:
    """True if every codepoint in ``text`` is in TRAD_SET.

    Strings that are not CJK (e.g. ASCII radical codes are NEVER passed
    here -- this is only used for value-side filtering) are unlikely
    in these tables, but we still gate on TRAD_SET membership.
    """
    return all(ord(c) in TRAD_SET for c in text)


# Pinyin (Hanyu Pinyin with tone marks) -> Bopomofo.
# Compiled from the published Pinyin/Bopomofo equivalence (a well-known fact;
# also published in Unicode Standard Annex). Tones map to ASCII suffixes
# (1-4 + light "0") for compact storage; we preserve the tone for homophone
# bucket separation.

INITIALS = [
    ("zh", "ㄓ"), ("ch", "ㄔ"), ("sh", "ㄕ"),
    ("b", "ㄅ"), ("p", "ㄆ"), ("m", "ㄇ"), ("f", "ㄈ"),
    ("d", "ㄉ"), ("t", "ㄊ"), ("n", "ㄋ"), ("l", "ㄌ"),
    ("g", "ㄍ"), ("k", "ㄎ"), ("h", "ㄏ"),
    ("j", "ㄐ"), ("q", "ㄑ"), ("x", "ㄒ"),
    ("r", "ㄖ"), ("z", "ㄗ"), ("c", "ㄘ"), ("s", "ㄙ"),
]

# Finals (the tricky bit: longest-match, with i/u/v glide handling)
FINALS = {
    "iong": "ㄩㄥ", "iang": "ㄧㄤ", "uang": "ㄨㄤ", "ueng": "ㄨㄥ",
    "ang": "ㄤ", "eng": "ㄥ", "ing": "ㄧㄥ", "ong": "ㄨㄥ",
    "iao": "ㄧㄠ", "uai": "ㄨㄞ", "ian": "ㄧㄢ", "uan": "ㄨㄢ",
    "üan": "ㄩㄢ", "van": "ㄩㄢ",
    "iou": "ㄧㄡ", "uei": "ㄨㄟ", "iu": "ㄧㄡ", "ui": "ㄨㄟ",
    "ai": "ㄞ", "ei": "ㄟ", "ao": "ㄠ", "ou": "ㄡ",
    "an": "ㄢ", "en": "ㄣ", "in": "ㄧㄣ", "un": "ㄨㄣ",
    "ün": "ㄩㄣ", "vn": "ㄩㄣ", "uen": "ㄨㄣ",
    "ia": "ㄧㄚ", "ie": "ㄧㄝ", "io": "ㄧㄛ",
    "ua": "ㄨㄚ", "uo": "ㄨㄛ", "üe": "ㄩㄝ", "ve": "ㄩㄝ",
    "er": "ㄦ",
    "a": "ㄚ", "o": "ㄛ", "e": "ㄜ", "ê": "ㄝ",
    "i": "ㄧ", "u": "ㄨ", "ü": "ㄩ", "v": "ㄩ",
}

TONE_MARKS = {
    "ā": ("a", "1"), "á": ("a", "2"), "ǎ": ("a", "3"), "à": ("a", "4"),
    "ē": ("e", "1"), "é": ("e", "2"), "ě": ("e", "3"), "è": ("e", "4"),
    "ī": ("i", "1"), "í": ("i", "2"), "ǐ": ("i", "3"), "ì": ("i", "4"),
    "ō": ("o", "1"), "ó": ("o", "2"), "ǒ": ("o", "3"), "ò": ("o", "4"),
    "ū": ("u", "1"), "ú": ("u", "2"), "ǔ": ("u", "3"), "ù": ("u", "4"),
    "ǖ": ("ü", "1"), "ǘ": ("ü", "2"), "ǚ": ("ü", "3"), "ǜ": ("ü", "4"),
    "m̄": ("m", "1"), "ḿ": ("m", "2"), "m̌": ("m", "3"), "m̀": ("m", "4"),
    "n̄": ("n", "1"), "ń": ("n", "2"), "ň": ("n", "3"), "ǹ": ("n", "4"),
}

# Tone digit -> Bopomofo tone mark (1 = no mark, 5/0 = light tone)
TONE_BPMF = {"1": "", "2": "ˊ", "3": "ˇ", "4": "ˋ", "5": "˙", "0": "˙"}


def parse_pinyin(syl: str):
    """Return (toneless_syllable, tone_digit). Strip tone marks from vowels."""
    out = []
    tone = "5"
    for ch in syl:
        if ch in TONE_MARKS:
            base, t = TONE_MARKS[ch]
            out.append(base)
            tone = t
        else:
            out.append(ch)
    return ("".join(out), tone)


def pinyin_to_bopomofo(syl: str):
    """Convert one Hanyu Pinyin syllable (with tone marks) to a Bopomofo
    syllable with tone mark suffix. Returns None on failure."""
    plain, tone = parse_pinyin(syl)
    plain = plain.lower()

    # Quirk: yi -> i, wu -> u, yu -> ü
    # zi/ci/si/zhi/chi/shi/ri => initial only (the trailing 'i' is the empty rime)
    bpmf = ""
    rest = plain

    # Special: "er" / "r" alone
    if plain == "er":
        return "ㄦ" + TONE_BPMF[tone]

    initial_b = ""
    if rest.startswith("zh"):
        initial_b, rest = "ㄓ", rest[2:]
    elif rest.startswith("ch"):
        initial_b, rest = "ㄔ", rest[2:]
    elif rest.startswith("sh"):
        initial_b, rest = "ㄕ", rest[2:]
    else:
        for k, v in INITIALS:
            if rest.startswith(k) and len(k) == 1:
                initial_b, rest = v, rest[len(k):]
                break

    # Y/W glides: yi/yu/wu/yan/yu...
    if not initial_b and rest.startswith("y"):
        # yi -> i, ye -> ie, ya -> ia, yan -> ian, yang -> iang, yin -> in,
        # yu -> ü, yue -> üe, yuan -> üan, yun -> ün, ying -> ing, yong -> iong
        ymap = {
            "yi": "i", "ya": "ia", "yo": "io", "ye": "ie", "yao": "iao", "you": "iou",
            "yan": "ian", "yang": "iang", "yin": "in", "ying": "ing", "yong": "iong",
            "yu": "ü", "yue": "üe", "yuan": "üan", "yun": "ün",
        }
        for src, dst in sorted(ymap.items(), key=lambda x: -len(x[0])):
            if rest == src:
                rest = dst
                break
        else:
            rest = "i" + rest[1:]
    if not initial_b and rest.startswith("w"):
        wmap = {"wu": "u", "wa": "ua", "wo": "uo", "wai": "uai", "wei": "uei",
                "wan": "uan", "wang": "uang", "wen": "uen", "weng": "ueng"}
        for src, dst in sorted(wmap.items(), key=lambda x: -len(x[0])):
            if rest == src:
                rest = dst
                break
        else:
            rest = "u" + rest[1:]

    # 'ju/qu/xu' - the 'u' is actually 'ü'
    if initial_b in ("ㄐ", "ㄑ", "ㄒ"):
        if rest.startswith("u"):
            rest = "ü" + rest[1:]

    # Handle 'i' empty-rime after zh/ch/sh/r/z/c/s
    if rest == "i" and initial_b in ("ㄓ", "ㄔ", "ㄕ", "ㄖ", "ㄗ", "ㄘ", "ㄙ"):
        return initial_b + TONE_BPMF[tone]

    # Match longest final
    final_b = ""
    for k in sorted(FINALS.keys(), key=lambda x: -len(x)):
        if rest == k:
            final_b = FINALS[k]
            rest = ""
            break
    if not final_b and rest:
        # Try matching as much as possible
        for k in sorted(FINALS.keys(), key=lambda x: -len(x)):
            if rest.endswith(k) and rest[:-len(k)] == "":
                final_b = FINALS[k]
                rest = ""
                break

    if rest:
        # Unrecognized syllable (e.g. erhua "r" suffix in some entries)
        return None

    return (initial_b + final_b + TONE_BPMF[tone]) or None


# --- Parse Unihan ---
def cp_to_char(cp_str):
    return chr(int(cp_str[2:], 16))


char_to_pinyin = {}  # char -> [pinyin_syllable, ...]
with open(os.path.join(UNIHAN, "Unihan_Readings.txt"), encoding="utf-8") as f:
    for line in f:
        if not line.startswith("U+"):
            continue
        parts = line.rstrip("\n").split("\t")
        if len(parts) != 3:
            continue
        cp, field, value = parts
        if field != "kMandarin":
            continue
        ch = cp_to_char(cp)
        # kMandarin may have multiple readings space-separated (zh-CN before zh-TW)
        # Take all of them.
        char_to_pinyin[ch] = value.strip().split()


char_to_cangjie = {}  # char -> [code1, code2, ...]
with open(os.path.join(UNIHAN, "Unihan_DictionaryLikeData.txt"), encoding="utf-8") as f:
    for line in f:
        if not line.startswith("U+"):
            continue
        parts = line.rstrip("\n").split("\t")
        if len(parts) != 3:
            continue
        cp, field, value = parts
        if field != "kCangjie":
            continue
        ch = cp_to_char(cp)
        char_to_cangjie[ch] = value.strip().split()


# --- Build char -> zhuyin and homophones ---
char_zhuyin = {}     # char -> [bpmf_syl, ...]
zhuyin_chars = {}    # bpmf_syl -> [chars] (preserving insertion order, dedup)
unrecognized_pinyin = set()

for ch, pys in char_to_pinyin.items():
    syls = []
    for py in pys:
        # Some kMandarin entries are comma-separated lists for variant readings
        for one in py.split(","):
            one = one.strip()
            if not one:
                continue
            bpmf = pinyin_to_bopomofo(one)
            if bpmf is None:
                unrecognized_pinyin.add(one)
                continue
            if bpmf not in syls:
                syls.append(bpmf)
    if syls:
        char_zhuyin[ch] = syls
        for s in syls:
            zhuyin_chars.setdefault(s, []).append(ch)

print(f"chars with zhuyin: {len(char_zhuyin)}")
print(f"unique zhuyin syllables: {len(zhuyin_chars)}")
if unrecognized_pinyin:
    print(f"warn: {len(unrecognized_pinyin)} pinyin syllables unrecognized; "
          f"sample: {list(unrecognized_pinyin)[:8]}")


def write_first_column_pairs(path, mapping):
    """Format used by RadicalDictionary.loadFirstColumnFile:
       <key> <TAB> <value1 value2 ...>
    """
    with open(path, "w", encoding="utf-8", newline="\n") as out:
        for k in sorted(mapping):
            vals = mapping[k]
            if isinstance(vals, list):
                out.write(f"{k}\t{' '.join(vals)}\n")
            else:
                out.write(f"{k}\t{vals}\n")


def write_radicals(path, mapping):
    """Format: <code> <TAB> <chars>"""
    with open(path, "w", encoding="utf-8", newline="\n") as out:
        for code in sorted(mapping):
            out.write(f"{code}\t{''.join(mapping[code])}\n")


# char_to_zhuyin: <char>\t<primary-zhuyin> (one syllable per char; first reading wins).
# Filter by TRAD_SET so simp-only chars never enter the homophone-lookup
# data the trad keyboards consult.
_filtered_char_zhuyin = {
    ch: [syls[0]] for ch, syls in char_zhuyin.items() if ord(ch) in TRAD_SET
}
write_radicals(
    os.path.join(OUT, "chinese_char_to_zhuyin"), _filtered_char_zhuyin
)
print(f"char_to_zhuyin rows (trad-only): {len(_filtered_char_zhuyin)}")

# homophones: <zhuyin-syllable>\t<chars no separator>. Drop simp chars
# from every candidate list; drop syllables that end up empty.
_filtered_zhuyin_chars = {}
for syl, chars in zhuyin_chars.items():
    kept = [c for c in chars if ord(c) in TRAD_SET]
    if kept:
        _filtered_zhuyin_chars[syl] = kept
write_radicals(
    os.path.join(OUT, "chinese_homophones"), _filtered_zhuyin_chars
)
print(f"homophone syllables (trad-only): {len(_filtered_zhuyin_chars)}")

# --- Cangjie radicals ---
cangjie_radicals = {}  # code -> [chars]
for ch, codes in char_to_cangjie.items():
    if ord(ch) not in TRAD_SET:
        continue
    for code in codes:
        # Cangjie codes use 'X' prefix for "difficult" indicator; lowercase already
        if not code:
            continue
        cangjie_radicals.setdefault(code.lower(), []).append(ch)

write_radicals(os.path.join(OUT, "cangjie_radicals"), cangjie_radicals)
print(f"cangjie codes (trad-only): {len(cangjie_radicals)}")

# --- Zhuyin radicals ---
# Same shape as the filtered homophone map (Zhuyin's "radical sequence"
# IS the Bopomofo syllable, so the candidate list mirrors homophones).
write_radicals(os.path.join(OUT, "zhuyin_radicals"), _filtered_zhuyin_chars)

print("done.")
