package com.anysoftkeyboard.ui.settings;

import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.LinearLayout;

import com.anysoftkeyboard.RobolectricFragmentTestCase;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Test;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;

public class LanguageSettingsFragmentTest extends RobolectricFragmentTestCase<LanguageSettingsFragment> {

    @NonNull
    @Override
    protected LanguageSettingsFragment createFragment() {
        return new LanguageSettingsFragment();
    }

    @Config(qualifiers = "land")
    @Test
    public void testLandscape() {
        RuntimeEnvironment.application.getResources().getConfiguration().orientation = Configuration.ORIENTATION_LANDSCAPE;
        final LanguageSettingsFragment languageSettingsFragment = startFragment();
        final LinearLayout rootView = (LinearLayout) languageSettingsFragment.getView();

        Assert.assertEquals(LinearLayout.HORIZONTAL, rootView.getOrientation());
        Assert.assertEquals(rootView.getChildCount(), rootView.getWeightSum(), 0f);
    }

    @Test
    public void testPortrait() {
        RuntimeEnvironment.application.getResources().getConfiguration().orientation = Configuration.ORIENTATION_PORTRAIT;
        final LanguageSettingsFragment languageSettingsFragment = startFragment();
        final LinearLayout rootView = (LinearLayout) languageSettingsFragment.getView();

        Assert.assertEquals(LinearLayout.VERTICAL, rootView.getOrientation());
    }

    @Test
    public void testNavigationKeyboards() {
        final LanguageSettingsFragment languageSettingsFragment = startFragment();

        Assert.assertTrue(navigateByClicking(languageSettingsFragment, R.id.settings_tile_keyboards) instanceof KeyboardAddOnBrowserFragment);
    }

    @Test
    public void testNavigationGrammar() {
        final LanguageSettingsFragment languageSettingsFragment = startFragment();

        Assert.assertTrue(navigateByClicking(languageSettingsFragment, R.id.settings_tile_grammar) instanceof DictionariesFragment);
    }

    @Test
    public void testNavigationTweaks() {
        final LanguageSettingsFragment languageSettingsFragment = startFragment();

        Assert.assertTrue(navigateByClicking(languageSettingsFragment, R.id.settings_tile_even_more) instanceof AdditionalLanguageSettingsFragment);
    }

    private Fragment navigateByClicking(Fragment rootFragment, int viewToClick) {
        final View viewById = rootFragment.getView().findViewById(viewToClick);
        Assert.assertNotNull(viewById);
        final View.OnClickListener onClickListener = Shadows.shadowOf(viewById).getOnClickListener();
        Assert.assertNotNull(onClickListener);
        onClickListener.onClick(viewById);
        Robolectric.flushForegroundThreadScheduler();
        return rootFragment.getActivity().getSupportFragmentManager().findFragmentById(R.id.main_ui_content);
    }
}