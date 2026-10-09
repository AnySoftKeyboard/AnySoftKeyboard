/*
 * Copyright (c) 2013 Menny Even-Danan
 * Copyright (c) 2026 AnySoftKeyboard contributors (radical-dictionary engine)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.anysoftkeyboard.dictionaries;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import com.anysoftkeyboard.base.utils.Logger;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A dictionary for radical-based input methods (e.g., Boshiamy, Zhuyin, Cangjie). Maps sequences of
 * Latin keystrokes (radicals) to target characters/words.
 *
 * <p>The dictionary file format is tab-separated: each line contains {@code
 * radical_sequence\tcharacter}. Multiple lines may share the same radical sequence to provide
 * multiple candidates.
 */
public class RadicalDictionary extends Dictionary {

  private static final String TAG = "ASKRadicalDict";

  @Nullable private final Context mContext;
  private final int mResourceId;
  private final int mPhrasesResourceId;
  private final int mHomophonesResourceId;
  private final int mCharToZhuyinResourceId;
  private final int mCharToRadicalResourceId;
  private final int mExcludeHomophoneCharsResourceId;
  private final int mIncludeCharsResourceId;
  private final int mCharFrequencyResourceId;

  /**
   * When true the loader splits a single multi-codepoint candidate string into one candidate per
   * CJK codepoint. Used by Cangjie and Zhuyin, both the bundled tables (source format is {@code
   * key\tchar1char2..}) and any user-supplied SAF overlay rows for those IMEs. Boshiamy leaves this
   * off so multi-codepoint outputs such as {@code liu\t嘸蝦米} remain a single candidate, both for the
   * bundled chart and for user-personal-additions files (e.g. liu.box). As a side-effect this is
   * also the per-keyboard knob for the frequency-rank stable sort in {@link #getExactMatches}:
   * Cangjie/Zhuyin opt into the most-common-first ordering, Boshiamy preserves the source file's
   * curated order (its first row per code is the author's primary pick, also indexed positionally
   * by the vrsfwlcbkj selector keys).
   */
  private final boolean mSplitMultiCodepointCandidates;

  @NonNull private final RadicalOverlay mOverlay;

  /** Snapshot of all radical data, published via a single volatile write of {@link #mData}. */
  private static final class RadicalData {
    final Map<String, List<String>> radicalMap;
    final Set<String> prefixes;
    final Set<String> validOutputs;
    final Map<String, List<String>> phraseMap;
    final Map<String, String> charToZhuyin;
    final Map<String, List<String>> zhuyinToChars;
    final Map<String, String> charToRadical;
    final Set<String> excludeHomophoneChars;

    /**
     * Codepoint whitelist for the per-keyboard include filter (see {@link
     * #valuePassesIncludeFilter(String, Set)}). Empty set means the filter is disabled.
     */
    final Set<String> includeChars;

    /** Map of CJK char -> rank (lower = more common). Chars not present rank as MAX_VALUE. */
    final Map<String, Integer> charFrequencyRank;

    RadicalData(
        Map<String, List<String>> radicalMap,
        Set<String> prefixes,
        Set<String> validOutputs,
        Map<String, List<String>> phraseMap,
        Map<String, String> charToZhuyin,
        Map<String, List<String>> zhuyinToChars,
        Map<String, String> charToRadical,
        Set<String> excludeHomophoneChars,
        Set<String> includeChars,
        Map<String, Integer> charFrequencyRank) {
      this.radicalMap = radicalMap;
      this.prefixes = prefixes;
      this.validOutputs = validOutputs;
      this.phraseMap = phraseMap;
      this.charToZhuyin = charToZhuyin;
      this.zhuyinToChars = zhuyinToChars;
      this.charToRadical = charToRadical;
      this.excludeHomophoneChars = excludeHomophoneChars;
      this.includeChars = includeChars;
      this.charFrequencyRank = charFrequencyRank;
    }
  }

  private static final RadicalData EMPTY_DATA =
      new RadicalData(
          Collections.emptyMap(),
          Collections.emptySet(),
          Collections.emptySet(),
          Collections.emptyMap(),
          Collections.emptyMap(),
          Collections.emptyMap(),
          Collections.emptyMap(),
          Collections.emptySet(),
          Collections.emptySet(),
          Collections.emptyMap());

  private volatile RadicalData mData = EMPTY_DATA;

  public RadicalDictionary(
      @NonNull CharSequence dictionaryName,
      @Nullable Context context,
      int resourceId,
      int phrasesResourceId,
      int homophonesResourceId,
      int charToZhuyinResourceId,
      int charToRadicalResourceId,
      int excludeHomophoneCharsResourceId,
      int charFrequencyResourceId,
      boolean splitMultiCodepointCandidates) {
    this(
        dictionaryName,
        context,
        resourceId,
        phrasesResourceId,
        homophonesResourceId,
        charToZhuyinResourceId,
        charToRadicalResourceId,
        excludeHomophoneCharsResourceId,
        0,
        charFrequencyResourceId,
        splitMultiCodepointCandidates,
        RadicalOverlay.NONE);
  }

  public RadicalDictionary(
      @NonNull CharSequence dictionaryName,
      @Nullable Context context,
      int resourceId,
      int phrasesResourceId,
      int homophonesResourceId,
      int charToZhuyinResourceId,
      int charToRadicalResourceId,
      int excludeHomophoneCharsResourceId,
      int includeCharsResourceId,
      int charFrequencyResourceId,
      boolean splitMultiCodepointCandidates,
      @NonNull RadicalOverlay overlay) {
    super(dictionaryName);
    mContext = context;
    mResourceId = resourceId;
    mPhrasesResourceId = phrasesResourceId;
    mHomophonesResourceId = homophonesResourceId;
    mCharToZhuyinResourceId = charToZhuyinResourceId;
    mCharToRadicalResourceId = charToRadicalResourceId;
    mExcludeHomophoneCharsResourceId = excludeHomophoneCharsResourceId;
    mIncludeCharsResourceId = includeCharsResourceId;
    mCharFrequencyResourceId = charFrequencyResourceId;
    mSplitMultiCodepointCandidates = splitMultiCodepointCandidates;
    mOverlay = overlay;
  }

