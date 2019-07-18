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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceFragmentCompat;
import com.anysoftkeyboard.android.NightMode;
import com.anysoftkeyboard.rx.GenericOnError;
import com.menny.android.anysoftkeyboard.R;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;

public class NightModeSettingsFragment extends PreferenceFragmentCompat {

    private Disposable mAppNightModeDisposable = Disposables.empty();

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.night_mode_prefs);
    }

    @Override
    public void onStart() {
        super.onStart();
        MainSettingsActivity.setActivityTitle(this, getString(R.string.night_mode_screen));
        mAppNightModeDisposable =
                NightMode.observeNightModeState(
                                getContext(),
                                R.string.settings_key_night_mode_app_theme_control,
                                R.bool.settings_default_true)
                        .subscribe(
                                enabled ->
                                        ((AppCompatActivity) getActivity())
                                                .getDelegate()
                                                .applyDayNight(),
                                GenericOnError.onError("NightModeSettingsFragment"));
    }

    @Override
    public void onStop() {
        super.onStop();
        mAppNightModeDisposable.dispose();
    }
}
