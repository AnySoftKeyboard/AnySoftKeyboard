package emoji;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

public class EmojiKeyboardsExtractor {
    private final List<EmojiCollector> mCollectors = new ArrayList<EmojiCollector>();
    private final File mXmlResourceFolder;
    private final File mSourceHtmlFile;
    private EmojiCollector mUncollectedEmojiCollector;

    /**
     * Download the emoji list from http://unicode.org/emoji/charts/full-emoji-list.html and save it where-ever.
     *
     * @param sourceUnicodeEmojiListFile path to the file saved from http://unicode.org/emoji/charts/full-emoji-list.html
     * @param targetResourceFolder       the app's resources folder
     */
    public EmojiKeyboardsExtractor(File sourceUnicodeEmojiListFile, File targetResourceFolder) {
        mSourceHtmlFile = sourceUnicodeEmojiListFile;
        mXmlResourceFolder = targetResourceFolder;
    }

    public void addEmojiCollector(EmojiCollector emojiCollector) {
        mCollectors.add(emojiCollector);
    }

    public void setUncollectedEmojisCollector(EmojiCollector emojiCollector) {
        mUncollectedEmojiCollector = emojiCollector;
    }

    public void parseEmojiListIntoKeyboardResources() throws IOException, TransformerException, ParserConfigurationException {
        List<EmojiData> parsedEmojiData = UnicodeOrgEmojiHtmlParser.parse(mSourceHtmlFile);

        for (EmojiData emojiData : parsedEmojiData) {
            boolean collected = false;
            for (EmojiCollector collector : mCollectors) {
                if (collector.visitEmoji(emojiData)) {
                    collected = true;
                }
            }

            if (mUncollectedEmojiCollector != null && !collected) {
                mUncollectedEmojiCollector.visitEmoji(emojiData);
            }

        }

        storeEmojisToResourceFiles(mCollectors, mUncollectedEmojiCollector, mXmlResourceFolder);
    }

    private void storeEmojisToResourceFiles(List<EmojiCollector> collectors, EmojiCollector uncollectedEmojiCollector, final File xmlResourceFolder) throws TransformerException, ParserConfigurationException, IOException {
        xmlResourceFolder.mkdirs();

        for (EmojiCollector collector : Stream.concat(collectors.stream(), Stream.of(uncollectedEmojiCollector)).collect(Collectors.toList())) {
            EmojiKeyboardCreator creator = new EmojiKeyboardCreator(xmlResourceFolder, collector);
            creator.buildKeyboardFile();
        }
    }
}