  public RadicalDictionary(
      @NonNull CharSequence dictionaryName,
      @Nullable Context context,
      int resourceId,
      int phrasesResourceId,
      int homophonesResourceId,
      int charToZhuyinResourceId,
      int charToRadicalResourceId,
      int excludeHomophoneCharsResourceId,
      int charFrequencyResourceId) {
    this(
        dictionaryName,
        context,
        resourceId,
        phrasesResourceId,
        homophonesResourceId,
        charToZhuyinResourceId,
        charToRadicalResourceId,
        excludeHomophoneCharsResourceId,
        charFrequencyResourceId,
        false);
  }

  public RadicalDictionary(
      @NonNull CharSequence dictionaryName,
      @Nullable Context context,
      int resourceId,
      int phrasesResourceId,
      int homophonesResourceId,
      int charToZhuyinResourceId,
      int charToRadicalResourceId,
      int excludeHomophoneCharsResourceId) {
    this(
        dictionaryName,
        context,
        resourceId,
        phrasesResourceId,
        homophonesResourceId,
        charToZhuyinResourceId,
        charToRadicalResourceId,
        excludeHomophoneCharsResourceId,
        0);
  }

  public RadicalDictionary(
      @NonNull CharSequence dictionaryName,
      @Nullable Context context,
      int resourceId,
      int phrasesResourceId,
      int homophonesResourceId,
      int charToZhuyinResourceId,
      int charToRadicalResourceId) {
    this(
        dictionaryName,
        context,
        resourceId,
        phrasesResourceId,
        homophonesResourceId,
        charToZhuyinResourceId,
        charToRadicalResourceId,
        0,
        0);
  }

  public RadicalDictionary(
      @NonNull CharSequence dictionaryName,
      @Nullable Context context,
      int resourceId,
      int phrasesResourceId) {
    this(dictionaryName, context, resourceId, phrasesResourceId, 0, 0, 0, 0, 0);
  }

  public RadicalDictionary(
      @NonNull CharSequence dictionaryName, @Nullable Context context, int resourceId) {
    this(dictionaryName, context, resourceId, 0, 0, 0, 0, 0, 0);
  }

