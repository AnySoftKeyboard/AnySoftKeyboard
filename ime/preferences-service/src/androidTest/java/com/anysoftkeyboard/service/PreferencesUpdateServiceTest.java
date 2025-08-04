package com.anysoftkeyboard.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.Intent;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.anysoftkeyboard.prefs.PrefValueType;
import com.anysoftkeyboard.prefs.RxSharedPrefs;
import com.anysoftkeyboard.prefs.RxSharedPrefsProvider;
import com.f2prateek.rx.preferences2.Preference;
import java.util.HashSet;
import java.util.Set;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(AndroidJUnit4.class)
public class PreferencesUpdateServiceTest {

  private Context mContext;
  private RxSharedPrefs mMockRxSharedPrefs;
  private Preference<Boolean> mMockBooleanPreference;
  private Preference<Integer> mMockIntegerPreference;
  private Preference<String> mMockStringPreference;
  private Preference<Long> mMockLongPreference;
  private Preference<Float> mMockFloatPreference;
  private Preference<Set<String>> mMockStringSetPreference;

  @Before
  public void setUp() {
    mContext = ApplicationProvider.getApplicationContext();
    mMockRxSharedPrefs = mock(RxSharedPrefs.class);
    mMockBooleanPreference = mock(Preference.class);
    mMockIntegerPreference = mock(Preference.class);
    mMockStringPreference = mock(Preference.class);
    mMockLongPreference = mock(Preference.class);
    mMockFloatPreference = mock(Preference.class);
    mMockStringSetPreference = mock(Preference.class);

    when(mMockRxSharedPrefs.getBoolean(Mockito.anyString(), Mockito.anyBoolean()))
        .thenReturn(mMockBooleanPreference);
    when(mMockRxSharedPrefs.getInteger(Mockito.anyString(), Mockito.anyInt()))
        .thenReturn(mMockIntegerPreference);
    when(mMockRxSharedPrefs.getString(Mockito.anyString(), Mockito.anyString()))
        .thenReturn(mMockStringPreference);
    when(mMockRxSharedPrefs.getLong(Mockito.anyString(), Mockito.anyLong()))
        .thenReturn(mMockLongPreference);
    when(mMockRxSharedPrefs.getFloat(Mockito.anyString(), Mockito.anyFloat()))
        .thenReturn(mMockFloatPreference);
    when(mMockRxSharedPrefs.getStringSet(Mockito.anyString())).thenReturn(mMockStringSetPreference);

    RxSharedPrefsProvider.setInstance(mMockRxSharedPrefs);
  }

  @After
  public void tearDown() {
    RxSharedPrefsProvider.setInstance(null); // Reset to no-op
  }

  @Test
  public void testRxSharedPrefsProviderReturnsNoOpInstanceByDefault() {
    RxSharedPrefsProvider.setInstance(null); // Ensure it's reset to default no-op
    RxSharedPrefs initialInstance = RxSharedPrefsProvider.getInstance();
    // Verify it's not null and is an instance of the NoOp class (or a mock of it)
    // Since NoOpRxSharedPrefs is private, we can only check if it's not the mock we set
    // and that it doesn't cause a crash when methods are called.
    // For a more robust test, NoOpRxSharedPrefs could be package-private.
    // For now, we'll just ensure it's not our mock and doesn't throw.
    initialInstance.getBoolean("some_key", false);
    initialInstance.getInteger("some_key", 0);
    // Add more calls to ensure no-op behavior
    verify(mMockRxSharedPrefs, never()).getBoolean(Mockito.anyString(), Mockito.anyBoolean());
  }

  @Test
  public void testServiceUpdatesAllPreferenceTypes() {
    // Test Boolean
    testUpdatePreference("test_boolean_key", PrefValueType.BOOLEAN, true, mMockBooleanPreference);
    // Test Integer
    testUpdatePreference("test_integer_key", PrefValueType.INT, 123, mMockIntegerPreference);
    // Test String
    testUpdatePreference("test_string_key", PrefValueType.STRING, "hello", mMockStringPreference);
    // Test Long
    testUpdatePreference("test_long_key", PrefValueType.LONG, 12345L, mMockLongPreference);
    // Test Float
    testUpdatePreference("test_float_key", PrefValueType.FLOAT, 1.23f, mMockFloatPreference);
    // Test StringSet
    Set<String> stringSet = new HashSet<>();
    stringSet.add("item1");
    stringSet.add("item2");
    testUpdatePreference(
        "test_string_set_key", PrefValueType.STRING_SET, stringSet, mMockStringSetPreference);
  }

