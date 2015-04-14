package com.anysoftkeyboard.ui.settings.setup;

import android.content.ComponentName;

import com.menny.android.anysoftkeyboard.AskGradleTestRunner;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AskGradleTestRunner.class)
public class SetupSupportTest {

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
}
