package com.anysoftkeyboard.prefs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.core.os.UserManagerCompat;
import androidx.core.util.Consumer;
import androidx.preference.PreferenceManager;
import com.anysoftkeyboard.base.utils.Logger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DirectBootAwareSharedPreferences implements SharedPreferences {

    @NonNull private final Context mContext;
    @NonNull private final SharedPreferencesFactory mSharedPreferencesFactory;
    @NonNull private final Consumer<SharedPreferences> mOnReadyListener;

    @NonNull private SharedPreferences mActual = new NoOpSharedPreferences();

    @VisibleForTesting
    DirectBootAwareSharedPreferences(
            @NonNull Context context,
            @NonNull Consumer<SharedPreferences> onReadyListener,
            @NonNull SharedPreferencesFactory sharedPreferencesFactory) {
        Logger.d("DirectBootAwareSharedPreferences", "Creating DirectBootAwareSharedPreferences");
        mContext = context;
        mOnReadyListener = onReadyListener;
        mSharedPreferencesFactory = sharedPreferencesFactory;
        obtainSharedPreferences();
    }

    @NonNull
    public static SharedPreferences create(@NonNull Context context) {
        return create(context, sp -> {} /*no op listener*/);
    }

    @NonNull
    public static SharedPreferences create(
            @NonNull Context context, @NonNull Consumer<SharedPreferences> onReadyListener) {
        // CHECKSTYLE:OFF
        return new DirectBootAwareSharedPreferences(
                context.getApplicationContext(),
                onReadyListener,
                PreferenceManager::getDefaultSharedPreferences);
        // CHECKSTYLE:ON
    }

    private void obtainSharedPreferences() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Logger.i("DirectBootAwareSharedPreferences", "obtainSharedPreferences: new device");
            if (UserManagerCompat.isUserUnlocked(mContext)) {
                final List<OnSharedPreferenceChangeListener> listeners;
                if (mActual instanceof NoOpSharedPreferences) {
                    listeners = ((NoOpSharedPreferences) mActual).mListeners;
                } else {
                    listeners = Collections.emptyList();
                }

                Logger.i(
                        "DirectBootAwareSharedPreferences",
                        "obtainSharedPreferences: trying to create a SharedPreferences");
                mActual = mSharedPreferencesFactory.create(mContext);
                Logger.i("DirectBootAwareSharedPreferences", "obtainSharedPreferences: Success!");
                for (OnSharedPreferenceChangeListener listener : listeners) {
                    mActual.registerOnSharedPreferenceChangeListener(listener);
                    // notify about changes
                    for (String key : mActual.getAll().keySet()) {
                        listener.onSharedPreferenceChanged(this, key);
                    }
                }
                mOnReadyListener.accept(this);
            } else {
                Logger.w(
                        "DirectBootAwareSharedPreferences",
                        "Device locked! Will fake Shared-Preferences");
                mActual = new NoOpSharedPreferences();
                Logger.i(
                        "DirectBootAwareSharedPreferences",
                        "obtainSharedPreferences: registerReceiver");
                mContext.registerReceiver(
                        new BroadcastReceiver() {
                            @Override
                            public void onReceive(Context context, Intent intent) {
                                Logger.i(
                                        "DirectBootAwareSharedPreferences",
                                        "mBootLockEndedReceiver: received '%s'",
                                        intent);
                                if (intent != null
                                        && Intent.ACTION_USER_UNLOCKED.equals(intent.getAction())) {
                                    context.unregisterReceiver(this);
                                    obtainSharedPreferences();
                                }
                            }
                        },
                        new IntentFilter(Intent.ACTION_USER_UNLOCKED));
            }
        } else {
            Logger.i("DirectBootAwareSharedPreferences", "obtainSharedPreferences: old device");
            mActual = mSharedPreferencesFactory.create(mContext);
            mOnReadyListener.accept(this);
        }
    }

    @Override
    public Map<String, ?> getAll() {
        return mActual.getAll();
    }

    @Nullable
    @Override
    public String getString(String key, @Nullable String defValue) {
        return mActual.getString(key, defValue);
    }

    @Nullable
    @Override
    public Set<String> getStringSet(String key, @Nullable Set<String> defValues) {
        return mActual.getStringSet(key, defValues);
    }

    @Override
    public int getInt(String key, int defValue) {
        return mActual.getInt(key, defValue);
    }

    @Override
    public long getLong(String key, long defValue) {
        return mActual.getLong(key, defValue);
    }

    @Override
    public float getFloat(String key, float defValue) {
        return mActual.getFloat(key, defValue);
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        return mActual.getBoolean(key, defValue);
    }

    @Override
    public boolean contains(String key) {
        return mActual.contains(key);
    }

    @Override
    public Editor edit() {
        return mActual.edit();
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(
            OnSharedPreferenceChangeListener listener) {
        mActual.registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(
            OnSharedPreferenceChangeListener listener) {
        mActual.unregisterOnSharedPreferenceChangeListener(listener);
    }

    interface SharedPreferencesFactory {
        @NonNull
        SharedPreferences create(@NonNull Context context);
    }

    private static class NoOpSharedPreferences implements SharedPreferences {
        private final List<OnSharedPreferenceChangeListener> mListeners = new ArrayList<>();

        @Override
        public Map<String, ?> getAll() {
            return Collections.emptyMap();
        }

        @Nullable
        @Override
        public String getString(String key, @Nullable String defValue) {
            return defValue;
        }

        @Nullable
        @Override
        public Set<String> getStringSet(String key, @Nullable Set<String> defValues) {
            return defValues;
        }

        @Override
        public int getInt(String key, int defValue) {
            return defValue;
        }

        @Override
        public long getLong(String key, long defValue) {
            return defValue;
        }

        @Override
        public float getFloat(String key, float defValue) {
            return defValue;
        }

        @Override
        public boolean getBoolean(String key, boolean defValue) {
            return defValue;
        }

        @Override
        public boolean contains(String key) {
            return false;
        }

        @Override
        public Editor edit() {
            return new NoOpEditor();
        }

        @Override
        public void registerOnSharedPreferenceChangeListener(
                OnSharedPreferenceChangeListener listener) {
            mListeners.add(listener);
        }

        @Override
        public void unregisterOnSharedPreferenceChangeListener(
                OnSharedPreferenceChangeListener listener) {
            mListeners.remove(listener);
        }

        private static class NoOpEditor implements Editor {
            @Override
            public Editor putString(String key, @Nullable String value) {
                return this;
            }

            @Override
            public Editor putStringSet(String key, @Nullable Set<String> values) {
                return this;
            }

            @Override
            public Editor putInt(String key, int value) {
                return this;
            }

            @Override
            public Editor putLong(String key, long value) {
                return this;
            }

            @Override
            public Editor putFloat(String key, float value) {
                return this;
            }

            @Override
            public Editor putBoolean(String key, boolean value) {
                return this;
            }

            @Override
            public Editor remove(String key) {
                return this;
            }

            @Override
            public Editor clear() {
                return this;
            }

            @Override
            public boolean commit() {
                return false;
            }

            @Override
            public void apply() {}
        }
    }
}
