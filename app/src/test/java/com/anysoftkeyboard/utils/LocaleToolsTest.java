package com.anysoftkeyboard.utils;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Locale;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class LocaleToolsTest {
    private Context mContext;

    @Before
    public void setUpLocale() {
        Locale.setDefault(Locale.US);
        mContext = RuntimeEnvironment.application;
    }

    @After
    public void tearDownLocale() {
        Locale.setDefault(Locale.US);
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.JELLY_BEAN)
    public void testSetAndResetValueAPI16() {
        Assert.assertEquals("English (United States)", mContext.getResources().getConfiguration().locale.getDisplayName());

        LocaleTools.applyLocaleToContext(mContext, "iw");

        Assert.assertEquals("iw", mContext.getResources().getConfiguration().locale.getLanguage());
        Assert.assertTrue(mContext.getResources().getConfiguration().locale.getDisplayName().contains("Hebrew"));

        LocaleTools.applyLocaleToContext(mContext, "");

        Assert.assertSame(Locale.getDefault(), mContext.getResources().getConfiguration().locale);

        LocaleTools.applyLocaleToContext(mContext, "NONE_EXISTING");

        Assert.assertEquals("none_existing", mContext.getResources().getConfiguration().locale.getLanguage());
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void testSetAndResetValueAPI17WithKnownLocale() {
        Assert.assertEquals("English (United States)", mContext.getResources().getConfiguration().locale.getDisplayName());

        LocaleTools.applyLocaleToContext(mContext, "de");

        Assert.assertEquals("de", mContext.getResources().getConfiguration().locale.getLanguage());
        Assert.assertTrue(mContext.getResources().getConfiguration().locale.getDisplayName().contains("German"));

        LocaleTools.applyLocaleToContext(mContext, "");

        Assert.assertSame(Locale.getDefault(), mContext.getResources().getConfiguration().locale);

        LocaleTools.applyLocaleToContext(mContext, "NONE_EXISTING");

        Assert.assertEquals("none_existing", mContext.getResources().getConfiguration().locale.getLanguage());
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void testSetAndResetValueAPI17WithUnknownLocale() {
        Assert.assertEquals("English (United States)", mContext.getResources().getConfiguration().locale.getDisplayName());

        LocaleTools.applyLocaleToContext(mContext, "eu");

        Assert.assertEquals("eu", mContext.getResources().getConfiguration().locale.getLanguage());
        Assert.assertTrue(mContext.getResources().getConfiguration().locale.getDisplayName().contains("Basque"));

        LocaleTools.applyLocaleToContext(mContext, "");

        Assert.assertSame(Locale.getDefault(), mContext.getResources().getConfiguration().locale);

        LocaleTools.applyLocaleToContext(mContext, "NONE_EXISTING");

        Assert.assertEquals("none_existing", mContext.getResources().getConfiguration().locale.getLanguage());
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.LOLLIPOP)
    public void testSetAndResetValueAPI21() {
        Assert.assertEquals("English (United States)", mContext.getResources().getConfiguration().locale.getDisplayName());

        LocaleTools.applyLocaleToContext(mContext, "ru");

        Assert.assertEquals("ru", mContext.getResources().getConfiguration().locale.getLanguage());
        Assert.assertEquals("Russian", mContext.getResources().getConfiguration().locale.getDisplayName());

        LocaleTools.applyLocaleToContext(mContext, "");

        Assert.assertEquals(Locale.getDefault().getLanguage(), mContext.getResources().getConfiguration().locale.getLanguage());

        LocaleTools.applyLocaleToContext(mContext, "NONE_EXISTING");
        //in this API level, Android is more strict, we can not set invalid values.
        Assert.assertEquals("en", mContext.getResources().getConfiguration().locale.getLanguage());
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Test
    @Config(sdk = Build.VERSION_CODES.N)
    public void testSetAndResetValueAPI24() {
        Assert.assertEquals("English (United States)", mContext.getResources().getConfiguration().locale.getDisplayName());
        Assert.assertEquals(1, mContext.getResources().getConfiguration().getLocales().size());
        Assert.assertEquals(Locale.getDefault().getDisplayName(), mContext.getResources().getConfiguration().getLocales().get(0).getDisplayName());

        LocaleTools.applyLocaleToContext(mContext, "ru");

        Assert.assertEquals("ru", mContext.getResources().getConfiguration().locale.getLanguage());
        Assert.assertEquals("Russian", mContext.getResources().getConfiguration().locale.getDisplayName());
        Assert.assertEquals(1, mContext.getResources().getConfiguration().getLocales().size());
        Assert.assertEquals("Russian", mContext.getResources().getConfiguration().getLocales().get(0).getDisplayName());

        LocaleTools.applyLocaleToContext(mContext, "");

        Assert.assertEquals(Locale.getDefault().getLanguage(), mContext.getResources().getConfiguration().locale.getLanguage());
        Assert.assertEquals(1, mContext.getResources().getConfiguration().getLocales().size());
        Assert.assertEquals(Locale.getDefault().getDisplayName(), mContext.getResources().getConfiguration().getLocales().get(0).getDisplayName());

        LocaleTools.applyLocaleToContext(mContext, "NONE_EXISTING");
        //in this API level, Android is more strict, we can not set invalid values.
        Assert.assertEquals("en", mContext.getResources().getConfiguration().locale.getLanguage());
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.N)
    public void testSetEmptyValue() {
        Assert.assertEquals(Locale.getDefault().getDisplayName(), mContext.getResources().getConfiguration().locale.getDisplayName());

        LocaleTools.applyLocaleToContext(mContext, "");
        //should default
        Assert.assertEquals(Locale.getDefault().getLanguage(), mContext.getResources().getConfiguration().locale.getLanguage());
        Assert.assertFalse(TextUtils.isEmpty(mContext.getResources().getConfiguration().locale.getLanguage()));
    }
}