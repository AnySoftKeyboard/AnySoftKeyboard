package com.anysoftkeyboard.keyboards;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.content.Context;
import android.text.TextUtils;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.addons.AddOn;
import com.anysoftkeyboard.addons.DefaultAddOn;
import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.keyboards.views.KeyDrawableStateProvider;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class ExternalAnyKeyboardTest {
    public static final KeyboardDimens SIMPLE_KeyboardDimens =
            new KeyboardDimens() {
                @Override
                public int getKeyboardMaxWidth() {
                    return 480;
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

                @Override
                public float getPaddingBottom() {
                    return 0;
                }
            };

    private AddOn mDefaultAddOn;
    private Context mContext;

    @Before
    public void setup() {
        mContext = getApplicationContext();
        mDefaultAddOn = new DefaultAddOn(mContext, mContext);
    }

    @Test
    public void testGeneralProperties() throws Exception {
        AnyKeyboard keyboard =
                AnyApplication.getKeyboardFactory(getApplicationContext())
                        .getEnabledAddOn()
                        .createKeyboard(Keyboard.KEYBOARD_ROW_MODE_NORMAL);
        Assert.assertNotNull(keyboard);
        Assert.assertTrue(keyboard instanceof ExternalAnyKeyboard);
        Assert.assertEquals("en", keyboard.getDefaultDictionaryLocale());
        Assert.assertEquals("English", keyboard.getKeyboardName());
        Assert.assertEquals("c7535083-4fe6-49dc-81aa-c5438a1a343a", keyboard.getKeyboardId());
        Assert.assertEquals(R.drawable.ic_status_english, keyboard.getKeyboardIconResId());
        Assert.assertEquals(1, keyboard.getKeyboardMode());
    }

    @Test
    public void testLoadedKeyboard() throws Exception {
        AnyKeyboard keyboard =
                AnyApplication.getKeyboardFactory(getApplicationContext())
                        .getEnabledAddOn()
                        .createKeyboard(Keyboard.KEYBOARD_ROW_MODE_NORMAL);
        Assert.assertNotNull(keyboard);
        keyboard.loadKeyboard(SIMPLE_KeyboardDimens);

        Assert.assertEquals(SIMPLE_KeyboardDimens.getKeyboardMaxWidth(), keyboard.getMinWidth());
        Assert.assertEquals(48, keyboard.getHeight());
        Assert.assertEquals(40, keyboard.getKeys().size());
        Assert.assertNotNull(keyboard.getShiftKey());
        Assert.assertEquals(KeyCodes.SHIFT, keyboard.getShiftKey().mCodes[0]);
    }

    @Test
    public void testDrawableState() throws Exception {
        // NOTE: this is used ONLY for the key's background drawable!
        AnyKeyboard keyboard =
                AnyApplication.getKeyboardFactory(getApplicationContext())
                        .getEnabledAddOn()
                        .createKeyboard(Keyboard.KEYBOARD_ROW_MODE_NORMAL);
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
        Assert.assertArrayEquals(
                provider.KEY_STATE_FUNCTIONAL_NORMAL, shiftKey.getCurrentDrawableState(provider));
        shiftKey.onPressed();
        Assert.assertArrayEquals(
                provider.KEY_STATE_FUNCTIONAL_PRESSED, shiftKey.getCurrentDrawableState(provider));
        shiftKey.onReleased();
        Assert.assertArrayEquals(
                provider.KEY_STATE_FUNCTIONAL_NORMAL, shiftKey.getCurrentDrawableState(provider));

        // enter
        AnyKeyboard.AnyKey enterKey =
                (AnyKeyboard.AnyKey) keyboard.getKeys().get(keyboard.getKeys().size() - 1);
        Assert.assertNotNull(enterKey);
        Assert.assertEquals(KeyCodes.ENTER, enterKey.getPrimaryCode());
        Assert.assertTrue(enterKey.isFunctional());
        Assert.assertArrayEquals(
                provider.KEY_STATE_ACTION_NORMAL, enterKey.getCurrentDrawableState(provider));
        enterKey.onPressed();
        Assert.assertArrayEquals(
                provider.KEY_STATE_ACTION_PRESSED, enterKey.getCurrentDrawableState(provider));
        enterKey.onReleased();
        Assert.assertArrayEquals(
                provider.KEY_STATE_ACTION_NORMAL, enterKey.getCurrentDrawableState(provider));
    }

    @Test
    public void testCodesParsing() throws Exception {
        ExternalAnyKeyboard keyboard =
                new ExternalAnyKeyboard(
                        mDefaultAddOn,
                        mContext,
                        mContext,
                        R.xml.keyboard_with_codes_as_letters,
                        R.xml.keyboard_with_codes_as_letters,
                        "test",
                        R.drawable.sym_keyboard_notification_icon,
                        0,
                        "en",
                        "",
                        "",
                        Keyboard.KEYBOARD_ROW_MODE_NORMAL);
        keyboard.loadKeyboard(SIMPLE_KeyboardDimens);

        final Keyboard.Key keyZ = keyboard.getKeys().get(0);
        Assert.assertNotNull(keyZ);
        Assert.assertEquals((int) 'z', keyZ.getPrimaryCode());
        Assert.assertEquals((int) 'z', keyZ.getCodeAtIndex(0, false));
        Assert.assertEquals((int) 'Z', keyZ.getCodeAtIndex(0, true));
        Assert.assertEquals("1żžź", keyZ.popupCharacters.toString());
        Assert.assertEquals(R.xml.popup_one_row, keyZ.popupResId);

        final Keyboard.Key keyX = keyboard.getKeys().get(1);
        Assert.assertNotNull(keyX);
        Assert.assertEquals((int) 'x', keyX.getPrimaryCode());
        Assert.assertEquals((int) 'x', keyX.getCodeAtIndex(0, false));
        Assert.assertEquals((int) 'X', keyX.getCodeAtIndex(0, true));
        Assert.assertTrue(TextUtils.isEmpty(keyX.popupCharacters));
        Assert.assertEquals(0, keyX.popupResId);

        /*disabled due to Robolectric issue: https://github.com/robolectric/robolectric/pull/3671
                final AnyKeyboard.AnyKey key3 = (AnyKeyboard.AnyKey) keyboard.getKeys().get(2);
                Assert.assertNotNull(key3);
                Assert.assertEquals("\'", key3.label.toString());
                Assert.assertEquals((int) '\'', key3.getPrimaryCode());
                Assert.assertEquals((int) '\'', key3.getCodeAtIndex(0, false));
                Assert.assertEquals((int) '\"', key3.getCodeAtIndex(0, true));
                Assert.assertEquals("„\"”", key3.popupCharacters.toString());
                Assert.assertEquals(R.xml.popup_one_row, key3.popupResId);
                Assert.assertTrue(key3.isFunctional());
        */
        final AnyKeyboard.AnyKey keyMinus4 = (AnyKeyboard.AnyKey) keyboard.getKeys().get(3);
        Assert.assertNotNull(keyMinus4);
        Assert.assertEquals(-4, keyMinus4.getPrimaryCode());
        Assert.assertEquals(-4, keyMinus4.getCodeAtIndex(0, false));
        Assert.assertEquals(-4, keyMinus4.getCodeAtIndex(0, true));
        Assert.assertEquals("f", keyMinus4.popupCharacters.toString());
        Assert.assertEquals(R.xml.popup_one_row, keyMinus4.popupResId);
        Assert.assertTrue(keyMinus4.isFunctional());

        final AnyKeyboard.AnyKey keyMinus5 = (AnyKeyboard.AnyKey) keyboard.getKeys().get(4);
        Assert.assertNotNull(keyMinus5);
        Assert.assertEquals(-5, keyMinus5.getPrimaryCode());
        Assert.assertEquals(-5, keyMinus5.getCodeAtIndex(0, false));
        Assert.assertEquals(-5, keyMinus5.getCodeAtIndex(0, true));
        Assert.assertTrue(TextUtils.isEmpty(keyMinus5.popupCharacters));
        Assert.assertEquals(0, keyMinus5.popupResId);
        Assert.assertTrue(keyMinus5.isFunctional());

        final AnyKeyboard.AnyKey keyP = (AnyKeyboard.AnyKey) keyboard.getKeys().get(5);
        Assert.assertNotNull(keyP);
        Assert.assertEquals((int) 'p', keyP.getPrimaryCode());
        Assert.assertEquals('p', keyP.getCodeAtIndex(0, false));
        Assert.assertEquals('P', keyP.getCodeAtIndex(0, true));
        Assert.assertEquals('a', keyP.getCodeAtIndex(1, false));
        Assert.assertEquals('A', keyP.getCodeAtIndex(1, true));
        Assert.assertEquals('b', keyP.getCodeAtIndex(2, false));
        Assert.assertEquals('B', keyP.getCodeAtIndex(2, true));
        Assert.assertTrue(TextUtils.isEmpty(keyP.popupCharacters));
        Assert.assertEquals(0, keyP.popupResId);
        Assert.assertFalse(keyP.isFunctional());

        final AnyKeyboard.AnyKey key99 = (AnyKeyboard.AnyKey) keyboard.getKeys().get(6);
        Assert.assertNotNull(keyP);
        Assert.assertEquals(99, key99.getPrimaryCode());
        Assert.assertEquals('c', key99.getCodeAtIndex(0, false));
        Assert.assertEquals('C', key99.getCodeAtIndex(0, true));
        Assert.assertEquals('d', key99.getCodeAtIndex(1, false));
        Assert.assertEquals('D', key99.getCodeAtIndex(1, true));
        Assert.assertEquals('e', key99.getCodeAtIndex(2, false));
        Assert.assertEquals('E', key99.getCodeAtIndex(2, true));
        Assert.assertEquals("ĥ", key99.popupCharacters.toString());
        Assert.assertEquals(R.xml.popup_one_row, key99.popupResId);
        Assert.assertFalse(key99.isFunctional());
    }
}
