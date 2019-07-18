package com.anysoftkeyboard.ui.settings;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.preference.Preference;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.RobolectricFragmentTestCase;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class QuickTextSettingsFragmentTest
        extends RobolectricFragmentTestCase<QuickTextSettingsFragment> {

    @Test
    @Config(sdk = Build.VERSION_CODES.N)
    public void testVisibleAtN() {
        final Preference preference =
                startFragment().findPreference("settings_key_default_emoji_skin_tone");
        Assert.assertNotNull(preference);
        Assert.assertTrue(preference.isVisible());
        Assert.assertTrue(preference.isEnabled());
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.M)
    public void testInvisibleBeforeN() {
        final Preference preference =
                startFragment().findPreference("settings_key_default_emoji_skin_tone");
        Assert.assertNotNull(preference);
        Assert.assertFalse(preference.isVisible());
        Assert.assertFalse(preference.isEnabled());
    }

    @NonNull
    @Override
    protected QuickTextSettingsFragment createFragment() {
        return new QuickTextSettingsFragment();
    }
}
