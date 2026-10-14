/*
 * Copyright (c) 2026 AnySoftKeyboard contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package com.anysoftkeyboard.dictionaries.radical.importers;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class RadicalTableImporterTest {

  private static ByteArrayInputStream in(String s) {
    return new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
  }

  @Test
  public void parsesIbusTabBasic() throws Exception {
    String src = "# comment\n" + "a\t對\n" + "\n" + "aa\t寸\n" + "ag\t威\n";
    Map<String, List<String>> table = RadicalTableImporter.parseIbusTab(in(src));
    Assert.assertEquals(3, table.size());
    Assert.assertEquals("對", table.get("a").get(0));
    Assert.assertEquals("寸", table.get("aa").get(0));
    Assert.assertEquals("威", table.get("ag").get(0));
  }

  @Test
  public void parsesIbusTabMultipleCandidatesSameCode() throws Exception {
    String src = "a\t對\n" + "a\t隊\n";
    Map<String, List<String>> table = RadicalTableImporter.parseIbusTab(in(src));
    Assert.assertEquals(1, table.size());
    Assert.assertEquals(2, table.get("a").size());
    Assert.assertEquals("對", table.get("a").get(0));
    Assert.assertEquals("隊", table.get("a").get(1));
  }

  @Test
  public void parsesIbusTabStripsBom() throws Exception {
    String src = "\uFEFFa\t對\n";
    Map<String, List<String>> table = RadicalTableImporter.parseIbusTab(in(src));
    Assert.assertEquals(1, table.size());
    Assert.assertEquals("對", table.get("a").get(0));
  }

  @Test
  public void parsesScimCinIgnoresHeader() throws Exception {
    String src =
        "%gen_inp\n"
            + "%ename Boshiamy\n"
            + "%keyname begin\n"
            + "a a\n"
            + "%keyname end\n"
            + "%chardef begin\n"
            + "a 對\n"
            + "aa 寸\n"
            + "ag 威\n"
            + "# comment inside\n"
            + "%chardef end\n"
            + "a 不應出現\n";
    Map<String, List<String>> table = RadicalTableImporter.parseScimCin(in(src));
    Assert.assertEquals(3, table.size());
    Assert.assertEquals("對", table.get("a").get(0));
    Assert.assertEquals(1, table.get("a").size());
  }

  @Test
  public void parsesRimeDictYamlWithWeightSorted() throws Exception {
    String src =
        "name: boshiamy\n"
            + "version: 0\n"
            + "...\n"
            + "---\n"
            + "對\ta\t100\n"
            + "隊\ta\t500\n"
            + "寸\taa\n";
    Map<String, List<String>> table = RadicalTableImporter.parseRimeDictYaml(in(src));
    Assert.assertEquals(2, table.size());
    // Higher weight first.
    Assert.assertEquals("隊", table.get("a").get(0));
    Assert.assertEquals("對", table.get("a").get(1));
    Assert.assertEquals("寸", table.get("aa").get(0));
  }

  @Test
  public void parseDispatchesByExtension() throws Exception {
    String tab = "a\t對\n";
    Map<String, List<String>> t1 = RadicalTableImporter.parse("foo.tab", in(tab));
    Assert.assertEquals("對", t1.get("a").get(0));

    String cin = "%chardef begin\na 對\n%chardef end\n";
    Map<String, List<String>> t2 = RadicalTableImporter.parse("foo.cin", in(cin));
    Assert.assertEquals("對", t2.get("a").get(0));

    String yaml = "---\n對\ta\n";
    Map<String, List<String>> t3 = RadicalTableImporter.parse("foo.dict.yaml", in(yaml));
    Assert.assertEquals("對", t3.get("a").get(0));
  }

  @Test
  public void writeProducesAddonFormat() throws Exception {
    String src = "a\t對\n" + "a\t隊\n" + "aa\t寸\n";
    Map<String, List<String>> table = RadicalTableImporter.parseIbusTab(in(src));
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    RadicalTableImporter.write(table, out);
    String written = out.toString("UTF-8");
    Assert.assertEquals("a\t對\n" + "a\t隊\n" + "aa\t寸\n", written);
  }

  @Test
  public void formatDetect() {
    Assert.assertEquals(
        RadicalTableImporter.Format.IBUS_TAB, RadicalTableImporter.Format.detect("anything.tab"));
    Assert.assertEquals(
        RadicalTableImporter.Format.BOSHIAMY_LIU_BOX,
        RadicalTableImporter.Format.detect("liu.box"));
    Assert.assertEquals(
        RadicalTableImporter.Format.SCIM_CIN, RadicalTableImporter.Format.detect("Boshiamy.cin"));
    Assert.assertEquals(
        RadicalTableImporter.Format.RIME_DICT_YAML,
        RadicalTableImporter.Format.detect("array.dict.yaml"));
  }

  @Test
  public void parseBoshiamyLiuBinEmptyReturnsEmpty() {
    Map<String, List<String>> table = RadicalTableImporter.parseBoshiamyLiuBin(new byte[0]);
    Assert.assertTrue(table.isEmpty());
    Map<String, List<String>> tooShort =
        RadicalTableImporter.parseBoshiamyLiuBin(new byte[] {0, 0, 0});
    Assert.assertTrue(tooShort.isEmpty());
  }

  @Test
  public void detectFromContentAndNameRoutesBinaryByNameAndContent() {
    byte[] nulHeader = new byte[64];
    nulHeader[3] = 0; // contains NUL bytes
    Assert.assertEquals(
        RadicalTableImporter.Format.BOSHIAMY_LIU_BIN,
        RadicalTableImporter.Format.detectFromContentAndName("liu1.tab", nulHeader));
    Assert.assertEquals(
        RadicalTableImporter.Format.BOSHIAMY_LIU_BIN,
        RadicalTableImporter.Format.detectFromContentAndName("liu-uni3.tab", nulHeader));
    // A text .tab file (no NUL, non-liu name) should be detected as IBus tab.
    byte[] textBytes = "a\t\u4e2d\n".getBytes(StandardCharsets.UTF_8);
    Assert.assertEquals(
        RadicalTableImporter.Format.IBUS_TAB,
        RadicalTableImporter.Format.detectFromContentAndName("foo.tab", textBytes));
    // SCIM .cin and RIME .yaml take precedence over content sniff.
    Assert.assertEquals(
        RadicalTableImporter.Format.SCIM_CIN,
        RadicalTableImporter.Format.detectFromContentAndName("foo.cin", nulHeader));
    Assert.assertEquals(
        RadicalTableImporter.Format.RIME_DICT_YAML,
        RadicalTableImporter.Format.detectFromContentAndName("foo.dict.yaml", nulHeader));
  }

  @Test
  public void parsesLiuBoxTextSpaceAndTabSeparators() throws Exception {
    String src =
        "# user additions\n"
            + "LIU 嘸蝦米\n"
            + "LIU1 嘸蝦米輸入法\n"
            + ".1\t\tㄅ\n"
            + "b. (´▽｀)\n"
            + "e. 😒\n"
            + "%selkey 1234567890\n"
            + "\n";
    Map<String, List<String>> table = RadicalTableImporter.parseLiuBoxText(in(src));
    Assert.assertEquals(5, table.size());
    Assert.assertEquals("嘸蝦米", table.get("LIU").get(0));
    Assert.assertEquals("嘸蝦米輸入法", table.get("LIU1").get(0));
    Assert.assertEquals("ㄅ", table.get(".1").get(0));
    Assert.assertEquals("(´▽｀)", table.get("b.").get(0));
    Assert.assertEquals("😒", table.get("e.").get(0));
  }

  @Test
  public void detectFormatBoxByExtension() {
    Assert.assertEquals(
        RadicalTableImporter.Format.BOSHIAMY_LIU_BOX,
        RadicalTableImporter.Format.detect("liu.box"));
    Assert.assertEquals(
        RadicalTableImporter.Format.BOSHIAMY_LIU_BOX,
        RadicalTableImporter.Format.detectFromContentAndName(
            "liu.box", "a 一\n".getBytes(StandardCharsets.UTF_8)));
  }

  @Test
  public void parseDispatchesBoxExtension() throws Exception {
    String src = "abc 字\n";
    Map<String, List<String>> table = RadicalTableImporter.parse("liu.box", in(src));
    Assert.assertEquals(1, table.size());
    Assert.assertEquals("字", table.get("abc").get(0));
  }
}
