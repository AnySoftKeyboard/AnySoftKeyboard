package emoji;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

public class EmojiKeyboardsExtractor {
    private final List<EmojiCollector> mCollectors = new ArrayList<EmojiCollector>();
    private final File mXmlResourceFolder;
    private final File mSourceHtmlFile;
    private EmojiCollector mUncollectedEmojiCollector;

    /**
     * Download the emoji list from https://unicode.org/Public/emoji/11.0/emoji-test.txt
     *
     * @param sourceUnicodeEmojiListFile path to the file saved from
     *     http://unicode.org/emoji/charts/full-emoji-list.html
     * @param targetResourceFolder the app's resources folder
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

    public void parseEmojiListIntoKeyboardResources()
            throws IOException, TransformerException, ParserConfigurationException {
        List<EmojiData> parsedEmojiData =
                UnicodeOrgEmojiTestDataParser.parse(
                        mSourceHtmlFile, EmojiCollector.ADDITION_TAGS_FOR_EMOJI);
        final AtomicInteger total = new AtomicInteger(0);

        System.out.println("Have " + parsedEmojiData.size() + " main emojis parsed. Collecting...");
        for (EmojiData emojiData : parsedEmojiData) {
            System.out.print(".");
            int collected = 0;
            for (EmojiCollector collector : mCollectors) {
                if (collector.visitEmoji(emojiData)) {
                    collected++;
                }
            }

            if (mUncollectedEmojiCollector != null && collected == 0) {
                mUncollectedEmojiCollector.visitEmoji(emojiData);
            } else if (collected > 1) {
                System.out.print(
                        String.format(
                                Locale.US,
                                "Emoji #%s (%s) was collected by %d collectors!",
                                emojiData.grouping,
                                emojiData.output,
                                collected));
            }
        }

        System.out.println("Storing into resources...");
        storeEmojisToResourceFiles(mCollectors, mUncollectedEmojiCollector, mXmlResourceFolder);

        parsedEmojiData.forEach(emojiData -> total.addAndGet(1 + emojiData.getVariants().size()));
        System.out.print(
                String.format(
                        Locale.US,
                        "Found %d root emojis, with %d including variants.",
                        parsedEmojiData.size(),
                        total.get()));
    }

    private void storeEmojisToResourceFiles(
            List<EmojiCollector> collectors,
            EmojiCollector uncollectedEmojiCollector,
            final File xmlResourceFolder)
            throws TransformerException, ParserConfigurationException, IOException {
        xmlResourceFolder.mkdirs();

        StringBuilder errors = new StringBuilder();
        for (EmojiCollector collector : collectors) {
            EmojiKeyboardCreator creator = new EmojiKeyboardCreator(xmlResourceFolder, collector);
            creator.buildKeyboardFile();
            if (collector.getOwnedEmjois().size() == 0) {
                errors.append("Collector for ")
                        .append(collector.getResourceFileName())
                        .append(" does not have any emojis collected!")
                        .append("\n");
            }
        }

        if (uncollectedEmojiCollector.getOwnedEmjois().size() > 0) {
            System.out.println(
                    String.format(
                            Locale.US,
                            "Some emojis were not collected! Storing them at file '%s'!",
                            uncollectedEmojiCollector.getResourceFileName()));
            EmojiKeyboardCreator creator =
                    new EmojiKeyboardCreator(xmlResourceFolder, uncollectedEmojiCollector);
            creator.buildKeyboardFile();
        }

        if (errors.length() > 0) {
            throw new IllegalStateException(errors.toString());
        }
    }
}
