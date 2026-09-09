/*
 * Copyright (c) 2026 AnySoftKeyboard contributors
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

package com.anysoftkeyboard.dictionaries.radical.importers;

import androidx.annotation.NonNull;
import com.anysoftkeyboard.base.utils.Logger;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses radical-input tables in one of three open formats:
 *
 * <ul>
 *   <li><b>IBus / OpenVanilla {@code .tab}</b>, bare {@code <code>\t<char>} lines, one mapping per
 *       line. May contain {@code #}-comments and blank lines.
 *   <li><b>SCIM {@code .cin}</b>, header directives followed by {@code %chardef begin} ... {@code
 *       %chardef end} block containing {@code <code> <char>} (whitespace-separated) lines.
 *   <li><b>RIME {@code *.dict.yaml}</b>, a YAML preamble terminated by a line {@code ---}, followed
 *       by lines {@code <text>\t<code>[\t<weight>]}. Note that RIME stores {@code text\tcode},
 *       which is the <em>inverse</em> of the addon's internal {@code code\ttext} format; this
 *       parser flips the columns.
 * </ul>
 *
 * <p>All parsers emit a unified {@code code -> [chars...]} mapping that can be serialized via
 * {@link #write(Map, OutputStream)} into the addon's bundled tab-separated raw-resource format.
 */
public final class RadicalTableImporter {

  private static final String TAG = "RadicalTableImporter";

  /**
   * Upper bound on entries accepted from a BoshiamyTIP binary table. The real {@code liu-uni*}
   * character tables carry roughly 30k entries; anything far above that means the decoder is being
   * pointed at a file with a different internal layout.
   */
  private static final int MAX_LIU_BIN_ENTRIES = 200_000;

  private RadicalTableImporter() {}

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

  /** Source format dispatched on file extension. */
  public enum Format {
    IBUS_TAB,
    SCIM_CIN,
    RIME_DICT_YAML,
    BOSHIAMY_LIU_BIN,
    BOSHIAMY_LIU_BOX;

    /**
     * Detects the format from a file name. Defaults to {@link #IBUS_TAB} (the simplest format) if
     * the extension is unrecognized. Note that {@code .tab} can be either the plain IBus text
     * format <em>or</em> the BoshiamyTIP binary format (file names {@code liu*.tab}, {@code
     * liu-uni*.tab}, {@code liubox.tab}, {@code liuext.tab}). Use {@link
     * #detectFromContentAndName(String, byte[])} for correct disambiguation; this method always
     * returns {@link #IBUS_TAB} for {@code .tab}.
     */
    @NonNull
    public static Format detect(@NonNull String fileName) {
      String lower = fileName.toLowerCase(java.util.Locale.ROOT);
      if (lower.endsWith(".cin")) return SCIM_CIN;
      if (lower.endsWith(".dict.yaml") || lower.endsWith(".yaml")) return RIME_DICT_YAML;
      if (lower.endsWith(".box")) return BOSHIAMY_LIU_BOX;
      return IBUS_TAB;
    }

    /**
     * Like {@link #detect(String)} but also sniffs the first bytes of the file to recognize the
     * BoshiamyTIP binary {@code .tab} format. A file is classified as {@link #BOSHIAMY_LIU_BIN}
     * when (a) its name matches the heuristic for a Boshiamy binary table ({@code liu*.tab},
     * including {@code liu-uni*.tab}, {@code liubox.tab}, {@code liuext.tab}) <em>or</em> (b) the
     * first 64 bytes contain a NUL byte (text formats never do).
     */
    @NonNull
    public static Format detectFromContentAndName(
        @NonNull String fileName, @NonNull byte[] firstBytes) {
      String lower = fileName.toLowerCase(java.util.Locale.ROOT);
      if (lower.endsWith(".cin")) return SCIM_CIN;
      if (lower.endsWith(".dict.yaml") || lower.endsWith(".yaml")) return RIME_DICT_YAML;
      if (lower.endsWith(".box")) return BOSHIAMY_LIU_BOX;
      boolean nameLooksBinary =
          lower.endsWith(".tab")
              && (lower.startsWith("liu") || lower.contains("/liu") || lower.contains("\\liu"));
      boolean contentLooksBinary = false;
      int probe = Math.min(64, firstBytes.length);
      for (int i = 0; i < probe; i++) {
        if (firstBytes[i] == 0) {
          contentLooksBinary = true;
          break;
        }
      }
      if (nameLooksBinary || contentLooksBinary) return BOSHIAMY_LIU_BIN;
      return IBUS_TAB;
    }
  }

