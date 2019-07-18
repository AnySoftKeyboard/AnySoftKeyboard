package com.anysoftkeyboard.ui.settings.setup;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.SharedPreferencesCompat;
import android.view.View;
import com.anysoftkeyboard.PermissionsRequestCodes;
import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.ui.settings.BasicAnyActivity;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;
import java.lang.ref.WeakReference;
import net.evendanan.chauffeur.lib.permissions.PermissionsRequest;

public class WizardPermissionsFragment extends WizardPageBaseFragment
        implements View.OnClickListener {

    private final PermissionsRequest mContactsPermissionRequest =
            new ContactPermissionRequest(this);

    @Override
    protected int getPageLayoutId() {
        return R.layout.keyboard_setup_wizard_page_permissions_layout;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.ask_for_permissions_action).setOnClickListener(this);
        mStateIcon.setOnClickListener(this);
        view.findViewById(R.id.disable_contacts_dictionary).setOnClickListener(this);
        view.findViewById(R.id.open_permissions_wiki_action).setOnClickListener(this);
    }

    @Override
    protected boolean isStepCompleted(@NonNull Context context) {
        return isContactsDictionaryDisabled(context)
                || // either the user disabled Contacts
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
                        == PackageManager.PERMISSION_GRANTED; // or the user granted permission
    }

    private boolean isContactsDictionaryDisabled(Context context) {
        return !AnyApplication.prefs(context)
                .getBoolean(
                        R.string.settings_key_use_contacts_dictionary,
                        R.bool.settings_default_contacts_dictionary)
                .get();
    }

    @Override
    public void refreshFragmentUi() {
        super.refreshFragmentUi();
        if (getActivity() != null) {
            @DrawableRes final int stateIcon;
            if (isContactsDictionaryDisabled(getActivity())) {
                mStateIcon.setClickable(true);
                stateIcon = R.drawable.ic_wizard_contacts_disabled;
            } else if (isStepCompleted(getActivity())) {
                mStateIcon.setClickable(false);
                stateIcon = R.drawable.ic_wizard_contacts_on;
            } else {
                stateIcon = R.drawable.ic_wizard_contacts_off;
            }
            mStateIcon.setImageResource(stateIcon);
        }
    }

    @Override
    public void onClick(View v) {
        BasicAnyActivity activity = (BasicAnyActivity) getActivity();
        if (activity == null) return;

        switch (v.getId()) {
            case R.id.ask_for_permissions_action:
            case R.id.step_state_icon:
                {
                    SharedPreferences sharedPreferences =
                            PreferenceManager.getDefaultSharedPreferences(activity);
                    final SharedPreferences.Editor edit = sharedPreferences.edit();
                    edit.putBoolean(getString(R.string.settings_key_use_contacts_dictionary), true);
                    SharedPreferencesCompat.EditorCompat.getInstance().apply(edit);
                    activity.startPermissionsRequest(mContactsPermissionRequest);
                    refreshWizardPager();
                }
                break;
            case R.id.disable_contacts_dictionary:
                {
                    SharedPreferences sharedPreferences =
                            PreferenceManager.getDefaultSharedPreferences(activity);
                    final SharedPreferences.Editor edit = sharedPreferences.edit();
                    edit.putBoolean(
                            getString(R.string.settings_key_use_contacts_dictionary), false);
                    SharedPreferencesCompat.EditorCompat.getInstance().apply(edit);
                    refreshWizardPager();
                }
                break;
            case R.id.open_permissions_wiki_action:
                Intent browserIntent =
                        new Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(
                                        getResources()
                                                .getString(R.string.permissions_wiki_site_url)));
                try {
                    startActivity(browserIntent);
                } catch (ActivityNotFoundException weirdException) {
                    // https://github.com/AnySoftKeyboard/AnySoftKeyboard/issues/516
                    // this means that there is nothing on the device
                    // that can handle Intent.ACTION_VIEW with "https" schema..
                    // silently swallowing it
                    Logger.w(
                            "WizardPermissionsFragment",
                            "Can not open '%' since there is nothing on the device that can handle it.",
                            browserIntent.getData());
                }
                break;
            default:
                throw new IllegalArgumentException(
                        "Failed to handle " + v.getId() + " in WizardPermissionsFragment");
        }
    }

    private static class ContactPermissionRequest
            extends PermissionsRequest.PermissionsRequestBase {

        private final WeakReference<WizardPermissionsFragment> mFragmentWeakReference;

        ContactPermissionRequest(WizardPermissionsFragment fragment) {
            super(
                    PermissionsRequestCodes.CONTACTS.getRequestCode(),
                    Manifest.permission.READ_CONTACTS);
            mFragmentWeakReference = new WeakReference<>(fragment);
        }

        @Override
        public void onPermissionsGranted() {
            WizardPermissionsFragment fragment = mFragmentWeakReference.get();
            if (fragment == null) return;

            fragment.refreshWizardPager();
        }

        @Override
        public void onPermissionsDenied(
                @NonNull String[] grantedPermissions,
                @NonNull String[] deniedPermissions,
                @NonNull String[] declinedPermissions) {
            /*no-op - Main-Activity handles this case*/
        }
    }
}
