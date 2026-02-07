package com.anysoftkeyboard.keyboards;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.anysoftkeyboard.keyboards.ExternalAnyKeyboardTest.SIMPLE_KeyboardDimens;

import android.os.Build;
import androidx.annotation.NonNull;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.MyShadowPaint;
import com.anysoftkeyboard.addons.DefaultAddOn;
import com.anysoftkeyboard.utils.EmojiUtils;
import com.menny.android.anysoftkeyboard.R;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
@Config(shadows = MyShadowPaint.class)
public class AnyPopupKeyboardTest {

  @NonNull
  private AnyPopupKeyboard createAnyPopupKeyboard(
      int keyboardResId, EmojiUtils.SkinTone skinTone, EmojiUtils.Gender gender) {
    return new AnyPopupKeyboard(
        new DefaultAddOn(getApplicationContext(), getApplicationContext()),
        getApplicationContext(),
        keyboardResId,
        SIMPLE_KeyboardDimens,
        "POP_KEYBOARD",
        skinTone,
        gender);
  }

  @Test
  public void testKeyboardResourceConstructor() throws Exception {
    AnyPopupKeyboard keyboard =
        createAnyPopupKeyboard(R.xml.quick_text_unicode_emoticons, null, null);
    Assert.assertEquals("POP_KEYBOARD", keyboard.getKeyboardName());

    Assert.assertEquals(98, keyboard.getKeys().size());
  }

  @Test
  @Config(sdk = Build.VERSION_CODES.M)
  public void testKeyboardResourceConstructorReadsTags() throws Exception {
    AnyPopupKeyboard keyboard =
        createAnyPopupKeyboard(R.xml.quick_text_unicode_emoticons, null, null);

    Assert.assertArrayEquals(
        "face,grin".split(","),
        ((AnyKeyboard.AnyKey) keyboard.getKeys().get(0)).getKeyTags().toArray());
    Assert.assertArrayEquals(
        "eye,face,grin,smile".split(","),
        ((AnyKeyboard.AnyKey) keyboard.getKeys().get(1)).getKeyTags().toArray());
  }

  @Test
  @Config(sdk = Build.VERSION_CODES.N)
  public void testKeyboardResourceConstructorReadsTagsWithExtension() throws Exception {
    AnyPopupKeyboard keyboard =
        createAnyPopupKeyboard(R.xml.quick_text_unicode_emoticons, null, null);

    Assert.assertArrayEquals(
        "grinning,face,grinning_face".split(","),
        ((AnyKeyboard.AnyKey) keyboard.getKeys().get(0)).getKeyTags().toArray());
  }

  private void assertKeyValues(AnyPopupKeyboard keyboard, int primaryCode, int y) {
    assertKeyValues(keyboard, primaryCode, y, -1);
  }

  private void assertKeyValues(AnyPopupKeyboard keyboard, int primaryCode, int y, int x) {
    final Keyboard.Key key =
        keyboard.getKeys().stream()
            .filter(k -> k.getPrimaryCode() == primaryCode)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Failed to find character " + primaryCode));
    Assert.assertEquals("Y mismatch", y, key.y);
    if (x != -1) Assert.assertEquals("X mismatch", x, key.x);
  }

  @Test
  public void testKeyboardPopupCharacterStringConstructor() throws Exception {
    AnyPopupKeyboard keyboard =
        new AnyPopupKeyboard(
            new DefaultAddOn(getApplicationContext(), getApplicationContext()),
            getApplicationContext(),
            "ûūùú",
            SIMPLE_KeyboardDimens,
            "POP_KEYBOARD");

    Assert.assertEquals("POP_KEYBOARD", keyboard.getKeyboardName());

    Assert.assertEquals(4, keyboard.getKeys().size());
    int row0Y = (int) SIMPLE_KeyboardDimens.getRowVerticalGap();
    assertKeyValues(keyboard, 'û', row0Y);
    assertKeyValues(keyboard, 'ū', row0Y);
    assertKeyValues(keyboard, 'ù', row0Y);
    assertKeyValues(keyboard, 'ú', row0Y);
    Assert.assertEquals(
        1, // one row
        keyboard.getKeys().stream().map(k -> k.y).distinct().count());
  }

