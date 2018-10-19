package com.anysoftkeyboard.dictionaries;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.test.SharedPrefsHelper;
import com.menny.android.anysoftkeyboard.AnyApplication;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class ExternalDictionaryFactoryTest {

    private ExternalDictionaryFactory mFactory;

    @Before
    public void setUp() throws Exception {
        mFactory = AnyApplication.getExternalDictionaryFactory(getApplicationContext());
    }

    @Test
    public void testPrefKey() {
        Assert.assertFalse(ExternalDictionaryFactory.isOverrideDictionaryPrefKey("sdfsdf"));
        Assert.assertFalse(ExternalDictionaryFactory.isOverrideDictionaryPrefKey("keyboard_sdfsdfsd"));
        Assert.assertFalse(ExternalDictionaryFactory.isOverrideDictionaryPrefKey("_override_dictionary"));
        Assert.assertFalse(ExternalDictionaryFactory.isOverrideDictionaryPrefKey(null));
        Assert.assertFalse(ExternalDictionaryFactory.isOverrideDictionaryPrefKey(""));
        Assert.assertTrue(ExternalDictionaryFactory.isOverrideDictionaryPrefKey("keyboard_sdfsdf_override_dictionary"));

        AnyKeyboard keyboard = Mockito.mock(AnyKeyboard.class);
        Mockito.doReturn("some_id").when(keyboard).getKeyboardId();
        final String prefKey = ExternalDictionaryFactory.getDictionaryOverrideKey(keyboard);
        Assert.assertNotNull(prefKey);

        Assert.assertTrue(ExternalDictionaryFactory.isOverrideDictionaryPrefKey(prefKey));
    }

    @Test
    public void testDefault() {
        final List<DictionaryAddOnAndBuilder> enabledAddOns = mFactory.getEnabledAddOns();
        Assert.assertNotNull(enabledAddOns);
        Assert.assertEquals(1, enabledAddOns.size());
        Assert.assertSame(enabledAddOns.get(0), mFactory.getEnabledAddOn());
        final List<DictionaryAddOnAndBuilder> allAddOns = mFactory.getAllAddOns();
        Assert.assertEquals(1, allAddOns.size());
        Assert.assertSame(allAddOns.get(0), enabledAddOns.get(0));

        DictionaryAddOnAndBuilder builder = enabledAddOns.get(0);

        Assert.assertNotNull(builder);
        Assert.assertEquals("en", builder.getLanguage());
        Assert.assertTrue(builder.createInitialSuggestions().size() > 0);
        Assert.assertNotNull(builder.createAutoText());
    }

    @Test
    public void testGetByLocale() {
        DictionaryAddOnAndBuilder enBuilder = mFactory.getDictionaryBuilderByLocale("en");
        Assert.assertNotNull(enBuilder);
        Assert.assertEquals("en", enBuilder.getLanguage());

        DictionaryAddOnAndBuilder nullBuilder = mFactory.getDictionaryBuilderByLocale("none");
        Assert.assertNull(nullBuilder);
    }

    @Test
    public void testBuildersForKeyboardHappyPath() {
        AnyKeyboard keyboard = Mockito.mock(AnyKeyboard.class);
        Mockito.doReturn("en").when(keyboard).getDefaultDictionaryLocale();
        Mockito.doReturn("some_id").when(keyboard).getKeyboardId();

        final List<DictionaryAddOnAndBuilder> buildersForKeyboard = mFactory.getBuildersForKeyboard(keyboard);
        Assert.assertNotNull(buildersForKeyboard);
        Assert.assertEquals(1, buildersForKeyboard.size());
        Assert.assertEquals("en", buildersForKeyboard.get(0).getLanguage());

        mFactory.setBuildersForKeyboard(keyboard, Collections.<DictionaryAddOnAndBuilder>emptyList());
        final List<DictionaryAddOnAndBuilder> buildersForKeyboardAgain = mFactory.getBuildersForKeyboard(keyboard);
        Assert.assertEquals(1, buildersForKeyboardAgain.size());
        Assert.assertEquals("en", buildersForKeyboardAgain.get(0).getLanguage());
    }

    @Test
    public void testEmptyBuildersForKeyboardIfUnknownLocale() {
        AnyKeyboard keyboard = Mockito.mock(AnyKeyboard.class);
        Mockito.doReturn("none").when(keyboard).getDefaultDictionaryLocale();
        Mockito.doReturn("some_id").when(keyboard).getKeyboardId();

        final List<DictionaryAddOnAndBuilder> buildersForKeyboard = mFactory.getBuildersForKeyboard(keyboard);
        Assert.assertNotNull(buildersForKeyboard);
        Assert.assertEquals(0, buildersForKeyboard.size());
    }

    @Test
    public void testOverrideBuildersForKeyboardHappyPath() {
        AnyKeyboard keyboard = Mockito.mock(AnyKeyboard.class);
        Mockito.doReturn("none").when(keyboard).getDefaultDictionaryLocale();
        Mockito.doReturn("some_id").when(keyboard).getKeyboardId();
        List<DictionaryAddOnAndBuilder> newBuilders = Collections.singletonList(mFactory.getEnabledAddOn());

        mFactory.setBuildersForKeyboard(keyboard, newBuilders);

        final List<DictionaryAddOnAndBuilder> buildersForKeyboard = mFactory.getBuildersForKeyboard(keyboard);
        Assert.assertNotNull(buildersForKeyboard);
        Assert.assertEquals(1, buildersForKeyboard.size());
        Assert.assertEquals("en", buildersForKeyboard.get(0).getLanguage());

        mFactory.setBuildersForKeyboard(keyboard, Collections.<DictionaryAddOnAndBuilder>emptyList());
        Assert.assertEquals(0, mFactory.getBuildersForKeyboard(keyboard).size());
    }

    @Test
    public void testOverrideWhenDictionaryUnknown() {
        AnyKeyboard keyboard = Mockito.mock(AnyKeyboard.class);
        Mockito.doReturn("none").when(keyboard).getDefaultDictionaryLocale();
        Mockito.doReturn("some_id").when(keyboard).getKeyboardId();

        SharedPrefsHelper.setPrefsValue(ExternalDictionaryFactory.getDictionaryOverrideKey(keyboard), "unknown_dictionary");

        final List<DictionaryAddOnAndBuilder> buildersForKeyboard = mFactory.getBuildersForKeyboard(keyboard);
        Assert.assertNotNull(buildersForKeyboard);
        Assert.assertEquals(0, buildersForKeyboard.size());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCanNotSetEnabled() {
        mFactory.setAddOnEnabled("something", true);
    }
}