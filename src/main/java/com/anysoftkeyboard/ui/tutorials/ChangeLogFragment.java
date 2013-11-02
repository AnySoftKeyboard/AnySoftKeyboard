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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.anysoftkeyboard.ui.dev.DeveloperUtils;
import com.anysoftkeyboard.utils.Log;
import com.menny.android.anysoftkeyboard.R;

import net.evendanan.pushingpixels.PassengerFragment;

public class ChangeLogFragment extends PassengerFragment {

    private static final String EXTRA_LOGS_TO_SHOW = "EXTRA_LOGS_TO_SHOW";
    private static final String EXTRA_SHOW_TITLE = "EXTRA_SHOW_TITLE";

    public static final int SHOW_ALL_CHANGELOG = -1;
    public static final int SHOW_LATEST_CHANGELOG = -2;
    public static final int SHOW_UNVIEWED_CHANGELOG = -3;

    public static ChangeLogFragment createFragment(int logToShow, boolean showTitle) {
        ChangeLogFragment fragment = new ChangeLogFragment();
        Bundle b = new Bundle();
        b.putInt(EXTRA_LOGS_TO_SHOW, logToShow);
        b.putBoolean(EXTRA_SHOW_TITLE, showTitle);

        fragment.setArguments(b);

        return fragment;
    }

    private static final String TAG = "ASK_CHANGELOG";

    private SharedPreferences mAppPrefs;

    private ViewGroup mLogContainer;

    private boolean mShowTitle = true;
    private int mLogToShow = SHOW_ALL_CHANGELOG;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mShowTitle = getArguments().getBoolean(EXTRA_SHOW_TITLE);
        mLogToShow = getArguments().getInt(EXTRA_LOGS_TO_SHOW);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.changelog, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        if (!mShowTitle) {
            view.findViewById(R.id.changelog_title_layout).setVisibility(View.GONE);
            ((FrameLayout)view.findViewById(R.id.change_log_content_frame)).setForeground(null);
        }

        Context appContext = getActivity().getApplicationContext();
        mLogContainer = (ViewGroup) view.findViewById(R.id.change_logs_container);

        mAppPrefs = PreferenceManager.getDefaultSharedPreferences(appContext);

        final boolean showAllLogs = mLogToShow != SHOW_UNVIEWED_CHANGELOG;
        //looking for logs to show
        Resources res = getResources();
        int currentVersionCode = 0;
        try {
            final PackageInfo info = DeveloperUtils.getPackageInfo(appContext);
            currentVersionCode = info.versionCode;
        } catch (final NameNotFoundException e) {
            Log.e(TAG, "Failed to locate package information! This is very weird... I'm installed.");
        }
        while (currentVersionCode > 0) {
            final String layoutResourceName = "changelog_layout_" + currentVersionCode;
            Log.d(TAG, "Looking for changelog " + layoutResourceName);
            final int resId = res.getIdentifier(layoutResourceName, "layout", appContext.getPackageName());
            if (resId != 0) {
                if (showAllLogs || !mAppPrefs.getBoolean(layoutResourceName, false)) {
                    Log.d(TAG, "Got a changelog #" + currentVersionCode + " which is " + layoutResourceName);
                    View logEntry = inflater.inflate(resId, mLogContainer, false);
                    String logTag = logEntry.getTag().toString();
                    ViewGroup logHeader = (ViewGroup) inflater.inflate(R.layout.changelogentry_header, mLogContainer, false);
                    TextView versionName = (TextView) logHeader.findViewById(R.id.changelog_version_title);
                    updateEntryText(versionName, logTag, currentVersionCode);

                    mLogContainer.addView(logHeader);
                    mLogContainer.addView(logEntry);
                    if (mLogToShow == SHOW_LATEST_CHANGELOG)
                        break;//in this case, one is enough.

                    mLogContainer.addView(inflater.inflate(R.layout.transparent_divider, mLogContainer, false));
                } else {
                    //if I've seen this that one, no need to continue with the loop
                    break;
                }
            }
            currentVersionCode--;
        }
    }

    protected void updateEntryText(TextView entryHeader, String versionName, int versionCode) {
        entryHeader.setText(getString(R.string.change_log_entry_header_template, versionCode, versionName));
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().setTitle(getString(R.string.changelog));
    }

    public static class CardedChangeLogFragment extends ChangeLogFragment {
        public CardedChangeLogFragment() {
            Bundle b = new Bundle();
            b.putBoolean(EXTRA_SHOW_TITLE, false);
            b.putInt(EXTRA_LOGS_TO_SHOW, ChangeLogFragment.SHOW_LATEST_CHANGELOG);
            setArguments(b);
        }

        protected void updateEntryText(TextView entryHeader, String versionName, int versionCode) {
            String cardedHeader = getString(R.string.change_log_card_title_template, versionCode, versionName);
            entryHeader.setText(cardedHeader);
        }
    }
}
