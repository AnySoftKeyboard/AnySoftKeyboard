package com.anysoftkeyboard;

import android.media.AudioManager;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowAudioManager;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(AudioManager.class)
public class ShadowAskAudioManager extends ShadowAudioManager {
    private boolean mAreSoundEffectsLoaded;

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
    public void  playSoundEffect(int effectType, float volume) {
    }
}
