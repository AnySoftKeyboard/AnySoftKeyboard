package com.anysoftkeyboard;

import org.junit.Assert;
import org.junit.Test;
import org.robolectric.annotation.Config;

public abstract class AnySoftKeyboardStartUpTest extends AnySoftKeyboardBaseTest {

    public static class AnySoftKeyboardStartUpTest1 extends AnySoftKeyboardStartUpTest {
        @Test
        @Config(minSdk = 16, maxSdk = 21)
        public void testBasicWorks1() throws Exception {
            implTestBasicWorks();
        }
    }

    public static class AnySoftKeyboardStartUpTest2 extends AnySoftKeyboardStartUpTest {
        @Test
        @Config(minSdk = 22, maxSdk = 26)
        public void testBasicWorks2() throws Exception {
            implTestBasicWorks();
        }
    }

    protected void implTestBasicWorks() {
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();
        mAnySoftKeyboardUnderTest.simulateTextTyping("h");
        mAnySoftKeyboardUnderTest.simulateTextTyping("e");
        mAnySoftKeyboardUnderTest.simulateTextTyping("l");
        Assert.assertEquals("hel", inputConnection.getCurrentTextInInputConnection());
        verifySuggestions(true, "hel", "hell", "hello");

        simulateFinishInputFlow();

        simulateOnStartInputFlow();
    }
}
