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

package com.anysoftkeyboard.ui.tutorials;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.anysoftkeyboard.ui.dev.DeveloperUtils;
import com.anysoftkeyboard.ui.settings.*;
import com.anysoftkeyboard.utils.Log;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

public class ChangeLogActivity extends BaseTutorialActivity {

    public static final String EXTRA_SHOW_ALL_LOGS = "EXTRA_SHOW_ALL_LOGS";

    private static final String TAG = "ASK_CHANGELOG";

    private SharedPreferences mAppPrefs;

    private ViewGroup mLogContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLogContainer = (ViewGroup) findViewById(R.id.change_logs_container);

        mAppPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        final CheckBox showNotifications = (CheckBox) findViewById(R.id.show_notifications_next_time);
        showNotifications.setChecked(AnyApplication.getConfig().getShowVersionNotification());

        showNotifications.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                AnyApplication.getConfig().setShowVersionNotification(!AnyApplication.getConfig().getShowVersionNotification());
            }
        });

        //looking for logs to show
        final boolean showAllLogs = getIntent().getBooleanExtra(EXTRA_SHOW_ALL_LOGS, false);
        Resources res = getResources();
        int currentVersionCode = 0;
        try {
            final PackageInfo info = DeveloperUtils.getPackageInfo(getApplicationContext());
            currentVersionCode = info.versionCode;
        } catch (final NameNotFoundException e) {
            Log.e(TAG, "Failed to locate package information! This is very weird... I'm installed.");
        }
        while (currentVersionCode > 0) {
            final String layoutResourceName = "changelog_layout_" + currentVersionCode;
            Log.d(TAG, "Looking for changelog " + layoutResourceName);
            final int resId = res.getIdentifier(layoutResourceName, "layout", getPackageName());
            if (resId != 0) {
                if (showAllLogs || !mAppPrefs.getBoolean(layoutResourceName, false)) {
                    Log.d(TAG, "Got a changelog #" + currentVersionCode + " which is " + layoutResourceName);
                    View logEntry = getLayoutInflater().inflate(resId, null);
                    String logTag = logEntry.getTag().toString();
                    ViewGroup logHeader = (ViewGroup) getLayoutInflater().inflate(R.layout.changelogentry_header, null);
                    TextView versionName = (TextView) logHeader.findViewById(R.id.changelog_version_title);
                    versionName.setText(logTag + " - v" + currentVersionCode);
                    TextView versionUrl = (TextView) logHeader.findViewById(R.id.changelog_version_url);
                    versionUrl.setText("http://s.evendanan.net/ask_r" + currentVersionCode);

                    mLogContainer.addView(logHeader);
                    mLogContainer.addView(logEntry);

                    setClickHandler(logEntry);
                } else {
                    //if I've seen this that one, no need to continue with the loop
                    break;
                }
            }
            currentVersionCode--;
        }
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.changelog;
    }

    @Override
    protected int getTitleResId() {
        return R.string.changelog;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        /*
		case R.id.goto_settings_button:
			MainForm.startSettings(getApplicationContext());
			break;
		case R.id.market_search_button:
			try
			{
				MainForm.searchMarketForAddons(getApplicationContext(), "");
			}
			catch(Exception ex)
			{
				Log.e(TAG, "Failed to launch Market!", ex);
			}
			break;*/
            case R.id.settings_bottom_row_button:
                Intent bottomRowSettings = new Intent(this, BottomRowSelector.class);
                bottomRowSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(bottomRowSettings);
                break;
            case R.id.settings_top_row_button:
                Intent topRowSettings = new Intent(this, TopRowSelector.class);
                topRowSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(topRowSettings);
                break;
            case R.id.settings_keyboard_theme_button:
                Intent themeSelector = new Intent(this, KeyboardThemeSelector.class);
                startActivity(themeSelector);
                break;
            case R.id.settings_word_editor_button:
                Intent wordEditor = new Intent(this, UserDictionaryEditorActivity.class);
                startActivity(wordEditor);
                break;
            default:
                super.onClick(v);
                break;
        }
    }
}