  @Test
  public void testKeyboardPopupCharacterStringTwoRowsConstructor() throws Exception {
    AnyPopupKeyboard keyboard =
        new AnyPopupKeyboard(
            new DefaultAddOn(getApplicationContext(), getApplicationContext()),
            getApplicationContext(),
            "qwertyuio",
            SIMPLE_KeyboardDimens,
            "POP_KEYBOARD");

    Assert.assertEquals("POP_KEYBOARD", keyboard.getKeyboardName());

    Assert.assertEquals(9, keyboard.getKeys().size());
    Assert.assertEquals(
        2, // two rows
        keyboard.getKeys().stream().map(k -> k.y).distinct().count());
    int vGap = (int) SIMPLE_KeyboardDimens.getRowVerticalGap();
    int keyHeight = (int) SIMPLE_KeyboardDimens.getNormalKeyHeight();
    // NOTE: the first characters in the list are in the bottom row!
    // yuio
    // qwert
    assertKeyValues(keyboard, 'y', vGap);
    assertKeyValues(keyboard, 'o', vGap);
    assertKeyValues(keyboard, 'q', vGap + keyHeight + vGap);
    assertKeyValues(keyboard, 't', vGap + keyHeight + vGap);
  }

  @Test
  public void testKeyboardPopupCharacterStringThreeRowsConstructor() throws Exception {
    AnyPopupKeyboard keyboard =
        new AnyPopupKeyboard(
            new DefaultAddOn(getApplicationContext(), getApplicationContext()),
            getApplicationContext(),
            "qwertasdfgzxc",
            SIMPLE_KeyboardDimens,
            "POP_KEYBOARD");

    Assert.assertEquals("POP_KEYBOARD", keyboard.getKeyboardName());

    Assert.assertEquals(13, keyboard.getKeys().size());
    Assert.assertEquals(3, keyboard.getKeys().stream().map(k -> k.y).distinct().count());
    int vGap = (int) SIMPLE_KeyboardDimens.getRowVerticalGap();
    int keyHeight = (int) SIMPLE_KeyboardDimens.getNormalKeyHeight();
    // NOTE: the first characters in the list are in the bottom row!
    // zxc
    // asdfg
    // qwert
    assertKeyValues(keyboard, 'z', vGap);
    assertKeyValues(keyboard, 'x', vGap);
    assertKeyValues(keyboard, 'c', vGap);
    assertKeyValues(keyboard, 'a', vGap + keyHeight + vGap);
    assertKeyValues(keyboard, 's', vGap + keyHeight + vGap);
    assertKeyValues(keyboard, 'd', vGap + keyHeight + vGap);
    assertKeyValues(keyboard, 'f', vGap + keyHeight + vGap);
    assertKeyValues(keyboard, 'g', vGap + keyHeight + vGap);
    assertKeyValues(keyboard, 'q', vGap + keyHeight + vGap + keyHeight + vGap);
    assertKeyValues(keyboard, 'w', vGap + keyHeight + vGap + keyHeight + vGap);
    assertKeyValues(keyboard, 'e', vGap + keyHeight + vGap + keyHeight + vGap);
    assertKeyValues(keyboard, 'r', vGap + keyHeight + vGap + keyHeight + vGap);
    assertKeyValues(keyboard, 't', vGap + keyHeight + vGap + keyHeight + vGap);
  }

