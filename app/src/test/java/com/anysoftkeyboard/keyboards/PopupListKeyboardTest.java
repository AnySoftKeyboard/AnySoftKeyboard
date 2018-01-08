package com.anysoftkeyboard.keyboards;

import com.anysoftkeyboard.AnySoftKeyboardTestRunner;
import com.anysoftkeyboard.addons.DefaultAddOn;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

import java.util.Arrays;

@RunWith(AnySoftKeyboardTestRunner.class)
public class PopupListKeyboardTest {
    @Test
    public void testEmptyCodes() {
        PopupListKeyboard keyboard = new PopupListKeyboard(new DefaultAddOn(RuntimeEnvironment.application, RuntimeEnvironment.application),
                RuntimeEnvironment.application, ExternalAnyKeyboardTest.SIMPLE_KeyboardDimens, Arrays.asList("one", "two", "three"), Arrays.asList("v-one", "v-two", "v-three"), "POP_KEYBOARD");
        for (int keyIndex = 0; keyIndex < keyboard.getKeys().size(); keyIndex++) {
            Assert.assertEquals(0, keyboard.getKeys().get(keyIndex).getCodeAtIndex(0, false));
        }

        for (int keyIndex = 0; keyIndex < keyboard.getKeys().size(); keyIndex++) {
            Assert.assertEquals(0, keyboard.getKeys().get(keyIndex).getCodeAtIndex(0, true));
        }
    }
}