package com.anysoftkeyboard.ui.settings;

import android.graphics.LightingColorFilter;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.anysoftkeyboard.RobolectricFragmentTestCase;
import com.anysoftkeyboard.keyboards.views.DemoAnyKeyboardView;
import com.anysoftkeyboard.test.SharedPrefsHelper;
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

    @Test
    public void testDemoAppsVisibility() {
        KeyboardThemeSelectorFragment fragment = startFragment();
        final CheckBox checkbox = fragment.getView().findViewById(R.id.apply_overlay);
        final View demoAppsRoot = fragment.getView().findViewById(R.id.overlay_demo_apps_root);

        Assert.assertEquals(View.GONE, demoAppsRoot.getVisibility());
        checkbox.performClick();
        Assert.assertEquals(View.VISIBLE, demoAppsRoot.getVisibility());
    }

    @Test
    public void testClickOnDemoApp() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_apply_remote_app_colors, true);
        KeyboardThemeSelectorFragment fragment = startFragment();
        final View demoAppsRoot = fragment.getView().findViewById(R.id.overlay_demo_apps_root);
        Assert.assertEquals(View.VISIBLE, demoAppsRoot.getVisibility());

        DemoAnyKeyboardView keyboard = fragment.getView().findViewById(R.id.demo_keyboard_view);
        Assert.assertNotNull(keyboard);

        Assert.assertNull(keyboard.getCurrentResourcesHolder().getKeyboardBackground().getColorFilter());

        fragment.getView().findViewById(R.id.theme_app_demo_whatsapp).performClick();
        Assert.assertEquals(0xff054d44, ((LightingColorFilter) keyboard.getCurrentResourcesHolder().getKeyboardBackground().getColorFilter()).getColorAdd());

        fragment.getView().findViewById(R.id.theme_app_demo_twitter).performClick();
        Assert.assertEquals(0xff005fd1, ((LightingColorFilter) keyboard.getCurrentResourcesHolder().getKeyboardBackground().getColorFilter()).getColorAdd());

        fragment.getView().findViewById(R.id.theme_app_demo_phone).performClick();
        Assert.assertEquals(0xff1c3aa9, ((LightingColorFilter) keyboard.getCurrentResourcesHolder().getKeyboardBackground().getColorFilter()).getColorAdd());

        fragment.getView().findViewById(R.id.theme_app_demo_gmail).performClick();
        Assert.assertEquals(0xffb93221, ((LightingColorFilter) keyboard.getCurrentResourcesHolder().getKeyboardBackground().getColorFilter()).getColorAdd());

        fragment.getView().findViewById(R.id.apply_overlay).performClick();

        Assert.assertNull(keyboard.getCurrentResourcesHolder().getKeyBackground().getColorFilter());
    }
}