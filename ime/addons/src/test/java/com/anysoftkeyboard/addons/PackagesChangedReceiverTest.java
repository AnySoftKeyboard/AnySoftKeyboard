package com.anysoftkeyboard.addons;

import android.app.Application;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import androidx.core.content.ContextCompat;
import androidx.test.core.app.ApplicationProvider;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowApplication;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class PackagesChangedReceiverTest {

  private AtomicReference<Intent> mMockPackageChangedConsumer;
  private PackagesChangedReceiver mUnderTest;
  private Application mApp;

  @Before
  public void setUp() {
    mApp = ApplicationProvider.getApplicationContext();
    mMockPackageChangedConsumer = new AtomicReference<>(null);
    mUnderTest = new PackagesChangedReceiver(mMockPackageChangedConsumer::set);
    // We'll register the receiver in each test if needed, or in a broader scope if all tests
    // require it.
    // For now, let's keep it simple and register/unregister per test or rely on direct onReceive
    // calls.
  }

  @Test
  public void testCreatesValidIntentFilter() {
    IntentFilter filter = PackagesChangedReceiver.createIntentFilter();
    Assert.assertNotNull(filter);
    Assert.assertEquals(4, filter.countActions());
    Assert.assertTrue(filter.hasAction(Intent.ACTION_PACKAGE_ADDED));
    Assert.assertTrue(filter.hasAction(Intent.ACTION_PACKAGE_REMOVED));
    Assert.assertTrue(filter.hasAction(Intent.ACTION_PACKAGE_REPLACED));
    Assert.assertTrue(filter.hasAction(Intent.ACTION_PACKAGE_CHANGED));

    Assert.assertEquals(1, filter.countDataSchemes());
    Assert.assertTrue(filter.hasDataScheme("package"));

    Assert.assertEquals(1, filter.countCategories());
    Assert.assertTrue(filter.hasCategory(Intent.CATEGORY_DEFAULT));
  }

  @Test
  public void testDoesNotRunOnNullIntent() {
    mUnderTest.onReceive(mApp, null);
    Assert.assertNull(mMockPackageChangedConsumer.get());
  }

  @Test
  public void testDoesNotRunOnNullIntentData() {
    mUnderTest.onReceive(mApp, new Intent());
    Assert.assertNull(mMockPackageChangedConsumer.get());
  }

  @Test
  public void testDoesNotRunOnNullContext() {
    Intent intent = new Intent(Intent.ACTION_PACKAGE_ADDED);
    intent.setData(Uri.parse("package:com.example.app"));
    mUnderTest.onReceive(null, intent);
    Assert.assertNull(mMockPackageChangedConsumer.get());
  }

  @Test
  public void testRunsOnPackageAdded() {
    Intent intent = new Intent(Intent.ACTION_PACKAGE_ADDED);
    intent.setData(Uri.parse("package:com.example.app"));
    mUnderTest.onReceive(mApp, intent);
    Assert.assertSame(intent, mMockPackageChangedConsumer.get());
  }

  @Test
  public void testRunsOnPackageRemoved() {
    Intent intent = new Intent(Intent.ACTION_PACKAGE_REMOVED);
    intent.setData(Uri.parse("package:com.example.app"));
    mUnderTest.onReceive(mApp, intent);
    Assert.assertSame(intent, mMockPackageChangedConsumer.get());
  }

  @Test
  public void testRunsOnPackageChanged() {
    Intent intent = new Intent(Intent.ACTION_PACKAGE_CHANGED);
    intent.setData(Uri.parse("package:com.example.app"));
    mUnderTest.onReceive(mApp, intent);
    Assert.assertSame(intent, mMockPackageChangedConsumer.get());
  }

  @Test
  public void testRunsOnPackageReplaced() {
    Intent intent = new Intent(Intent.ACTION_PACKAGE_REPLACED);
    intent.setData(Uri.parse("package:com.example.app"));
    mUnderTest.onReceive(mApp, intent);
    Assert.assertSame(intent, mMockPackageChangedConsumer.get());
  }

  @Test
  public void testReceiverRegistration() {
    ShadowApplication shadowApp = Shadows.shadowOf(mApp);
    IntentFilter filter = PackagesChangedReceiver.createIntentFilter();
    ContextCompat.registerReceiver(mApp, mUnderTest, filter, ContextCompat.RECEIVER_NOT_EXPORTED);

    Assert.assertTrue(
        shadowApp.getRegisteredReceivers().stream()
            .anyMatch(
                wrapper ->
                    wrapper.getBroadcastReceiver() == mUnderTest
                        && wrapper.getIntentFilter() == filter));

    mApp.unregisterReceiver(mUnderTest);

    Assert.assertFalse(
        shadowApp.getRegisteredReceivers().stream()
            .anyMatch(wrapper -> wrapper.getBroadcastReceiver() == mUnderTest));
  }
}
