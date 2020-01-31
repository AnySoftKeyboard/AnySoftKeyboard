package com.anysoftkeyboard.ui.settings;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.RobolectricFragmentTestCase;
import com.anysoftkeyboard.ViewTestUtils;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.test.SharedPrefsHelper;
import com.anysoftkeyboard.utils.GeneralDialogTestUtil;
import com.menny.android.anysoftkeyboard.R;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.Shadows;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class AdditionalUiSettingsFragmentTest
        extends RobolectricFragmentTestCase<AdditionalUiSettingsFragment> {

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
        final Fragment next =
                fragment.getActivity()
                        .getSupportFragmentManager()
                        .findFragmentById(R.id.main_ui_content);
        Assert.assertTrue(next instanceof AdditionalUiSettingsFragment.TopRowAddOnBrowserFragment);
        Assert.assertFalse(next.hasOptionsMenu());
    }

    @Test
    public void testNavigationCommonBottomRow() {
        final AdditionalUiSettingsFragment fragment = startFragment();

        ViewTestUtils.performClick(fragment.findPreference("settings_key_ext_kbd_bottom_row_key"));

        Robolectric.flushForegroundThreadScheduler();
        final Fragment next =
                fragment.getActivity()
                        .getSupportFragmentManager()
                        .findFragmentById(R.id.main_ui_content);
        Assert.assertTrue(
                next instanceof AdditionalUiSettingsFragment.BottomRowAddOnBrowserFragment);
        Assert.assertFalse(next.hasOptionsMenu());
    }

    @Test
    public void testNavigationTweaks() {
        final AdditionalUiSettingsFragment fragment = startFragment();

        ViewTestUtils.performClick(fragment.findPreference("tweaks"));

        Robolectric.flushForegroundThreadScheduler();
        final Fragment next =
                fragment.getActivity()
                        .getSupportFragmentManager()
                        .findFragmentById(R.id.main_ui_content);
        Assert.assertTrue(next instanceof UiTweaksFragment);
    }

    @Test
    public void testNavigationSupportedRowsAndHappyPath() {
        final AdditionalUiSettingsFragment fragment = startFragment();

        ViewTestUtils.performClick(fragment.findPreference("settings_key_supported_row_modes"));

        Robolectric.flushForegroundThreadScheduler();

        AlertDialog latestAlertDialog = GeneralDialogTestUtil.getLatestShownDialog();
        Assert.assertNotNull(latestAlertDialog);

        Assert.assertEquals(4, latestAlertDialog.getListView().getAdapter().getCount());
        Assert.assertEquals(
                "Messaging input field", latestAlertDialog.getListView().getAdapter().getItem(0));
        Assert.assertEquals(
                "URL input field", latestAlertDialog.getListView().getAdapter().getItem(1));
        Assert.assertEquals(
                "Email input field", latestAlertDialog.getListView().getAdapter().getItem(2));
        Assert.assertEquals(
                "Password input field", latestAlertDialog.getListView().getAdapter().getItem(3));

        Assert.assertTrue(
                SharedPrefsHelper.getPrefValue(
                        Keyboard.getPrefKeyForEnabledRowMode(Keyboard.KEYBOARD_ROW_MODE_EMAIL),
                        true));
        Shadows.shadowOf(latestAlertDialog.getListView()).performItemClick(2);
        Assert.assertTrue(latestAlertDialog.isShowing());
        latestAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
        Assert.assertFalse(latestAlertDialog.isShowing());
        Assert.assertFalse(
                SharedPrefsHelper.getPrefValue(
                        Keyboard.getPrefKeyForEnabledRowMode(Keyboard.KEYBOARD_ROW_MODE_EMAIL),
                        true));
    }

    @Test
    public void testNavigationSupportedRowsAndCancel() {
        final AdditionalUiSettingsFragment fragment = startFragment();

        ViewTestUtils.performClick(fragment.findPreference("settings_key_supported_row_modes"));

        Robolectric.flushForegroundThreadScheduler();

        AlertDialog latestAlertDialog = GeneralDialogTestUtil.getLatestShownDialog();
        Assert.assertNotNull(latestAlertDialog);

        Assert.assertTrue(
                SharedPrefsHelper.getPrefValue(
                        Keyboard.getPrefKeyForEnabledRowMode(Keyboard.KEYBOARD_ROW_MODE_EMAIL),
                        true));
        Shadows.shadowOf(latestAlertDialog.getListView()).performItemClick(2);
        latestAlertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).performClick();
        Assert.assertFalse(latestAlertDialog.isShowing());
    }
}
