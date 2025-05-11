package emojis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class EmojiKeyboardCreatorTest {
  @Test
  void testBuildKeyboardFileCreatesXmlFile() throws Exception {
    var folder = Files.createTempDirectory("emoji_test").toFile();
    var fakeCollection =
        new EmojiCollection() {
          @Override
          public String getResourceFileName() {
            return "test_keyboard.xml";
          }

          @Override
          public String getKeyboardId() {
            return "";
          }

          @Override
          public String getNameResId() {
            return "";
          }

          @Override
          public String getIconResId() {
            return "";
          }

          @Override
          public String getLabelResId() {
            return "";
          }

          @Override
          public String getDefaultOutputResId() {
            return "";
          }

          @Override
          public String getDescription() {
            return "";
          }

          @Override
          public List<EmojiData> generateOwnedEmojis() {
            return Arrays.asList(
                new EmojiData(
                    0,
                    "desc",
                    "baseDesc",
                    "group",
                    "\uD83D\uDE00",
                    Collections.singletonList("smile")),
                new EmojiData(
                    1,
                    "desc2",
                    "baseDesc2",
                    "group",
                    "\uD83D\uDE01",
                    Arrays.asList("sad", "person")),
                new EmojiData(
                    2, "desc3", "baseDesc3", "group", "\uD83D\uDE02", Collections.emptyList()));
          }
        };
    var creator = new EmojiKeyboardCreator(folder, fakeCollection);

    creator.buildKeyboardFile();

    var expectedFile = new File(folder, "test_keyboard.xml");
    assertTrue(expectedFile.exists(), "Keyboard XML file should be created");

    var fileContent = new String(Files.readAllBytes(expectedFile.toPath())).trim();
    var expectedContents =
        new String(
                Files.readAllBytes(new File("emojis/fixtures/expected_test_keyboard.xml").toPath()))
            .trim();
    assertEquals(expectedContents, fileContent);
  }

  @Test
  void testBuildKeyboardWithPopUp() throws Exception {
    var folder = Files.createTempDirectory("emoji_test").toFile();
    var fakeCollection =
        new EmojiCollection() {
          @Override
          public String getResourceFileName() {
            return "test_keyboard_with_popups.xml";
          }

          @Override
          public String getKeyboardId() {
            return "";
          }

          @Override
          public String getNameResId() {
            return "";
          }

          @Override
          public String getIconResId() {
            return "";
          }

          @Override
          public String getLabelResId() {
            return "";
          }

          @Override
          public String getDefaultOutputResId() {
            return "";
          }

          @Override
          public String getDescription() {
            return "";
          }

          @Override
          public List<EmojiData> generateOwnedEmojis() {
            var key =
                new EmojiData(
                    0,
                    "desc",
                    "baseDesc",
                    "group",
                    "\uD83D\uDE00",
                    Collections.singletonList("smile"));
            key.addVariant(
                new EmojiData(
                    0, "desc", "baseDesc", "group", "\uD83D\uDE01", Collections.emptyList()));
            return Arrays.asList(
                key,
                new EmojiData(
                    1, "desc3", "baseDesc3", "group", "\uD83D\uDE02", Collections.emptyList()));
          }
        };
    var creator = new EmojiKeyboardCreator(folder, fakeCollection);

    creator.buildKeyboardFile();

    var expectedFile = new File(folder, "test_keyboard_with_popups.xml");
    assertTrue(expectedFile.exists(), "Keyboard XML file should be created");

    var fileContent = new String(Files.readAllBytes(expectedFile.toPath())).trim();
    var expectedContents =
        new String(
                Files.readAllBytes(
                    new File("emojis/fixtures/expected_test_keyboard_with_popups.xml").toPath()))
            .trim();
    assertEquals(expectedContents, fileContent);

    // popup keyboard
    var expectedPopupFile = new File(folder, "test_keyboard_with_popups_popup_0.xml");
    assertTrue(expectedPopupFile.exists(), "Keyboard XML file should be created");
    var popupFileContent = new String(Files.readAllBytes(expectedPopupFile.toPath())).trim();
    var expectedPopupContents =
        new String(
                Files.readAllBytes(
                    new File("emojis/fixtures/expected_test_keyboard_with_popups_popup_0.xml")
                        .toPath()))
            .trim();
    assertEquals(popupFileContent, expectedPopupContents);
  }

  @Test
  void testBuildKeyboardWithGenderAndSkinTones() throws Exception {
    var folder = Files.createTempDirectory("emoji_test").toFile();
    var fakeCollection =
        new EmojiCollection() {
          @Override
          public String getResourceFileName() {
            return "test_keyboard_with_genders_and_skin_tones.xml";
          }

          @Override
          public String getKeyboardId() {
            return "";
          }

          @Override
          public String getNameResId() {
            return "";
          }

          @Override
          public String getIconResId() {
            return "";
          }

          @Override
          public String getLabelResId() {
            return "";
          }

          @Override
          public String getDefaultOutputResId() {
            return "";
          }

          @Override
          public String getDescription() {
            return "";
          }

          @Override
          public List<EmojiData> generateOwnedEmojis() {
            return Arrays.asList(
                // person running facing right: medium-light skin tone
                new EmojiData(
                    0, "desc", "baseDesc", "group", "🏃🏼‍➡️", Collections.singletonList("smile")),
                // man running facing right
                new EmojiData(
                    1, "desc2", "baseDesc2", "group", "🏃‍♂️‍➡️", Arrays.asList("sad", "person")),
                // women with bunny ears
                new EmojiData(2, "desc3", "baseDesc3", "group", "👯‍♀️", Collections.emptyList()),
                // woman and man holding hands: dark skin tone, medium-dark skin tone
                new EmojiData(
                    3, "desc3", "baseDesc3", "group", "👩🏿‍🤝‍👨🏾", Collections.emptyList()));
          }
        };
    var creator = new EmojiKeyboardCreator(folder, fakeCollection);

    creator.buildKeyboardFile();

    var expectedFile = new File(folder, "test_keyboard_with_genders_and_skin_tones.xml");
    assertTrue(expectedFile.exists(), "Keyboard XML file should be created");

    var fileContent = new String(Files.readAllBytes(expectedFile.toPath())).trim();
    var expectedContents =
        new String(
                Files.readAllBytes(
                    new File(
                            "emojis/fixtures/expected_test_keyboard_with_genders_and_skin_tones.xml")
                        .toPath()))
            .trim();
    assertEquals(expectedContents, fileContent);
  }
}
