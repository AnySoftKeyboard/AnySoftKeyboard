package com.anysoftkeyboard.keyboards.views;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.api.KeyCodes;
import com.menny.android.anysoftkeyboard.R;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class ThemeAttributeKeyCodesTest {

  @Test
  public void testKeyCodesMapping() {
    Assert.assertEquals(
        KeyCodes.SHIFT,
        ThemeAttributeKeyCodes.getKeyCodeForIconThemeAttribute(R.attr.iconKeyShift));
    Assert.assertEquals(
        KeyCodes.CTRL,
        ThemeAttributeKeyCodes.getKeyCodeForIconThemeAttribute(R.attr.iconKeyControl));
    Assert.assertEquals(
        KeyCodes.ENTER,
        ThemeAttributeKeyCodes.getKeyCodeForIconThemeAttribute(R.attr.iconKeyAction));
    Assert.assertEquals(
        KeyCodes.DELETE,
        ThemeAttributeKeyCodes.getKeyCodeForIconThemeAttribute(R.attr.iconKeyBackspace));
    Assert.assertEquals(
        KeyCodes.CANCEL,
        ThemeAttributeKeyCodes.getKeyCodeForIconThemeAttribute(R.attr.iconKeyCancel));
    Assert.assertEquals(
        KeyCodes.MODE_ALPHABET,
        ThemeAttributeKeyCodes.getKeyCodeForIconThemeAttribute(R.attr.iconKeyGlobe));
    Assert.assertEquals(
        KeyCodes.SPACE,
        ThemeAttributeKeyCodes.getKeyCodeForIconThemeAttribute(R.attr.iconKeySpace));
    Assert.assertEquals(
        KeyCodes.TAB, ThemeAttributeKeyCodes.getKeyCodeForIconThemeAttribute(R.attr.iconKeyTab));
    Assert.assertEquals(
        KeyCodes.ARROW_DOWN,
        ThemeAttributeKeyCodes.getKeyCodeForIconThemeAttribute(R.attr.iconKeyArrowDown));
    Assert.assertEquals(
        KeyCodes.ARROW_LEFT,
        ThemeAttributeKeyCodes.getKeyCodeForIconThemeAttribute(R.attr.iconKeyArrowLeft));
    Assert.assertEquals(
        KeyCodes.ARROW_RIGHT,
        ThemeAttributeKeyCodes.getKeyCodeForIconThemeAttribute(R.attr.iconKeyArrowRight));
    Assert.assertEquals(
        KeyCodes.ARROW_UP,
        ThemeAttributeKeyCodes.getKeyCodeForIconThemeAttribute(R.attr.iconKeyArrowUp));
    Assert.assertEquals(
        KeyCodes.MOVE_HOME,
        ThemeAttributeKeyCodes.getKeyCodeForIconThemeAttribute(R.attr.iconKeyInputMoveHome));
    Assert.assertEquals(
        KeyCodes.MOVE_END,
        ThemeAttributeKeyCodes.getKeyCodeForIconThemeAttribute(R.attr.iconKeyInputMoveEnd));
    Assert.assertEquals(
        KeyCodes.VOICE_INPUT,
        ThemeAttributeKeyCodes.getKeyCodeForIconThemeAttribute(R.attr.iconKeyMic));
    Assert.assertEquals(
        KeyCodes.SETTINGS,
        ThemeAttributeKeyCodes.getKeyCodeForIconThemeAttribute(R.attr.iconKeySettings));
    Assert.assertEquals(
        KeyCodes.MERGE_LAYOUT,
        ThemeAttributeKeyCodes.getKeyCodeForIconThemeAttribute(R.attr.iconKeyCondenseNormal));
    Assert.assertEquals(
        KeyCodes.SPLIT_LAYOUT,
        ThemeAttributeKeyCodes.getKeyCodeForIconThemeAttribute(R.attr.iconKeyCondenseSplit));
    Assert.assertEquals(
        KeyCodes.COMPACT_LAYOUT_TO_RIGHT,
        ThemeAttributeKeyCodes.getKeyCodeForIconThemeAttribute(
            R.attr.iconKeyCondenseCompactToRight));
    Assert.assertEquals(
        KeyCodes.COMPACT_LAYOUT_TO_LEFT,
        ThemeAttributeKeyCodes.getKeyCodeForIconThemeAttribute(
            R.attr.iconKeyCondenseCompactToLeft));
    Assert.assertEquals(
        KeyCodes.CLIPBOARD_COPY,
        ThemeAttributeKeyCodes.getKeyCodeForIconThemeAttribute(R.attr.iconKeyClipboardCopy));
    Assert.assertEquals(
        KeyCodes.CLIPBOARD_CUT,
        ThemeAttributeKeyCodes.getKeyCodeForIconThemeAttribute(R.attr.iconKeyClipboardCut));
    Assert.assertEquals(
        KeyCodes.CLIPBOARD_PASTE,
        ThemeAttributeKeyCodes.getKeyCodeForIconThemeAttribute(R.attr.iconKeyClipboardPaste));
    Assert.assertEquals(
        KeyCodes.CLIPBOARD_SELECT_ALL,
        ThemeAttributeKeyCodes.getKeyCodeForIconThemeAttribute(R.attr.iconKeyClipboardSelect));
    Assert.assertEquals(
        KeyCodes.CLIPBOARD_SELECT,
        ThemeAttributeKeyCodes.getKeyCodeForIconThemeAttribute(R.attr.iconKeyClipboardFineSelect));
    Assert.assertEquals(
        KeyCodes.QUICK_TEXT_POPUP,
        ThemeAttributeKeyCodes.getKeyCodeForIconThemeAttribute(R.attr.iconKeyQuickTextPopup));
    Assert.assertEquals(
        KeyCodes.QUICK_TEXT,
        ThemeAttributeKeyCodes.getKeyCodeForIconThemeAttribute(R.attr.iconKeyQuickText));
    Assert.assertEquals(
        KeyCodes.UNDO, ThemeAttributeKeyCodes.getKeyCodeForIconThemeAttribute(R.attr.iconKeyUndo));
    Assert.assertEquals(
        KeyCodes.REDO, ThemeAttributeKeyCodes.getKeyCodeForIconThemeAttribute(R.attr.iconKeyRedo));
    Assert.assertEquals(
        KeyCodes.FORWARD_DELETE,
        ThemeAttributeKeyCodes.getKeyCodeForIconThemeAttribute(R.attr.iconKeyForwardDelete));
    Assert.assertEquals(
        KeyCodes.IMAGE_MEDIA_POPUP,
        ThemeAttributeKeyCodes.getKeyCodeForIconThemeAttribute(R.attr.iconKeyImageInsert));
    Assert.assertEquals(
        KeyCodes.CLEAR_QUICK_TEXT_HISTORY,
        ThemeAttributeKeyCodes.getKeyCodeForIconThemeAttribute(
            R.attr.iconKeyClearQuickTextHistory));
    Assert.assertEquals(0, ThemeAttributeKeyCodes.getKeyCodeForIconThemeAttribute(0));
  }
}
