package com.anysoftkeyboard.keyboardextensions;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class KeyboardExtensionFactoryTest {

    @Test
    public void testGetCurrentKeyboardExtensionBottomDefault() throws Exception {
        KeyboardExtension extension =
                AnyApplication.getBottomRowFactory(getApplicationContext()).getEnabledAddOn();
        Assert.assertNotNull(extension);
        Assert.assertEquals("09f8f280-dee2-11e0-9572-0800200c9a66", extension.getId());
        Assert.assertEquals(KeyboardExtension.TYPE_BOTTOM, extension.getExtensionType());
        Assert.assertEquals(
                R.xml.ext_kbd_bottom_row_regular_with_voice, extension.getKeyboardResId());
    }

    @Test
    public void testGetCurrentKeyboardExtensionBottomChanged() throws Exception {
        AnyApplication.getBottomRowFactory(getApplicationContext())
                .setAddOnEnabled("3659b9e0-dee2-11e0-9572-0800200c9a55", true);
        KeyboardExtension extension =
                AnyApplication.getBottomRowFactory(getApplicationContext()).getEnabledAddOn();
        Assert.assertNotNull(extension);
        Assert.assertEquals("3659b9e0-dee2-11e0-9572-0800200c9a55", extension.getId());
        Assert.assertEquals(KeyboardExtension.TYPE_BOTTOM, extension.getExtensionType());
        Assert.assertEquals(R.xml.ext_kbd_bottom_row_iphone, extension.getKeyboardResId());
    }

    @Test
    public void testGetCurrentKeyboardExtensionTopDefault() throws Exception {
        KeyboardExtension extension =
                AnyApplication.getTopRowFactory(getApplicationContext()).getEnabledAddOn();
        Assert.assertNotNull(extension);
        Assert.assertEquals("5d945f40-ded5-11e0-9572-0800200c9a66", extension.getId());
        Assert.assertEquals(KeyboardExtension.TYPE_TOP, extension.getExtensionType());
        Assert.assertEquals(R.xml.ext_kbd_top_row_small, extension.getKeyboardResId());
    }

    @Test
    public void testGetCurrentKeyboardExtensionTopChanged() throws Exception {
        AnyApplication.getTopRowFactory(getApplicationContext())
                .setAddOnEnabled("642e9690-ded5-11e0-9572-0800200c9a66", true);
        KeyboardExtension extension =
                AnyApplication.getTopRowFactory(getApplicationContext()).getEnabledAddOn();
        Assert.assertNotNull(extension);
        Assert.assertEquals("642e9690-ded5-11e0-9572-0800200c9a66", extension.getId());
        Assert.assertEquals(KeyboardExtension.TYPE_TOP, extension.getExtensionType());
        Assert.assertEquals(R.xml.ext_kbd_top_row_normal, extension.getKeyboardResId());
    }

    @Test
    public void testGetCurrentKeyboardExtensionExtensionDefault() throws Exception {
        KeyboardExtension extension =
                AnyApplication.getKeyboardExtensionFactory(getApplicationContext())
                        .getEnabledAddOn();
        Assert.assertNotNull(extension);
        Assert.assertEquals("6f1ecea0-dee2-11e0-9572-0800200c9a66", extension.getId());
        Assert.assertEquals(KeyboardExtension.TYPE_EXTENSION, extension.getExtensionType());
        Assert.assertEquals(
                R.xml.ext_kbd_ext_keyboard_numbers_symbols, extension.getKeyboardResId());
    }

    @Test
    public void testGetAllAvailableExtensions() throws Exception {
        assertBasicListDetails(
                AnyApplication.getBottomRowFactory(getApplicationContext()).getAllAddOns(),
                15,
                KeyboardExtension.TYPE_BOTTOM);
        assertBasicListDetails(
                AnyApplication.getTopRowFactory(getApplicationContext()).getAllAddOns(),
                11,
                KeyboardExtension.TYPE_TOP);
        assertBasicListDetails(
                AnyApplication.getKeyboardExtensionFactory(getApplicationContext()).getAllAddOns(),
                1,
                KeyboardExtension.TYPE_EXTENSION);
    }

    private void assertBasicListDetails(
            List<KeyboardExtension> availableExtensions,
            int extensionsCount,
            @KeyboardExtension.KeyboardExtensionType int type) {
        Assert.assertNotNull(availableExtensions);
        Assert.assertEquals(extensionsCount, availableExtensions.size());
        for (KeyboardExtension extension : availableExtensions) {
            Assert.assertNotNull(extension);
            Assert.assertEquals(type, extension.getExtensionType());
        }
    }
}
