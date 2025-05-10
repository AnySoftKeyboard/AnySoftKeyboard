package emojis;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;

class VariantDetectorTest {
  List<EmojiData> parseFromString(String data) throws IOException {
    File tempFile = File.createTempFile("emoji-test", ".txt");
    tempFile.deleteOnExit();
    try (FileWriter writer = new FileWriter(tempFile)) {
      writer.write(data);
    }
    return UnicodeOrgEmojiTestDataParser.parse(tempFile);
  }

  @Test
  public void testPersonDetectorHappyPath() throws Exception {
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
    var underTest = new PersonDetector();

    assertFalse(underTest.isVariant(result.get(0), result.get(0)));
    assertTrue(underTest.isVariant(result.get(0), result.get(1)));
    assertTrue(underTest.isVariant(result.get(0), result.get(2)));
    assertTrue(underTest.isVariant(result.get(0), result.get(3)));
    assertTrue(underTest.isVariant(result.get(0), result.get(4)));
    assertTrue(underTest.isVariant(result.get(0), result.get(5)));
    assertFalse(underTest.isVariant(result.get(0), result.get(6)));
    assertFalse(underTest.isVariant(result.get(0), result.get(7)));
    assertFalse(underTest.isVariant(result.get(0), result.get(8)));
    assertFalse(underTest.isVariant(result.get(0), result.get(9)));
    assertFalse(underTest.isVariant(result.get(0), result.get(0)));

    assertFalse(underTest.isVariant(result.get(6), result.get(1)));
    assertFalse(underTest.isVariant(result.get(6), result.get(2)));
    assertFalse(underTest.isVariant(result.get(6), result.get(3)));
    assertFalse(underTest.isVariant(result.get(6), result.get(4)));
    assertFalse(underTest.isVariant(result.get(6), result.get(5)));
    assertFalse(underTest.isVariant(result.get(6), result.get(6)));
    assertTrue(underTest.isVariant(result.get(6), result.get(7)));
    assertTrue(underTest.isVariant(result.get(6), result.get(8)));
    assertTrue(underTest.isVariant(result.get(6), result.get(9)));
  }

  @Test
  public void testPersonDetectorFoldedHandsIssue() throws Exception {
    String input =
        """
1F64F                                                  ; fully-qualified     # 🙏 E0.6 folded hands
1F64F 1F3FB                                            ; fully-qualified     # 🙏🏻 E1.0 folded hands: light skin tone
1F64F 1F3FC                                            ; fully-qualified     # 🙏🏼 E1.0 folded hands: medium-light skin tone
1F64F 1F3FD                                            ; fully-qualified     # 🙏🏽 E1.0 folded hands: medium skin tone
1F64F 1F3FE                                            ; fully-qualified     # 🙏🏾 E1.0 folded hands: medium-dark skin tone
1F64F 1F3FF                                            ; fully-qualified     # 🙏🏿 E1.0 folded hands: dark skin tone
""";

    List<EmojiData> result = parseFromString(input);
    var underTest = new PersonDetector();

    assertFalse(underTest.isVariant(result.get(0), result.get(0)));
    assertTrue(underTest.isVariant(result.get(0), result.get(1)));
    assertTrue(underTest.isVariant(result.get(0), result.get(2)));
    assertTrue(underTest.isVariant(result.get(0), result.get(3)));
    assertTrue(underTest.isVariant(result.get(0), result.get(4)));
    assertTrue(underTest.isVariant(result.get(0), result.get(5)));
  }
}
