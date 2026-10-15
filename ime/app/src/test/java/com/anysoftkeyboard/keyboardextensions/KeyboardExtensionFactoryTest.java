package com.anysoftkeyboard.keyboardextensions;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class KeyboardExtensionFactoryTest {

  @Test
  public void testGetCurrentKeyboardExtensionBottomDefault() throws Exception {
    KeyboardExtension extension =
        AnyApplication.getBottomRowFactory(getApplicationContext()).getEnabledAddOn();
    Assert.assertNotNull(extension);
    Assert.assertEquals("09f8f280-dee2-11e0-9572-0800200c9a66", extension.getId());
    Assert.assertEquals(KeyboardExtension.TYPE_BOTTOM, extension.getExtensionType());
    Assert.assertEquals(R.xml.ext_kbd_bottom_row_regular_with_voice, extension.getKeyboardResId());
  }

  @Test
  public void testGetCurrentKeyboardExtensionBottomChanged() throws Exception {
    AnyApplication.getBottomRowFactory(getApplicationContext())
        .setAddOnEnabled("3659b9e0-dee2-11e0-9572-0800200c9a55", true);
    KeyboardExtension extension =
        AnyApplication.getBottomRowFactory(getApplicationContext()).getEnabledAddOn();
    Assert.assertNotNull(extension);
    Assert.assertEquals("3659b9e0-dee2-11e0-9572-0800200c9a55", extension.getId());
    Assert.assertEquals(KeyboardExtension.TYPE_BOTTOM, extension.getExtensionType());
    Assert.assertEquals(R.xml.ext_kbd_bottom_row_iphone, extension.getKeyboardResId());
  }

  @Test
  public void testGetCurrentKeyboardExtensionTopDefault() throws Exception {
    KeyboardExtension extension =
        AnyApplication.getTopRowFactory(getApplicationContext()).getEnabledAddOn();
    Assert.assertNotNull(extension);
    Assert.assertEquals("5d945f40-ded5-11e0-9572-0800200c9a66", extension.getId());
    Assert.assertEquals(KeyboardExtension.TYPE_TOP, extension.getExtensionType());
    Assert.assertEquals(R.xml.ext_kbd_top_row_small, extension.getKeyboardResId());
  }

  @Test
  public void testGetCurrentKeyboardExtensionTopChanged() throws Exception {
    AnyApplication.getTopRowFactory(getApplicationContext())
        .setAddOnEnabled("642e9690-ded5-11e0-9572-0800200c9a66", true);
    KeyboardExtension extension =
        AnyApplication.getTopRowFactory(getApplicationContext()).getEnabledAddOn();
    Assert.assertNotNull(extension);
    Assert.assertEquals("642e9690-ded5-11e0-9572-0800200c9a66", extension.getId());
    Assert.assertEquals(KeyboardExtension.TYPE_TOP, extension.getExtensionType());
    Assert.assertEquals(R.xml.ext_kbd_top_row_normal, extension.getKeyboardResId());
  }

  @Test
  public void testGetCurrentKeyboardExtensionExtensionDefault() throws Exception {
    KeyboardExtension extension =
        AnyApplication.getKeyboardExtensionFactory(getApplicationContext()).getEnabledAddOn();
    Assert.assertNotNull(extension);
    Assert.assertEquals("6f1ecea0-dee2-11e0-9572-0800200c9a66", extension.getId());
    Assert.assertEquals(KeyboardExtension.TYPE_EXTENSION, extension.getExtensionType());
    Assert.assertEquals(R.xml.ext_kbd_ext_keyboard_numbers_symbols, extension.getKeyboardResId());
  }

  @Test
  public void testGetAllAvailableExtensions() throws Exception {
    assertBasicListDetails(
        AnyApplication.getBottomRowFactory(getApplicationContext()).getAllAddOns(),
        16,
        KeyboardExtension.TYPE_BOTTOM);
    assertBasicListDetails(
        AnyApplication.getTopRowFactory(getApplicationContext()).getAllAddOns(),
        11,
        KeyboardExtension.TYPE_TOP);
    assertBasicListDetails(
        AnyApplication.getKeyboardExtensionFactory(getApplicationContext()).getAllAddOns(),
        1,
        KeyboardExtension.TYPE_EXTENSION);
  }

  @Test
  public void testKeyboardExtensionScope() {
    KeyboardExtension defaultTopRow =
        AnyApplication.getTopRowFactory(getApplicationContext()).getEnabledAddOn();
    Assert.assertNotNull(defaultTopRow);
    Assert.assertEquals(KeyboardExtension.SCOPE_ANY, defaultTopRow.getExtensionScope());
    Assert.assertTrue(defaultTopRow.isCompatible(KeyboardExtension.SCOPE_ALPHABET));
    Assert.assertTrue(defaultTopRow.isCompatible(KeyboardExtension.SCOPE_SYMBOLS));

    KeyboardExtension numberRow =
        AnyApplication.getTopRowFactory(getApplicationContext())
            .getAddOnById("1fae0220-ded6-11e0-9572-0800200c9a66");
    Assert.assertNotNull(numberRow);
    Assert.assertEquals(KeyboardExtension.SCOPE_ALPHABET, numberRow.getExtensionScope());
    Assert.assertTrue(numberRow.isCompatible(KeyboardExtension.SCOPE_ALPHABET));
    Assert.assertFalse(numberRow.isCompatible(KeyboardExtension.SCOPE_SYMBOLS));

    KeyboardExtension numberRowAlt =
        AnyApplication.getTopRowFactory(getApplicationContext())
            .getAddOnById("69520BA1-7EB1-424B-B5C5-C3C4857E53FE");
    Assert.assertNotNull(numberRowAlt);
    Assert.assertEquals(KeyboardExtension.SCOPE_ALPHABET, numberRowAlt.getExtensionScope());

    KeyboardExtension numberRowSimple =
        AnyApplication.getTopRowFactory(getApplicationContext())
            .getAddOnById("d9d6f590-db8b-11e7-8f1a-0800200c9a66");
    Assert.assertNotNull(numberRowSimple);
    Assert.assertEquals(KeyboardExtension.SCOPE_ALPHABET, numberRowSimple.getExtensionScope());
  }

  private void assertBasicListDetails(
      List<KeyboardExtension> availableExtensions,
      int extensionsCount,
      @KeyboardExtension.KeyboardExtensionType int type) {
    Assert.assertNotNull(availableExtensions);
    Assert.assertEquals(extensionsCount, availableExtensions.size());
    for (KeyboardExtension extension : availableExtensions) {
      Assert.assertNotNull(extension);
      Assert.assertEquals(type, extension.getExtensionType());
    }
  }
}
