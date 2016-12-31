package com.anysoftkeyboard.addons;

import android.content.Context;
import android.util.AttributeSet;

import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.HashSet;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
public class AddOnsFactoryTest {

    @Test
    public void testGetAllAddOns() throws Exception {
        TestableAddOnsFactory factory = new TestableAddOnsFactory(true);
        List<TestAddOn> list = factory.getAllAddOns(RuntimeEnvironment.application);
        Assert.assertTrue(list.size() > 0);

        HashSet<String> seenIds = new HashSet<>();
        for (AddOn addOn : list) {
            Assert.assertNotNull(addOn);
            Assert.assertFalse(seenIds.contains(addOn.getId()));
            seenIds.add(addOn.getId());
        }
    }

    private static final int STABLE_THEMES_COUNT = 10;

    @Test
    public void testFiltersDebugAddOnOnReleaseBuilds() throws Exception {
        TestableAddOnsFactory factory = new TestableAddOnsFactory(false);
        List<TestAddOn> list = factory.getAllAddOns(RuntimeEnvironment.application);
        Assert.assertEquals(STABLE_THEMES_COUNT, list.size());
    }

    @Test
    public void testDoesNotFiltersDebugAddOnOnDebugBuilds() throws Exception {
        TestableAddOnsFactory factory = new TestableAddOnsFactory(true);
        List<TestAddOn> list = factory.getAllAddOns(RuntimeEnvironment.application);
        //right now, we have 3 themes that are marked as dev.
        Assert.assertEquals(STABLE_THEMES_COUNT + 3, list.size());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetAllAddOnsReturnsUnmodifiableList() throws Exception {
        TestableAddOnsFactory factory = new TestableAddOnsFactory(true);
        List<TestAddOn> list = factory.getAllAddOns(RuntimeEnvironment.application);

        list.remove(0);
    }

    private static class TestAddOn extends AddOnImpl {
        TestAddOn(Context askContext, Context packageContext, String id, int nameResId, String description, int sortIndex) {
            super(askContext, packageContext, id, nameResId, description, sortIndex);
        }
    }

    private static class TestableAddOnsFactory extends AddOnsFactory<TestAddOn> {

        private TestableAddOnsFactory(boolean isDevBuild) {
            super("ASK_KT", "com.anysoftkeyboard.plugin.KEYBOARD_THEME", "com.anysoftkeyboard.plugindata.keyboardtheme",
                    "KeyboardThemes", "KeyboardTheme",
                    R.xml.keyboard_themes, true, isDevBuild);
        }

        @Override
        protected TestAddOn createConcreteAddOn(Context askContext, Context context, String prefId, int nameId, String description, int sortIndex, AttributeSet attrs) {
            return new TestAddOn(askContext, context, prefId, nameId, description, sortIndex);
        }
    }
}