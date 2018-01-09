package com.anysoftkeyboard;

import org.junit.Assert;
import org.junit.Test;
import org.robolectric.annotation.Config;

public class AnySoftKeyboardStartUpTest extends AnySoftKeyboardBaseTest {
    @Test
    @Config(sdk = Config.ALL_SDKS)
    public void testBasicWorks() throws Exception {
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
