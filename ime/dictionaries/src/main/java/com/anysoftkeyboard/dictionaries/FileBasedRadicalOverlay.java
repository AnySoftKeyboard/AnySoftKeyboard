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

package com.anysoftkeyboard.dictionaries;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@link RadicalOverlay} backed by a per-keyboard directory on disk. Layout:
 *
 * <pre>
 *   &lt;baseDir&gt;/&lt;keyboardId&gt;/
 *     main.tab            (replaces bundled main table)
 *     liu.box             (additive overlay merged into main table)
 *     phrases.tab         (replaces bundled phrases)
 *     phrases.box         (additive overlay merged into phrases)
 *     homophones.tab      (replaces bundled homophones)
 *     char_to_zhuyin.tab  (replaces bundled char->zhuyin)
 *     char_to_radical.tab (replaces bundled char->radical)
 *     char_frequency.tab  (replaces bundled char-frequency)
 * </pre>
 *
 * <p>All files are tab-separated text with the same on-disk format as the addon's bundled raw
 * resources. Lines beginning with {@code #} are comments; empty lines are ignored.
 *
 * <p>If the per-keyboard directory does not exist, every accessor returns {@code null} (i.e., the
 * overlay behaves identically to {@link RadicalOverlay#NONE}).
 *
 * <p>The implementation does <em>not</em> validate file contents at construction time, it only
 * checks for file existence. Parsing happens lazily on each {@link
 * RadicalOverlay.StreamSupplier#open()}.
 */
public final class FileBasedRadicalOverlay implements RadicalOverlay {

  /** File names used inside a per-keyboard overlay directory. */
  public static final String FILE_MAIN = "main.tab";

  public static final String FILE_MAIN_OVERLAY = "liu.box";
  public static final String FILE_PHRASES = "phrases.tab";
  public static final String FILE_PHRASES_OVERLAY = "phrases.box";
  public static final String FILE_HOMOPHONES = "homophones.tab";
  public static final String FILE_CHAR_TO_ZHUYIN = "char_to_zhuyin.tab";
  public static final String FILE_CHAR_TO_RADICAL = "char_to_radical.tab";
  public static final String FILE_CHAR_FREQUENCY = "char_frequency.tab";

  @NonNull private final File mDir;

  /**
   * @param baseDir the parent directory (typically {@code context.getFilesDir() + "/boshiamy"}).
   * @param keyboardId the radical keyboard's stable ID (typically the addon's UUID).
   */
  public FileBasedRadicalOverlay(@NonNull File baseDir, @NonNull String keyboardId) {
    this.mDir = new File(baseDir, sanitizeKeyboardId(keyboardId));
  }

  /**
   * Sanitizes a keyboard ID for use as a directory name. Allows only ASCII alphanumerics, hyphen
   * and underscore, every other character is replaced with {@code '_'}. This prevents
   * path-traversal via crafted IDs even though IDs come from trusted addon XML.
   */
  @NonNull
  static String sanitizeKeyboardId(@NonNull String id) {
    StringBuilder out = new StringBuilder(id.length());
    for (int i = 0; i < id.length(); i++) {
      char c = id.charAt(i);
      boolean ok =
          (c >= 'a' && c <= 'z')
              || (c >= 'A' && c <= 'Z')
              || (c >= '0' && c <= '9')
              || c == '-'
              || c == '_';
      out.append(ok ? c : '_');
    }
    return out.toString();
  }

  /** The on-disk directory this overlay reads from. Useful for diagnostics and for callers. */
  @NonNull
  public File getDirectory() {
    return mDir;
  }

  @Nullable
  private List<StreamSupplier> singleFileIfExists(@NonNull String fileName) {
    final File f = new File(mDir, fileName);
    if (!f.isFile() || !f.canRead()) return null;
    List<StreamSupplier> out = new ArrayList<>(1);
    out.add(fileSupplier(f));
    return Collections.unmodifiableList(out);
  }

  @NonNull
  private static StreamSupplier fileSupplier(@NonNull File f) {
    final File captured = f;
    return new StreamSupplier() {
      @NonNull
      @Override
      public InputStream open() throws java.io.IOException {
        return new FileInputStream(captured);
      }

      @NonNull
      @Override
      public String name() {
        return captured.getName();
      }
    };
  }

  /**
   * Returns suppliers for every {@code liu*.tab} (BoshiamyTIP binary) file in the directory, sorted
   * by name, or null when none exist. These are merged additively into the main table.
   */
  @Nullable
  private List<StreamSupplier> liuBinariesIfPresent() {
    if (!mDir.isDirectory()) return null;
    File[] files = mDir.listFiles();
    if (files == null) return null;
    java.util.List<File> matched = new ArrayList<>();
    for (File f : files) {
      if (!f.isFile() || !f.canRead()) continue;
      // Same restriction as the SAF overlay: only the liu-uni* character tables.
      if (DocumentFileRadicalOverlay.isLiuCharacterTable(f.getName())) matched.add(f);
    }
    if (matched.isEmpty()) return null;
    // Collections.sort + explicit comparator, List#sort and Comparator#comparing are
    // API 24+ and minSdk is 23.
    Collections.sort(matched, (a, b) -> a.getName().compareTo(b.getName()));
    List<StreamSupplier> out = new ArrayList<>(matched.size());
    for (File f : matched) out.add(fileSupplier(f));
    return Collections.unmodifiableList(out);
  }

  @Nullable
  @Override
  public List<StreamSupplier> mainTableReplacement() {
    return singleFileIfExists(FILE_MAIN);
  }

  @Nullable
  @Override
  public List<StreamSupplier> mainTableOverlay() {
    List<StreamSupplier> overlay = new ArrayList<>();
    List<StreamSupplier> liu = liuBinariesIfPresent();
    if (liu != null) overlay.addAll(liu);
    List<StreamSupplier> liubox = singleFileIfExists(FILE_MAIN_OVERLAY);
    if (liubox != null) overlay.addAll(liubox);
    return overlay.isEmpty() ? null : Collections.unmodifiableList(overlay);
  }

  @Nullable
  @Override
  public List<StreamSupplier> phrasesReplacement() {
    return singleFileIfExists(FILE_PHRASES);
  }

  @Nullable
  @Override
  public List<StreamSupplier> phrasesOverlay() {
    return singleFileIfExists(FILE_PHRASES_OVERLAY);
  }

  @Nullable
  @Override
  public List<StreamSupplier> homophonesReplacement() {
    return singleFileIfExists(FILE_HOMOPHONES);
  }

  @Nullable
  @Override
  public List<StreamSupplier> charToZhuyinReplacement() {
    return singleFileIfExists(FILE_CHAR_TO_ZHUYIN);
  }

  @Nullable
  @Override
  public List<StreamSupplier> charToRadicalReplacement() {
    return singleFileIfExists(FILE_CHAR_TO_RADICAL);
  }

  @Nullable
  @Override
  public List<StreamSupplier> charFrequencyReplacement() {
    return singleFileIfExists(FILE_CHAR_FREQUENCY);
  }
}
