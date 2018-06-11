package com.anysoftkeyboard.ui.settings;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.preference.Preference;

import com.anysoftkeyboard.RobolectricFragmentTestCase;
import com.anysoftkeyboard.ui.dev.DeveloperToolsFragment;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Test;
import org.robolectric.Robolectric;

public class MainTweaksFragmentTest extends RobolectricFragmentTestCase<MainTweaksFragment> {

    @NonNull
    @Override
    protected MainTweaksFragment createFragment() {
        return new MainTweaksFragment();
    }

    @Test
    public void testNavigateToDevTools() {
        MainTweaksFragment fragment = startFragment();

        final Preference preferenceDevTools = fragment.findPreference(MainTweaksFragment.DEV_TOOLS_KEY);
        preferenceDevTools.getOnPreferenceClickListener().onPreferenceClick(preferenceDevTools);

        Robolectric.flushForegroundThreadScheduler();
        Fragment navigatedToFragment = fragment.getActivity().getSupportFragmentManager().findFragmentById(R.id.main_ui_content);
        Assert.assertTrue(navigatedToFragment instanceof DeveloperToolsFragment);
    }
}