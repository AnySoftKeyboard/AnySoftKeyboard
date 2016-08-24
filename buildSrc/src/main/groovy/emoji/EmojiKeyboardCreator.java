package emoji;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
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
    private final EmojiCollector collector;

    EmojiKeyboardCreator(File xmlResourceFolder, EmojiCollector collector) throws IOException {
        this.keyboardResourceFile = new File(xmlResourceFolder, collector.getResourceFileName());

        this.collector = collector;
    }

    void buildKeyboardFile() throws ParserConfigurationException, TransformerException {
        System.out.print(String.format(Locale.US, "EmojiKeyboardCreator will write to %s with %d emojis...", keyboardResourceFile, collector.getOwnedEmjois().size()));

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        Document doc = docBuilder.newDocument();
        Element keyboardElement = doc.createElement("Keyboard");
        /*
        <Keyboard xmlns:android="http://schemas.android.com/apk/res/android"
            android:keyHeight="@integer/key_normal_height"
            android:keyWidth="20%p">
        */
        keyboardElement.setAttributeNS("http://schemas.android.com/apk/res/android", "android:keyHeight", "@integer/key_normal_height");
        keyboardElement.setAttributeNS("http://schemas.android.com/apk/res/android", "android:keyWidth", "20%p");
        doc.appendChild(keyboardElement);

        Element rowElement = doc.createElement("Row");
        keyboardElement.appendChild(rowElement);

        for (EmojiData emojiData : collector.getOwnedEmjois()) {
            /*
            <Key
                android:keyLabel="\uD83C\uDF93"
                android:keyOutputText="\uD83C\uDF93"/>
             */
            Element keyElement = doc.createElement("Key");
            rowElement.appendChild(keyElement);

            keyElement.setAttributeNS("http://schemas.android.com/apk/res/android", "android:keyLabel", emojiData.output);
            keyElement.setAttributeNS("http://schemas.android.com/apk/res/android", "android:keyOutputText", emojiData.output);
            keyElement.setAttributeNS("http://schemas.android.com/apk/res-auto", "ask:tags", String.join(",", Arrays.asList(emojiData.tags)));
        }

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(keyboardResourceFile);

        // Output to console for testing
        // StreamResult result = new StreamResult(System.out);

        transformer.transform(source, result);

        System.out.println("Done!");
    }
}
