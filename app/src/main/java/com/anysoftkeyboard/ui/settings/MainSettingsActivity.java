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
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.SharedPreferencesCompat;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;
import android.view.View;

import com.anysoftkeyboard.PermissionsRequestCodes;
import com.anysoftkeyboard.quicktextkeys.ui.QuickTextKeysBrowseFragment;
import com.menny.android.anysoftkeyboard.R;

import net.evendanan.chauffeur.lib.experiences.TransitionExperiences;
import net.evendanan.chauffeur.lib.permissions.PermissionsFragmentChauffeurActivity;
import net.evendanan.chauffeur.lib.permissions.PermissionsRequest;
import net.evendanan.pushingpixels.EdgeEffectHacker;

import java.lang.ref.WeakReference;

public class MainSettingsActivity extends PermissionsFragmentChauffeurActivity {

    public static final String EXTRA_KEY_APP_SHORTCUT_ID = "shortcut_id";

    private CharSequence mTitle;
    private BottomNavigationView mBottomNavigationView;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main_ui);

        mTitle = getTitle();

        mBottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);

        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.bottom_nav_home_button:
                        navigateToHomeRoot();
                        break;
                    case R.id.bottom_nav_language_button:
                        addFragmentToUi(new LanguageSettingsFragment(), TransitionExperiences.ROOT_FRAGMENT_EXPERIENCE_TRANSITION);
                        break;
                    case R.id.bottom_nav_themes_button:
                        addFragmentToUi(new KeyboardThemeSelectorFragment(), TransitionExperiences.ROOT_FRAGMENT_EXPERIENCE_TRANSITION);
                        break;
                    case R.id.bottom_nav_gestures_button:
                        addFragmentToUi(new GesturesSettingsFragment(), TransitionExperiences.ROOT_FRAGMENT_EXPERIENCE_TRANSITION);
                        break;
                    case R.id.bottom_nav_quick_text_button:
                        addFragmentToUi(new QuickTextKeysBrowseFragment(), TransitionExperiences.ROOT_FRAGMENT_EXPERIENCE_TRANSITION);
                        break;
                }
                return true;
            }
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        //applying my very own Edge-Effect color
        EdgeEffectHacker.brandGlowEffect(this, ContextCompat.getColor(this, R.color.app_accent));
        handleAppShortcuts(getIntent());
    }

    private void handleAppShortcuts(Intent intent) {
        if (intent != null && Intent.ACTION_VIEW.equals(intent.getAction()) && intent.hasExtra(EXTRA_KEY_APP_SHORTCUT_ID)) {
            final String shortcutId = intent.getStringExtra(EXTRA_KEY_APP_SHORTCUT_ID);
            intent.removeExtra(EXTRA_KEY_APP_SHORTCUT_ID);

            switch (shortcutId) {
                case "keyboards":
                    mBottomNavigationView.setSelectedItemId(R.id.bottom_nav_language_button);
                    addFragmentToUi(new KeyboardAddOnBrowserFragment(), TransitionExperiences.DEEPER_EXPERIENCE_TRANSITION);
                    break;
                case "themes":
                    mBottomNavigationView.setSelectedItemId(R.id.bottom_nav_themes_button);
                    break;
                case "gestures":
                    mBottomNavigationView.setSelectedItemId(R.id.bottom_nav_gestures_button);
                    break;
                case "quick_keys":
                    mBottomNavigationView.setSelectedItemId(R.id.bottom_nav_quick_text_button);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown app-shortcut " + shortcutId);
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleAppShortcuts(intent);
    }

    @NonNull
    @Override
    protected Fragment createRootFragmentInstance() {
        return new MainFragment();
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

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    //side menu navigation methods

    public void navigateToHomeRoot() {
        mBottomNavigationView.setVisibility(View.VISIBLE);
        addFragmentToUi(createRootFragmentInstance(), TransitionExperiences.ROOT_FRAGMENT_EXPERIENCE_TRANSITION);
    }

    /**
     * Will set the title in the hosting Activity's title.
     * Will only set the title if the fragment is hosted by the Activity's manager, and not inner one.
     */
    public static void setActivityTitle(Fragment fragment, CharSequence title) {
        FragmentActivity activity = fragment.getActivity();
        if (activity.getSupportFragmentManager() == fragment.getFragmentManager()) {
            activity.setTitle(title);
        }
    }



    public static void setBottomNavVisibility(Fragment fragment, boolean visible) {
        FragmentActivity activity = fragment.getActivity();
        if (activity.getSupportFragmentManager() == fragment.getFragmentManager() && activity instanceof MainSettingsActivity) {
            ((MainSettingsActivity)activity).mBottomNavigationView.setVisibility(visible? View.VISIBLE : View.GONE);
        }
    }

    private final DialogInterface.OnClickListener mContactsDictionaryDialogListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, final int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainSettingsActivity.this, Manifest.permission.READ_CONTACTS)) {
                        startContactsPermissionRequest();
                    } else {
                        startAppPermissionsActivity();
                    }
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    final SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(getString(R.string.settings_key_use_contacts_dictionary), false);
                    SharedPreferencesCompat.EditorCompat.getInstance().apply(editor);
                    break;
            }
        }
    };

    private AlertDialog mAlertDialog;

    public void startContactsPermissionRequest() {
        startPermissionsRequest(new ContactPermissionRequest(this));
    }

    @NonNull
    protected PermissionsRequest createPermissionRequestFromIntentRequest(int requestId, @NonNull String[] permissions, @NonNull Intent intent) {
        if (requestId == PermissionsRequestCodes.CONTACTS.getRequestCode()) {
            return new ContactPermissionRequest(this);
        } else {
            return super.createPermissionRequestFromIntentRequest(requestId, permissions, intent);
        }
    }

    private static class ContactPermissionRequest extends PermissionsRequest.PermissionsRequestBase {

        private final WeakReference<MainSettingsActivity> mMainSettingsActivityWeakReference;

        public ContactPermissionRequest(MainSettingsActivity activity) {
            super(PermissionsRequestCodes.CONTACTS.getRequestCode(), Manifest.permission.READ_CONTACTS);
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
        public void onPermissionsDenied(@NonNull String[] grantedPermissions, @NonNull String[] deniedPermissions, @NonNull String[] declinedPermissions) {
            MainSettingsActivity activity = mMainSettingsActivityWeakReference.get();
            if (activity == null) return;
            //if the result is DENIED and the OS says "do not show rationale", it means the user has ticked "Don't ask me again".
            final boolean userSaysDontAskAgain = !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_CONTACTS);
            //the user has denied us from reading the Contacts information.
            //I'll ask them to whether they want to grant anyway, or disable ContactDictionary
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setCancelable(true);
            builder.setIcon(R.drawable.ic_notification_contacts_permission_required);
            builder.setTitle(R.string.notification_read_contacts_title);
            builder.setMessage(activity.getString(R.string.contacts_permissions_dialog_message));
            builder.setPositiveButton(activity.getString(userSaysDontAskAgain ? R.string.navigate_to_app_permissions : R.string.allow_permission), activity.mContactsDictionaryDialogListener);
            builder.setNegativeButton(activity.getString(R.string.turn_off_contacts_dictionary), activity.mContactsDictionaryDialogListener);

            if (activity.mAlertDialog != null && activity.mAlertDialog.isShowing())
                activity.mAlertDialog.dismiss();
            activity.mAlertDialog = builder.create();
            activity.mAlertDialog.show();
        }
    }
}
