package com.anysoftkeyboard.prefs;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.test.SharedPrefsHelper;
import com.f2prateek.rx.preferences2.Preference;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.reactivex.disposables.CompositeDisposable;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
@SuppressFBWarnings("DLS_DEAD_LOCAL_STORE")
public class RxSharedPrefsTest {

    private CompositeDisposable mCompositeDisposable;

    @Before
    public void setup() {
        mCompositeDisposable = new CompositeDisposable();
    }

    @After
    public void tearDown() {
        mCompositeDisposable.dispose();
    }

    @Test
    public void testLevelSet() {
        new RxSharedPrefs(
                getApplicationContext(), getDefaultSharedPreferences(getApplicationContext()));

        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Assert.assertEquals(
                RxSharedPrefs.CONFIGURATION_LEVEL_VALUE,
                preferences.getInt(RxSharedPrefs.CONFIGURATION_VERSION, 0));
    }

    @Test
    public void testBooleanHappyPath() {
        RxSharedPrefs impl =
                new RxSharedPrefs(
                        getApplicationContext(),
                        getDefaultSharedPreferences(getApplicationContext()));

        final Preference<Boolean> preference =
                impl.getBoolean(R.string.pref_test_key, R.bool.pref_test_value);
        Assert.assertTrue(preference.get());

        final AtomicReference<Boolean> observedValue = new AtomicReference<>(null);
        mCompositeDisposable.add(preference.asObservable().subscribe(observedValue::set));

        Assert.assertTrue(observedValue.get());

        SharedPrefsHelper.setPrefsValue(R.string.pref_test_key, false);

        Assert.assertFalse(preference.get());
        Assert.assertFalse(observedValue.get());
    }

    @Test
    public void testStringHappyPath() {
        RxSharedPrefs impl =
                new RxSharedPrefs(
                        getApplicationContext(),
                        getDefaultSharedPreferences(getApplicationContext()));

        final Preference<String> preference =
                impl.getString(R.string.pref_test_key, R.string.pref_test_value);
        Assert.assertEquals("value", preference.get());

        final AtomicReference<String> observedValue = new AtomicReference<>(null);
        mCompositeDisposable.add(preference.asObservable().subscribe(observedValue::set));

        Assert.assertEquals("value", preference.get());

        SharedPrefsHelper.setPrefsValue(R.string.pref_test_key, "iw");

        Assert.assertEquals("iw", preference.get());
        Assert.assertEquals("iw", observedValue.get());
    }

    @Test
    public void testDoesNotUpdateVibrationIfNewInstall() {
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Assert.assertFalse(preferences.contains(RxSharedPrefs.CONFIGURATION_VERSION));
        Assert.assertFalse(preferences.contains("vibrate_on_key_press_duration"));
        Assert.assertFalse(preferences.contains("settings_key_vibrate_on_key_press_duration_int"));
    }

    @Test
    public void testDoesNotUpdateVibrationIfNotSetBefore() {
        SharedPrefsHelper.setPrefsValue(RxSharedPrefs.CONFIGURATION_VERSION, 11);
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Assert.assertFalse(preferences.contains("vibrate_on_key_press_duration"));
        Assert.assertFalse(preferences.contains("settings_key_vibrate_on_key_press_duration_int"));

        new RxSharedPrefs(
                getApplicationContext(), getDefaultSharedPreferences(getApplicationContext()));

        Assert.assertFalse(preferences.contains("settings_key_vibrate_on_key_press_duration_int"));
        Assert.assertEquals(12, preferences.getInt(RxSharedPrefs.CONFIGURATION_VERSION, 0));
    }

