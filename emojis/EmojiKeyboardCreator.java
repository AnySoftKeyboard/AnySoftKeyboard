package emojis;

import emojis.utils.JavaEmojiUtils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class EmojiKeyboardCreator {
  private final File keyboardResourceFile;
  private final EmojiCollection collector;
  private final String keyWidth;
  private final String comment;
  private final boolean splitToRows;

  EmojiKeyboardCreator(File xmlResourceFolder, EmojiCollection collector) {
    this(xmlResourceFolder, collector, null, false, "20%p");
  }

  private EmojiKeyboardCreator(
      File xmlResourceFolder,
      EmojiCollection collector,
      String comment,
      boolean splitToRows,
      String keyWidth) {
    this.keyboardResourceFile = new File(xmlResourceFolder, collector.getResourceFileName());
    this.collector = collector;
    this.keyWidth = keyWidth;
    this.comment = comment;
    this.splitToRows = splitToRows;
  }

  private void deleteAllBuiltKeyboard() {
    if (keyboardResourceFile.exists() && !keyboardResourceFile.delete()) {
      throw new RuntimeException("Could not delete " + keyboardResourceFile.getAbsolutePath());
    }
  }

  int buildKeyboardFile() throws ParserConfigurationException, TransformerException, IOException {
    deleteAllBuiltKeyboard();

    List<EmojiKeyboardCreator> additionalPopupCreators = new ArrayList<>();

    final List<EmojiData> parentEmojiDataList = collector.generateOwnedEmojis();
    System.out.printf(
        Locale.US,
        "EmojiKeyboardCreator will write to %s with %d emojis...",
        keyboardResourceFile,
        parentEmojiDataList.size());

    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

    Document doc = docBuilder.newDocument();
    Element keyboardElement = doc.createElement("Keyboard");
    if (comment != null) keyboardElement.appendChild(doc.createComment(comment));
    keyboardElement.setAttributeNS(
        "http://schemas.android.com/apk/res/android",
        "android:keyHeight",
        "@integer/key_normal_height");
    keyboardElement.setAttributeNS(
        "http://schemas.android.com/apk/res/android", "android:keyWidth", keyWidth);
    doc.appendChild(keyboardElement);

    final List<List<EmojiData>> rows;
    if (false /*change this*/) {
      rows = new ArrayList<>();
      // assuming all keys are of the same base
      // splitting by gender
      TreeMap<Integer, List<EmojiData>> rowsMap = new TreeMap<>();
      parentEmojiDataList.forEach(
          emojiData -> {
            int key = 0;
            final int flagSize = 2 * JavaEmojiUtils.Gender.values().length;
            int keyFlagValue = flagSize * emojiData.orderedGenders.size();
            for (int genderIndex = 0;
                genderIndex < emojiData.orderedGenders.size();
                genderIndex++) {
              key += keyFlagValue * (emojiData.orderedGenders.get(genderIndex).ordinal() + 1);
              keyFlagValue = keyFlagValue / flagSize;
            }
            if (!rowsMap.containsKey(key)) rowsMap.put(key, new ArrayList<EmojiData>());
            rowsMap.get(key).add(emojiData);
          });
      System.out.println("Has " + rowsMap.size() + " rows.");
      Integer key = rowsMap.firstKey();
      while (key != null) {
        List<EmojiData> aRow = rowsMap.get(key);
        System.out.println("Row with key " + key + " has " + aRow.size() + " entries.");
        rows.add(aRow);
        key = rowsMap.higherKey(key);
      }
    } else {
      rows = Collections.singletonList(parentEmojiDataList);
    }

    for (List<EmojiData> row : rows) {
      Element rowElement = doc.createElement("Row");
      keyboardElement.appendChild(rowElement);

      for (int i = 0; i < row.size(); i++) {
        EmojiData emojiData = row.get(i);
        /*
        <Key
            android:keyLabel="\uD83C\uDF93"
            android:popupKeyboard="@xml/popup_qwerty_u"
            android:keyOutputText="\uD83C\uDF93"/>
         */
        Element keyElement = doc.createElement("Key");
        rowElement.appendChild(keyElement);

        keyElement.setAttributeNS(
            "http://schemas.android.com/apk/res/android", "android:keyLabel", emojiData.output);
        keyElement.setAttributeNS(
            "http://schemas.android.com/apk/res/android",
            "android:keyOutputText",
            emojiData.output);
        if (!emojiData.tags.isEmpty()) {
          keyElement.setAttributeNS(
              "http://schemas.android.com/apk/res-auto",
              "ask:tags",
              String.join(",", emojiData.tags));
        }
        if (!emojiData.orderedGenders.isEmpty()) {
          keyElement.setAttributeNS(
              "http://schemas.android.com/apk/res-auto",
              "ask:genders",
              String.join(",", adjustEnums(emojiData.orderedGenders)));
        }
        if (!emojiData.orderedSkinTones.isEmpty()) {
          keyElement.setAttributeNS(
              "http://schemas.android.com/apk/res-auto",
              "ask:skinTones",
              String.join(",", adjustEnums(emojiData.orderedSkinTones)));
        }
        final List<String> variants = emojiData.getVariants();
        if (!variants.isEmpty()) {
          final String collectorName =
              collector
                  .getResourceFileName()
                  .substring(0, collector.getResourceFileName().length() - 4);
          final String popupKeysLayoutName = collectorName + "_popup_" + i;

          keyElement.setAttributeNS(
              "http://schemas.android.com/apk/res/android",
              "android:popupKeyboard",
              "@xml/" + popupKeysLayoutName);

          final List<EmojiData> variantEmojiList = new ArrayList<>(variants.size());
          for (int i1 = 0; i1 < variants.size(); i1++) {
            String variant = variants.get(i1);
            EmojiData data =
                new EmojiData(
                    i1,
                    "",
                    "",
                    emojiData.grouping,
                    variant,
                    Collections.emptyList() /*let's say that variants should not show tags*/);
            variantEmojiList.add(data);
          }
          EmojiCollection variantsCollection =
              new EmojiCollection() {
                @Override
                public String getResourceFileName() {
                  return popupKeysLayoutName + ".xml";
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
                  return variantEmojiList;
                }
              };
          additionalPopupCreators.add(
              new EmojiKeyboardCreator(
                  keyboardResourceFile.getParentFile(),
                  variantsCollection,
                  "Variants for " + emojiData.output,
                  true,
                  "15%p"));
        }
      }
    }

    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
    DOMSource source = new DOMSource(doc);
    //noinspection ResultOfMethodCallIgnored
    keyboardResourceFile.delete();

    StreamResult result = new StreamResult(keyboardResourceFile);

    // Output to console for testing
    // StreamResult result = new StreamResult(System.out);

    transformer.transform(source, result);

    if (!additionalPopupCreators.isEmpty()) {
      System.out.println("Building variants popup files...");
      for (EmojiKeyboardCreator creator : additionalPopupCreators) {
        creator.buildKeyboardFile();
      }
    }
    System.out.println("Done!");

    return parentEmojiDataList.size();
  }

  private static List<String> adjustEnums(List<? extends Enum> tags) {
    return tags.stream().distinct().map(Enum::toString).collect(Collectors.toList());
  }
}
