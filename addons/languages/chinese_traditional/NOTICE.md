# Third-party data sources

This language pack bundles factual mapping tables. The pack's own source
code and `scripts/build_data.py` are under the project-wide Apache-2.0
license (repository root `LICENSE`). The data tables retain the license
of their upstream source, summarised below.

**License hygiene**: every redistributable file in this addon is
either under a permissive license (MIT, BSD-3-Clause, Apache-2.0,
Unicode License) or is bare factual data with no software-license claim
attached. **No GPL, LGPL, AGPL, or other copyleft-licensed source is
used, imported, or transformed by any script in this directory.**

| File family (under `pack/src/main/res/raw/`)                   | Upstream source                                                                                                                                                                                                                                                                                                                                                                          | Upstream license / status                                                                  |
| -------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------ |
| `chinese_char_to_zhuyin*`                                      | [Unicode Unihan database](https://www.unicode.org/charts/unihan.html), field `kMandarin` (Pinyin to Bopomofo converted by `scripts/build_data.py`)                                                                                                                                                                                                                                       | [Unicode License](https://www.unicode.org/license.html) (BSD-style, Apache-2.0 compatible) |
| `chinese_homophones*`                                          | Derived from `kMandarin` (Unihan) by `scripts/build_data.py`                                                                                                                                                                                                                                                                                                                             | Unicode License                                                                            |
| `chinese_char_frequency`                                       | **Generated** from the Unihan `kHanyuPinlu` field (per-million occurrence counts from the 漢語頻率詞典 corpus), ordered by descending count with a codepoint tie-break; characters absent from `kHanyuPinlu` are appended afterwards so every shipped character has a deterministic rank                                                                                                 | [Unicode License](https://www.unicode.org/license.html)                                    |
| `chinese_phrases`                                              | **Generated** by `scripts/build_phrases.py` from [McBopomofo](https://github.com/openvanilla/McBopomofo) `Source/Data/phrase.occ` (Traditional Chinese phrase frequency data; McBopomofo's data README notes its phrase material derives from `tsi.src` of libtabe, BSD licensed). Each multi-character phrase becomes a `first-character -> remainder` suggestion, most frequent first. | MIT (phrase material BSD)                                                                  |
| `chinese_opencc_*`                                             | [OpenCC](https://github.com/BYVoid/OpenCC) `STCharacters.txt` / `TSCharacters.txt` (`TSCharacters` drives the Simplified table's 台式簡體 ordering; both are also referenced for homophone exclusion)                                                                                                                                                                                    | Apache-2.0                                                                                 |
| `chinese_chars_trad`, `chinese_chars_simp`, `chinese_chars_jp` | **Generated** from the [Unicode Unihan IRG sources](https://www.unicode.org/charts/unihan.html) (`Unihan_IRGSources.txt`, kept under `scratch/Unihan/` and **not** redistributed in this repo) by `scripts/boshiamy_variants.py`; one CJK codepoint per line, used to filter both bundled and SAF-imported radical rows                                                                  | [Unicode License](https://www.unicode.org/license.html) (BSD-style)                        |
| `cangjie_radicals*`                                            | Unihan field `kCangjie` (Cangjie 5 decomposition), via `scripts/build_data.py`                                                                                                                                                                                                                                                                                                           | Unicode License                                                                            |
| `zhuyin_radicals*`                                             | Derived from `kMandarin` (Unihan), via `scripts/build_data.py`                                                                                                                                                                                                                                                                                                                           | Unicode License                                                                            |
| `boshiamy_radicals`                                            | **Generated** by `scripts/boshiamy_variants.py` from `scripts/openxiami_radicals_raw.tab` (the [Open Xiami 開放蝦米](https://docs.google.com/spreadsheets/d/1_j_O7aS6mew96gYwG58f82i3LCsbc0MmBAX-vxZmbE4/) community table, see "Boshiamy provenance"), keeping only rows whose CJK codepoints are all in the Traditional set                                                            | Open Xiami: contributors grant unconditional use to anyone, commercial or not              |
| `boshiamy_radicals_simplified`                                 | Same Open Xiami source, each value converted to its simplified form via the bundled OpenCC `TSCharacters` map and de-duplicated per code, then filtered against the Simplified set; converting before filtering reproduces the authentic 台式簡體 ordering                                                                                                                               | Open Xiami: unconditional grant                                                            |
| `boshiamy_radicals_japanese`                                   | Same Open Xiami source, filtered against the Japanese set (`kIRG_JSource` minus the JM Mojikyo and JK Korean-compat zones; kana values pass via the jp char-set)                                                                                                                                                                                                                         | Open Xiami: unconditional grant                                                            |
| `boshiamy_char_to_radical`                                     | **Generated** as the reverse index of `boshiamy_radicals` by `scripts/boshiamy_variants.py` (shortest code per character), so it can never drift from the forward table                                                                                                                                                                                                                  | Open Xiami: unconditional grant                                                            |

## Boshiamy provenance

Boshiamy 嘸蝦米 is a Chinese input method developed and commercially
sold by 行易有限公司 (Boshiamy Co., Ltd.). The company still holds
copyright over the code table shipped inside its commercial product,
and the tables that circulate online are generally extracted from it.
**This pack therefore does not use any table taken from that product.**

Instead the bundled tables are generated from
[**Open Xiami** 開放蝦米](https://docs.google.com/spreadsheets/d/1_j_O7aS6mew96gYwG58f82i3LCsbc0MmBAX-vxZmbE4/),
a community project that re-created a Boshiamy-compatible encoding from
scratch on Google Sheets, precisely in order to avoid those copyright
concerns. Its 版權宣告 (copyright declaration) sheet states, in summary:

1. The underlying 1990s patent lapsed at the end of 2003, but 行易 still
   owns the code table inside its own software.
2. To avoid the copyright status of the tables circulating online, the
   project rebuilds an "open-format Xiami encoding library" collaboratively,
   following Boshiamy's documented character-decomposition principles.
3. Contributors accept the project's encoding rules and weighting.
4. **Contributors unconditionally license their encodings to anyone, for
   any use, commercial or non-commercial.**

The spreadsheet's per-cell revision history is the project's own record
that the entries were typed by contributors rather than copied in bulk.

The export used to build this pack is committed as
`scripts/openxiami_source_export.tsv`, and
`scripts/openxiami_to_raw.py` converts it into
`scripts/openxiami_radicals_raw.tab`, so the whole chain is reproducible
and diffable.

**What this addon does _not_ do:**

- It does **not** ship, copy, or derive from the code table inside the
  commercial BoshiamyTIP product.
- It does **not** copy or derive from the LGPL-licensed
  [OpenVanilla](https://github.com/OpenVanilla/openvanilla-source) Liu
  input method tables.
- It does **not** bundle or transform any GPL/LGPL/AGPL upstream.
- It does **not** claim copyright over the underlying radical
  convention (which is Boshiamy Co.'s domain).

Users who own a Boshiamy license and want its full character coverage
can import their own `liu*.tab` files at runtime through the SAF picker
described below; nothing of the kind is redistributed here. This mirrors
how other open-source Boshiamy front-ends handle the same problem.

If you are 行易有限公司 or another rights holder and believe any
specific entry should be removed, please open an issue on the
AnySoftKeyboard repository.

## Regenerating the data

`scripts/build_data.py` regenerates the Unihan-derived dictionaries
(`chinese_char_to_zhuyin`, `chinese_homophones`, `cangjie_radicals`,
`zhuyin_radicals`) from a local copy of the Unicode Unihan archive.

`scripts/openxiami_to_raw.py` converts the Open Xiami sheet export
(`scripts/openxiami_source_export.tsv`, exported from the
`輸入法碼表-繁中` tab as TSV) into `scripts/openxiami_radicals_raw.tab`.

`scripts/boshiamy_variants.py` then regenerates **all seven** bundled
output files (`chinese_chars_trad`, `chinese_chars_simp`,
`chinese_chars_jp`, `boshiamy_radicals`,
`boshiamy_radicals_simplified`, `boshiamy_radicals_japanese`,
`boshiamy_char_to_radical`) deterministically. It needs a local copy
of `Unihan_IRGSources.txt` placed under `scratch/Unihan/` (download
from <https://www.unicode.org/Public/UCD/latest/ucd/>; this ~50 MB
archive is **not** committed). Run:

```
python addons/languages/chinese_traditional/scripts/boshiamy_variants.py
```

The script reads `scripts/openxiami_radicals_raw.tab` (build-time input
only, not packaged into the APK) plus the Unihan IRG source registry,
computes per-keyboard codepoint whitelists from the IRG zone tags,
writes those whitelists out as `chinese_chars_*` raw resources (which
the runtime then re-uses at SAF-import time to filter user-supplied
tables), and emits byte-for-byte reproducible tables that contain only
rows whose value falls inside the target script's codepoint set.
Reviewers can verify provenance by re-running and diffing.

`chinese_char_frequency` is generated from the Unihan `kHanyuPinlu`
field; see the table above.

## Variant coverage

Only the Traditional table comes from Open Xiami (stored as
`scripts/openxiami_radicals_raw.tab`, build-time input only, not
bundled into the APK). The bundled `boshiamy_radicals` (trad),
`boshiamy_radicals_simplified` and `boshiamy_radicals_japanese` are all
derived programmatically from it:

- **Trad** (`boshiamy_radicals`): keeps every row whose value contains
  only CJK codepoints in the Traditional Chinese set
  (`kIRG_HSource` ∪ `kIRG_TSource` minus the T3 compatibility-only
  zone). Boshiamy "kana"-prefix codes (`,foo` / `.foo`) are dropped.
  This is why typing `ltn` correctly yields `床 親 喿` on the trad
  keyboard while `a` yields only `對` (not `对` / `対`).
- **Simp** (`boshiamy_radicals_simplified`): converts each value to its
  simplified form with the bundled OpenCC `TSCharacters` map, de-dupes
  per code, then keeps rows whose value contains only CJK codepoints in
  the Simplified Chinese set (`kIRG_GSource` minus G1 fan-ti, GE
  GB16500-ext, GH HKSCS-via-G and GK Korean-via-G zones). Converting
  before filtering is what reproduces the authentic 台式簡體 ordering:
  the simplified glyph of a Traditional primary inherits the primary
  slot (`iwn` yields `这` first, not the secondary `汞`).
- **JP** (`boshiamy_radicals_japanese`): keeps rows whose value
  contains only CJK codepoints in the Japanese set
  (`kIRG_JSource` minus the JM Mojikyo and JK Korean-compat zones)
  and/or hiragana / katakana.

The exact same per-keyboard char-set raw files are referenced from
`chinese_traditional_dictionaries.xml` via the new `includeCharsResId`
attribute, but the runtime applies the filter **only** to the bundled
main table (where it is a defensive no-op since the table is already
pre-filtered at build time). User-supplied SAF overlays (`liu.box`,
`*.tab`, etc.) are intentionally **not** filtered; those are treated
as the user's curated personal additions and are kept verbatim across
all three Boshiamy keyboards regardless of script. See
`RadicalDictionary.valuePassesIncludeFilter`.

One known asymmetry that is **not** re-derived: the **reverse** lookup
`boshiamy_char_to_radical` (used by the homophone / "find this char's
Boshiamy code" feature) is trad-keyed, so looking up a simplified-only or
shinjitai-only glyph won't reverse into a code.

The bundled next-word phrase table (`chinese_phrases`) is Traditional and is
shared by the Boshiamy, Cangjie and Zhuyin keyboards, since its lookup key is
the previously-committed character rather than radical input. Users can
replace or extend it through the SAF picker described below.

This behaviour is acceptable for the IME's intended use; advanced users
who need simplified-keyed reverse lookup or their own phrase tables can
supply their own files via the SAF picker described below.

## Migration note (removed keyboard)

The previously-shipped "嘸蝦米 台簡" (zh-TW-simplified) keyboard with
addon id `b64cd192-8cce-43b5-b5e9-4c7124e2fd7a` has been retired. Users
who had only this keyboard enabled will silently fall back to the
default keyboard on first run after upgrade (`AddOnsFactory` filters out
unknown ids); they should re-enable "嘸蝦米 简体" (id
`a53bc081-6bbd-42a4-a4d7-3b5012d1ec5e`) which now uses the OpenCC-derived
simplified table by default and accepts SAF-picked overlays for
power-users who maintain their own simplified Boshiamy tables.

## Bringing your own tables (user overlay)

Users may replace or augment the bundled radical tables at runtime by
dropping files into the addon's external-files directory (or a folder
of their own choosing; see "Custom overlay folder" below):

```
<overlay-base>/<addon-uuid>/
    main.tab            Replaces the bundled main radical -> char table.
    liu*.tab            Additive overlay (BoshiamyTIP binary format,
                        e.g. liu1.tab / liu-uni3.tab); auto-detected
                        and merged on top of whichever main table is
                        active.
    liu.box             Additive overlay (plain-text shortcuts);
                        merged on top of whichever main table is active.
    phrases.tab         Replaces the bundled phrases table.
    phrases.box         Additive overlay on top of phrases.
    homophones.tab      Replaces the bundled homophone table.
    char_to_zhuyin.tab  Replaces the bundled char -> Zhuyin lookup.
    char_to_radical.tab Replaces the bundled char -> radical lookup.
    char_frequency.tab  Replaces the bundled char-frequency ranking.
```

The per-keyboard `<addon-uuid>/` sub-folder is optional. If the folder
you pick has no such sub-folder, the files in the picked folder itself
are used, which is the simplest setup when you only use one radical
keyboard. Use sub-folders when you want one picked folder to serve
several keyboards with different tables.

Default `<overlay-base>` is the addon's external files directory:
`/sdcard/Android/data/<addon-package>/files/boshiamy/`. It is
adb-writable without root.

### Supported file formats

Plain-text mapping files use one entry per line, `<code><tab><char(s)>`.
Lines beginning with `#` and blank lines are ignored. UTF-8 (with
optional BOM).

`liu*.tab` may also be the proprietary BoshiamyTIP binary format
shipped by 行易 (Boshiamy Co., Ltd.). Format is auto-detected by
filename prefix (`liu`) and content sniffing (presence of NUL bytes in
the header); recognised binary files are decoded internally to the
same key->char mapping as plain text. Users supplying their own binary
files retain all responsibility for the licensing of those files;
nothing of the kind is bundled here.

`.cin` (SCIM) and `.dict.yaml` (RIME) source files are also accepted by
the importer and converted on-the-fly.

### Custom overlay folder

By default the overlay folder is the addon-private external storage
above. Users who prefer a shared location (e.g. a `Documents/boshiamy`
folder syncable across devices) can set **Settings -> Dictionary ->
Radical input overlay folder** to an absolute path; per-keyboard
subfolders (`<addon-uuid>/`) are then expected inside that path.

The `<addon-package>` is the Chinese-Traditional pack's application ID
(e.g. `com.anysoftkeyboard.languagepack.chinese_traditional`). The
`<addon-uuid>` is the per-keyboard ID declared in the addon XML (run
`adb shell run-as <addon-package> ls files/boshiamy/` to discover it).

Example workflow (no root required):

```bash
# 1. Convert an upstream table to the addon's format.
#    Three open formats are accepted as input:
#      - IBus / OpenVanilla .tab    (<code>\t<char> per line)
#      - SCIM .cin                  (%chardef begin ... %chardef end)
#      - RIME *.dict.yaml           (text\tcode\tweight, after ---)
#    Use any IBus-compatible tool, or any RIME conversion utility.

# 2. Push the converted file:
adb push my_main.tab \
  /sdcard/Android/data/com.anysoftkeyboard.languagepack.chinese_traditional/files/boshiamy/<addon-uuid>/main.tab

# 3. Force the dictionary to reload (toggle the keyboard or restart ASK).
```

User-supplied files retain their own licensing; they are never
redistributed by this project.
