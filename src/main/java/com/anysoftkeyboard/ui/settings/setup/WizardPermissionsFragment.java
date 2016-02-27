package com.anysoftkeyboard.ui.settings.setup;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.SharedPreferencesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.anysoftkeyboard.PermissionsRequestCodes;
import com.anysoftkeyboard.ui.settings.MainSettingsActivity;
import com.anysoftkeyboard.utils.Log;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

public class WizardPermissionsFragment extends WizardPageBaseFragment implements View.OnClickListener {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.keyboard_setup_wizard_page_permissions_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.ask_for_permissions_action).setOnClickListener(this);
        view.findViewById(R.id.disable_contacts_dictionary).setOnClickListener(this);
        view.findViewById(R.id.open_permissions_wiki_action).setOnClickListener(this);
    }

    @Override
    protected boolean isStepCompleted() {
        return !AnyApplication.getConfig().useContactsDictionary() ||//either the user disabled Contacts
                ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;//or the user granted permission
    }

    @Override
    protected boolean isStepPreConditionDone() {
        return SetupSupport.isThisKeyboardSetAsDefaultIME(getActivity());
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("GGGG", "onPause");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("GGGG", "onResume");
    }

    @Override
    public void onClick(View v) {
        MainSettingsActivity activity = (MainSettingsActivity) getActivity();
        if (activity == null) return;

        switch (v.getId()) {
            case R.id.ask_for_permissions_action:
                activity.startPermissionsRequestAsActivity(PermissionsRequestCodes.CONTACTS.getRequestCode(), Manifest.permission.READ_CONTACTS);
                break;
            case R.id.disable_contacts_dictionary:
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
                SharedPreferencesCompat.EditorCompat.getInstance().apply(
                        sharedPreferences
                                .edit()
                                .putBoolean(getString(R.string.settings_key_use_contacts_dictionary), false)
                );
                refreshWizardPager();
                break;
            case R.id.open_permissions_wiki_action:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.permissions_wiki_site_url)));
                try {
                    startActivity(browserIntent);
                } catch (ActivityNotFoundException weirdException) {
                    //https://github.com/AnySoftKeyboard/AnySoftKeyboard/issues/516
                    //this means that there is nothing on the device
                    //that can handle Intent.ACTION_VIEW with "https" schema..
                    //silently swallowing it
                    Log.w("WizardPermissionsFragment", "Can not open '%' since there is nothing on the device that can handle it.", browserIntent.getData());
                }
                break;
        }
    }
}
