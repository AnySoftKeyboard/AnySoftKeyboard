package com.anysoftkeyboard.ime;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Vibrator;

import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.base.utils.Logger;
import com.menny.android.anysoftkeyboard.R;

import io.reactivex.Observable;

public abstract class AnySoftKeyboardSoundEffects extends AnySoftKeyboardClipboard {

    private final BroadcastReceiver mSoundPreferencesChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateRingerMode();
        }
    };

    private AudioManager mAudioManager;
    private boolean mSilentMode;
    private boolean mSoundOn;
    //-1 means not to use custom volume
    private float mCustomSoundVolume;
    private Vibrator mVibrator;
    private int mVibrationDuration;

    @Override
    public void onCreate() {
        super.onCreate();

        mVibrator = ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE));
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        updateRingerMode();
        // register to receive ringer mode changes for silent mode
        registerReceiver(mSoundPreferencesChangedReceiver, new IntentFilter(AudioManager.RINGER_MODE_CHANGED_ACTION));

        addDisposable(prefs().getString(R.string.settings_key_vibrate_on_key_press_duration, R.string.settings_default_vibrate_on_key_press_duration)
                .asObservable().map(Integer::parseInt).subscribe(value -> mVibrationDuration = value));
        addDisposable(prefs().getBoolean(R.string.settings_key_sound_on, R.bool.settings_default_sound_on)
                .asObservable().subscribe(value -> {
                    mSoundOn = value;
                    if (mSoundOn) {
                        mAudioManager.loadSoundEffects();
                    } else {
                        mAudioManager.unloadSoundEffects();
                    }
                }, t -> Logger.w(TAG, t, "Failed to interact with AudioManager!")));
        addDisposable(Observable.combineLatest(
                prefs().getBoolean(R.string.settings_key_use_custom_sound_volume, R.bool.settings_default_false).asObservable(),
                prefs().getInteger(R.string.settings_key_custom_sound_volume, R.integer.settings_default_zero_value).asObservable(),
                (useCustomVolume, customVolumeLevel) -> {
                    if (useCustomVolume) {
                        return customVolumeLevel / 100f;
                    } else {
                        return -1.0f;
                    }
                }).subscribe(customVolume -> mCustomSoundVolume = customVolume));
    }

    @Override
    public void onPress(int primaryCode) {
        super.onPress(primaryCode);
        if (mVibrationDuration > 0 && primaryCode != 0 && mVibrator != null) {
            try {
                mVibrator.vibrate(mVibrationDuration);
            } catch (Exception e) {
                Logger.w(TAG, "Failed to interact with vibrator! Disabling for now.");
                mVibrationDuration = 0;
            }
        }

        if (mSoundOn && (!mSilentMode) && primaryCode != 0) {
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

    private void updateRingerMode() {
        mSilentMode = (mAudioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mSoundPreferencesChangedReceiver);
    }
}
