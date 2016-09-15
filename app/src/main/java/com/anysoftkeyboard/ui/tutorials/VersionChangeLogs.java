package com.anysoftkeyboard.ui.tutorials;

import android.net.Uri;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VersionChangeLogs {
    static List<VersionChangeLog> createChangeLog() {
        List<VersionChangeLog> log = new ArrayList<>();

        log.add(new VersionChangeLog(1, 8, "r5", Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestones/1.8-r5"),
                "Localization update: KN, KU, PT-BR."));

        log.add(new VersionChangeLog(1, 8, "r4", Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestones/1.8-r4"),
                "Updated Emoticon emoji listing - presentation is everything \uD83D\uDE0E!",
                "Also, flags \uD83C\uDFC1 were also added to the emoji list.",
                "Merged People and Gestures, and added a bunch \uD83D\uDE4B.",
                "Unfortunately - to make sure emojis are full supported - it is only available for Android 6.0 devices \uD83D\uDE22."));

        log.add(new VersionChangeLog(1, 8, "r3", Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestones/1.8-r3"),
                "New Dark-Gray theme.",
                "Bugs squashing, including an under-the-radar RTL issue.",
                "Localization update: SL."));

        log.add(new VersionChangeLog(1, 8, "r2", Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestones/1.8-r2"),
                "Better keyboard preview when selecting addons (themes, language, etc.).",
                "Now tells you about clipboard copy.",
                "A few bug fixes here and there.",
                "YABTU.",
                "Localization update: FR, SL."));

        log.add(new VersionChangeLog(1, 8, "r1", Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestones/1.8-r1"),
                "Improvements on the keyboard preview. Now demos typing.",
                "Several crash and bug fixes.",
                "Localization update: FR (100%)."));

        log.add(new VersionChangeLog(1, 8, "", Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestones/1.8"),
                "New keyboard preview - now showing EXACTLY how the keyboard should look.",
                "New launcher icon - first iteration. Ya ya, it's not perfect.",
                "Spellchecker fix, and other bug fixes.",
                "Build-tools update.",
                "Localization: DE (complete), SL (complete), MY."));

        log.add(new VersionChangeLog(1, 7, "r7", Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestones/1.7-r7"),
                "A few more bug fixes.",
                "Localization: FR, NL, NO, RU, UK."));

        log.add(new VersionChangeLog(1, 7, "r6", Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestones/1.7-r6"),
                "Status-bar icon fix. It is not a setting.",
                "Swipe from Backspace will delete a whole word for'ya.",
                "Update to the English auto-complete dictionary. Way overdue.."));

        log.add(new VersionChangeLog(1, 7, "r5", Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestones/1.7-r5"),
                "Punctuation and space swapping magic.",
                "Bug fixes all around."));

        log.add(new VersionChangeLog(1, 7, "r4", Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestones/1.7-r4"),
                "Fix for a weird bug with manually picked words, plus some crashers."));

        log.add(new VersionChangeLog(1, 7, "r3", Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestones/1.7-r3"),
                "Even more bugs crashed!",
                "The utility box (swipe up from space-bar) now has SELECT-ALL key. Pro tip: long-press that key and then use left&right arrows to precisely select characters.",
                "Talking about space-bar. Try double-spacing.",
                "And, yes, YABTU.",
                "Localization update: LT (100% complete!), KU."));

        log.add(new VersionChangeLog(1, 7, "r2", Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestones/1.7-r2"),
                "Bugs, bugs, bugs. Squashed.",
                "A better way to load auto-complete dictionary.",
                "Localization update: FR, LT, IT, KU."));

        log.add(new VersionChangeLog(1, 7, "r1", Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestones/1.7-r1"),
                "Marshmallow Permissions support - now we can annoy you with even more dialogs!",
                "Bugs squashing all around. Thanks for the reports!",
                "Localization update: DE, SL, FR, PT. And awesome note: German, Slovenian and French have been completely translated!",
                "YABTU"));

        log.add(new VersionChangeLog(1, 7, "", Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestones/1.7"),
                "Printing-out speed ups. This is very helpful to you 150-words-a-minute wizards.",
                "Physical-Keyboard interaction enhancements. This has Wife-Seal-of-Approval stamp on it.",
                "Automatically switch to previously used layout in an App. Probably good for multilingual users.",
                "And, fixed a few bugs (probably also introduced a few...)",
                "Localization update: PL, CA, MY, UK, DE, FR, SL.",
                "YABTU"));

        log.add(new VersionChangeLog(1, 6, "r3.1", Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestones/1.6-r3.1"),
                "Again, fixing a hard crash in a release. Next release will have a longer beta period."));

        log.add(new VersionChangeLog(1, 6, "r3", Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestones/1.6-r3"),
                "A few crash fixes.",
                "More than a few bugs (which I found on my own!) fixing",
                "A few more characters when long-pressing dash.",
                "YABTU.",
                "Localization update: KU, PL."));

        log.add(new VersionChangeLog(1, 6, "r2.1", Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestones/1.6-r2.1"),
                "Super annoying crash fixing."));

        log.add(new VersionChangeLog(1, 6, "r2", Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestones/1.6-r2"),
                "More about Clipboard: long-pressing the Paste key allows pasting from the past!",
                "Now supporting devices with non-standard touch support.",
                "Crashes, crashes, crashes... Gone.",
                "A few UI refinements.",
                "YABTU."));

        log.add(new VersionChangeLog(1, 6, "r1", Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestones/1.6-r1"),
                "Clipboard actions! Checkout the utility keyboard (swipe up from the space-bar).",
                "Small UI changes, too small to notice. But I'm happier.",
                "Bug squashing.",
                "YABTU.",
                "Localization update: tlh (Klingon), IW, and complete translation for DE (thanks goes to Nick Felsch)."));

        log.add(new VersionChangeLog(1, 6, "", Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestones/1.6"),
                "Next Words prediction is here! It learns from your typing (so, give it a little time to start suggesting).",
                "And, yes, previous line was auto-completed using Next Words prediction.",
                "You requested and someone did it: new Lean Light theme is here.",
                "I keep finding crashes, but then they magically go away.",
                "YABTU (Yet another build tools update).",
                "Localization update: TR, PL, DE."));

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
