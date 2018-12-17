package com.anysoftkeyboard.ui.settings;

import android.support.annotation.NonNull;
import android.widget.CheckBox;
import android.widget.TextView;

import com.anysoftkeyboard.RobolectricFragmentTestCase;
import com.f2prateek.rx.preferences2.Preference;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Test;

import androidx.test.core.app.ApplicationProvider;

public class KeyboardThemeSelectorFragmentTest extends RobolectricFragmentTestCase<KeyboardThemeSelectorFragment> {

    @NonNull
    @Override
    protected KeyboardThemeSelectorFragment createFragment() {
        return new KeyboardThemeSelectorFragment();
    }

    @Test
    public void testApplyOverlayCheckBoxChanges() {
        KeyboardThemeSelectorFragment fragment = startFragment();
        final CheckBox checkbox = fragment.getView().findViewById(R.id.apply_overlay);
        final TextView summary = fragment.getView().findViewById(R.id.apply_overlay_summary);

        final Preference<Boolean> pref = AnyApplication.prefs(ApplicationProvider.getApplicationContext())
                .getBoolean(R.string.settings_key_apply_remote_app_colors, R.bool.settings_default_apply_remote_app_colors);

        Assert.assertFalse(pref.get());
        Assert.assertEquals(checkbox.isChecked(), pref.get());
        Assert.assertEquals(summary.getText(), fragment.getResources().getString(R.string.apply_overlay_summary_off));

        checkbox.performClick();

        Assert.assertTrue(pref.get());
        Assert.assertEquals(checkbox.isChecked(), pref.get());
        Assert.assertEquals(summary.getText(), fragment.getResources().getString(R.string.apply_overlay_summary_on));
    }
}