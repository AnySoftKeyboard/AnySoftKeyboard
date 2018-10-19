package com.anysoftkeyboard.keyboards;

import static com.anysoftkeyboard.keyboards.ExternalAnyKeyboardTest.SIMPLE_KeyboardDimens;

import static java.util.Arrays.asList;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.addons.DefaultAddOn;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class PopupListKeyboardTest {
    @Test
    public void testEmptyCodes() {
        PopupListKeyboard keyboard = new PopupListKeyboard(new DefaultAddOn(getApplicationContext(),
                getApplicationContext()),
                getApplicationContext(), SIMPLE_KeyboardDimens, asList("one", "two", "three"),
                asList("v-one", "v-two", "v-three"), "POP_KEYBOARD");
        for (int keyIndex = 0; keyIndex < keyboard.getKeys().size(); keyIndex++) {
            Assert.assertEquals(0, keyboard.getKeys().get(keyIndex).getCodeAtIndex(0, false));
        }

        for (int keyIndex = 0; keyIndex < keyboard.getKeys().size(); keyIndex++) {
            Assert.assertEquals(0, keyboard.getKeys().get(keyIndex).getCodeAtIndex(0, true));
        }
    }
}