  @Test
  public void testKeyboardPopupSupportsMirrorOneRow() throws Exception {
    String popupCharacters = "qwert";
    AnyPopupKeyboard keyboard =
        new AnyPopupKeyboard(
            new DefaultAddOn(getApplicationContext(), getApplicationContext()),
            getApplicationContext(),
            popupCharacters,
            SIMPLE_KeyboardDimens,
            "POP_KEYBOARD");
    int vGap = (int) SIMPLE_KeyboardDimens.getRowVerticalGap();
    int hGap = (int) SIMPLE_KeyboardDimens.getKeyHorizontalGap();
    // Each key is 10% of keyboard width (48px), minus the horizontal gap
    final int keyWidth =
        (int)
            (SIMPLE_KeyboardDimens.getKeyboardMaxWidth() / 10
                - SIMPLE_KeyboardDimens.getKeyHorizontalGap());

    assertKeyValues(keyboard, 'q', vGap, hGap / 2);
    assertKeyValues(keyboard, 'w', vGap, hGap + keyWidth);
    assertKeyValues(keyboard, 'e', vGap, 2 * hGap + 2 * keyWidth);
    assertKeyValues(keyboard, 'r', vGap, 3 * hGap + 3 * keyWidth);
    assertKeyValues(keyboard, 't', vGap, 4 * hGap + 4 * keyWidth);

    keyboard.mirrorKeys();
    // same order, mirrored X position
    // Mirrored using formula: newX = keyboardWidth - oldX - keyWidth
    // All keys have half-gap before every key
    assertKeyValues(keyboard, 'q', vGap, 4 * hGap + 4 * keyWidth);
    assertKeyValues(keyboard, 'w', vGap, 3 * hGap + hGap / 2 + 3 * keyWidth);
    assertKeyValues(keyboard, 'e', vGap, 2 * hGap + hGap / 2 + 2 * keyWidth);
    assertKeyValues(keyboard, 'r', vGap, hGap + hGap / 2 + keyWidth);
    assertKeyValues(keyboard, 't', vGap, hGap / 2);
  }

  @Test
  public void testKeyboardPopupSupportsMirrorOneRowNotFull() throws Exception {
    String popupCharacters = "qwe";
    AnyPopupKeyboard keyboard =
        new AnyPopupKeyboard(
            new DefaultAddOn(getApplicationContext(), getApplicationContext()),
            getApplicationContext(),
            popupCharacters,
            SIMPLE_KeyboardDimens,
            "POP_KEYBOARD");
    int vGap = (int) SIMPLE_KeyboardDimens.getRowVerticalGap();
    int hGap = (int) SIMPLE_KeyboardDimens.getKeyHorizontalGap();
    // Each key is 10% of keyboard width (48px), minus the horizontal gap
    final int keyWidth =
        (int)
            (SIMPLE_KeyboardDimens.getKeyboardMaxWidth() / 10
                - SIMPLE_KeyboardDimens.getKeyHorizontalGap());

    assertKeyValues(keyboard, 'q', vGap, hGap / 2);
    assertKeyValues(keyboard, 'w', vGap, hGap + keyWidth);
    assertKeyValues(keyboard, 'e', vGap, 2 * hGap + 2 * keyWidth);

    keyboard.mirrorKeys();
    // same order, mirrored X position
    // Mirrored using formula: newX = keyboardWidth - oldX - keyWidth
    // keyboardWidth = 3 keys (width 38 each) + 3 gaps (10 each) + 2 half-gaps (5
    // each) = 144
    assertKeyValues(keyboard, 'q', vGap, 2 * hGap + 2 * keyWidth);
    assertKeyValues(keyboard, 'w', vGap, hGap + hGap / 2 + keyWidth);
    assertKeyValues(keyboard, 'e', vGap, hGap / 2);
  }

