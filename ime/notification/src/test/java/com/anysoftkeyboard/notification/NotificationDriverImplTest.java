package com.anysoftkeyboard.notification;

import android.Manifest;
import android.app.NotificationManager;
import android.os.Build;
import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.test.TestFragmentActivity;
import java.util.Objects;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class NotificationDriverImplTest {

  private NotificationDriverImpl mUnderTest;

  @Before
  public void setUp() {
    mUnderTest = new NotificationDriverImpl(RuntimeEnvironment.getApplication());
  }

  @Test
  public void testInitializeChannelsForProduction() {
    var context = RuntimeEnvironment.getApplication();
    var manager = context.getSystemService(NotificationManager.class);
    Assert.assertEquals(0, manager.getNotificationChannels().size());

    mUnderTest.initializeChannels(true);

    Assert.assertEquals(2, manager.getNotificationChannels().size());
    Assert.assertTrue(
        manager.getNotificationChannels().stream()
            .filter(v -> Objects.equals(v.getId(), NotificationChannels.Tester.mChannelId))
            .findFirst()
            .isEmpty());
  }

  @Test
  public void testInitializeChannelsForTesters() {
    var context = RuntimeEnvironment.getApplication();
    var manager = context.getSystemService(NotificationManager.class);
    Assert.assertEquals(0, manager.getNotificationChannels().size());

    mUnderTest.initializeChannels(false);

    Assert.assertEquals(3, manager.getNotificationChannels().size());
    Assert.assertFalse(
        manager.getNotificationChannels().stream()
            .filter(v -> Objects.equals(v.getId(), NotificationChannels.Tester.mChannelId))
            .findFirst()
            .isEmpty());
  }

  @Test(expected = RuntimeException.class)
  public void testDoubleInitFails() {
    mUnderTest.initializeChannels(true);
    mUnderTest.initializeChannels(true);
  }

  @Test
  @Config(sdk = Build.VERSION_CODES.S_V2)
  public void testAlwaysHavePermissionToPostNotification() {
    try (var scenario = ActivityScenario.launch(TestFragmentActivity.class)) {
      scenario
          .moveToState(Lifecycle.State.RESUMED)
          .onActivity(
              activity -> {
                Assert.assertTrue(mUnderTest.askForNotificationPostPermission(activity));
              });
    }
  }

  @Test
  @Config(sdk = Build.VERSION_CODES.TIRAMISU)
  public void testReturnsTrueIfAlreadyHasPermission() {
    var appShadow = Shadows.shadowOf(RuntimeEnvironment.getApplication());
    appShadow.grantPermissions(Manifest.permission.POST_NOTIFICATIONS);
    try (var scenario = ActivityScenario.launch(TestFragmentActivity.class)) {
      scenario
          .moveToState(Lifecycle.State.RESUMED)
          .onActivity(
              activity -> {
                Assert.assertTrue(mUnderTest.askForNotificationPostPermission(activity));
              });
    }
  }
}
