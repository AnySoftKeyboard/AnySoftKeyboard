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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.SharedPreferencesCompat;
import android.support.v7.app.AlertDialog;
import com.anysoftkeyboard.PermissionsRequestCodes;
import com.anysoftkeyboard.ui.settings.setup.SetUpKeyboardWizardFragment;
import com.menny.android.anysoftkeyboard.R;
import java.lang.ref.WeakReference;
import net.evendanan.chauffeur.lib.permissions.PermissionsFragmentChauffeurActivity;
import net.evendanan.chauffeur.lib.permissions.PermissionsRequest;
import net.evendanan.pixel.EdgeEffectHacker;

public class BasicAnyActivity extends PermissionsFragmentChauffeurActivity {

    private AlertDialog mAlertDialog;

    private final DialogInterface.OnClickListener mContactsDictionaryDialogListener =
            (dialog, which) -> {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        if (ActivityCompat.shouldShowRequestPermissionRationale(
                                BasicAnyActivity.this, Manifest.permission.READ_CONTACTS)) {
                            startContactsPermissionRequest();
                        } else {
                            startAppPermissionsActivity();
                        }
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        SharedPreferences sharedPreferences =
                                PreferenceManager.getDefaultSharedPreferences(
                                        getApplicationContext());
                        final SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean(
                                getString(R.string.settings_key_use_contacts_dictionary), false);
                        SharedPreferencesCompat.EditorCompat.getInstance().apply(editor);
                        break;
                    default:
                        throw new IllegalArgumentException(
                                "Failed to handle "
                                        + which
                                        + " in mContactsDictionaryDialogListener");
                }
            };

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(getViewLayoutResourceId());
    }

    @LayoutRes
    protected int getViewLayoutResourceId() {
        return R.layout.initial_setup_main_ui;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // applying my very own Edge-Effect color
        EdgeEffectHacker.brandGlowEffect(this, ContextCompat.getColor(this, R.color.app_accent));
    }

    @NonNull
    @Override
    protected Fragment createRootFragmentInstance() {
        return new SetUpKeyboardWizardFragment();
    }

    @Override
    protected int getFragmentRootUiElementId() {
        return R.id.main_ui_content;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
            mAlertDialog = null;
        }
    }

    public void startContactsPermissionRequest() {
        startPermissionsRequest(new ContactPermissionRequest(this));
    }

    @NonNull
    @Override
    protected PermissionsRequest createPermissionRequestFromIntentRequest(
            int requestId, @NonNull String[] permissions, @NonNull Intent intent) {
        if (requestId == PermissionsRequestCodes.CONTACTS.getRequestCode()) {
            return new ContactPermissionRequest(this);
        } else {
            return super.createPermissionRequestFromIntentRequest(requestId, permissions, intent);
        }
    }

    private static class ContactPermissionRequest
            extends PermissionsRequest.PermissionsRequestBase {

        private final WeakReference<BasicAnyActivity> mMainSettingsActivityWeakReference;

        ContactPermissionRequest(BasicAnyActivity activity) {
            super(
                    PermissionsRequestCodes.CONTACTS.getRequestCode(),
                    Manifest.permission.READ_CONTACTS);
            mMainSettingsActivityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void onPermissionsGranted() {
            /*
            nothing to do here, it will re-load the contact dictionary next time the
            input-connection will start.
            */
        }

        @Override
        public void onPermissionsDenied(
                @NonNull String[] grantedPermissions,
                @NonNull String[] deniedPermissions,
                @NonNull String[] declinedPermissions) {
            BasicAnyActivity activity = mMainSettingsActivityWeakReference.get();
            if (activity == null) return;
            // if the result is DENIED and the OS says "do not show rationale", it means the user
            // has ticked "Don't ask me again".
            final boolean userSaysDontAskAgain =
                    !ActivityCompat.shouldShowRequestPermissionRationale(
                            activity, Manifest.permission.READ_CONTACTS);
            // the user has denied us from reading the Contacts information.
            // I'll ask them to whether they want to grant anyway, or disable ContactDictionary
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setCancelable(true);
            builder.setIcon(R.drawable.ic_notification_contacts_permission_required);
            builder.setTitle(R.string.notification_read_contacts_title);
            builder.setMessage(activity.getString(R.string.contacts_permissions_dialog_message));
            builder.setPositiveButton(
                    activity.getString(
                            userSaysDontAskAgain
                                    ? R.string.navigate_to_app_permissions
                                    : R.string.allow_permission),
                    activity.mContactsDictionaryDialogListener);
            builder.setNegativeButton(
                    activity.getString(R.string.turn_off_contacts_dictionary),
                    activity.mContactsDictionaryDialogListener);

            if (activity.mAlertDialog != null && activity.mAlertDialog.isShowing())
                activity.mAlertDialog.dismiss();
            activity.mAlertDialog = builder.create();
            activity.mAlertDialog.show();
        }
    }
}
