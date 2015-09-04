package com.anysoftkeyboard.ui.tutorials;

import android.net.Uri;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VersionChangeLogs {
    static List<VersionChangeLog> createChangeLog() {
        List<VersionChangeLog> log = new ArrayList<>();

        log.add(new VersionChangeLog(1, 6, "", Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestones/1.6"),
                "Next Words prediction is here! It learns from your typing (so, give it a little time to start suggesting).",
                "Localization update: "));

        log.add(new VersionChangeLog(1, 5, "r4", Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestones/v1.5_r4"),
                "Linguistics say 'Help' and 'help' are the same word. Completion will take care of that now. #TheCustomerAlwaysRight",
                "People complained about crashes. I fixed them. #TheCustomerAlwaysRight2",
                "Updating build tools - yes, it's that boring."));

        log.add(new VersionChangeLog(1, 5, "r3", Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestones/v1.5_r3"),
                "...and then pressing SHIFT changed symbols on the bottom row.",
                "Heard of some crashes hanging around in the keyboard. Crushed them!",
                "Not going the extra mile anymore - no longer suggesting words if the App said not to."));

        log.add(new VersionChangeLog(1, 5, "r2", Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestones/v1.5_r2"),
                "A few crashes gone missing with this release.",
                "An annoying bug went looking for the crashes and never seen again."));

        log.add(new VersionChangeLog(1, 5, "r1", Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestones/v1.5_r1"),
                "A few UI improvements.",
                "A few crashes evasions.",
                "Localization update: BE, MY, DE."));

        log.add(new VersionChangeLog(1, 5, "", Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestones/1.5"),
                "New and improved key preview, with tasty animations and stuff.",
                "Emoji History tab: your recently used emojis are closer than ever.",
                "Support for List-Quick-Text has been re-enabled.",
                "A few bug fixes.",
                "Localization update: CA, DE, ES, NL, RU."));

        log.add(new VersionChangeLog(1, 4, "r2", Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestones/1.4_r2"),
                "Even more crash fixes.",
                "Build system update.",
                "Localization update: RU, UK, DE."));

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
