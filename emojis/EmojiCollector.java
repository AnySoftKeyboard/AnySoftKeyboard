package emojis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class EmojiCollector implements EmojiCollection {

  private static final Map<String, List<String>> ADDITION_TAGS_FOR_EMOJI = new HashMap<>();
  public static final EmojiCollector EMOTICONS_COLLECTOR =
      new GroupEmojiCollector(
          new PersonDetector(),
          "quick_text_unicode_emoticons.xml",
          "@string/settings_default_quick_text_key_id",
          "@string/unicode_quick_text_key_name",
          "@drawable/ic_quick_text_dark_theme",
          "@string/quick_text_smiley_key_unicode_output",
          "@string/quick_text_smiley_key_unicode_output",
          "",
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
          "heart",
          "hand-fingers-open",
          "hand-fingers-partial",
          "hand-single-finger",
          "hand-fingers-closed",
          "hands");
  public static final EmojiCollector PEOPLE_COLLECTOR =
      new GroupEmojiCollector(
          new PersonDetector(),
          "quick_text_unicode_people.xml",
          "623e21f5-9200-4c0b-b4c7-9691129d7f1f",
          "@string/unicode_people_quick_text_key_name",
          "@drawable/ic_quick_text_dark_theme",
          "@string/quick_text_smiley_key_unicode_people_output",
          "@string/quick_text_smiley_key_unicode_people_output",
          "",
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
      new GroupEmojiCollector(
          new None(),
          "quick_text_unicode_accessories.xml",
          "913d2e9a-2c77-46f3-a966-5ce17a3150ef",
          "@string/unicode_accessories_quick_text_key_name",
          "@drawable/ic_quick_text_dark_theme",
          "@string/quick_text_smiley_key_unicode_accessories_output",
          "@string/quick_text_smiley_key_unicode_accessories_output",
          "",
          "clothing");
  public static final EmojiCollector NATURE_COLLECTOR =
      new GroupEmojiCollector(
          new None(),
          "quick_text_unicode_nature.xml",
          "4a4501b4-0a1f-4fc3-9557-91538dd3f92a",
          "@string/unicode_nature_quick_text_key_name",
          "@drawable/ic_quick_text_dark_theme",
          "@string/quick_text_smiley_key_unicode_nature_output",
          "@string/quick_text_smiley_key_unicode_nature_output",
          "",
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
          new None(),
          "quick_text_unicode_food.xml",
          "1057806d-4f6e-42aa-8dfd-eea57995c2ee",
          "@string/unicode_food_quick_text_key_name",
          "@drawable/ic_quick_text_dark_theme",
          "@string/quick_text_smiley_key_unicode_food_output",
          "@string/quick_text_smiley_key_unicode_food_output",
          "",
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
          new None(),
          "quick_text_unicode_transport.xml",
          "032e0d3f-9281-4514-8c7c-c6cf3ff8b45b",
          "@string/unicode_transport_quick_text_key_name",
          "@drawable/ic_quick_text_dark_theme",
          "@string/quick_text_smiley_key_unicode_transport_output",
          "@string/quick_text_smiley_key_unicode_transport_output",
          "",
          "transport-ground",
          "transport-water",
          "transport-air");
  public static final EmojiCollector SIGNS_COLLECTOR =
      new GroupEmojiCollector(
          new None(),
          "quick_text_unicode_signs.xml",
          "a3e77c5f-133e-41d2-8449-6220daf9388b",
          "@string/unicode_signs_quick_text_key_name",
          "@drawable/ic_quick_text_dark_theme",
          "@string/quick_text_smiley_key_unicode_signs_output",
          "@string/quick_text_smiley_key_unicode_signs_output",
          "",
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
          new None(),
          "quick_text_unicode_scape.xml",
          "653e7203-5790-4042-9d5a-f2cb186ccaf9",
          "@string/unicode_scape_quick_text_key_name",
          "@drawable/ic_quick_text_dark_theme",
          "@string/quick_text_smiley_key_unicode_scape_output",
          "@string/quick_text_smiley_key_unicode_scape_output",
          "",
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
          new PersonDetector(),
          "quick_text_unicode_activity.xml",
          "a4dac174-81cf-41e6-a925-4ed3a32270c5",
          "@string/unicode_activity_quick_text_key_name",
          "@drawable/ic_quick_text_dark_theme",
          "@string/quick_text_smiley_key_unicode_activity_output",
          "@string/quick_text_smiley_key_unicode_activity_output",
          "",
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
          new None(),
          "quick_text_unicode_office.xml",
          "df479326-1728-44e8-a797-8e40ff0e2a41",
          "@string/unicode_office_quick_text_key_name",
          "@drawable/ic_quick_text_dark_theme",
          "@string/quick_text_smiley_key_unicode_office_output",
          "@string/quick_text_smiley_key_unicode_office_output",
          "",
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
          new None(),
          "quick_text_unicode_occasions.xml",
          "f8bfef54-db96-42e5-b3aa-d9e2ebd57a3a",
          "@string/unicode_occasions_quick_text_key_name",
          "@drawable/ic_quick_text_dark_theme",
          "@string/quick_text_smiley_key_unicode_occasions_output",
          "@string/quick_text_smiley_key_unicode_occasions_output",
          "",
          "event",
          "award-medal");
  public static final EmojiCollector FLAGS_COLLECTOR =
      new GroupEmojiCollector(
          new None(),
          "quick_text_unicode_flags.xml",
          "5afdb441-dbbe-46cf-98b8-2a5059e3b5e5",
          "@string/unicode_flags_quick_text_key_name",
          "@drawable/ic_quick_text_dark_theme",
          "@string/quick_text_smiley_key_unicode_flags_output",
          "@string/quick_text_smiley_key_unicode_flags_output",
          "",
          "flag",
          "country-flag",
          "subdivision-flag");
  public static final EmojiCollector UNCOLLECTED_COLLECTOR =
      new EmojiCollector(
          new None(),
          "quick_text_unicode_uncollected.xml",
          "",
          "",
          "",
          "",
          "",
          "DO NOT INCLUDE - holds emojis that were not collected") {
        @Override
        protected boolean isMyEmoji(EmojiData emojiData) {
          return true;
        }
      };

  static {
    ADDITION_TAGS_FOR_EMOJI.put("\uD83C\uDDE6\uD83C\uDDEA", Arrays.asList("UAE"));
    ADDITION_TAGS_FOR_EMOJI.put("\uD83D\uDE4F", Arrays.asList("pray", "thanks", "thank_you"));
    ADDITION_TAGS_FOR_EMOJI.put("\uD83C\uDDFA\uD83C\uDDF8", Arrays.asList("USA", "US"));
    ADDITION_TAGS_FOR_EMOJI.put("\uD83E\uDD17", Arrays.asList("hug"));
  }

  private final String mResourceFileName;
  private final List<EmojiData> mOwnedEmoji = new ArrayList<>();
  private final VariantDetector mVariantDetector;
  private final String mKeyboardId;
  private final String mNameResId;
  private final String mIconResId;
  private final String mLabelResId;
  private final String mDefaultOutputResId;
  private final String mDescription;

  protected EmojiCollector(
      VariantDetector variantDetector,
      String emojiKeyboardResourceFilename,
      String keyboardId,
      String nameResId,
      String iconResId,
      String labelResId,
      String defaultOutputResId,
      String description) {
    this.mResourceFileName = emojiKeyboardResourceFilename;
    this.mKeyboardId = keyboardId;
    this.mNameResId = nameResId;
    this.mIconResId = iconResId;
    this.mLabelResId = labelResId;
    this.mDefaultOutputResId = defaultOutputResId;
    this.mDescription = description;
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
            // add additional tags
            if (ADDITION_TAGS_FOR_EMOJI.containsKey(emojiData.output)) {
              emojiData.tags.addAll(ADDITION_TAGS_FOR_EMOJI.get(emojiData.output));
            }
          }
        });

    emojiDataList.removeIf(variants::contains);

    return emojiDataList;
  }

  private static class GroupEmojiCollector extends EmojiCollector {
    private final List<String> mSubGroups;

    protected GroupEmojiCollector(
        VariantDetector variantDetector,
        String emojiKeyboardResourceFilename,
        String keyboardId,
        String nameResId,
        String iconResId,
        String labelResId,
        String defaultOutputResId,
        String description,
        String... subgroups) {
      super(
          variantDetector,
          emojiKeyboardResourceFilename,
          keyboardId,
          nameResId,
          iconResId,
          labelResId,
          defaultOutputResId,
          description);
      mSubGroups = Arrays.asList(subgroups);
    }

    protected boolean isMyEmoji(EmojiData emojiData) {
      return mSubGroups.stream().anyMatch(emojiData.grouping::endsWith);
    }
  }
}
