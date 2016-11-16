package com.anysoftkeyboard.keyboards;

import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.keyboards.views.KeyDrawableStateProvider;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

@RunWith(RobolectricTestRunner.class)
public class ExternalAnyKeyboardTest {
    public static final KeyboardDimens SIMPLE_KeyboardDimens = new KeyboardDimens() {
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

    @Test
    public void testGeneralProperties() throws Exception {
        AnyKeyboard keyboard = KeyboardFactory.getAllAvailableKeyboards(RuntimeEnvironment.application).get(0).createKeyboard(RuntimeEnvironment.application, Keyboard.KEYBOARD_ROW_MODE_NORMAL);
        Assert.assertNotNull(keyboard);
        Assert.assertTrue(keyboard instanceof ExternalAnyKeyboard);
        Assert.assertEquals("en", keyboard.getDefaultDictionaryLocale());
        Assert.assertEquals("English", keyboard.getKeyboardName());
        Assert.assertEquals("keyboard_c7535083-4fe6-49dc-81aa-c5438a1a343a", keyboard.getKeyboardPrefId());
        Assert.assertEquals(R.drawable.ic_stat_en, keyboard.getKeyboardIconResId());
        Assert.assertEquals(1, keyboard.getKeyboardMode());
    }

    @Test
    public void testLoadedKeyboard() throws Exception {
        AnyKeyboard keyboard = KeyboardFactory.getAllAvailableKeyboards(RuntimeEnvironment.application).get(0).createKeyboard(RuntimeEnvironment.application, Keyboard.KEYBOARD_ROW_MODE_NORMAL);
        Assert.assertNotNull(keyboard);
        keyboard.loadKeyboard(SIMPLE_KeyboardDimens);

        Assert.assertEquals(10 * SIMPLE_KeyboardDimens.getKeyMaxWidth(), keyboard.getMinWidth());
        Assert.assertEquals(44, keyboard.getHeight());
        Assert.assertEquals(39, keyboard.getKeys().size());
        Assert.assertNotNull(keyboard.getShiftKey());
        Assert.assertEquals(KeyCodes.SHIFT, keyboard.getShiftKey().codes[0]);
    }

    @Test
    public void testDrawableState() throws Exception {
        //NOTE: this is used ONLY for the key's background drawable!
        AnyKeyboard keyboard = KeyboardFactory.getAllAvailableKeyboards(RuntimeEnvironment.application).get(0).createKeyboard(RuntimeEnvironment.application, Keyboard.KEYBOARD_ROW_MODE_NORMAL);
        Assert.assertNotNull(keyboard);
        keyboard.loadKeyboard(SIMPLE_KeyboardDimens);

        KeyDrawableStateProvider provider = new KeyDrawableStateProvider(1, 2, 3, 4, 5);
        AnyKeyboard.AnyKey key = (AnyKeyboard.AnyKey) keyboard.getKeys().get(4);
        Assert.assertFalse(key.isFunctional());
        Assert.assertArrayEquals(provider.KEY_STATE_NORMAL, key.getCurrentDrawableState(provider));
        key.onPressed();
        Assert.assertArrayEquals(provider.KEY_STATE_PRESSED, key.getCurrentDrawableState(provider));
        key.onReleased();
        Assert.assertArrayEquals(provider.KEY_STATE_NORMAL, key.getCurrentDrawableState(provider));

        AnyKeyboard.AnyKey shiftKey = (AnyKeyboard.AnyKey) keyboard.getShiftKey();
        Assert.assertNotNull(shiftKey);
        Assert.assertEquals(KeyCodes.SHIFT, shiftKey.getPrimaryCode());
        Assert.assertTrue(shiftKey.isFunctional());
        Assert.assertArrayEquals(provider.KEY_STATE_FUNCTIONAL_NORMAL, shiftKey.getCurrentDrawableState(provider));
        shiftKey.onPressed();
        Assert.assertArrayEquals(provider.KEY_STATE_FUNCTIONAL_PRESSED, shiftKey.getCurrentDrawableState(provider));
        shiftKey.onReleased();
        Assert.assertArrayEquals(provider.KEY_STATE_FUNCTIONAL_NORMAL, shiftKey.getCurrentDrawableState(provider));

        //enter
        AnyKeyboard.AnyKey enterKey = (AnyKeyboard.AnyKey) keyboard.getKeys().get(keyboard.getKeys().size() - 1);
        Assert.assertNotNull(enterKey);
        Assert.assertEquals(KeyCodes.ENTER, enterKey.getPrimaryCode());
        Assert.assertTrue(enterKey.isFunctional());
        Assert.assertArrayEquals(provider.KEY_STATE_ACTION_NORMAL, enterKey.getCurrentDrawableState(provider));
        enterKey.onPressed();
        Assert.assertArrayEquals(provider.KEY_STATE_ACTION_PRESSED, enterKey.getCurrentDrawableState(provider));
        enterKey.onReleased();
        Assert.assertArrayEquals(provider.KEY_STATE_ACTION_NORMAL, enterKey.getCurrentDrawableState(provider));
    }
}