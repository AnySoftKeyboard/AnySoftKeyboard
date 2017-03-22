package com.anysoftkeyboard.keyboards;

import com.anysoftkeyboard.AnySoftKeyboardTestRunner;
import com.anysoftkeyboard.addons.DefaultAddOn;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(AnySoftKeyboardTestRunner.class)
public class AnyPopupKeyboardTest {
    @Test
    public void testKeyboardResourceConstructor() throws Exception {
        AnyPopupKeyboard keyboard = new AnyPopupKeyboard(new DefaultAddOn(RuntimeEnvironment.application, RuntimeEnvironment.application),
                RuntimeEnvironment.application, RuntimeEnvironment.application, R.xml.quick_text_unicode_emoticons, ExternalAnyKeyboardTest.SIMPLE_KeyboardDimens, "POP_KEYBOARD");
        Assert.assertEquals("POP_KEYBOARD", keyboard.getKeyboardName());

        Assert.assertEquals(77, keyboard.getKeys().size());
    }

    @Test
    @Config(sdk = 23)
    public void testKeyboardResourceConstructorReadsTags() throws Exception {
        AnyPopupKeyboard keyboard = new AnyPopupKeyboard(new DefaultAddOn(RuntimeEnvironment.application, RuntimeEnvironment.application),
                RuntimeEnvironment.application, RuntimeEnvironment.application, R.xml.quick_text_unicode_emoticons, ExternalAnyKeyboardTest.SIMPLE_KeyboardDimens, "POP_KEYBOARD");

        Assert.assertArrayEquals("face,grin".split(","), ((AnyKeyboard.AnyKey) keyboard.getKeys().get(0)).getKeyTags().toArray());
        Assert.assertArrayEquals("eye,face,grin,smile".split(","), ((AnyKeyboard.AnyKey) keyboard.getKeys().get(1)).getKeyTags().toArray());
    }
}