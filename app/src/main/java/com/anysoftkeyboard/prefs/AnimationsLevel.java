package com.anysoftkeyboard.prefs;

import android.content.Context;

import com.anysoftkeyboard.android.PowerSaving;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

import io.reactivex.Observable;

public enum AnimationsLevel {
    Full,
    Some,
    None;

    public static Observable<AnimationsLevel> createPrefsObservable(Context appContext) {
        return Observable.combineLatest(
                PowerSaving.observePowerSavingState(appContext, R.string.settings_key_power_save_mode_animation_control),
                AnyApplication.prefs(appContext).getString(R.string.settings_key_tweak_animations_level, R.string.settings_default_tweak_animations_level).asObservable()
                        .map(value -> {
                            switch (value) {
                                case "none":
                                    return AnimationsLevel.None;
                                case "some":
                                    return AnimationsLevel.Some;
                                default:
                                    return AnimationsLevel.Full;
                            }
                        }),
                (powerSavingState, animationLevel) -> {
                    if (powerSavingState) {
                        return AnimationsLevel.None;
                    } else {
                        return animationLevel;
                    }
                });
    }
}
