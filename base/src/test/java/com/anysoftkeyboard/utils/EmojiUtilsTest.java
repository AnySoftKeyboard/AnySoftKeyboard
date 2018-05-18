package com.anysoftkeyboard.utils;

import com.anysoftkeyboard.AnySoftKeyboardPlainTestRunner;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AnySoftKeyboardPlainTestRunner.class)
public class EmojiUtilsTest {

    @Test
    public void testIsLabelOfEmoji() {
        Assert.assertTrue(EmojiUtils.isLabelOfEmoji("\uD83D\uDC4D"));
        Assert.assertTrue(EmojiUtils.isLabelOfEmoji("\uD83D\uDC69\u200D\u2708\uFE0F"));

        Assert.assertFalse(EmojiUtils.isLabelOfEmoji("☺"));
        Assert.assertFalse(EmojiUtils.isLabelOfEmoji("A"));
    }

    @Test
    public void testContainsSkinTone() {
        Assert.assertFalse(EmojiUtils.containsSkinTone("\uD83D\uDC4D", EmojiUtils.SkinTone.Fitzpatrick_2));
        Assert.assertTrue(EmojiUtils.containsSkinTone("\uD83D\uDC4D\uD83C\uDFFB", EmojiUtils.SkinTone.Fitzpatrick_2));
        Assert.assertFalse(EmojiUtils.containsSkinTone("\uD83D\uDC4D\uD83C\uDFFC", EmojiUtils.SkinTone.Fitzpatrick_2));

        Assert.assertFalse(EmojiUtils.containsSkinTone("\uD83D\uDC4D\uD83C", EmojiUtils.SkinTone.Fitzpatrick_2));
        Assert.assertFalse(EmojiUtils.containsSkinTone("\uD83D", EmojiUtils.SkinTone.Fitzpatrick_2));

        Assert.assertFalse(EmojiUtils.containsSkinTone("\uDFFB", EmojiUtils.SkinTone.Fitzpatrick_2));
        Assert.assertFalse(EmojiUtils.containsSkinTone("", EmojiUtils.SkinTone.Fitzpatrick_2));
    }

    @Test
    public void testRemoveSkinTone() {
        for (EmojiUtils.SkinTone skinTone : EmojiUtils.SkinTone.values()) {
            Assert.assertEquals("\uD83D\uDC4D", EmojiUtils.removeSkinTone("\uD83D\uDC4D", skinTone));
        }

        Assert.assertEquals("\uD83D\uDC75", EmojiUtils.removeSkinTone("\uD83D\uDC75\uD83C\uDFFE", EmojiUtils.SkinTone.Fitzpatrick_5));

        Assert.assertEquals("\uD83D\uDC75\uD83C", EmojiUtils.removeSkinTone("\uD83D\uDC75\uD83C", EmojiUtils.SkinTone.Fitzpatrick_5));

        Assert.assertEquals("\uD83D\uDC75", EmojiUtils.removeSkinTone("\uD83C\uDFFE\uD83D\uDC75", EmojiUtils.SkinTone.Fitzpatrick_5));
        Assert.assertEquals("\uD83D\uDC75\uD83C\uDFFE", EmojiUtils.removeSkinTone("\uD83D\uDC75\uD83C\uDFFE", EmojiUtils.SkinTone.Fitzpatrick_2));
    }
}