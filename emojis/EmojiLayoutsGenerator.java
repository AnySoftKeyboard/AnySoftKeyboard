package emojis;

import java.io.File;

public class EmojiLayoutsGenerator {
  public static void main(String[] args) throws Exception {
    if (args.length != 2) {
      System.err.println("Usage: EmojiLayoutGenerator <emoji-test.txt path> <output folder path>");
      System.exit(1);
    }

    var emojiTestFile = new File(args[0]);
    var outputFolder = new File(args[1]);

    var extractor = new EmojiKeyboardsExtractor(emojiTestFile, outputFolder);

    // Add collectors
    extractor.addEmojiCollector(EmojiCollector.EMOTICONS_COLLECTOR);
    extractor.addEmojiCollector(EmojiCollector.FLAGS_COLLECTOR);
    extractor.addEmojiCollector(EmojiCollector.PEOPLE_COLLECTOR);
    extractor.addEmojiCollector(EmojiCollector.ACTIVITY_COLLECTOR);
    extractor.addEmojiCollector(EmojiCollector.ACCESSORIES_COLLECTOR);
    extractor.addEmojiCollector(EmojiCollector.SCAPE_COLLECTOR);
    extractor.addEmojiCollector(EmojiCollector.FOOD_COLLECTOR);
    extractor.addEmojiCollector(EmojiCollector.NATURE_COLLECTOR);
    extractor.addEmojiCollector(EmojiCollector.OFFICE_COLLECTOR);
    extractor.addEmojiCollector(EmojiCollector.SIGNS_COLLECTOR);
    extractor.addEmojiCollector(EmojiCollector.OCCASIONS_COLLECTOR);
    extractor.addEmojiCollector(EmojiCollector.TRANSPORT_COLLECTOR);

    extractor.setUncollectedEmojisCollector(EmojiCollector.UNCOLLECTED_COLLECTOR);

    extractor.parseEmojiListIntoKeyboardResources();
  }
}