  /** Convenience dispatch on file name. Content is sniffed to disambiguate {@code .tab}. */
  @NonNull
  public static Map<String, List<String>> parse(@NonNull String fileName, @NonNull InputStream is)
      throws IOException {
    // We need to peek at the first bytes for binary detection. Buffer the entire stream, these
    // tables top out at ~1 MB so memory cost is acceptable, and binary decoding requires random
    // access anyway.
    byte[] data = readAll(is);
    Format fmt = Format.detectFromContentAndName(fileName, data);
    switch (fmt) {
      case SCIM_CIN:
        return parseScimCin(new java.io.ByteArrayInputStream(data));
      case RIME_DICT_YAML:
        return parseRimeDictYaml(new java.io.ByteArrayInputStream(data));
      case BOSHIAMY_LIU_BIN:
        return parseBoshiamyLiuBin(data);
      case BOSHIAMY_LIU_BOX:
        return parseLiuBoxText(new java.io.ByteArrayInputStream(data));
      case IBUS_TAB:
      default:
        return parseIbusTab(new java.io.ByteArrayInputStream(data));
    }
  }

  private static byte[] readAll(InputStream is) throws IOException {
    java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
    byte[] buf = new byte[8192];
    int n;
    while ((n = is.read(buf)) > 0) out.write(buf, 0, n);
    return out.toByteArray();
  }

  /**
   * Parses an IBus {@code .tab} stream. Lines are {@code <code>\t<char(s)>}. A line with multiple
   * tab-separated tokens after the code is treated as multiple candidates for that code.
   */
  @NonNull
  public static Map<String, List<String>> parseIbusTab(@NonNull InputStream is) throws IOException {
    Map<String, List<String>> out = new LinkedHashMap<>();
    try (BufferedReader r = newReader(is)) {
      String line;
      while ((line = r.readLine()) != null) {
        line = stripBom(line);
        line = trimRight(line);
        if (line.isEmpty() || line.charAt(0) == '#') continue;
        int tab = line.indexOf('\t');
        if (tab <= 0 || tab >= line.length() - 1) continue;
        String code = line.substring(0, tab).trim();
        if (code.isEmpty()) continue;
        String rest = line.substring(tab + 1);
        for (String candidate : rest.split("\t")) {
          String c = candidate.trim();
          if (!c.isEmpty()) {
            bucket(out, code).add(c);
          }
        }
      }
    }
    return out;
  }

  /**
   * Parses a SCIM {@code .cin} stream. Only the {@code %chardef begin} ... {@code %chardef end}
   * block is consumed; everything else (such as {@code %keyname}, {@code %selkey}, {@code %ename})
   * is ignored. Inside the chardef block, each line is {@code <code><whitespace><char>}.
   */
  @NonNull
  public static Map<String, List<String>> parseScimCin(@NonNull InputStream is) throws IOException {
    Map<String, List<String>> out = new LinkedHashMap<>();
    try (BufferedReader r = newReader(is)) {
      String line;
      boolean inChardef = false;
      while ((line = r.readLine()) != null) {
        line = stripBom(line);
        line = trimRight(line);
        String trimmed = line.trim();
        if (trimmed.isEmpty() || trimmed.charAt(0) == '#') continue;
        if (trimmed.startsWith("%chardef")) {
          String tail =
              trimmed.substring("%chardef".length()).trim().toLowerCase(java.util.Locale.ROOT);
          if (tail.startsWith("begin")) {
            inChardef = true;
          } else if (tail.startsWith("end")) {
            inChardef = false;
          }
          continue;
        }
        if (!inChardef) continue;
        int sep = -1;
        for (int i = 0; i < trimmed.length(); i++) {
          char c = trimmed.charAt(i);
          if (c == ' ' || c == '\t') {
            sep = i;
            break;
          }
        }
        if (sep <= 0 || sep >= trimmed.length() - 1) continue;
        String code = trimmed.substring(0, sep);
        String chars = trimmed.substring(sep + 1).trim();
        if (chars.isEmpty()) continue;
        bucket(out, code).add(chars);
      }
    }
    return out;
  }

