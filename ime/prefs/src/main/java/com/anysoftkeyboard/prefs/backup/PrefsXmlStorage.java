package com.anysoftkeyboard.prefs.backup;

import com.anysoftkeyboard.utils.XmlWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.Map;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class PrefsXmlStorage {

    public PrefsXmlStorage() {}

    private static void writePrefItems(XmlWriter output, Iterable<PrefItem> items, boolean atRoot)
            throws IOException {
        for (PrefItem item : items) {
            if (!atRoot) output.writeEntity("pref");

            for (Map.Entry<String, String> aValue : item.getValues()) {
                final String value = aValue.getValue();
                if (value == null) continue;

                output.writeEntity("value").writeAttribute(aValue.getKey(), value).endEntity();
            }

            writePrefItems(output, item.getChildren(), false);

            if (!atRoot) output.endEntity();
        }
    }

    public void store(PrefsRoot prefsRoot, OutputStream outputFile) throws Exception {
        try (final XmlWriter output = new XmlWriter(outputFile)) {
            output.writeEntity("AnySoftKeyboardPrefs")
                    .writeAttribute("version", Integer.toString(prefsRoot.getVersion()));

            writePrefItems(output, Collections.singleton(prefsRoot), true);

            output.endEntity(); // AnySoftKeyboardPrefs
        }
    }

    public PrefsRoot load(InputStream inputFile) throws Exception {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
        final PrefsXmlParser prefsXmlParser = new PrefsXmlParser();
        try (inputFile) {
            parser.parse(inputFile, prefsXmlParser);
            return prefsXmlParser.getParsedRoot();
        }
    }

    private static class PrefsXmlParser extends DefaultHandler {
        private final Deque<PrefItem> mCurrentNode = new ArrayDeque<>();
        private PrefsRoot mParsedRoot;

        @Override
        public void startElement(
                String uri, String localName, String qualifiedName, Attributes attributes)
                throws SAXException {
            super.startElement(uri, localName, qualifiedName, attributes);
            switch (qualifiedName) {
                case "AnySoftKeyboardPrefs":
                    if (mCurrentNode.isEmpty()) {
                        mParsedRoot =
                                new PrefsRoot(Integer.parseInt(attributes.getValue("version")));
                        mCurrentNode.push(mParsedRoot);
                    } else {
                        throw new IllegalStateException(
                                "AnySoftKeyboardPrefs should be the root node!");
                    }
                    break;
                case "pref":
                    mCurrentNode.push(mCurrentNode.peek().createChild());
                    break;
                case "value":
                    mCurrentNode.peek().addValue(attributes.getQName(0), attributes.getValue(0));
                    break;
                default:
                    // will allow unknown nodes, so we can try to support older/newer XML structures
                    break;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qualifiedName)
                throws SAXException {
            super.endElement(uri, localName, qualifiedName);
            switch (qualifiedName) {
                case "AnySoftKeyboardPrefs":
                case "pref":
                    mCurrentNode.pop();
                    break;
                default:
                    // the other nodes do not have children.
                    break;
            }
        }

        PrefsRoot getParsedRoot() {
            return mParsedRoot;
        }
    }
}
