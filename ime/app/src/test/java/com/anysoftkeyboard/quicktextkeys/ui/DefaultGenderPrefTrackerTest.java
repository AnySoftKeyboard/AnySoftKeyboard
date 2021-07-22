package com.anysoftkeyboard.quicktextkeys.ui;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.test.SharedPrefsHelper;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;
import emoji.utils.JavaEmojiUtils;
import java.util.HashSet;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class DefaultGenderPrefTrackerTest {

    @Test
    public void getDefaultGender() {
        DefaultGenderPrefTracker tracker =
                new DefaultGenderPrefTracker(AnyApplication.prefs(getApplicationContext()));

        // default value is null
        Assert.assertNull(tracker.getDefaultGender());

        final String[] values =
                getApplicationContext()
                        .getResources()
                        .getStringArray(R.array.settings_key_default_emoji_gender_values);
        Assert.assertNotNull(values);
        // random + person
        Assert.assertEquals(JavaEmojiUtils.Gender.values().length + 2, values.length);

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_default_emoji_gender, values[1]);
        Assert.assertEquals(JavaEmojiUtils.Gender.Woman, tracker.getDefaultGender());

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_default_emoji_gender, values[2]);
        Assert.assertEquals(JavaEmojiUtils.Gender.Man, tracker.getDefaultGender());

        SharedPrefsHelper.setPrefsValue(
                R.string.settings_key_default_emoji_gender, values[0] /*person*/);
        Assert.assertNull(tracker.getDefaultGender());

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_default_emoji_gender, values[1]);
        Assert.assertEquals(JavaEmojiUtils.Gender.Woman, tracker.getDefaultGender());

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_default_emoji_gender, "blah");
        // failing to person
        Assert.assertNull(tracker.getDefaultGender());

        SharedPrefsHelper.setPrefsValue(
                R.string.settings_key_default_emoji_gender, values[3] /*random*/);
        Set<JavaEmojiUtils.Gender> seen = new HashSet<>();
        for (int i = 0; i < 10000; i++) {
            final JavaEmojiUtils.Gender gender = tracker.getDefaultGender();
            Assert.assertNotNull(gender);
            seen.add(gender);
        }

        Assert.assertEquals(JavaEmojiUtils.Gender.values().length, seen.size());
    }

    @Test
    public void testDispose() {
        DefaultGenderPrefTracker tracker =
                new DefaultGenderPrefTracker(AnyApplication.prefs(getApplicationContext()));
        Assert.assertFalse(tracker.isDisposed());

        Assert.assertNull(tracker.getDefaultGender());
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_default_emoji_gender, "woman");
        Assert.assertEquals(JavaEmojiUtils.Gender.Woman, tracker.getDefaultGender());

        tracker.dispose();
        Assert.assertTrue(tracker.isDisposed());

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_default_emoji_gender, "man");
        // does not change
        Assert.assertEquals(JavaEmojiUtils.Gender.Woman, tracker.getDefaultGender());
    }
}
