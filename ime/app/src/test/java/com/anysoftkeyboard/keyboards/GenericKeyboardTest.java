package com.anysoftkeyboard.keyboards;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.anysoftkeyboard.keyboards.ExternalAnyKeyboardTest.SIMPLE_KeyboardDimens;

import android.content.Context;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.addons.AddOn;
import com.anysoftkeyboard.addons.DefaultAddOn;
import com.anysoftkeyboard.keyboardextensions.KeyboardExtension;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class GenericKeyboardTest {

  private AddOn mDefaultAddOn;
  private Context mContext;
  private KeyboardExtension mTopRow;
  private KeyboardExtension mBottomRow;

  @Before
  public void setup() {
    mContext = getApplicationContext();
    mDefaultAddOn = new DefaultAddOn(mContext, mContext);
    mTopRow = AnyApplication.getTopRowFactory(mContext).getEnabledAddOn();
    mBottomRow = AnyApplication.getBottomRowFactory(mContext).getEnabledAddOn();
  }

  @Test
  public void testDoNotShowPasswordTopRow() {
    // generic keyboards do not show password rows. ever.
    GenericKeyboard keyboard =
        new GenericKeyboard(
            mDefaultAddOn,
            mContext,
            R.xml.symbols,
            R.xml.symbols,
            "test",
            "test",
            Keyboard.KEYBOARD_ROW_MODE_NORMAL);
    keyboard.loadKeyboard(SIMPLE_KeyboardDimens, mTopRow, mBottomRow);

    Assert.assertEquals(-2, keyboard.getKeys().get(0).getPrimaryCode());

    keyboard =
        new GenericKeyboard(
            mDefaultAddOn,
            mContext,
            R.xml.symbols,
            R.xml.symbols,
            "test",
            "test",
            Keyboard.KEYBOARD_ROW_MODE_PASSWORD);
    keyboard.loadKeyboard(SIMPLE_KeyboardDimens, mTopRow, mBottomRow);

    Assert.assertEquals(-2, keyboard.getKeys().get(0).getPrimaryCode());
  }

  @Test
  public void testKeyboardIdPassed() {
    GenericKeyboard keyboard =
        new GenericKeyboard(
            mDefaultAddOn,
            mContext,
            R.xml.symbols,
            R.xml.symbols,
            "test",
            "test",
            Keyboard.KEYBOARD_ROW_MODE_NORMAL);
    Assert.assertEquals("test", keyboard.getKeyboardId());
    Assert.assertNotEquals(keyboard.getKeyboardId(), mDefaultAddOn.getId());
  }

  @Test
  public void testIsNotAlphabetKeyboard() {
    GenericKeyboard keyboard =
        new GenericKeyboard(
            mDefaultAddOn,
            mContext,
            R.xml.symbols,
            R.xml.symbols,
            "test",
            "test",
            Keyboard.KEYBOARD_ROW_MODE_NORMAL);
    Assert.assertFalse(keyboard.isAlphabetKeyboard());
    Assert.assertEquals(KeyboardExtension.SCOPE_SYMBOLS, keyboard.getKeyboardScope());
  }

  @Test
  public void testDoNotShowAlphabetScopedTopRowOnSymbolsKeyboard() {
    KeyboardExtension numberTopRow =
        AnyApplication.getTopRowFactory(mContext)
            .getAddOnById("1fae0220-ded6-11e0-9572-0800200c9a66");
    Assert.assertNotNull(numberTopRow);

    GenericKeyboard keyboard =
        new GenericKeyboard(
            mDefaultAddOn,
            mContext,
            R.xml.symbols,
            R.xml.symbols,
            "test",
            "test",
            Keyboard.KEYBOARD_ROW_MODE_NORMAL);
    keyboard.loadKeyboard(SIMPLE_KeyboardDimens, numberTopRow, mBottomRow);

    // Number row should not be added on top of symbols keyboard;
    // first key should be 1 (code 49) from symbols.xml row 1 itself, not a prepended generic row.
    Assert.assertEquals(49, keyboard.getKeys().get(0).getPrimaryCode());
    // And second row begins with ! (code 33)
    Assert.assertEquals(33, keyboard.getKeys().get(10).getPrimaryCode());
  }

  @Test
  public void testFalseShowPreviewAtRoot() throws Exception {
    GenericKeyboard anyKeyboard =
        new GenericKeyboard(
            mDefaultAddOn,
            mContext,
            R.xml.keyboard_with_false_show_preview_at_root,
            R.xml.keyboard_with_false_show_preview_at_root,
            "test",
            "test",
            Keyboard.KEYBOARD_ROW_MODE_NORMAL);
    anyKeyboard.loadKeyboard(SIMPLE_KeyboardDimens, mTopRow, mBottomRow);

    final int indexAfterTopRow = 4;

    Assert.assertFalse(anyKeyboard.showPreview);
    Assert.assertEquals(52, anyKeyboard.getKeys().get(indexAfterTopRow).getPrimaryCode());
    Assert.assertFalse(anyKeyboard.getKeys().get(indexAfterTopRow).showPreview);
    // overrides locally
    Assert.assertEquals(53, anyKeyboard.getKeys().get(indexAfterTopRow + 1).getPrimaryCode());
    Assert.assertFalse(anyKeyboard.getKeys().get(indexAfterTopRow + 1).showPreview);
    // overrides locally
    Assert.assertEquals(54, anyKeyboard.getKeys().get(indexAfterTopRow + 2).getPrimaryCode());
    Assert.assertTrue(anyKeyboard.getKeys().get(indexAfterTopRow + 2).showPreview);
    Assert.assertEquals(47, anyKeyboard.getKeys().get(indexAfterTopRow + 3).getPrimaryCode());
    Assert.assertFalse(anyKeyboard.getKeys().get(indexAfterTopRow + 3).showPreview);
  }

  @Test
  public void testTrueShowPreviewAtRoot() throws Exception {
    GenericKeyboard anyKeyboard =
        new GenericKeyboard(
            mDefaultAddOn,
            mContext,
            R.xml.keyboard_with_true_show_preview_at_root,
            R.xml.keyboard_with_true_show_preview_at_root,
            "test",
            "test",
            Keyboard.KEYBOARD_ROW_MODE_NORMAL);
    anyKeyboard.loadKeyboard(SIMPLE_KeyboardDimens, mTopRow, mBottomRow);

    final int indexAfterTopRow = 4;

    Assert.assertTrue(anyKeyboard.showPreview);
    Assert.assertTrue(anyKeyboard.getKeys().get(indexAfterTopRow).showPreview);
    // overrides locally
    Assert.assertFalse(anyKeyboard.getKeys().get(indexAfterTopRow + 1).showPreview);
    // overrides locally
    Assert.assertTrue(anyKeyboard.getKeys().get(indexAfterTopRow + 2).showPreview);
    Assert.assertTrue(anyKeyboard.getKeys().get(indexAfterTopRow + 3).showPreview);
  }

  @Test
  public void testNoShowPreviewAtRoot() throws Exception {
    GenericKeyboard anyKeyboard =
        new GenericKeyboard(
            mDefaultAddOn,
            mContext,
            R.xml.keyboard_with_no_show_preview_at_root,
            R.xml.keyboard_with_no_show_preview_at_root,
            "test",
            "test",
            Keyboard.KEYBOARD_ROW_MODE_NORMAL);
    anyKeyboard.loadKeyboard(SIMPLE_KeyboardDimens, mTopRow, mBottomRow);

    final int indexAfterTopRow = 4;

    Assert.assertTrue(anyKeyboard.showPreview);
    Assert.assertTrue(anyKeyboard.getKeys().get(indexAfterTopRow).showPreview);
    // overrides locally
    Assert.assertFalse(anyKeyboard.getKeys().get(indexAfterTopRow + 1).showPreview);
    // overrides locally
    Assert.assertTrue(anyKeyboard.getKeys().get(indexAfterTopRow + 2).showPreview);
    Assert.assertTrue(anyKeyboard.getKeys().get(indexAfterTopRow + 3).showPreview);
  }
}
