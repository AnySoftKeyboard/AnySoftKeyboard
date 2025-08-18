package com.anysoftkeyboard.addons;

import android.app.Application;
import android.content.Intent;
import androidx.test.core.app.ApplicationProvider;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Shadows;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class UserUnlockedReceiverTest {

  private AtomicReference<Intent> mMockUnlockConsumer;
  private UserUnlockedReceiver mUnderTest;
  private Application mApp;

  @Before
  public void setUp() {
    mApp = ApplicationProvider.getApplicationContext();
    mMockUnlockConsumer = new AtomicReference<>(null);
    mUnderTest = new UserUnlockedReceiver(mMockUnlockConsumer::set);
  }

  @Test
  public void testDoesNotRunOnNullIntent() {
    mUnderTest.onReceive(mApp, null);
    Assert.assertNull(mMockUnlockConsumer.get());
  }

  @Test
  public void testDoesNotRunOnWrongAction() {
    mUnderTest.onReceive(mApp, new Intent(Intent.ACTION_BOOT_COMPLETED));
    Assert.assertNull(mMockUnlockConsumer.get());
  }

  @Test
  public void testRunsOnUserUnlocked() {
    var intent = new Intent(Intent.ACTION_USER_UNLOCKED);
    var shadowApp = Shadows.shadowOf(mApp);
    mUnderTest.onReceive(mApp, intent);
    Assert.assertSame(mMockUnlockConsumer.get(), intent);
  }

  @Test
  public void testCreatesValidIntentFilter() {
    var filter = UserUnlockedReceiver.createIntentFilter();
    Assert.assertNotNull(filter);
    Assert.assertEquals(1, filter.countActions());
    Assert.assertTrue(filter.hasAction(Intent.ACTION_USER_UNLOCKED));
  }
}