  @Override
  protected void loadAllResources() {
    if (mContext == null || mResourceId <= 0) {
      return;
    }
    final Resources res = mContext.getResources();

    // Load the per-keyboard include filter first so it's available when parsing
    // the main table (bundled or overlay), keeps both code paths consistent.
    final HashSet<String> includeChars = new HashSet<>();
    if (mIncludeCharsResourceId > 0) {
      final int[] includeResIds = resolveResourceIds(res, mIncludeCharsResourceId);
      if (includeResIds != null) {
        for (int resId : includeResIds) {
          loadFirstColumnFile(res, resId, includeChars);
        }
      }
    }

    final HashMap<String, List<String>> radicalMap = new HashMap<>();
    final HashSet<String> prefixes = new HashSet<>();
    final HashSet<String> validOutputs = new HashSet<>();

    // Main radical table: overlay replacement wins over bundled; overlay always merges in addition.
    //
    // Overlays are loaded FIRST so that, for codes both sources know, the user's own table
    // decides which candidate is primary. The bundled table is a subset whose ordering comes
    // from the Open Xiami sheet, so someone who imports a fuller Boshiamy table expects that
    // table's ranking to win (typing "pri" should offer 到 before the rare 佽). addCandidate
    // keeps the first occurrence, so the bundled pass afterwards only fills in codes and
    // characters the overlay did not supply.
    //
    // The include filter applies to the bundled main table AND to any SAF / user-supplied overlay
    // table (so e.g. a simp-only external table won't pollute the trad keyboard). The ONE exception
    // is `liu.box`, that is McBopomofo/嘸蝦米's user-curated personal additions file, and the user
    // explicitly wants their custom characters and phrases available on every Boshiamy keyboard
    // regardless of script. Overlays whose source name matches {@code liu.box} are passed through
    // with an empty include-set; everything else uses the per-keyboard include filter.
    final List<RadicalOverlay.StreamSupplier> mainOverlay = mOverlay.mainTableOverlay();
    if (mainOverlay != null) {
      for (RadicalOverlay.StreamSupplier supplier : mainOverlay) {
        loadOverlayRadicals(
            supplier, radicalMap, prefixes, validOutputs, includeCharsFor(supplier, includeChars));
      }
    }
    final List<RadicalOverlay.StreamSupplier> mainReplacement = mOverlay.mainTableReplacement();
    if (mainReplacement != null && !mainReplacement.isEmpty()) {
      for (RadicalOverlay.StreamSupplier supplier : mainReplacement) {
        loadOverlayRadicals(
            supplier, radicalMap, prefixes, validOutputs, includeCharsFor(supplier, includeChars));
      }
    } else {
      final int[] resIds = resolveResourceIds(res, mResourceId);
      if (resIds == null || resIds.length == 0) {
        Logger.w(TAG, "No radical dictionary resources to load");
        return;
      }
      for (int resId : resIds) {
        loadResourceFile(res, resId, radicalMap, prefixes, validOutputs, includeChars);
      }
    }

    // Load phrase associations if available
    final HashMap<String, List<String>> phraseMap = new HashMap<>();
    final List<RadicalOverlay.StreamSupplier> phrasesReplacement = mOverlay.phrasesReplacement();
    if (phrasesReplacement != null && !phrasesReplacement.isEmpty()) {
      for (RadicalOverlay.StreamSupplier supplier : phrasesReplacement) {
        loadOverlayPhrases(supplier, phraseMap);
      }
    } else if (mPhrasesResourceId > 0) {
      final int[] phraseResIds = resolveResourceIds(res, mPhrasesResourceId);
      if (phraseResIds != null) {
        for (int resId : phraseResIds) {
          loadPhrasesFile(res, resId, phraseMap);
        }
      }
    }
    final List<RadicalOverlay.StreamSupplier> phrasesOverlay = mOverlay.phrasesOverlay();
    if (phrasesOverlay != null) {
      for (RadicalOverlay.StreamSupplier supplier : phrasesOverlay) {
        loadOverlayPhrases(supplier, phraseMap);
      }
    }

    // Load homophone data if available
    final HashMap<String, String> charToZhuyin = new HashMap<>();
    final HashMap<String, List<String>> zhuyinToChars = new HashMap<>();
    final HashMap<String, String> charToRadical = new HashMap<>();

    final List<RadicalOverlay.StreamSupplier> homophonesReplacement =
        mOverlay.homophonesReplacement();
    if (homophonesReplacement != null && !homophonesReplacement.isEmpty()) {
      for (RadicalOverlay.StreamSupplier supplier : homophonesReplacement) {
        loadOverlayHomophones(supplier, zhuyinToChars);
      }
    } else if (mHomophonesResourceId > 0) {
      final int[] homophoneResIds = resolveResourceIds(res, mHomophonesResourceId);
      if (homophoneResIds != null) {
        for (int resId : homophoneResIds) {
          loadHomophonesFile(res, resId, zhuyinToChars);
        }
      }
    }
    final List<RadicalOverlay.StreamSupplier> charToZhuyinReplacement =
        mOverlay.charToZhuyinReplacement();
    if (charToZhuyinReplacement != null && !charToZhuyinReplacement.isEmpty()) {
      for (RadicalOverlay.StreamSupplier supplier : charToZhuyinReplacement) {
        loadOverlayKeyValue(supplier, charToZhuyin);
      }
    } else if (mCharToZhuyinResourceId > 0) {
      final int[] zhuyinResIds = resolveResourceIds(res, mCharToZhuyinResourceId);
      if (zhuyinResIds != null) {
        for (int resId : zhuyinResIds) {
          loadKeyValueFile(res, resId, charToZhuyin);
        }
      }
    }
    final List<RadicalOverlay.StreamSupplier> charToRadicalReplacement =
        mOverlay.charToRadicalReplacement();
    if (charToRadicalReplacement != null && !charToRadicalReplacement.isEmpty()) {
      for (RadicalOverlay.StreamSupplier supplier : charToRadicalReplacement) {
        loadOverlayKeyValue(supplier, charToRadical);
      }
    } else if (mCharToRadicalResourceId > 0) {
      final int[] radicalRevResIds = resolveResourceIds(res, mCharToRadicalResourceId);
      if (radicalRevResIds != null) {
        for (int resId : radicalRevResIds) {
          loadKeyValueFile(res, resId, charToRadical);
        }
      }
    }

    // Load characters to exclude from homophone results (e.g. simp/trad form filter).
    final HashSet<String> excludeHomophoneChars = new HashSet<>();
    if (mExcludeHomophoneCharsResourceId > 0) {
      final int[] excludeResIds = resolveResourceIds(res, mExcludeHomophoneCharsResourceId);
      if (excludeResIds != null) {
        for (int resId : excludeResIds) {
          loadFirstColumnFile(res, resId, excludeHomophoneChars);
        }
      }
    }

    // Load char frequency: a single string of CJK chars ordered most-common-first.
    final HashMap<String, Integer> charFrequencyRank = new HashMap<>();
    final List<RadicalOverlay.StreamSupplier> charFrequencyReplacement =
        mOverlay.charFrequencyReplacement();
    if (charFrequencyReplacement != null && !charFrequencyReplacement.isEmpty()) {
      for (RadicalOverlay.StreamSupplier supplier : charFrequencyReplacement) {
        loadOverlayCharFrequency(supplier, charFrequencyRank);
      }
    } else if (mCharFrequencyResourceId > 0) {
      final int[] freqResIds = resolveResourceIds(res, mCharFrequencyResourceId);
      if (freqResIds != null) {
        for (int resId : freqResIds) {
          loadCharFrequencyFile(res, resId, charFrequencyRank);
        }
      }
    }

    // Pre-sort candidates once here rather than on every keystroke, so
    // getExactMatches stays a pure map lookup. Applies to the dictionaries
    // that opted into frequency ordering (Cangjie / Zhuyin); Boshiamy keeps
    // its curated source order untouched.
    applyFrequencyOrdering(radicalMap, charFrequencyRank);

    // Publish all data atomically via single volatile write
    mData =
        new RadicalData(
            radicalMap,
            prefixes,
            validOutputs,
            phraseMap,
            charToZhuyin,
            zhuyinToChars,
            charToRadical,
            excludeHomophoneChars,
            includeChars,
            charFrequencyRank);

    Logger.d(
        TAG,
        "Loaded radical dictionary: %d mappings, %d prefixes, %d valid outputs",
        radicalMap.size(),
        prefixes.size(),
        validOutputs.size());
  }

  private void loadOverlayRadicals(
      @NonNull RadicalOverlay.StreamSupplier supplier,
      @NonNull HashMap<String, List<String>> radicalMap,
      @NonNull HashSet<String> prefixes,
      @NonNull HashSet<String> validOutputs,
      @NonNull Set<String> includeChars) {
    final String name = supplier.name();
    // Sniff first bytes for binary detection.
    byte[] sniff;
    try (InputStream is = supplier.open()) {
      sniff = readAllBytes(is);
    } catch (Exception e) {
      Logger.e(TAG, e, "Failed to read radical overlay %s", name);
      return;
    }
    com.anysoftkeyboard.dictionaries.radical.importers.RadicalTableImporter.Format fmt =
        com.anysoftkeyboard.dictionaries.radical.importers.RadicalTableImporter.Format
            .detectFromContentAndName(name, sniff);
    if (fmt
        == com.anysoftkeyboard.dictionaries.radical.importers.RadicalTableImporter.Format
            .IBUS_TAB) {
      // Same on-disk format as the bundled raw resources, parse directly without converting.
      try (InputStream is = new java.io.ByteArrayInputStream(sniff)) {
        int count = parseRadicalStream(is, radicalMap, prefixes, validOutputs, includeChars);
        Logger.d(TAG, "Loaded %d radical entries from overlay %s", count, name);
      } catch (Exception e) {
        Logger.e(TAG, e, "Failed to load radical overlay %s", name);
      }
      return;
    }
    // Binary or alternative text format, convert via importer then feed serialized form.
    try {
      Map<String, List<String>> parsed =
          com.anysoftkeyboard.dictionaries.radical.importers.RadicalTableImporter.parse(
              name, new java.io.ByteArrayInputStream(sniff));
      int count = 0;
      int skipped = 0;
      for (Map.Entry<String, List<String>> e : parsed.entrySet()) {
        String code = e.getKey().toLowerCase(java.util.Locale.ROOT);
        for (String ch : e.getValue()) {
          if (!valuePassesIncludeFilter(ch, includeChars)) {
            skipped++;
            continue;
          }
          if (mSplitMultiCodepointCandidates) {
            int idx = 0;
            int vlen = ch.length();
            while (idx < vlen) {
              int cp = ch.codePointAt(idx);
              int cpLen = Character.charCount(cp);
              String oneCandidate = ch.substring(idx, idx + cpLen);
              if (!valuePassesIncludeFilter(oneCandidate, includeChars)) {
                skipped++;
                idx += cpLen;
                continue;
              }
              addCandidate(radicalMap, code, oneCandidate);
              validOutputs.add(oneCandidate);
              idx += cpLen;
            }
          } else {
            addCandidate(radicalMap, code, ch);
            validOutputs.add(ch);
          }
          for (int i = 1; i <= code.length(); i++) {
            prefixes.add(code.substring(0, i));
          }
          count++;
        }
      }
      Logger.d(
          TAG,
          "Loaded %d radical entries from overlay %s (format=%s, skipped=%d by include filter)",
          count,
          name,
          fmt,
          skipped);
    } catch (Exception e) {
      Logger.e(TAG, e, "Failed to convert radical overlay %s", name);
    }
  }

