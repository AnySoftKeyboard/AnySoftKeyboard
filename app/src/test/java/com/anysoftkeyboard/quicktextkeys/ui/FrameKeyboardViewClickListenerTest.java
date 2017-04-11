package com.anysoftkeyboard.quicktextkeys.ui;

import android.content.Intent;
import android.view.View;

import com.anysoftkeyboard.AnySoftKeyboardTestRunner;
import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.keyboards.views.OnKeyboardActionListener;
import com.anysoftkeyboard.ui.settings.MainSettingsActivity;
import com.menny.android.anysoftkeyboard.R;

import net.evendanan.chauffeur.lib.FragmentChauffeurActivity;
import net.evendanan.chauffeur.lib.experiences.TransitionExperiences;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowApplication;

@RunWith(AnySoftKeyboardTestRunner.class)
public class FrameKeyboardViewClickListenerTest {

    @Test
    public void testOnClickClose() throws Exception {
        OnKeyboardActionListener keyboardActionListener = Mockito.mock(OnKeyboardActionListener.class);
        FrameKeyboardViewClickListener listener = new FrameKeyboardViewClickListener(keyboardActionListener);
        Mockito.verifyZeroInteractions(keyboardActionListener);
        View view = new View(RuntimeEnvironment.application);
        view.setId(R.id.quick_keys_popup_close);
        listener.onClick(view);
        Mockito.verify(keyboardActionListener).onKey(KeyCodes.CANCEL, null, 0, null, true);
        Mockito.verifyNoMoreInteractions(keyboardActionListener);
    }

    @Test
    public void testOnClickBackSpace() throws Exception {
        OnKeyboardActionListener keyboardActionListener = Mockito.mock(OnKeyboardActionListener.class);
        FrameKeyboardViewClickListener listener = new FrameKeyboardViewClickListener(keyboardActionListener);
        Mockito.verifyZeroInteractions(keyboardActionListener);
        View view = new View(RuntimeEnvironment.application);
        view.setId(R.id.quick_keys_popup_backspace);
        listener.onClick(view);
        Mockito.verify(keyboardActionListener).onKey(KeyCodes.DELETE, null, 0, null, true);
        Mockito.verifyNoMoreInteractions(keyboardActionListener);
    }

    @Test
    public void testOnClickSetting() throws Exception {
        OnKeyboardActionListener keyboardActionListener = Mockito.mock(OnKeyboardActionListener.class);
        FrameKeyboardViewClickListener listener = new FrameKeyboardViewClickListener(keyboardActionListener);
        Mockito.verifyZeroInteractions(keyboardActionListener);
        View view = new View(RuntimeEnvironment.application);
        view.setId(R.id.quick_keys_popup_quick_keys_settings);
        listener.onClick(view);
        Intent expectedIntent = FragmentChauffeurActivity.createStartActivityIntentForAddingFragmentToUi(
                RuntimeEnvironment.application, MainSettingsActivity.class, new QuickTextKeysBrowseFragment(),
                TransitionExperiences.ROOT_FRAGMENT_EXPERIENCE_TRANSITION);
        expectedIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

        Intent settingIntent = ShadowApplication.getInstance().getNextStartedActivity();
        Assert.assertEquals(expectedIntent.getComponent().flattenToString(), settingIntent.getComponent().flattenToString());
        Assert.assertEquals(expectedIntent.getFlags(), settingIntent.getFlags());
        //closes the keyboard
        Mockito.verify(keyboardActionListener).onKey(KeyCodes.CANCEL, null, 0, null, true);
        Mockito.verifyNoMoreInteractions(keyboardActionListener);
    }
}