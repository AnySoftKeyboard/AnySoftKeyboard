package com.anysoftkeyboard.dictionaries;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.addons.AddOn;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link RadicalDictionaryConfig} and the radical-only path in {@link
 * DictionaryAddOnAndBuilder}.
 */
@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class RadicalDictionaryConfigTest {

  @Test
  public void empty_hasNoRadicalDictionary() {
    Assert.assertFalse(RadicalDictionaryConfig.EMPTY.hasRadicalDictionary());
    Assert.assertEquals(AddOn.INVALID_RES_ID, RadicalDictionaryConfig.EMPTY.getRadicalDictResId());
    Assert.assertEquals(AddOn.INVALID_RES_ID, RadicalDictionaryConfig.EMPTY.getHomophonesResId());
  }

  @Test
  public void builder_setsAllFields() {
    RadicalDictionaryConfig cfg =
        new RadicalDictionaryConfig.Builder()
            .radicalDictResId(11)
            .radicalPhrasesResId(22)
            .homophonesResId(33)
            .charToZhuyinResId(44)
            .charToRadicalResId(55)
            .excludeHomophoneCharsResId(66)
            .charFrequencyResId(77)
            .build();

    Assert.assertTrue(cfg.hasRadicalDictionary());
    Assert.assertEquals(11, cfg.getRadicalDictResId());
    Assert.assertEquals(22, cfg.getRadicalPhrasesResId());
    Assert.assertEquals(33, cfg.getHomophonesResId());
    Assert.assertEquals(44, cfg.getCharToZhuyinResId());
    Assert.assertEquals(55, cfg.getCharToRadicalResId());
    Assert.assertEquals(66, cfg.getExcludeHomophoneCharsResId());
    Assert.assertEquals(77, cfg.getCharFrequencyResId());
  }

  @Test
  public void radicalOnlyConfig_hasRadicalDictionary() {
    // Note: cannot test createDictionary() returning null directly here because
    // ShadowDictionaryAddOnAndBuilder in the test classpath always returns an
    // InMemoryDictionary. The null-path is implicitly covered at runtime by the
    // chinese_traditional pack's tests.
    RadicalDictionaryConfig cfg =
        new RadicalDictionaryConfig.Builder().radicalDictResId(123).build();
    Assert.assertTrue(cfg.hasRadicalDictionary());
    Assert.assertEquals(123, cfg.getRadicalDictResId());
  }
}
