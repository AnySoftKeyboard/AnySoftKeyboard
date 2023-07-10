package com.anysoftkeyboard.ui.settings;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import com.anysoftkeyboard.RobolectricFragmentTestCase;
import com.anysoftkeyboard.ViewTestUtils;
import com.anysoftkeyboard.test.SharedPrefsHelper;
import com.menny.android.anysoftkeyboard.R;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GesturesSettingsFragmentTest
    extends RobolectricFragmentTestCase<GesturesSettingsFragment> {

  private List<Preference> mAffectedPrefs;
  private List<Preference> mNotAffectedPrefs;
  private CheckBoxPreference mGestureTypingPref;

  @Override
  protected int getStartFragmentNavigationId() {
    return R.id.gesturesSettingsFragment;
  }

  @Before
  public void startFragmentAndSetPrefs() {
    GesturesSettingsFragment fragment = startFragment();
    mAffectedPrefs =
        fragment.findPrefs(
            "settings_key_swipe_up_action",
            "settings_key_swipe_down_action",
            "settings_key_swipe_left_action",
            "settings_key_swipe_right_action");
    mNotAffectedPrefs =
        fragment.findPrefs(
            "settings_key_swipe_left_space_bar_action",
            "settings_key_swipe_right_space_bar_action",
            "settings_key_swipe_left_two_fingers_action",
            "settings_key_swipe_right_two_fingers_action",
            "settings_key_swipe_up_from_spacebar_action",
            "settings_key_pinch_gesture_action",
            "settings_key_separate_gesture_action",
            "settings_key_swipe_velocity_threshold",
            "settings_key_swipe_distance_threshold");
    mGestureTypingPref = fragment.findPreference("settings_key_gesture_typing");

    for (int prefIndex = 0;
        prefIndex < fragment.getPreferenceScreen().getPreferenceCount();
        prefIndex++) {
      final Preference preference = fragment.getPreferenceScreen().getPreference(prefIndex);
      if (preference instanceof PreferenceCategory) continue;
      Assert.assertTrue(
          "Failed for pref key " + preference.getKey(),
          preference == mGestureTypingPref
              || mAffectedPrefs.contains(preference)
              || mNotAffectedPrefs.contains(preference));
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

    Assert.assertSame(
        GeneralDialogTestUtil.NO_DIALOG, GeneralDialogTestUtil.getLatestShownDialog());

    ViewTestUtils.performClick(mGestureTypingPref);
    Assert.assertTrue(mGestureTypingPref.isChecked());

    final AlertDialog dialog = GeneralDialogTestUtil.getLatestShownDialog();
    Assert.assertNotSame(GeneralDialogTestUtil.NO_DIALOG, dialog);
    Assert.assertEquals(
        "BETA Feature!", GeneralDialogTestUtil.getTitleFromDialog(dialog).toString());
    dialog.dismiss();

    ViewTestUtils.performClick(mGestureTypingPref);
    Assert.assertFalse(mGestureTypingPref.isChecked());

    Assert.assertSame(
        GeneralDialogTestUtil.NO_DIALOG, GeneralDialogTestUtil.getLatestShownDialog());
  }

  @Test
  public void testStartWithEnabled() {
    getActivityController().destroy();

    SharedPrefsHelper.setPrefsValue(R.string.settings_key_gesture_typing, true);

    startFragmentAndSetPrefs();

    Assert.assertTrue(mGestureTypingPref.isChecked());
    Assert.assertSame(
        GeneralDialogTestUtil.NO_DIALOG, GeneralDialogTestUtil.getLatestShownDialog());

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