  /**
   * Strips a leading UTF-8 BOM (U+FEFF) from the first line of a stream. Editors on Windows
   * (Notepad, Excel-as-TSV, many SCIM exports) emit BOM-prefixed UTF-8 by default; without this
   * {@link String#trim()} leaves the BOM in place (it is greater than U+0020) and the first key in
   * every user-supplied overlay table becomes silently unmatchable.
   */
  private static String stripBom(@NonNull String line) {
    if (!line.isEmpty() && line.charAt(0) == '\uFEFF') {
      return line.substring(1);
    }
    return line;
  }

  /**
   * API-23-safe replacement for {@code map.computeIfAbsent(key, k -> new ArrayList<>())}. {@link
   * Map#computeIfAbsent} is API 24+ while this module's minSdk is 23, and core-library desugaring
   * is not enabled for this project.
   */
  private static <K, V> List<V> bucket(@NonNull Map<K, List<V>> map, K key) {
    List<V> list = map.get(key);
    if (list == null) {
      list = new ArrayList<>();
      map.put(key, list);
    }
    return list;
  }

  /**
   * Appends {@code candidate} for {@code code} unless that exact candidate is already listed.
   *
   * <p>Sources overlap: a user can hand us several Boshiamy tables at once (liu-uni, liu-uni2,
   * liu-uni3, liu-uni4 all cover much of the same ground) and those merge on top of the bundled
   * table. Without this the candidate strip shows one copy per contributing file, e.g. "趟趟趟趟". The
   * first occurrence wins, so the bundled/curated order is preserved.
   *
   * @return true if the candidate was actually added.
   */
  private static boolean addCandidate(
      @NonNull Map<String, List<String>> radicalMap,
      @NonNull String code,
      @NonNull String candidate) {
    final List<String> list = bucket(radicalMap, code);
    // Candidate lists are short (a handful of entries), so a linear scan is cheaper than
    // maintaining a parallel Set for every code.
    if (list.contains(candidate)) return false;
    list.add(candidate);
    return true;
  }

  /**
   * API-23-safe replacement for {@code rank.getOrDefault(key, Integer.MAX_VALUE)}. {@link
   * Map#getOrDefault} is API 24+ while this module's minSdk is 23. Characters missing from the
   * frequency table sort last.
   */
  private static int rankOf(@NonNull Map<String, Integer> rank, String key) {
    Integer r = rank.get(key);
    return r == null ? Integer.MAX_VALUE : r;
  }

  /**
   * Sorts every candidate list in {@code radicalMap} by frequency rank, in place, once at load
   * time. Doing this here keeps {@link #getExactMatches} a pure lookup, so no work is repeated on
   * each keystroke.
   *
   * <p>No-op unless this dictionary opted into frequency ordering via {@code
   * splitMultiCodepointCandidates} (Cangjie / Zhuyin). Boshiamy deliberately preserves the source
   * file's curated order, because line 1 of a code is the author's primary character and is also
   * what the positional selector keys index into.
   */
  private void applyFrequencyOrdering(
      @NonNull Map<String, List<String>> radicalMap, @NonNull Map<String, Integer> rank) {
    if (!mSplitMultiCodepointCandidates || rank.isEmpty()) return;
    for (Map.Entry<String, List<String>> entry : radicalMap.entrySet()) {
      final List<String> candidates = entry.getValue();
      if (candidates == null || candidates.size() < 2) continue;
      // Collections.sort is stable (TimSort), so candidates absent from the frequency
      // table keep their original file order at the end.
      Collections.sort(candidates, (a, b) -> Integer.compare(rankOf(rank, a), rankOf(rank, b)));
    }
  }

  private static byte[] readAllBytes(@NonNull InputStream is) throws IOException {
    java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
    byte[] buf = new byte[8192];
    int n;
    while ((n = is.read(buf)) > 0) out.write(buf, 0, n);
    return out.toByteArray();
  }

  private void loadOverlayPhrases(
      @NonNull RadicalOverlay.StreamSupplier supplier,
      @NonNull HashMap<String, List<String>> phraseMap) {
    try (InputStream is = supplier.open()) {
      int count = parsePhrasesStream(is, phraseMap);
      Logger.d(TAG, "Loaded %d phrase associations from overlay", count);
    } catch (Exception e) {
      Logger.e(TAG, e, "Failed to load phrases overlay");
    }
  }

  private void loadOverlayHomophones(
      @NonNull RadicalOverlay.StreamSupplier supplier,
      @NonNull HashMap<String, List<String>> zhuyinToChars) {
    try (InputStream is = supplier.open()) {
      int count = parseHomophonesStream(is, zhuyinToChars);
      Logger.d(TAG, "Loaded %d homophone groups from overlay", count);
    } catch (Exception e) {
      Logger.e(TAG, e, "Failed to load homophones overlay");
    }
  }

