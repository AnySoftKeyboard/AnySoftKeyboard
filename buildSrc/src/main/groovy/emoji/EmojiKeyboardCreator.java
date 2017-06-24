package emoji;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

class EmojiKeyboardCreator {
    private final File keyboardResourceFile;
    private final EmojiCollection collector;

    EmojiKeyboardCreator(File xmlResourceFolder, EmojiCollection collector) throws IOException {
        this.keyboardResourceFile = new File(xmlResourceFolder, collector.getResourceFileName());

        this.collector = collector;
    }

    void buildKeyboardFile() throws ParserConfigurationException, TransformerException, IOException {
        List<EmojiKeyboardCreator> additionalPopupCreators = new ArrayList<>();

        System.out.print(String.format(Locale.US, "EmojiKeyboardCreator will write to %s with %d emojis...", keyboardResourceFile, collector.getOwnedEmjois().size()));

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        Document doc = docBuilder.newDocument();
        Element keyboardElement = doc.createElement("Keyboard");
        /*
        <Keyboard xmlns:android="http://schemas.android.com/apk/res/android"
            android:keyHeight="@integer/key_normal_height"
            android:keyWidth="20%p"
            android:popupCharacters="asdasdas" >
        */
        keyboardElement.setAttributeNS("http://schemas.android.com/apk/res/android", "android:keyHeight", "@integer/key_normal_height");
        keyboardElement.setAttributeNS("http://schemas.android.com/apk/res/android", "android:keyWidth", "20%p");
        doc.appendChild(keyboardElement);

        Element rowElement = doc.createElement("Row");
        keyboardElement.appendChild(rowElement);

        List<EmojiData> ownedEmjois = collector.getOwnedEmjois();
        for (int i = 0; i < ownedEmjois.size(); i++) {
            EmojiData emojiData = ownedEmjois.get(i);
            /*
            <Key
                android:keyLabel="\uD83C\uDF93"
                android:popupKeyboard="@xml/popup_qwerty_u"
                android:keyOutputText="\uD83C\uDF93"/>
             */
            Element keyElement = doc.createElement("Key");
            rowElement.appendChild(keyElement);

            keyElement.setAttributeNS("http://schemas.android.com/apk/res/android", "android:keyLabel", emojiData.output);
            keyElement.setAttributeNS("http://schemas.android.com/apk/res/android", "android:keyOutputText", emojiData.output);
            keyElement.setAttributeNS("http://schemas.android.com/apk/res-auto", "ask:tags", String.join(",", emojiData.tags));
            final List<String> variants = emojiData.getVariants();
            if (variants.size() > 0) {
                final String collectorName = collector.getResourceFileName().substring(0, collector.getResourceFileName().length() - 4);
                final String popupKeysLayoutName = collectorName + "_popup_" + i;

                keyElement.setAttributeNS("http://schemas.android.com/apk/res/android", "android:popupKeyboard", "@xml/" + popupKeysLayoutName);

                final List<EmojiData> emojiDataList = new ArrayList<>(variants.size());
                for (int i1 = 0; i1 < variants.size(); i1++) {
                    String variant = variants.get(i1);
                    EmojiData data = new EmojiData(i1, variant, variant, Collections.emptyList()/*let's say that variants should not show tags*/);
                    emojiDataList.add(data);
                }
                EmojiCollection variantsCollection = new EmojiCollection() {
                    @Override
                    public String getResourceFileName() {
                        return popupKeysLayoutName + ".xml";
                    }

                    @Override
                    public List<EmojiData> getOwnedEmjois() {
                        return emojiDataList;
                    }
                };
                additionalPopupCreators.add(new EmojiKeyboardCreator(keyboardResourceFile.getParentFile(), variantsCollection));
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

        if (additionalPopupCreators.size() > 0) {
            System.out.println("Building variants popup files...");
            for (EmojiKeyboardCreator creator : additionalPopupCreators) {
                creator.buildKeyboardFile();
            }
        }
        System.out.println("Done!");
    }
}
