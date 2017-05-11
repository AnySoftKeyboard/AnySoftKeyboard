package com.anysoftkeyboard.ui.settings.setup;

import android.content.ComponentName;

import com.anysoftkeyboard.AnySoftKeyboardTestRunner;
import com.anysoftkeyboard.keyboards.KeyboardAddOnAndBuilder;
import com.anysoftkeyboard.keyboards.KeyboardFactory;
import com.menny.android.anysoftkeyboard.AnyRoboApplication;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.Locale;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AnySoftKeyboardTestRunner.class)
public class SetupSupportTest {

    private AnyRoboApplication mApplication;

    @Before
    public void setup() {
        Locale.setDefault(Locale.US);
        mApplication = (AnyRoboApplication) RuntimeEnvironment.application;
    }

    @After
    public void tearDown() {
        Locale.setDefault(Locale.US);
    }

    @Test
    public void testIsThisKeyboardSetAsDefaultIME() throws Exception {
        final String MY_IME_PACKAGE = "net.evendanan.ime";
        assertFalse(SetupSupport.isThisKeyboardSetAsDefaultIME(new ComponentName("net.some.one.else", "net.some.one.else.IME").flattenToString(), MY_IME_PACKAGE));
        assertFalse(SetupSupport.isThisKeyboardSetAsDefaultIME(new ComponentName("net.some.one.else", "net.some.other.IME").flattenToString(), MY_IME_PACKAGE));
        assertFalse(SetupSupport.isThisKeyboardSetAsDefaultIME(new ComponentName("net.some.one.else", ".IME").flattenToString(), MY_IME_PACKAGE));
        assertFalse(SetupSupport.isThisKeyboardSetAsDefaultIME(null, MY_IME_PACKAGE));

        assertTrue(SetupSupport.isThisKeyboardSetAsDefaultIME(new ComponentName(MY_IME_PACKAGE, MY_IME_PACKAGE + ".IME").flattenToString(), MY_IME_PACKAGE));
        assertTrue(SetupSupport.isThisKeyboardSetAsDefaultIME(new ComponentName(MY_IME_PACKAGE, "net.some.other.IME").flattenToString(), MY_IME_PACKAGE));
        assertTrue(SetupSupport.isThisKeyboardSetAsDefaultIME(new ComponentName(MY_IME_PACKAGE, ".IME").flattenToString(), MY_IME_PACKAGE));
    }