  private void loadOverlayKeyValue(
      @NonNull RadicalOverlay.StreamSupplier supplier, @NonNull HashMap<String, String> map) {
    try (InputStream is = supplier.open()) {
      int count = parseKeyValueStream(is, map);
      Logger.d(TAG, "Loaded %d key-value entries from overlay", count);
    } catch (Exception e) {
      Logger.e(TAG, e, "Failed to load key-value overlay");
    }
  }

  private void loadOverlayCharFrequency(
      @NonNull RadicalOverlay.StreamSupplier supplier, @NonNull HashMap<String, Integer> rankMap) {
    try (InputStream is = supplier.open()) {
      int count = parseCharFrequencyStream(is, rankMap);
      Logger.d(TAG, "Loaded %d frequency-ranked chars from overlay", count);
    } catch (Exception e) {
      Logger.e(TAG, e, "Failed to load char-frequency overlay");
    }
  }

  private int[] resolveResourceIds(@NonNull Resources res, int resourceId) {
    if (resourceId <= 0) return null;

    final String resType = res.getResourceTypeName(resourceId);
    if ("raw".equalsIgnoreCase(resType)) {
      return new int[] {resourceId};
    } else {
      TypedArray array = res.obtainTypedArray(resourceId);
      try {
        int[] ids = new int[array.length()];
        for (int i = 0; i < array.length(); i++) {
          ids[i] = array.getResourceId(i, 0);
        }
        return ids;
      } finally {
        array.recycle();
      }
    }
  }

  private void loadResourceFile(
      @NonNull Resources res,
      int resId,
      @NonNull HashMap<String, List<String>> radicalMap,
      @NonNull HashSet<String> prefixes,
      @NonNull HashSet<String> validOutputs,
      @NonNull Set<String> includeChars) {
    try (InputStream is = res.openRawResource(resId)) {
      int count = parseRadicalStream(is, radicalMap, prefixes, validOutputs, includeChars);
      Logger.d(TAG, "Loaded %d radical entries from resource %d", count, resId);
    } catch (Exception e) {
      Logger.e(TAG, e, "Failed to load radical dictionary resource %d", resId);
    }
  }

  /**
   * Parses a tab-separated {@code radical\tcharacter} stream into the given collections. Returns
   * the number of entries successfully read. Safe to call multiple times to accumulate from several
   * sources (bundled + overlay).
   *
   * <p>{@code includeChars} acts as a per-keyboard whitelist: a row is skipped when any CJK or kana
   * codepoint in its value is absent from the set. Pass {@link Collections#emptySet()} to disable
   * the filter, which is what non-CJK callers and most tests do.
   */
  @VisibleForTesting
  int parseRadicalStream(
      @NonNull InputStream is,
      @NonNull HashMap<String, List<String>> radicalMap,
      @NonNull HashSet<String> prefixes,
      @NonNull HashSet<String> validOutputs)
      throws IOException {
    return parseRadicalStream(is, radicalMap, prefixes, validOutputs, Collections.emptySet());
  }

