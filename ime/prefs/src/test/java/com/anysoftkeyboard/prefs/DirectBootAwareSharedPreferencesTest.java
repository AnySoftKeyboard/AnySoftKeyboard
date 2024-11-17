package com.anysoftkeyboard.prefs;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.UserManager;
import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.test.core.app.ApplicationProvider;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.test.SharedPrefsHelper;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowUserManager;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.N)
public class DirectBootAwareSharedPreferencesTest {

  private TestSharedPreferencesFactory mFactory;

  @Before
  public void setUp() {
    mFactory = new TestSharedPreferencesFactory();
  }

  @Test
  public void testInNormalModeRedirects() {
    mFactory.setInDirectBoot(false);

    DirectBootAwareSharedPreferences underTest =
        new DirectBootAwareSharedPreferences(
            ApplicationProvider.getApplicationContext(), sp -> {}, mFactory);

    underTest
        .edit()
        .putBoolean("boolean", true)
        .putFloat("float", 1.1f)
        .putString("string", "a string")
        .putInt("int", 42)
        .putLong("long", 99999999999L)
        .putStringSet("set", Collections.singleton("a value"))
        .commit();

    Assert.assertTrue(underTest.getBoolean("boolean", false));
    Assert.assertEquals(1.1f, underTest.getFloat("float", 3.3f), 0.2f);
    Assert.assertEquals("a string", underTest.getString("string", "not a string"));
    Assert.assertEquals(42, underTest.getInt("int", 1));
    Assert.assertEquals(99999999999L, underTest.getLong("long", 123L));
    Assert.assertArrayEquals(
        new String[] {"a value"},
        underTest.getStringSet("set", Collections.emptySet()).toArray(new String[0]));
  }

  @Test
  public void testInLockedModeDoesNothing() {
    mFactory.setInDirectBoot(true);

    DirectBootAwareSharedPreferences underTest =
        new DirectBootAwareSharedPreferences(
            ApplicationProvider.getApplicationContext(), sp -> {}, mFactory);

    underTest
        .edit()
        .putBoolean("boolean", true)
        .putFloat("float", 1.1f)
        .putString("string", "a string")
        .putInt("int", 42)
        .putLong("long", 99999999999L)
        .putStringSet("set", Collections.singleton("a value"))
        .commit();

    // returns the defaults
    Assert.assertFalse(underTest.getBoolean("boolean", false));
    Assert.assertEquals(3.3f, underTest.getFloat("float", 3.3f), 0.2f);
    Assert.assertEquals("not a string", underTest.getString("string", "not a string"));
    Assert.assertEquals(1, underTest.getInt("int", 1));
    Assert.assertEquals(123L, underTest.getLong("long", 123L));
    Assert.assertArrayEquals(
        new String[0],
        underTest.getStringSet("set", Collections.emptySet()).toArray(new String[0]));
  }

  @Test
  public void testInLockedToNormalModeSwitch() {
    mFactory.setInDirectBoot(true);

    DirectBootAwareSharedPreferences underTest =
        new DirectBootAwareSharedPreferences(
            ApplicationProvider.getApplicationContext(), sp -> {}, mFactory);

    underTest.edit().putBoolean("boolean", true).commit();
    // returns the defaults
    Assert.assertFalse(underTest.getBoolean("boolean", false));

    mFactory.setInDirectBoot(false);
    underTest.edit().putBoolean("boolean", true).commit();
    // returns the saved value
    Assert.assertTrue(underTest.getBoolean("boolean", false));
  }

  @Test
  @Config(sdk = Build.VERSION_CODES.M)
  public void testDoesNotCreateReceiverIfOldVersion() {
    DirectBootAwareSharedPreferences underTest =
        new DirectBootAwareSharedPreferences(
            ApplicationProvider.getApplicationContext(), sp -> {}, mFactory);
    Assert.assertNotNull(underTest);
    ShadowApplication shadowApplication =
        Shadows.shadowOf((Application) ApplicationProvider.getApplicationContext());
    Assert.assertFalse(
        shadowApplication.getRegisteredReceivers().stream()
            .anyMatch(w -> w.intentFilter.hasAction(Intent.ACTION_USER_UNLOCKED)));
  }

