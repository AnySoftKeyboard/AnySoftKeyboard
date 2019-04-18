package com.anysoftkeyboard.ui.settings;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.RobolectricFragmentTestCase;
import com.anysoftkeyboard.ViewTestUtils;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.test.SharedPrefsHelper;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowAlertDialog;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class AdditionalUiSettingsFragmentTest extends RobolectricFragmentTestCase<AdditionalUiSettingsFragment> {

    @NonNull
    @Override
    protected AdditionalUiSettingsFragment createFragment() {
        return new AdditionalUiSettingsFragment();
    }

    @Test
    public void testNavigationCommonTopRow() {
        final AdditionalUiSettingsFragment fragment = startFragment();

        ViewTestUtils.performClick(fragment.findPreference("settings_key_ext_kbd_top_row_key"));

        Robolectric.flushForegroundThreadScheduler();
        final Fragment next = fragment.getActivity().getSupportFragmentManager().findFragmentById(R.id.main_ui_content);
        Assert.assertTrue(next instanceof AdditionalUiSettingsFragment.TopRowAddOnBrowserFragment);
        Assert.assertFalse(next.hasOptionsMenu());
    }

    @Test
    public void testNavigationCommonBottomRow() {
        final AdditionalUiSettingsFragment fragment = startFragment();

        ViewTestUtils.performClick(fragment.findPreference("settings_key_ext_kbd_bottom_row_key"));

        Robolectric.flushForegroundThreadScheduler();
        final Fragment next = fragment.getActivity().getSupportFragmentManager().findFragmentById(R.id.main_ui_content);
        Assert.assertTrue(next instanceof AdditionalUiSettingsFragment.BottomRowAddOnBrowserFragment);
        Assert.assertFalse(next.hasOptionsMenu());
    }

    @Test
    public void testNavigationTweaks() {
        final AdditionalUiSettingsFragment fragment = startFragment();

        ViewTestUtils.performClick(fragment.findPreference("tweaks"));

        Robolectric.flushForegroundThreadScheduler();
        final Fragment next = fragment.getActivity().getSupportFragmentManager().findFragmentById(R.id.main_ui_content);
        Assert.assertTrue(next instanceof UiTweaksFragment);
    }

    @Test
    public void testNavigationSupportedRowsAndHappyPath() {
        final AdditionalUiSettingsFragment fragment = startFragment();

        ViewTestUtils.performClick(fragment.findPreference("settings_key_supported_row_modes"));

        Robolectric.flushForegroundThreadScheduler();

        AlertDialog latestAlertDialog = ShadowAlertDialog.getLatestAlertDialog();
        Assert.assertNotNull(latestAlertDialog);
        ShadowAlertDialog shadowAlertDialog = Shadows.shadowOf(latestAlertDialog);

        Assert.assertEquals(4, shadowAlertDialog.getItems().length);
        Assert.assertEquals("Messaging input field", shadowAlertDialog.getItems()[0]);
        Assert.assertEquals("URL input field", shadowAlertDialog.getItems()[1]);
        Assert.assertEquals("Email input field", shadowAlertDialog.getItems()[2]);
        Assert.assertEquals("Password input field", shadowAlertDialog.getItems()[3]);

        Assert.assertTrue(SharedPrefsHelper.getPrefValue(Keyboard.getPrefKeyForEnabledRowMode(Keyboard.KEYBOARD_ROW_MODE_EMAIL), true));
        shadowAlertDialog.clickOnItem(2);
        Assert.assertFalse(shadowAlertDialog.hasBeenDismissed());
        latestAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
        Assert.assertTrue(shadowAlertDialog.hasBeenDismissed());
        Assert.assertFalse(SharedPrefsHelper.getPrefValue(Keyboard.getPrefKeyForEnabledRowMode(Keyboard.KEYBOARD_ROW_MODE_EMAIL), true));
    }

    @Test
    public void testNavigationSupportedRowsAndCancel() {
        final AdditionalUiSettingsFragment fragment = startFragment();

        ViewTestUtils.performClick(fragment.findPreference("settings_key_supported_row_modes"));

        Robolectric.flushForegroundThreadScheduler();

        AlertDialog latestAlertDialog = ShadowAlertDialog.getLatestAlertDialog();
        Assert.assertNotNull(latestAlertDialog);
        ShadowAlertDialog shadowAlertDialog = Shadows.shadowOf(latestAlertDialog);

        Assert.assertTrue(SharedPrefsHelper.getPrefValue(Keyboard.getPrefKeyForEnabledRowMode(Keyboard.KEYBOARD_ROW_MODE_EMAIL), true));
        shadowAlertDialog.clickOnItem(2);
        latestAlertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).performClick();
        Assert.assertTrue(shadowAlertDialog.hasBeenDismissed());
    }
}