package com.anysoftkeyboard.prefs.backup;

import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.utils.XmlWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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

    private final File mStorageFile;

    public PrefsXmlStorage(File storageFile) {
        mStorageFile = storageFile;
    }

    public void store(PrefsRoot prefsRoot) throws Exception {
        final File targetFolder = mStorageFile.getParentFile();
        // parent folder may be null in case the file is on the root folder.
        if (targetFolder != null && !targetFolder.exists() && !targetFolder.mkdirs()) {
            throw new IOException("Failed to of storage folder " + targetFolder.getAbsolutePath());
        }

        // https://github.com/menny/Java-very-tiny-XmlWriter/blob/master/XmlWriter.java
        final XmlWriter output = new XmlWriter(mStorageFile);
        try {
            output.writeEntity("AnySoftKeyboardPrefs")
                    .writeAttribute("version", Integer.toString(prefsRoot.getVersion()));

            writePrefItems(output, Collections.singleton(prefsRoot), true);

            output.endEntity(); // AnySoftKeyboardPrefs
        } finally {
            try {
                output.close();
            } catch (IllegalStateException e) {
                // catching and swallowing. This could be because of an exception while writing to
                // the XML
                // maybe a non-ASCII key?
                Logger.w(
                        "PrefsXmlStorage",
                        e,
                        "Caught an IllegalStateException while closing storage backup file "
                                + mStorageFile);
            }
        }
    }

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

    public PrefsRoot load() throws Exception {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
        final PrefsXmlParser prefsXmlParser = new PrefsXmlParser();
        try (FileInputStream fileInputStream = new FileInputStream(mStorageFile)) {
            parser.parse(fileInputStream, prefsXmlParser);
        }
        return prefsXmlParser.getParsedRoot();
    }

    private static class PrefsXmlParser extends DefaultHandler {
        private PrefsRoot mParsedRoot;
        private final Deque<PrefItem> mCurrentNode = new ArrayDeque<>();

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
