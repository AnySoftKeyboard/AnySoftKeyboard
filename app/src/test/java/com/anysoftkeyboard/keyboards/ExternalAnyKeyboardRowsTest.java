package com.anysoftkeyboard.keyboards;

import android.support.annotation.NonNull;

import com.anysoftkeyboard.keyboardextensions.KeyboardExtension;
import com.anysoftkeyboard.keyboardextensions.KeyboardExtensionFactory;
import com.google.common.base.Preconditions;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

@RunWith(RobolectricTestRunner.class)
public class ExternalAnyKeyboardRowsTest {
    private static final KeyboardDimens SIMPLE_KeyboardDimens = new KeyboardDimens() {
        @Override
        public int getKeyboardMaxWidth() {
            return 120;
        }

        @Override
        public int getKeyMaxWidth() {
            return 10;
        }

        @Override
        public float getKeyHorizontalGap() {
            return 1;
        }

        @Override
        public float getRowVerticalGap() {
            return 2;
        }

        @Override
        public int getNormalKeyHeight() {
            return 5;
        }

        @Override
        public int getSmallKeyHeight() {
            return 4;
        }

        @Override
        public int getLargeKeyHeight() {
            return 6;
        }
    };

    KeyboardAddOnAndBuilder mKeyboardBuilder;

    @Before
    public void setUp() {
        mKeyboardBuilder = KeyboardFactory.getAllAvailableKeyboards(RuntimeEnvironment.application).get(0);
    }

    @NonNull
    private AnyKeyboard createAndLoadKeyboardForMode(@Keyboard.KeyboardRowModeId int mode, int topRowIndex) {
        AnyKeyboard keyboard = Preconditions.checkNotNull(mKeyboardBuilder.createKeyboard(RuntimeEnvironment.application, mode));

        KeyboardExtension topRow = KeyboardExtensionFactory.getAllAvailableExtensions(RuntimeEnvironment.application, KeyboardExtension.TYPE_TOP).get(topRowIndex);
        KeyboardExtension bottomRow = KeyboardExtensionFactory.getCurrentKeyboardExtension(RuntimeEnvironment.application, KeyboardExtension.TYPE_BOTTOM);
        keyboard.loadKeyboard(SIMPLE_KeyboardDimens, topRow, bottomRow);


        return keyboard;
    }

    @Test
    public void testKeyboardRowNormalModeNoneTopRow() throws Exception {
        AnyKeyboard keyboard = createAndLoadKeyboardForMode(Keyboard.KEYBOARD_ROW_MODE_NORMAL, 0);

        Assert.assertEquals(40, keyboard.getHeight());
        Assert.assertEquals(36, keyboard.getKeys().size());
    }

    @Test
    public void testKeyboardRowImModeNoneTopRow() throws Exception {
        AnyKeyboard keyboard = createAndLoadKeyboardForMode(Keyboard.KEYBOARD_ROW_MODE_IM, 0);

        Assert.assertEquals(40, keyboard.getHeight());
        Assert.assertEquals(36, keyboard.getKeys().size());
    }

    @Test
    public void testKeyboardRowEmailModeNoneTopRow() throws Exception {
        AnyKeyboard keyboard = createAndLoadKeyboardForMode(Keyboard.KEYBOARD_ROW_MODE_EMAIL, 0);

        Assert.assertEquals(40, keyboard.getHeight());
        Assert.assertEquals(35, keyboard.getKeys().size());
    }

    @Test
    public void testKeyboardRowUrlModeNoneTopRow() throws Exception {
        AnyKeyboard keyboard = createAndLoadKeyboardForMode(Keyboard.KEYBOARD_ROW_MODE_URL, 0);

        Assert.assertEquals(40, keyboard.getHeight());
        Assert.assertEquals(35, keyboard.getKeys().size());
    }

    @Test
    public void testKeyboardRowPasswordModeNoneTopRow() throws Exception {
        AnyKeyboard keyboard = createAndLoadKeyboardForMode(Keyboard.KEYBOARD_ROW_MODE_PASSWORD, 0);

        Assert.assertEquals(46/*extra row*/, keyboard.getHeight());
        Assert.assertEquals(46/*additional 10 keys over normal*/, keyboard.getKeys().size());
    }

    @Test
    public void testKeyboardRowNormalModeSmallTopRow() throws Exception {
        AnyKeyboard keyboard = createAndLoadKeyboardForMode(Keyboard.KEYBOARD_ROW_MODE_NORMAL, 1);

        Assert.assertEquals(44, keyboard.getHeight());
        Assert.assertEquals(40, keyboard.getKeys().size());
    }

    @Test
    public void testKeyboardRowImModeSmallTopRow() throws Exception {
        AnyKeyboard keyboard = createAndLoadKeyboardForMode(Keyboard.KEYBOARD_ROW_MODE_IM, 1);

        Assert.assertEquals(44, keyboard.getHeight());
        Assert.assertEquals(40, keyboard.getKeys().size());
    }

    @Test
    public void testKeyboardRowEmailModeSmallTopRow() throws Exception {
        AnyKeyboard keyboard = createAndLoadKeyboardForMode(Keyboard.KEYBOARD_ROW_MODE_EMAIL, 1);

        Assert.assertEquals(44, keyboard.getHeight());
        Assert.assertEquals(39, keyboard.getKeys().size());
    }

    @Test
    public void testKeyboardRowUrlModeSmallTopRow() throws Exception {
        AnyKeyboard keyboard = createAndLoadKeyboardForMode(Keyboard.KEYBOARD_ROW_MODE_URL, 1);

        Assert.assertEquals(44, keyboard.getHeight());
        Assert.assertEquals(39, keyboard.getKeys().size());
    }

    @Test
    public void testKeyboardRowPasswordModeSmallTopRow() throws Exception {
        AnyKeyboard keyboard = createAndLoadKeyboardForMode(Keyboard.KEYBOARD_ROW_MODE_PASSWORD, 1);

        Assert.assertEquals(50/*extra row*/, keyboard.getHeight());
        Assert.assertEquals(50/*additional 10 keys over normal*/, keyboard.getKeys().size());
    }
}