    @Test
    public void testUpdatesVibrationIfSetBefore() {
        SharedPrefsHelper.setPrefsValue(RxSharedPrefs.CONFIGURATION_VERSION, 11);
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Assert.assertFalse(preferences.contains("vibrate_on_key_press_duration"));
        Assert.assertFalse(preferences.contains("settings_key_vibrate_on_key_press_duration_int"));

        SharedPrefsHelper.setPrefsValue("vibrate_on_key_press_duration", "17");

        new RxSharedPrefs(
                getApplicationContext(), getDefaultSharedPreferences(getApplicationContext()));

        Assert.assertTrue(preferences.contains("settings_key_vibrate_on_key_press_duration_int"));
        Assert.assertFalse(preferences.contains("vibrate_on_key_press_duration"));
        Assert.assertEquals(
                17, preferences.getInt("settings_key_vibrate_on_key_press_duration_int", 0));
    }

    @Test
    public void testDoesNotCrashIfPreviousValueWasNotInteger() {
        SharedPrefsHelper.setPrefsValue(RxSharedPrefs.CONFIGURATION_VERSION, 11);
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPrefsHelper.setPrefsValue("vibrate_on_key_press_duration", "crash");
        new RxSharedPrefs(
                getApplicationContext(), getDefaultSharedPreferences(getApplicationContext()));

        Assert.assertEquals(
                0, preferences.getInt("settings_key_vibrate_on_key_press_duration_int", 0));
    }

    @Test
    public void testDoesNotCrashIfPreviousValueWasNull() {
        SharedPrefsHelper.setPrefsValue(RxSharedPrefs.CONFIGURATION_VERSION, 11);
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPrefsHelper.setPrefsValue("vibrate_on_key_press_duration", null);
        new RxSharedPrefs(
                getApplicationContext(), getDefaultSharedPreferences(getApplicationContext()));

        Assert.assertEquals(
                0, preferences.getInt("settings_key_vibrate_on_key_press_duration_int", 0));
    }

    @Test
    public void testSetupFallbackDictionaryToFalseIfWasNotSetBefore() {
        SharedPrefsHelper.setPrefsValue(RxSharedPrefs.CONFIGURATION_VERSION, 11);

        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Assert.assertFalse(
                preferences.contains("settings_key_always_use_fallback_user_dictionary"));

        new RxSharedPrefs(
                getApplicationContext(), getDefaultSharedPreferences(getApplicationContext()));

        Assert.assertTrue(preferences.contains("settings_key_always_use_fallback_user_dictionary"));
        Assert.assertFalse(
                preferences.getBoolean("settings_key_always_use_fallback_user_dictionary", true));
    }

    @Test
    public void testDoesNotSetupFallbackDictionaryIfConfigurationVersionIsEmpty() {
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Assert.assertFalse(
                preferences.contains("settings_key_always_use_fallback_user_dictionary"));

        new RxSharedPrefs(
                getApplicationContext(), getDefaultSharedPreferences(getApplicationContext()));

        Assert.assertFalse(
                preferences.contains("settings_key_always_use_fallback_user_dictionary"));
    }

    @Test
    public void testDoesNotSetupFallbackDictionaryIfConfigurationVersion12() {
        SharedPrefsHelper.setPrefsValue(RxSharedPrefs.CONFIGURATION_VERSION, 12);

        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Assert.assertFalse(
                preferences.contains("settings_key_always_use_fallback_user_dictionary"));

        new RxSharedPrefs(
                getApplicationContext(), getDefaultSharedPreferences(getApplicationContext()));

        Assert.assertFalse(
                preferences.contains("settings_key_always_use_fallback_user_dictionary"));
    }

    @Test
    public void testDoesNotSetupFallbackDictionaryToFalseIfWasSetBeforeToFalse() {
        SharedPrefsHelper.setPrefsValue("settings_key_always_use_fallback_user_dictionary", false);
        SharedPrefsHelper.setPrefsValue(RxSharedPrefs.CONFIGURATION_VERSION, 11);

        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Assert.assertTrue(preferences.contains("settings_key_always_use_fallback_user_dictionary"));

        new RxSharedPrefs(
                getApplicationContext(), getDefaultSharedPreferences(getApplicationContext()));

        Assert.assertTrue(preferences.contains("settings_key_always_use_fallback_user_dictionary"));
        Assert.assertFalse(
                preferences.getBoolean("settings_key_always_use_fallback_user_dictionary", true));
    }

