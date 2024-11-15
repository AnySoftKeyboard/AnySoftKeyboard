package com.anysoftkeyboard;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.annotation.SuppressLint;
import android.os.Build;
import android.text.TextUtils;
import androidx.annotation.RequiresApi;
import com.anysoftkeyboard.test.SharedPrefsHelper;
import com.menny.android.anysoftkeyboard.R;
import java.util.Locale;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
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
  @Config(sdk = Build.VERSION_CODES.LOLLIPOP)
  public void testSetAndResetValueAPI21() {
    Assert.assertEquals(
        "System",
        getApplicationContext().getString(R.string.settings_default_force_locale_setting));
    Assert.assertEquals(
        "English (United States)",
        mAnySoftKeyboardUnderTest.getResources().getConfiguration().locale.getDisplayName());

    SharedPrefsHelper.setPrefsValue(R.string.settings_key_force_locale, "ru");

    Assert.assertEquals(
        "ru", mAnySoftKeyboardUnderTest.getResources().getConfiguration().locale.getLanguage());
    Assert.assertEquals(
        "Russian",
        mAnySoftKeyboardUnderTest.getResources().getConfiguration().locale.getDisplayName());

    SharedPrefsHelper.setPrefsValue(R.string.settings_key_force_locale, "System");

    Assert.assertEquals(
        Locale.getDefault().getLanguage(),
        mAnySoftKeyboardUnderTest.getResources().getConfiguration().locale.getLanguage());

    SharedPrefsHelper.setPrefsValue(R.string.settings_key_force_locale, "NONE_EXISTING");
    // in this API level, Android is more strict, we can not set invalid values.
    Assert.assertEquals(
        "en", mAnySoftKeyboardUnderTest.getResources().getConfiguration().locale.getLanguage());
  }

  @RequiresApi(api = Build.VERSION_CODES.N)
  @Test
  @Config(sdk = Build.VERSION_CODES.N)
  @SuppressLint("UseSdkSuppress")
  public void testSetAndResetValueAPI24() {
    Assert.assertEquals(
        "System",
        getApplicationContext().getString(R.string.settings_default_force_locale_setting));
    Assert.assertEquals(
        "English (United States)",
        mAnySoftKeyboardUnderTest.getResources().getConfiguration().locale.getDisplayName());
    Assert.assertEquals(
        1, mAnySoftKeyboardUnderTest.getResources().getConfiguration().getLocales().size());
    Assert.assertEquals(
        Locale.getDefault().getDisplayName(),
        mAnySoftKeyboardUnderTest
            .getResources()
            .getConfiguration()
            .getLocales()
            .get(0)
            .getDisplayName());

    SharedPrefsHelper.setPrefsValue(R.string.settings_key_force_locale, "ru");

    Assert.assertEquals(
        "ru", mAnySoftKeyboardUnderTest.getResources().getConfiguration().locale.getLanguage());
    Assert.assertEquals(
        "Russian",
        mAnySoftKeyboardUnderTest.getResources().getConfiguration().locale.getDisplayName());
    Assert.assertEquals(
        1, mAnySoftKeyboardUnderTest.getResources().getConfiguration().getLocales().size());
    Assert.assertEquals(
        "Russian",
        mAnySoftKeyboardUnderTest
            .getResources()
            .getConfiguration()
            .getLocales()
            .get(0)
            .getDisplayName());

    SharedPrefsHelper.setPrefsValue(R.string.settings_key_force_locale, "System");

    Assert.assertEquals(
        Locale.getDefault().getLanguage(),
        mAnySoftKeyboardUnderTest.getResources().getConfiguration().locale.getLanguage());
    Assert.assertEquals(
        1, mAnySoftKeyboardUnderTest.getResources().getConfiguration().getLocales().size());
    Assert.assertEquals(
        Locale.getDefault().getDisplayName(),
        mAnySoftKeyboardUnderTest
            .getResources()
            .getConfiguration()
            .getLocales()
            .get(0)
            .getDisplayName());

    SharedPrefsHelper.setPrefsValue(R.string.settings_key_force_locale, "NONE_EXISTING");
    // in this API level, Android is more strict, we can not set invalid values.
    Assert.assertEquals(
        "en", mAnySoftKeyboardUnderTest.getResources().getConfiguration().locale.getLanguage());
  }

  @Test
  @Config(sdk = Build.VERSION_CODES.N)
  public void testSetEmptyValue() {
    Assert.assertEquals(
        Locale.getDefault().getDisplayName(),
        mAnySoftKeyboardUnderTest.getResources().getConfiguration().locale.getDisplayName());

    SharedPrefsHelper.setPrefsValue(R.string.settings_key_force_locale, "");
    // should default
    Assert.assertEquals(
        Locale.getDefault().getLanguage(),
        mAnySoftKeyboardUnderTest.getResources().getConfiguration().locale.getLanguage());
    Assert.assertFalse(
        TextUtils.isEmpty(
            mAnySoftKeyboardUnderTest.getResources().getConfiguration().locale.getLanguage()));
  }
}
