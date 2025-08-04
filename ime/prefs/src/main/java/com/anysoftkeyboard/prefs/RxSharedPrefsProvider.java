package com.anysoftkeyboard.prefs;

import com.f2prateek.rx.preferences2.Preference;
import java.util.Collections;
import java.util.Set;

public class RxSharedPrefsProvider {

  private static RxSharedPrefs sInstance = new NoOpRxSharedPrefs();

  public static RxSharedPrefs getInstance() {
    return sInstance;
  }

  public static void setInstance(RxSharedPrefs instance) {
    sInstance = instance != null ? instance : new NoOpRxSharedPrefs();
  }

  private static class NoOpRxSharedPrefs extends RxSharedPrefs {
    public NoOpRxSharedPrefs() {
      super(null, null); // Context and Consumer are not used in no-op
    }

    @Override
    public Preference<Boolean> getBoolean(String prefKey, boolean defaultValue) {
      return new NoOpPreference<>(defaultValue);
    }

    @Override
    public Preference<Integer> getInteger(String prefKey, int defaultValue) {
      return new NoOpPreference<>(defaultValue);
    }

    @Override
    public Preference<String> getString(String prefKey, String defaultValue) {
      return new NoOpPreference<>(defaultValue);
    }

    @Override
    public Preference<Long> getLong(String prefKey, long defaultValue) {
      return new NoOpPreference<>(defaultValue);
    }

    @Override
    public Preference<Float> getFloat(String prefKey, float defaultValue) {
      return new NoOpPreference<>(defaultValue);
    }

    @Override
    public Preference<Set<String>> getStringSet(String prefKey) {
      return new NoOpPreference<>(Collections.emptySet());
    }

    private static class NoOpPreference<T> extends Preference<T> {
      private final T mDefaultValue;

      public NoOpPreference(T defaultValue) {
        super(null, null); // Key and defaultValue are not used in no-op
        mDefaultValue = defaultValue;
      }

      @Override
      public T get() {
        return mDefaultValue;
      }

      @Override
      public void set(T value) {
        // No-op
      }

      @Override
      public boolean isSet() {
        return false;
      }

      @Override
      public void delete() {
        // No-op
      }
    }
  }
}
