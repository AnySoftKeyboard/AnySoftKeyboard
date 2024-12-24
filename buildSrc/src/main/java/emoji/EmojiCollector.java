package emoji;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class EmojiCollector implements EmojiCollection {

  public static final Map<String, List<String>> ADDITION_TAGS_FOR_EMOJI = new HashMap<>();
  public static final EmojiCollector EMOTICONS_COLLECTOR =
      new GroupEmojiCollector(
          "quick_text_unicode_emoticons.xml",
          new PersonDetector(),
          "face-smiling",
          "face-affection",
          "face-tongue",
          "face-hand",
          "face-neutral-skeptical",
          "face-sleepy",
          "face-unwell",
          "face-hat",
          "face-glasses",
          "face-concerned",
          "face-negative",
          "face-costume",
          "cat-face",
          "monkey-face",
          "emotion",
          "hand-fingers-open",
          "hand-fingers-partial",
          "hand-single-finger",
          "hand-fingers-closed",
          "hands");
  public static final EmojiCollector PEOPLE_COLLECTOR =
      new GroupEmojiCollector(
          "quick_text_unicode_people.xml",
          new PersonDetector(),
          "hand-prop",
          "body-parts",
          "person",
          "person-gesture",
          "person-role",
          "person-fantasy",
          "family",
          "person-symbol",
          "skin-tone",
          "hair-style");
  public static final EmojiCollector ACCESSORIES_COLLECTOR =
      new GroupEmojiCollector("quick_text_unicode_accessories.xml", new None(), "clothing");
  public static final EmojiCollector NATURE_COLLECTOR =
      new GroupEmojiCollector(
          "quick_text_unicode_nature.xml",
          new None(),
          "animal-mammal",
          "animal-bird",
          "animal-amphibian",
          "animal-reptile",
          "food-marine", // weird, but we need those here too
          "animal-marine",
          "animal-bug",
          "plant-flower",
          "plant-other");
  public static final EmojiCollector FOOD_COLLECTOR =
      new GroupEmojiCollector(
          "quick_text_unicode_food.xml",
          new None(),
          "food-fruit",
          "food-vegetable",
          "food-prepared",
          "food-asian",
          "food-marine",
          "food-sweet",
          "drink",
          "dishware");
  public static final EmojiCollector TRANSPORT_COLLECTOR =
      new GroupEmojiCollector(
          "quick_text_unicode_transport.xml",
          new None(),
          "transport-ground",
          "transport-water",
          "transport-air");
  public static final EmojiCollector SIGNS_COLLECTOR =
      new GroupEmojiCollector(
          "quick_text_unicode_signs.xml",
          new None(),
          "time",
          "sound",
          "transport-sign",
          "warning",
          "arrow",
          "religion",
          "zodiac",
          "av-symbol",
          "gender",
          "math",
          "punctuation",
          "currency",
          "other-symbol",
          "keycap",
          "alphanum",
          "geometric");
  public static final EmojiCollector SCAPE_COLLECTOR =
      new GroupEmojiCollector(
          "quick_text_unicode_scape.xml",
          new None(),
          "place-map",
          "place-geographic",
          "place-building",
          "place-religious",
          "place-other",
          "hotel",
          "weather",
          "household");
  public static final EmojiCollector ACTIVITY_COLLECTOR =
      new GroupEmojiCollector(
          "quick_text_unicode_activity.xml",
          new PersonDetector(),
          "person-activity",
          "person-sport",
          "person-resting",
          "sport",
          "game",
          "crafts",
          "music",
          "musical-instrument");
  public static final EmojiCollector OFFICE_COLLECTOR =
      new GroupEmojiCollector(
          "quick_text_unicode_office.xml",
          new None(),
          "phone",
          "computer",
          "video",
          "book-paper",
          "money",
          "mail",
          "writing",
          "office",
          "lock",
          "tool",
          "science",
          "medical",
          "other-object");
  public static final EmojiCollector OCCASIONS_COLLECTOR =
      new GroupEmojiCollector(
          "quick_text_unicode_occasions.xml", new None(), "event", "award-medal");
  public static final EmojiCollector FLAGS_COLLECTOR =
      new GroupEmojiCollector(
          "quick_text_unicode_flags.xml", new None(), "flag", "country-flag", "subdivision-flag");
  public static final EmojiCollector UNCOLLECTED_COLLECTOR =
      new EmojiCollector("quick_text_unicode_uncollected.xml", new None()) {
        @Override
        protected boolean isMyEmoji(EmojiData emojiData) {
          return true;
        }
      };

  static {
    ADDITION_TAGS_FOR_EMOJI.put("\uD83C\uDDE6\uD83C\uDDEA", Arrays.asList("UAE"));
    ADDITION_TAGS_FOR_EMOJI.put("\uD83D\uDE4F", Arrays.asList("pray"));
    ADDITION_TAGS_FOR_EMOJI.put("\uD83C\uDDFA\uD83C\uDDF8", Arrays.asList("USA", "US"));
  }

  private final String mResourceFileName;
  private final List<EmojiData> mOwnedEmoji = new ArrayList<>();
  private final VariantDetector mVariantDetector;

  protected EmojiCollector(String emojiKeyboardResourceFilename, VariantDetector variantDetector) {
    mResourceFileName = emojiKeyboardResourceFilename;
    mVariantDetector = variantDetector;
  }

  boolean visitEmoji(EmojiData emojiData) {
    if (isMyEmoji(emojiData)) {
      if (!mOwnedEmoji.contains(emojiData)) {
        mOwnedEmoji.add(emojiData);
      }
      return true;
    } else {
      return false;
    }
  }

  protected abstract boolean isMyEmoji(EmojiData emojiData);

  @Override
  public String getResourceFileName() {
    return mResourceFileName;
  }

  @Override
  public List<EmojiData> generateOwnedEmojis() {
    List<EmojiData> emojiDataList = new ArrayList<>(mOwnedEmoji);
    emojiDataList.sort(Comparator.comparingInt(o -> o.position));
    final Set<EmojiData> variants = new HashSet<>();
    final List<EmojiData> workspace = new ArrayList<>(emojiDataList);
    emojiDataList.forEach(
        emojiData -> {
          if (!variants.contains(emojiData)) {
            workspace.forEach(
                possibleVariant -> {
                  if (mVariantDetector.isVariant(emojiData, possibleVariant)) {
                    emojiData.addVariant(possibleVariant);
                    variants.add(possibleVariant);
                  }
                });
            workspace.removeIf(variants::contains);
          }
        });

    emojiDataList.removeIf(variants::contains);

    return emojiDataList;
  }

  private static class GroupEmojiCollector extends EmojiCollector {
    private final List<String> mSubGroups;

    protected GroupEmojiCollector(
        String emojiKeyboardResourceFilename,
        VariantDetector variantDetector,
        String... subgroups) {
      super(emojiKeyboardResourceFilename, variantDetector);
      mSubGroups = Arrays.asList(subgroups);
    }

    protected boolean isMyEmoji(EmojiData emojiData) {
      return mSubGroups.stream().anyMatch(emojiData.grouping::endsWith);
    }
  }
}
