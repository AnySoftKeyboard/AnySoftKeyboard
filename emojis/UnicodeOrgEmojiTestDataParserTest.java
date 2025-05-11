package emojis;

import static org.junit.jupiter.api.Assertions.assertEquals;

import emojis.utils.JavaEmojiUtils;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class UnicodeOrgEmojiTestDataParserTest {
  /** Utility method to feed a string as input to the parse function. */
  List<EmojiData> parseFromString(String data) throws IOException {
    File tempFile = File.createTempFile("emoji-test", ".txt");
    tempFile.deleteOnExit();
    try (FileWriter writer = new FileWriter(tempFile)) {
      writer.write(data);
    }
    return UnicodeOrgEmojiTestDataParser.parse(tempFile);
  }

  @Test
  void testSimpleEmojis() throws IOException {
    String input =
        """
1F629                                                  ; fully-qualified     # 😩 E0.6 weary face
1F62B                                                  ; fully-qualified     # 😫 E0.6 tired face
1F971                                                  ; fully-qualified     # 🥱 E12.0 yawning face
""";

    List<EmojiData> result = parseFromString(input);

    assertEquals(3, result.size());
    assertEquals("😩", result.get(0).output);
    assertEquals(1, result.get(0).position);
    assertEquals("😫", result.get(1).output);
    assertEquals("🥱", result.get(2).output);
    assertEquals(3, result.get(2).position);
  }

  @Test
  void testParseQualifiedAndFully() throws IOException {
    String input =
        """
1F480                                                  ; fully-qualified     # 💀 E0.6 skull
2620 FE0F                                              ; fully-qualified     # ☠️ E1.0 skull and crossbones
2620                                                   ; unqualified         # ☠ E1.0 skull and crossbones
""";

    List<EmojiData> result = parseFromString(input);

    assertEquals(2, result.size());
    assertEquals("💀", result.get(0).output);
    assertEquals("☠️", result.get(1).output);
  }

  @Test
  void testGrouping() throws IOException {
    String input =
        """
# emoji-test.txt

# group: group1

# subgroup: group1-1
1F600                                                  ; fully-qualified     # 😀 E1.0 grinning face
1F603                                                  ; fully-qualified     # 😃 E0.6 grinning face with big eyes

# subgroup: group1-2
1F970                                                  ; fully-qualified     # 🥰 E11.0 smiling face with hearts

# group: group2
1F60B                                                  ; fully-qualified     # 😋 E0.6 face savoring food

# group: group3
1F61C                                                  ; fully-qualified     # 😜 E0.6 winking face with tongue
1F911                                                  ; fully-qualified     # 🤑 E1.0 money-mouth face

# subgroup: group3 1
1FAE1                                                  ; fully-qualified     # 🫡 E14.0 saluting face

""";

    List<EmojiData> result = parseFromString(input);

    assertEquals(7, result.size());
    assertEquals("😀", result.get(0).output);
    assertEquals(1, result.get(0).position);
    assertEquals("group1-group1-1", result.get(0).grouping);
    assertEquals("😃", result.get(1).output);
    assertEquals("group1-group1-1", result.get(1).grouping);
    assertEquals("🥰", result.get(2).output);
    assertEquals("group1-group1-2", result.get(2).grouping);
    assertEquals("😋", result.get(3).output);
    assertEquals("group2", result.get(3).grouping);
    assertEquals("😜", result.get(4).output);
    assertEquals("group3", result.get(4).grouping);
    assertEquals("🫡", result.get(6).output);
    assertEquals("group3-group3-1", result.get(6).grouping);
    assertEquals(7, result.get(6).position);
  }

  @Test
  void testSkinToneVariants() throws IOException {
    String input =
        """
270B                                                   ; fully-qualified     # ✋ E0.6 raised hand
270B 1F3FB                                             ; fully-qualified     # ✋🏻 E1.0 raised hand: light skin tone
270B 1F3FC                                             ; fully-qualified     # ✋🏼 E1.0 raised hand: medium-light skin tone
270B 1F3FD                                             ; fully-qualified     # ✋🏽 E1.0 raised hand: medium skin tone
270B 1F3FE                                             ; fully-qualified     # ✋🏾 E1.0 raised hand: medium-dark skin tone
270B 1F3FF                                             ; fully-qualified     # ✋🏿 E1.0 raised hand: dark skin tone
1F596                                                  ; fully-qualified     # 🖖 E1.0 vulcan salute
1F596 1F3FB                                            ; fully-qualified     # 🖖🏻 E1.0 vulcan salute: light skin tone
1F596 1F3FC                                            ; fully-qualified     # 🖖🏼 E1.0 vulcan salute: medium-light skin tone
1F596 1F3FF                                            ; fully-qualified     # 🖖🏿 E1.0 vulcan salute: dark skin tone
""";

    List<EmojiData> result = parseFromString(input);

    assertEquals(10, result.size());
    assertEquals("✋", result.get(0).output);
    assertEquals(0, result.get(0).getVariants().size());
    assertEquals("✋🏻", result.get(1).output);
    assertEquals("🖖🏿", result.get(9).output);
  }

  @Test
  void testTags() throws IOException {
    String input =
        """
270B                                                   ; fully-qualified     # ✋ E0.6 raised hand
270B 1F3FB                                             ; fully-qualified     # ✋🏻 E1.0 raised hand: light skin tone
270B 1F3FF                                             ; fully-qualified     # ✋🏿 E1.0 raised hand: dark skin tone
1F596                                                  ; fully-qualified     # 🖖 E1.0 vulcan salute
1F596 1F3FB                                            ; fully-qualified     # 🖖🏻 E1.0 vulcan salute: light skin tone
""";

    List<EmojiData> result = parseFromString(input);

    assertEquals(5, result.size());
    assertEquals("✋", result.get(0).output);
    assertEquals(Arrays.asList("raised", "hand", "raised_hand"), result.get(0).tags);
    assertEquals("✋🏻", result.get(1).output);
    assertEquals(Arrays.asList("raised", "hand", "raised_hand"), result.get(1).tags);
  }

  @Test
  void testSkinTones() throws IOException {
    String input =
        """
1F9CD                                                  ; fully-qualified     # 🧍 E12.0 person standing
1F9CD 1F3FB                                            ; fully-qualified     # 🧍🏻 E12.0 person standing: light skin tone
1F9CD 1F3FC                                            ; fully-qualified     # 🧍🏼 E12.0 person standing: medium-light skin tone
1F9CD 1F3FD                                            ; fully-qualified     # 🧍🏽 E12.0 person standing: medium skin tone
1F9CD 1F3FE                                            ; fully-qualified     # 🧍🏾 E12.0 person standing: medium-dark skin tone
1F9CD 1F3FF                                            ; fully-qualified     # 🧍🏿 E12.0 person standing: dark skin tone
""";

    List<EmojiData> result = parseFromString(input);

    assertEquals(Arrays.asList(), result.get(0).orderedSkinTones);
    assertEquals(
        Arrays.asList(JavaEmojiUtils.SkinTone.Fitzpatrick_2), result.get(1).orderedSkinTones);
    assertEquals(
        Arrays.asList(JavaEmojiUtils.SkinTone.Fitzpatrick_3), result.get(2).orderedSkinTones);
    assertEquals(
        Arrays.asList(JavaEmojiUtils.SkinTone.Fitzpatrick_4), result.get(3).orderedSkinTones);
    assertEquals(
        Arrays.asList(JavaEmojiUtils.SkinTone.Fitzpatrick_5), result.get(4).orderedSkinTones);
    assertEquals(
        Arrays.asList(JavaEmojiUtils.SkinTone.Fitzpatrick_6), result.get(5).orderedSkinTones);
  }

  @Test
  void testDescription() throws IOException {
    String input =
        """
270B                                                   ; fully-qualified     # ✋ E0.6 raised hand
270B 1F3FB                                             ; fully-qualified     # ✋🏻 E1.0 raised hand: light skin tone
""";

    List<EmojiData> result = parseFromString(input);

    assertEquals("✋", result.get(0).output);
    assertEquals("raised hand", result.get(0).description);
    assertEquals("raised hand", result.get(0).baseOutputDescription);
    assertEquals("✋🏻", result.get(1).output);
    assertEquals("raised hand: light skin tone", result.get(1).description);
    assertEquals("raised hand", result.get(1).baseOutputDescription);
  }
}
