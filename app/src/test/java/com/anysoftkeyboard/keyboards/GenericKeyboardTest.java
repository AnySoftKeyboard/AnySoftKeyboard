package com.anysoftkeyboard.keyboards;

import android.content.Context;

import com.anysoftkeyboard.AnySoftKeyboardTestRunner;
import com.anysoftkeyboard.addons.AddOn;
import com.anysoftkeyboard.addons.DefaultAddOn;
import com.anysoftkeyboard.ime.AnySoftKeyboardKeyboardTagsSearcher;
import com.anysoftkeyboard.keyboardextensions.KeyboardExtension;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

@RunWith(AnySoftKeyboardTestRunner.class)
public class GenericKeyboardTest {

    private AddOn mDefaultAddOn;
    private Context mContext;
    private KeyboardDimens mKeyboardDimens;
    private KeyboardExtension mTopRow;
    private KeyboardExtension mBottomRow;

    @Before
    public void setup() {
        mContext = RuntimeEnvironment.application;
        mDefaultAddOn = new DefaultAddOn(mContext, mContext);
        mKeyboardDimens = new AnySoftKeyboardKeyboardTagsSearcher.SimpleKeyboardDimens();
        mTopRow = AnyApplication.getTopRowFactory(mContext).getEnabledAddOn();
        mBottomRow = AnyApplication.getBottomRowFactory(mContext).getEnabledAddOn();
    }

    @Test
    public void testDoNotShowPasswordTopRow() {
        //generic keyboards do not show password rows. ever.
        GenericKeyboard keyboard = new GenericKeyboard(mDefaultAddOn, mContext, R.xml.symbols, R.xml.symbols, "test", "test", Keyboard.KEYBOARD_ROW_MODE_NORMAL, false);
        keyboard.loadKeyboard(mKeyboardDimens, mTopRow, mBottomRow);

        Assert.assertEquals(-2, keyboard.getKeys().get(0).getPrimaryCode());

        keyboard = new GenericKeyboard(mDefaultAddOn, mContext, R.xml.symbols, R.xml.symbols, "test", "test", Keyboard.KEYBOARD_ROW_MODE_PASSWORD, false);
        keyboard.loadKeyboard(mKeyboardDimens, mTopRow, mBottomRow);

        Assert.assertEquals(-2, keyboard.getKeys().get(0).getPrimaryCode());
    }

    @Test
    public void testDisabledPreviewGetter() {
        GenericKeyboard keyboard = new GenericKeyboard(mDefaultAddOn, mContext, R.xml.symbols, R.xml.symbols, "test", "test", Keyboard.KEYBOARD_ROW_MODE_NORMAL, false);
        Assert.assertFalse(keyboard.disableKeyPreviews());
        keyboard = new GenericKeyboard(mDefaultAddOn, mContext, R.xml.symbols, R.xml.symbols, "test", "test", Keyboard.KEYBOARD_ROW_MODE_NORMAL, true);
        Assert.assertTrue(keyboard.disableKeyPreviews());
    }

    @Test
    public void testKeyboardIdPassed() {
        GenericKeyboard keyboard = new GenericKeyboard(mDefaultAddOn, mContext, R.xml.symbols, R.xml.symbols, "test", "test", Keyboard.KEYBOARD_ROW_MODE_NORMAL, false);
        Assert.assertEquals("test", keyboard.getKeyboardId());
        Assert.assertNotEquals(keyboard.getKeyboardId(), mDefaultAddOn.getId());
    }
}