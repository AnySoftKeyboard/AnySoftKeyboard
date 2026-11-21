package com.anysoftkeyboard.phonetic;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

/** Loads the real phrase table from the Traditional Chinese pack (checked in, so deterministic). */
final class TestTables {
  private static final String TABLE_PATH =
      "addons/languages/chinese_traditional/pack/src/main/res/raw/zhuyin_phrases";

  private static PhraseTable sTable;

  private TestTables() {}

  static synchronized PhraseTable zhuyin() {
    if (sTable == null) {
      try (InputStream in = new FileInputStream(findFromRepoRoot(TABLE_PATH))) {
        sTable = PhraseTable.read(in);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }
    return sTable;
  }

  /** Works from the module dir (Gradle) and from the repo root (plain javac/JUnit runs). */
  static File findFromRepoRoot(String relativePath) {
    File dir = new File("").getAbsoluteFile();
    while (dir != null) {
      final File candidate = new File(dir, relativePath);
      if (candidate.exists()) return candidate;
      dir = dir.getParentFile();
    }
    throw new IllegalStateException("Cannot find " + relativePath);
  }

  /** Feeds every code point of {@code keys} to the composer. */
  static void type(ComposingEngine engine, String keys) {
    keys.codePoints().forEach(engine::onKey);
  }
}
