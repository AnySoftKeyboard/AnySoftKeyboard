package com.anysoftkeyboard.powersave;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;

import com.github.karczews.rxbroadcastreceiver.RxBroadcastReceivers;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

import io.reactivex.Observable;

public class PowerSaving {
    @NonNull
    public static Observable<Boolean> observePowerSavingState(@NonNull Context context) {
        return Observable.combineLatest(
                AnyApplication.prefs(context).getString(R.string.settings_key_power_save_mode, R.string.settings_default_power_save_mode_value).asObservable(),
                RxBroadcastReceivers.fromIntentFilter(context.getApplicationContext(), getPowerSavingIntentFilter()),
                (powerSavingPref, batteryIntent) -> {
                    switch (powerSavingPref) {
                        case "never":
                            return false;
                        case "always":
                            return true;
                        default:
                            return Intent.ACTION_BATTERY_LOW.equals(batteryIntent.getAction());
                    }
                }).startWith(false);
    }


    private static IntentFilter getPowerSavingIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_LOW);
        filter.addAction(Intent.ACTION_BATTERY_OKAY);

        return filter;
    }
}
