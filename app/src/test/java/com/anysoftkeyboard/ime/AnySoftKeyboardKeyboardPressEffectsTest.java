package com.anysoftkeyboard.ime;

import android.media.AudioManager;

import com.anysoftkeyboard.AnySoftKeyboardBaseTest;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.ShadowAskAudioManager;
import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.test.SharedPrefsHelper;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowVibrator;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class AnySoftKeyboardKeyboardPressEffectsTest extends AnySoftKeyboardBaseTest {

    @Test
    public void testLoadAndUnloadSystemSounds() {
        ShadowAskAudioManager shadowAudioManager = (ShadowAskAudioManager) Shadows.shadowOf(mAnySoftKeyboardUnderTest.getAudioManager());
        Assert.assertEquals(RuntimeEnvironment.application.getResources().getBoolean(R.bool.settings_default_sound_on), shadowAudioManager.areSoundEffectsLoaded());

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_sound_on, true);
        Assert.assertTrue(shadowAudioManager.areSoundEffectsLoaded());
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_sound_on, false);
        Assert.assertFalse(shadowAudioManager.areSoundEffectsLoaded());
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_sound_on, true);
        Assert.assertTrue(shadowAudioManager.areSoundEffectsLoaded());

        mAnySoftKeyboardController.destroy();
        Assert.assertFalse(shadowAudioManager.areSoundEffectsLoaded());
    }

    @Test
    public void testPlaysSoundIfEnabled() {
        ShadowAskAudioManager shadowAudioManager = (ShadowAskAudioManager) Shadows.shadowOf(mAnySoftKeyboardUnderTest.getAudioManager());
        //consuming demo - if one took place at start up
        shadowAudioManager.getLastPlaySoundEffectType();
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_sound_on, true);
        //demo
        Assert.assertEquals(AudioManager.FX_KEYPRESS_SPACEBAR, shadowAudioManager.getLastPlaySoundEffectType());

        mAnySoftKeyboardUnderTest.onPress('j');
        Assert.assertEquals(AudioManager.FX_KEYPRESS_STANDARD, shadowAudioManager.getLastPlaySoundEffectType());

        mAnySoftKeyboardUnderTest.onPress(KeyCodes.SPACE);
        Assert.assertEquals(AudioManager.FX_KEYPRESS_SPACEBAR, shadowAudioManager.getLastPlaySoundEffectType());

        mAnySoftKeyboardUnderTest.onPress(KeyCodes.ENTER);
        Assert.assertEquals(AudioManager.FX_KEYPRESS_RETURN, shadowAudioManager.getLastPlaySoundEffectType());

        mAnySoftKeyboardUnderTest.onPress(KeyCodes.DELETE);
        Assert.assertEquals(AudioManager.FX_KEYPRESS_DELETE, shadowAudioManager.getLastPlaySoundEffectType());

        mAnySoftKeyboardUnderTest.onPress(KeyCodes.SHIFT);
        Assert.assertEquals(AudioManager.FX_KEY_CLICK, shadowAudioManager.getLastPlaySoundEffectType());
        mAnySoftKeyboardUnderTest.onPress(KeyCodes.SHIFT_LOCK);
        Assert.assertEquals(AudioManager.FX_KEY_CLICK, shadowAudioManager.getLastPlaySoundEffectType());
        mAnySoftKeyboardUnderTest.onPress(KeyCodes.CTRL);
        Assert.assertEquals(AudioManager.FX_KEY_CLICK, shadowAudioManager.getLastPlaySoundEffectType());
        mAnySoftKeyboardUnderTest.onPress(KeyCodes.CTRL_LOCK);
        Assert.assertEquals(AudioManager.FX_KEY_CLICK, shadowAudioManager.getLastPlaySoundEffectType());
        mAnySoftKeyboardUnderTest.onPress(KeyCodes.ALT);
        Assert.assertEquals(AudioManager.FX_KEY_CLICK, shadowAudioManager.getLastPlaySoundEffectType());
        mAnySoftKeyboardUnderTest.onPress(KeyCodes.MODE_ALPHABET);
        Assert.assertEquals(AudioManager.FX_KEY_CLICK, shadowAudioManager.getLastPlaySoundEffectType());
        mAnySoftKeyboardUnderTest.onPress(KeyCodes.MODE_SYMOBLS);
        Assert.assertEquals(AudioManager.FX_KEY_CLICK, shadowAudioManager.getLastPlaySoundEffectType());
        mAnySoftKeyboardUnderTest.onPress(KeyCodes.KEYBOARD_CYCLE_INSIDE_MODE);
        Assert.assertEquals(AudioManager.FX_KEY_CLICK, shadowAudioManager.getLastPlaySoundEffectType());
        mAnySoftKeyboardUnderTest.onPress(KeyCodes.KEYBOARD_MODE_CHANGE);
        Assert.assertEquals(AudioManager.FX_KEY_CLICK, shadowAudioManager.getLastPlaySoundEffectType());

        mAnySoftKeyboardUnderTest.onPress(0);
        Assert.assertEquals(Integer.MIN_VALUE, shadowAudioManager.getLastPlaySoundEffectType());
    }

    @Test
    public void testDoesNotPlaysSoundIfDisabled() {
        ShadowAskAudioManager shadowAudioManager = (ShadowAskAudioManager) Shadows.shadowOf(mAnySoftKeyboardUnderTest.getAudioManager());
        //consuming demo - if one took place at start up
        shadowAudioManager.getLastPlaySoundEffectType();

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_sound_on, false);
        //no demo here
        Assert.assertEquals(Integer.MIN_VALUE, shadowAudioManager.getLastPlaySoundEffectType());

        mAnySoftKeyboardUnderTest.onPress(KeyCodes.SPACE);
        Assert.assertEquals(Integer.MIN_VALUE, shadowAudioManager.getLastPlaySoundEffectType());

        mAnySoftKeyboardUnderTest.onPress('j');
        Assert.assertEquals(Integer.MIN_VALUE, shadowAudioManager.getLastPlaySoundEffectType());

        mAnySoftKeyboardUnderTest.onPress(KeyCodes.ENTER);
        Assert.assertEquals(Integer.MIN_VALUE, shadowAudioManager.getLastPlaySoundEffectType());

        mAnySoftKeyboardUnderTest.onPress(KeyCodes.DELETE);
        Assert.assertEquals(Integer.MIN_VALUE, shadowAudioManager.getLastPlaySoundEffectType());

        mAnySoftKeyboardUnderTest.onPress(0);
        Assert.assertEquals(Integer.MIN_VALUE, shadowAudioManager.getLastPlaySoundEffectType());

        mAnySoftKeyboardUnderTest.onPress(KeyCodes.SHIFT);
        Assert.assertEquals(Integer.MIN_VALUE, shadowAudioManager.getLastPlaySoundEffectType());
    }

    @Test
    public void testDoesNotVibrateDisabled() {
        Robolectric.flushForegroundThreadScheduler();
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_vibrate_on_key_press_duration, "0");
        ShadowVibrator shadowVibrator = Shadows.shadowOf(mAnySoftKeyboardUnderTest.getVibrator());

        mAnySoftKeyboardUnderTest.onPress(0);
        Assert.assertFalse(shadowVibrator.isVibrating());

        mAnySoftKeyboardUnderTest.onPress(KeyCodes.SPACE);
        Assert.assertFalse(shadowVibrator.isVibrating());
    }

    @Test
    public void testVibrateWhenEnabled() {
        Robolectric.flushForegroundThreadScheduler();
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_vibrate_on_key_press_duration, "10");
        ShadowVibrator shadowVibrator = Shadows.shadowOf(mAnySoftKeyboardUnderTest.getVibrator());
        //demo
        Assert.assertTrue(shadowVibrator.isVibrating());
        Assert.assertEquals(10, shadowVibrator.getMilliseconds());
        Robolectric.flushForegroundThreadScheduler();
        Assert.assertFalse(shadowVibrator.isVibrating());

        mAnySoftKeyboardUnderTest.onPress(0);
        Assert.assertFalse(shadowVibrator.isVibrating());

        mAnySoftKeyboardUnderTest.onPress(KeyCodes.SPACE);
        Assert.assertTrue(shadowVibrator.isVibrating());
        Assert.assertEquals(10, shadowVibrator.getMilliseconds());
    }

    @Test
    public void testDoesNotLongPressVibrateDisabled() {
        final Keyboard.Key key = Mockito.mock(Keyboard.Key.class);

        Robolectric.flushForegroundThreadScheduler();
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_vibrate_on_long_press, false);
        Robolectric.flushForegroundThreadScheduler();
        ShadowVibrator shadowVibrator = Shadows.shadowOf(mAnySoftKeyboardUnderTest.getVibrator());

        Mockito.doReturn(0).when(key).getPrimaryCode();
        mAnySoftKeyboardUnderTest.onLongPressDone(key);
        Assert.assertFalse(shadowVibrator.isVibrating());

        Mockito.doReturn(KeyCodes.SPACE).when(key).getPrimaryCode();
        mAnySoftKeyboardUnderTest.onLongPressDone(key);
        Assert.assertFalse(shadowVibrator.isVibrating());
    }

    @Test
    public void testVibrateLongPressWhenEnabled() {
        final Keyboard.Key key = Mockito.mock(Keyboard.Key.class);

        Robolectric.flushForegroundThreadScheduler();
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_vibrate_on_long_press, true);
        Robolectric.flushForegroundThreadScheduler();
        ShadowVibrator shadowVibrator = Shadows.shadowOf(mAnySoftKeyboardUnderTest.getVibrator());

        Mockito.doReturn(0).when(key).getPrimaryCode();
        mAnySoftKeyboardUnderTest.onLongPressDone(key);
        Assert.assertFalse(shadowVibrator.isVibrating());

        Mockito.doReturn(KeyCodes.SPACE).when(key).getPrimaryCode();
        mAnySoftKeyboardUnderTest.onLongPressDone(key);
        Assert.assertTrue(shadowVibrator.isVibrating());
    }
}