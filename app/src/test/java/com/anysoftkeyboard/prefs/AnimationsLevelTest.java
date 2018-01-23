package com.anysoftkeyboard.prefs;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.test.SharedPrefsHelper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RuntimeEnvironment;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class AnimationsLevelTest {

    @Test
    @SuppressWarnings("unchecked")
    public void testCreatePrefsObservable() throws Exception {
        final Consumer<AnimationsLevel> consumer = (Consumer<AnimationsLevel>) Mockito.mock(Consumer.class);
        final Disposable disposable = AnimationsLevel.createPrefsObservable(RuntimeEnvironment.application).subscribe(consumer);
        Mockito.verify(consumer).accept(AnimationsLevel.Some);
        Mockito.verifyNoMoreInteractions(consumer);

        Mockito.reset(consumer);
        SharedPrefsHelper.setPrefsValue(com.menny.android.anysoftkeyboard.R.string.settings_key_tweak_animations_level, "none");

        Mockito.verify(consumer).accept(AnimationsLevel.None);
        Mockito.verifyNoMoreInteractions(consumer);

        Mockito.reset(consumer);
        SharedPrefsHelper.setPrefsValue(com.menny.android.anysoftkeyboard.R.string.settings_key_tweak_animations_level, "full");

        Mockito.verify(consumer).accept(AnimationsLevel.Full);
        Mockito.verifyNoMoreInteractions(consumer);

        Mockito.reset(consumer);
        SharedPrefsHelper.setPrefsValue(com.menny.android.anysoftkeyboard.R.string.settings_key_tweak_animations_level, "ddd");

        Mockito.verify(consumer).accept(AnimationsLevel.Full);
        Mockito.verifyNoMoreInteractions(consumer);

        Mockito.reset(consumer);
        SharedPrefsHelper.setPrefsValue(com.menny.android.anysoftkeyboard.R.string.settings_key_tweak_animations_level, "some");

        Mockito.verify(consumer).accept(AnimationsLevel.Some);
        Mockito.verifyNoMoreInteractions(consumer);

        disposable.dispose();

        Mockito.reset(consumer);
        SharedPrefsHelper.setPrefsValue(com.menny.android.anysoftkeyboard.R.string.settings_key_tweak_animations_level, "full");
        Mockito.verifyZeroInteractions(consumer);
    }
}