    @Test
    public void testDoesNotSetupFallbackDictionaryToFalseIfWasSetBeforeToTrue() {
        SharedPrefsHelper.setPrefsValue("settings_key_always_use_fallback_user_dictionary", true);
        SharedPrefsHelper.setPrefsValue(RxSharedPrefs.CONFIGURATION_VERSION, 11);

        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Assert.assertTrue(preferences.contains("settings_key_always_use_fallback_user_dictionary"));

        new RxSharedPrefs(
                getApplicationContext(), getDefaultSharedPreferences(getApplicationContext()));

        Assert.assertTrue(preferences.contains("settings_key_always_use_fallback_user_dictionary"));
        Assert.assertTrue(
                preferences.getBoolean("settings_key_always_use_fallback_user_dictionary", false));
    }

    @Test
    public void testConvertTheme() {
        SharedPrefsHelper.setPrefsValue(
                "settings_key_keyboard_theme_key", "28860f10-cf16-11e1-9b23-0800200c9a66");
        SharedPrefsHelper.setPrefsValue(RxSharedPrefs.CONFIGURATION_VERSION, 10);

        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Assert.assertFalse(preferences.contains("theme_28860f10-cf16-11e1-9b23-0800200c9a66"));

        new RxSharedPrefs(
                getApplicationContext(), getDefaultSharedPreferences(getApplicationContext()));

        Assert.assertTrue(preferences.contains("theme_28860f10-cf16-11e1-9b23-0800200c9a66"));
        Assert.assertTrue(
                preferences.getBoolean("theme_28860f10-cf16-11e1-9b23-0800200c9a66", false));
    }

    @Test
    public void testDoesNotConvertThemeIfNoPrefEntry() {
        SharedPrefsHelper.clearPrefsValue("settings_key_keyboard_theme_key");
        SharedPrefsHelper.setPrefsValue(RxSharedPrefs.CONFIGURATION_VERSION, 10);

        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Assert.assertFalse(preferences.contains("theme_28860f10-cf16-11e1-9b23-0800200c9a66"));

        new RxSharedPrefs(
                getApplicationContext(), getDefaultSharedPreferences(getApplicationContext()));

        Assert.assertFalse(preferences.contains("theme_28860f10-cf16-11e1-9b23-0800200c9a66"));
    }

    @Test
    public void testDoesNotConvertThemeIfConfigLevelIsHigh() {
        SharedPrefsHelper.setPrefsValue(
                "settings_key_keyboard_theme_key", "28860f10-cf16-11e1-9b23-0800200c9a66");
        SharedPrefsHelper.setPrefsValue(RxSharedPrefs.CONFIGURATION_VERSION, 11);

        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Assert.assertFalse(preferences.contains("theme_28860f10-cf16-11e1-9b23-0800200c9a66"));

        new RxSharedPrefs(
                getApplicationContext(), getDefaultSharedPreferences(getApplicationContext()));
        Assert.assertFalse(preferences.contains("theme_28860f10-cf16-11e1-9b23-0800200c9a66"));
    }

    @Test
    public void testDoesNotConvertThemeIfFreshInstall() {
        SharedPrefsHelper.setPrefsValue(
                "settings_key_keyboard_theme_key", "28860f10-cf16-11e1-9b23-0800200c9a66");
        SharedPrefsHelper.clearPrefsValue(RxSharedPrefs.CONFIGURATION_VERSION);

        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Assert.assertFalse(preferences.contains("theme_28860f10-cf16-11e1-9b23-0800200c9a66"));

        new RxSharedPrefs(
                getApplicationContext(), getDefaultSharedPreferences(getApplicationContext()));
        Assert.assertFalse(preferences.contains("theme_28860f10-cf16-11e1-9b23-0800200c9a66"));
    }

