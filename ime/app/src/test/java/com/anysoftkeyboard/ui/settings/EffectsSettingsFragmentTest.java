package com.anysoftkeyboard.ui.settings;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.RobolectricFragmentTestCase;
import com.anysoftkeyboard.ViewTestUtils;
import com.menny.android.anysoftkeyboard.R;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class EffectsSettingsFragmentTest
        extends RobolectricFragmentTestCase<EffectsSettingsFragment> {

    @NonNull
    @Override
    protected EffectsSettingsFragment createFragment() {
        return new EffectsSettingsFragment();
    }

    @Test
    public void testNavigateToPowerSavingFragment() {
        final EffectsSettingsFragment fragment = startFragment();

        ViewTestUtils.performClick(fragment.findPreference("settings_key_power_save_mode"));

        Robolectric.flushForegroundThreadScheduler();
        final Fragment next =
                fragment.getActivity()
                        .getSupportFragmentManager()
                        .findFragmentById(R.id.main_ui_content);
        Assert.assertTrue(next instanceof PowerSavingSettingsFragment);
    }
}
