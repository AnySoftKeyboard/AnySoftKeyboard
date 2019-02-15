package com.anysoftkeyboard.android;

import android.content.Context;
import android.support.annotation.BoolRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import com.anysoftkeyboard.prefs.RxSharedPrefs;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

import io.reactivex.Observable;
import io.reactivex.annotations.CheckReturnValue;

public class NightMode {

    @CheckReturnValue
    @NonNull
    public static Observable<Boolean> observeNightModeState(@NonNull Context context, @StringRes int enablePrefResId, @BoolRes int defaultValueResId) {
        final Observable<Boolean> nightMode = ((AnyApplication) context.getApplicationContext()).getNightModeObservable();
        final RxSharedPrefs prefs = AnyApplication.prefs(context);
        return Observable.combineLatest(
                prefs.getString(R.string.settings_key_night_mode, R.string.settings_default_night_mode_value).asObservable(),
                enablePrefResId == 0 ? Observable.just(true) : prefs.getBoolean(enablePrefResId, defaultValueResId).asObservable(),
                nightMode,
                (nightModePref, enabledPref, nightModeState) -> {
                    if (!enabledPref) return false;

                    switch (nightModePref) {
                        case "never":
                            return false;
                        case "always":
                            return true;
                        default:
                            return nightModeState;
                    }
                })
                .distinctUntilChanged();
    }
}
