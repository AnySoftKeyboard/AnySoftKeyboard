# Traditional Chinese Language Pack

This add-on bundles three input methods commonly used for Traditional
Chinese. Boshiamy and Cangjie are radical-based, Zhuyin is phonetic:

| IME                 | Keyboards                                                                                                | Notes                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
| ------------------- | -------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Boshiamy 嘸蝦米** | 繁體 / 简体 / 日本語 (3 layouts)                                                                         | QWERTY + custom radical chars; supports homophone lookup (同) and radical-candidate-selector keys (`vrsfwlcbkj`). Bundled tables come from the community [Open Xiami 開放蝦米](https://docs.google.com/spreadsheets/d/1_j_O7aS6mew96gYwG58f82i3LCsbc0MmBAX-vxZmbE4/) project (see `NOTICE.md`); the 简体 and 日本語 tables are derived from the 繁體 table. Users who own a Boshiamy license can import their own `liu*.tab` for fuller coverage via the SAF folder picker in Settings -> Dictionary. |
| **Cangjie 5 倉頡**  | Standard 5th-edition mapping                                                                             | QWERTY shape; each key shows its Cangjie radical (日月金木水火土…) as a hint.                                                                                                                                                                                                                                                                                                                                                                                                                         |
| **Zhuyin 注音**     | 注音 (Standard 大千), 注音 ，。, 注音 倚天 (ETen 41), 注音 許氏 (Hsu), 注音 倚天26 (ETen 26) (5 layouts) | Sentence-level phonetic input: type whole phrases (tones optional, space = 1st tone) and the engine converts them to the most likely Hanzi (ㄋㄧˇㄏㄠˇㄇㄚ˙ → 你好嗎). The strip offers the whole sentence, then words and characters from the start: one tap commits a character, a word or the sentence. ← / → move inside the composition to fix a middle word. Picks are learned (not in incognito).                                                                                              |

Boshiamy and Cangjie reuse the `RadicalDictionary` engine in
`ime/dictionaries/`; Zhuyin uses the phonetic engine in `ime/phonetic/`
(lattice + Viterbi over McBopomofo's phrase data). This plugin only ships the
data tables, drawables and keyboard XML.

## How to use

1. Install AnySoftKeyboard.
2. Install this language pack APK.
3. In AnySoftKeyboard settings, enable any of the keyboards above.
4. Type a radical sequence (Boshiamy, Cangjie) or Bopomofo (Zhuyin);
   candidates appear in the suggestion strip.
5. Tap a candidate or press space to commit. On Boshiamy, long-press the "同"
   key for homophone lookup, or press one of the selector keys (`v`, `r`,
   `s`, `f`, `w`, `l`, `c`, `b`, `k`, `j`) to pick the 2nd–10th candidate.

## Data sources & licensing

Source code in this pack is under the project-wide **Apache-2.0**
license (see the repository root `LICENSE`), like the rest of
AnySoftKeyboard.

Bundled data tables under `pack/src/main/res/raw/` retain their upstream
licenses (Unicode License, Apache-2.0, MIT, BSD). The full per-file
provenance table is in [`NOTICE.md`](NOTICE.md).

### Boshiamy data

The Boshiamy 嘸蝦米 tables are generated from the community
[Open Xiami 開放蝦米](https://docs.google.com/spreadsheets/d/1_j_O7aS6mew96gYwG58f82i3LCsbc0MmBAX-vxZmbE4/)
project, which built a compatible encoding from scratch on Google Sheets
and whose contributors grant unconditional use to anyone. This pack ships
**only** mapping data: no tutorial, training material, software code, or
UI. See [`NOTICE.md`](NOTICE.md) for the full chain and the takedown
contact path.

Users who own a Boshiamy license can import their own `liu*.tab` at
runtime through the SAF picker for fuller character coverage.

The Cangjie 5 mapping is sourced from the Unihan `kCangjie` field, and
the Zhuyin layout is the standard 大千 phonetic layout in widespread
use. The other Zhuyin keyboards are the ETen 41-key, Hsu and ETen 26-key
layouts; on the two 26-key ones several symbols share a key and the
engine picks by context.

### Next-word phrase data

The bundled phrase table (`chinese_phrases`) is generated by
`scripts/build_phrases.py` from
[McBopomofo](https://github.com/openvanilla/McBopomofo)'s
`Source/Data/phrase.occ`, a Traditional Chinese phrase-frequency list.
McBopomofo is MIT licensed, and its data README records that the phrase
material derives from `tsi.src` of libtabe (BSD licensed). Each
multi-character phrase becomes a `first-character -> remainder`
suggestion, ordered most frequent first.

### Zhuyin phrase data

The Zhuyin phrase table (`zhuyin_phrases`, binary) is generated by
`scripts/build_zhuyin_phrases.py` from the same McBopomofo data
(`BPMFBase.txt`, `BPMFMappings.txt`, `phrase.occ` and the heterophony
lists) at a pinned commit; the download commands are in the script's
header, the row in [`NOTICE.md`](NOTICE.md).

## Regenerating data

`scripts/build_data.py` regenerates the data files from Unihan and the
input Boshiamy/phrase lists. The script is committed alongside the pack
so the data pipeline is reproducible from source.
`scripts/build_zhuyin_phrases.py` does the same for the Zhuyin phrase
table; its tests run with
`python3 -m unittest build_zhuyin_phrases_test` from `scripts/`.
