package com.anysoftkeyboard.addons;

import android.content.Intent;
import androidx.test.core.app.ApplicationProvider;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class UserUnlockedReceiverTest {

  private Runnable mMockUnlockRunnable;
  private UserUnlockedReceiver mUnderTest;

  @Before
  public void setUp() {
    mMockUnlockRunnable = Mockito.mock(Runnable.class);
    mUnderTest = new UserUnlockedReceiver(mMockUnlockRunnable);
  }

  @Test
  public void testDoesNotRunOnNullIntent() {
    mUnderTest.onReceive(ApplicationProvider.getApplicationContext(), null);
    Mockito.verify(mMockUnlockRunnable, Mockito.never()).run();
  }

  @Test
  public void testDoesNotRunOnWrongAction() {
    mUnderTest.onReceive(
        ApplicationProvider.getApplicationContext(), new Intent(Intent.ACTION_BOOT_COMPLETED));
    Mockito.verify(mMockUnlockRunnable, Mockito.never()).run();
  }

  @Test
  public void testRunsOnUserUnlocked() {
    mUnderTest.onReceive(
        ApplicationProvider.getApplicationContext(), new Intent(Intent.ACTION_USER_UNLOCKED));
    Mockito.verify(mMockUnlockRunnable).run();
  }

  @Test
  public void testCreatesValidIntentFilter() {
    var filter = mUnderTest.createIntentFilter();
    Assert.assertNotNull(filter);
    Assert.assertEquals(1, filter.countActions());
    Assert.assertTrue(filter.hasAction(Intent.ACTION_USER_UNLOCKED));
  }
}
