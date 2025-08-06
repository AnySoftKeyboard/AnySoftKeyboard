package com.anysoftkeyboard.prefs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SharedPreferencesChangeReceiver extends BroadcastReceiver {

  public static final String ACTION_CHANGE_PREF = "com.anysoftkeyboard.ACTION_CHANGE_PREF";

  public static final String EXTRA_PREF_KEY = "com.anysoftkeyboard.EXTRA_PREF_KEY";

  public static final String EXTRA_PREF_VALUE = "com.anysoftkeyboard.EXTRA_PREF_VALUE";

  public static final String EXTRA_PREF_TYPE = "com.anysoftkeyboard.EXTRA_PREF_TYPE";

  @Override
  public void onReceive(Context context, Intent intent) {
    if (intent == null || !ACTION_CHANGE_PREF.equals(intent.getAction())) {
      return;
    }

    final String key = intent.getStringExtra(EXTRA_PREF_KEY);
    final PrefType type = (PrefType) intent.getSerializableExtra(EXTRA_PREF_TYPE);

    if (key == null || type == null) {
      return;
    }

    SharedPreferences prefs =
        PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    final SharedPreferences.Editor editor = prefs.edit();

    switch (type) {
      case BOOLEAN:
        editor.putBoolean(key, intent.getBooleanExtra(EXTRA_PREF_VALUE, false));
        break;
      case INT:
        editor.putInt(key, intent.getIntExtra(EXTRA_PREF_VALUE, 0));
        break;
      case FLOAT:
        editor.putFloat(key, intent.getFloatExtra(EXTRA_PREF_VALUE, 0f));
        break;
      case STRING:
        String stringValue = intent.getStringExtra(EXTRA_PREF_VALUE);
        if (stringValue != null) {
          editor.putString(key, stringValue);
        }
        break;
    }

    editor.apply();
  }
}
