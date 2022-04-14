package com.anysoftkeyboard.prefs;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.anysoftkeyboard.test.SharedPrefsHelper.getSharedPreferences;

import android.Manifest;
import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.UserManager;
import android.preference.PreferenceManager;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.test.SharedPrefsHelper;
import com.f2prateek.rx.preferences2.Preference;
import com.google.common.io.Files;
import io.reactivex.disposables.CompositeDisposable;
import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowUserManager;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class RxSharedPrefsTest {

    private CompositeDisposable mCompositeDisposable;
    private File mInputApplyFile;

    @Before
    public void setup() {
        mCompositeDisposable = new CompositeDisposable();
    }

    @After
    public void tearDown() {
        mCompositeDisposable.dispose();
    }

    private void testRestoreFunction(File inputFile) {
        Assert.assertNotNull(inputFile);
        Assert.assertTrue(inputFile.isFile());
        mInputApplyFile = inputFile;
    }

    @Test
    public void testLevelSet() {
        new RxSharedPrefs(getApplicationContext(), this::testRestoreFunction);

        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Assert.assertEquals(
                RxSharedPrefs.CONFIGURATION_LEVEL_VALUE,
                preferences.getInt(RxSharedPrefs.CONFIGURATION_VERSION, 0));
    }

    @Test
    public void testBooleanHappyPath() {
        RxSharedPrefs impl = new RxSharedPrefs(getApplicationContext(), this::testRestoreFunction);

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
        RxSharedPrefs impl = new RxSharedPrefs(getApplicationContext(), this::testRestoreFunction);

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

        new RxSharedPrefs(getApplicationContext(), this::testRestoreFunction);

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

        new RxSharedPrefs(getApplicationContext(), this::testRestoreFunction);

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
        new RxSharedPrefs(getApplicationContext(), this::testRestoreFunction);

        Assert.assertEquals(
                0, preferences.getInt("settings_key_vibrate_on_key_press_duration_int", 0));
    }

    @Test
    public void testDoesNotCrashIfPreviousValueWasNull() {
        SharedPrefsHelper.setPrefsValue(RxSharedPrefs.CONFIGURATION_VERSION, 11);
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPrefsHelper.setPrefsValue("vibrate_on_key_press_duration", null);
        new RxSharedPrefs(getApplicationContext(), this::testRestoreFunction);

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

        new RxSharedPrefs(getApplicationContext(), this::testRestoreFunction);

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

        new RxSharedPrefs(getApplicationContext(), this::testRestoreFunction);

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

        new RxSharedPrefs(getApplicationContext(), this::testRestoreFunction);

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

        new RxSharedPrefs(getApplicationContext(), this::testRestoreFunction);

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

        new RxSharedPrefs(getApplicationContext(), this::testRestoreFunction);

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

        new RxSharedPrefs(getApplicationContext(), this::testRestoreFunction);

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

        new RxSharedPrefs(getApplicationContext(), this::testRestoreFunction);

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

        new RxSharedPrefs(getApplicationContext(), this::testRestoreFunction);
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

        new RxSharedPrefs(getApplicationContext(), this::testRestoreFunction);
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

        new RxSharedPrefs(getApplicationContext(), this::testRestoreFunction);

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

        new RxSharedPrefs(getApplicationContext(), this::testRestoreFunction);
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

        new RxSharedPrefs(getApplicationContext(), this::testRestoreFunction);
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

        new RxSharedPrefs(getApplicationContext(), this::testRestoreFunction);
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

        new RxSharedPrefs(getApplicationContext(), this::testRestoreFunction);
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

        new RxSharedPrefs(getApplicationContext(), this::testRestoreFunction);
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

        new RxSharedPrefs(getApplicationContext(), this::testRestoreFunction);
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

        new RxSharedPrefs(getApplicationContext(), this::testRestoreFunction);
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

        new RxSharedPrefs(getApplicationContext(), this::testRestoreFunction);
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

        new RxSharedPrefs(getApplicationContext(), this::testRestoreFunction);
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

        new RxSharedPrefs(getApplicationContext(), this::testRestoreFunction);
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

        new RxSharedPrefs(getApplicationContext(), this::testRestoreFunction);
        Assert.assertFalse(
                preferences.contains("ext_kbd_enabled_1_3DFFC2AD-8BC8-47F3-962A-918156AD8DD0"));
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.M /*first version to require runtime permissions*/)
    public void testDoesNotAutoApplyPrefFileIfNoPermission() throws Exception {
        final Application context = getApplicationContext();
        final File autoApplyFile =
                new File(
                        context.getExternalFilesDir(null), RxSharedPrefs.AUTO_APPLY_PREFS_FILENAME);
        try (BufferedWriter writer = Files.newWriter(autoApplyFile, StandardCharsets.UTF_8)) {
            writer.write("<Prefs/>");
        }

        Shadows.shadowOf(context).denyPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        new RxSharedPrefs(context, this::testRestoreFunction);

        Assert.assertTrue(autoApplyFile.exists());
        // this means the restore function was not called
        Assert.assertNull(mInputApplyFile);
        final String[] listedFiles = autoApplyFile.getParentFile().list();
        Assert.assertNotNull(listedFiles);
        Assert.assertFalse(
                Arrays.stream(listedFiles)
                        .anyMatch(
                                name ->
                                        name.startsWith(
                                                RxSharedPrefs
                                                        .AUTO_APPLIED_PREFS_FILENAME_TEMPLATE_PREFIX)));
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.M /*first version to require runtime permissions*/)
    public void testDoesNotAutoApplyPrefFileIfNoApplyFile() throws Exception {
        final Application context = getApplicationContext();
        final File autoApplyFile =
                new File(
                        context.getExternalFilesDir(null), RxSharedPrefs.AUTO_APPLY_PREFS_FILENAME);
        Shadows.shadowOf(context).grantPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        Assert.assertFalse(autoApplyFile.exists());
        new RxSharedPrefs(context, this::testRestoreFunction);
        Assert.assertFalse(autoApplyFile.exists());
        // this means the restore function was not called
        Assert.assertNull(mInputApplyFile);
        final String[] listedFiles = autoApplyFile.getParentFile().list();
        Assert.assertNotNull(listedFiles);
        Assert.assertFalse(
                Arrays.stream(listedFiles)
                        .anyMatch(
                                name ->
                                        name.startsWith(
                                                RxSharedPrefs
                                                        .AUTO_APPLIED_PREFS_FILENAME_TEMPLATE_PREFIX)));
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.M /*first version to require runtime permissions*/)
    public void testDoesNotCrashOnException() throws Exception {
        final Application context = getApplicationContext();
        final File autoApplyFile =
                new File(
                        context.getExternalFilesDir(null), RxSharedPrefs.AUTO_APPLY_PREFS_FILENAME);
        try (BufferedWriter writer = Files.newWriter(autoApplyFile, StandardCharsets.UTF_8)) {
            writer.write("<Prefs/>");
        }
        Shadows.shadowOf(context).grantPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        final AtomicBoolean called = new AtomicBoolean(false);
        new RxSharedPrefs(
                context,
                file -> {
                    called.set(true);
                    throw new Exception();
                });
        Assert.assertTrue(autoApplyFile.exists());
        Assert.assertTrue(called.get());
        final String[] listedFiles = autoApplyFile.getParentFile().list();
        Assert.assertNotNull(listedFiles);
        Assert.assertFalse(
                Arrays.stream(listedFiles)
                        .anyMatch(
                                name ->
                                        name.startsWith(
                                                RxSharedPrefs
                                                        .AUTO_APPLIED_PREFS_FILENAME_TEMPLATE_PREFIX)));
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.M /*first version to require runtime permissions*/)
    public void testHappyPathAppliesIfWritePermissionAndHasFile() throws Exception {
        final Application context = getApplicationContext();
        final File autoApplyFile =
                new File(
                        context.getExternalFilesDir(null), RxSharedPrefs.AUTO_APPLY_PREFS_FILENAME);
        Shadows.shadowOf(context).grantPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        try (BufferedWriter writer = Files.newWriter(autoApplyFile, StandardCharsets.UTF_8)) {
            writer.write("<Prefs/>");
        }

        new RxSharedPrefs(context, this::testRestoreFunction);
        Assert.assertFalse(autoApplyFile.exists());
        // this means the restore function was called
        Assert.assertEquals(mInputApplyFile, autoApplyFile);
        final String[] listedFiles = autoApplyFile.getParentFile().list();
        Assert.assertNotNull(listedFiles);
        Assert.assertTrue(
                Arrays.stream(listedFiles)
                        .anyMatch(
                                name ->
                                        name.startsWith(
                                                RxSharedPrefs
                                                        .AUTO_APPLIED_PREFS_FILENAME_TEMPLATE_PREFIX)));
    }

    @Test
    public void testUpdatesStatisticsAndApplyFileOnlyWhenUserUnlocks() throws Exception {
        Application application = getApplicationContext();
        final File autoApplyFile =
                new File(
                        application.getExternalFilesDir(null),
                        RxSharedPrefs.AUTO_APPLY_PREFS_FILENAME);
        Shadows.shadowOf(application).grantPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        try (BufferedWriter writer = Files.newWriter(autoApplyFile, StandardCharsets.UTF_8)) {
            writer.write("<Prefs/>");
        }
        ShadowUserManager shadowUserManager =
                Shadows.shadowOf(application.getSystemService(UserManager.class));
        shadowUserManager.setUserUnlocked(false);

        Assert.assertNull(mInputApplyFile);
        Assert.assertNotEquals(
                RxSharedPrefs.CONFIGURATION_LEVEL_VALUE,
                getSharedPreferences().getInt(RxSharedPrefs.CONFIGURATION_VERSION, -12));

        new RxSharedPrefs(application, this::testRestoreFunction);

        Assert.assertNull(mInputApplyFile);
        Assert.assertNotEquals(
                RxSharedPrefs.CONFIGURATION_LEVEL_VALUE,
                getSharedPreferences().getInt(RxSharedPrefs.CONFIGURATION_VERSION, -12));

        shadowUserManager.setUserUnlocked(true);
        Shadows.shadowOf(application).getRegisteredReceivers().stream()
                .filter(w -> w.intentFilter.hasAction(Intent.ACTION_USER_UNLOCKED))
                .forEach(
                        w ->
                                w.broadcastReceiver.onReceive(
                                        application, new Intent(Intent.ACTION_USER_UNLOCKED)));

        Assert.assertNotNull(mInputApplyFile);
        Assert.assertEquals(
                RxSharedPrefs.CONFIGURATION_LEVEL_VALUE,
                getSharedPreferences().getInt(RxSharedPrefs.CONFIGURATION_VERSION, -12));
    }

    @Test
    public void testUpdatesStatisticsAndApplyFileIfUserAlreadyUnlocked() throws Exception {
        Application application = getApplicationContext();
        final File autoApplyFile =
                new File(
                        application.getExternalFilesDir(null),
                        RxSharedPrefs.AUTO_APPLY_PREFS_FILENAME);
        Shadows.shadowOf(application).grantPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        try (BufferedWriter writer = Files.newWriter(autoApplyFile, StandardCharsets.UTF_8)) {
            writer.write("<Prefs/>");
        }
        ShadowUserManager shadowUserManager =
                Shadows.shadowOf(application.getSystemService(UserManager.class));
        shadowUserManager.setUserUnlocked(true);

        Assert.assertNull(mInputApplyFile);
        Assert.assertNotEquals(
                RxSharedPrefs.CONFIGURATION_LEVEL_VALUE,
                getSharedPreferences().getInt(RxSharedPrefs.CONFIGURATION_VERSION, -12));
        new RxSharedPrefs(application, this::testRestoreFunction);

        Assert.assertNotNull(mInputApplyFile);
        Assert.assertEquals(
                RxSharedPrefs.CONFIGURATION_LEVEL_VALUE,
                getSharedPreferences().getInt(RxSharedPrefs.CONFIGURATION_VERSION, -12));
    }

    @Test
    public void testReturnsDefaultsUntilUserUnlocks() throws Exception {

        Application application = getApplicationContext();
        ShadowUserManager shadowUserManager =
                Shadows.shadowOf(application.getSystemService(UserManager.class));
        shadowUserManager.setUserUnlocked(false);

        SharedPrefsHelper.setPrefsValue("testing-int", 42);
        SharedPrefsHelper.setPrefsValue("testing-string", "42");
        RxSharedPrefs underTest = new RxSharedPrefs(application, this::testRestoreFunction);

        AtomicInteger intAnswer = new AtomicInteger(0);
        AtomicReference<String> stringAnswer = new AtomicReference<>("none");

        mCompositeDisposable.add(
                underTest
                        .getInteger("testing-int", R.integer.pref_test_int_value)
                        .asObservable()
                        .subscribe(intAnswer::set));
        Assert.assertEquals(12, intAnswer.get());
        intAnswer.set(0);
        mCompositeDisposable.add(
                underTest
                        .getString("testing-string", R.string.pref_test_value)
                        .asObservable()
                        .subscribe(stringAnswer::set));
        Assert.assertEquals("value", stringAnswer.get());
        stringAnswer.set("none");

        shadowUserManager.setUserUnlocked(true);
        Shadows.shadowOf(application).getRegisteredReceivers().stream()
                .filter(w -> w.intentFilter.hasAction(Intent.ACTION_USER_UNLOCKED))
                .forEach(
                        w ->
                                w.broadcastReceiver.onReceive(
                                        application, new Intent(Intent.ACTION_USER_UNLOCKED)));

        // now, we will get the actual stored value
        Assert.assertEquals(42, intAnswer.get());
        Assert.assertEquals("42", stringAnswer.get());

        SharedPrefsHelper.setPrefsValue("testing-int", 43);
        SharedPrefsHelper.setPrefsValue("testing-string", "43");

        Assert.assertEquals(43, intAnswer.get());
        Assert.assertEquals("43", stringAnswer.get());
    }
}
