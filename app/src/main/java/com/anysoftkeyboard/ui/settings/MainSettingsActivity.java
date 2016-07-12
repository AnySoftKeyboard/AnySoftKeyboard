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
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.SharedPreferencesCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.anysoftkeyboard.PermissionsRequestCodes;
import com.anysoftkeyboard.keyboards.KeyboardFactory;
import com.anysoftkeyboard.theme.KeyboardTheme;
import com.anysoftkeyboard.theme.KeyboardThemeFactory;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

import net.evendanan.chauffeur.lib.experiences.TransitionExperiences;
import net.evendanan.chauffeur.lib.permissions.PermissionsFragmentChauffeurActivity;
import net.evendanan.chauffeur.lib.permissions.PermissionsRequest;
import net.evendanan.pushingpixels.EdgeEffectHacker;

import java.lang.ref.WeakReference;

public class MainSettingsActivity extends PermissionsFragmentChauffeurActivity {

    private DrawerLayout mDrawerRootLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mTitle;
    private CharSequence mDrawerTitle;
    private SharedPreferences.OnSharedPreferenceChangeListener menuExtraUpdaterOnConfigChange = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            updateMenuExtraData();
        }
    };

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main_ui);

        mTitle = mDrawerTitle = getTitle();

        mDrawerRootLayout = (DrawerLayout) findViewById(R.id.main_root_layout);
        mDrawerRootLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.LEFT);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerRootLayout,         /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(mTitle);
                ActivityCompat.invalidateOptionsMenu(MainSettingsActivity.this);// creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(mDrawerTitle);
                ActivityCompat.invalidateOptionsMenu(MainSettingsActivity.this);// creates call to onPrepareOptionsMenu()
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerRootLayout.setDrawerListener(mDrawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        AnyApplication.getConfig().addChangedListener(menuExtraUpdaterOnConfigChange);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
        //applying my very own Edge-Effect color
        EdgeEffectHacker.brandGlowEffect(this, ContextCompat.getColor(this, R.color.app_accent));
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
    protected void onStart() {
        super.onStart();
        //updating menu's data
        updateMenuExtraData();
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
    protected void onDestroy() {
        super.onDestroy();
        AnyApplication.getConfig().removeChangedListener(menuExtraUpdaterOnConfigChange);
    }

    private void updateMenuExtraData() {
        TextView keyboardsData = (TextView) findViewById(R.id.keyboards_group_extra_data);
        final int all = KeyboardFactory.getAllAvailableKeyboards(getApplicationContext()).size();
        final int enabled = KeyboardFactory.getEnabledKeyboards(getApplicationContext()).size();
        keyboardsData.setText(getString(R.string.keyboards_group_extra_template, enabled, all));

        TextView themeData = (TextView) findViewById(R.id.theme_extra_data);
        KeyboardTheme theme = KeyboardThemeFactory.getCurrentKeyboardTheme(getApplicationContext());
        if (theme == null)
            theme = KeyboardThemeFactory.getFallbackTheme(getApplicationContext());
        themeData.setText(getString(R.string.selected_add_on_summary, theme.getName()));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    //side menu navigation methods

    public void onNavigateToRootClicked(View v) {
        mDrawerRootLayout.closeDrawers();
        addFragmentToUi(createRootFragmentInstance(), TransitionExperiences.ROOT_FRAGMENT_EXPERIENCE_TRANSITION);
    }

    public void onNavigateToKeyboardAddonSettings(View v) {
        mDrawerRootLayout.closeDrawers();
        addFragmentToUi(new KeyboardAddOnBrowserFragment(), TransitionExperiences.SUB_ROOT_FRAGMENT_EXPERIENCE_TRANSITION);
    }

    public void onNavigateToDictionarySettings(View v) {
        mDrawerRootLayout.closeDrawers();
        addFragmentToUi(new DictionariesFragment(), TransitionExperiences.SUB_ROOT_FRAGMENT_EXPERIENCE_TRANSITION);
    }

    public void onNavigateToLanguageSettings(View v) {
        mDrawerRootLayout.closeDrawers();
        addFragmentToUi(new AdditionalLanguageSettingsFragment(), TransitionExperiences.SUB_ROOT_FRAGMENT_EXPERIENCE_TRANSITION);

    }

    public void onNavigateToKeyboardThemeSettings(View v) {
        mDrawerRootLayout.closeDrawers();
        addFragmentToUi(new KeyboardThemeSelectorFragment(), TransitionExperiences.SUB_ROOT_FRAGMENT_EXPERIENCE_TRANSITION);
    }

    public void onNavigateToEffectsSettings(View v) {
        mDrawerRootLayout.closeDrawers();
        addFragmentToUi(new EffectsSettingsFragment(), TransitionExperiences.SUB_ROOT_FRAGMENT_EXPERIENCE_TRANSITION);
    }

    public void onNavigateToGestureSettings(View v) {
        mDrawerRootLayout.closeDrawers();
        addFragmentToUi(new GesturesSettingsFragment(), TransitionExperiences.SUB_ROOT_FRAGMENT_EXPERIENCE_TRANSITION);
    }

    public void onNavigateToQuickTextSettings(View v) {
        mDrawerRootLayout.closeDrawers();
        addFragmentToUi(new QuickTextSettingsFragment(), TransitionExperiences.SUB_ROOT_FRAGMENT_EXPERIENCE_TRANSITION);
    }

    public void onNavigateToUserInterfaceSettings(View v) {
        mDrawerRootLayout.closeDrawers();
        addFragmentToUi(new AdditionalUiSettingsFragment(), TransitionExperiences.SUB_ROOT_FRAGMENT_EXPERIENCE_TRANSITION);
    }

    public void onNavigateToAboutClicked(View v) {
        mDrawerRootLayout.closeDrawers();
        addFragmentToUi(new AboutAnySoftKeyboardFragment(), TransitionExperiences.SUB_ROOT_FRAGMENT_EXPERIENCE_TRANSITION);
    }

    public void setFullScreen(boolean fullScreen) {
        if (fullScreen) {
            getSupportActionBar().hide();
            mDrawerRootLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        } else {
            getSupportActionBar().show();
            mDrawerRootLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        }
    }

    public void openDrawer() {
        mDrawerRootLayout.openDrawer(Gravity.LEFT);
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
                    SharedPreferencesCompat.EditorCompat.getInstance().apply(
                            sharedPreferences
                                    .edit()
                                    .putBoolean(getString(R.string.settings_key_use_contacts_dictionary), false)
                    );
                    break;
            }
        }
    };

    private AlertDialog mAlertDialog;

    public void startContactsPermissionRequest() {
        startPermissionsRequest(new ContactPermissionRequest(this));
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

            if (activity.mAlertDialog != null && activity.mAlertDialog.isShowing()) activity.mAlertDialog.dismiss();
            activity.mAlertDialog = builder.create();
            activity.mAlertDialog.show();
        }
    }
}
