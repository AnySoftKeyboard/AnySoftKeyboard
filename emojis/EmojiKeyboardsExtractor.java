package emojis;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class EmojiKeyboardsExtractor {
  private final List<EmojiCollector> mCollectors = new ArrayList<>();
  private final File mXmlResourceFolder;
  private final File mSourceHtmlFile;
  private EmojiCollector mUncollectedEmojiCollector;

  /**
   * Download the emoji list from https://unicode.org/Public/emoji/latest/emoji-test.txt
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
    List<EmojiData> parsedEmojiData = UnicodeOrgEmojiTestDataParser.parse(mSourceHtmlFile);
    final AtomicInteger total = new AtomicInteger(0);

    System.out.println("Have " + parsedEmojiData.size() + " main emojis parsed. Collecting...");
    for (EmojiData emojiData : parsedEmojiData) {
      final boolean debug = emojiData.baseOutputDescription.contains("health");
      System.out.print(".");
      if (debug) System.out.print("!");
      int collected = 0;
      for (EmojiCollector collector : mCollectors) {
        if (collector.visitEmoji(emojiData)) {
          collected++;
        }
      }

      if (mUncollectedEmojiCollector != null && collected == 0) {
        mUncollectedEmojiCollector.visitEmoji(emojiData);
      } else if (collected > 1) {
        System.out.printf(
            Locale.US,
            "Emoji #%s (%s) was collected by %d collectors!",
            emojiData.grouping,
            emojiData.output,
            collected);
      }
    }

    System.out.println("Storing into resources...");
    storeEmojisToResourceFiles(mCollectors, mUncollectedEmojiCollector, mXmlResourceFolder);

    parsedEmojiData.forEach(emojiData -> total.addAndGet(1 + emojiData.getVariants().size()));
    System.out.printf(
        Locale.US,
        "Found %d root emojis, with %d including variants.",
        parsedEmojiData.size(),
        total.get());
  }

  private void storeEmojisToResourceFiles(
      List<EmojiCollector> collectors,
      EmojiCollector uncollectedEmojiCollector,
      final File xmlResourceFolder)
      throws TransformerException, ParserConfigurationException, IOException {
    if (!xmlResourceFolder.isDirectory() && !xmlResourceFolder.mkdirs()) {
      throw new RuntimeException(
          "Could not create resources folder: " + xmlResourceFolder.getAbsolutePath());
    }

    var errors = new StringBuilder();
    for (EmojiCollector collector : collectors) {
      var creator = new EmojiKeyboardCreator(xmlResourceFolder, collector);
      if (creator.buildKeyboardFile() == 0) {
        errors
            .append("Collector for ")
            .append(collector.getResourceFileName())
            .append(" does not have any emojis collected!")
            .append("\n");
      }
    }

    final List<EmojiData> uncollectedEmojis = uncollectedEmojiCollector.generateOwnedEmojis();
    if (!uncollectedEmojis.isEmpty()) {
      System.out.printf(
          Locale.US,
          "Some emojis were not collected! Storing them at file '%s'!%n",
          uncollectedEmojiCollector.getResourceFileName());
      var creator = new EmojiKeyboardCreator(xmlResourceFolder, uncollectedEmojiCollector);
      creator.buildKeyboardFile();
    }

    // building the quick-text addons XML declaration file
    var docFactory = DocumentBuilderFactory.newInstance();
    var docBuilder = docFactory.newDocumentBuilder();

    // Root element
    var doc = docBuilder.newDocument();
    var rootElement = doc.createElement("QuickTextKeys");
    doc.appendChild(rootElement);
    // Using the collectors to generate the quick-keys nodes
    var addOnIndex = 1;
    for (EmojiCollector collector : collectors) {
      addAddOnToXmlNode(collector, rootElement, doc, addOnIndex);
      addOnIndex++;
    }
    // Adding static quick-keys addons
    for (EmojiCollection collector : getStaticAddOnCollectors()) {
      addAddOnToXmlNode(collector, rootElement, doc, addOnIndex);
      addOnIndex++;
    }

    // Write the content into XML file
    var transformerFactory = TransformerFactory.newInstance();
    var transformer = transformerFactory.newTransformer();
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

    var source = new DOMSource(doc);
    var addOnsDeclarationsFile = new File(xmlResourceFolder, "quick_text_keys.xml");
    if (addOnsDeclarationsFile.exists() && !addOnsDeclarationsFile.delete()) {
      throw new RuntimeException("Could not delete " + addOnsDeclarationsFile.getAbsolutePath());
    }
    var result = new StreamResult(addOnsDeclarationsFile);
    transformer.transform(source, result);
    System.out.printf(
        Locale.US, "Write addons file to '%s'%n.", addOnsDeclarationsFile.getAbsolutePath());

    if (!errors.isEmpty()) {
      throw new IllegalStateException(errors.toString());
    }
  }

  private static EmojiCollection[] getStaticAddOnCollectors() {
    return new EmojiCollection[] {
      new StaticEmojiCollector(
          "popup_default_quick_text.xml",
          "698b8c20-19df-11e1-bddb-0800200c9a67",
          "@string/default_quick_text_key_name",
          "@drawable/ic_quick_text_dark_theme",
          "@string/quick_text_smiley_key_label",
          "@string/quick_text_smiley_key_output",
          ""),
      new StaticEmojiCollector(
          "popup_smileys.xml",
          "0077b34d-770f-4083-83e4-081957e06c27",
          "@string/simley_key_name",
          "@drawable/ic_quick_text_dark_theme",
          "@string/quick_text_smiley_key_label",
          "@string/quick_text_smiley_key_output",
          ""),
      new StaticEmojiCollector(
          "popup_smileys_short.xml",
          "085020ea-f496-4c0c-80cb-45ca50635c59",
          "@string/short_smiley_key_name",
          "@drawable/ic_quick_text_dark_theme",
          "@string/quick_text_short_smiley_key_label",
          "@string/quick_text_short_smiley_key_output",
          ""),
      new StaticEmojiCollector(
          "popup_kaomoji.xml",
          "9493bdf1-ae8e-4d5d-a1e3-065bfdf83b60",
          "@string/quick_text_kaomoji",
          "@drawable/ic_quick_text_dark_theme",
          "@string/quick_text_kaomoji_key_label",
          "@string/quick_text_kaomoji_key_output",
          "")
    };
  }

  private static class StaticEmojiCollector implements EmojiCollection {
    private final String mResourceFileName;
    private final String mKeyboardId;
    private final String mNameResId;
    private final String mIconResId;
    private final String mLabelResId;
    private final String mDefaultOutputResId;
    private final String mDescription;

    private StaticEmojiCollector(
        String resourceFileName,
        String keyboardId,
        String nameResId,
        String iconResId,
        String labelResId,
        String defaultOutputResId,
        String description) {
      mResourceFileName = resourceFileName;
      mKeyboardId = keyboardId;
      mNameResId = nameResId;
      mIconResId = iconResId;
      mLabelResId = labelResId;
      mDefaultOutputResId = defaultOutputResId;
      mDescription = description;
    }

    @Override
    public String getResourceFileName() {
      return mResourceFileName;
    }

    @Override
    public String getKeyboardId() {
      return mKeyboardId;
    }

    @Override
    public String getNameResId() {
      return mNameResId;
    }

    @Override
    public String getIconResId() {
      return mIconResId;
    }

    @Override
    public String getLabelResId() {
      return mLabelResId;
    }

    @Override
    public String getDefaultOutputResId() {
      return mDefaultOutputResId;
    }

    @Override
    public String getDescription() {
      return mDescription;
    }

    @Override
    public List<EmojiData> generateOwnedEmojis() {
      return Collections.emptyList();
    }
  }

  private static void addAddOnToXmlNode(
      EmojiCollection collector, Element rootElement, Document doc, int addOnIndex) {
    var addOn = doc.createElement("QuickTextKey");
    rootElement.appendChild(addOn);

    addOn.setAttribute("id", collector.getKeyboardId());
    addOn.setAttribute("nameResId", collector.getNameResId());
    addOn.setAttribute(
        "popupKeyboard",
        String.format(
            Locale.ROOT, "@xml/%s", collector.getResourceFileName().replaceFirst("\\.xml$", "")));
    addOn.setAttribute("keyIcon", collector.getIconResId());
    addOn.setAttribute("keyLabel", collector.getLabelResId());
    addOn.setAttribute("keyOutputText", collector.getDefaultOutputResId());
    addOn.setAttribute("description", collector.getDescription());
    addOn.setAttribute("index", Integer.toString(addOnIndex));
  }
}
