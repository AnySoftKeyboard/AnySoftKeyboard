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

        simulateFinishInputFlow(false);

        simulateFinishInputFlow(false);

        Assert.assertEquals("hel", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.simulateTextTyping("l");
        mAnySoftKeyboardUnderTest.simulateTextTyping("o");
        Assert.assertEquals("hello", inputConnection.getCurrentTextInInputConnection());
    }
}
