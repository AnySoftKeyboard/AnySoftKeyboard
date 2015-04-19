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
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.anysoftkeyboard.ui.settings.MainFragment;
import com.menny.android.anysoftkeyboard.R;

import net.evendanan.pushingpixels.FragmentChauffeurActivity;
import net.evendanan.pushingpixels.PassengerFragment;
import net.evendanan.pushingpixels.PassengerFragmentSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChangeLogFragment extends PassengerFragment {

	private static class VersionChangeLog {
		public final String versionName;
		public final String[] changes;
		public final Uri changesWebUrl;

		public VersionChangeLog(int major, int minor, String qualifier, Uri changesWebUrl, String... changes) {
			if (TextUtils.isEmpty(qualifier)) {
				this.versionName = String.format(Locale.US, "%d.%d", major, minor);
			} else {
				this.versionName = String.format(Locale.US, "%d.%d-%s", major, minor, qualifier);
			}

			this.changes = changes;
			this.changesWebUrl = changesWebUrl;
		}
	}

	private static final String EXTRA_LOGS_TO_SHOW = "EXTRA_LOGS_TO_SHOW";

	public static final int SHOW_ALL_CHANGELOG = -1;
	public static final int SHOW_LATEST_CHANGELOG = -2;

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

	private static List<VersionChangeLog> createChangeLog() {
		List<VersionChangeLog> log = new ArrayList<>();
		log.add(new VersionChangeLog(1, 4, "", Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestones/1.4"),
				"Crash fixes.",
				"Reduced APK size.",
				"More closely following http://semver.org/.",
				"Localization update: "));

		log.add(new VersionChangeLog(1, 3, "20150402", Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestones/v140"),
				"Lots of additional Emojis. Enable them in Settings.",
				"Small UI fix for Emoji settings.",
				"Reduced APK size.",
				"Removed Tips popup.",
				"Localization update: RU, AR, ES, CA, NO, TR."));

		return log;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		LayoutInflater inflater = LayoutInflater.from(getActivity());

		Context appContext = getActivity().getApplicationContext();
		mLogContainer = (ViewGroup) view.findViewById(getLogItemsContainerId());

		mAppPrefs = PreferenceManager.getDefaultSharedPreferences(appContext);

		for (VersionChangeLog change : createChangeLog()) {
			View logHeader = inflater.inflate(R.layout.changelogentry_header, mLogContainer, false);
			TextView versionName = (TextView) logHeader.findViewById(R.id.changelog_version_title);
			versionName.setPaintFlags(versionName.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
			setTitleText(versionName, change.versionName);

			mLogContainer.addView(logHeader);
			for (String changeEntry : change.changes) {
				TextView entryView = (TextView) inflater.inflate(R.layout.changelogentry_item, mLogContainer, false);
				entryView.setText("• "+changeEntry);
				mLogContainer.addView(entryView);
			}
			//TODO: add milestone url
			if (mLogToShow == SHOW_LATEST_CHANGELOG) break;//in this case, one is enough.
			//adding a divider between version
			mLogContainer.addView(inflater.inflate(R.layout.transparent_divider, mLogContainer, false));
		}
	}

	protected void setTitleText(TextView titleView, String versionName) {
		titleView.setText(getString(R.string.change_log_entry_header_template_without_name, versionName));
	}

	protected int getLogItemsContainerId() {
		return R.id.change_logs_container;
	}

	@Override
	public void onStart() {
		super.onStart();
		PassengerFragmentSupport.setActivityTitle(this, getString(R.string.changelog));
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
		protected int getMainLayout() {
			return R.layout.card_with_more_container;
		}

		@Override
		public void onViewCreated(View view, Bundle savedInstanceState) {
			super.onViewCreated(view, savedInstanceState);
			ViewGroup container = (ViewGroup) view.findViewById(R.id.card_with_read_more);
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
		protected void setTitleText(TextView titleView, String versionName) {
			titleView.setText(getString(R.string.change_log_card_title_template, versionName));
		}
	}
}