  @Test
  public void testDoesNotCreateReceiverIfNoNeed() {
    mFactory.setInDirectBoot(false);
    DirectBootAwareSharedPreferences underTest =
        new DirectBootAwareSharedPreferences(
            ApplicationProvider.getApplicationContext(), sp -> {}, mFactory);
    Assert.assertNotNull(underTest);
    ShadowApplication shadowApplication =
        Shadows.shadowOf((Application) ApplicationProvider.getApplicationContext());
    Assert.assertFalse(
        shadowApplication.getRegisteredReceivers().stream()
            .anyMatch(w -> w.intentFilter.hasAction(Intent.ACTION_USER_UNLOCKED)));
  }

  @Test
  public void testCreateReceiverIfNeededAndRemovesWhenInTheClear() {
    mFactory.setInDirectBoot(true);
    Context applicationContext = ApplicationProvider.getApplicationContext();
    DirectBootAwareSharedPreferences underTest =
        new DirectBootAwareSharedPreferences(applicationContext, sp -> {}, mFactory);
    Assert.assertNotNull(underTest);
    ShadowApplication shadowApplication = Shadows.shadowOf((Application) applicationContext);
    Assert.assertTrue(
        shadowApplication.getRegisteredReceivers().stream()
            .anyMatch(w -> w.intentFilter.hasAction(Intent.ACTION_USER_UNLOCKED)));
    // if receiver gets a null intent, it should not unregister
    shadowApplication.getRegisteredReceivers().stream()
        .filter(w -> w.intentFilter.hasAction(Intent.ACTION_USER_UNLOCKED))
        .forEach(w -> w.broadcastReceiver.onReceive(applicationContext, null));
    Assert.assertTrue(
        shadowApplication.getRegisteredReceivers().stream()
            .anyMatch(w -> w.intentFilter.hasAction(Intent.ACTION_USER_UNLOCKED)));
    // if receiver gets an intent with a different action, it should not unregister
    shadowApplication.getRegisteredReceivers().stream()
        .filter(w -> w.intentFilter.hasAction(Intent.ACTION_USER_UNLOCKED))
        .forEach(
            w -> w.broadcastReceiver.onReceive(applicationContext, new Intent(Intent.ACTION_SEND)));
    Assert.assertTrue(
        shadowApplication.getRegisteredReceivers().stream()
            .anyMatch(w -> w.intentFilter.hasAction(Intent.ACTION_USER_UNLOCKED)));
    // if receiver gets the right action, it should unregister
    mFactory.setInDirectBoot(false);
    Assert.assertFalse(
        shadowApplication.getRegisteredReceivers().stream()
            .anyMatch(w -> w.intentFilter.hasAction(Intent.ACTION_USER_UNLOCKED)));
  }

  @Test
  @Config(sdk = Build.VERSION_CODES.M)
  public void testCallsOnReadyAfterCreateOnOldDevices() {
    final AtomicReference<SharedPreferences> called = new AtomicReference<>(null);
    DirectBootAwareSharedPreferences underTest =
        new DirectBootAwareSharedPreferences(
            ApplicationProvider.getApplicationContext(), called::set, mFactory);
    Assert.assertNotNull(underTest);
    Assert.assertSame(underTest, called.get());
  }

  @Test
  public void testCallsOnReadyIfDeviceIsUnlocked() {
    mFactory.setInDirectBoot(false);
    final AtomicReference<SharedPreferences> called = new AtomicReference<>(null);
    DirectBootAwareSharedPreferences underTest =
        new DirectBootAwareSharedPreferences(
            ApplicationProvider.getApplicationContext(), called::set, mFactory);
    Assert.assertNotNull(underTest);
    Assert.assertSame(underTest, called.get());
  }

  @Test
  public void testCallsOnReadyAfterDeviceIsUnlocked() {
    mFactory.setInDirectBoot(true);
    final AtomicReference<SharedPreferences> called = new AtomicReference<>(null);
    DirectBootAwareSharedPreferences underTest =
        new DirectBootAwareSharedPreferences(
            ApplicationProvider.getApplicationContext(), called::set, mFactory);
    Assert.assertNotNull(underTest);
    // still locked
    Assert.assertNull(called.get());

    // unlocking
    mFactory.setInDirectBoot(false);
    Assert.assertSame(underTest, called.get());
  }