  @Test
  public void testKeyboardPopupSupportsMirrorMultipleFullRows() throws Exception {
    String popupCharacters = "qwertasdfg";
    // asdfg
    // qwert
    AnyPopupKeyboard keyboard =
        new AnyPopupKeyboard(
            new DefaultAddOn(getApplicationContext(), getApplicationContext()),
            getApplicationContext(),
            popupCharacters,
            SIMPLE_KeyboardDimens,
            "POP_KEYBOARD");
    int vGap = (int) SIMPLE_KeyboardDimens.getRowVerticalGap();
    int keyHeight = (int) SIMPLE_KeyboardDimens.getNormalKeyHeight();
    int hGap = (int) SIMPLE_KeyboardDimens.getKeyHorizontalGap();
    // Each key is 10% of keyboard width (48px), minus the horizontal gap
    final int keyWidth =
        (int)
            (SIMPLE_KeyboardDimens.getKeyboardMaxWidth() / 10
                - SIMPLE_KeyboardDimens.getKeyHorizontalGap());

    Assert.assertEquals(10, keyboard.getKeys().size());
    assertKeyValues(keyboard, 'q', vGap + keyHeight + vGap, hGap / 2);
    assertKeyValues(keyboard, 'w', vGap + keyHeight + vGap, hGap + keyWidth);
    assertKeyValues(keyboard, 'e', vGap + keyHeight + vGap, 2 * hGap + 2 * keyWidth);
    assertKeyValues(keyboard, 'r', vGap + keyHeight + vGap, 3 * hGap + 3 * keyWidth);
    assertKeyValues(keyboard, 't', vGap + keyHeight + vGap, 4 * hGap + 4 * keyWidth);
    assertKeyValues(keyboard, 'a', vGap, hGap / 2);
    assertKeyValues(keyboard, 's', vGap, hGap + keyWidth);
    assertKeyValues(keyboard, 'd', vGap, 2 * hGap + 2 * keyWidth);
    assertKeyValues(keyboard, 'f', vGap, 3 * hGap + 3 * keyWidth);
    assertKeyValues(keyboard, 'g', vGap, 4 * hGap + 4 * keyWidth);

    keyboard.mirrorKeys();
    // same order, mirrored X position
    // Mirrored using formula: newX = keyboardWidth - oldX - keyWidth
    // keyboardWidth = 5 keys + 5 gaps + 2 half-gaps = 5*38 + 5*10 + 2*5 = 190 + 50
    // + 10 = 250
    // But wait, that's per row. Both rows have same width, so use the max width.
    Assert.assertEquals(10, keyboard.getKeys().size());
    assertKeyValues(keyboard, 'q', vGap + keyHeight + vGap, 4 * hGap + 4 * keyWidth);
    assertKeyValues(keyboard, 'w', vGap + keyHeight + vGap, 3 * hGap + hGap / 2 + 3 * keyWidth);
    assertKeyValues(keyboard, 'e', vGap + keyHeight + vGap, 2 * hGap + hGap / 2 + 2 * keyWidth);
    assertKeyValues(keyboard, 'r', vGap + keyHeight + vGap, hGap + hGap / 2 + keyWidth);
    assertKeyValues(keyboard, 't', vGap + keyHeight + vGap, hGap / 2);
    assertKeyValues(keyboard, 'a', vGap, 4 * hGap + 4 * keyWidth);
    assertKeyValues(keyboard, 's', vGap, 3 * hGap + hGap / 2 + 3 * keyWidth);
    assertKeyValues(keyboard, 'd', vGap, 2 * hGap + hGap / 2 + 2 * keyWidth);
    assertKeyValues(keyboard, 'f', vGap, hGap + hGap / 2 + keyWidth);
    assertKeyValues(keyboard, 'g', vGap, hGap / 2);
  }

