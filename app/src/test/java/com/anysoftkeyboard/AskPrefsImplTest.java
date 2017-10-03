package com.anysoftkeyboard;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.anysoftkeyboard.addons.AddOnsFactoryTest;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@RunWith(AnySoftKeyboardTestRunner.class)
@SuppressFBWarnings("DLS_DEAD_LOCAL_STORE")
public class AskPrefsImplTest {

    @Test
    public void testLevelSet() {
        AskPrefsImpl impl = new AskPrefsImpl(RuntimeEnvironment.application);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application);
        Assert.assertEquals(AskPrefsImpl.CONFIGURATION_LEVEL_VALUE, preferences.getInt(AskPrefsImpl.CONFIGURATION_VERSION, 0));
    }

    //START - UPGRADE TO LEVEL 11

    @Test
    public void testConvertTheme() {
        SharedPrefsHelper.setPrefsValue("settings_key_keyboard_theme_key", "28860f10-cf16-11e1-9b23-0800200c9a66");
        SharedPrefsHelper.setPrefsValue(AskPrefsImpl.CONFIGURATION_VERSION, 10);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application);
        Assert.assertFalse(preferences.contains("theme_28860f10-cf16-11e1-9b23-0800200c9a66"));
        Assert.assertNotEquals("28860f10-cf16-11e1-9b23-0800200c9a66",
                AnyApplication.getKeyboardThemeFactory(RuntimeEnvironment.application).getEnabledAddOn().getId());

        AddOnsFactoryTest.clearFactoryCache(AnyApplication.getKeyboardThemeFactory(RuntimeEnvironment.application));
        AskPrefsImpl impl = new AskPrefsImpl(RuntimeEnvironment.application);
        Assert.assertTrue(preferences.contains("theme_28860f10-cf16-11e1-9b23-0800200c9a66"));
        Assert.assertTrue(preferences.getBoolean("theme_28860f10-cf16-11e1-9b23-0800200c9a66", false));
        Assert.assertEquals("28860f10-cf16-11e1-9b23-0800200c9a66",
                AnyApplication.getKeyboardThemeFactory(RuntimeEnvironment.application).getEnabledAddOn().getId());
    }

    @Test
    public void testDoesNotConvertThemeIfNoPrefEntry() {
        SharedPrefsHelper.clearPrefsValue("settings_key_keyboard_theme_key");
        SharedPrefsHelper.setPrefsValue(AskPrefsImpl.CONFIGURATION_VERSION, 10);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application);
        Assert.assertFalse(preferences.contains("theme_28860f10-cf16-11e1-9b23-0800200c9a66"));
        Assert.assertNotEquals("28860f10-cf16-11e1-9b23-0800200c9a66",
                AnyApplication.getKeyboardThemeFactory(RuntimeEnvironment.application).getEnabledAddOn().getId());

        AddOnsFactoryTest.clearFactoryCache(AnyApplication.getKeyboardThemeFactory(RuntimeEnvironment.application));
        AskPrefsImpl impl = new AskPrefsImpl(RuntimeEnvironment.application);
        Assert.assertFalse(preferences.contains("theme_28860f10-cf16-11e1-9b23-0800200c9a66"));
        Assert.assertNotEquals("28860f10-cf16-11e1-9b23-0800200c9a66",
                AnyApplication.getKeyboardThemeFactory(RuntimeEnvironment.application).getEnabledAddOn().getId());
    }

    @Test
    public void testDoesNotConvertThemeIfConfigLevelIsHigh() {
        SharedPrefsHelper.setPrefsValue("settings_key_keyboard_theme_key", "28860f10-cf16-11e1-9b23-0800200c9a66");
        SharedPrefsHelper.setPrefsValue(AskPrefsImpl.CONFIGURATION_VERSION, 11);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application);
        Assert.assertFalse(preferences.contains("theme_28860f10-cf16-11e1-9b23-0800200c9a66"));
        Assert.assertNotEquals("28860f10-cf16-11e1-9b23-0800200c9a66",
                AnyApplication.getKeyboardThemeFactory(RuntimeEnvironment.application).getEnabledAddOn().getId());

        AddOnsFactoryTest.clearFactoryCache(AnyApplication.getKeyboardThemeFactory(RuntimeEnvironment.application));
        AskPrefsImpl impl = new AskPrefsImpl(RuntimeEnvironment.application);
        Assert.assertFalse(preferences.contains("theme_28860f10-cf16-11e1-9b23-0800200c9a66"));
        Assert.assertNotEquals("28860f10-cf16-11e1-9b23-0800200c9a66",
                AnyApplication.getKeyboardThemeFactory(RuntimeEnvironment.application).getEnabledAddOn().getId());
    }

    @Test
    public void testDoesNotConvertThemeIfFreshInstall() {
        SharedPrefsHelper.setPrefsValue("settings_key_keyboard_theme_key", "28860f10-cf16-11e1-9b23-0800200c9a66");
        SharedPrefsHelper.clearPrefsValue(AskPrefsImpl.CONFIGURATION_VERSION);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application);
        Assert.assertFalse(preferences.contains("theme_28860f10-cf16-11e1-9b23-0800200c9a66"));
        Assert.assertNotEquals("28860f10-cf16-11e1-9b23-0800200c9a66",
                AnyApplication.getKeyboardThemeFactory(RuntimeEnvironment.application).getEnabledAddOn().getId());

        AddOnsFactoryTest.clearFactoryCache(AnyApplication.getKeyboardThemeFactory(RuntimeEnvironment.application));
        AskPrefsImpl impl = new AskPrefsImpl(RuntimeEnvironment.application);
        Assert.assertFalse(preferences.contains("theme_28860f10-cf16-11e1-9b23-0800200c9a66"));
        Assert.assertNotEquals("28860f10-cf16-11e1-9b23-0800200c9a66",
                AnyApplication.getKeyboardThemeFactory(RuntimeEnvironment.application).getEnabledAddOn().getId());
    }

    @Test
    public void testConvertQuickKey() {
        SharedPrefsHelper.setPrefsValue("settings_key_ordered_active_quick_text_keys", "623e21f5-9200-4c0b-b4c7-9691129d7f1f,1057806d-4f6e-42aa-8dfd-eea57995c2ee");
        SharedPrefsHelper.setPrefsValue(AskPrefsImpl.CONFIGURATION_VERSION, 10);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application);
        Assert.assertFalse(preferences.contains("quick_text_1057806d-4f6e-42aa-8dfd-eea57995c2ee"));
        Assert.assertFalse(preferences.contains("quick_text_623e21f5-9200-4c0b-b4c7-9691129d7f1f"));
        Assert.assertNotEquals("623e21f5-9200-4c0b-b4c7-9691129d7f1f", AnyApplication.getQuickTextKeyFactory(RuntimeEnvironment.application).getEnabledIds().get(0));
        Assert.assertNotEquals("1057806d-4f6e-42aa-8dfd-eea57995c2ee", AnyApplication.getQuickTextKeyFactory(RuntimeEnvironment.application).getEnabledIds().get(1));

        AddOnsFactoryTest.clearFactoryCache(AnyApplication.getQuickTextKeyFactory(RuntimeEnvironment.application));
        AskPrefsImpl impl = new AskPrefsImpl(RuntimeEnvironment.application);
        Assert.assertTrue(preferences.contains("quick_text_1057806d-4f6e-42aa-8dfd-eea57995c2ee"));
        Assert.assertTrue(preferences.contains("quick_text_623e21f5-9200-4c0b-b4c7-9691129d7f1f"));
        Assert.assertEquals("623e21f5-9200-4c0b-b4c7-9691129d7f1f", AnyApplication.getQuickTextKeyFactory(RuntimeEnvironment.application).getAllAddOns().get(0).getId());
        Assert.assertEquals("1057806d-4f6e-42aa-8dfd-eea57995c2ee", AnyApplication.getQuickTextKeyFactory(RuntimeEnvironment.application).getAllAddOns().get(1).getId());
    }

    @Test
    public void testDoesNotConvertQuickKeyIfNoPrefEntry() {
        SharedPrefsHelper.clearPrefsValue("settings_key_ordered_active_quick_text_keys");
        SharedPrefsHelper.setPrefsValue(AskPrefsImpl.CONFIGURATION_VERSION, 10);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application);
        Assert.assertFalse(preferences.contains("quick_text_1057806d-4f6e-42aa-8dfd-eea57995c2ee"));
        Assert.assertFalse(preferences.contains("quick_text_623e21f5-9200-4c0b-b4c7-9691129d7f1f"));
        Assert.assertNotEquals("623e21f5-9200-4c0b-b4c7-9691129d7f1f", AnyApplication.getQuickTextKeyFactory(RuntimeEnvironment.application).getEnabledIds().get(0));
        Assert.assertNotEquals("1057806d-4f6e-42aa-8dfd-eea57995c2ee", AnyApplication.getQuickTextKeyFactory(RuntimeEnvironment.application).getEnabledIds().get(1));

        AddOnsFactoryTest.clearFactoryCache(AnyApplication.getQuickTextKeyFactory(RuntimeEnvironment.application));
        AskPrefsImpl impl = new AskPrefsImpl(RuntimeEnvironment.application);
        Assert.assertFalse(preferences.contains("quick_text_1057806d-4f6e-42aa-8dfd-eea57995c2ee"));
        Assert.assertFalse(preferences.contains("quick_text_623e21f5-9200-4c0b-b4c7-9691129d7f1f"));
        Assert.assertNotEquals("623e21f5-9200-4c0b-b4c7-9691129d7f1f", AnyApplication.getQuickTextKeyFactory(RuntimeEnvironment.application).getAllAddOns().get(0).getId());
        Assert.assertNotEquals("1057806d-4f6e-42aa-8dfd-eea57995c2ee", AnyApplication.getQuickTextKeyFactory(RuntimeEnvironment.application).getAllAddOns().get(1).getId());
    }

    @Test
    public void testDoesNotConvertQuickKeyIfConfigLevelIsHigh() {
        SharedPrefsHelper.setPrefsValue("settings_key_ordered_active_quick_text_keys", "623e21f5-9200-4c0b-b4c7-9691129d7f1f,1057806d-4f6e-42aa-8dfd-eea57995c2ee");
        SharedPrefsHelper.setPrefsValue(AskPrefsImpl.CONFIGURATION_VERSION, 11);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application);
        Assert.assertFalse(preferences.contains("quick_text_1057806d-4f6e-42aa-8dfd-eea57995c2ee"));
        Assert.assertFalse(preferences.contains("quick_text_623e21f5-9200-4c0b-b4c7-9691129d7f1f"));
        Assert.assertNotEquals("623e21f5-9200-4c0b-b4c7-9691129d7f1f", AnyApplication.getQuickTextKeyFactory(RuntimeEnvironment.application).getEnabledIds().get(0));
        Assert.assertNotEquals("1057806d-4f6e-42aa-8dfd-eea57995c2ee", AnyApplication.getQuickTextKeyFactory(RuntimeEnvironment.application).getEnabledIds().get(1));

        AddOnsFactoryTest.clearFactoryCache(AnyApplication.getQuickTextKeyFactory(RuntimeEnvironment.application));
        AskPrefsImpl impl = new AskPrefsImpl(RuntimeEnvironment.application);
        Assert.assertFalse(preferences.contains("quick_text_1057806d-4f6e-42aa-8dfd-eea57995c2ee"));
        Assert.assertFalse(preferences.contains("quick_text_623e21f5-9200-4c0b-b4c7-9691129d7f1f"));
        Assert.assertNotEquals("623e21f5-9200-4c0b-b4c7-9691129d7f1f", AnyApplication.getQuickTextKeyFactory(RuntimeEnvironment.application).getAllAddOns().get(0).getId());
        Assert.assertNotEquals("1057806d-4f6e-42aa-8dfd-eea57995c2ee", AnyApplication.getQuickTextKeyFactory(RuntimeEnvironment.application).getAllAddOns().get(1).getId());
    }

    @Test
    public void testDoesNotConvertQuickKeyIfFreshInstall() {
        SharedPrefsHelper.setPrefsValue("settings_key_ordered_active_quick_text_keys", "623e21f5-9200-4c0b-b4c7-9691129d7f1f,1057806d-4f6e-42aa-8dfd-eea57995c2ee");
        SharedPrefsHelper.clearPrefsValue(AskPrefsImpl.CONFIGURATION_VERSION);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application);
        Assert.assertFalse(preferences.contains("quick_text_1057806d-4f6e-42aa-8dfd-eea57995c2ee"));
        Assert.assertFalse(preferences.contains("quick_text_623e21f5-9200-4c0b-b4c7-9691129d7f1f"));
        Assert.assertNotEquals(2, AnyApplication.getQuickTextKeyFactory(RuntimeEnvironment.application).getEnabledIds().size());

        AddOnsFactoryTest.clearFactoryCache(AnyApplication.getQuickTextKeyFactory(RuntimeEnvironment.application));
        AskPrefsImpl impl = new AskPrefsImpl(RuntimeEnvironment.application);
        Assert.assertFalse(preferences.contains("quick_text_1057806d-4f6e-42aa-8dfd-eea57995c2ee"));
        Assert.assertFalse(preferences.contains("quick_text_623e21f5-9200-4c0b-b4c7-9691129d7f1f"));
        Assert.assertNotEquals("623e21f5-9200-4c0b-b4c7-9691129d7f1f", AnyApplication.getQuickTextKeyFactory(RuntimeEnvironment.application).getAllAddOns().get(0).getId());
        Assert.assertNotEquals("1057806d-4f6e-42aa-8dfd-eea57995c2ee", AnyApplication.getQuickTextKeyFactory(RuntimeEnvironment.application).getAllAddOns().get(1).getId());
    }

    @Test
    public void testConvertTopGenericRow() {
        SharedPrefsHelper.setPrefsValue("settings_key_ext_kbd_top_row_key", "1fae0220-ded6-11e0-9572-0800200c9a66");
        SharedPrefsHelper.setPrefsValue(AskPrefsImpl.CONFIGURATION_VERSION, 10);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application);
        Assert.assertFalse(preferences.contains("ext_kbd_enabled_2_1fae0220-ded6-11e0-9572-0800200c9a66"));
        Assert.assertNotEquals("1fae0220-ded6-11e0-9572-0800200c9a66",
                AnyApplication.getTopRowFactory(RuntimeEnvironment.application).getEnabledAddOn().getId());

        AddOnsFactoryTest.clearFactoryCache(AnyApplication.getTopRowFactory(RuntimeEnvironment.application));
        AskPrefsImpl impl = new AskPrefsImpl(RuntimeEnvironment.application);
        Assert.assertTrue(preferences.contains("ext_kbd_enabled_2_1fae0220-ded6-11e0-9572-0800200c9a66"));
        Assert.assertTrue(preferences.getBoolean("ext_kbd_enabled_2_1fae0220-ded6-11e0-9572-0800200c9a66", false));
        Assert.assertEquals("1fae0220-ded6-11e0-9572-0800200c9a66",
                AnyApplication.getTopRowFactory(RuntimeEnvironment.application).getEnabledAddOn().getId());
    }

    @Test
    public void testDoesNotConvertTopGenericRowIfNoPrefEntry() {
        SharedPrefsHelper.clearPrefsValue("settings_key_ext_kbd_top_row_key");
        SharedPrefsHelper.setPrefsValue(AskPrefsImpl.CONFIGURATION_VERSION, 10);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application);
        Assert.assertFalse(preferences.contains("ext_kbd_enabled_2_1fae0220-ded6-11e0-9572-0800200c9a66"));
        Assert.assertNotEquals("1fae0220-ded6-11e0-9572-0800200c9a66",
                AnyApplication.getTopRowFactory(RuntimeEnvironment.application).getEnabledAddOn().getId());

        AddOnsFactoryTest.clearFactoryCache(AnyApplication.getTopRowFactory(RuntimeEnvironment.application));
        AskPrefsImpl impl = new AskPrefsImpl(RuntimeEnvironment.application);
        Assert.assertFalse(preferences.contains("ext_kbd_enabled_2_1fae0220-ded6-11e0-9572-0800200c9a66"));
        //default value
        Assert.assertEquals(RuntimeEnvironment.application.getText(R.string.settings_default_top_row_key),
                AnyApplication.getTopRowFactory(RuntimeEnvironment.application).getEnabledAddOn().getId());
    }

    @Test
    public void testDoesNotConvertTopGenericRowIfConfigLevelIsHigh() {
        SharedPrefsHelper.setPrefsValue("settings_key_ext_kbd_top_row_key", "1fae0220-ded6-11e0-9572-0800200c9a66");
        SharedPrefsHelper.setPrefsValue(AskPrefsImpl.CONFIGURATION_VERSION, 11);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application);
        Assert.assertFalse(preferences.contains("ext_kbd_enabled_2_1fae0220-ded6-11e0-9572-0800200c9a66"));
        Assert.assertNotEquals("1fae0220-ded6-11e0-9572-0800200c9a66",
                AnyApplication.getTopRowFactory(RuntimeEnvironment.application).getEnabledAddOn().getId());

        AddOnsFactoryTest.clearFactoryCache(AnyApplication.getTopRowFactory(RuntimeEnvironment.application));
        AskPrefsImpl impl = new AskPrefsImpl(RuntimeEnvironment.application);
        Assert.assertFalse(preferences.contains("ext_kbd_enabled_2_1fae0220-ded6-11e0-9572-0800200c9a66"));
        //default value
        Assert.assertEquals(RuntimeEnvironment.application.getText(R.string.settings_default_top_row_key),
                AnyApplication.getTopRowFactory(RuntimeEnvironment.application).getEnabledAddOn().getId());
    }

    @Test
    public void testDoesNotConvertTopGenericRowIfFreshInstall() {
        SharedPrefsHelper.setPrefsValue("settings_key_ext_kbd_top_row_key", "1fae0220-ded6-11e0-9572-0800200c9a66");
        SharedPrefsHelper.clearPrefsValue(AskPrefsImpl.CONFIGURATION_VERSION);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application);
        Assert.assertFalse(preferences.contains("ext_kbd_enabled_2_1fae0220-ded6-11e0-9572-0800200c9a66"));
        Assert.assertNotEquals("1fae0220-ded6-11e0-9572-0800200c9a66",
                AnyApplication.getTopRowFactory(RuntimeEnvironment.application).getEnabledAddOn().getId());

        AddOnsFactoryTest.clearFactoryCache(AnyApplication.getTopRowFactory(RuntimeEnvironment.application));
        AskPrefsImpl impl = new AskPrefsImpl(RuntimeEnvironment.application);
        Assert.assertFalse(preferences.contains("ext_kbd_enabled_2_1fae0220-ded6-11e0-9572-0800200c9a66"));
        //default value
        Assert.assertEquals(RuntimeEnvironment.application.getText(R.string.settings_default_top_row_key),
                AnyApplication.getTopRowFactory(RuntimeEnvironment.application).getEnabledAddOn().getId());
    }

    @Test
    public void testConvertBottomGenericRow() {
        SharedPrefsHelper.setPrefsValue("settings_key_ext_kbd_bottom_row_key", "3DFFC2AD-8BC8-47F3-962A-918156AD8DD0");
        SharedPrefsHelper.setPrefsValue(AskPrefsImpl.CONFIGURATION_VERSION, 10);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application);
        Assert.assertFalse(preferences.contains("ext_kbd_enabled_1_3DFFC2AD-8BC8-47F3-962A-918156AD8DD0"));
        Assert.assertNotEquals("3DFFC2AD-8BC8-47F3-962A-918156AD8DD0",
                AnyApplication.getBottomRowFactory(RuntimeEnvironment.application).getEnabledAddOn().getId());

        AddOnsFactoryTest.clearFactoryCache(AnyApplication.getBottomRowFactory(RuntimeEnvironment.application));
        AskPrefsImpl impl = new AskPrefsImpl(RuntimeEnvironment.application);
        Assert.assertTrue(preferences.contains("ext_kbd_enabled_1_3DFFC2AD-8BC8-47F3-962A-918156AD8DD0"));
        Assert.assertTrue(preferences.getBoolean("ext_kbd_enabled_1_3DFFC2AD-8BC8-47F3-962A-918156AD8DD0", false));
        Assert.assertEquals("3DFFC2AD-8BC8-47F3-962A-918156AD8DD0",
                AnyApplication.getBottomRowFactory(RuntimeEnvironment.application).getEnabledAddOn().getId());
    }

    @Test
    public void testDoesNotConvertBottomGenericRowIfNoPrefEntry() {
        SharedPrefsHelper.clearPrefsValue("settings_key_ext_kbd_bottom_row_key");
        SharedPrefsHelper.setPrefsValue(AskPrefsImpl.CONFIGURATION_VERSION, 10);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application);
        Assert.assertFalse(preferences.contains("ext_kbd_enabled_1_3DFFC2AD-8BC8-47F3-962A-918156AD8DD0"));
        Assert.assertNotEquals("3DFFC2AD-8BC8-47F3-962A-918156AD8DD0",
                AnyApplication.getBottomRowFactory(RuntimeEnvironment.application).getEnabledAddOn().getId());

        AddOnsFactoryTest.clearFactoryCache(AnyApplication.getBottomRowFactory(RuntimeEnvironment.application));
        AskPrefsImpl impl = new AskPrefsImpl(RuntimeEnvironment.application);
        Assert.assertFalse(preferences.contains("ext_kbd_enabled_1_3DFFC2AD-8BC8-47F3-962A-918156AD8DD0"));
        //default value
        Assert.assertEquals(RuntimeEnvironment.application.getText(R.string.settings_default_ext_kbd_bottom_row_key),
                AnyApplication.getBottomRowFactory(RuntimeEnvironment.application).getEnabledAddOn().getId());
    }

    @Test
    public void testDoesNotConvertBottomGenericRowIfConfigLevelIsHigh() {
        SharedPrefsHelper.setPrefsValue("settings_key_ext_kbd_bottom_row_key", "3DFFC2AD-8BC8-47F3-962A-918156AD8DD0");
        SharedPrefsHelper.setPrefsValue(AskPrefsImpl.CONFIGURATION_VERSION, 11);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application);
        Assert.assertFalse(preferences.contains("ext_kbd_enabled_1_3DFFC2AD-8BC8-47F3-962A-918156AD8DD0"));
        Assert.assertNotEquals("3DFFC2AD-8BC8-47F3-962A-918156AD8DD0",
                AnyApplication.getBottomRowFactory(RuntimeEnvironment.application).getEnabledAddOn().getId());

        AddOnsFactoryTest.clearFactoryCache(AnyApplication.getBottomRowFactory(RuntimeEnvironment.application));
        AskPrefsImpl impl = new AskPrefsImpl(RuntimeEnvironment.application);
        Assert.assertFalse(preferences.contains("ext_kbd_enabled_1_3DFFC2AD-8BC8-47F3-962A-918156AD8DD0"));
        //default value
        Assert.assertEquals(RuntimeEnvironment.application.getText(R.string.settings_default_ext_kbd_bottom_row_key),
                AnyApplication.getBottomRowFactory(RuntimeEnvironment.application).getEnabledAddOn().getId());
    }

    @Test
    public void testDoesNotConvertBottomGenericRowIfFreshInstall() {
        SharedPrefsHelper.setPrefsValue("settings_key_ext_kbd_bottom_row_key", "3DFFC2AD-8BC8-47F3-962A-918156AD8DD0");
        SharedPrefsHelper.clearPrefsValue(AskPrefsImpl.CONFIGURATION_VERSION);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application);
        Assert.assertFalse(preferences.contains("ext_kbd_enabled_1_3DFFC2AD-8BC8-47F3-962A-918156AD8DD0"));
        Assert.assertNotEquals("3DFFC2AD-8BC8-47F3-962A-918156AD8DD0",
                AnyApplication.getBottomRowFactory(RuntimeEnvironment.application).getEnabledAddOn().getId());

        AddOnsFactoryTest.clearFactoryCache(AnyApplication.getBottomRowFactory(RuntimeEnvironment.application));
        AskPrefsImpl impl = new AskPrefsImpl(RuntimeEnvironment.application);
        Assert.assertFalse(preferences.contains("ext_kbd_enabled_1_3DFFC2AD-8BC8-47F3-962A-918156AD8DD0"));
        //default value
        Assert.assertEquals(RuntimeEnvironment.application.getText(R.string.settings_default_ext_kbd_bottom_row_key),
                AnyApplication.getBottomRowFactory(RuntimeEnvironment.application).getEnabledAddOn().getId());
    }

    //END - UPGRADE TO LEVEL 11
}