    @Test
    public void testIsThisKeyboardEnabled() throws Exception {
        final String MY_IME_PACKAGE = "net.evendanan.ime";
        assertFalse(SetupSupport.isThisKeyboardEnabled("", MY_IME_PACKAGE));
        //one keyboard
        assertFalse(SetupSupport.isThisKeyboardEnabled(new ComponentName("net.some.one.else", "net.some.one.else.IME").flattenToString(), MY_IME_PACKAGE));
        assertFalse(SetupSupport.isThisKeyboardEnabled(new ComponentName("net.some.one.else", "net.some.other.IME").flattenToString(), MY_IME_PACKAGE));
        assertFalse(SetupSupport.isThisKeyboardEnabled(new ComponentName("net.some.one.else", ".IME").flattenToString(), MY_IME_PACKAGE));
        assertFalse(SetupSupport.isThisKeyboardEnabled(null, MY_IME_PACKAGE));

        assertTrue(SetupSupport.isThisKeyboardEnabled(new ComponentName(MY_IME_PACKAGE, MY_IME_PACKAGE + ".IME").flattenToString(), MY_IME_PACKAGE));
        assertTrue(SetupSupport.isThisKeyboardEnabled(new ComponentName(MY_IME_PACKAGE, "net.some.other.IME").flattenToString(), MY_IME_PACKAGE));
        assertTrue(SetupSupport.isThisKeyboardEnabled(new ComponentName(MY_IME_PACKAGE, ".IME").flattenToString(), MY_IME_PACKAGE));

        //now, two keyboards
        assertFalse(SetupSupport.isThisKeyboardEnabled(
                new ComponentName("net.some.one.else", "net.some.one.else.IME").flattenToString() + ":" + new ComponentName("net.some.one.e1", "net.some.one.e1.IME").flattenToString(),
                MY_IME_PACKAGE));
        assertFalse(SetupSupport.isThisKeyboardEnabled(
                new ComponentName("net.some.one.else", "net.some.other.IME").flattenToString() + ":" + new ComponentName("net.some.one.e1", "net.some.one.e1.IME").flattenToString(),
                MY_IME_PACKAGE));
        assertFalse(SetupSupport.isThisKeyboardEnabled(
                new ComponentName("net.some.one.else", ".IME").flattenToString() + ":" + new ComponentName("net.some.one.e1", "net.some.one.e1.IME").flattenToString(),
                MY_IME_PACKAGE));

        assertTrue(SetupSupport.isThisKeyboardEnabled(
                new ComponentName(MY_IME_PACKAGE, MY_IME_PACKAGE + ".IME").flattenToString() + ":" + new ComponentName("net.some.one.e1", "net.some.one.e1.IME").flattenToString(),
                MY_IME_PACKAGE));
        assertTrue(SetupSupport.isThisKeyboardEnabled(
                new ComponentName("net.some.one.e1", "net.some.one.e1.IME").flattenToString() + ":" + new ComponentName(MY_IME_PACKAGE, "net.some.other.IME").flattenToString(),
                MY_IME_PACKAGE));
        assertTrue(SetupSupport.isThisKeyboardEnabled(
                new ComponentName(MY_IME_PACKAGE, ".IME").flattenToString() + ":" + new ComponentName("net.some.one.e1", "net.some.one.e1.IME").flattenToString(),
                MY_IME_PACKAGE));

        //last test, three
        assertFalse(SetupSupport.isThisKeyboardEnabled(
                new ComponentName("net.some.one.else", "net.some.one.else.IME").flattenToString() + ":" + new ComponentName("net.some.one.e1", "net.some.one.e1.IME").flattenToString() + ":" + new ComponentName("net.some.one.e2", "net.some.one.e2.IME").flattenToString(),
                MY_IME_PACKAGE));
        assertTrue(SetupSupport.isThisKeyboardEnabled(
                new ComponentName("net.some.one.e2", ".IME").flattenToString() + ":" + new ComponentName(MY_IME_PACKAGE, ".IME").flattenToString() + ":" + new ComponentName("net.some.one.e1", ".IME").flattenToString(),
                MY_IME_PACKAGE));
    }

    @Test
    public void testHasLanguagePackForCurrentLocale() {
        final KeyboardFactory spiedKeyboardFactory = mApplication.getSpiedKeyboardFactory();
        ArrayList<KeyboardAddOnAndBuilder> mockResponse = new ArrayList<>(spiedKeyboardFactory.getAllAddOns());

        Assert.assertTrue(SetupSupport.hasLanguagePackForCurrentLocale(mockResponse));

        Locale.setDefault(Locale.FRENCH);
        Assert.assertFalse(SetupSupport.hasLanguagePackForCurrentLocale(mockResponse));

        KeyboardAddOnAndBuilder frenchBuilder = Mockito.mock(KeyboardAddOnAndBuilder.class);
        Mockito.doReturn("fr").when(frenchBuilder).getKeyboardLocale();
        mockResponse.add(frenchBuilder);

        Assert.assertTrue(SetupSupport.hasLanguagePackForCurrentLocale(mockResponse));

        Locale.setDefault(new Locale("he"));
        Assert.assertFalse(SetupSupport.hasLanguagePackForCurrentLocale(mockResponse));

        KeyboardAddOnAndBuilder hebrewBuilder = Mockito.mock(KeyboardAddOnAndBuilder.class);
        Mockito.doReturn("iw").when(hebrewBuilder).getKeyboardLocale();
        mockResponse.add(hebrewBuilder);

        Locale.setDefault(new Locale("iw"));
        Assert.assertTrue(SetupSupport.hasLanguagePackForCurrentLocale(mockResponse));

        Locale.setDefault(new Locale("ru"));
        Assert.assertFalse(SetupSupport.hasLanguagePackForCurrentLocale(mockResponse));

        Mockito.doReturn("ru").when(hebrewBuilder).getKeyboardLocale();
        Assert.assertTrue(SetupSupport.hasLanguagePackForCurrentLocale(mockResponse));
    }
}
