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
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

/**
 * Pluggable source of <em>user-supplied</em> radical-dictionary data that augments or replaces the
 * addon's bundled tables. Each accessor returns:
 *
 * <ul>
 *   <li>{@code null}, the user has not provided this table; fall back to the bundled resource.
 *   <li>a non-null {@link InputStream} factory list, open those streams instead of (for the
 *       <em>replace</em> APIs) or in addition to (for the <em>overlay</em> APIs) the bundled
 *       resource. Each stream is in the same tab-separated format as the bundled raw resources.
 * </ul>
 *
 * <p>This interface intentionally yields {@code InputStream}s lazily via {@link StreamSupplier} so
 * the dictionary loader can re-open streams across multi-file inputs and so callers don't have to
 * manage stream lifetime up-front.
 *
 * <p>All implementations must be thread-safe, the dictionary loader runs on a background thread but
 * may be invoked concurrently with overlay-mutation flows on the UI thread.
 */
public interface RadicalOverlay {

  /** Lazy supplier of a fresh {@link InputStream}. */
  interface StreamSupplier {
    @NonNull
    InputStream open() throws IOException;

    /**
     * Optional file-name hint used by importers for format detection (e.g. {@code liu1.tab} vs a
     * plain text table). Default returns the empty string; implementations backed by a real file
     * should return the file's basename.
     */
    @NonNull
    default String name() {
      return "";
    }
  }

  /** No-op overlay; every accessor returns {@code null} (use bundled resources only). */
  RadicalOverlay NONE = new RadicalOverlay() {};

  /**
   * Replacement for the main radical -> character table. When non-null, the loader uses these
   * streams <em>instead</em> of the bundled main table. Order matters: streams are read in sequence
   * and later entries can append to the same key.
   */
  @Nullable
  default List<StreamSupplier> mainTableReplacement() {
    return null;
  }

  /**
   * Additive overlay applied <em>on top of</em> the main table (whether bundled or replaced).
   * Entries here add to existing radical -> character lists rather than replace them. This is the
   * canonical place for a personal {@code liu.box}-style mapping.
   */
  @Nullable
  default List<StreamSupplier> mainTableOverlay() {
    return null;
  }

  /** Replacement for the phrase association table. */
  @Nullable
  default List<StreamSupplier> phrasesReplacement() {
    return null;
  }

  /** Additive overlay on top of the phrase table. */
  @Nullable
  default List<StreamSupplier> phrasesOverlay() {
    return null;
  }

  /** Replacement for the homophone table (Zhuyin syllable -> chars). */
  @Nullable
  default List<StreamSupplier> homophonesReplacement() {
    return null;
  }

  /** Replacement for char -> Zhuyin lookup. */
  @Nullable
  default List<StreamSupplier> charToZhuyinReplacement() {
    return null;
  }

  /** Replacement for char -> radical lookup. */
  @Nullable
  default List<StreamSupplier> charToRadicalReplacement() {
    return null;
  }

  /** Replacement for character-frequency ranking. */
  @Nullable
  default List<StreamSupplier> charFrequencyReplacement() {
    return null;
  }

  /** Returns an unmodifiable empty list, convenience helper for implementations. */
  @NonNull
  static List<StreamSupplier> empty() {
    return Collections.emptyList();
  }
}