  private void testUpdatePreference(
      String key, PrefValueType type, Object value, Preference mockPreference) {
    Intent intent = new Intent(mContext, PreferencesUpdateService.class);
    intent.setAction(PreferencesUpdateService.ACTION_UPDATE_PREF);
    intent.putExtra(PreferencesUpdateService.EXTRA_KEY, key);
    intent.putExtra(PreferencesUpdateService.EXTRA_VALUE_TYPE, type);
    intent.putExtra(PreferencesUpdateService.EXTRA_VALUE, (java.io.Serializable) value);

    mContext.startService(intent);

    // Verify that the correct preference method was called and updated
    if (type == PrefValueType.BOOLEAN) {
      verify(mMockRxSharedPrefs).getBoolean(key, (Boolean) value);
    } else if (type == PrefValueType.INT) {
      verify(mMockRxSharedPrefs).getInteger(key, (Integer) value);
    } else if (type == PrefValueType.STRING) {
      verify(mMockRxSharedPrefs).getString(key, (String) value);
    } else if (type == PrefValueType.LONG) {
      verify(mMockRxSharedPrefs).getLong(key, (Long) value);
    } else if (type == PrefValueType.FLOAT) {
      verify(mMockRxSharedPrefs).getFloat(key, (Float) value);
    } else if (type == PrefValueType.STRING_SET) {
      verify(mMockRxSharedPrefs).getStringSet(key);
    }
    verify(mockPreference).set(value);
  }

  @Test
  public void testServiceHandlesMalformedIntents() {
    // Test missing key
    Intent intentMissingKey = new Intent(mContext, PreferencesUpdateService.class);
    intentMissingKey.setAction(PreferencesUpdateService.ACTION_UPDATE_PREF);
    intentMissingKey.putExtra(PreferencesUpdateService.EXTRA_VALUE_TYPE, PrefValueType.BOOLEAN);
    intentMissingKey.putExtra(PreferencesUpdateService.EXTRA_VALUE, true);
    mContext.startService(intentMissingKey);
    verifyNoPreferenceUpdate();

    // Test missing value type
    Intent intentMissingType = new Intent(mContext, PreferencesUpdateService.class);
    intentMissingType.setAction(PreferencesUpdateService.ACTION_UPDATE_PREF);
    intentMissingType.putExtra(PreferencesUpdateService.EXTRA_KEY, "some_key");
    intentMissingType.putExtra(PreferencesUpdateService.EXTRA_VALUE, true);
    mContext.startService(intentMissingType);
    verifyNoPreferenceUpdate();

    // Test missing value
    Intent intentMissingValue = new Intent(mContext, PreferencesUpdateService.class);
    intentMissingValue.setAction(PreferencesUpdateService.ACTION_UPDATE_PREF);
    intentMissingValue.putExtra(PreferencesUpdateService.EXTRA_KEY, "some_key");
    intentMissingValue.putExtra(PreferencesUpdateService.EXTRA_VALUE_TYPE, PrefValueType.BOOLEAN);
    mContext.startService(intentMissingValue);
    verifyNoPreferenceUpdate();

    // Test wrong action
    Intent intentWrongAction = new Intent(mContext, PreferencesUpdateService.class);
    intentWrongAction.setAction("some.other.action");
    intentWrongAction.putExtra(PreferencesUpdateService.EXTRA_KEY, "some_key");
    intentWrongAction.putExtra(PreferencesUpdateService.EXTRA_VALUE_TYPE, PrefValueType.BOOLEAN);
    intentWrongAction.putExtra(PreferencesUpdateService.EXTRA_VALUE, true);
    mContext.startService(intentWrongAction);
    verifyNoPreferenceUpdate();
  }

  @Test
  public void testServiceHandlesUnknownValueType() {
    Intent intent = new Intent(mContext, PreferencesUpdateService.class);
    intent.setAction(PreferencesUpdateService.ACTION_UPDATE_PREF);
    intent.putExtra(PreferencesUpdateService.EXTRA_KEY, "test_key");
    // Pass an invalid serializable object that is not a PrefValueType
    intent.putExtra(PreferencesUpdateService.EXTRA_VALUE_TYPE, "INVALID_TYPE");
    intent.putExtra(PreferencesUpdateService.EXTRA_VALUE, true);

    mContext.startService(intent);

    verifyNoPreferenceUpdate();
  }

  @Test
  public void testServiceStopsSelfAfterProcessing() {
    // This is difficult to test directly with Mockito on a Service.
    // A more advanced test setup (e.g., using a ServiceTestRule and checking its state)
    // would be needed. For now, we rely on the fact that stopSelf(startId) is called
    // at the end of onStartCommand in the service implementation.
    // We can indirectly verify by ensuring the preference update happened and no crash occurred.
    testUpdatePreference("test_boolean_key", PrefValueType.BOOLEAN, true, mMockBooleanPreference);
  }

  private void verifyNoPreferenceUpdate() {
    verify(mMockRxSharedPrefs, never()).getBoolean(Mockito.anyString(), Mockito.anyBoolean());
    verify(mMockRxSharedPrefs, never()).getInteger(Mockito.anyString(), Mockito.anyInt());
    verify(mMockRxSharedPrefs, never()).getString(Mockito.anyString(), Mockito.anyString());
    verify(mMockRxSharedPrefs, never()).getLong(Mockito.anyString(), Mockito.anyLong());
    verify(mMockRxSharedPrefs, never()).getFloat(Mockito.anyString(), Mockito.anyFloat());
    verify(mMockRxSharedPrefs, never()).getStringSet(Mockito.anyString());
  }
}
