package com.anysoftkeyboard;

import android.media.AudioManager;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowAudioManager;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(AudioManager.class)
public class ShadowAskAudioManager extends ShadowAudioManager {
    private boolean mAreSoundEffectsLoaded;
    private int mEffectType = Integer.MIN_VALUE;
    private float mVolume = Float.MIN_VALUE;

    @Implementation
    public void loadSoundEffects() {
        mAreSoundEffectsLoaded = true;
    }

    @Implementation
    public void unloadSoundEffects() {
        mAreSoundEffectsLoaded = false;
    }

    public boolean areSoundEffectsLoaded() {
        return mAreSoundEffectsLoaded;
    }

    @Implementation
    public void playSoundEffect(int effectType, float volume) {
        mEffectType = effectType;
        mVolume = volume;
    }

    public float getLastPlaySoundEffectVolume() {
        final float volume = mVolume;
        mVolume = Float.MIN_VALUE;
        return volume;
    }

    public int getLastPlaySoundEffectType() {
        final int effectType = mEffectType;
        mEffectType = Integer.MIN_VALUE;
        return effectType;
    }
}
