package com.anysoftkeyboard.dictionaries;

import com.anysoftkeyboard.AnySoftKeyboardTestRunner;
import com.anysoftkeyboard.keyboards.AnyKeyboard;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RuntimeEnvironment;

import java.util.List;

@RunWith(AnySoftKeyboardTestRunner.class)
public class ExternalDictionaryFactoryTest {

    private ExternalDictionaryFactory mFactory;

    @Before
    public void setUp() throws Exception {
        mFactory = new ExternalDictionaryFactory(RuntimeEnvironment.application);
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

    @Test(expected = UnsupportedOperationException.class)
    public void testCanNotSetEnabled() {
        mFactory.setAddOnEnabled("something", true);
    }
}