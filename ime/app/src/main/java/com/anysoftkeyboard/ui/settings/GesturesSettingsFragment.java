/*
 * Copyright (c) 2013 Menny Even-Danan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.anysoftkeyboard.ui.settings;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import com.menny.android.anysoftkeyboard.R;
import java.util.ArrayList;
import java.util.List;
import net.evendanan.pixel.GeneralDialogController;

public class GesturesSettingsFragment extends PreferenceFragmentCompat {

    private GeneralDialogController mGeneralDialogController;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.prefs_gestures_prefs);
        mGeneralDialogController = new GeneralDialogController(getActivity(), this::setupDialog);
    }

    private void setupDialog(
            androidx.appcompat.app.AlertDialog.Builder builder, int optionId, Object data) {
        builder.setTitle(R.string.gesture_typing_alert_title)
                .setMessage(R.string.gesture_typing_alert_message)
                .setPositiveButton(
                        R.string.gesture_typing_alert_button, (dialog, which) -> dialog.dismiss());
    }

    @VisibleForTesting
    List<Preference> findPrefs(String... keys) {
        ArrayList<Preference> prefs = new ArrayList<>(keys.length);
        for (String key : keys) {
            final Preference preference = findPreference(key);

            prefs.add(preference);
        }

        return prefs;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findPreference(getString(R.string.settings_key_gesture_typing))
                .setOnPreferenceChangeListener(
                        (preference, newValue) -> {
                            final boolean gestureTypingEnabled = (boolean) newValue;
                            if (gestureTypingEnabled) {
                                mGeneralDialogController.showDialog(1);
                            }
                            for (Preference affectedPref : getAffectedPrefs()) {
                                affectedPref.setEnabled(!gestureTypingEnabled);
                            }
                            return true;
                        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        findPreference(getString(R.string.settings_key_gesture_typing))
                .setOnPreferenceChangeListener(null);
    }

    @Override
    public void onStart() {
        super.onStart();
        MainSettingsActivity.setActivityTitle(
                this, getString(R.string.unicode_gestures_quick_text_key_name));

        final boolean gestureTypingEnabled =
                ((CheckBoxPreference)
                                findPreference(getString(R.string.settings_key_gesture_typing)))
                        .isChecked();
        for (Preference affectedPref : getAffectedPrefs()) {
            affectedPref.setEnabled(!gestureTypingEnabled);
        }
    }

    private List<Preference> getAffectedPrefs() {
        return findPrefs(
                "settings_key_swipe_up_action",
                "settings_key_swipe_down_action",
                "settings_key_swipe_left_action",
                "settings_key_swipe_right_action");
    }

    @Override
    public void onStop() {
        super.onStop();
        mGeneralDialogController.dismiss();
    }
}
