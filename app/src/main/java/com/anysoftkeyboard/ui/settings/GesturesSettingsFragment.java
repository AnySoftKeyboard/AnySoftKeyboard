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

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.View;

import com.menny.android.anysoftkeyboard.R;

import java.util.ArrayList;
import java.util.List;

public class GesturesSettingsFragment extends PreferenceFragmentCompat {

    @Nullable
    private AlertDialog mAlertDialog;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.prefs_gestures_prefs);
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
        findPreference(getString(R.string.settings_key_gesture_typing)).setOnPreferenceChangeListener((preference, newValue) -> {
            final boolean gestureTypingEnabled = (boolean) newValue;
            if (gestureTypingEnabled) {
                mAlertDialog = new AlertDialog.Builder(getActivity()).setTitle(R.string.gesture_typing_alert_title)
                        .setMessage(R.string.gesture_typing_alert_message)
                        .setPositiveButton(R.string.gesture_typing_alert_button, (dialog, which) -> dialog.dismiss())
                        .create();
                mAlertDialog.show();
            }
            for (Preference affectedPref : getAffectedPrefs()) {
                affectedPref.setEnabled(!gestureTypingEnabled);
            }
            return true;
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        MainSettingsActivity.setActivityTitle(this, getString(R.string.unicode_gestures_quick_text_key_name));

        final boolean gestureTypingEnabled = ((CheckBoxPreference) findPreference(getString(R.string.settings_key_gesture_typing))).isChecked();
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
        if (mAlertDialog != null) {
            mAlertDialog.dismiss();
            mAlertDialog = null;
        }
    }
}
