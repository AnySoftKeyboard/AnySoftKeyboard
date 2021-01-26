package com.anysoftkeyboard.ime;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.anysoftkeyboard.TestableAnySoftKeyboard.createEditorInfo;
import static com.anysoftkeyboard.android.NightModeTest.configurationForNightMode;

import android.content.res.Configuration;
import android.media.AudioManager;
import android.view.inputmethod.EditorInfo;
import com.anysoftkeyboard.AnySoftKeyboardBaseTest;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.ShadowAskAudioManager;
import com.anysoftkeyboard.TestableAnySoftKeyboard;
import com.anysoftkeyboard.android.PowerSavingTest;
import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.views.preview.AboveKeyPositionCalculator;
import com.anysoftkeyboard.keyboards.views.preview.AboveKeyboardPositionCalculator;
import com.anysoftkeyboard.keyboards.views.preview.KeyPreviewsController;
import com.anysoftkeyboard.keyboards.views.preview.KeyPreviewsManager;
import com.anysoftkeyboard.keyboards.views.preview.NullKeyPreviewsManager;
import com.anysoftkeyboard.test.SharedPrefsHelper;
import com.anysoftkeyboard.theme.KeyboardThemeFactory;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowVibrator;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class AnySoftKeyboardPressEffectsTest extends AnySoftKeyboardBaseTest {

    @Override
    protected Class<? extends TestableAnySoftKeyboard> getServiceClass() {
        return TestableAnySoftKeyboardPressEffects.class;
    }

    @Test
    public void testLoadAndUnloadSystemSounds() {
        ShadowAskAudioManager shadowAudioManager =
                (ShadowAskAudioManager)
                        Shadows.shadowOf(mAnySoftKeyboardUnderTest.getAudioManager());
        Assert.assertEquals(
                getApplicationContext().getResources().getBoolean(R.bool.settings_default_sound_on),
                shadowAudioManager.areSoundEffectsLoaded());

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
        ShadowAskAudioManager shadowAudioManager =
                (ShadowAskAudioManager)
                        Shadows.shadowOf(mAnySoftKeyboardUnderTest.getAudioManager());
        // consuming demo - if one took place at start up
        shadowAudioManager.getLastPlaySoundEffectType();
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_sound_on, true);
        // demo
        Assert.assertEquals(
                AudioManager.FX_KEYPRESS_SPACEBAR, shadowAudioManager.getLastPlaySoundEffectType());

        mAnySoftKeyboardUnderTest.onPress('j');
        Assert.assertEquals(
                AudioManager.FX_KEYPRESS_STANDARD, shadowAudioManager.getLastPlaySoundEffectType());

        mAnySoftKeyboardUnderTest.onPress(KeyCodes.SPACE);
        Assert.assertEquals(
                AudioManager.FX_KEYPRESS_SPACEBAR, shadowAudioManager.getLastPlaySoundEffectType());

        mAnySoftKeyboardUnderTest.onPress(KeyCodes.ENTER);
        Assert.assertEquals(
                AudioManager.FX_KEYPRESS_RETURN, shadowAudioManager.getLastPlaySoundEffectType());

        mAnySoftKeyboardUnderTest.onPress(KeyCodes.DELETE);
        Assert.assertEquals(
                AudioManager.FX_KEYPRESS_DELETE, shadowAudioManager.getLastPlaySoundEffectType());

        mAnySoftKeyboardUnderTest.onPress(KeyCodes.SHIFT);
        Assert.assertEquals(
                AudioManager.FX_KEY_CLICK, shadowAudioManager.getLastPlaySoundEffectType());
        mAnySoftKeyboardUnderTest.onPress(KeyCodes.SHIFT_LOCK);
        Assert.assertEquals(
                AudioManager.FX_KEY_CLICK, shadowAudioManager.getLastPlaySoundEffectType());
        mAnySoftKeyboardUnderTest.onPress(KeyCodes.CTRL);
        Assert.assertEquals(
                AudioManager.FX_KEY_CLICK, shadowAudioManager.getLastPlaySoundEffectType());
        mAnySoftKeyboardUnderTest.onPress(KeyCodes.CTRL_LOCK);
        Assert.assertEquals(
                AudioManager.FX_KEY_CLICK, shadowAudioManager.getLastPlaySoundEffectType());
        mAnySoftKeyboardUnderTest.onPress(KeyCodes.ALT);
        Assert.assertEquals(
                AudioManager.FX_KEY_CLICK, shadowAudioManager.getLastPlaySoundEffectType());
        mAnySoftKeyboardUnderTest.onPress(KeyCodes.MODE_ALPHABET);
        Assert.assertEquals(
                AudioManager.FX_KEY_CLICK, shadowAudioManager.getLastPlaySoundEffectType());
        mAnySoftKeyboardUnderTest.onPress(KeyCodes.MODE_SYMOBLS);
        Assert.assertEquals(
                AudioManager.FX_KEY_CLICK, shadowAudioManager.getLastPlaySoundEffectType());
        mAnySoftKeyboardUnderTest.onPress(KeyCodes.KEYBOARD_CYCLE_INSIDE_MODE);
        Assert.assertEquals(
                AudioManager.FX_KEY_CLICK, shadowAudioManager.getLastPlaySoundEffectType());
        mAnySoftKeyboardUnderTest.onPress(KeyCodes.KEYBOARD_MODE_CHANGE);
        Assert.assertEquals(
                AudioManager.FX_KEY_CLICK, shadowAudioManager.getLastPlaySoundEffectType());

        mAnySoftKeyboardUnderTest.onPress(0);
        Assert.assertEquals(Integer.MIN_VALUE, shadowAudioManager.getLastPlaySoundEffectType());
    }

    @Test
    public void testDoNotPlaysSoundWhenLowPower() {
        ShadowAskAudioManager shadowAudioManager =
                (ShadowAskAudioManager)
                        Shadows.shadowOf(mAnySoftKeyboardUnderTest.getAudioManager());
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_sound_on, true);
        shadowAudioManager.getLastPlaySoundEffectType();

        mAnySoftKeyboardUnderTest.onPress(KeyCodes.SPACE);
        Assert.assertEquals(
                AudioManager.FX_KEYPRESS_SPACEBAR, shadowAudioManager.getLastPlaySoundEffectType());

        PowerSavingTest.sendBatteryState(true);

        mAnySoftKeyboardUnderTest.onPress(KeyCodes.SPACE);
        Assert.assertEquals(Integer.MIN_VALUE, shadowAudioManager.getLastPlaySoundEffectType());

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_power_save_mode_sound_control, false);

        mAnySoftKeyboardUnderTest.onPress(KeyCodes.SPACE);
        Assert.assertEquals(
                AudioManager.FX_KEYPRESS_SPACEBAR, shadowAudioManager.getLastPlaySoundEffectType());
    }

    @Test
    public void testDoNotPlaysSoundWhenNightTime() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_night_mode, "follow_system");
        ShadowAskAudioManager shadowAudioManager =
                (ShadowAskAudioManager)
                        Shadows.shadowOf(mAnySoftKeyboardUnderTest.getAudioManager());
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_sound_on, true);
        shadowAudioManager.getLastPlaySoundEffectType();

        mAnySoftKeyboardUnderTest.onPress(KeyCodes.SPACE);
        Assert.assertEquals(
                AudioManager.FX_KEYPRESS_SPACEBAR, shadowAudioManager.getLastPlaySoundEffectType());

        AnyApplication application = getApplicationContext();
        application.onConfigurationChanged(
                configurationForNightMode(Configuration.UI_MODE_NIGHT_YES));

        mAnySoftKeyboardUnderTest.onPress(KeyCodes.SPACE);
        Assert.assertEquals(Integer.MIN_VALUE, shadowAudioManager.getLastPlaySoundEffectType());

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_night_mode_sound_control, false);

        mAnySoftKeyboardUnderTest.onPress(KeyCodes.SPACE);
        Assert.assertEquals(
                AudioManager.FX_KEYPRESS_SPACEBAR, shadowAudioManager.getLastPlaySoundEffectType());
    }

    @Test
    public void testDoNotVibrateWhenNightTime() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_night_mode, "follow_system");
        Robolectric.flushForegroundThreadScheduler();
        SharedPrefsHelper.setPrefsValue(
                R.string.settings_key_vibrate_on_key_press_duration_int, 10);
        ShadowVibrator shadowVibrator = Shadows.shadowOf(mAnySoftKeyboardUnderTest.getVibrator());

        Robolectric.flushForegroundThreadScheduler();
        Assert.assertFalse(shadowVibrator.isVibrating());

        mAnySoftKeyboardUnderTest.onPress(KeyCodes.SPACE);
        Assert.assertTrue(shadowVibrator.isVibrating());

        Robolectric.flushForegroundThreadScheduler();

        AnyApplication application = getApplicationContext();
        application.onConfigurationChanged(
                configurationForNightMode(Configuration.UI_MODE_NIGHT_YES));

        mAnySoftKeyboardUnderTest.onPress(KeyCodes.SPACE);
        Assert.assertFalse(shadowVibrator.isVibrating());

        Robolectric.flushForegroundThreadScheduler();

        application.onConfigurationChanged(
                configurationForNightMode(Configuration.UI_MODE_NIGHT_NO));
        mAnySoftKeyboardUnderTest.onPress(KeyCodes.SPACE);
        Assert.assertTrue(shadowVibrator.isVibrating());

        Robolectric.flushForegroundThreadScheduler();

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_night_mode_vibration_control, false);
        application.onConfigurationChanged(
                configurationForNightMode(Configuration.UI_MODE_NIGHT_YES));

        Robolectric.flushForegroundThreadScheduler();

        mAnySoftKeyboardUnderTest.onPress(KeyCodes.SPACE);
        Assert.assertTrue(shadowVibrator.isVibrating());
    }

    @Test
    public void testDoesNotPlaysSoundIfDisabled() {
        ShadowAskAudioManager shadowAudioManager =
                (ShadowAskAudioManager)
                        Shadows.shadowOf(mAnySoftKeyboardUnderTest.getAudioManager());
        // consuming demo - if one took place at start up
        shadowAudioManager.getLastPlaySoundEffectType();

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_sound_on, false);
        // no demo here
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
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_vibrate_on_key_press_duration_int, 0);
        ShadowVibrator shadowVibrator = Shadows.shadowOf(mAnySoftKeyboardUnderTest.getVibrator());

        mAnySoftKeyboardUnderTest.onPress(0);
        Assert.assertFalse(shadowVibrator.isVibrating());

        mAnySoftKeyboardUnderTest.onPress(KeyCodes.SPACE);
        Assert.assertFalse(shadowVibrator.isVibrating());
    }

    @Test
    public void testVibrateWhenEnabled() {
        Robolectric.flushForegroundThreadScheduler();
        SharedPrefsHelper.setPrefsValue(
                R.string.settings_key_vibrate_on_key_press_duration_int, 10);
        ShadowVibrator shadowVibrator = Shadows.shadowOf(mAnySoftKeyboardUnderTest.getVibrator());
        // demo
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
    public void testDoNotVibrateWhenLowPower() {
        Robolectric.flushForegroundThreadScheduler();
        SharedPrefsHelper.setPrefsValue(
                R.string.settings_key_vibrate_on_key_press_duration_int, 10);
        ShadowVibrator shadowVibrator = Shadows.shadowOf(mAnySoftKeyboardUnderTest.getVibrator());

        Robolectric.flushForegroundThreadScheduler();
        Assert.assertFalse(shadowVibrator.isVibrating());

        mAnySoftKeyboardUnderTest.onPress(KeyCodes.SPACE);
        Assert.assertTrue(shadowVibrator.isVibrating());

        Robolectric.flushForegroundThreadScheduler();

        PowerSavingTest.sendBatteryState(true);

        mAnySoftKeyboardUnderTest.onPress(KeyCodes.SPACE);
        Assert.assertFalse(shadowVibrator.isVibrating());

        Robolectric.flushForegroundThreadScheduler();

        SharedPrefsHelper.setPrefsValue(
                R.string.settings_key_power_save_mode_vibration_control, false);

        mAnySoftKeyboardUnderTest.onPress(KeyCodes.SPACE);
        Assert.assertTrue(shadowVibrator.isVibrating());
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

    @Test
    public void testSetupActualKeyPreviewController() {
        simulateOnStartInputFlow();
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardView())
                .setKeyPreviewController(Mockito.isA(KeyPreviewsManager.class));
    }

    @Test
    public void testNewKeyPreviewControllerOnInputViewReCreate() {
        simulateOnStartInputFlow();
        final ArgumentCaptor<KeyPreviewsController> captor =
                ArgumentCaptor.forClass(KeyPreviewsController.class);
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardView())
                .setKeyPreviewController(captor.capture());
        final KeyPreviewsController firstInstance = captor.getValue();
        simulateFinishInputFlow();

        mAnySoftKeyboardUnderTest.onCreateInputView();
        simulateOnStartInputFlow();
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardView())
                .setKeyPreviewController(
                        Mockito.argThat(argument -> argument != null && argument != firstInstance));
    }

    @Test
    public void testUsesNoOpKeyPreviewWhenDisabledInSettings() {
        simulateFinishInputFlow();
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_key_press_shows_preview_popup, false);

        simulateOnStartInputFlow();
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardView())
                .setKeyPreviewController(Mockito.isA(NullKeyPreviewsManager.class));
    }

    @Test
    public void testUsesNoOpKeyPreviewWhenNoAnimations() {
        simulateFinishInputFlow();
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_tweak_animations_level, "none");
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardView())
                .setKeyPreviewController(Mockito.isA(NullKeyPreviewsManager.class));
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_tweak_animations_level, "some");
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardView(), Mockito.times(2))
                .setKeyPreviewController(Mockito.isA(KeyPreviewsManager.class));
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_tweak_animations_level, "full");
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardView(), Mockito.times(3))
                .setKeyPreviewController(Mockito.isA(KeyPreviewsManager.class));
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_tweak_animations_level, "none");
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardView(), Mockito.times(2))
                .setKeyPreviewController(Mockito.isA(NullKeyPreviewsManager.class));
    }

    @Test
    public void testUsesNoOpKeyPreviewWhenLowPower() {
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardView())
                .setKeyPreviewController(Mockito.isA(KeyPreviewsManager.class));
        PowerSavingTest.sendBatteryState(true);
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardView())
                .setKeyPreviewController(Mockito.isA(NullKeyPreviewsManager.class));
        PowerSavingTest.sendBatteryState(false);
        Mockito.verify(
                        mAnySoftKeyboardUnderTest.getSpiedKeyboardView(),
                        Mockito.times(2 /*the second time*/))
                .setKeyPreviewController(Mockito.isA(KeyPreviewsManager.class));
    }

    private KeyPreviewsController getLastKeyPreviewController() {
        return ((TestableAnySoftKeyboardPressEffects) mAnySoftKeyboardUnderTest).mLastController;
    }

    @Test
    public void testUsesCorrectPositionCalculatorForKeyPreview() {
        Assert.assertTrue(
                ((KeyPreviewsManager) getLastKeyPreviewController()).getPositionCalculator()
                        instanceof AboveKeyPositionCalculator);

        SharedPrefsHelper.setPrefsValue(
                R.string.settings_key_key_press_preview_popup_position, "above_keyboard");
        Assert.assertTrue(
                ((KeyPreviewsManager) getLastKeyPreviewController()).getPositionCalculator()
                        instanceof AboveKeyboardPositionCalculator);

        SharedPrefsHelper.setPrefsValue(
                R.string.settings_key_key_press_preview_popup_position, "above_key");
        Assert.assertTrue(
                ((KeyPreviewsManager) getLastKeyPreviewController()).getPositionCalculator()
                        instanceof AboveKeyPositionCalculator);
    }

    @Test
    public void testKeyPreviewControllerIsDestroyWhenNewOneCreated() {
        final KeyPreviewsManager first = (KeyPreviewsManager) getLastKeyPreviewController();
        Mockito.verify(first, Mockito.never()).destroy();

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_key_press_shows_preview_popup, false);
        final KeyPreviewsController second = getLastKeyPreviewController();
        Assert.assertNotSame(first, second);
        Mockito.verify(first).destroy();
    }

    @Test
    public void testKeyPreviewIsNoOpWhenPasswordField() {
        simulateFinishInputFlow();
        simulateOnStartInputFlow(
                false,
                createEditorInfo(
                        EditorInfo.IME_ACTION_NONE,
                        EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_PASSWORD));
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardView())
                .setKeyPreviewController(Mockito.isA(NullKeyPreviewsManager.class));

        simulateFinishInputFlow();
        simulateOnStartInputFlow(
                false, createEditorInfo(EditorInfo.IME_ACTION_NONE, EditorInfo.TYPE_CLASS_TEXT));
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardView(), Mockito.times(2))
                .setKeyPreviewController(Mockito.isA(KeyPreviewsManager.class));

        // does not re-create
        simulateFinishInputFlow();
        simulateOnStartInputFlow(
                false, createEditorInfo(EditorInfo.IME_ACTION_NONE, EditorInfo.TYPE_CLASS_TEXT));
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardView(), Mockito.times(2))
                .setKeyPreviewController(Mockito.isA(KeyPreviewsManager.class));

        simulateFinishInputFlow();
        simulateOnStartInputFlow(
                false,
                createEditorInfo(
                        EditorInfo.IME_ACTION_NONE,
                        EditorInfo.TYPE_CLASS_TEXT
                                | EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD));
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardView(), Mockito.times(2))
                .setKeyPreviewController(Mockito.isA(NullKeyPreviewsManager.class));

        // does not re-create
        simulateFinishInputFlow();
        simulateOnStartInputFlow(
                false,
                createEditorInfo(
                        EditorInfo.IME_ACTION_NONE,
                        EditorInfo.TYPE_CLASS_TEXT
                                | EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD));
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardView(), Mockito.times(2))
                .setKeyPreviewController(Mockito.isA(NullKeyPreviewsManager.class));

        simulateFinishInputFlow();
        simulateOnStartInputFlow(
                false, createEditorInfo(EditorInfo.IME_ACTION_NONE, EditorInfo.TYPE_CLASS_TEXT));
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardView(), Mockito.times(3))
                .setKeyPreviewController(Mockito.isA(KeyPreviewsManager.class));

        simulateFinishInputFlow();
        simulateOnStartInputFlow(
                false,
                createEditorInfo(
                        EditorInfo.IME_ACTION_NONE,
                        EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_WEB_PASSWORD));
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardView(), Mockito.times(3))
                .setKeyPreviewController(Mockito.isA(NullKeyPreviewsManager.class));

        simulateFinishInputFlow();
        simulateOnStartInputFlow(
                false, createEditorInfo(EditorInfo.IME_ACTION_NONE, EditorInfo.TYPE_CLASS_TEXT));
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardView(), Mockito.times(4))
                .setKeyPreviewController(Mockito.isA(KeyPreviewsManager.class));

        simulateFinishInputFlow();
        simulateOnStartInputFlow(
                false,
                createEditorInfo(
                        EditorInfo.IME_ACTION_NONE,
                        EditorInfo.TYPE_CLASS_NUMBER | EditorInfo.TYPE_NUMBER_VARIATION_PASSWORD));
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardView(), Mockito.times(4))
                .setKeyPreviewController(Mockito.isA(NullKeyPreviewsManager.class));

        simulateFinishInputFlow();
        simulateOnStartInputFlow(
                false, createEditorInfo(EditorInfo.IME_ACTION_NONE, EditorInfo.TYPE_CLASS_NUMBER));
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardView(), Mockito.times(5))
                .setKeyPreviewController(Mockito.isA(KeyPreviewsManager.class));
    }

    @Test
    public void testNewThemeReCreateController() {
        final KeyboardThemeFactory keyboardThemeFactory =
                AnyApplication.getKeyboardThemeFactory(getApplicationContext());
        final KeyPreviewsController first = getLastKeyPreviewController();
        keyboardThemeFactory.setAddOnEnabled("55d9797c-850c-40a8-9a5d-7467b55bd537", true);
        Assert.assertNotSame(first, getLastKeyPreviewController());
    }

    private static class TestableAnySoftKeyboardPressEffects extends TestableAnySoftKeyboard {
        KeyPreviewsController mLastController;

        @Override
        protected void onNewControllerOrInputView(
                KeyPreviewsController controller, InputViewBinder inputViewBinder) {
            mLastController = Mockito.spy(controller);
            super.onNewControllerOrInputView(mLastController, inputViewBinder);
        }
    }
}
