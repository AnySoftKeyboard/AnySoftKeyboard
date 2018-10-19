package com.anysoftkeyboard.quicktextkeys.ui;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.test.SharedPrefsHelper;
import com.anysoftkeyboard.utils.EmojiUtils;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashSet;
import java.util.Set;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class DefaultSkinTonePrefTrackerTest {

    @Test
    public void getDefaultSkinTone() {
        DefaultSkinTonePrefTracker tracker = new DefaultSkinTonePrefTracker(AnyApplication.prefs(getApplicationContext()));

        //default value is null
        Assert.assertNull(tracker.getDefaultSkinTone());

        final String[] skinToneValues = getApplicationContext().getResources().getStringArray(R.array.settings_key_default_emoji_skin_tone_values);
        //random + generic
        Assert.assertEquals(EmojiUtils.SkinTone.values().length + 2, skinToneValues.length);

        Assert.assertNotNull(skinToneValues);
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_default_emoji_skin_tone, skinToneValues[1]);
        Assert.assertEquals(EmojiUtils.SkinTone.Fitzpatrick_2, tracker.getDefaultSkinTone());

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_default_emoji_skin_tone, skinToneValues[2]);
        Assert.assertEquals(EmojiUtils.SkinTone.Fitzpatrick_3, tracker.getDefaultSkinTone());

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_default_emoji_skin_tone, skinToneValues[3]);
        Assert.assertEquals(EmojiUtils.SkinTone.Fitzpatrick_4, tracker.getDefaultSkinTone());

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_default_emoji_skin_tone, skinToneValues[4]);
        Assert.assertEquals(EmojiUtils.SkinTone.Fitzpatrick_5, tracker.getDefaultSkinTone());

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_default_emoji_skin_tone, skinToneValues[5]);
        Assert.assertEquals(EmojiUtils.SkinTone.Fitzpatrick_6, tracker.getDefaultSkinTone());

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_default_emoji_skin_tone, skinToneValues[0]/*generic*/);
        Assert.assertNull(tracker.getDefaultSkinTone());

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_default_emoji_skin_tone, skinToneValues[5]);
        Assert.assertEquals(EmojiUtils.SkinTone.Fitzpatrick_6, tracker.getDefaultSkinTone());

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_default_emoji_skin_tone, "blah");
        //failing to generic
        Assert.assertNull(tracker.getDefaultSkinTone());

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_default_emoji_skin_tone, skinToneValues[6]/*random*/);
        Set<EmojiUtils.SkinTone> seen = new HashSet<>();
        for (int i=0; i<10000; i++) {
            final EmojiUtils.SkinTone skinTone = tracker.getDefaultSkinTone();
            Assert.assertNotNull(skinTone);
            seen.add(skinTone);
        }

        Assert.assertEquals(EmojiUtils.SkinTone.values().length, seen.size());
    }

    @Test
    public void testDispose() {
        DefaultSkinTonePrefTracker tracker = new DefaultSkinTonePrefTracker(AnyApplication.prefs(getApplicationContext()));
        Assert.assertFalse(tracker.isDisposed());

        Assert.assertNull(tracker.getDefaultSkinTone());
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_default_emoji_skin_tone, "type_2");
        Assert.assertEquals(EmojiUtils.SkinTone.Fitzpatrick_2, tracker.getDefaultSkinTone());

        tracker.dispose();
        Assert.assertTrue(tracker.isDisposed());

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_default_emoji_skin_tone, "type_3");
        //does not change
        Assert.assertEquals(EmojiUtils.SkinTone.Fitzpatrick_2, tracker.getDefaultSkinTone());
    }
}