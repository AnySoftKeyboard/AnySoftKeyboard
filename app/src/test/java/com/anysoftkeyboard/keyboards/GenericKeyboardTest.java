package com.anysoftkeyboard.keyboards;

import android.content.Context;

import com.anysoftkeyboard.addons.AddOn;
import com.anysoftkeyboard.addons.DefaultAddOn;
import com.anysoftkeyboard.ime.AnySoftKeyboardKeyboardTagsSearcher;
import com.anysoftkeyboard.keyboardextensions.KeyboardExtension;
import com.anysoftkeyboard.keyboardextensions.KeyboardExtensionFactory;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

@RunWith(RobolectricTestRunner.class)
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
        mTopRow = KeyboardExtensionFactory.getAllAvailableExtensions(RuntimeEnvironment.application, KeyboardExtension.TYPE_TOP).get(0);
        mBottomRow = KeyboardExtensionFactory.getCurrentKeyboardExtension(RuntimeEnvironment.application, KeyboardExtension.TYPE_BOTTOM);

    }

    @Test
    public void testDoNotShowPasswordTopRow() {
        GenericKeyboard keyboard = new GenericKeyboard(mDefaultAddOn, mContext, R.xml.symbols, R.xml.symbols,  "test", "test", Keyboard.KEYBOARD_ROW_MODE_NORMAL, false);
        keyboard.loadKeyboard(mKeyboardDimens, mTopRow, mBottomRow);

        Assert.assertEquals((int)'1', keyboard.getKeys().get(0).getPrimaryCode());
        Assert.assertNotEquals((int)'1', keyboard.getKeys().get(10).getPrimaryCode());

        keyboard = new GenericKeyboard(mDefaultAddOn, mContext, R.xml.symbols, R.xml.symbols,  "test", "test", Keyboard.KEYBOARD_ROW_MODE_PASSWORD, false);
        keyboard.loadKeyboard(mKeyboardDimens, mTopRow, mBottomRow);

        Assert.assertEquals((int)'1', keyboard.getKeys().get(0).getPrimaryCode());
        Assert.assertNotEquals((int)'1', keyboard.getKeys().get(10).getPrimaryCode());
    }

    @Test
    public void testDisabledPreviewGetter() {
        GenericKeyboard keyboard = new GenericKeyboard(mDefaultAddOn, mContext, R.xml.symbols, R.xml.symbols,  "test", "test", Keyboard.KEYBOARD_ROW_MODE_NORMAL, false);
        Assert.assertFalse(keyboard.disableKeyPreviews());
        keyboard = new GenericKeyboard(mDefaultAddOn, mContext, R.xml.symbols, R.xml.symbols,  "test", "test", Keyboard.KEYBOARD_ROW_MODE_NORMAL, true);
        Assert.assertTrue(keyboard.disableKeyPreviews());
    }
}