package com.anysoftkeyboard.keyboards;

import com.anysoftkeyboard.AnySoftKeyboardTestRunner;
import com.anysoftkeyboard.SharedPrefsHelper;
import com.menny.android.anysoftkeyboard.AnyApplication;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

import java.util.List;

@RunWith(AnySoftKeyboardTestRunner.class)
public class KeyboardFactoryTest {

    private KeyboardFactory mKeyboardFactory;

    @Before
    public void setUp() throws Exception {
        mKeyboardFactory = AnyApplication.getKeyboardFactory(RuntimeEnvironment.application);
    }

    @Test
    public void hasMultipleAlphabets() throws Exception {
        Assert.assertFalse(mKeyboardFactory.hasMultipleAlphabets());

        SharedPrefsHelper.ensureKeyboardAtIndexEnabled(1, true);
        Assert.assertTrue(mKeyboardFactory.hasMultipleAlphabets());

        SharedPrefsHelper.ensureKeyboardAtIndexEnabled(0, false);
        Assert.assertFalse(mKeyboardFactory.hasMultipleAlphabets());
    }

    @Test
    public void testDefaultKeyboardId() {
        final List<KeyboardAddOnAndBuilder> allAddOns = mKeyboardFactory.getAllAddOns();
        Assert.assertEquals(7, allAddOns.size());
        KeyboardAddOnAndBuilder addon = mKeyboardFactory.getEnabledAddOn();
        Assert.assertNotNull(addon);
        Assert.assertEquals("c7535083-4fe6-49dc-81aa-c5438a1a343a", addon.getId());

        Assert.assertTrue(mKeyboardFactory.isAddOnEnabledByDefault("c7535083-4fe6-49dc-81aa-c5438a1a343a"));
        Assert.assertFalse(mKeyboardFactory.isAddOnEnabledByDefault("c7535083-4fe6-49dc-81aa-c5438a1a343b"));
    }

}