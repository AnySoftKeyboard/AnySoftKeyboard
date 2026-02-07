package com.anysoftkeyboard.releaseinfo;

import android.net.Uri;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VersionChangeLogs {
  static List<VersionChangeLog> createChangeLog() {
    final List<VersionChangeLog> log = new ArrayList<>();

    log.add(
        new VersionChangeLog(
            1,
            13,
            "",
            Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestone/95"),
            "Improved gesture-typing memory management - fewer crashes, better performance.",
            "Improved gesture-typing accuracy.",
            "Support for 16 KB memory page sizes (Android 15 requirement).",
            "Minimum Android version is 6.0 (Marshmallow, API level 23).",
            "Updated emoji data for Android 15+.",
            "Fixed emoji keyboard crash.",
            "Better edge-to-edge display support for modern Android versions.",
            "Various stability improvements and bug fixes.",
            "YABTU.",
            "Updated translations from the community (at crowdin.net)."));
    log.add(
        new VersionChangeLog(
            1,
            12,
            "",
            Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestone/94"),
            "Support for Android 15 (API 35).",
            "Improved clipboard support.",
            "Several fixes to the settings app navigation.",
            "Vibration fixes.",
            "Suggestions pick and order fixes.",
            "Improved pop-up keys order.",
            "Gesture-typing supports user-dictionary.",
            "Support for direct-boot devices.",
            "Reduced installation size (for supporting devices).",
            "Updated translations from the community (at crowdin.net)."));
    log.add(
        new VersionChangeLog(
            1,
            11,
            "r1",
            Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestone/93"),
            "Minimum Android version is 4.0.3 (ICS, API level 15).",
            "Basic support for OS field auto-fill.",
            "Better vibration control for newer OS versions.",
            "Fixes around permission requests.",
            "Fixes for colorized nav-bar.",
            "A few small gesture-typing fixes.",
            "Other bug fixes.",
            "Updated translations from the community."));
    log.add(
        new VersionChangeLog(
            1,
            10,
            "r4",
            Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestone/92"),
            "Keep safe! #covid19 \uD83D\uDE37",
            "Updated emoji to version 13.1 - Android 8.1+",
            "So, you just copied some text? Let me suggest pasting it.",
            "Wrapping selected text with \"'<>(){}[]*-_`~.",
            "Finally, you can decide where the settings backup file should be.",
            "People spotted bugs. We slayed them!",
            "YABTU.",
            "Updated translations from the community."));
    log.add(
        new VersionChangeLog(
            1,
            10,
            "r3",
            Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestone/91"),
            "Colorized nav-bar on supported devices.",
            "Gesture-Typing fixes and improvements.",
            "Various fixes around theme setting.",
            "Updated build tools.",
            "Updated translations."));

    log.add(
        new VersionChangeLog(
            1,
            10,
            "r2",
            Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestone/90"),
            "Support for image/gif insertion. This is an option in the Emoji popup.",
            "Update Emojis to v12.0.",
            "Improvements for Gesture-Typing. Thanks to Philipp Fischbeck.",
            "New themes.",
            "Updated localization: AR, DE, ES-MX, EU, KMR, LT, RU, TR, UK"));

    log.add(
        new VersionChangeLog(
            1,
            10,
            "r1",
            Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestone/89"),
            "Keyboard colors will adapt to used app. Enable this in Themes settings.",
            "Night Mode - if enabled, keyboard and app will use dark theme.",
            "Power Saving mode tweaks.",
            "Updated build-tools (smaller binary now).",
            "Halmak layout for English.",
            "Updated localization: AR, BE, BG, DE, EO, EU, ES, ES-MX, FR, IT, IW (HE),"
                + " KMR, KU, NB, PT, SC, SK, TR."));

    log.add(
        new VersionChangeLog(
            1,
            9,
            "r6",
            Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestone/88"),
            "➿ Gesture-Typing is here! As BETA. You'll need to enable it in Settings if"
                + " you want to try it out.",
            "Honoring IME_FLAG_NO_PERSONALIZED_LEARNING and PASSWORD as Incognito - if"
                + " an app ask us not to remember stuff, we'll do it.",
            "Updated localization: BE, CKB, EU, FR, IT, NB, SC."));

    log.add(
        new VersionChangeLog(
            1,
            9,
            "r5",
            Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestone/87"),
            "Power-Saving mode improvements - you can pick which features to include in"
                + " Power-Saving.",
            "Also, we allow switching to dark, simple theme in Power-Saving mode. But"
                + " this is optional.",
            "New Workman layout, Terminal generic-top-row and long-press fixes. Done by"
                + " Alex Griffin.",
            "Updated localization: AR, BE, EU, FR, HU, IT, KA, KN, KU, LT, NB, NL, PT,"
                + " RO, RU, SC, UK."));

    log.add(
        new VersionChangeLog(
            1,
            9,
            "r4",
            Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestone/86"),
            "Power-Saving mode - when battery is low, we will not do animations or"
                + " dictionary look ups.",
            "A few UI/UX changes.",
            "A few bug fixes.",
            "Updated localization: CA, IT, RO."));

    log.add(
        new VersionChangeLog(
            1,
            9,
            "r3",
            Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestone/85"),
            "You can now set the default skin-tone for emojis #expressYourself. Android" + " 7+.",
            "Clipboard is now synced with outside changes. Long-press PASTE for list.",
            "Various bug fixes.",
            "Updated localization: BE, CA, EU, LT, NB, RU, SC, TR, UK."));

    log.add(
        new VersionChangeLog(
            1,
            9,
            "r2",
            Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestone/84"),
            "Completely rewrote backup and restore mechanism. Now you can backup words,"
                + " abbr, next-word and all-app settings.",
            "New Kaomoji emoji group.",
            "Pressing shift will change caps of selected text.",
            "A few bug fixes.",
            "Updated localization: AR, BE, BG, DU, EU, FI, FIL, FR, HU, KA, KMR, KU,"
                + " LT, NB, PT-rBR, RU, SC, TR, UK."));

    log.add(
        new VersionChangeLog(
            1,
            9,
            "r1",
            Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestone/83"),
            "New Settings UI bottom navigation.",
            "Also, new Setup Wizard UI.",
            "And many new settings all around.",
            "Localization update: AR, BE, pt-rBR, ES, EU, FR, FIL, HU, KU, LT, MY, SC,"
                + " TR, RU, UK.",
            "YABTU and also will only work with Android 2.2 \uD83C\uDF6A or newer."));

    log.add(
        new VersionChangeLog(
            1,
            8,
            "r12",
            Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestone/82"),
            "Fine-tuned icon to match Android's design.",
            "Updated emojis for Android-O.",
            "Long-pressing SHIFT will lock. Or unlock..",
            "Incognito Mode! Long-press ENTER to start.",
            "YABTU",
            "Localization update: FR, EU, NL, IW."));

    log.add(
        new VersionChangeLog(
            1,
            8,
            "r11",
            Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestone/81"),
            "All languages are equal! You can now reorder keyboards. In keyboard"
                + " selection page - long press an enabled keyboard and drag it.",
            "Now you can use multiple dictionaries on a given keyboard. Long-press"
                + " ENTER for options.",
            "Brought alternative layouts: DVORAK, COLEMAK, Terminal, and Compact" + " English.",
            "New theme by Algimantas",
            "YABTU",
            "Localization update: too many to mention."));

    log.add(
        new VersionChangeLog(
            1,
            8,
            "r10",
            Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestones/1.8-r10"),
            "Some bug fixes.",
            "In tweaks: You can now force a locale inside AnySoftKeyboard.",
            "YABTU",
            "Localization update: BE, DE, CA, EU, FR."));

    log.add(
        new VersionChangeLog(
            1,
            8,
            "r9",
            Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestones/1.8-r9"),
            "New Initial Setup Wizard. For clearer activation.",
            "Seems like we did not include words with accents in suggestions. We do" + " now!",
            "Better Emoji-Search experience. Type a : to start searching.",
            "Yet more bugs squashed.",
            "Localization update: BE, NL, FR, SL, DE, NO, EU."));

    log.add(
        new VersionChangeLog(
            1,
            8,
            "r8",
            Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestones/1.8-r8"),
            "Much improved English dictionary.",
            "More than a few bug fixes.",
            "Localization update: FR, SL, BE and all new EU (Basque)."));

    log.add(
        new VersionChangeLog(
            1,
            8,
            "r7.1",
            Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestone/78"),
            "Bringing back missing Alphabet keys from some layouts.",
            "Getting the hint if you say no to contacts."));

    log.add(
        new VersionChangeLog(
            1,
            8,
            "r7",
            Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestones/1.8-r7"),
            "Language key will now show only if you actually have more than one layout"
                + " enabled.",
            "For Android 7.1 - you now have Launcher shortcuts.",
            "A few changes to the dictionary loading mechanism.",
            "Some bug fixes.",
            "Emojis are no longer popups, but their own keyboard. Plus, long press to"
                + " get various skin tones (if available).",
            "YABTU",
            "Localization update: BE, UK, FR, TR, and SL."));

    log.add(
        new VersionChangeLog(
            1,
            8,
            "r6",
            Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestones/1.8-r6"),
            "Quite a few bug fixes, some related to shift states.",
            "Some English dictionary tuning.",
            "Now showing Greek alphabets in long-press popups.",
            "Localization update: FR, BE."));

    log.add(
        new VersionChangeLog(
            1,
            8,
            "r5",
            Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestones/1.8-r5"),
            "Many bug fixes",
            "Now you can \uD83D\uDD0D for emojis! Just start your search with a colon,"
                + " like this - ':magnifying",
            "Long-press an emoji to find out its assigned tags.",
            "A massive rewrite of long-press interactions. I hope I didn't break"
                + " anything (major).",
            "When in a password field, numbers will show up at the top of the keyboard,"
                + " because strong passwords and such.",
            "Also, pressing SHIFT, will switch the numbers-row to symbols, because"
                + " strong passwords and such.",
            "YABTU.",
            "Localization update: KN, KU, PT-BR."));

    log.add(
        new VersionChangeLog(
            1,
            8,
            "r4",
            Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestones/1.8-r4"),
            "Updated Emoticon emoji listing - presentation is everything \uD83D\uDE0E!",
            "Also, flags \uD83C\uDFC1 were also added to the emoji list.",
            "Merged People and Gestures, and added a bunch \uD83D\uDE4B.",
            "Unfortunately - to make sure emojis are full supported - it is only"
                + " available for Android 6.0 devices \uD83D\uDE22."));

    log.add(
        new VersionChangeLog(
            1,
            8,
            "r3",
            Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestones/1.8-r3"),
            "New Dark-Gray theme.",
            "Bugs squashing, including an under-the-radar RTL issue.",
            "Localization update: SL."));

    log.add(
        new VersionChangeLog(
            1,
            8,
            "r2",
            Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestones/1.8-r2"),
            "Better keyboard preview when selecting addons (themes, language, etc.).",
            "Now tells you about clipboard copy.",
            "A few bug fixes here and there.",
            "YABTU.",
            "Localization update: FR, SL."));

    log.add(
        new VersionChangeLog(
            1,
            8,
            "r1",
            Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestones/1.8-r1"),
            "Improvements on the keyboard preview. Now demos typing.",
            "Several crash and bug fixes.",
            "Localization update: FR (100%)."));

    log.add(
        new VersionChangeLog(
            1,
            8,
            "",
            Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestones/1.8"),
            "New keyboard preview - now showing EXACTLY how the keyboard should look.",
            "New launcher icon - first iteration. Ya ya, it's not perfect.",
            "Spellchecker fix, and other bug fixes.",
            "Build-tools update.",
            "Localization: DE (complete), SL (complete), MY."));

    log.add(
        new VersionChangeLog(
            1,
            7,
            "r7",
            Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestones/1.7-r7"),
            "A few more bug fixes.",
            "Localization: FR, NL, NO, RU, UK."));

    log.add(
        new VersionChangeLog(
            1,
            7,
            "r6",
            Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestones/1.7-r6"),
            "Status-bar icon fix. It is not a setting.",
            "Swipe from Backspace will delete a whole word for'ya.",
            "Update to the English auto-complete dictionary. Way overdue.."));

    log.add(
        new VersionChangeLog(
            1,
            7,
            "r5",
            Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestones/1.7-r5"),
            "Punctuation and space swapping magic.",
            "Bug fixes all around."));

    log.add(
        new VersionChangeLog(
            1,
            7,
            "r4",
            Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestones/1.7-r4"),
            "Fix for a weird bug with manually picked words, plus some crashers."));

    log.add(
        new VersionChangeLog(
            1,
            7,
            "r3",
            Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestones/1.7-r3"),
            "Even more bugs crashed!",
            "The utility box (swipe up from space-bar) now has SELECT-ALL key. Pro tip:"
                + " long-press that key and then use left&right arrows to precisely"
                + " select characters.",
            "Talking about space-bar. Try double-spacing.",
            "And, yes, YABTU.",
            "Localization update: LT (100% complete!), KU."));

    log.add(
        new VersionChangeLog(
            1,
            7,
            "r2",
            Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestones/1.7-r2"),
            "Bugs, bugs, bugs. Squashed.",
            "A better way to load auto-complete dictionary.",
            "Localization update: FR, LT, IT, KU."));

    log.add(
        new VersionChangeLog(
            1,
            7,
            "r1",
            Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestones/1.7-r1"),
            "Marshmallow Permissions support - now we can annoy you with even more" + " dialogs!",
            "Bugs squashing all around. Thanks for the reports!",
            "Localization update: DE, SL, FR, PT. And awesome note: German, Slovenian"
                + " and French have been completely translated!",
            "YABTU"));

    log.add(
        new VersionChangeLog(
            1,
            7,
            "",
            Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestones/1.7"),
            "Printing-out speed ups. This is very helpful to you 150-words-a-minute" + " wizards.",
            "Physical-Keyboard interaction enhancements. This has Wife-Seal-of-Approval"
                + " stamp on it.",
            "Automatically switch to previously used layout in an App. Probably good"
                + " for multilingual users.",
            "And, fixed a few bugs (probably also introduced a few...)",
            "Localization update: PL, CA, MY, UK, DE, FR, SL.",
            "YABTU"));
    return log;
  }

  public static class VersionChangeLog {
    public final String versionName;
    public final String[] changes;
    public final Uri changesWebUrl;

    public VersionChangeLog(
        int major, int minor, String qualifier, Uri changesWebUrl, String... changes) {
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
