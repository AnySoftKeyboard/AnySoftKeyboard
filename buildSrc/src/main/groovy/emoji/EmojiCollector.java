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
    }

    public static final EmojiCollector EMOTICONS_COLLECTOR = new EmojiCollector("quick_text_unicode_emoticons.xml") {
        @Override
        protected boolean isMyEmoji(EmojiData emojiData) {
            return emojiData.grouping.contains("Smileys") &&
                    (emojiData.grouping.contains("face-") || emojiData.grouping.contains("cat-face") ||
                            emojiData.grouping.contains("monkey-face") || emojiData.grouping.contains("skin-tone") ||
                            emojiData.grouping.contains("body") || emojiData.grouping.contains("emotion"));
        }
    };
    public static final EmojiCollector PEOPLE_COLLECTOR = new EmojiCollector("quick_text_unicode_people.xml") {
        @Override
        protected boolean isMyEmoji(EmojiData emojiData) {
            return emojiData.grouping.contains("Smileys") &&
                    (emojiData.grouping.contains("person") || emojiData.grouping.contains("family")) &&
                    !emojiData.grouping.contains("person-activity") &&
                    !emojiData.grouping.contains("person-sport");
        }
    };
    public static final EmojiCollector ACCESSORIES_COLLECTOR = new EmojiCollector("quick_text_unicode_accessories.xml") {
        @Override
        protected boolean isMyEmoji(EmojiData emojiData) {
            return emojiData.grouping.contains("Smileys") &&
                    (emojiData.grouping.contains("clothing"));
        }
    };
    public static final EmojiCollector NATURE_COLLECTOR = new EmojiCollector("quick_text_unicode_nature.xml") {
        @Override
        protected boolean isMyEmoji(EmojiData emojiData) {
            return emojiData.grouping.contains("Nature") ||
                    (emojiData.grouping.contains("Travel") &&
                            emojiData.grouping.contains("sky") && emojiData.grouping.contains("weather"));
        }
    };
    public static final EmojiCollector FOOD_COLLECTOR = new EmojiCollector("quick_text_unicode_food.xml") {
        @Override
        protected boolean isMyEmoji(EmojiData emojiData) {
            return emojiData.grouping.contains("Food") /*&&
                    (emojiData.grouping.contains("animal-") ||
                            emojiData.grouping.contains("plant-"))*/;
        }
    };
    public static final EmojiCollector TRANSPORT_COLLECTOR = new EmojiCollector("quick_text_unicode_transport.xml") {
        @Override
        protected boolean isMyEmoji(EmojiData emojiData) {
            return emojiData.grouping.contains("Travel") &&
                    emojiData.grouping.contains("transport-");
        }
    };
    public static final EmojiCollector SIGNS_COLLECTOR = new EmojiCollector("quick_text_unicode_signs.xml") {
        @Override
        protected boolean isMyEmoji(EmojiData emojiData) {
            return (emojiData.grouping.contains("Travel") &&
                    emojiData.grouping.contains("time")) ||
                    (emojiData.grouping.contains("Objects") && emojiData.grouping.contains("sound")) ||
                    emojiData.grouping.startsWith("Symbols");
        }
    };
    public static final EmojiCollector SCAPE_COLLECTOR = new EmojiCollector("quick_text_unicode_scape.xml") {
        @Override
        protected boolean isMyEmoji(EmojiData emojiData) {
            return (emojiData.grouping.contains("Travel") &&
                    !emojiData.grouping.contains("transport-") &&
                    !emojiData.grouping.contains("time")) ||
                    (emojiData.grouping.contains("Objects") &&
                            (emojiData.grouping.contains("household") || emojiData.grouping.contains("other-object")));
        }
    };
    public static final EmojiCollector ACTIVITY_COLLECTOR = new EmojiCollector("quick_text_unicode_activity.xml") {
        @Override
        protected boolean isMyEmoji(EmojiData emojiData) {
            return (emojiData.grouping.contains("Smileys") &&
                    (emojiData.grouping.contains("person-activity") || emojiData.grouping.contains("person-sport"))) ||
                    (emojiData.grouping.contains("Activities") &&
                            (emojiData.grouping.contains("sport") || emojiData.grouping.contains("game") || emojiData.grouping.contains("crafts"))) ||
                    (emojiData.grouping.contains("Objects") && emojiData.grouping.contains("music"));
        }
    };
    public static final EmojiCollector OFFICE_COLLECTOR = new EmojiCollector("quick_text_unicode_office.xml") {
        @Override
        protected boolean isMyEmoji(EmojiData emojiData) {
            return emojiData.grouping.contains("Objects") &&
                    (emojiData.grouping.contains("phone") || emojiData.grouping.contains("computer") ||
                            emojiData.grouping.contains("video") || emojiData.grouping.contains("book-paper") ||
                            emojiData.grouping.contains("money") || emojiData.grouping.contains("mail") ||
                            emojiData.grouping.contains("writing") || emojiData.grouping.contains("office") ||
                            emojiData.grouping.contains("lock") || emojiData.grouping.contains("tool") ||
                            emojiData.grouping.contains("science") || emojiData.grouping.contains("medical"));
        }
    };
    public static final EmojiCollector OCCASIONS_COLLECTOR = new EmojiCollector("quick_text_unicode_occasions.xml") {
        @Override
        protected boolean isMyEmoji(EmojiData emojiData) {
            return emojiData.grouping.contains("Activities") &&
                    (emojiData.grouping.contains("event") || emojiData.grouping.contains("award-medal"));
        }
    };
    public static final EmojiCollector FLAGS_COLLECTOR = new EmojiCollector("quick_text_unicode_flags.xml") {
        @Override
        protected boolean isMyEmoji(EmojiData emojiData) {
            return emojiData.grouping.startsWith("Flags");
        }
    };

    public static final EmojiCollector UNCOLLECTED_COLLECTOR = new EmojiCollector("quick_text_unicode_uncollected.xml") {
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

    private void sortEmojis(List<EmojiData> emojiDataList) {
        emojiDataList.sort(Comparator.comparingInt(o -> o.position));
    }
}
