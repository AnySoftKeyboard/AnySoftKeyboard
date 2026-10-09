/*
 * Copyright (c) 2026 AnySoftKeyboard contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package com.anysoftkeyboard.dictionaries;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

public class RadicalOverlayCacheTest {

  @Test
  public void wrappedOverlayReadsBaseOnce_acrossMultipleAccesses() {
    AtomicInteger opens = new AtomicInteger();
    RadicalOverlay base = singleMainOverlay("a\tA\n", opens);

    RadicalOverlayCache cache = new RadicalOverlayCache();
    RadicalOverlay wrapped = cache.wrap("file:/tmp/x/addon1", base);

    // First materialisation reads the stream once
    List<RadicalOverlay.StreamSupplier> first = wrapped.mainTableReplacement();
    assertNotNull(first);
    consume(first);
    assertEquals(1, opens.get());

    // Subsequent accessor calls do NOT reopen the base supplier
    consume(wrapped.mainTableReplacement());
    consume(wrapped.mainTableReplacement());
    assertNull(wrapped.mainTableOverlay()); // null entries cached as null
    assertEquals(1, opens.get());
  }

  @Test
  public void invalidateAll_forcesReReadOnNextAccess() {
    AtomicInteger opens = new AtomicInteger();
    RadicalOverlay base = singleMainOverlay("a\tA\n", opens);

    RadicalOverlayCache cache = new RadicalOverlayCache();
    RadicalOverlay wrapped = cache.wrap("file:/tmp/x/addon1", base);

    consume(wrapped.mainTableReplacement());
    assertEquals(1, opens.get());

    cache.invalidateAll();
    assertEquals(0, cache.snapshotCountForTest());

    consume(wrapped.mainTableReplacement());
    assertEquals(2, opens.get());
  }

  @Test
  public void differentCacheKeys_doNotCollide() {
    AtomicInteger opensA = new AtomicInteger();
    AtomicInteger opensB = new AtomicInteger();
    RadicalOverlay baseA = singleMainOverlay("a\tA\n", opensA);
    RadicalOverlay baseB = singleMainOverlay("b\tB\n", opensB);

    RadicalOverlayCache cache = new RadicalOverlayCache();
    RadicalOverlay wrappedA = cache.wrap("file:/d1/addon", baseA);
    RadicalOverlay wrappedB = cache.wrap("file:/d2/addon", baseB);

    consume(wrappedA.mainTableReplacement());
    consume(wrappedB.mainTableReplacement());

    assertEquals(2, cache.snapshotCountForTest());
    assertEquals(1, opensA.get());
    assertEquals(1, opensB.get());
  }

  // ---- helpers -----------------------------------------------------------------------------

  private static void consume(@Nullable List<RadicalOverlay.StreamSupplier> entries) {
    if (entries == null) return;
    for (RadicalOverlay.StreamSupplier s : entries) {
      try (InputStream in = s.open()) {
        byte[] buf = new byte[1024];
        //noinspection StatementWithEmptyBody
        while (in.read(buf) >= 0) {
          // drain
        }
      } catch (Exception ignored) {
        // tests do not throw
      }
    }
  }

  @NonNull
  private static RadicalOverlay singleMainOverlay(
      @NonNull String content, @NonNull AtomicInteger openCounter) {
    final byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
    final RadicalOverlay.StreamSupplier supplier =
        new RadicalOverlay.StreamSupplier() {
          @Override
          @NonNull
          public InputStream open() {
            openCounter.incrementAndGet();
            return new ByteArrayInputStream(bytes);
          }

          @Override
          @NonNull
          public String name() {
            return "main.tab";
          }
        };
    final List<RadicalOverlay.StreamSupplier> single =
        Collections.unmodifiableList(Arrays.asList(supplier));
    return new RadicalOverlay() {
      @Override
      @Nullable
      public List<StreamSupplier> mainTableReplacement() {
        return single;
      }

      @Override
      @Nullable
      public List<StreamSupplier> mainTableOverlay() {
        return null;
      }

      @Override
      @Nullable
      public List<StreamSupplier> phrasesReplacement() {
        return null;
      }

      @Override
      @Nullable
      public List<StreamSupplier> phrasesOverlay() {
        return null;
      }

      @Override
      @Nullable
      public List<StreamSupplier> homophonesReplacement() {
        return null;
      }

      @Override
      @Nullable
      public List<StreamSupplier> charToZhuyinReplacement() {
        return null;
      }

      @Override
      @Nullable
      public List<StreamSupplier> charToRadicalReplacement() {
        return null;
      }

      @Override
      @Nullable
      public List<StreamSupplier> charFrequencyReplacement() {
        return null;
      }
    };
  }
}
