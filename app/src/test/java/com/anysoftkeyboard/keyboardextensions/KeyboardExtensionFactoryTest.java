package com.anysoftkeyboard.keyboardextensions;

import com.anysoftkeyboard.SharedPrefsHelper;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;

@RunWith(RobolectricTestRunner.class)
public class KeyboardExtensionFactoryTest {

    @Test
    public void testGetCurrentKeyboardExtensionBottomDefault() throws Exception {
        KeyboardExtension extension = KeyboardExtensionFactory.getCurrentKeyboardExtension(RuntimeEnvironment.application, KeyboardExtension.TYPE_BOTTOM);
        Assert.assertNotNull(extension);
        Assert.assertEquals("09f8f280-dee2-11e0-9572-0800200c9a66", extension.getId());
        Assert.assertEquals(KeyboardExtension.TYPE_BOTTOM, extension.getExtensionType());
        Assert.assertEquals(R.xml.ext_kbd_bottom_row_regular_with_voice, extension.getKeyboardResId());
    }

    @Test
    public void testGetCurrentKeyboardExtensionBottomChanged() throws Exception {
        SharedPrefsHelper.setPrefsValue(RuntimeEnvironment.application.getString(R.string.settings_key_ext_kbd_bottom_row_key), "3659b9e0-dee2-11e0-9572-0800200c9a55");
        KeyboardExtension extension = KeyboardExtensionFactory.getCurrentKeyboardExtension(RuntimeEnvironment.application, KeyboardExtension.TYPE_BOTTOM);
        Assert.assertNotNull(extension);
        Assert.assertEquals("3659b9e0-dee2-11e0-9572-0800200c9a55", extension.getId());
        Assert.assertEquals(KeyboardExtension.TYPE_BOTTOM, extension.getExtensionType());
        Assert.assertEquals(R.xml.ext_kbd_bottom_row_iphone, extension.getKeyboardResId());
    }

    @Test
    public void testGetCurrentKeyboardExtensionTopDefault() throws Exception {
        KeyboardExtension extension = KeyboardExtensionFactory.getCurrentKeyboardExtension(RuntimeEnvironment.application, KeyboardExtension.TYPE_TOP);
        Assert.assertNotNull(extension);
        Assert.assertEquals("5d945f40-ded5-11e0-9572-0800200c9a66", extension.getId());
        Assert.assertEquals(KeyboardExtension.TYPE_TOP, extension.getExtensionType());
        Assert.assertEquals(R.xml.ext_kbd_top_row_small, extension.getKeyboardResId());
    }

    @Test
    public void testGetCurrentKeyboardExtensionTopChanged() throws Exception {
        SharedPrefsHelper.setPrefsValue(RuntimeEnvironment.application.getString(R.string.settings_key_ext_kbd_top_row_key), "642e9690-ded5-11e0-9572-0800200c9a66");
        KeyboardExtension extension = KeyboardExtensionFactory.getCurrentKeyboardExtension(RuntimeEnvironment.application, KeyboardExtension.TYPE_TOP);
        Assert.assertNotNull(extension);
        Assert.assertEquals("642e9690-ded5-11e0-9572-0800200c9a66", extension.getId());
        Assert.assertEquals(KeyboardExtension.TYPE_TOP, extension.getExtensionType());
        Assert.assertEquals(R.xml.ext_kbd_top_row_normal, extension.getKeyboardResId());
    }

    @Test
    public void testGetCurrentKeyboardExtensionEmptyChanged() throws Exception {
        SharedPrefsHelper.setPrefsValue(RuntimeEnvironment.application.getString(R.string.settings_key_ext_kbd_top_row_key), "82910c70-ded1-11e0-9572-0800200c9a66");
        KeyboardExtension extension = KeyboardExtensionFactory.getCurrentKeyboardExtension(RuntimeEnvironment.application, KeyboardExtension.TYPE_TOP);
        Assert.assertNotNull(extension);
        Assert.assertEquals("82910c70-ded1-11e0-9572-0800200c9a66", extension.getId());
        Assert.assertEquals(KeyboardExtension.TYPE_TOP, extension.getExtensionType());
        Assert.assertEquals(0, extension.getKeyboardResId());
    }

    @Test
    public void testGetCurrentKeyboardExtensionExtensionDefault() throws Exception {
        KeyboardExtension extension = KeyboardExtensionFactory.getCurrentKeyboardExtension(RuntimeEnvironment.application, KeyboardExtension.TYPE_EXTENSION);
        Assert.assertNotNull(extension);
        Assert.assertEquals("6f1ecea0-dee2-11e0-9572-0800200c9a66", extension.getId());
        Assert.assertEquals(KeyboardExtension.TYPE_EXTENSION, extension.getExtensionType());
        Assert.assertEquals(R.xml.ext_kbd_ext_keyboard_numbers_symbols, extension.getKeyboardResId());

    }

    @Test
    public void testGetAllAvailableExtensions() throws Exception {
        assertBasicListDetails(KeyboardExtensionFactory.getAllAvailableExtensions(RuntimeEnvironment.application, KeyboardExtension.TYPE_BOTTOM), 7, KeyboardExtension.TYPE_BOTTOM);
        assertBasicListDetails(KeyboardExtensionFactory.getAllAvailableExtensions(RuntimeEnvironment.application, KeyboardExtension.TYPE_TOP), 6, KeyboardExtension.TYPE_TOP);
        assertBasicListDetails(KeyboardExtensionFactory.getAllAvailableExtensions(RuntimeEnvironment.application, KeyboardExtension.TYPE_EXTENSION), 1, KeyboardExtension.TYPE_EXTENSION);
    }

    private void assertBasicListDetails(ArrayList<KeyboardExtension> availableExtensions, int extensionsCount, @KeyboardExtension.KeyboardExtensionType int type) {
        Assert.assertNotNull(availableExtensions);
        Assert.assertEquals(extensionsCount, availableExtensions.size());
        for (KeyboardExtension extension : availableExtensions) {
            Assert.assertNotNull(extension);
            Assert.assertEquals(type, extension.getExtensionType());
        }
    }
}