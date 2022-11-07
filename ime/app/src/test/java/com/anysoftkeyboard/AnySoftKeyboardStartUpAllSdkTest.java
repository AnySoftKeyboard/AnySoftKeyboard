package com.anysoftkeyboard;

import static org.robolectric.annotation.Config.OLDEST_SDK;

import com.anysoftkeyboard.test.TestUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public abstract class AnySoftKeyboardStartUpAllSdkTest extends AnySoftKeyboardBaseTest {

    void testBasicWorks_impl() {
        TestInputConnection inputConnection =
                (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();
        mAnySoftKeyboardUnderTest.simulateTextTyping("h");
        mAnySoftKeyboardUnderTest.simulateTextTyping("e");
        mAnySoftKeyboardUnderTest.simulateTextTyping("l");
        Assert.assertEquals("hel", inputConnection.getCurrentTextInInputConnection());
        verifySuggestions(true, "hel", "he'll", "hello", "hell");

        simulateFinishInputFlow();

        simulateOnStartInputFlow();
    }

    public static class AnySoftKeyboardStartUpAllSdkTest1 extends AnySoftKeyboardStartUpAllSdkTest {
        @Test
        @Config(minSdk = OLDEST_SDK, maxSdk = 21)
        public void testBasicWorks() {
            testBasicWorks_impl();
        }
    }

    public static class AnySoftKeyboardStartUpAllSdkTest2 extends AnySoftKeyboardStartUpAllSdkTest {
        @Test
        @Config(minSdk = 22, maxSdk = 25)
        public void testBasicWorks() {
            testBasicWorks_impl();
        }
    }

    public static class AnySoftKeyboardStartUpAllSdkTest3 extends AnySoftKeyboardStartUpAllSdkTest {
        @Test
        @Config(minSdk = 26, maxSdk = TestUtils.NEWEST_STABLE_API_LEVEL)
        public void testBasicWorks() {
            testBasicWorks_impl();
        }
    }
}
