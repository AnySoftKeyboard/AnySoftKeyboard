package com.anysoftkeyboard.keyboards;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.addons.SupportTest;
import com.menny.android.anysoftkeyboard.AnyApplication;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class KeyboardFactoryTest {

    private KeyboardFactory mKeyboardFactory;

    @Before
    public void setUp() throws Exception {
        mKeyboardFactory = AnyApplication.getKeyboardFactory(getApplicationContext());
    }

    @Test
    public void hasMultipleAlphabets() throws Exception {
        Assert.assertFalse(mKeyboardFactory.hasMultipleAlphabets());

        SupportTest.ensureKeyboardAtIndexEnabled(1, true);
        Assert.assertTrue(mKeyboardFactory.hasMultipleAlphabets());

        SupportTest.ensureKeyboardAtIndexEnabled(0, false);
        Assert.assertFalse(mKeyboardFactory.hasMultipleAlphabets());
    }

    @Test
    public void testDefaultKeyboardId() {
        final List<KeyboardAddOnAndBuilder> allAddOns = mKeyboardFactory.getAllAddOns();
        Assert.assertEquals(9, allAddOns.size());
        KeyboardAddOnAndBuilder addon = mKeyboardFactory.getEnabledAddOn();
        Assert.assertNotNull(addon);
        Assert.assertEquals("c7535083-4fe6-49dc-81aa-c5438a1a343a", addon.getId());

        Assert.assertTrue(mKeyboardFactory.isAddOnEnabledByDefault("c7535083-4fe6-49dc-81aa-c5438a1a343a"));
        Assert.assertFalse(mKeyboardFactory.isAddOnEnabledByDefault("c7535083-4fe6-49dc-81aa-c5438a1a343b"));
    }

}