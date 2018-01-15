package com.anysoftkeyboard;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;

import com.anysoftkeyboard.test.SharedPrefsHelper;
import com.menny.android.anysoftkeyboard.R;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Locale;

@RunWith(AnySoftKeyboardTestRunner.class)
public class AnySoftKeyboardForceLocaleTest extends AnySoftKeyboardBaseTest {

    @Before
    public void setUpLocale() {
        Locale.setDefault(Locale.US);
    }

    @After
    public void tearDownLocale() {
        Locale.setDefault(Locale.US);
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.JELLY_BEAN)
    public void testSetAndResetValueAPI16() {
        Assert.assertEquals("System", RuntimeEnvironment.application.getString(R.string.settings_default_force_locale_setting));
        Assert.assertEquals("English (United States)", mAnySoftKeyboardUnderTest.getResources().getConfiguration().locale.getDisplayName());

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_force_locale, "iw");

        Assert.assertEquals("iw", mAnySoftKeyboardUnderTest.getResources().getConfiguration().locale.getLanguage());
        Assert.assertTrue(mAnySoftKeyboardUnderTest.getResources().getConfiguration().locale.getDisplayName().contains("Hebrew"));

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_force_locale, "System");

        Assert.assertSame(Locale.getDefault(), mAnySoftKeyboardUnderTest.getResources().getConfiguration().locale);

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_force_locale, "NONE_EXISTING");

        Assert.assertEquals("none_existing", mAnySoftKeyboardUnderTest.getResources().getConfiguration().locale.getLanguage());
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void testSetAndResetValueAPI17WithKnownLocale() {
        Assert.assertEquals("System", RuntimeEnvironment.application.getString(R.string.settings_default_force_locale_setting));
        Assert.assertEquals("English (United States)", mAnySoftKeyboardUnderTest.getResources().getConfiguration().locale.getDisplayName());

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_force_locale, "de");

        Assert.assertEquals("de", mAnySoftKeyboardUnderTest.getResources().getConfiguration().locale.getLanguage());
        Assert.assertTrue(mAnySoftKeyboardUnderTest.getResources().getConfiguration().locale.getDisplayName().contains("German"));

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_force_locale, "System");

        Assert.assertSame(Locale.getDefault(), mAnySoftKeyboardUnderTest.getResources().getConfiguration().locale);

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_force_locale, "NONE_EXISTING");

        Assert.assertEquals("none_existing", mAnySoftKeyboardUnderTest.getResources().getConfiguration().locale.getLanguage());
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void testSetAndResetValueAPI17WithUnknownLocale() {
        Assert.assertEquals("System", RuntimeEnvironment.application.getString(R.string.settings_default_force_locale_setting));
        Assert.assertEquals("English (United States)", mAnySoftKeyboardUnderTest.getResources().getConfiguration().locale.getDisplayName());

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_force_locale, "eu");

        Assert.assertEquals("eu", mAnySoftKeyboardUnderTest.getResources().getConfiguration().locale.getLanguage());
        Assert.assertTrue(mAnySoftKeyboardUnderTest.getResources().getConfiguration().locale.getDisplayName().contains("Basque"));

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_force_locale, "System");

        Assert.assertSame(Locale.getDefault(), mAnySoftKeyboardUnderTest.getResources().getConfiguration().locale);

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_force_locale, "NONE_EXISTING");

        Assert.assertEquals("none_existing", mAnySoftKeyboardUnderTest.getResources().getConfiguration().locale.getLanguage());
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.LOLLIPOP)
    public void testSetAndResetValueAPI21() {
        Assert.assertEquals("System", RuntimeEnvironment.application.getString(R.string.settings_default_force_locale_setting));
        Assert.assertEquals("English (United States)", mAnySoftKeyboardUnderTest.getResources().getConfiguration().locale.getDisplayName());

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_force_locale, "ru");

        Assert.assertEquals("ru", mAnySoftKeyboardUnderTest.getResources().getConfiguration().locale.getLanguage());
        Assert.assertEquals("Russian", mAnySoftKeyboardUnderTest.getResources().getConfiguration().locale.getDisplayName());

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_force_locale, "System");

        Assert.assertEquals(Locale.getDefault().getLanguage(), mAnySoftKeyboardUnderTest.getResources().getConfiguration().locale.getLanguage());

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_force_locale, "NONE_EXISTING");
        //in this API level, Android is more strict, we can not set invalid values.
        Assert.assertEquals("en", mAnySoftKeyboardUnderTest.getResources().getConfiguration().locale.getLanguage());
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Test
    @Config(sdk = Build.VERSION_CODES.N)
    public void testSetAndResetValueAPI24() {
        Assert.assertEquals("System", RuntimeEnvironment.application.getString(R.string.settings_default_force_locale_setting));
        Assert.assertEquals("English (United States)", mAnySoftKeyboardUnderTest.getResources().getConfiguration().locale.getDisplayName());
        Assert.assertEquals(1, mAnySoftKeyboardUnderTest.getResources().getConfiguration().getLocales().size());
        Assert.assertEquals(Locale.getDefault().getDisplayName(), mAnySoftKeyboardUnderTest.getResources().getConfiguration().getLocales().get(0).getDisplayName());

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_force_locale, "ru");

        Assert.assertEquals("ru", mAnySoftKeyboardUnderTest.getResources().getConfiguration().locale.getLanguage());
        Assert.assertEquals("Russian", mAnySoftKeyboardUnderTest.getResources().getConfiguration().locale.getDisplayName());
        Assert.assertEquals(1, mAnySoftKeyboardUnderTest.getResources().getConfiguration().getLocales().size());
        Assert.assertEquals("Russian", mAnySoftKeyboardUnderTest.getResources().getConfiguration().getLocales().get(0).getDisplayName());

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_force_locale, "System");

        Assert.assertEquals(Locale.getDefault().getLanguage(), mAnySoftKeyboardUnderTest.getResources().getConfiguration().locale.getLanguage());
        Assert.assertEquals(1, mAnySoftKeyboardUnderTest.getResources().getConfiguration().getLocales().size());
        Assert.assertEquals(Locale.getDefault().getDisplayName(), mAnySoftKeyboardUnderTest.getResources().getConfiguration().getLocales().get(0).getDisplayName());

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_force_locale, "NONE_EXISTING");
        //in this API level, Android is more strict, we can not set invalid values.
        Assert.assertEquals("en", mAnySoftKeyboardUnderTest.getResources().getConfiguration().locale.getLanguage());
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.N)
    public void testSetEmptyValue() {
        Assert.assertEquals(Locale.getDefault().getDisplayName(), mAnySoftKeyboardUnderTest.getResources().getConfiguration().locale.getDisplayName());

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_force_locale, "");
        //should default
        Assert.assertEquals(Locale.getDefault().getLanguage(), mAnySoftKeyboardUnderTest.getResources().getConfiguration().locale.getLanguage());
        Assert.assertFalse(TextUtils.isEmpty(mAnySoftKeyboardUnderTest.getResources().getConfiguration().locale.getLanguage()));
    }
}