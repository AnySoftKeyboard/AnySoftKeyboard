package com.anysoftkeyboard.ime;

import android.content.Context;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Vibrator;

import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.base.utils.Logger;
import com.github.karczews.rxbroadcastreceiver.RxBroadcastReceivers;
import com.menny.android.anysoftkeyboard.R;

import io.reactivex.Observable;

public abstract class AnySoftKeyboardPressEffects extends AnySoftKeyboardClipboard {

    private AudioManager mAudioManager;
    private static final float SILENT = 0.0f;
    private static final float SYSTEM_VOLUME = -1.0f;
    private float mCustomSoundVolume = SYSTEM_VOLUME;

    private Vibrator mVibrator;
    private int mVibrationDuration;

    @Override
    public void onCreate() {
        super.onCreate();

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mVibrator = ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE));

        addDisposable(Observable.combineLatest(
                RxBroadcastReceivers.fromIntentFilter(getApplicationContext(), new IntentFilter(AudioManager.RINGER_MODE_CHANGED_ACTION)),
                prefs().getBoolean(R.string.settings_key_sound_on, R.bool.settings_default_sound_on).asObservable(),
                prefs().getBoolean(R.string.settings_key_use_custom_sound_volume, R.bool.settings_default_false).asObservable(),
                prefs().getInteger(R.string.settings_key_custom_sound_volume, R.integer.settings_default_zero_value).asObservable(),
                (soundIntent, soundOn, useCustomVolume, customVolumeLevel) -> {
                    if (mAudioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) return SILENT;
                    if (!soundOn) return SILENT;

                    if (useCustomVolume) {
                        return customVolumeLevel / 100f;
                    } else {
                        return SYSTEM_VOLUME;
                    }
                }).subscribe(customVolume -> {
                    mCustomSoundVolume = customVolume;
                    //demo
                    performKeySound(KeyCodes.SPACE);
                },
                t -> Logger.w(TAG, t, "Failed to read custom volume prefs")));

        addDisposable(prefs().getString(R.string.settings_key_vibrate_on_key_press_duration, R.string.settings_default_vibrate_on_key_press_duration)
                .asObservable().map(Integer::parseInt)
                .subscribe(value -> {
                    mVibrationDuration = value;
                    //demo
                    performKeyVibration(KeyCodes.SPACE);
                }, t -> Logger.w(TAG, t, "Failed to get vibrate duration")));
    }

    private void performKeySound(int primaryCode) {
        if (mCustomSoundVolume != SILENT && primaryCode != 0) {
            final int keyFX;
            switch (primaryCode) {
                case 13:
                case KeyCodes.ENTER:
                    keyFX = AudioManager.FX_KEYPRESS_RETURN;
                    break;
                case KeyCodes.DELETE:
                    keyFX = AudioManager.FX_KEYPRESS_DELETE;
                    break;
                case KeyCodes.SPACE:
                    keyFX = AudioManager.FX_KEYPRESS_SPACEBAR;
                    break;
                default:
                    keyFX = AudioManager.FX_KEY_CLICK;
            }
            mAudioManager.playSoundEffect(keyFX, mCustomSoundVolume);
        }
    }

    private void performKeyVibration(int primaryCode) {
        if (mVibrationDuration > 0 && primaryCode != 0) {
            try {
                mVibrator.vibrate(mVibrationDuration);
            } catch (Exception e) {
                Logger.w(TAG, "Failed to interact with vibrator! Disabling for now.");
                mVibrationDuration = 0;
            }
        }
    }

    @Override
    public void onPress(int primaryCode) {
        super.onPress(primaryCode);

        performKeySound(primaryCode);
        performKeyVibration(primaryCode);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAudioManager.unloadSoundEffects();
    }
}
