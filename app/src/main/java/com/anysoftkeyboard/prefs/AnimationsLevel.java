package com.anysoftkeyboard.prefs;

import android.content.Context;

import com.menny.android.anysoftkeyboard.AnyApplication;

import io.reactivex.Observable;

public enum AnimationsLevel {
    Full,
    Some,
    None;

    public static Observable<AnimationsLevel> createPrefsObservable(Context appContext) {
        return AnyApplication.prefs(appContext).getString(com.menny.android.anysoftkeyboard.R.string.settings_key_tweak_animations_level, com.menny.android.anysoftkeyboard.R.string.settings_default_tweak_animations_level)
                .asObservable().map(value -> {
                    switch (value) {
                        case "none":
                            return AnimationsLevel.None;
                        case "some":
                            return AnimationsLevel.Some;
                        default:
                            return AnimationsLevel.Full;
                    }
                });
    }
}
