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
import android.graphics.Paint;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.anysoftkeyboard.ui.dev.DeveloperUtils;
import com.anysoftkeyboard.ui.settings.MainFragment;
import com.anysoftkeyboard.utils.Log;
import com.menny.android.anysoftkeyboard.BuildConfig;
import com.menny.android.anysoftkeyboard.R;

import net.evendanan.pushingpixels.FragmentChauffeurActivity;
import net.evendanan.pushingpixels.PassengerFragment;

public class ChangeLogFragment extends PassengerFragment {

    private static final String EXTRA_LOGS_TO_SHOW = "EXTRA_LOGS_TO_SHOW";

    public static final int SHOW_ALL_CHANGELOG = -1;
    public static final int SHOW_LATEST_CHANGELOG = -2;
    public static final int SHOW_UNVIEWED_CHANGELOG = -3;

    public static ChangeLogFragment createFragment(int logToShow) {
        ChangeLogFragment fragment = new ChangeLogFragment();
        Bundle b = createArgs(logToShow);
        fragment.setArguments(b);

        return fragment;
    }

    private static Bundle createArgs(int logToShow) {
        Bundle b = new Bundle();
        b.putInt(EXTRA_LOGS_TO_SHOW, logToShow);
        return b;
    }

    private static final String TAG = "ASK_CHANGELOG";

    private SharedPreferences mAppPrefs;

    private ViewGroup mLogContainer;

    private int mLogToShow = SHOW_ALL_CHANGELOG;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLogToShow = getArguments().getInt(EXTRA_LOGS_TO_SHOW);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(getMainLayout(), container, false);
    }

    protected int getMainLayout() {
        return R.layout.changelog;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LayoutInflater inflater = LayoutInflater.from(getActivity());

        Context appContext = getActivity().getApplicationContext();
        mLogContainer = (ViewGroup) view.findViewById(getLogItemsContainerId());

        mAppPrefs = PreferenceManager.getDefaultSharedPreferences(appContext);

        final boolean showAllLogs = mLogToShow != SHOW_UNVIEWED_CHANGELOG;
        //looking for logs to show
        Resources res = getResources();
        int currentVersionCode = 0;
        PackageInfo info = null;
        try {
            info = DeveloperUtils.getPackageInfo(appContext);
            currentVersionCode = info.versionCode;
        } catch (final NameNotFoundException e) {
            Log.e(TAG, "Failed to locate package information! This is very weird... I'm installed.");
        }
        while (info != null && currentVersionCode > 0) {
            final String layoutResourceName = "changelog_layout_" + currentVersionCode;
            Log.d(TAG, "Looking for changelog " + layoutResourceName);
            final int resId = res.getIdentifier(layoutResourceName, "layout", appContext.getPackageName());
            if (resId != 0) {
                if (showAllLogs || !mAppPrefs.getBoolean(layoutResourceName, false)) {
                    Log.d(TAG, "Got a changelog #" + currentVersionCode + " which is " + layoutResourceName);
                    View logEntry = inflater.inflate(resId, mLogContainer, false);
                    Object logTag = logEntry.getTag();
                    View logHeader = inflater.inflate(R.layout.changelogentry_header, mLogContainer, false);
                    TextView versionName = (TextView) logHeader.findViewById(R.id.changelog_version_title);
                    versionName.setPaintFlags(versionName.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                    updateEntryText(versionName, logTag, currentVersionCode, info, layoutResourceName);

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

    protected int getLogItemsContainerId() {
        return R.id.change_logs_container;
    }

    protected void updateEntryText(TextView entryHeader, Object tag, int versionCode, PackageInfo packageInfo, String layoutResourceName) {
        String versionName;
        if (tag == null) {
            if (!BuildConfig.DEBUG)
                throw new IllegalStateException("In RELEASE mode, all change log items must have a tag. Please include the version name in layout " + layoutResourceName);
            if (packageInfo.versionCode == versionCode)
                //automatically adding the version name
                versionName = packageInfo.versionName;
            else
                versionName = null;
        } else {
            versionName = tag.toString();
        }

        if (TextUtils.isEmpty(versionName)) {
            entryHeader.setText(getString(R.string.change_log_entry_header_template_without_name, versionCode));
        } else {
            entryHeader.setText(getString(R.string.change_log_entry_header_template, versionCode, versionName));
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().setTitle(getString(R.string.changelog));
    }

    public static class CardedChangeLogFragment extends ChangeLogFragment {
        public CardedChangeLogFragment() {
            setArguments(createArgs(ChangeLogFragment.SHOW_LATEST_CHANGELOG));
        }

        @Override
        protected int getLogItemsContainerId() {
            return R.id.card_with_read_more;
        }

        @Override
        protected int getMainLayout() { return R.layout.card_with_more_container; }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            ViewGroup container = (ViewGroup)view.findViewById(R.id.card_with_read_more);
            MainFragment.setupLink(container, R.id.read_more_link, new ClickableSpan() {
                @Override
                public void onClick(View v) {
                    FragmentChauffeurActivity activity = (FragmentChauffeurActivity) getActivity();
                    activity.addFragmentToUi(ChangeLogFragment.createFragment(ChangeLogFragment.SHOW_ALL_CHANGELOG),
                            FragmentChauffeurActivity.FragmentUiContext.ExpandedItem,
                            getView());
                }
            }, true);
        }

        @Override
        protected void updateEntryText(TextView entryHeader, Object tag, int versionCode, PackageInfo packageInfo, String layoutResourceName) {
            if (tag == null && !BuildConfig.DEBUG)
                throw new IllegalStateException("In RELEASE mode, all change log items must have a tag. Please include the version name in layout " + layoutResourceName);

            String cardedHeader = getString(R.string.change_log_card_title_template, versionCode, packageInfo.versionName);
            entryHeader.setText(cardedHeader);
        }
    }
}