    @Test
    public void testConvertQuickKey() {
        SharedPrefsHelper.setPrefsValue(
                "settings_key_ordered_active_quick_text_keys",
                "623e21f5-9200-4c0b-b4c7-9691129d7f1f,1057806d-4f6e-42aa-8dfd-eea57995c2ee");
        SharedPrefsHelper.setPrefsValue(RxSharedPrefs.CONFIGURATION_VERSION, 10);

        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Assert.assertFalse(preferences.contains("quick_text_1057806d-4f6e-42aa-8dfd-eea57995c2ee"));
        Assert.assertFalse(preferences.contains("quick_text_623e21f5-9200-4c0b-b4c7-9691129d7f1f"));

        new RxSharedPrefs(
                getApplicationContext(), getDefaultSharedPreferences(getApplicationContext()));

        Assert.assertTrue(preferences.contains("quick_text_1057806d-4f6e-42aa-8dfd-eea57995c2ee"));
        Assert.assertTrue(preferences.contains("quick_text_623e21f5-9200-4c0b-b4c7-9691129d7f1f"));
        Assert.assertEquals(
                "623e21f5-9200-4c0b-b4c7-9691129d7f1f,1057806d-4f6e-42aa-8dfd-eea57995c2ee",
                preferences.getString("quick_text_AddOnsFactory_order_key", ""));
    }

    @Test
    public void testDoesNotConvertQuickKeyIfNoPrefEntry() {
        SharedPrefsHelper.clearPrefsValue("settings_key_ordered_active_quick_text_keys");
        SharedPrefsHelper.setPrefsValue(RxSharedPrefs.CONFIGURATION_VERSION, 10);

        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Assert.assertFalse(preferences.contains("quick_text_1057806d-4f6e-42aa-8dfd-eea57995c2ee"));
        Assert.assertFalse(preferences.contains("quick_text_623e21f5-9200-4c0b-b4c7-9691129d7f1f"));

        new RxSharedPrefs(
                getApplicationContext(), getDefaultSharedPreferences(getApplicationContext()));
        Assert.assertFalse(preferences.contains("quick_text_1057806d-4f6e-42aa-8dfd-eea57995c2ee"));
        Assert.assertFalse(preferences.contains("quick_text_623e21f5-9200-4c0b-b4c7-9691129d7f1f"));
        Assert.assertFalse(preferences.contains("quick_text_AddOnsFactory_order_key"));
    }

    @Test
    public void testDoesNotConvertQuickKeyIfConfigLevelIsHigh() {
        SharedPrefsHelper.setPrefsValue(
                "settings_key_ordered_active_quick_text_keys",
                "623e21f5-9200-4c0b-b4c7-9691129d7f1f,1057806d-4f6e-42aa-8dfd-eea57995c2ee");
        SharedPrefsHelper.setPrefsValue(RxSharedPrefs.CONFIGURATION_VERSION, 11);

        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Assert.assertFalse(preferences.contains("quick_text_1057806d-4f6e-42aa-8dfd-eea57995c2ee"));
        Assert.assertFalse(preferences.contains("quick_text_623e21f5-9200-4c0b-b4c7-9691129d7f1f"));

        new RxSharedPrefs(
                getApplicationContext(), getDefaultSharedPreferences(getApplicationContext()));
        Assert.assertFalse(preferences.contains("quick_text_1057806d-4f6e-42aa-8dfd-eea57995c2ee"));
        Assert.assertFalse(preferences.contains("quick_text_623e21f5-9200-4c0b-b4c7-9691129d7f1f"));
        Assert.assertFalse(preferences.contains("quick_text_AddOnsFactory_order_key"));
    }

