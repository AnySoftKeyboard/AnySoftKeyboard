package emoji;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class EmojiCollector implements EmojiCollection {

    public static final Map<String, List<String>> ADDITION_TAGS_FOR_EMOJI = new HashMap<>();

    static {
        ADDITION_TAGS_FOR_EMOJI.put("\uD83C\uDDE6\uD83C\uDDEA", Arrays.asList("UAE"));
        ADDITION_TAGS_FOR_EMOJI.put("\uD83D\uDE4F", Arrays.asList("pray"));
        ADDITION_TAGS_FOR_EMOJI.put("\uD83C\uDDFA\uD83C\uDDF8", Arrays.asList("USA", "US"));
    }

    public static final EmojiCollector EMOTICONS_COLLECTOR =
            new CompositeEmojiCollector(
                    "quick_text_unicode_emoticons.xml",
                    new GroupEmojiCollector(
                            "quick_text_unicode_emoticons.xml",
                            "Smileys",
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
                            "emotion"),
                    new GroupEmojiCollector(
                            "quick_text_unicode_emoticons.xml",
                            "People",
                            "hand-fingers-open",
                            "hand-fingers-partial",
                            "hand-single-finger",
                            "hand-fingers-closed",
                            "hands"));

    public static final EmojiCollector PEOPLE_COLLECTOR =
            new CompositeEmojiCollector(
                    "quick_text_unicode_people.xml",
                    new GroupEmojiCollector(
                            "quick_text_unicode_people.xml",
                            "People",
                            "hand-prop",
                            "body-parts",
                            "person",
                            "person-gesture",
                            "person-role",
                            "person-fantasy",
                            "family",
                            "person-symbol"),
                    new GroupEmojiCollector(
                            "quick_text_unicode_people.xml",
                            "Component",
                            "skin-tone",
                            "hair-style"));

    public static final EmojiCollector ACCESSORIES_COLLECTOR =
            new GroupEmojiCollector("quick_text_unicode_accessories.xml", "Objects", "clothing");

    public static final EmojiCollector NATURE_COLLECTOR =
            new GroupEmojiCollector(
                    "quick_text_unicode_nature.xml",
                    "Animals",
                    "animal-mammal",
                    "animal-bird",
                    "animal-amphibian",
                    "animal-reptile",
                    "animal-marine",
                    "animal-bug",
                    "plant-flower",
                    "plant-other");

    public static final EmojiCollector FOOD_COLLECTOR =
            new GroupEmojiCollector(
                    "quick_text_unicode_food.xml",
                    "Food",
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
                    "Travel",
                    "transport-ground",
                    "transport-water",
                    "transport-air");

    public static final EmojiCollector SIGNS_COLLECTOR =
            new CompositeEmojiCollector(
                    "quick_text_unicode_signs.xml",
                    new GroupEmojiCollector("quick_text_unicode_signs.xml", "Travel", "time"),
                    new GroupEmojiCollector("quick_text_unicode_signs.xml", "Objects", "sound"),
                    new GroupEmojiCollector(
                            "quick_text_unicode_signs.xml",
                            "Symbols",
                            "transport-sign",
                            "warning",
                            "arrow",
                            "religion",
                            "zodiac",
                            "av-symbol",
                            "gender",
                            "other-symbol",
                            "keycap",
                            "alphanum",
                            "geometric"));

    public static final EmojiCollector SCAPE_COLLECTOR =
            new CompositeEmojiCollector(
                    "quick_text_unicode_scape.xml",
                    new GroupEmojiCollector(
                            "quick_text_unicode_scape.xml",
                            "Travel",
                            "place-map",
                            "place-geographic",
                            "place-building",
                            "place-religious",
                            "place-other",
                            "hotel",
                            "weather"),
                    new GroupEmojiCollector(
                            "quick_text_unicode_scape.xml", "Objects", "household"));

    public static final EmojiCollector ACTIVITY_COLLECTOR =
            new CompositeEmojiCollector(
                    "quick_text_unicode_activity.xml",
                    new GroupEmojiCollector(
                            "quick_text_unicode_activity.xml",
                            "People",
                            "person-activity",
                            "person-sport",
                            "person-resting"),
                    new GroupEmojiCollector(
                            "quick_text_unicode_activity.xml",
                            "Activities",
                            "sport",
                            "game",
                            "crafts"),
                    new GroupEmojiCollector(
                            "quick_text_unicode_activity.xml",
                            "Objects",
                            "music",
                            "musical-instrument"));

    public static final EmojiCollector OFFICE_COLLECTOR =
            new CompositeEmojiCollector(
                    "quick_text_unicode_office.xml",
                    new GroupEmojiCollector(
                            "quick_text_unicode_office.xml",
                            "Objects",
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
                            "other-object"));

    public static final EmojiCollector OCCASIONS_COLLECTOR =
            new GroupEmojiCollector(
                    "quick_text_unicode_occasions.xml", "Activities", "event", "award-medal");

    public static final EmojiCollector FLAGS_COLLECTOR =
            new GroupEmojiCollector(
                    "quick_text_unicode_flags.xml",
                    "Flags",
                    "flag",
                    "country-flag",
                    "subdivision-flag");

    public static final EmojiCollector UNCOLLECTED_COLLECTOR =
            new EmojiCollector("quick_text_unicode_uncollected.xml") {
                @Override
                protected boolean isMyEmoji(EmojiData emojiData) {
                    return true;
                }
            };
    private final String mResourceFileName;
    private final List<EmojiData> mOwnedEmoji = new ArrayList<>();

    protected EmojiCollector(String emojiKeyboardResourceFilename) {
        mResourceFileName = emojiKeyboardResourceFilename;
    }

    boolean visitEmoji(EmojiData emojiData) {
        if (isMyEmoji(emojiData)) {
            mOwnedEmoji.add(emojiData);
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
    public List<EmojiData> getOwnedEmjois() {
        sortEmojis(mOwnedEmoji);
        return mOwnedEmoji;
    }

    private static class GroupEmojiCollector extends EmojiCollector {
        private final String mGroup;
        private final List<String> mSubGroups;

        protected GroupEmojiCollector(
                String emojiKeyboardResourceFilename, String group, String... subgroups) {
            super(emojiKeyboardResourceFilename);
            mGroup = group;
            mSubGroups = Arrays.asList(subgroups);
        }

        protected boolean isMyEmoji(EmojiData emojiData) {
            return emojiData.grouping.startsWith(mGroup)
                    && mSubGroups.stream().anyMatch(emojiData.grouping::endsWith);
        }
    }

    private static class CompositeEmojiCollector extends EmojiCollector {
        private final List<EmojiCollector> mChildren;

        protected CompositeEmojiCollector(
                String emojiKeyboardResourceFilename, EmojiCollector... children) {
            super(emojiKeyboardResourceFilename);
            mChildren = Arrays.asList(children);
        }

        protected boolean isMyEmoji(EmojiData emojiData) {
            return mChildren.stream().anyMatch(child -> child.isMyEmoji(emojiData));
        }
    }

    private void sortEmojis(List<EmojiData> emojiDataList) {
        emojiDataList.sort(Comparator.comparingInt(o -> o.position));
    }
}
