package com.anysoftkeyboard.ui.settings;

import android.support.annotation.NonNull;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class NextWordSettingsFragmentTest extends BaseSettingsFragmentTest<NextWordSettingsFragment> {

    @NonNull
    @Override
    protected NextWordSettingsFragment createFragment() {
        return new NextWordSettingsFragment();
    }

    @Test
    public void testShowLanguageStats() {
        final NextWordSettingsFragment nextWordSettingsFragment = startFragment();

    }
}