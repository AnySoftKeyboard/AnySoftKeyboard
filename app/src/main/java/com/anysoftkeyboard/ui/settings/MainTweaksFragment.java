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

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.anysoftkeyboard.prefs.GlobalPrefsBackup;
import com.anysoftkeyboard.rx.RxSchedulers;
import com.anysoftkeyboard.ui.GeneralDialogController;
import com.anysoftkeyboard.ui.dev.DeveloperToolsFragment;
import com.menny.android.anysoftkeyboard.R;

import net.evendanan.chauffeur.lib.FragmentChauffeurActivity;
import net.evendanan.chauffeur.lib.experiences.TransitionExperiences;
import net.evendanan.pushingpixels.RxProgressDialog;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;

public class MainTweaksFragment extends PreferenceFragmentCompat {

    @VisibleForTesting
    static final String DEV_TOOLS_KEY = "dev_tools";

    private static final int FAILED_DIALOG = 10;
    private static final int SUCCESS_DIALOG = 20;

    private GeneralDialogController mDialogController;
    @NonNull
    private CompositeDisposable mDisposable = new CompositeDisposable();

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.prefs_main_tweaks);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDialogController = new GeneralDialogController(getActivity(), this::onSetupDialogRequired);

        Preference preference = findPreference(DEV_TOOLS_KEY);
        if (preference == null) {
            throw new NullPointerException("Preference with key '" + DEV_TOOLS_KEY + "' was not found in resource " + R.xml.prefs_main_tweaks);
        } else {
            preference.setOnPreferenceClickListener(this::onDevToolsPreferenceClicked);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main_tweaks_menu, menu);
    }

    @Override
    public void onStart() {
        super.onStart();
        MainSettingsActivity.setActivityTitle(this, getString(R.string.tweaks_group));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mDialogController.dismiss();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.backup_prefs:
            case R.id.restore_prefs:
                mDialogController.showDialog(item.getItemId());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onSetupDialogRequired(AlertDialog.Builder builder, int optionId, Object data) {
        final int actionString;
        final boolean backup;
        switch (optionId) {
            case R.id.backup_prefs:
                backup = true;
                actionString = R.string.word_editor_action_backup_words;
                builder.setTitle(R.string.pick_prefs_providers_to_backup);
                break;
            case R.id.restore_prefs:
                backup = false;
                actionString = R.string.word_editor_action_restore_words;
                builder.setTitle(R.string.pick_prefs_providers_to_restore);
                break;
            default:
                throw new IllegalArgumentException("The option-id " + optionId + " is not supported here.");
        }

        final List<GlobalPrefsBackup.ProviderDetails> supportedProviders = GlobalPrefsBackup.getAllPrefsProviders(getContext());
        final CharSequence[] providersTitles = new CharSequence[supportedProviders.size()];
        final boolean[] initialChecked = new boolean[supportedProviders.size()];
        final Boolean[] checked = new Boolean[supportedProviders.size()];

        for (int providerIndex = 0; providerIndex < supportedProviders.size(); providerIndex++) {
            //starting with everything checked
            checked[providerIndex] = initialChecked[providerIndex] = true;
            providersTitles[providerIndex] = getText(supportedProviders.get(providerIndex).providerTitle);
        }

        builder.setMultiChoiceItems(providersTitles, initialChecked, (dialogInterface, i, b) -> checked[i] = b);
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setCancelable(true);
        builder.setPositiveButton(actionString, (dialog, which) -> {
            mDisposable.dispose();
            mDisposable = new CompositeDisposable();


            final Observable<Pair<List<GlobalPrefsBackup.ProviderDetails>, Boolean[]>> dialogObservable =
                    RxProgressDialog.create(new Pair<>(supportedProviders, checked), getActivity(), getText(R.string.take_a_while_progress_message))
                            .subscribeOn(RxSchedulers.background());

            final Observable<Boolean> prefsOperationObservable = backup ? GlobalPrefsBackup.backup(dialogObservable) : GlobalPrefsBackup.restore(dialogObservable);

            mDisposable.add(prefsOperationObservable
                    .lastOrError()
                    .observeOn(RxSchedulers.mainThread())
                    .subscribe(
                            lastBoolean -> mDialogController.showDialog(SUCCESS_DIALOG),
                            e -> mDialogController.showDialog(FAILED_DIALOG, e)));
        });
    }

    private boolean onDevToolsPreferenceClicked(Preference p) {
        Activity activity = getActivity();
        if (activity != null && activity instanceof FragmentChauffeurActivity) {
            ((FragmentChauffeurActivity) activity).addFragmentToUi(new DeveloperToolsFragment(), TransitionExperiences.DEEPER_EXPERIENCE_TRANSITION);
            return true;
        }
        return false;
    }

}