  @Test
  public void testKeyboardPopupSupportsMirrorMultipleRowsNotFullBalanced() throws Exception {
    String popupCharacters = "qwertasd";
    // asd
    // qwert
    AnyPopupKeyboard keyboard =
        new AnyPopupKeyboard(
            new DefaultAddOn(getApplicationContext(), getApplicationContext()),
            getApplicationContext(),
            popupCharacters,
            SIMPLE_KeyboardDimens,
            "POP_KEYBOARD");
    int vGap = (int) SIMPLE_KeyboardDimens.getRowVerticalGap();
    int keyHeight = (int) SIMPLE_KeyboardDimens.getNormalKeyHeight();
    int hGap = (int) SIMPLE_KeyboardDimens.getKeyHorizontalGap();
    // Each key is 10% of keyboard width (48px), minus the horizontal gap
    final int keyWidth =
        (int)
            (SIMPLE_KeyboardDimens.getKeyboardMaxWidth() / 10
                - SIMPLE_KeyboardDimens.getKeyHorizontalGap());

    Assert.assertEquals(8, keyboard.getKeys().size());
    assertKeyValues(keyboard, 'q', vGap + keyHeight + vGap, hGap / 2);
    assertKeyValues(keyboard, 'w', vGap + keyHeight + vGap, hGap + keyWidth);
    assertKeyValues(keyboard, 'e', vGap + keyHeight + vGap, 2 * hGap + 2 * keyWidth);
    assertKeyValues(keyboard, 'r', vGap + keyHeight + vGap, 3 * hGap + 3 * keyWidth);
    assertKeyValues(keyboard, 't', vGap + keyHeight + vGap, 4 * hGap + 4 * keyWidth);
    assertKeyValues(keyboard, 'a', vGap, hGap / 2);
    assertKeyValues(keyboard, 's', vGap, hGap + keyWidth);
    assertKeyValues(keyboard, 'd', vGap, 2 * hGap + 2 * keyWidth);

    keyboard.mirrorKeys();
    // same order, mirrored X position
    // Mirrored using formula: newX = keyboardWidth - oldX - keyWidth
    // All rows use the same keyboard width (max of all rows) = width of qwert row
    Assert.assertEquals(8, keyboard.getKeys().size());
    assertKeyValues(keyboard, 'q', vGap + keyHeight + vGap, 4 * hGap + 4 * keyWidth);
    assertKeyValues(keyboard, 'w', vGap + keyHeight + vGap, 3 * hGap + hGap / 2 + 3 * keyWidth);
    assertKeyValues(keyboard, 'e', vGap + keyHeight + vGap, 2 * hGap + hGap / 2 + 2 * keyWidth);
    assertKeyValues(keyboard, 'r', vGap + keyHeight + vGap, hGap + hGap / 2 + keyWidth);
    assertKeyValues(keyboard, 't', vGap + keyHeight + vGap, hGap / 2);
    assertKeyValues(keyboard, 'a', vGap, 4 * hGap + 4 * keyWidth);
    assertKeyValues(keyboard, 's', vGap, 3 * hGap + hGap / 2 + 3 * keyWidth);
    assertKeyValues(keyboard, 'd', vGap, 2 * hGap + hGap / 2 + 2 * keyWidth);
  }

  @Test
  public void testKeyboardPopupSupportsMirrorMultipleRowsNotFullNotBalanced() throws Exception {
    String popupCharacters = "qwertas";
    // as
    // qwert
    AnyPopupKeyboard keyboard =
        new AnyPopupKeyboard(
            new DefaultAddOn(getApplicationContext(), getApplicationContext()),
            getApplicationContext(),
            popupCharacters,
            SIMPLE_KeyboardDimens,
            "POP_KEYBOARD");
    int vGap = (int) SIMPLE_KeyboardDimens.getRowVerticalGap();
    int keyHeight = (int) SIMPLE_KeyboardDimens.getNormalKeyHeight();
    int hGap = (int) SIMPLE_KeyboardDimens.getKeyHorizontalGap();
    // Each key is 10% of keyboard width (48px), minus the horizontal gap
    final int keyWidth =
        (int)
            (SIMPLE_KeyboardDimens.getKeyboardMaxWidth() / 10
                - SIMPLE_KeyboardDimens.getKeyHorizontalGap());

    Assert.assertEquals(7, keyboard.getKeys().size());
    assertKeyValues(keyboard, 'q', vGap + keyHeight + vGap, hGap / 2);
    assertKeyValues(keyboard, 'w', vGap + keyHeight + vGap, hGap + keyWidth);
    assertKeyValues(keyboard, 'e', vGap + keyHeight + vGap, 2 * hGap + 2 * keyWidth);
    assertKeyValues(keyboard, 'r', vGap + keyHeight + vGap, 3 * hGap + 3 * keyWidth);
    assertKeyValues(keyboard, 't', vGap + keyHeight + vGap, 4 * hGap + 4 * keyWidth);
    assertKeyValues(keyboard, 'a', vGap, hGap / 2);
    assertKeyValues(keyboard, 's', vGap, hGap + keyWidth);

    keyboard.mirrorKeys();
    // same order, mirrored X position
    // Mirrored using formula: newX = keyboardWidth - oldX - keyWidth
    // All rows use the same keyboard width (max of all rows) = width of qwert row
    Assert.assertEquals(7, keyboard.getKeys().size());
    assertKeyValues(keyboard, 'q', vGap + keyHeight + vGap, 4 * hGap + 4 * keyWidth);
    assertKeyValues(keyboard, 'w', vGap + keyHeight + vGap, 3 * hGap + hGap / 2 + 3 * keyWidth);
    assertKeyValues(keyboard, 'e', vGap + keyHeight + vGap, 2 * hGap + hGap / 2 + 2 * keyWidth);
    assertKeyValues(keyboard, 'r', vGap + keyHeight + vGap, hGap + hGap / 2 + keyWidth);
    assertKeyValues(keyboard, 't', vGap + keyHeight + vGap, hGap / 2);
    assertKeyValues(keyboard, 'a', vGap, 4 * hGap + 4 * keyWidth);
    assertKeyValues(keyboard, 's', vGap, 3 * hGap + hGap / 2 + 3 * keyWidth);
  }

