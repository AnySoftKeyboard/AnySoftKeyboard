package emoji;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class EmojiCollector {
    public static final EmojiCollector EMOTICONS_COLLECTOR = new EmojiCollector("quick_text_unicode_emoticons.xml") {
        @Override
        protected boolean isMyEmoji(EmojiData emojiData) {
            return emojiData.index >= 1 && emojiData.index <= 98;
        }
    };
    public static final EmojiCollector PEOPLE_COLLECTOR = new EmojiCollector("quick_text_unicode_people.xml") {
        @Override
        protected boolean isMyEmoji(EmojiData emojiData) {
            return (emojiData.index >= 99 && emojiData.index <= 290) ||
                    (emojiData.index >= 315 && emojiData.index <= 352) ||
                    (emojiData.index >= 544 && emojiData.index <= 583) ||
                    (emojiData.index >= 358 && emojiData.index <= 537) ||
                    (emojiData.index >= 585 && emojiData.index <= 586);
        }
    };
    public static final EmojiCollector ACCESSORIES_COLLECTOR = new EmojiCollector("quick_text_unicode_accessories.xml") {
        @Override
        protected boolean isMyEmoji(EmojiData emojiData) {
            return (emojiData.index >= 592 && emojiData.index <= 619);
        }
    };
    public static final EmojiCollector ANIMALS_COLLECTOR = new EmojiCollector("quick_text_unicode_animals.xml") {
        @Override
        protected boolean isMyEmoji(EmojiData emojiData) {
            return (emojiData.index >= 620 && emojiData.index <= 704);
        }
    };
    public static final EmojiCollector NATURE_COLLECTOR = new EmojiCollector("quick_text_unicode_nature.xml") {
        @Override
        protected boolean isMyEmoji(EmojiData emojiData) {
            return (emojiData.index >= 705 && emojiData.index <= 751) ||
                    (emojiData.index >= 816 && emojiData.index <= 825) ||
                    (emojiData.index >= 864 && emojiData.index <= 865) ||
                    (emojiData.index >= 869 && emojiData.index <= 870) ||
                    (emojiData.index >= 990 && emojiData.index <= 1034) ||
                    (emojiData.index == 584);
        }
    };
    public static final EmojiCollector FOOD_COLLECTOR = new EmojiCollector("quick_text_unicode_food.xml") {
        @Override
        protected boolean isMyEmoji(EmojiData emojiData) {
            return (emojiData.index >= 752 && emojiData.index <= 815);
        }
    };
    public static final EmojiCollector TRANSPORT_COLLECTOR = new EmojiCollector("quick_text_unicode_transport.xml") {
        @Override
        protected boolean isMyEmoji(EmojiData emojiData) {
            return (emojiData.index >= 880 && emojiData.index <= 944);
        }
    };
    public static final EmojiCollector ACTIVITY_COLLECTOR = new EmojiCollector("quick_text_unicode_activity.xml") {
        @Override
        protected boolean isMyEmoji(EmojiData emojiData) {
            return (emojiData.index >= 538 && emojiData.index <= 543) ||
                    (emojiData.index >= 826 && emojiData.index <= 830) ||
                    (emojiData.index >= 291 && emojiData.index <= 314) ||
                    (emojiData.index >= 871 && emojiData.index <= 879) ||
                    (emojiData.index >= 947 && emojiData.index <= 958) ||
                    (emojiData.index >= 1060 && emojiData.index <= 1158) ||
                    (emojiData.index >= 1178 && emojiData.index <= 1189) ||
                    (emojiData.index >= 1302 && emojiData.index <= 1310) ||
                    (emojiData.index >= 1319 && emojiData.index <= 1323);
        }
    };
    public static final EmojiCollector CITY_COLLECTOR = new EmojiCollector("quick_text_unicode_city.xml") {
        @Override
        protected boolean isMyEmoji(EmojiData emojiData) {
            return (emojiData.index >= 831 && emojiData.index <= 863) ||
                    (emojiData.index >= 866 && emojiData.index <= 868) ||
                    (emojiData.index >= 945 && emojiData.index <= 946) ||
                    (emojiData.index == 1324);
        }
    };
    public static final EmojiCollector OFFICE_COLLECTOR = new EmojiCollector("quick_text_unicode_office.xml") {
        @Override
        protected boolean isMyEmoji(EmojiData emojiData) {
            return (emojiData.index >= 1190 && emojiData.index <= 1301) ||
                    (emojiData.index >= 1311 && emojiData.index <= 1318);
        }
    };
    public static final EmojiCollector SIGNS_COLLECTOR = new EmojiCollector("quick_text_unicode_signs.xml") {
        @Override
        protected boolean isMyEmoji(EmojiData emojiData) {
            return (emojiData.index >= 959 && emojiData.index <= 989) ||
                    (emojiData.index >= 1159 && emojiData.index <= 1177) ||
                    (emojiData.index >= 1325 && emojiData.index <= 1529) ||
                    (emojiData.index >= 587 && emojiData.index <= 591);
        }
    };
    public static final EmojiCollector OCCASIONS_COLLECTOR = new EmojiCollector("quick_text_unicode_occasions.xml") {
        @Override
        protected boolean isMyEmoji(EmojiData emojiData) {
            return (emojiData.index >= 1035 && emojiData.index <= 1059);
        }
    };

    public static final EmojiCollector FLAGS_COLLECTOR = new EmojiCollector("quick_text_unicode_flags.xml") {
        @Override
        protected boolean isMyEmoji(EmojiData emojiData) {
            return emojiData.name.contains("REGIONAL INDICATOR") || (emojiData.index >= 1530 && emojiData.index <= 1534);
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

    String getResourceFileName() {
        return mResourceFileName;
    }

    List<EmojiData> getOwnedEmjois() {
        sortEmojis(mOwnedEmoji);
        return mOwnedEmoji;
    }

    protected void sortEmojis(List<EmojiData> emojiDataList) {
        Collections.sort(emojiDataList, (o1, o2) -> o1.index - o2.index);
    }
}