    @Test
    public void testDoesNotConvertQuickKeyIfFreshInstall() {
        SharedPrefsHelper.setPrefsValue(
                "settings_key_ordered_active_quick_text_keys",
                "623e21f5-9200-4c0b-b4c7-9691129d7f1f,1057806d-4f6e-42aa-8dfd-eea57995c2ee");
        SharedPrefsHelper.clearPrefsValue(RxSharedPrefs.CONFIGURATION_VERSION);

        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Assert.assertFalse(preferences.contains("quick_text_1057806d-4f6e-42aa-8dfd-eea57995c2ee"));
        Assert.assertFalse(preferences.contains("quick_text_623e21f5-9200-4c0b-b4c7-9691129d7f1f"));

        new RxSharedPrefs(
                getApplicationContext(), getDefaultSharedPreferences(getApplicationContext()));
        Assert.assertFalse(preferences.contains("quick_text_1057806d-4f6e-42aa-8dfd-eea57995c2ee"));
        Assert.assertFalse(preferences.contains("quick_text_623e21f5-9200-4c0b-b4c7-9691129d7f1f"));
        Assert.assertFalse(preferences.contains("quick_text_AddOnsFactory_order_key"));
    }

    @Test
    public void testConvertTopGenericRow() {
        SharedPrefsHelper.setPrefsValue(
                "settings_key_ext_kbd_top_row_key", "1fae0220-ded6-11e0-9572-0800200c9a66");
        SharedPrefsHelper.setPrefsValue(RxSharedPrefs.CONFIGURATION_VERSION, 10);

        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Assert.assertFalse(
                preferences.contains("ext_kbd_enabled_2_1fae0220-ded6-11e0-9572-0800200c9a66"));

        new RxSharedPrefs(
                getApplicationContext(), getDefaultSharedPreferences(getApplicationContext()));
        Assert.assertTrue(
                preferences.contains("ext_kbd_enabled_2_1fae0220-ded6-11e0-9572-0800200c9a66"));
        Assert.assertTrue(
                preferences.getBoolean(
                        "ext_kbd_enabled_2_1fae0220-ded6-11e0-9572-0800200c9a66", false));
    }

    @Test
    public void testDoesNotConvertTopGenericRowIfNoPrefEntry() {
        SharedPrefsHelper.clearPrefsValue("settings_key_ext_kbd_top_row_key");
        SharedPrefsHelper.setPrefsValue(RxSharedPrefs.CONFIGURATION_VERSION, 10);

        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Assert.assertFalse(
                preferences.contains("ext_kbd_enabled_2_1fae0220-ded6-11e0-9572-0800200c9a66"));

        new RxSharedPrefs(
                getApplicationContext(), getDefaultSharedPreferences(getApplicationContext()));
        Assert.assertFalse(
                preferences.contains("ext_kbd_enabled_2_1fae0220-ded6-11e0-9572-0800200c9a66"));
    }

    @Test
    public void testDoesNotConvertTopGenericRowIfConfigLevelIsHigh() {
        SharedPrefsHelper.setPrefsValue(
                "settings_key_ext_kbd_top_row_key", "1fae0220-ded6-11e0-9572-0800200c9a66");
        SharedPrefsHelper.setPrefsValue(RxSharedPrefs.CONFIGURATION_VERSION, 11);

        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Assert.assertFalse(
                preferences.contains("ext_kbd_enabled_2_1fae0220-ded6-11e0-9572-0800200c9a66"));

        new RxSharedPrefs(
                getApplicationContext(), getDefaultSharedPreferences(getApplicationContext()));
        Assert.assertFalse(
                preferences.contains("ext_kbd_enabled_2_1fae0220-ded6-11e0-9572-0800200c9a66"));
    }

