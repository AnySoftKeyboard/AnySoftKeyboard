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
import androidx.annotation.VisibleForTesting;
import com.anysoftkeyboard.base.utils.Logger;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory cache of {@link RadicalOverlay} snapshots, keyed by an opaque cache key built from the
 * overlay's base descriptor (path or content URI) and the addon ID.
 *
 * <p>The first call to any per-table accessor on a {@linkplain #wrap wrapped} overlay reads every
 * supplied stream once into a {@code byte[]} snapshot; subsequent calls return suppliers backed by
 * {@link ByteArrayInputStream}s over the snapshot. The snapshot survives across keyboard switches
 * and is only invalidated by explicit user action ({@link #invalidate(String)} / {@link
 * #invalidateAll()}) or by process death.
 *
 * <p>Instances of this class are intended to be owned per-process by the addon factory rather than
 * held in static state; tests construct fresh instances to avoid leakage across runs.
 */
public final class RadicalOverlayCache {

  private static final String TAG = "RadicalOverlayCache";

  private final ConcurrentHashMap<String, Snapshot> mSnapshots = new ConcurrentHashMap<>();

  /**
   * Wraps {@code base} so the first access to any accessor materialises and caches the streams for
   * this {@code cacheKey}. Subsequent calls (in this process, until {@link #invalidate} or {@link
   * #invalidateAll} clears the entry) return the cached snapshot directly.
   */
  @NonNull
  public RadicalOverlay wrap(@NonNull String cacheKey, @NonNull RadicalOverlay base) {
    return new CachedOverlay(cacheKey, base);
  }

  /** Drops the snapshot for a specific cache key; the next access reloads from the base. */
  public void invalidate(@NonNull String cacheKey) {
    mSnapshots.remove(cacheKey);
  }

  /** Drops all snapshots; the next access for each key reloads from the base. */
  public void invalidateAll() {
    mSnapshots.clear();
  }

  @VisibleForTesting
  int snapshotCountForTest() {
    return mSnapshots.size();
  }

  @NonNull
  private Snapshot snapshotFor(@NonNull String key, @NonNull RadicalOverlay base) {
    // Don't cache a snapshot that resulted in entirely-empty data, this typically signals a
    // transient failure (DocumentsProvider not ready, SAF cursor returned null, ...). Caching
    // the empty result would permanently disable the overlay until the user taps
    // "Refresh imported tables". On the next access we re-call base which now has a chance to
    // heal (e.g. DocumentFileRadicalOverlay won't have marked itself resolved on a transient
    // failure, so it will re-query SAF).
    Snapshot existing = mSnapshots.get(key);
    if (existing != null) return existing;
    Snapshot fresh = Snapshot.of(base);
    if (fresh.isEmpty()) return fresh;
    Snapshot prior = mSnapshots.putIfAbsent(key, fresh);
    return prior != null ? prior : fresh;
  }

  private final class CachedOverlay implements RadicalOverlay {
    private final String mKey;
    private final RadicalOverlay mBase;

    CachedOverlay(@NonNull String key, @NonNull RadicalOverlay base) {
      mKey = key;
      mBase = base;
    }

    @Override
    @Nullable
    public List<StreamSupplier> mainTableReplacement() {
      return snapshotFor(mKey, mBase).mainTableReplacement;
    }

    @Override
    @Nullable
    public List<StreamSupplier> mainTableOverlay() {
      return snapshotFor(mKey, mBase).mainTableOverlay;
    }

    @Override
    @Nullable
    public List<StreamSupplier> phrasesReplacement() {
      return snapshotFor(mKey, mBase).phrasesReplacement;
    }

    @Override
    @Nullable
    public List<StreamSupplier> phrasesOverlay() {
      return snapshotFor(mKey, mBase).phrasesOverlay;
    }

    @Override
    @Nullable
    public List<StreamSupplier> homophonesReplacement() {
      return snapshotFor(mKey, mBase).homophonesReplacement;
    }

    @Override
    @Nullable
    public List<StreamSupplier> charToZhuyinReplacement() {
      return snapshotFor(mKey, mBase).charToZhuyinReplacement;
    }

    @Override
    @Nullable
    public List<StreamSupplier> charToRadicalReplacement() {
      return snapshotFor(mKey, mBase).charToRadicalReplacement;
    }

    @Override
    @Nullable
    public List<StreamSupplier> charFrequencyReplacement() {
      return snapshotFor(mKey, mBase).charFrequencyReplacement;
    }
  }

  private static final class Snapshot {
    @Nullable final List<RadicalOverlay.StreamSupplier> mainTableReplacement;
    @Nullable final List<RadicalOverlay.StreamSupplier> mainTableOverlay;
    @Nullable final List<RadicalOverlay.StreamSupplier> phrasesReplacement;
    @Nullable final List<RadicalOverlay.StreamSupplier> phrasesOverlay;
    @Nullable final List<RadicalOverlay.StreamSupplier> homophonesReplacement;
    @Nullable final List<RadicalOverlay.StreamSupplier> charToZhuyinReplacement;
    @Nullable final List<RadicalOverlay.StreamSupplier> charToRadicalReplacement;
    @Nullable final List<RadicalOverlay.StreamSupplier> charFrequencyReplacement;

    private Snapshot(
        @Nullable List<RadicalOverlay.StreamSupplier> mainTableReplacement,
        @Nullable List<RadicalOverlay.StreamSupplier> mainTableOverlay,
        @Nullable List<RadicalOverlay.StreamSupplier> phrasesReplacement,
        @Nullable List<RadicalOverlay.StreamSupplier> phrasesOverlay,
        @Nullable List<RadicalOverlay.StreamSupplier> homophonesReplacement,
        @Nullable List<RadicalOverlay.StreamSupplier> charToZhuyinReplacement,
        @Nullable List<RadicalOverlay.StreamSupplier> charToRadicalReplacement,
        @Nullable List<RadicalOverlay.StreamSupplier> charFrequencyReplacement) {
      this.mainTableReplacement = mainTableReplacement;
      this.mainTableOverlay = mainTableOverlay;
      this.phrasesReplacement = phrasesReplacement;
      this.phrasesOverlay = phrasesOverlay;
      this.homophonesReplacement = homophonesReplacement;
      this.charToZhuyinReplacement = charToZhuyinReplacement;
      this.charToRadicalReplacement = charToRadicalReplacement;
      this.charFrequencyReplacement = charFrequencyReplacement;
    }

    boolean isEmpty() {
      return mainTableReplacement == null
          && mainTableOverlay == null
          && phrasesReplacement == null
          && phrasesOverlay == null
          && homophonesReplacement == null
          && charToZhuyinReplacement == null
          && charToRadicalReplacement == null
          && charFrequencyReplacement == null;
    }

    @NonNull
    static Snapshot of(@NonNull RadicalOverlay base) {
      return new Snapshot(
          materialize(base.mainTableReplacement()),
          materialize(base.mainTableOverlay()),
          materialize(base.phrasesReplacement()),
          materialize(base.phrasesOverlay()),
          materialize(base.homophonesReplacement()),
          materialize(base.charToZhuyinReplacement()),
          materialize(base.charToRadicalReplacement()),
          materialize(base.charFrequencyReplacement()));
    }

    @Nullable
    private static List<RadicalOverlay.StreamSupplier> materialize(
        @Nullable List<RadicalOverlay.StreamSupplier> source) {
      if (source == null) return null;
      if (source.isEmpty()) return Collections.emptyList();
      List<RadicalOverlay.StreamSupplier> out = new ArrayList<>(source.size());
      for (RadicalOverlay.StreamSupplier supplier : source) {
        final String name = supplier.name();
        final byte[] bytes;
        try (InputStream raw = supplier.open()) {
          bytes = readAll(raw);
        } catch (IOException e) {
          Logger.w(TAG, "Skipping overlay entry %s: %s", name, e.getMessage());
          continue;
        }
        out.add(byteSupplier(bytes, name));
      }
      return Collections.unmodifiableList(out);
    }

    @NonNull
    private static RadicalOverlay.StreamSupplier byteSupplier(
        @NonNull final byte[] bytes, @NonNull final String name) {
      return new RadicalOverlay.StreamSupplier() {
        @Override
        @NonNull
        public InputStream open() {
          return new ByteArrayInputStream(bytes);
        }

        @Override
        @NonNull
        public String name() {
          return name;
        }
      };
    }

    @NonNull
    private static byte[] readAll(@NonNull InputStream is) throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      byte[] buf = new byte[8192];
      int n;
      while ((n = is.read(buf)) >= 0) baos.write(buf, 0, n);
      return baos.toByteArray();
    }
  }
}
