package com.anysoftkeyboard.ime;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Vibrator;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;

import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.prefs.RxSharedPrefs;
import com.menny.android.anysoftkeyboard.R;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public abstract class AnySoftKeyboardSoundEffects extends AnySoftKeyboardClipboard {

    private final BroadcastReceiver mSoundPreferencesChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateRingerMode();
        }
    };

    private AudioManager mAudioManager;
    private KeyboardSoundEffects mKeyboardSoundEffects;

    private KeyboardVibrator mKeyboardVibrator;

    @Override
    public void onCreate() {
        super.onCreate();

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mKeyboardSoundEffects = new KeyboardSoundEffects(mAudioManager);

        updateRingerMode();
        // register to receive ringer mode changes for silent mode
        registerReceiver(mSoundPreferencesChangedReceiver, new IntentFilter(AudioManager.RINGER_MODE_CHANGED_ACTION));

        mKeyboardVibrator = new KeyboardVibrator(getApplicationContext());
        addDisposable(mKeyboardVibrator.start(prefs()));
        addDisposable(mKeyboardSoundEffects.start(prefs()));
    }

    @Override
    public void onPress(int primaryCode) {
        super.onPress(primaryCode);

        mKeyboardVibrator.performKeySound(primaryCode);
        mKeyboardSoundEffects.performKeySound(primaryCode);
    }

    private void updateRingerMode() {
        mKeyboardSoundEffects.setSilentMode((mAudioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mSoundPreferencesChangedReceiver);
        mAudioManager.unloadSoundEffects();
    }

    public static class KeyboardVibrator {
        private final Vibrator mVibrator;
        private int mVibrationDuration;

        public KeyboardVibrator(Context applicationContext) {
            mVibrator = ((Vibrator) applicationContext.getSystemService(Context.VIBRATOR_SERVICE));
        }

        public Disposable start(RxSharedPrefs prefs) {
            return prefs.getString(R.string.settings_key_vibrate_on_key_press_duration, R.string.settings_default_vibrate_on_key_press_duration)
                    .asObservable().map(Integer::parseInt)
                    .subscribe(value -> {
                        mVibrationDuration = value;
                        //demo
                        performKeySound(KeyCodes.SPACE);
                    }, t -> Logger.w(TAG, t, "Failed to get vibrate duration"));
        }

        public void performKeySound(int primaryCode) {
            if (mVibrationDuration > 0 && primaryCode != 0 && mVibrator != null) {
                try {
                    mVibrator.vibrate(mVibrationDuration);
                } catch (Exception e) {
                    Logger.w(TAG, "Failed to interact with vibrator! Disabling for now.");
                    mVibrationDuration = 0;
                }
            }
        }
    }

    public static class KeyboardSoundEffects {
        @NonNull
        private final AudioManager mAudioManager;
        private boolean mSilentMode;
        private boolean mSoundOn;
        //-1 means not to use custom volume
        private float mCustomSoundVolume;

        public KeyboardSoundEffects(@NonNull AudioManager audioManager) {
            mAudioManager = audioManager;
        }

        @CheckResult
        public Disposable start(RxSharedPrefs prefs) {
            return new CompositeDisposable(
                    prefs.getBoolean(R.string.settings_key_sound_on, R.bool.settings_default_sound_on)
                            .asObservable().subscribe(value -> {
                        mSoundOn = value;
                        if (mSoundOn) {
                            mAudioManager.loadSoundEffects();
                            //demo
                            performKeySound(KeyCodes.SPACE);
                        } else {
                            mAudioManager.unloadSoundEffects();
                        }
                    }, t -> Logger.w(TAG, t, "Failed to interact with AudioManager!")),
                    Observable.combineLatest(
                            prefs.getBoolean(R.string.settings_key_use_custom_sound_volume, R.bool.settings_default_false).asObservable(),
                            prefs.getInteger(R.string.settings_key_custom_sound_volume, R.integer.settings_default_zero_value).asObservable(),
                            (useCustomVolume, customVolumeLevel) -> {
                                if (useCustomVolume) {
                                    return customVolumeLevel / 100f;
                                } else {
                                    return -1.0f;
                                }
                            }).subscribe(
                            customVolume -> {
                                mCustomSoundVolume = customVolume;
                                //demo
                                performKeySound(KeyCodes.SPACE);
                            },
                            t -> Logger.w(TAG, t, "Failed to read custom volume prefs"))

            );
        }

        public void setSilentMode(boolean silentMode) {
            mSilentMode = silentMode;
        }

        public void performKeySound(int primaryCode) {
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
    }
}