    @Test
    public void testDoesNotConvertTopGenericRowIfFreshInstall() {
        SharedPrefsHelper.setPrefsValue(
                "settings_key_ext_kbd_top_row_key", "1fae0220-ded6-11e0-9572-0800200c9a66");
        SharedPrefsHelper.clearPrefsValue(RxSharedPrefs.CONFIGURATION_VERSION);

        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Assert.assertFalse(
                preferences.contains("ext_kbd_enabled_2_1fae0220-ded6-11e0-9572-0800200c9a66"));

        new RxSharedPrefs(
                getApplicationContext(), getDefaultSharedPreferences(getApplicationContext()));
        Assert.assertFalse(
                preferences.contains("ext_kbd_enabled_2_1fae0220-ded6-11e0-9572-0800200c9a66"));
    }

    @Test
    public void testConvertBottomGenericRow() {
        SharedPrefsHelper.setPrefsValue(
                "settings_key_ext_kbd_bottom_row_key", "3DFFC2AD-8BC8-47F3-962A-918156AD8DD0");
        SharedPrefsHelper.setPrefsValue(RxSharedPrefs.CONFIGURATION_VERSION, 10);

        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Assert.assertFalse(
                preferences.contains("ext_kbd_enabled_1_3DFFC2AD-8BC8-47F3-962A-918156AD8DD0"));

        new RxSharedPrefs(
                getApplicationContext(), getDefaultSharedPreferences(getApplicationContext()));
        Assert.assertTrue(
                preferences.contains("ext_kbd_enabled_1_3DFFC2AD-8BC8-47F3-962A-918156AD8DD0"));
        Assert.assertTrue(
                preferences.getBoolean(
                        "ext_kbd_enabled_1_3DFFC2AD-8BC8-47F3-962A-918156AD8DD0", false));
    }

    @Test
    public void testDoesNotConvertBottomGenericRowIfNoPrefEntry() {
        SharedPrefsHelper.clearPrefsValue("settings_key_ext_kbd_bottom_row_key");
        SharedPrefsHelper.setPrefsValue(RxSharedPrefs.CONFIGURATION_VERSION, 10);

        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Assert.assertFalse(
                preferences.contains("ext_kbd_enabled_1_3DFFC2AD-8BC8-47F3-962A-918156AD8DD0"));

        new RxSharedPrefs(
                getApplicationContext(), getDefaultSharedPreferences(getApplicationContext()));
        Assert.assertFalse(
                preferences.contains("ext_kbd_enabled_1_3DFFC2AD-8BC8-47F3-962A-918156AD8DD0"));
    }

    @Test
    public void testDoesNotConvertBottomGenericRowIfConfigLevelIsHigh() {
        SharedPrefsHelper.setPrefsValue(
                "settings_key_ext_kbd_bottom_row_key", "3DFFC2AD-8BC8-47F3-962A-918156AD8DD0");
        SharedPrefsHelper.setPrefsValue(RxSharedPrefs.CONFIGURATION_VERSION, 11);

        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Assert.assertFalse(
                preferences.contains("ext_kbd_enabled_1_3DFFC2AD-8BC8-47F3-962A-918156AD8DD0"));

        new RxSharedPrefs(
                getApplicationContext(), getDefaultSharedPreferences(getApplicationContext()));
        Assert.assertFalse(
                preferences.contains("ext_kbd_enabled_1_3DFFC2AD-8BC8-47F3-962A-918156AD8DD0"));
    }

    @Test
    public void testDoesNotConvertBottomGenericRowIfFreshInstall() {
        SharedPrefsHelper.setPrefsValue(
                "settings_key_ext_kbd_bottom_row_key", "3DFFC2AD-8BC8-47F3-962A-918156AD8DD0");
        SharedPrefsHelper.clearPrefsValue(RxSharedPrefs.CONFIGURATION_VERSION);

        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Assert.assertFalse(
                preferences.contains("ext_kbd_enabled_1_3DFFC2AD-8BC8-47F3-962A-918156AD8DD0"));

        new RxSharedPrefs(
                getApplicationContext(), getDefaultSharedPreferences(getApplicationContext()));
        Assert.assertFalse(
                preferences.contains("ext_kbd_enabled_1_3DFFC2AD-8BC8-47F3-962A-918156AD8DD0"));
    }
}