  /**
   * Parses an OpenVanilla/Liu {@code .box} (a.k.a. {@code liu.box}) user-additions text file. Each
   * line is {@code <code><whitespace><value>} where {@code <whitespace>} is one or more spaces or
   * tabs and {@code <value>} is a single character, phrase, emoji, kaomoji, etc. The code may
   * contain any of {@code A-Z a-z 0-9 , . ' [ ] - / ;} (typically the layout's typeable keys).
   * Lines beginning with {@code #} or {@code %} (the latter being directive lines such as {@code
   * %selkey} carried over from CIN-style configs) are skipped, as are blank lines.
   *
   * <p>This is strictly more permissive than {@link #parseIbusTab(InputStream)} (which requires a
   * tab separator) and is used only when the file's extension is {@code .box}.
   */
  @NonNull
  public static Map<String, List<String>> parseLiuBoxText(@NonNull InputStream is)
      throws IOException {
    Map<String, List<String>> out = new LinkedHashMap<>();
    try (BufferedReader r = newReader(is)) {
      String line;
      while ((line = r.readLine()) != null) {
        line = stripBom(line);
        line = trimRight(line);
        String trimmed = line.trim();
        if (trimmed.isEmpty()) continue;
        char first = trimmed.charAt(0);
        if (first == '#' || first == '%') continue;
        int sep = -1;
        for (int i = 0; i < trimmed.length(); i++) {
          char c = trimmed.charAt(i);
          if (c == ' ' || c == '\t') {
            sep = i;
            break;
          }
        }
        if (sep <= 0 || sep >= trimmed.length() - 1) continue;
        String code = trimmed.substring(0, sep);
        String value = trimmed.substring(sep + 1).trim();
        if (value.isEmpty()) continue;
        bucket(out, code).add(value);
      }
    }
    return out;
  }

  /**
   * Parses a RIME {@code *.dict.yaml} stream. Skips header up to and including the YAML document
   * separator {@code ---}; thereafter accepts {@code <text>\t<code>[\t<weight>]} lines and inverts
   * to {@code code -> [text...]}.
   *
   * <p>If a weight column is present, entries are emitted in descending weight order within a given
   * code group. Otherwise insertion order is preserved.
   */
  @NonNull
  public static Map<String, List<String>> parseRimeDictYaml(@NonNull InputStream is)
      throws IOException {
    Map<String, List<WeightedEntry>> grouped = new LinkedHashMap<>();
    try (BufferedReader r = newReader(is)) {
      String line;
      boolean inData = false;
      while ((line = r.readLine()) != null) {
        line = stripBom(line);
        line = trimRight(line);
        String trimmed = line.trim();
        if (!inData) {
          if (trimmed.equals("---") || trimmed.equals("...")) {
            inData = true;
          }
          continue;
        }
        if (trimmed.isEmpty() || trimmed.charAt(0) == '#') continue;
        int tab = line.indexOf('\t');
        if (tab <= 0 || tab >= line.length() - 1) continue;
        String text = line.substring(0, tab).trim();
        String rest = line.substring(tab + 1);
        int tab2 = rest.indexOf('\t');
        String code;
        double weight = 0.0;
        if (tab2 < 0) {
          code = rest.trim();
        } else {
          code = rest.substring(0, tab2).trim();
          String w = rest.substring(tab2 + 1).trim();
          try {
            weight = Double.parseDouble(w);
          } catch (NumberFormatException ignored) {
            weight = 0.0;
          }
        }
        if (text.isEmpty() || code.isEmpty()) continue;
        bucket(grouped, code).add(new WeightedEntry(text, weight));
      }
    }
    Map<String, List<String>> out = new LinkedHashMap<>();
    for (Map.Entry<String, List<WeightedEntry>> e : grouped.entrySet()) {
      List<WeightedEntry> entries = e.getValue();
      Collections.sort(entries, (a, b) -> Double.compare(b.weight, a.weight));
      List<String> texts = new ArrayList<>(entries.size());
      for (WeightedEntry we : entries) texts.add(we.text);
      out.put(e.getKey(), texts);
    }
    return out;
  }

