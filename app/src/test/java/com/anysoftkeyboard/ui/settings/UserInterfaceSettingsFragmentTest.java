package com.anysoftkeyboard.ui.settings;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.View;

import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Test;

public class UserInterfaceSettingsFragmentTest extends BaseSettingsFragmentTest<UserInterfaceSettingsFragment> {

    @NonNull
    @Override
    protected UserInterfaceSettingsFragment createFragment() {
        return new UserInterfaceSettingsFragment();
    }

    @Override
    public void testLandscape() {
        super.testLandscape();
        //also
        Fragment fragment = startFragment();
        Assert.assertEquals(View.GONE, fragment.getView().findViewById(R.id.demo_keyboard_view_background).getVisibility());
    }

    @Override
    public void testPortrait() {
        super.testPortrait();
        //also
        Fragment fragment = startFragment();
        Assert.assertEquals(View.VISIBLE, fragment.getView().findViewById(R.id.demo_keyboard_view_background).getVisibility());
    }

    @Test
    public void testNavigationThemes() {
        final Fragment fragment = startFragment();

        Assert.assertTrue(navigateByClicking(fragment, R.id.settings_tile_themes) instanceof KeyboardThemeSelectorFragment);
    }

    @Test
    public void testNavigationEffects() {
        final Fragment fragment = startFragment();

        Assert.assertTrue(navigateByClicking(fragment, R.id.settings_tile_effects) instanceof EffectsSettingsFragment);
    }

    @Test
    public void testNavigationTweaks() {
        final Fragment fragment = startFragment();

        Assert.assertTrue(navigateByClicking(fragment, R.id.settings_tile_even_more) instanceof AdditionalUiSettingsFragment);
    }
}