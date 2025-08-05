package com.anysoftkeyboard.prefs;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;
import androidx.test.core.app.ApplicationProvider;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class SharedPreferencesChangeReceiverTest {

  private SharedPreferencesChangeReceiver mUnderTest;
  private Context mContext;
  private SharedPreferences mSharedPreferences;

  @Before
  public void setUp() {
    mContext = ApplicationProvider.getApplicationContext();
    mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    mUnderTest = new SharedPreferencesChangeReceiver();
    // ensuring prefs are clean
    mSharedPreferences.edit().clear().commit();
  }

  @Test
  public void testHandlesBoolean() {
    Intent intent = new Intent(SharedPreferencesChangeReceiver.ACTION_CHANGE_PREF);
    intent.putExtra(SharedPreferencesChangeReceiver.EXTRA_PREF_KEY, "test_key");
    intent.putExtra(SharedPreferencesChangeReceiver.EXTRA_PREF_TYPE, PrefType.BOOLEAN);
    intent.putExtra(SharedPreferencesChangeReceiver.EXTRA_PREF_VALUE, true);

    mUnderTest.onReceive(mContext, intent);

    Assert.assertTrue(mSharedPreferences.getBoolean("test_key", false));
  }

  @Test
  public void testHandlesInt() {
    Intent intent = new Intent(SharedPreferencesChangeReceiver.ACTION_CHANGE_PREF);
    intent.putExtra(SharedPreferencesChangeReceiver.EXTRA_PREF_KEY, "test_key");
    intent.putExtra(SharedPreferencesChangeReceiver.EXTRA_PREF_TYPE, PrefType.INT);
    intent.putExtra(SharedPreferencesChangeReceiver.EXTRA_PREF_VALUE, 42);

    mUnderTest.onReceive(mContext, intent);

    Assert.assertEquals(42, mSharedPreferences.getInt("test_key", 0));
  }

  @Test
  public void testHandlesFloat() {
    Intent intent = new Intent(SharedPreferencesChangeReceiver.ACTION_CHANGE_PREF);
    intent.putExtra(SharedPreferencesChangeReceiver.EXTRA_PREF_KEY, "test_key");
    intent.putExtra(SharedPreferencesChangeReceiver.EXTRA_PREF_TYPE, PrefType.FLOAT);
    intent.putExtra(SharedPreferencesChangeReceiver.EXTRA_PREF_VALUE, 42.5f);

    mUnderTest.onReceive(mContext, intent);

    Assert.assertEquals(42.5f, mSharedPreferences.getFloat("test_key", 0f), 0.01f);
  }

  @Test
  public void testHandlesString() {
    Intent intent = new Intent(SharedPreferencesChangeReceiver.ACTION_CHANGE_PREF);
    intent.putExtra(SharedPreferencesChangeReceiver.EXTRA_PREF_KEY, "test_key");
    intent.putExtra(SharedPreferencesChangeReceiver.EXTRA_PREF_TYPE, PrefType.STRING);
    intent.putExtra(SharedPreferencesChangeReceiver.EXTRA_PREF_VALUE, "a string");

    mUnderTest.onReceive(mContext, intent);

    Assert.assertEquals("a string", mSharedPreferences.getString("test_key", null));
  }

  @Test
  public void testIgnoresNullIntent() {
    mUnderTest.onReceive(mContext, null);
    Assert.assertEquals(0, mSharedPreferences.getAll().size());
  }

  @Test
  public void testIgnoresIntentWithNoAction() {
    Intent intent = new Intent();
    mUnderTest.onReceive(mContext, intent);
    Assert.assertEquals(0, mSharedPreferences.getAll().size());
  }

  @Test
  public void testIgnoresIntentWithWrongAction() {
    Intent intent = new Intent("wrong.action");
    mUnderTest.onReceive(mContext, intent);
    Assert.assertEquals(0, mSharedPreferences.getAll().size());
  }

  @Test
  public void testIgnoresIntentWithNoKey() {
    Intent intent = new Intent(SharedPreferencesChangeReceiver.ACTION_CHANGE_PREF);
    intent.putExtra(SharedPreferencesChangeReceiver.EXTRA_PREF_TYPE, PrefType.BOOLEAN);
    intent.putExtra(SharedPreferencesChangeReceiver.EXTRA_PREF_VALUE, true);
    mUnderTest.onReceive(mContext, intent);
    Assert.assertEquals(0, mSharedPreferences.getAll().size());
  }

  @Test
  public void testIgnoresIntentWithNoType() {
    Intent intent = new Intent(SharedPreferencesChangeReceiver.ACTION_CHANGE_PREF);
    intent.putExtra(SharedPreferencesChangeReceiver.EXTRA_PREF_KEY, "test_key");
    intent.putExtra(SharedPreferencesChangeReceiver.EXTRA_PREF_VALUE, true);
    mUnderTest.onReceive(mContext, intent);
    Assert.assertEquals(0, mSharedPreferences.getAll().size());
  }

  @Test
  public void testIgnoresIntentWithNullString() {
    Intent intent = new Intent(SharedPreferencesChangeReceiver.ACTION_CHANGE_PREF);
    intent.putExtra(SharedPreferencesChangeReceiver.EXTRA_PREF_KEY, "test_key");
    intent.putExtra(SharedPreferencesChangeReceiver.EXTRA_PREF_TYPE, PrefType.STRING);
    intent.putExtra(SharedPreferencesChangeReceiver.EXTRA_PREF_VALUE, (String) null);
    mUnderTest.onReceive(mContext, intent);
    Assert.assertFalse(mSharedPreferences.contains("test_key"));
  }
}
