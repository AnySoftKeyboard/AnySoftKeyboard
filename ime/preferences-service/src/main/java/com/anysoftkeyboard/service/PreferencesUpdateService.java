package com.anysoftkeyboard.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import com.anysoftkeyboard.prefs.PrefValueType;
import com.anysoftkeyboard.prefs.RxSharedPrefs;
import com.anysoftkeyboard.prefs.RxSharedPrefsProvider;
import com.f2prateek.rx.preferences2.Preference;
import java.util.HashSet;
import java.util.Set;

public class PreferencesUpdateService extends Service {

  public static final String ACTION_UPDATE_PREF = "com.anysoftkeyboard.service.ACTION_UPDATE_PREF";
  public static final String EXTRA_KEY = "extra_key";
  public static final String EXTRA_VALUE_TYPE = "extra_value_type";
  public static final String EXTRA_VALUE = "extra_value";

  private static final String TAG = "PreferencesUpdateService";

  @Override
  public IBinder onBind(Intent intent) {
    return null; // This is a started service, not a bound service
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    if (intent == null || !ACTION_UPDATE_PREF.equals(intent.getAction())) {
      Log.w(TAG, "Received invalid intent: " + intent);
      stopSelf(startId);
      return START_NOT_STICKY;
    }

    String key = intent.getStringExtra(EXTRA_KEY);
    PrefValueType valueType = (PrefValueType) intent.getSerializableExtra(EXTRA_VALUE_TYPE);
    Object value = intent.getSerializableExtra(EXTRA_VALUE);

    if (key == null || valueType == null || value == null) {
      Log.w(
          TAG, "Missing intent extras. Key: " + key + ", Type: " + valueType + ", Value: " + value);
      stopSelf(startId);
      return START_NOT_STICKY;
    }

    RxSharedPrefs rxSharedPrefs = RxSharedPrefsProvider.getInstance();

    try {
      switch (valueType) {
        case BOOLEAN:
          ((Preference<Boolean>) rxSharedPrefs.getBoolean(key, (Boolean) value))
              .set((Boolean) value);
          break;
        case INT:
          ((Preference<Integer>) rxSharedPrefs.getInteger(key, (Integer) value))
              .set((Integer) value);
          break;
        case LONG:
          ((Preference<Long>) rxSharedPrefs.getLong(key, (Long) value)).set((Long) value);
          break;
        case FLOAT:
          ((Preference<Float>) rxSharedPrefs.getFloat(key, (Float) value)).set((Float) value);
          break;
        case STRING:
          ((Preference<String>) rxSharedPrefs.getString(key, (String) value)).set((String) value);
          break;
        case STRING_SET:
          // RxPreferences does not have a direct set for String Set with default value
          // We need to get the preference first, then set it.
          Preference<Set<String>> stringSetPreference = rxSharedPrefs.getStringSet(key);
          stringSetPreference.set(new HashSet<>((Set<String>) value));
          break;
        default:
          Log.w(TAG, "Unknown PrefValueType: " + valueType);
          break;
      }
      Log.i(TAG, "Successfully updated preference: " + key + " to " + value);
    } catch (ClassCastException e) {
      Log.e(
          TAG,
          "Type mismatch for preference "
              + key
              + ". Expected "
              + valueType
              + ", got "
              + value.getClass().getSimpleName(),
          e);
    } catch (Exception e) {
      Log.e(TAG, "Error updating preference " + key, e);
    }

    stopSelf(startId);
    return START_NOT_STICKY;
  }
}
