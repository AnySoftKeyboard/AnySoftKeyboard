package com.anysoftkeyboard.ui.settings;

import android.app.Application;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;

import com.anysoftkeyboard.RobolectricFragmentTestCase;
import com.anysoftkeyboard.ViewTestUtils;
import com.anysoftkeyboard.test.SharedPrefsHelper;
import com.anysoftkeyboard.ui.GeneralDialogControllerTest;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowAlertDialog;

import java.util.List;

import androidx.test.core.app.ApplicationProvider;

public class GesturesSettingsFragmentTest extends RobolectricFragmentTestCase<GesturesSettingsFragment> {

    private List<Preference> mAffectedPrefs;
    private List<Preference> mNotAffectedPrefs;
    private CheckBoxPreference mGestureTypingPref;

    @NonNull
    @Override
    protected GesturesSettingsFragment createFragment() {
        return new GesturesSettingsFragment();
    }

    @Before
    public void startFragmentAndSetPrefs() {
        GesturesSettingsFragment fragment = startFragment();
        mAffectedPrefs = fragment.findPrefs(
                "settings_key_swipe_up_action",
                "settings_key_swipe_down_action",
                "settings_key_swipe_left_action",
                "settings_key_swipe_right_action");
        mNotAffectedPrefs = fragment.findPrefs(
                "settings_key_swipe_left_space_bar_action",
                "settings_key_swipe_right_space_bar_action",
                "settings_key_swipe_left_two_fingers_action",
                "settings_key_swipe_right_two_fingers_action",
                "settings_key_swipe_up_from_spacebar_action",
                "settings_key_pinch_gesture_action",
                "settings_key_separate_gesture_action",
                "settings_key_swipe_velocity_threshold",
                "settings_key_swipe_distance_threshold");
        mGestureTypingPref = (CheckBoxPreference) fragment.findPreference("settings_key_gesture_typing");

        for (int prefIndex = 0; prefIndex < fragment.getPreferenceScreen().getPreferenceCount(); prefIndex++) {
            final Preference preference = fragment.getPreferenceScreen().getPreference(prefIndex);
            if (preference instanceof PreferenceCategory) continue;
            Assert.assertTrue("Failed for pref key " + preference.getKey(),
                    preference == mGestureTypingPref ||
                            mAffectedPrefs.contains(preference) ||
                            mNotAffectedPrefs.contains(preference));
        }
    }

    @Test
    public void testDisabledSomeGesturesWhenGestureTypingEnabled() {
        Assert.assertFalse(mGestureTypingPref.isChecked());

        for (Preference pref : mAffectedPrefs) {
            Assert.assertTrue(pref.isEnabled());
        }

        for (Preference pref : mNotAffectedPrefs) {
            Assert.assertTrue(pref.isEnabled());
        }

        ViewTestUtils.performClick(mGestureTypingPref);

        Assert.assertTrue(mGestureTypingPref.isChecked());

        for (Preference pref : mAffectedPrefs) {
            Assert.assertFalse(pref.isEnabled());
        }

        for (Preference pref : mNotAffectedPrefs) {
            Assert.assertTrue(pref.isEnabled());
        }

        ViewTestUtils.performClick(mGestureTypingPref);

        Assert.assertFalse(mGestureTypingPref.isChecked());

        for (Preference pref : mAffectedPrefs) {
            Assert.assertTrue(pref.isEnabled());
        }

        for (Preference pref : mNotAffectedPrefs) {
            Assert.assertTrue(pref.isEnabled());
        }
    }

    @Test
    public void testShowAlertWhenEnablingGesture() {
        Assert.assertFalse(mGestureTypingPref.isChecked());

        Assert.assertNull(ShadowAlertDialog.getLatestAlertDialog());

        ViewTestUtils.performClick(mGestureTypingPref);
        Assert.assertTrue(mGestureTypingPref.isChecked());

        final AlertDialog dialog = GeneralDialogControllerTest.getLatestShownDialog();
        Assert.assertNotSame(GeneralDialogControllerTest.NO_DIALOG, dialog);
        Assert.assertEquals("BETA Feature!", GeneralDialogControllerTest.getTitleFromDialog(dialog).toString());
        dialog.dismiss();
        ShadowAlertDialog.reset();

        ViewTestUtils.performClick(mGestureTypingPref);
        Assert.assertFalse(mGestureTypingPref.isChecked());

        Assert.assertNull(ShadowAlertDialog.getLatestAlertDialog());
    }

    @Test
    public void testStartWithEnabled() {
        getFragmentController().destroy();

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_gesture_typing, true);

        startFragmentAndSetPrefs();

        Assert.assertTrue(mGestureTypingPref.isChecked());
        Assert.assertNull(Shadows.shadowOf((Application) ApplicationProvider.getApplicationContext()).getLatestAlertDialog());

        for (Preference pref : mAffectedPrefs) {
            Assert.assertFalse(pref.isEnabled());
        }

        for (Preference pref : mNotAffectedPrefs) {
            Assert.assertTrue(pref.isEnabled());
        }

        ViewTestUtils.performClick(mGestureTypingPref);

        Assert.assertFalse(mGestureTypingPref.isChecked());

        for (Preference pref : mAffectedPrefs) {
            Assert.assertTrue(pref.isEnabled());
        }

        for (Preference pref : mNotAffectedPrefs) {
            Assert.assertTrue(pref.isEnabled());
        }
    }
}