  @Test
  public void testListenersPassedWhenSwitchPrefImpl() {
    mFactory.setInDirectBoot(true);

    DirectBootAwareSharedPreferences underTest =
        new DirectBootAwareSharedPreferences(
            ApplicationProvider.getApplicationContext(), sp -> {}, mFactory);

    final AtomicInteger valueReceiver = new AtomicInteger(-1);
    SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener =
        (sharedPreferences, key) -> valueReceiver.set(sharedPreferences.getInt("int", -1));
    underTest.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
    underTest.edit().putInt("int", 1).commit();
    // nothing was set, we're in no-op land
    Assert.assertEquals(-1, valueReceiver.get());

    mFactory.setInDirectBoot(false);
    Assert.assertEquals(-1, valueReceiver.get());
    underTest.edit().putInt("int", 2).commit();
    Assert.assertEquals(2, valueReceiver.get());
  }

  @Test
  public void testListenersCalledWithAllKeysWhenSwitched() {
    SharedPrefsHelper.setPrefsValue("int", 40);
    mFactory.setInDirectBoot(true);

    DirectBootAwareSharedPreferences underTest =
        new DirectBootAwareSharedPreferences(
            ApplicationProvider.getApplicationContext(), sp -> {}, mFactory);

    final AtomicInteger valueReceiver = new AtomicInteger(-1);
    SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener =
        (sharedPreferences, key) -> valueReceiver.set(sharedPreferences.getInt("int", -1));
    underTest.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
    underTest.edit().putInt("int", 1).commit();
    // nothing was set, we're in no-op land
    Assert.assertEquals(-1, valueReceiver.get());

    mFactory.setInDirectBoot(false);
    // listener was called on switch
    Assert.assertEquals(40, valueReceiver.get());
    underTest.edit().putInt("int", 2).commit();
    Assert.assertEquals(2, valueReceiver.get());
  }

  @Test
  public void testListenersNotPassedWhenSwitchPrefImplIfRemoved() {
    mFactory.setInDirectBoot(true);

    DirectBootAwareSharedPreferences underTest =
        new DirectBootAwareSharedPreferences(
            ApplicationProvider.getApplicationContext(), sp -> {}, mFactory);

    final AtomicInteger valueReceiver = new AtomicInteger(-1);
    SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener =
        (sharedPreferences, key) -> valueReceiver.set(sharedPreferences.getInt("int", -1));
    underTest.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
    underTest.unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);

    mFactory.setInDirectBoot(false);
    Assert.assertEquals(-1, valueReceiver.get());
    underTest.edit().putInt("int", 2).commit();
    // it is unregister, nothing will happen
    Assert.assertEquals(-1, valueReceiver.get());
  }

  public static class TestSharedPreferencesFactory
      implements DirectBootAwareSharedPreferences.SharedPreferencesFactory {
    private final ShadowUserManager mShadowUserManager;
    private boolean mInDirectBootState = false;

    TestSharedPreferencesFactory() {
      mShadowUserManager =
          Shadows.shadowOf(
              ApplicationProvider.getApplicationContext().getSystemService(UserManager.class));
    }

    public void setInDirectBoot(boolean directBoot) {
      directBoot = directBoot && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
      if (mInDirectBootState != directBoot) {
        mInDirectBootState = directBoot;
        mShadowUserManager.setUserUnlocked(!mInDirectBootState);
        if (!mInDirectBootState /*boot ended*/) {
          Application applicationContext = ApplicationProvider.getApplicationContext();
          Shadows.shadowOf(applicationContext).getRegisteredReceivers().stream()
              .filter(w -> w.intentFilter.hasAction(Intent.ACTION_USER_UNLOCKED))
              .forEach(
                  w ->
                      w.broadcastReceiver.onReceive(
                          applicationContext, new Intent(Intent.ACTION_USER_UNLOCKED)));
        }
      }
    }

    @NonNull @Override
    public SharedPreferences create(@NonNull Context context) {
      if (mInDirectBootState) {
        throw new IllegalStateException("in direct-boot state");
      } else {
        return PreferenceManager.getDefaultSharedPreferences(context);
      }
    }
  }
}
