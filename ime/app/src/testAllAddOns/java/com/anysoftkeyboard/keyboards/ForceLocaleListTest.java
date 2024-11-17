package com.anysoftkeyboard.keyboards;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.text.TextUtils;
import androidx.test.core.app.ApplicationProvider;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.test.TestUtils;
import com.anysoftkeyboard.utils.LocaleTools;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;
import java.util.Locale;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
@Config(sdk = TestUtils.LATEST_STABLE_API_LEVEL)
public class ForceLocaleListTest {

  @Test
  public void testAllLocaleInForceLocalesListAreValid() throws Exception {
    final String[] forceLocaleArray =
        ApplicationProvider.getApplicationContext()
            .getResources()
            .getStringArray(R.array.settings_key_force_locale_values);
    for (String locale : forceLocaleArray) {
      final Locale actualLocale = Locale.forLanguageTag(locale);
      Assert.assertNotNull(actualLocale);
    }
  }

  @Test
  public void testNoForceLocaleCrashes() throws Exception {
    final String[] forceLocaleArray =
        ApplicationProvider.getApplicationContext()
            .getResources()
            .getStringArray(R.array.settings_key_force_locale_values);
    for (String locale : forceLocaleArray) {
      Assert.assertNotNull(LocaleTools.getLocaleForLocaleString(locale));
    }
  }

  @Test
  public void testAllLocalesInKeyboardAddOnsAreValid() throws Exception {
    AnyApplication.getKeyboardFactory(getApplicationContext())
        .getAllAddOns()
        .forEach(
            builder -> {
              final String keyboardLocale = builder.getKeyboardLocale();
              // keyboards may have empty locale. This means they don't want
              // a dictionary (say, Terminal)
              if (!TextUtils.isEmpty(keyboardLocale)) {
                Assert.assertNotNull(
                    "Looking for locate tag " + keyboardLocale,
                    Locale.forLanguageTag(keyboardLocale));
              }
            });
  }

  @Test
  public void testAllLocalesInDictionaryAddOnsAreValid() throws Exception {
    AnyApplication.getExternalDictionaryFactory(getApplicationContext())
        .getAllAddOns()
        .forEach(
            builder -> {
              final String localeString = builder.getLanguage();
              Assert.assertNotNull("for dictionary " + builder.getId(), localeString);
              Assert.assertFalse(
                  "for dictionary " + builder.getId(), TextUtils.isEmpty(localeString));
              Assert.assertNotNull(
                  "Looking for locate tag " + localeString, Locale.forLanguageTag(localeString));
            });
  }
}
