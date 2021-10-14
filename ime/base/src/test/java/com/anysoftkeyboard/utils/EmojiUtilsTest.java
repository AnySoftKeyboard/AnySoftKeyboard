package com.anysoftkeyboard.utils;

import com.anysoftkeyboard.AnySoftKeyboardPlainTestRunner;
import emoji.utils.JavaEmojiUtils;
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
        Assert.assertFalse(
                EmojiUtils.containsSkinTone("\uD83D\uDC4D", JavaEmojiUtils.SkinTone.Fitzpatrick_2));
        Assert.assertTrue(
                EmojiUtils.containsSkinTone(
                        "\uD83D\uDC4D\uD83C\uDFFB", JavaEmojiUtils.SkinTone.Fitzpatrick_2));
        Assert.assertFalse(
                EmojiUtils.containsSkinTone(
                        "\uD83D\uDC4D\uD83C\uDFFC", JavaEmojiUtils.SkinTone.Fitzpatrick_2));

        Assert.assertFalse(
                EmojiUtils.containsSkinTone(
                        "\uD83D\uDC4D\uD83C", JavaEmojiUtils.SkinTone.Fitzpatrick_2));
        Assert.assertFalse(
                EmojiUtils.containsSkinTone("\uD83D", JavaEmojiUtils.SkinTone.Fitzpatrick_2));

        Assert.assertFalse(
                EmojiUtils.containsSkinTone("\uDFFB", JavaEmojiUtils.SkinTone.Fitzpatrick_2));
        Assert.assertFalse(EmojiUtils.containsSkinTone("", JavaEmojiUtils.SkinTone.Fitzpatrick_2));
    }

    @Test
    public void testContainsSkinToneGeneral() {
        Assert.assertFalse(JavaEmojiUtils.containsSkinTone("\uD83D\uDC4D"));
        Assert.assertTrue(JavaEmojiUtils.containsSkinTone("\uD83D\uDC4D\uD83C\uDFFB"));
        Assert.assertFalse(JavaEmojiUtils.containsSkinTone("h"));
    }

    @Test
    public void testContainsGenderGeneral() {
        Assert.assertFalse(JavaEmojiUtils.containsGender("\uD83E\uDDD4"));
        Assert.assertTrue(JavaEmojiUtils.containsGender("\uD83E\uDDD4\u200D♀"));
        Assert.assertTrue(JavaEmojiUtils.containsGender("\uD83E\uDDD4\uD83C\uDFFB\u200D♀"));
        Assert.assertFalse(JavaEmojiUtils.containsGender("\uD83E\uDDD4\uD83C\uDFFB"));
        Assert.assertFalse(JavaEmojiUtils.containsGender("h"));
    }

    @Test
    public void testGetAllSkinTones() {
        Assert.assertEquals(0, JavaEmojiUtils.getAllSkinTones("\uD83D\uDC4D").size());
        Assert.assertArrayEquals(
                new JavaEmojiUtils.SkinTone[] {JavaEmojiUtils.SkinTone.Fitzpatrick_2},
                JavaEmojiUtils.getAllSkinTones("\uD83D\uDC4D\uD83C\uDFFB").toArray());
    }

    @Test
    public void testGetAllGenders() {
        Assert.assertEquals(0, JavaEmojiUtils.getAllGenders("\uD83E\uDDD4").size());
        Assert.assertArrayEquals(
                new JavaEmojiUtils.Gender[] {JavaEmojiUtils.Gender.Woman},
                JavaEmojiUtils.getAllGenders("\uD83E\uDDD4\u200D♀").toArray());
    }

    @Test
    public void testRemoveSkinTone() {
        for (JavaEmojiUtils.SkinTone skinTone : JavaEmojiUtils.SkinTone.values()) {
            Assert.assertEquals(
                    "\uD83D\uDC4D", EmojiUtils.removeSkinTone("\uD83D\uDC4D", skinTone).toString());
        }

        Assert.assertEquals(
                "\uD83D\uDC75",
                EmojiUtils.removeSkinTone(
                                "\uD83D\uDC75\uD83C\uDFFE", JavaEmojiUtils.SkinTone.Fitzpatrick_5)
                        .toString());

        Assert.assertEquals(
                "\uD83D\uDC75\uD83C",
                EmojiUtils.removeSkinTone(
                                "\uD83D\uDC75\uD83C", JavaEmojiUtils.SkinTone.Fitzpatrick_5)
                        .toString());

        Assert.assertEquals(
                "\uD83D\uDC75",
                EmojiUtils.removeSkinTone(
                                "\uD83C\uDFFE\uD83D\uDC75", JavaEmojiUtils.SkinTone.Fitzpatrick_5)
                        .toString());
        Assert.assertEquals(
                "\uD83D\uDC75\uD83C\uDFFE",
                EmojiUtils.removeSkinTone(
                                "\uD83D\uDC75\uD83C\uDFFE", JavaEmojiUtils.SkinTone.Fitzpatrick_2)
                        .toString());
    }

    @Test
    public void testRemoveSkinToneGeneral() {
        Assert.assertEquals(
                "\uD83D\uDC4D", JavaEmojiUtils.removeSkinTones("\uD83D\uDC4D").toString());
        Assert.assertEquals(
                "\uD83D\uDC75",
                JavaEmojiUtils.removeSkinTones("\uD83D\uDC75\uD83C\uDFFE").toString());
        Assert.assertEquals(
                "\uD83E\uDDD4\u200D♂",
                JavaEmojiUtils.removeSkinTones("\uD83E\uDDD4\uD83C\uDFFB\u200D♂").toString());
    }

    @Test
    public void testRemoveGenderGeneral() {
        Assert.assertEquals(
                "\uD83D\uDC4D", JavaEmojiUtils.removeGenders("\uD83D\uDC4D").toString());
        Assert.assertEquals(
                "\uD83D\uDC75", JavaEmojiUtils.removeGenders("\uD83D\uDC75\u200D♂").toString());
        Assert.assertEquals(
                "\uD83E\uDDD4\uD83C\uDFFB",
                JavaEmojiUtils.removeGenders("\uD83E\uDDD4\uD83C\uDFFB\u200D♂").toString());
    }

    @Test
    public void testContainsGender() {
        Assert.assertFalse(EmojiUtils.containsGender("\uD83E\uDDD4", JavaEmojiUtils.Gender.Man));
        Assert.assertFalse(EmojiUtils.containsGender("\uD83E\uDDD4", JavaEmojiUtils.Gender.Woman));
        Assert.assertFalse(
                EmojiUtils.containsGender("\uD83E\uDDD4\uD83C\uDFFE", JavaEmojiUtils.Gender.Man));
        Assert.assertFalse(
                EmojiUtils.containsGender("\uD83E\uDDD4\uD83C\uDFFE", JavaEmojiUtils.Gender.Woman));
        Assert.assertFalse(
                EmojiUtils.containsGender(
                        "\uD83E\uDDD4\uD83C\uDFFB\u200D♀", JavaEmojiUtils.Gender.Man));
        Assert.assertTrue(
                EmojiUtils.containsGender(
                        "\uD83E\uDDD4\uD83C\uDFFB\u200D♀", JavaEmojiUtils.Gender.Woman));
        Assert.assertTrue(
                EmojiUtils.containsGender(
                        "\uD83E\uDDD4\uD83C\uDFFB\u200D♂", JavaEmojiUtils.Gender.Man));
        Assert.assertTrue(
                EmojiUtils.containsGender("\\uD83E\uDDD4\u200D♂️", JavaEmojiUtils.Gender.Man));
    }

    @Test
    public void testRemoveGender() {
        for (JavaEmojiUtils.Gender gender : JavaEmojiUtils.Gender.values()) {
            Assert.assertEquals(
                    "\uD83D\uDC4D", EmojiUtils.removeGender("\uD83D\uDC4D", gender).toString());
        }

        // woman-mini-qualified
        Assert.assertEquals(
                "\uD83E\uDDD4",
                EmojiUtils.removeGender("\uD83E\uDDD4\u200D♀", JavaEmojiUtils.Gender.Woman)
                        .toString());
        // woman-fully-qualified
        Assert.assertEquals(
                "\uD83E\uDDD4",
                EmojiUtils.removeGender("\uD83E\uDDD4\u200D♀️", JavaEmojiUtils.Gender.Woman)
                        .toString());
        // man-minimal-qualified-dark-skin
        Assert.assertEquals(
                "\uD83E\uDDD4\uD83C\uDFFF",
                EmojiUtils.removeGender(
                                "\uD83E\uDDD4\uD83C\uDFFF\u200D♂", JavaEmojiUtils.Gender.Man)
                        .toString());
        // man-fully-qualified-dark-skin
        Assert.assertEquals(
                "\uD83E\uDDD4\uD83C\uDFFF",
                EmojiUtils.removeGender(
                                "\uD83E\uDDD4\uD83C\uDFFF\u200D♂️", JavaEmojiUtils.Gender.Man)
                        .toString());
    }
}
