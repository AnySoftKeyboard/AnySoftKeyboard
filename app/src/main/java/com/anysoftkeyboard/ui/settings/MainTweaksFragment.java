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

import android.Manifest;
import android.app.Activity;
import android.os.Build;
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

import com.anysoftkeyboard.PermissionsRequestCodes;
import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.prefs.GlobalPrefsBackup;
import com.anysoftkeyboard.rx.RxSchedulers;
import com.anysoftkeyboard.ui.GeneralDialogController;
import com.anysoftkeyboard.ui.dev.DeveloperToolsFragment;
import com.menny.android.anysoftkeyboard.R;

import net.evendanan.chauffeur.lib.FragmentChauffeurActivity;
import net.evendanan.chauffeur.lib.experiences.TransitionExperiences;
import net.evendanan.chauffeur.lib.permissions.PermissionsRequest;
import net.evendanan.pixel.RxProgressDialog;

import java.lang.ref.WeakReference;
import java.util.List;

import io.reactivex.ObservableSource;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;

public class MainTweaksFragment extends PreferenceFragmentCompat {

    @VisibleForTesting
    static final String DEV_TOOLS_KEY = "dev_tools";

    static final int DIALOG_SAVE_SUCCESS = 10;
    static final int DIALOG_SAVE_FAILED = 11;
    static final int DIALOG_LOAD_SUCCESS = 20;
    static final int DIALOG_LOAD_FAILED = 21;

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
                ((MainSettingsActivity) getActivity()).startPermissionsRequest(new StoragePermissionRequest(this, item.getItemId()));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onSetupDialogRequired(AlertDialog.Builder builder, int optionId, Object data) {
        switch (optionId) {
            case R.id.backup_prefs:
            case R.id.restore_prefs:
                onBackupRestoreDialogRequired(builder, optionId);
                break;
            case DIALOG_SAVE_SUCCESS:
                builder.setTitle(R.string.prefs_providers_operation_success);
                builder.setMessage(getString(R.string.prefs_providers_backed_up_to, data));
                builder.setPositiveButton(android.R.string.ok, null);
                break;
            case DIALOG_SAVE_FAILED:
                builder.setTitle(R.string.prefs_providers_operation_failed);
                builder.setMessage(getString(R.string.prefs_providers_failed_backup_due_to, data));
                builder.setPositiveButton(android.R.string.ok, null);
                break;
            case DIALOG_LOAD_SUCCESS:
                builder.setTitle(R.string.prefs_providers_operation_success);
                builder.setMessage(getString(R.string.prefs_providers_restored_to, data));
                builder.setPositiveButton(android.R.string.ok, null);
                break;
            case DIALOG_LOAD_FAILED:
                builder.setTitle(R.string.prefs_providers_operation_failed);
                builder.setMessage(getString(R.string.prefs_providers_failed_restore_due_to, data));
                builder.setPositiveButton(android.R.string.ok, null);
                break;
            default:
                throw new IllegalArgumentException("The option-id " + optionId + " is not supported here.");
        }
    }

    private void onBackupRestoreDialogRequired(AlertDialog.Builder builder, int optionId) {
        final int actionString;
        final Function<Pair<List<GlobalPrefsBackup.ProviderDetails>, Boolean[]>, ObservableSource<GlobalPrefsBackup.ProviderDetails>> action;
        final int successDialog;
        final int failedDialog;
        switch (optionId) {
            case R.id.backup_prefs:
                action = GlobalPrefsBackup::backup;
                actionString = R.string.word_editor_action_backup_words;
                builder.setTitle(R.string.pick_prefs_providers_to_backup);
                successDialog = DIALOG_SAVE_SUCCESS;
                failedDialog = DIALOG_SAVE_FAILED;
                break;
            case R.id.restore_prefs:
                action = GlobalPrefsBackup::restore;
                actionString = R.string.word_editor_action_restore_words;
                builder.setTitle(R.string.pick_prefs_providers_to_restore);
                successDialog = DIALOG_LOAD_SUCCESS;
                failedDialog = DIALOG_LOAD_FAILED;
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


            mDisposable.add(RxProgressDialog.create(new Pair<>(supportedProviders, checked), getActivity(), getText(R.string.take_a_while_progress_message), R.layout.progress_window)
                    .subscribeOn(RxSchedulers.background())
                    .flatMap(action)
                    .observeOn(RxSchedulers.mainThread())
                    .subscribe(
                            providerDetails -> Logger.i("MainTweaksFragment", "Finished backing up %s", providerDetails.provider.providerId()),
                            e -> {
                                Logger.w("MainTweaksFragment", e, "Failed to do operation due to %s", e.getMessage());
                                mDialogController.showDialog(failedDialog, e.getMessage());
                            },
                            () -> mDialogController.showDialog(successDialog, GlobalPrefsBackup.getBackupFile().getAbsolutePath())));
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


    private static class StoragePermissionRequest extends
            PermissionsRequest.PermissionsRequestBase {

        private final WeakReference<MainTweaksFragment> mFragmentWeakReference;
        private final int mOptionId;

        StoragePermissionRequest(MainTweaksFragment fragment, int optionId) {
            super(PermissionsRequestCodes.STORAGE.getRequestCode(),
                    getPermissionsForOsVersion());
            mOptionId = optionId;
            mFragmentWeakReference = new WeakReference<>(fragment);
        }

        @NonNull
        private static String[] getPermissionsForOsVersion() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                return new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE};
            } else {
                return new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
            }
        }

        @Override
        public void onPermissionsGranted() {
            MainTweaksFragment fragment = mFragmentWeakReference.get();
            if (fragment == null) return;

            fragment.mDialogController.showDialog(mOptionId);
        }

        @Override
        public void onPermissionsDenied(@NonNull String[] grantedPermissions,
                @NonNull String[] deniedPermissions, @NonNull String[] declinedPermissions) {
            /*no-op - Main-Activity handles this case*/
        }
    }
}