  @VisibleForTesting
  int parseRadicalStream(
      @NonNull InputStream is,
      @NonNull HashMap<String, List<String>> radicalMap,
      @NonNull HashSet<String> prefixes,
      @NonNull HashSet<String> validOutputs,
      @NonNull Set<String> includeChars)
      throws IOException {
    int lineCount = 0;
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (isClosed()) return lineCount;

        line = stripBom(line).trim();
        if (line.isEmpty() || line.startsWith("#")) continue;

        int tabIndex = line.indexOf('\t');
        if (tabIndex <= 0 || tabIndex >= line.length() - 1) continue;

        String radicals = line.substring(0, tabIndex).toLowerCase(java.util.Locale.ROOT);
        String character = line.substring(tabIndex + 1);

        if (mSplitMultiCodepointCandidates) {
          // For Cangjie/Zhuyin the source format is `key\tchar1char2char3`, one line per radical
          // key with all candidates concatenated. Split into one candidate per CJK codepoint so
          // they can be selected independently in the candidate strip and committed individually.
          int idx = 0;
          final int vlen = character.length();
          boolean addedAny = false;
          while (idx < vlen) {
            int cp = character.codePointAt(idx);
            int cpLen = Character.charCount(cp);
            String oneCandidate = character.substring(idx, idx + cpLen);
            if (valuePassesIncludeFilter(oneCandidate, includeChars)) {
              addCandidate(radicalMap, radicals, oneCandidate);
              validOutputs.add(oneCandidate);
              addedAny = true;
            }
            idx += cpLen;
          }
          if (!addedAny) continue;
        } else {
          if (!valuePassesIncludeFilter(character, includeChars)) continue;
          addCandidate(radicalMap, radicals, character);
          validOutputs.add(character);
        }

        // Store all prefixes for partial match detection
        for (int i = 1; i <= radicals.length(); i++) {
          prefixes.add(radicals.substring(0, i));
        }

        lineCount++;
      }
    }
    return lineCount;
  }

  /**
   * CJK / kana codepoint membership check for the per-keyboard include filter.
   *
   * <ul>
   *   <li>Empty {@code includeChars} disables the filter (everything passes).
   *   <li>CJK ideographs (Unified, Ext A–H, Compat) must be present in {@code includeChars}.
   *   <li>Hiragana and katakana codepoints must be present in {@code includeChars}, this is how the
   *       JP keyboard opts in (its char-set file includes the kana ranges), while trad/simp
   *       keyboards naturally reject kana since their char-sets are CJK-only.
   *   <li>All other codepoints (ASCII, punctuation, emoji, kaomoji symbols) pass, they are
   *       script-neutral.
   * </ul>
   */
  @VisibleForTesting
  static boolean valuePassesIncludeFilter(
      @NonNull String value, @NonNull Set<String> includeChars) {
    if (includeChars.isEmpty()) return true;
    final int len = value.length();
    int i = 0;
    while (i < len) {
      int cp = value.codePointAt(i);
      int cpLen = Character.charCount(cp);
      if (isCjkOrKanaCodepoint(cp)) {
        if (!includeChars.contains(new String(Character.toChars(cp)))) return false;
      }
      i += cpLen;
    }
    return true;
  }

  /**
   * Per-overlay decision for which include set to use. {@code liu.box} is the user's personal
   * additions file in McBopomofo/嘸蝦米, those entries are exempt from the per-keyboard script filter
   * so the user's custom characters and phrases appear on every Boshiamy keyboard regardless of
   * script. Every other overlay source (e.g. external {@code *.tab}, {@code *.cin}, {@code *.yaml})
   * is filtered just like the bundled main table.
   */
  @NonNull
  private static Set<String> includeCharsFor(
      @NonNull RadicalOverlay.StreamSupplier supplier, @NonNull Set<String> includeChars) {
    final String name = supplier.name();
    if (name != null && "liu.box".equalsIgnoreCase(name)) {
      return Collections.emptySet();
    }
    return includeChars;
  }

  private static boolean isCjkOrKanaCodepoint(int cp) {
    return (cp >= 0x3400 && cp <= 0x4DBF) // CJK Ext A
        || (cp >= 0x4E00 && cp <= 0x9FFF) // CJK Unified
        || (cp >= 0xF900 && cp <= 0xFAFF) // CJK Compat Ideographs
        || (cp >= 0x20000 && cp <= 0x2FFFF) // CJK Ext B/C/D/E/F
        || (cp >= 0x30000 && cp <= 0x3134F) // CJK Ext G/H
        || (cp >= 0x3040 && cp <= 0x309F) // Hiragana
        || (cp >= 0x30A0 && cp <= 0x30FF) // Katakana
        || (cp >= 0x31F0 && cp <= 0x31FF); // Katakana Phonetic Extensions
  }

  private void loadPhrasesFile(
      @NonNull Resources res, int resId, @NonNull HashMap<String, List<String>> phraseMap) {
    try (InputStream is = res.openRawResource(resId)) {
      int count = parsePhrasesStream(is, phraseMap);
      Logger.d(TAG, "Loaded %d phrase associations from resource %d", count, resId);
    } catch (Exception e) {
      Logger.e(TAG, e, "Failed to load phrases resource %d", resId);
    }
  }

  @VisibleForTesting
  int parsePhrasesStream(@NonNull InputStream is, @NonNull HashMap<String, List<String>> phraseMap)
      throws IOException {
    int lineCount = 0;
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (isClosed()) return lineCount;

        line = stripBom(line).trim();
        if (line.isEmpty() || line.startsWith("#")) continue;

        int tabIndex = line.indexOf('\t');
        if (tabIndex <= 0 || tabIndex >= line.length() - 1) continue;

        String trigger = line.substring(0, tabIndex);
        String nextChars = line.substring(tabIndex + 1);

        // Bundled and user-supplied phrase files merge, so drop repeats the same way the
        // radical map does.
        addCandidate(phraseMap, trigger, nextChars);
        lineCount++;
      }
    }
    return lineCount;
  }

  private void loadHomophonesFile(
      @NonNull Resources res, int resId, @NonNull HashMap<String, List<String>> zhuyinToChars) {
    try (InputStream is = res.openRawResource(resId)) {
      int count = parseHomophonesStream(is, zhuyinToChars);
      Logger.d(TAG, "Loaded %d homophone groups from resource %d", count, resId);
    } catch (Exception e) {
      Logger.e(TAG, e, "Failed to load homophones resource %d", resId);
    }
  }

  @VisibleForTesting
  int parseHomophonesStream(
      @NonNull InputStream is, @NonNull HashMap<String, List<String>> zhuyinToChars)
      throws IOException {
    int lineCount = 0;
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (isClosed()) return lineCount;
        line = stripBom(line).trim();
        if (line.isEmpty() || line.startsWith("#")) continue;
        int tabIndex = line.indexOf('\t');
        if (tabIndex <= 0 || tabIndex >= line.length() - 1) continue;
        String zhuyin = line.substring(0, tabIndex);
        String chars = line.substring(tabIndex + 1);
        ArrayList<String> charList = new ArrayList<>();
        for (int i = 0; i < chars.length(); ) {
          int cp = chars.codePointAt(i);
          charList.add(new String(Character.toChars(cp)));
          i += Character.charCount(cp);
        }
        zhuyinToChars.put(zhuyin, charList);
        lineCount++;
      }
    }
    return lineCount;
  }

  private void loadKeyValueFile(
      @NonNull Resources res, int resId, @NonNull HashMap<String, String> map) {
    try (InputStream is = res.openRawResource(resId)) {
      int count = parseKeyValueStream(is, map);
      Logger.d(TAG, "Loaded %d key-value entries from resource %d", count, resId);
    } catch (Exception e) {
      Logger.e(TAG, e, "Failed to load key-value resource %d", resId);
    }
  }

  @VisibleForTesting
  int parseKeyValueStream(@NonNull InputStream is, @NonNull HashMap<String, String> map)
      throws IOException {
    int lineCount = 0;
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (isClosed()) return lineCount;
        line = stripBom(line).trim();
        if (line.isEmpty() || line.startsWith("#")) continue;
        int tabIndex = line.indexOf('\t');
        if (tabIndex <= 0 || tabIndex >= line.length() - 1) continue;
        String key = line.substring(0, tabIndex);
        String value = line.substring(tabIndex + 1);
        // Map#putIfAbsent is API 24+ and minSdk is 23: keep the first mapping wins semantics.
        if (!map.containsKey(key)) {
          map.put(key, value);
        }
        lineCount++;
      }
    }
    return lineCount;
  }

  /**
   * Loads the first whitespace/tab-delimited column from each line of a resource file into the
   * given set. Lines starting with '#' and blank lines are ignored. Used for OpenCC-style
   * conversion tables where the key column is the simp (or trad) character we want to filter.
   *
   * <p>OpenCC's {@code STCharacters.txt} / {@code TSCharacters.txt} are 1-to-N mappings: the key is
   * the source variant and the value column lists the target variants. When the source character
   * itself appears in its own target list, the character is shared between scripts (it is valid in
   * both Simplified and Traditional). In that case we must NOT add it to the exclude set, otherwise
   * typing radicals that produce a shared character (e.g. 台) would filter it out from results in
   * the other variant's keyboard.
   */
  private void loadFirstColumnFile(
      @NonNull Resources res, int resId, @NonNull HashSet<String> set) {
    try (InputStream is = res.openRawResource(resId);
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
      String line;
      int lineCount = 0;
      while ((line = reader.readLine()) != null) {
        if (isClosed()) return;
        line = stripBom(line).trim();
        if (line.isEmpty() || line.startsWith("#")) continue;
        int sep = -1;
        for (int i = 0; i < line.length(); i++) {
          char c = line.charAt(i);
          if (c == '\t' || c == ' ') {
            sep = i;
            break;
          }
        }
        String key = sep > 0 ? line.substring(0, sep) : line;
        if (key.isEmpty()) continue;
        // Skip self-mapping entries: if the key appears among the target
        // variants, the character is shared between scripts and is valid in
        // both, do not add it to the exclude set.
        if (sep > 0) {
          String values = line.substring(sep + 1);
          if (containsCodePointSequence(values, key)) continue;
        }
        set.add(key);
        lineCount++;
      }
      Logger.d(TAG, "Loaded %d filter entries from resource %d", lineCount, resId);
    } catch (Exception e) {
      Logger.e(TAG, e, "Failed to load filter resource %d", resId);
    }
  }

  /** Returns true if {@code haystack} contains {@code needle} as a token (codepoint sequence). */
  private static boolean containsCodePointSequence(String haystack, String needle) {
    if (needle.isEmpty()) return false;
    int i = 0;
    final int hLen = haystack.length();
    final int nLen = needle.length();
    while (i + nLen <= hLen) {
      if (haystack.regionMatches(i, needle, 0, nLen)) return true;
      i += Character.charCount(haystack.codePointAt(i));
    }
    return false;
  }

  private void loadCharFrequencyFile(
      @NonNull Resources res, int resId, @NonNull HashMap<String, Integer> rankMap) {
    try (InputStream is = res.openRawResource(resId)) {
      int count = parseCharFrequencyStream(is, rankMap);
      Logger.d(TAG, "Loaded %d frequency-ranked chars from resource %d", count, resId);
    } catch (Exception e) {
      Logger.e(TAG, e, "Failed to load char frequency resource %d", resId);
    }
  }

  @VisibleForTesting
  int parseCharFrequencyStream(@NonNull InputStream is, @NonNull HashMap<String, Integer> rankMap)
      throws IOException {
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
      // The file is one logical sequence of CJK characters in descending
      // frequency order. Newlines are ignored.
      int rank = rankMap.size();
      String line;
      while ((line = reader.readLine()) != null) {
        if (isClosed()) return rankMap.size();
        int i = 0;
        final int len = line.length();
        while (i < len) {
          int cp = line.codePointAt(i);
          int charLen = Character.charCount(cp);
          if (cp >= 0x3400 && cp <= 0x9FFF) {
            String key = new String(Character.toChars(cp));
            if (!rankMap.containsKey(key)) {
              rankMap.put(key, rank++);
            }
          }
          i += charLen;
        }
      }
    }
    return rankMap.size();
  }

  /**
   * Looks up homophones for a character. Finds the Zhuyin pronunciation, then returns all
   * characters with the same pronunciation.
   *
   * <p>Results are filtered to characters that this dictionary actually knows how to type (i.e.
   * have a known radical code) and to the dictionary's variant (e.g. Traditional). They are then
   * sorted by frequency rank (most common first), with the originally-queried character always
   * first if it survives filtering.
   *
   * @param character the character to look up homophones for
   * @return list of homophone characters, or empty list
   */
  @NonNull
  public List<String> getHomophones(@NonNull String character) {
    final RadicalData data = mData;
    String zhuyin = data.charToZhuyin.get(character);
    if (zhuyin == null) return Collections.emptyList();
    List<String> homophones = data.zhuyinToChars.get(zhuyin);
    if (homophones == null || homophones.isEmpty()) return Collections.emptyList();
    final Set<String> exclude = data.excludeHomophoneChars;
    final Map<String, String> radicalCodes = data.charToRadical;
    final boolean filterUntypable = !radicalCodes.isEmpty();
    final List<String> filtered = new ArrayList<>(homophones.size());
    for (String h : homophones) {
      if (!exclude.isEmpty() && exclude.contains(h)) continue;
      // Drop characters this dictionary cannot type, without a radical code
      // there is no way for the user to enter them, so they are noise in the
      // candidate list.
      if (filterUntypable && !radicalCodes.containsKey(h)) continue;
      filtered.add(h);
    }
    if (filtered.size() <= 1) return filtered;
    final Map<String, Integer> rank = data.charFrequencyRank;
    if (!rank.isEmpty()) {
      Collections.sort(
          filtered,
          (a, b) -> {
            int ra = rankOf(rank, a);
            int rb = rankOf(rank, b);
            if (ra != rb) return Integer.compare(ra, rb);
            return a.compareTo(b);
          });
    }
    return filtered;
  }

  /** Returns the Zhuyin pronunciation for a character, or null if unknown. */
  @Nullable
  public String getZhuyin(@NonNull String character) {
    return mData.charToZhuyin.get(character);
  }

  /** Returns the Boshiamy radical code for a character, or null if unknown. */
  @Nullable
  public String getRadicalCode(@NonNull String character) {
    return mData.charToRadical.get(character);
  }

  /** Returns true if homophone data is loaded and available. */
  public boolean hasHomophoneData() {
    return !mData.charToZhuyin.isEmpty();
  }

  /**
   * Returns next-word suggestions for the given character/word.
   *
   * @param previousWord the previously committed character/word
   * @return list of suggested continuations, or empty list
   */
  @NonNull
  public List<String> getNextWordSuggestions(@NonNull String previousWord) {
    final RadicalData data = mData;
    // Try exact match first
    List<String> suggestions = data.phraseMap.get(previousWord);
    if (suggestions != null) return Collections.unmodifiableList(suggestions);
    // For multi-char words, try the last character as trigger
    if (previousWord.length() > 1) {
      String lastChar =
          previousWord.substring(
              previousWord.length()
                  - Character.charCount(previousWord.codePointBefore(previousWord.length())));
      suggestions = data.phraseMap.get(lastChar);
      if (suggestions != null) return Collections.unmodifiableList(suggestions);
    }
    return Collections.emptyList();
  }

  /**
   * Returns exact matches for the given radical sequence.
   *
   * <p>The per-keyboard include filter is applied at parse time (both bundled and overlay rows), so
   * this method is now a pure lookup against the pre-filtered radical map; it does not run any
   * variant filtering or fallback recovery. Cangjie/Zhuyin still get a stable frequency sort via
   * {@link #mSplitMultiCodepointCandidates}.
   *
   * @param radicals the radical key sequence (lowercase)
   * @return list of matching characters/words, or empty list if no match
   */
  @NonNull
  public List<String> getExactMatches(@NonNull String radicals) {
    final RadicalData data = mData;
    List<String> matches = data.radicalMap.get(radicals.toLowerCase(java.util.Locale.ROOT));
    if (matches == null || matches.isEmpty()) return Collections.emptyList();
    // Pure lookup: candidate order was already resolved when the table was built
    // (see applyFrequencyOrdering), so nothing is recomputed per keystroke.
    return Collections.unmodifiableList(matches);
  }

  /** Returns true if a character is excluded for this dictionary's variant. */
  public boolean isExcludedChar(@NonNull String character) {
    final Set<String> exclude = mData.excludeHomophoneChars;
    return !exclude.isEmpty() && exclude.contains(character);
  }

  /**
   * Checks if the given radical sequence is a prefix of any valid radical sequence.
   *
   * @param radicals the radical key sequence (lowercase)
   * @return true if there are potential matches with more keystrokes
   */
  public boolean hasPartialMatch(@NonNull String radicals) {
    return mData.prefixes.contains(radicals.toLowerCase(java.util.Locale.ROOT));
  }

  @Override
  public void getSuggestions(
      @NonNull final KeyCodesProvider composer, @NonNull final WordCallback callback) {
    if (isLoading()) return;

    final CharSequence typedWord = composer.getTypedWord();
    if (typedWord == null || typedWord.length() == 0) return;

    final String radicals = typedWord.toString().toLowerCase(java.util.Locale.ROOT);
    final List<String> matches = getExactMatches(radicals);

    // Add exact matches with high frequency
    int frequency = MAX_WORD_FREQUENCY;
    for (String match : matches) {
      char[] chars = match.toCharArray();
      if (!callback.addWord(chars, 0, chars.length, frequency, this)) {
        return;
      }
      frequency = Math.max(1, frequency - 1);
    }
  }

  @Override
  public boolean isValidWord(@NonNull CharSequence word) {
    return mData.validOutputs.contains(word.toString());
  }

  @Override
  public void getLoadedWords(@NonNull GetWordsCallback callback) {
    // Radical dictionaries don't provide loaded words in the traditional sense
    callback.onGetWordsFinished(new char[0][], new int[0]);
  }

  @Override
  protected void closeAllResources() {
    mData = EMPTY_DATA;
  }

  /**
   * Adds a single radical mapping programmatically. This is only safe to call during {@link
   * #loadAllResources()}, which runs under synchronization from {@link
   * Dictionary#loadDictionary()}. The primary use case is in test subclasses.
   *
   * <p>Always deep-copies the previously-published collections before mutating, so that any reader
   * still holding a reference to the prior {@link RadicalData} snapshot continues to see a
   * consistent (frozen) view. This makes the publish-via-volatile invariant honest at the cost of
   * one extra map/set copy per mapping, negligible for a test-only path.
   */
  protected void addRadicalMapping(@NonNull String radicals, @NonNull String character) {
    RadicalData current = mData;
    HashMap<String, List<String>> map = new HashMap<>(current.radicalMap);
    final String lower = radicals.toLowerCase(java.util.Locale.ROOT);
    List<String> existing = map.get(lower);
    List<String> next = existing == null ? new ArrayList<>() : new ArrayList<>(existing);
    next.add(character);
    map.put(lower, next);

    HashSet<String> prefixes = new HashSet<>(current.prefixes);
    for (int i = 1; i <= lower.length(); i++) {
      prefixes.add(lower.substring(0, i));
    }

    HashSet<String> outputs = new HashSet<>(current.validOutputs);
    outputs.add(character);

    // Publish atomically
    mData =
        new RadicalData(
            map,
            prefixes,
            outputs,
            current.phraseMap,
            current.charToZhuyin,
            current.zhuyinToChars,
            current.charToRadical,
            current.excludeHomophoneChars,
            current.includeChars,
            current.charFrequencyRank);
  }

  /**
   * Installs homophone-related and frequency data programmatically. Intended for test subclasses
   * that bypass resource loading. Caller must have populated radical mappings via {@link
   * #addRadicalMapping(String, String)} first if filtering by typability is desired.
   *
   * <p>The {@code synchronized} keyword serializes concurrent installer calls from tests; reader
   * visibility is guaranteed independently by {@link #mData} being {@code volatile} (each reader
   * snapshots {@code mData} once and reads from that immutable wrapper). Tests should not call this
   * concurrently with {@link #loadAllResources()}; the only safe race-free pattern is "load
   * resources, then install test data" on the same thread.
   */
  @VisibleForTesting(otherwise = VisibleForTesting.NONE)
  protected synchronized void installHomophoneData(
      @NonNull Map<String, String> charToZhuyin,
      @NonNull Map<String, List<String>> zhuyinToChars,
      @NonNull Map<String, String> charToRadical,
      @NonNull Set<String> excludeHomophoneChars,
      @NonNull Map<String, Integer> charFrequencyRank) {
    RadicalData current = mData;
    // Frequency data arrives after the mappings in the test flow, so re-apply the
    // load-time ordering here; production does this inside loadAllResources.
    Map<String, List<String>> ordered = new HashMap<>();
    for (Map.Entry<String, List<String>> e : current.radicalMap.entrySet()) {
      ordered.put(e.getKey(), new ArrayList<>(e.getValue()));
    }
    applyFrequencyOrdering(ordered, charFrequencyRank);
    mData =
        new RadicalData(
            ordered,
            current.prefixes,
            current.validOutputs,
            current.phraseMap,
            charToZhuyin,
            zhuyinToChars,
            charToRadical,
            excludeHomophoneChars,
            current.includeChars,
            charFrequencyRank);
  }
}
