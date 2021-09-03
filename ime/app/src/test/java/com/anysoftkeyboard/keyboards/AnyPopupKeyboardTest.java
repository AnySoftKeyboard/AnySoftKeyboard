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
import emoji.utils.JavaEmojiUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
@Config(shadows = MyShadowPaint.class)
public class AnyPopupKeyboardTest {

    @NonNull
    private AnyPopupKeyboard createAnyPopupKeyboard(
            int keyboardResId, JavaEmojiUtils.SkinTone skinTone, JavaEmojiUtils.Gender gender) {
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

        Assert.assertEquals(77, keyboard.getKeys().size());
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
    @Ignore("till https://github.com/robolectric/robolectric/pull/6456 is merged")
    public void testKeyboardSwitchesSkinTone() throws Exception {
        AnyPopupKeyboard keyboardWithGeneric =
                createAnyPopupKeyboard(R.xml.quick_text_unicode_people, null, null);
        for (JavaEmojiUtils.SkinTone skinTone : JavaEmojiUtils.SkinTone.values()) {
            Assert.assertFalse(
                    EmojiUtils.containsSkinTone(
                            keyboardWithGeneric.getKeys().get(0).text, skinTone));
        }

        AnyPopupKeyboard keyboardWithSkinTone =
                createAnyPopupKeyboard(
                        R.xml.quick_text_unicode_people,
                        JavaEmojiUtils.SkinTone.Fitzpatrick_2,
                        null);
        for (JavaEmojiUtils.SkinTone skinTone : JavaEmojiUtils.SkinTone.values()) {
            Assert.assertEquals(
                    skinTone == JavaEmojiUtils.SkinTone.Fitzpatrick_2,
                    EmojiUtils.containsSkinTone(
                            keyboardWithSkinTone.getKeys().get(0).text, skinTone));
        }
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.N)
    @Ignore("till https://github.com/robolectric/robolectric/pull/6456 is merged")
    public void testKeyboardSwitchesGender() throws Exception {
        AnyPopupKeyboard keyboardWithSkinTone =
                createAnyPopupKeyboard(
                        R.xml.quick_text_unicode_people, null, JavaEmojiUtils.Gender.Man);
        for (JavaEmojiUtils.Gender gender : JavaEmojiUtils.Gender.values()) {
            Assert.assertEquals(
                    gender == JavaEmojiUtils.Gender.Man,
                    EmojiUtils.containsGender(keyboardWithSkinTone.getKeys().get(0).text, gender));
        }
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.N)
    @Ignore("till https://github.com/robolectric/robolectric/pull/6456 is merged")
    public void testKeyboardSwitchesGenderAndSkinTone() throws Exception {
        AnyPopupKeyboard keyboardWithSkinTone =
                createAnyPopupKeyboard(
                        R.xml.quick_text_unicode_people,
                        JavaEmojiUtils.SkinTone.Fitzpatrick_5,
                        JavaEmojiUtils.Gender.Woman);
        Assert.assertTrue(
                EmojiUtils.containsGender(
                        keyboardWithSkinTone.getKeys().get(0).text, JavaEmojiUtils.Gender.Woman));
        Assert.assertTrue(
                EmojiUtils.containsSkinTone(
                        keyboardWithSkinTone.getKeys().get(0).text,
                        JavaEmojiUtils.SkinTone.Fitzpatrick_5));
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.M)
    @Ignore("till https://github.com/robolectric/robolectric/pull/6456 is merged")
    public void testHidesKeysWithNoGlyph() throws Exception {
        AnyPopupKeyboard keyboard =
                createAnyPopupKeyboard(R.xml.quick_text_unicode_people, null, null);

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