  /**
   * Serializes a parsed code -> chars map into the addon's bundled raw-resource format: one {@code
   * <code>\t<char>\n} line per (code, char) pair.
   */
  public static void write(@NonNull Map<String, List<String>> table, @NonNull OutputStream os)
      throws IOException {
    Writer w = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
    try {
      for (Map.Entry<String, List<String>> e : table.entrySet()) {
        String code = e.getKey();
        for (String ch : e.getValue()) {
          w.write(code);
          w.write('\t');
          w.write(ch);
          w.write('\n');
        }
      }
    } finally {
      w.flush();
    }
  }

  /**
   * Decodes a BoshiamyTIP binary {@code .tab} file (e.g. {@code liu1.tab}, {@code liu-uni*.tab},
   * {@code liubox.tab}, {@code liuext.tab}). Mirrors the Python reference port of <a
   * href="https://github.com/shadowjohn/UCL_LIU/blob/master/tools/uni2txt.php">uni2txt.php</a>.
   *
   * <p>The file format is a fixed 32-character root-key alphabet ({@code "
   * abcdefghijklmnopqrstuvwxyz,.'[]"}) indexing into a header table and a packed 24-bit data array;
   * each entry encodes a (4-key, BMP-codepoint) pair.
   *
   * <p>Returns an empty map for files that fail header sanity checks (rather than throwing),
   * matching the philosophy of the other parsers in this class which tolerate garbage input.
   */
  @NonNull
  public static Map<String, List<String>> parseBoshiamyLiuBin(@NonNull byte[] data) {
    Map<String, List<String>> out = new LinkedHashMap<>();
    final String rootkey = " abcdefghijklmnopqrstuvwxyz,.'[]";
    int emitted = 0;
    if (data.length < 8) return out;
    int i1 = uint16Le(data, 0);
    // int words = uint16Le(data, 4); // total word count, informational, unused
    int hdr2 = uint16Le(data, 2);
    int hdr6 = uint16Le(data, 6);
    int i2 = i1 + hdr2;
    int i3 = i2 + hdr6;
    int i4 = i3 + hdr6;
    if (i4 < 0 || i4 >= data.length) return out;

    for (int i = 0; i < 1024; i++) {
      char key0 = rootkey.charAt(i / 32);
      char key1 = rootkey.charAt(i % 32);
      if (key0 == ' ') continue;

      int startCi = uint16Le(data, i * 2);
      int endCi = uint16Le(data, i * 2 + 2);
      if (endCi < startCi) continue;

      for (int ci = startCi; ci < endCi; ci++) {
        int bit24 = getBitsBigEndianByteAligned(data, i4, 24, ci);
        if (bit24 < 0) continue;
        int hi = getBitsPacked(data, i1, 2, ci);
        if (hi < 0) continue;
        int lo = bit24 & 0x3FFF;
        char key2 = rootkey.charAt((bit24 >> 19) & 0x1F);
        char key3 = rootkey.charAt((bit24 >> 14) & 0x1F);

        StringBuilder kb = new StringBuilder(4);
        kb.append(key0).append(key1).append(key2).append(key3);
        // Strip spaces (placeholder keys).
        String key = stripSpaces(kb);
        if (key.isEmpty()) continue;

        int codepoint = (hi << 14) | lo;
        // Only emit valid BMP scalar codepoints (this format cannot encode astral planes).
        if (codepoint < 0x20 || codepoint > 0xFFFF) continue;
        if (codepoint >= 0xD800 && codepoint <= 0xDFFF) continue; // surrogate
        String ch = String.valueOf((char) codepoint);
        bucket(out, key).add(ch);
        if (++emitted > MAX_LIU_BIN_ENTRIES) {
          // Not a character table (or a corrupt one): the real liu-uni* tables hold on the
          // order of 30k entries, whereas running this decoder over a different Boshiamy
          // file such as liuext.tab/liuphon.tab yields millions of junk rows that would
          // swamp the radical map. Refuse the file rather than poison the dictionary.
          Logger.w(
              TAG,
              "Refusing Boshiamy binary table: more than %d entries, this is not a"
                  + " character table",
              MAX_LIU_BIN_ENTRIES);
          return new LinkedHashMap<>();
        }
      }
    }
    return out;
  }

