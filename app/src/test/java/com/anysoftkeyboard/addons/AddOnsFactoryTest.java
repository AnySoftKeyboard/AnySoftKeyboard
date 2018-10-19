package com.anysoftkeyboard.addons;

import static com.menny.android.anysoftkeyboard.R.string.settings_default_keyboard_theme_key;
import static com.menny.android.anysoftkeyboard.R.xml.keyboard_themes;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.StringRes;
import android.support.v4.content.SharedPreferencesCompat;
import android.util.AttributeSet;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.keyboards.KeyboardAddOnAndBuilder;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashSet;
import java.util.List;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class AddOnsFactoryTest {

    private static final int STABLE_THEMES_COUNT = 13;
    private static final int UNSTABLE_THEMES_COUNT = 2;

    @Test(expected = IllegalArgumentException.class)
    public void testMustSupplyPrefix() throws Exception {
        new AddOnsFactory.SingleAddOnsFactory<TestAddOn>(getApplicationContext(),
                "ASK_KT", "com.anysoftkeyboard.plugin.KEYBOARD_THEME", "com.anysoftkeyboard.plugindata.keyboardtheme",
                "KeyboardThemes", "KeyboardTheme", ""/*empty pref-prefix*/,
                keyboard_themes, settings_default_keyboard_theme_key, true) {

            @Override
            public void setAddOnEnabled(CharSequence addOnId, boolean enabled) {}

            @Override
            protected TestAddOn createConcreteAddOn(Context askContext, Context context, int apiVersion, CharSequence prefId, CharSequence name, CharSequence description, boolean isHidden, int sortIndex, AttributeSet attrs) {
                return null;
            }
        };
    }

    @Test
    public void testGetAllAddOns() throws Exception {
        TestableAddOnsFactory factory = new TestableAddOnsFactory(true);
        List<TestAddOn> list = factory.getAllAddOns();
        Assert.assertTrue(list.size() > 0);

        HashSet<CharSequence> seenIds = new HashSet<>();
        for (AddOn addOn : list) {
            Assert.assertNotNull(addOn);
            Assert.assertFalse(seenIds.contains(addOn.getId()));
            seenIds.add(addOn.getId());
        }
    }

    @Test
    public void testFiltersDebugAddOnOnReleaseBuilds() throws Exception {
        TestableAddOnsFactory factory = new TestableAddOnsFactory(false);
        List<TestAddOn> list = factory.getAllAddOns();
        Assert.assertEquals(STABLE_THEMES_COUNT, list.size());
    }

    @Test
    public void testDoesNotFiltersDebugAddOnOnDebugBuilds() throws Exception {
        TestableAddOnsFactory factory = new TestableAddOnsFactory(true);
        List<TestAddOn> list = factory.getAllAddOns();
        //right now, we have 3 themes that are marked as dev.
        Assert.assertEquals(STABLE_THEMES_COUNT + UNSTABLE_THEMES_COUNT, list.size());
    }

    @Test
    public void testParsesApiLevel() {
        final KeyboardAddOnAndBuilder english16Keys = AnyApplication.getKeyboardFactory(getApplicationContext()).getAddOnById("12335055-4aa6-49dc-8456-c7d38a1a5123");
        Assert.assertNotNull(english16Keys);
        Assert.assertNotEquals(0, english16Keys.getApiVersion());
        Assert.assertEquals(getApplicationContext().getResources().getInteger(R.integer.anysoftkeyboard_api_version_code), english16Keys.getApiVersion());
    }

    @Test
    public void testHiddenAddOnsAreNotReturned() throws Exception {
        TestableAddOnsFactory factory = new TestableAddOnsFactory(false);
        List<TestAddOn> list = factory.getAllAddOns();
        final String hiddenThemeId = "2a94cf8c-266c-47fd-8c8c-c9c57d28d7dc";
        Assert.assertEquals(hiddenThemeId, getApplicationContext().getString(R.string.fallback_keyboard_theme_id));
        //ensuring we can get this hidden theme by calling it specifically
        final AddOn hiddenAddOn = factory.getAddOnById(hiddenThemeId);
        Assert.assertNotNull(hiddenAddOn);
        Assert.assertEquals(hiddenThemeId, hiddenAddOn.getId());
        //ensuring the hidden theme is not in the list of all themes
        for (TestAddOn addOn : list) {
            Assert.assertNotEquals(hiddenThemeId, addOn.getId());
            Assert.assertNotSame(hiddenAddOn, addOn);
            Assert.assertNotEquals(hiddenAddOn.getId(), addOn.getId());
        }

    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetAllAddOnsReturnsUnmodifiableList() throws Exception {
        TestableAddOnsFactory factory = new TestableAddOnsFactory(true);
        List<TestAddOn> list = factory.getAllAddOns();

        list.remove(0);
    }

    @Test
    public void testOnlyOneEnabledAddOnWhenSingleSelection() throws Exception {
        TestableSingleAddOnsFactory factory = new TestableSingleAddOnsFactory();
        Assert.assertEquals(1, factory.getEnabledAddOns().size());
        TestAddOn initialAddOn = factory.getEnabledAddOns().get(0);
        Assert.assertSame(initialAddOn, factory.getEnabledAddOn());

        factory.setAddOnEnabled(factory.getAllAddOns().get(0).getId(), true);
        Assert.assertEquals(1, factory.getEnabledAddOns().size());
        TestAddOn secondAddOn = factory.getEnabledAddOns().get(0);
        Assert.assertSame(secondAddOn, factory.getEnabledAddOn());
        Assert.assertNotEquals(secondAddOn.getId(), initialAddOn.getId());

        //disabling the enabled add on should re-enabled the default
        factory.setAddOnEnabled(secondAddOn.getId(), false);
        Assert.assertEquals(1, factory.getEnabledAddOns().size());
        TestAddOn reEnabledAddOn = factory.getEnabledAddOns().get(0);
        Assert.assertSame(reEnabledAddOn, factory.getEnabledAddOn());
        Assert.assertNotEquals(secondAddOn.getId(), reEnabledAddOn.getId());
        Assert.assertEquals(initialAddOn.getId(), reEnabledAddOn.getId());

        //but disabling default does not change
        factory.setAddOnEnabled(reEnabledAddOn.getId(), false);
        Assert.assertEquals(1, factory.getEnabledAddOns().size());
        TestAddOn fallbackAddOn = factory.getEnabledAddOns().get(0);
        Assert.assertSame(fallbackAddOn, factory.getEnabledAddOn());
        Assert.assertEquals(fallbackAddOn.getId(), initialAddOn.getId());

    }

    @Test
    public void testManyEnabledAddOnWhenMultiSelection() throws Exception {
        TestableMultiAddOnsFactory factory = new TestableMultiAddOnsFactory();
        Assert.assertEquals(1, factory.getEnabledAddOns().size());
        TestAddOn initialAddOn = factory.getEnabledAddOns().get(0);
        Assert.assertSame(initialAddOn, factory.getEnabledAddOn());

        factory.setAddOnEnabled(factory.getAllAddOns().get(0).getId(), true);
        Assert.assertEquals(2, factory.getEnabledAddOns().size());
        TestAddOn firstAddOn = factory.getEnabledAddOns().get(0);
        TestAddOn secondAddOn = factory.getEnabledAddOns().get(1);
        Assert.assertSame(firstAddOn, factory.getEnabledAddOn());

        Assert.assertEquals(firstAddOn.getId(), factory.getAllAddOns().get(0).getId());
        Assert.assertEquals(secondAddOn.getId(), initialAddOn.getId());

        factory.setAddOnEnabled(secondAddOn.getId(), false);
        Assert.assertEquals(1, factory.getEnabledAddOns().size());
        TestAddOn enableAddOn = factory.getEnabledAddOns().get(0);
        Assert.assertSame(enableAddOn, factory.getEnabledAddOn());
        Assert.assertEquals(firstAddOn.getId(), enableAddOn.getId());

        //but disabling keeps the default
        factory.setAddOnEnabled(firstAddOn.getId(), false);
        Assert.assertEquals(1, factory.getEnabledAddOns().size());
        TestAddOn fallbackAddOn = factory.getEnabledAddOns().get(0);
        Assert.assertSame(fallbackAddOn, factory.getEnabledAddOn());
        Assert.assertEquals(fallbackAddOn.getId(), initialAddOn.getId());
        //and even if we try to disable, it still enabled
        factory.setAddOnEnabled(initialAddOn.getId(), false);
        Assert.assertEquals(1, factory.getEnabledAddOns().size());
        TestAddOn defaultAddOn = factory.getEnabledAddOns().get(0);
        Assert.assertSame(defaultAddOn, factory.getEnabledAddOn());
        Assert.assertEquals(defaultAddOn.getId(), initialAddOn.getId());
    }

    public static void clearFactoryCache(AddOnsFactory<?> factory) {
        factory.clearAddOnList();
    }

    private static class TestAddOn extends AddOnImpl {
        TestAddOn(Context askContext, Context packageContext, int apiVersion, CharSequence id, CharSequence name, CharSequence description, boolean isHidden, int sortIndex) {
            super(askContext, packageContext, apiVersion, id, name, description, isHidden, sortIndex);
        }
    }

    private static class TestableAddOnsFactory extends AddOnsFactory<TestAddOn> {

        private TestableAddOnsFactory(boolean isDevBuild) {
            this(R.string.settings_default_keyboard_theme_key, isDevBuild);
        }

        private TestableAddOnsFactory(@StringRes int defaultAddOnId, boolean isDevBuild) {
            super(getApplicationContext(), "ASK_KT", "com.anysoftkeyboard.plugin.KEYBOARD_THEME", "com.anysoftkeyboard.plugindata.keyboardtheme",
                    "KeyboardThemes", "KeyboardTheme", "test_",
                    keyboard_themes, defaultAddOnId, true, isDevBuild);
        }

        @Override
        public void setAddOnEnabled(CharSequence addOnId, boolean enabled) {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            setAddOnEnableValueInPrefs(editor, addOnId, enabled);
            SharedPreferencesCompat.EditorCompat.getInstance().apply(editor);
        }

        @Override
        protected TestAddOn createConcreteAddOn(Context askContext, Context context, int apiVersion, CharSequence prefId, CharSequence name, CharSequence description, boolean isHidden, int sortIndex, AttributeSet attrs) {
            return new TestAddOn(askContext, context, apiVersion, prefId, name, description, isHidden, sortIndex);
        }
    }

    private static class TestableSingleAddOnsFactory extends AddOnsFactory.SingleAddOnsFactory<TestAddOn> {
        protected TestableSingleAddOnsFactory() {
            super(getApplicationContext(), "ASK_KT", "com.anysoftkeyboard.plugin.KEYBOARD_THEME", "com.anysoftkeyboard.plugindata.keyboardtheme",
                    "KeyboardThemes", "KeyboardTheme", "test_",
                    keyboard_themes, settings_default_keyboard_theme_key, true);
        }

        @Override
        protected TestAddOn createConcreteAddOn(Context askContext, Context context, int apiVersion, CharSequence prefId, CharSequence name, CharSequence description, boolean isHidden, int sortIndex, AttributeSet attrs) {
            return new TestAddOn(askContext, context, apiVersion, prefId, name, description, isHidden, sortIndex);
        }
    }

    private static class TestableMultiAddOnsFactory extends AddOnsFactory.MultipleAddOnsFactory<TestAddOn> {
        protected TestableMultiAddOnsFactory() {
            super(getApplicationContext(), "ASK_KT", "com.anysoftkeyboard.plugin.KEYBOARD_THEME", "com.anysoftkeyboard.plugindata.keyboardtheme",
                    "KeyboardThemes", "KeyboardTheme", "test_",
                    keyboard_themes, settings_default_keyboard_theme_key, true);
        }

        @Override
        protected TestAddOn createConcreteAddOn(Context askContext, Context context, int apiVersion, CharSequence prefId, CharSequence name, CharSequence description, boolean isHidden, int sortIndex, AttributeSet attrs) {
            return new TestAddOn(askContext, context, apiVersion, prefId, name, description, isHidden, sortIndex);
        }
    }
}
