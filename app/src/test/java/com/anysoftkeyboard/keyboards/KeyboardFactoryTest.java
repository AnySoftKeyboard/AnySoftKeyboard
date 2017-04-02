package com.anysoftkeyboard.keyboards;

import com.anysoftkeyboard.AnySoftKeyboardTestRunner;
import com.anysoftkeyboard.SharedPrefsHelper;
import com.menny.android.anysoftkeyboard.AnyApplication;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

import java.util.List;

@RunWith(AnySoftKeyboardTestRunner.class)
public class KeyboardFactoryTest {

    @Test
    public void hasMultipleAlphabets() throws Exception {
        Assert.assertFalse(AnyApplication.getKeyboardFactory(RuntimeEnvironment.application).hasMultipleAlphabets());

        SharedPrefsHelper.ensureKeyboardAtIndexEnabled(1, true);
        Assert.assertTrue(AnyApplication.getKeyboardFactory(RuntimeEnvironment.application).hasMultipleAlphabets());

        SharedPrefsHelper.ensureKeyboardAtIndexEnabled(0, false);
        Assert.assertFalse(AnyApplication.getKeyboardFactory(RuntimeEnvironment.application).hasMultipleAlphabets());
    }

    @Test
    public void testDefaultKeyboardId() {
        final List<KeyboardAddOnAndBuilder> allAddOns = AnyApplication.getKeyboardFactory(RuntimeEnvironment.application).getAllAddOns();
        Assert.assertEquals(3, allAddOns.size());
        KeyboardAddOnAndBuilder addon = AnyApplication.getKeyboardFactory(RuntimeEnvironment.application).getEnabledAddOn();
        Assert.assertNotNull(addon);
        Assert.assertEquals("c7535083-4fe6-49dc-81aa-c5438a1a343a", addon.getId());
    }

}