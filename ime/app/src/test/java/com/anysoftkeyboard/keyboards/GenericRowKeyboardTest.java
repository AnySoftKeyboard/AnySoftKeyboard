package com.anysoftkeyboard.keyboards;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.anysoftkeyboard.keyboards.ExternalAnyKeyboardTest.SIMPLE_KeyboardDimens;
import static com.anysoftkeyboard.keyboards.Keyboard.KEYBOARD_ROW_MODE_NORMAL;

import android.content.Context;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.addons.AddOn;
import com.anysoftkeyboard.addons.DefaultAddOn;
import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.keyboardextensions.KeyboardExtension;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class GenericRowKeyboardTest {

    private AddOn mDefaultAddOn;
    private Context mContext;
    private KeyboardExtension mRowExtension;

    @Before
    public void setup() {
        mContext = getApplicationContext();
        mDefaultAddOn = new DefaultAddOn(mContext, mContext);
        mRowExtension = Mockito.spy(AnyApplication.getBottomRowFactory(mContext).getEnabledAddOn());
    }

    @Test
    public void testHasPopupForSymbolsWhenFromAlphabetKeyboard() {
        Mockito.doReturn(R.xml.test_ext_kbd_row_with_symbols)
                .when(mRowExtension)
                .getKeyboardResId();
        AnyKeyboard.GenericRowKeyboard keyboard =
                new AnyKeyboard.GenericRowKeyboard(
                        mRowExtension,
                        mContext,
                        SIMPLE_KeyboardDimens,
                        true,
                        KEYBOARD_ROW_MODE_NORMAL);

        Keyboard.Key key =
                keyboard.getKeys().stream()
                        .filter(k -> k.getPrimaryCode() == KeyCodes.MODE_SYMBOLS)
                        .findFirst()
                        .orElse(null);
        Assert.assertNotNull(key);
        Assert.assertEquals(R.xml.ext_symbols, key.popupResId);
        Assert.assertFalse(key.externalResourcePopupLayout);
    }

    @Test
    public void testHasPopupForChangeModeWhenFromAlphabetKeyboard() {
        Mockito.doReturn(R.xml.test_ext_kbd_row_with_mode_change)
                .when(mRowExtension)
                .getKeyboardResId();
        AnyKeyboard.GenericRowKeyboard keyboard =
                new AnyKeyboard.GenericRowKeyboard(
                        mRowExtension,
                        mContext,
                        SIMPLE_KeyboardDimens,
                        true,
                        KEYBOARD_ROW_MODE_NORMAL);

        Keyboard.Key key =
                keyboard.getKeys().stream()
                        .filter(k -> k.getPrimaryCode() == KeyCodes.KEYBOARD_MODE_CHANGE)
                        .findFirst()
                        .orElse(null);
        Assert.assertNotNull(key);
        Assert.assertEquals(R.xml.ext_symbols, key.popupResId);
        Assert.assertFalse(key.externalResourcePopupLayout);
    }

    @Test
    public void testDoesNotHavePopupSymbolsWhenFromNonAlphabetKeyboard() {
        Mockito.doReturn(R.xml.test_ext_kbd_row_with_symbols)
                .when(mRowExtension)
                .getKeyboardResId();
        AnyKeyboard.GenericRowKeyboard keyboard =
                new AnyKeyboard.GenericRowKeyboard(
                        mRowExtension,
                        mContext,
                        SIMPLE_KeyboardDimens,
                        false,
                        KEYBOARD_ROW_MODE_NORMAL);

        Keyboard.Key key =
                keyboard.getKeys().stream()
                        .filter(k -> k.getPrimaryCode() == KeyCodes.MODE_SYMBOLS)
                        .findFirst()
                        .orElse(null);
        Assert.assertNotNull(key);
        Assert.assertEquals(0, key.popupResId);
    }

    public void testDoesNotHavePopupSymbolsWhenNoSymbolsKeyboard() {
        Mockito.doReturn(R.xml.test_ext_kbd_row_without_symbols_or_mode_change)
                .when(mRowExtension)
                .getKeyboardResId();
        AnyKeyboard.GenericRowKeyboard keyboard =
                new AnyKeyboard.GenericRowKeyboard(
                        mRowExtension,
                        mContext,
                        SIMPLE_KeyboardDimens,
                        true,
                        KEYBOARD_ROW_MODE_NORMAL);

        keyboard.getKeys().forEach(k -> Assert.assertNotEquals(R.xml.ext_symbols, k.popupResId));
    }

    public void testDoesNotHavePopupSymbolsWhenNoSymbolsKeyboardInNonAlphabet() {
        Mockito.doReturn(R.xml.test_ext_kbd_row_without_symbols_or_mode_change)
                .when(mRowExtension)
                .getKeyboardResId();
        AnyKeyboard.GenericRowKeyboard keyboard =
                new AnyKeyboard.GenericRowKeyboard(
                        mRowExtension,
                        mContext,
                        SIMPLE_KeyboardDimens,
                        false,
                        KEYBOARD_ROW_MODE_NORMAL);

        keyboard.getKeys().forEach(k -> Assert.assertNotEquals(R.xml.ext_symbols, k.popupResId));
    }
}
