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

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class FileBasedRadicalOverlayTest {

  @Rule public TemporaryFolder tmp = new TemporaryFolder();

  private static void writeFile(File f, String content) throws Exception {
    f.getParentFile().mkdirs();
    try (FileOutputStream os = new FileOutputStream(f)) {
      os.write(content.getBytes(StandardCharsets.UTF_8));
    }
  }

  private static String readAll(InputStream is) throws Exception {
    java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
    byte[] buf = new byte[1024];
    int n;
    while ((n = is.read(buf)) > 0) out.write(buf, 0, n);
    return out.toString("UTF-8");
  }

  @Test
  public void emptyDirReturnsNullForAllAccessors() {
    FileBasedRadicalOverlay overlay = new FileBasedRadicalOverlay(tmp.getRoot(), "kbd1");
    Assert.assertNull(overlay.mainTableReplacement());
    Assert.assertNull(overlay.mainTableOverlay());
    Assert.assertNull(overlay.phrasesReplacement());
    Assert.assertNull(overlay.homophonesReplacement());
    Assert.assertNull(overlay.charToZhuyinReplacement());
    Assert.assertNull(overlay.charToRadicalReplacement());
    Assert.assertNull(overlay.charFrequencyReplacement());
  }

  @Test
  public void mainAndOverlayFilesAreSurfaced() throws Exception {
    File kbdDir = new File(tmp.getRoot(), "kbd1");
    writeFile(new File(kbdDir, FileBasedRadicalOverlay.FILE_MAIN), "a\t對\n");
    writeFile(new File(kbdDir, FileBasedRadicalOverlay.FILE_MAIN_OVERLAY), ",a\tㄇ\n");

    FileBasedRadicalOverlay overlay = new FileBasedRadicalOverlay(tmp.getRoot(), "kbd1");

    List<RadicalOverlay.StreamSupplier> mainSuppliers = overlay.mainTableReplacement();
    Assert.assertNotNull(mainSuppliers);
    Assert.assertEquals(1, mainSuppliers.size());
    try (InputStream is = mainSuppliers.get(0).open()) {
      Assert.assertEquals("a\t對\n", readAll(is));
    }

    List<RadicalOverlay.StreamSupplier> overlaySuppliers = overlay.mainTableOverlay();
    Assert.assertNotNull(overlaySuppliers);
    try (InputStream is = overlaySuppliers.get(0).open()) {
      Assert.assertEquals(",a\tㄇ\n", readAll(is));
    }
  }

  @Test
  public void differentKeyboardIdsReadDifferentDirs() throws Exception {
    File k1 = new File(tmp.getRoot(), "kbdA");
    File k2 = new File(tmp.getRoot(), "kbdB");
    writeFile(new File(k1, FileBasedRadicalOverlay.FILE_MAIN), "a\t一\n");
    writeFile(new File(k2, FileBasedRadicalOverlay.FILE_MAIN), "a\t二\n");

    FileBasedRadicalOverlay oa = new FileBasedRadicalOverlay(tmp.getRoot(), "kbdA");
    FileBasedRadicalOverlay ob = new FileBasedRadicalOverlay(tmp.getRoot(), "kbdB");
    try (InputStream isA = oa.mainTableReplacement().get(0).open();
        InputStream isB = ob.mainTableReplacement().get(0).open()) {
      Assert.assertEquals("a\t一\n", readAll(isA));
      Assert.assertEquals("a\t二\n", readAll(isB));
    }
  }

  @Test
  public void keyboardIdIsSanitized() throws Exception {
    // Path traversal attempt, characters outside [A-Za-z0-9_-] are replaced with '_'.
    FileBasedRadicalOverlay overlay = new FileBasedRadicalOverlay(tmp.getRoot(), "../../etc/evil");
    File expected =
        new File(tmp.getRoot(), FileBasedRadicalOverlay.sanitizeKeyboardId("../../etc/evil"));
    Assert.assertEquals(expected.getCanonicalPath(), overlay.getDirectory().getCanonicalPath());
    // Ensure the dot/slash characters were sanitized.
    Assert.assertFalse(overlay.getDirectory().getName().contains("/"));
    Assert.assertFalse(overlay.getDirectory().getName().contains("\\"));
    Assert.assertFalse(overlay.getDirectory().getName().equals(".."));
  }
}