  @Test
  public void testEmptyCodes() {
    AnyPopupKeyboard keyboard =
        createAnyPopupKeyboard(R.xml.keyboard_with_keys_with_no_codes, null, null);
    for (int keyIndex = 0; keyIndex < keyboard.getKeys().size(); keyIndex++) {
      Assert.assertEquals(0, keyboard.getKeys().get(keyIndex).getCodeAtIndex(0, false));
    }

    for (int keyIndex = 0; keyIndex < keyboard.getKeys().size(); keyIndex++) {
      // NOTE: popup keyboard will not look at long-press key codes and such..
      Assert.assertEquals(0, keyboard.getKeys().get(keyIndex).getCodeAtIndex(0, true));
    }
  }

  @Test
  @Config(sdk = Build.VERSION_CODES.N)
  public void testKeyboardSwitchesSkinTone() throws Exception {
    AnyPopupKeyboard keyboardWithGeneric =
        createAnyPopupKeyboard(R.xml.quick_text_unicode_people, null, null);
    Assert.assertEquals("\uD83D\uDC85", keyboardWithGeneric.getKeys().get(1).text);
    Assert.assertEquals("\uD83D\uDC85", keyboardWithGeneric.getKeys().get(1).label);

    AnyPopupKeyboard keyboardWithSkinTone =
        createAnyPopupKeyboard(
            R.xml.quick_text_unicode_people, EmojiUtils.SkinTone.Fitzpatrick_2, null);

    Assert.assertEquals("\uD83D\uDC85\uD83C\uDFFB", keyboardWithSkinTone.getKeys().get(1).text);
    Assert.assertEquals("\uD83D\uDC85\uD83C\uDFFB", keyboardWithSkinTone.getKeys().get(1).label);
  }

  @Test
  @Config(sdk = Build.VERSION_CODES.M)
  public void testHidesKeysWithNoGlyph() throws Exception {
    AnyPopupKeyboard keyboard = createAnyPopupKeyboard(R.xml.quick_text_unicode_people, null, null);

    MyShadowPaint.addStringWithoutGlyph(keyboard.getKeys().get(2).text.toString());

    keyboard = createAnyPopupKeyboard(R.xml.quick_text_unicode_people, null, null);

    Assert.assertTrue(keyboard.getKeys().get(0).width > 0);
    Assert.assertTrue(keyboard.getKeys().get(0).text.length() > 0);
    Assert.assertFalse(keyboard.getKeys().get(2).width > 0);
    Assert.assertEquals("", keyboard.getKeys().get(2).text);
  }

  @Test
  @Config(sdk = Build.VERSION_CODES.M)
  public void testDoesNotHideKeysWithJustText() throws Exception {
    MyShadowPaint.addStringWithoutGlyph(
        "(* ^ ω ^) "); // this should not matter since `hasGlyph` should not be called
    AnyPopupKeyboard keyboard = createAnyPopupKeyboard(R.xml.popup_kaomoji, null, null);
    Assert.assertEquals("(* ^ ω ^) ", keyboard.getKeys().get(0).text);
  }
}
