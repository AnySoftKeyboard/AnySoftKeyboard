package com.anysoftkeyboard;

import static org.robolectric.annotation.Config.OLDEST_SDK;

import android.os.Build;
import com.anysoftkeyboard.test.TestUtils;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public abstract class AnySoftKeyboardStartUpAllSdkTest extends AnySoftKeyboardBaseTest {

  @Override
  public void setUpForAnySoftKeyboardBase() throws Exception {
    // get around java.lang.IllegalStateException: The Window Context should have been attached to a
    // DisplayArea
    Assume.assumeTrue("Need to figure how to start it in 32", Build.VERSION.SDK_INT != 32);
    Assume.assumeTrue("Need to figure how to start it in 33", Build.VERSION.SDK_INT != 33);
    Assume.assumeTrue("Need to figure how to start it in 34", Build.VERSION.SDK_INT != 34);
    super.setUpForAnySoftKeyboardBase();
  }

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

  public static class AnySoftKeyboardStartUpAllSdkShard1Test
      extends AnySoftKeyboardStartUpAllSdkTest {
    @Test
    @Config(minSdk = OLDEST_SDK, maxSdk = 23)
    public void testBasicWorks() {
      testBasicWorks_impl();
    }
  }

  public static class AnySoftKeyboardStartUpAllSdkShard2Test
      extends AnySoftKeyboardStartUpAllSdkTest {
    @Test
    @Config(minSdk = 24, maxSdk = 28)
    public void testBasicWorks() {
      testBasicWorks_impl();
    }
  }

  public static class AnySoftKeyboardStartUpAllSdkShard3Test
      extends AnySoftKeyboardStartUpAllSdkTest {
    @Test
    @Config(minSdk = 29, maxSdk = TestUtils.NEWEST_STABLE_API_LEVEL)
    public void testBasicWorks() {
      testBasicWorks_impl();
    }
  }
}
