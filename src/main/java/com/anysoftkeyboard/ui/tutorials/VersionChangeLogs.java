package com.anysoftkeyboard.ui.tutorials;

import android.net.Uri;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VersionChangeLogs {
	static List<VersionChangeLog> createChangeLog() {
		List<VersionChangeLog> log = new ArrayList<>();
		log.add(new VersionChangeLog(1, 4, "r2", Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestones/1.4_r2"),
				"Even more crash fixes.",
				"Build system update."));

		log.add(new VersionChangeLog(1, 4, "r1", Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestones/1.4_r1"),
				"Various crash fixes."));

		log.add(new VersionChangeLog(1, 4, "", Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestones/1.4"),
				"Crash fixes.",
				"Reduced APK size.",
				"More closely following http://semver.org/.",
				"Localization update: TH, RU."));

		log.add(new VersionChangeLog(1, 3, "20150402", Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestones/v140"),
				"Lots of additional Emojis. Enable them in Settings.",
				"Small UI fix for Emoji settings.",
				"Reduced APK size.",
				"Removed Tips popup.",
				"Localization update: RU, AR, ES, CA, NO, TR."));

		return log;
	}

	public static class VersionChangeLog {
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
}