  private static int uint16Le(byte[] data, int addr) {
    if (addr < 0 || addr + 1 >= data.length) return 0;
    return (data[addr] & 0xFF) | ((data[addr + 1] & 0xFF) << 8);
  }

  /**
   * Reads {@code nbit} bits (must be 1, 2, or 4) from a packed bitstream starting at byte {@code
   * start}, returning entry index {@code i}. Matches the Python reference's bit ordering (MSB-first
   * within each byte). Returns -1 if the read would go out of bounds.
   */
  private static int getBitsPacked(byte[] data, int start, int nbit, int i) {
    if (!(nbit == 1 || nbit == 2 || nbit == 4)) {
      throw new IllegalArgumentException("nbit must be 1, 2 or 4: " + nbit);
    }
    int byteAddr = start + (i * nbit) / 8;
    if (byteAddr < 0 || byteAddr >= data.length) return -1;
    int shift = 8 - nbit - ((i * nbit) % 8);
    int mask = (1 << nbit) - 1;
    return ((data[byteAddr] & 0xFF) >> shift) & mask;
  }

  /**
   * Reads {@code nbit} bits (must be a positive multiple of 8) from a byte-aligned big-endian value
   * array starting at byte {@code start}, entry index {@code i}. Returns -1 if the read would go
   * out of bounds.
   */
  private static int getBitsBigEndianByteAligned(byte[] data, int start, int nbit, int i) {
    if (nbit <= 0 || nbit % 8 != 0) {
      throw new IllegalArgumentException("nbit must be a positive multiple of 8: " + nbit);
    }
    int nbyte = nbit / 8;
    int addr = start + i * nbyte;
    if (addr < 0 || addr + nbyte > data.length) return -1;
    int value = 0;
    for (int j = 0; j < nbyte; j++) {
      value = (value << 8) | (data[addr + j] & 0xFF);
    }
    return value;
  }

  private static String stripSpaces(CharSequence cs) {
    StringBuilder sb = new StringBuilder(cs.length());
    for (int i = 0; i < cs.length(); i++) {
      char c = cs.charAt(i);
      if (c != ' ') sb.append(c);
    }
    return sb.toString();
  }

  private static BufferedReader newReader(InputStream is) {
    Reader r = new InputStreamReader(is, StandardCharsets.UTF_8);
    return new BufferedReader(r);
  }

  private static String stripBom(String line) {
    if (!line.isEmpty() && line.charAt(0) == '\uFEFF') {
      return line.substring(1);
    }
    return line;
  }

  private static String trimRight(String s) {
    int end = s.length();
    while (end > 0) {
      char c = s.charAt(end - 1);
      if (c == ' ' || c == '\t' || c == '\r') {
        end--;
      } else {
        break;
      }
    }
    return end == s.length() ? s : s.substring(0, end);
  }

  private static final class WeightedEntry {
    final String text;
    final double weight;

    WeightedEntry(String text, double weight) {
      this.text = text;
      this.weight = weight;
    }
  }
}
