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
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import com.anysoftkeyboard.android.PermissionRequestHelper;
import com.anysoftkeyboard.quicktextkeys.ui.QuickTextKeysBrowseFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.menny.android.anysoftkeyboard.R;
import net.evendanan.chauffeur.lib.FragmentChauffeurActivity;
import net.evendanan.chauffeur.lib.experiences.TransitionExperiences;
import net.evendanan.pixel.EdgeEffectHacker;
import pub.devrel.easypermissions.AfterPermissionGranted;

public class MainSettingsActivity extends FragmentChauffeurActivity {

    public static final String EXTRA_KEY_APP_SHORTCUT_ID = "shortcut_id";
    public static final String ACTION_REQUEST_PERMISSION_ACTIVITY =
            "ACTION_REQUEST_PERMISSION_ACTIVITY";
    public static final String EXTRA_KEY_ACTION_REQUEST_PERMISSION_ACTIVITY =
            "EXTRA_KEY_ACTION_REQUEST_PERMISSION_ACTIVITY";

    private CharSequence mTitle;
    private BottomNavigationView mBottomNavigationView;

    /**
     * Will set the title in the hosting Activity's title. Will only set the title if the fragment
     * is hosted by the Activity's manager, and not inner one.
     */
    public static void setActivityTitle(Fragment fragment, CharSequence title) {
        FragmentActivity activity = fragment.requireActivity();
        if (activity.getSupportFragmentManager() == fragment.getParentFragmentManager()) {
            activity.setTitle(title);
        }
    }

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main_ui);

        mTitle = getTitle();

        mBottomNavigationView = findViewById(R.id.bottom_navigation);

        mBottomNavigationView.setOnNavigationItemSelectedListener(
                item -> {
                    switch (item.getItemId()) {
                        case R.id.bottom_nav_home_button:
                            navigateToHomeRoot();
                            break;
                        case R.id.bottom_nav_language_button:
                            addFragmentToUi(
                                    new LanguageSettingsFragment(),
                                    TransitionExperiences.ROOT_FRAGMENT_EXPERIENCE_TRANSITION);
                            break;
                        case R.id.bottom_nav_ui_button:
                            addFragmentToUi(
                                    new UserInterfaceSettingsFragment(),
                                    TransitionExperiences.ROOT_FRAGMENT_EXPERIENCE_TRANSITION);
                            break;
                        case R.id.bottom_nav_gestures_button:
                            addFragmentToUi(
                                    new GesturesSettingsFragment(),
                                    TransitionExperiences.ROOT_FRAGMENT_EXPERIENCE_TRANSITION);
                            break;
                        case R.id.bottom_nav_quick_text_button:
                            addFragmentToUi(
                                    new QuickTextKeysBrowseFragment(),
                                    TransitionExperiences.ROOT_FRAGMENT_EXPERIENCE_TRANSITION);
                            break;
                        default:
                            throw new IllegalArgumentException(
                                    "Failed to handle "
                                            + item.getItemId()
                                            + " in mBottomNavigationView.setOnNavigationItemSelectedListener");
                    }
                    return true;
                });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // applying my very own Edge-Effect color
        EdgeEffectHacker.brandGlowEffect(this, ContextCompat.getColor(this, R.color.app_accent));

        handleAppShortcuts(getIntent());
        handlePermissionRequest(getIntent());
    }

    private void handlePermissionRequest(Intent intent) {
        if (intent != null
                && ACTION_REQUEST_PERMISSION_ACTIVITY.equals(intent.getAction())
                && intent.hasExtra(EXTRA_KEY_ACTION_REQUEST_PERMISSION_ACTIVITY)) {
            final String permission =
                    intent.getStringExtra(EXTRA_KEY_ACTION_REQUEST_PERMISSION_ACTIVITY);
            intent.removeExtra(EXTRA_KEY_ACTION_REQUEST_PERMISSION_ACTIVITY);
            if (permission.equals(Manifest.permission.READ_CONTACTS)) {
                startContactsPermissionRequest();
            } else {
                throw new IllegalArgumentException("Unknown permission request " + permission);
            }
        }
    }

    @AfterPermissionGranted(PermissionRequestHelper.CONTACTS_PERMISSION_REQUEST_CODE)
    public void startContactsPermissionRequest() {
        PermissionRequestHelper.check(
                this, PermissionRequestHelper.CONTACTS_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionRequestHelper.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    private void handleAppShortcuts(Intent intent) {
        if (intent != null
                && Intent.ACTION_VIEW.equals(intent.getAction())
                && intent.hasExtra(EXTRA_KEY_APP_SHORTCUT_ID)) {
            final String shortcutId = intent.getStringExtra(EXTRA_KEY_APP_SHORTCUT_ID);
            intent.removeExtra(EXTRA_KEY_APP_SHORTCUT_ID);

            switch (shortcutId) {
                case "keyboards":
                    mBottomNavigationView.setSelectedItemId(R.id.bottom_nav_language_button);
                    addFragmentToUi(
                            new KeyboardAddOnBrowserFragment(),
                            TransitionExperiences.DEEPER_EXPERIENCE_TRANSITION);
                    break;
                case "themes":
                    mBottomNavigationView.setSelectedItemId(R.id.bottom_nav_ui_button);
                    addFragmentToUi(
                            new KeyboardThemeSelectorFragment(),
                            TransitionExperiences.DEEPER_EXPERIENCE_TRANSITION);
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

    @Override
    protected int getFragmentRootUiElementId() {
        return R.id.main_ui_content;
    }

    @NonNull
    @Override
    protected Fragment createRootFragmentInstance() {
        return new MainFragment();
    }

    // side menu navigation methods

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    public void navigateToHomeRoot() {
        addFragmentToUi(
                createRootFragmentInstance(),
                TransitionExperiences.ROOT_FRAGMENT_EXPERIENCE_TRANSITION);
    }
}
