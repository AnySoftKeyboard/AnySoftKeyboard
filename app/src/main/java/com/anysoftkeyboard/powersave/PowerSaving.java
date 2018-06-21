package com.anysoftkeyboard.powersave;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.PowerManager;
import android.support.annotation.BoolRes;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.annotation.StringRes;

import com.anysoftkeyboard.prefs.RxSharedPrefs;
import com.github.karczews.rxbroadcastreceiver.RxBroadcastReceivers;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

import io.reactivex.Observable;
import io.reactivex.annotations.CheckReturnValue;

public class PowerSaving {

    @CheckReturnValue
    @NonNull
    public static Observable<Boolean> observePowerSavingState(@NonNull Context context, @StringRes int enablePrefResId, @BoolRes int defaultValueResId) {
        final RxSharedPrefs prefs = AnyApplication.prefs(context);
        return Observable.combineLatest(
                prefs.getString(R.string.settings_key_power_save_mode, R.string.settings_default_power_save_mode_value).asObservable(),
                enablePrefResId == 0? Observable.just(true) : prefs.getBoolean(enablePrefResId, defaultValueResId).asObservable(),
                RxBroadcastReceivers.fromIntentFilter(context.getApplicationContext(), getBatteryStateIntentFilter()).startWith(new Intent(Intent.ACTION_BATTERY_OKAY)),
                getOsPowerSavingStateObservable(context),
                (powerSavingPref, enabledPref, batteryIntent, osPowerSavingState) -> {
                    if (!enabledPref) return false;

                    switch (powerSavingPref) {
                        case "never":
                            return false;
                        case "always":
                            return true;
                        default:
                            return osPowerSavingState || Intent.ACTION_BATTERY_LOW.equals(batteryIntent.getAction());
                    }
                })
                .distinctUntilChanged();
    }

    @CheckReturnValue
    @NonNull
    public static Observable<Boolean> observePowerSavingState(@NonNull Context context, @StringRes int enablePrefResId) {
        return observePowerSavingState(context, enablePrefResId, R.bool.settings_default_true);
    }

    private static Observable<Boolean> getOsPowerSavingStateObservable(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final PowerManager powerManager = (PowerManager) context.getSystemService(Service.POWER_SERVICE);
            return RxBroadcastReceivers.fromIntentFilter(context, getPowerSavingIntentFilter())
                    .map(i -> powerManager.isPowerSaveMode())
                    .startWith(false);
        } else {
            return Observable.just(false);
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private static IntentFilter getPowerSavingIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED);

        return filter;
    }

    private static IntentFilter getBatteryStateIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_LOW);
        filter.addAction(Intent.ACTION_BATTERY_OKAY);

        return filter;
    }
}
