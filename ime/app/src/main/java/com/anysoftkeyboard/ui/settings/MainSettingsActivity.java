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

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;
import com.anysoftkeyboard.quicktextkeys.ui.QuickTextKeysBrowseFragment;
import com.menny.android.anysoftkeyboard.R;
import net.evendanan.chauffeur.lib.experiences.TransitionExperiences;

public class MainSettingsActivity extends BasicAnyActivity {

    public static final String EXTRA_KEY_APP_SHORTCUT_ID = "shortcut_id";

    private CharSequence mTitle;
    private BottomNavigationView mBottomNavigationView;

    @Override
    protected int getViewLayoutResourceId() {
        return R.layout.main_ui;
    }

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        mTitle = getTitle();

        mBottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);

        mBottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
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
                    }
                });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        handleAppShortcuts(getIntent());
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

    @NonNull
    @Override
    protected Fragment createRootFragmentInstance() {
        return new MainFragment();
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    // side menu navigation methods

    public void navigateToHomeRoot() {
        addFragmentToUi(
                createRootFragmentInstance(),
                TransitionExperiences.ROOT_FRAGMENT_EXPERIENCE_TRANSITION);
    }

    /**
     * Will set the title in the hosting Activity's title. Will only set the title if the fragment
     * is hosted by the Activity's manager, and not inner one.
     */
    public static void setActivityTitle(Fragment fragment, CharSequence title) {
        FragmentActivity activity = fragment.getActivity();
        if (activity.getSupportFragmentManager() == fragment.getFragmentManager()) {
            activity.setTitle(title);
        }
    }
}
