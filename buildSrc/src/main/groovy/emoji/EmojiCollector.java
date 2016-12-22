package emoji;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//(emojiData.index >=  && emojiData.index <= )
//inIndexRange(emojiData, , )
public abstract class EmojiCollector implements EmojiCollection {

    private static boolean inIndexRange(EmojiData emojiData, int... ranges) {
        if (ranges.length % 2 != 0) throw new IllegalArgumentException("Ranges come in pairs: low, high.");
        for (int rangePairIndex=0; rangePairIndex<ranges.length; rangePairIndex+=2) {
            if (emojiData.index >= ranges[rangePairIndex] && emojiData.index <= ranges[rangePairIndex+1]) return true;
        }

        return false;
    }

    public static final EmojiCollector EMOTICONS_COLLECTOR = new EmojiCollector("quick_text_unicode_emoticons.xml") {
        @Override
        protected boolean isMyEmoji(EmojiData emojiData) {
            return inIndexRange(emojiData, 1, 98,       1030, 1204,     1229, 1258);
        }
    };
    public static final EmojiCollector PEOPLE_COLLECTOR = new EmojiCollector("quick_text_unicode_people.xml") {
        @Override
        protected boolean isMyEmoji(EmojiData emojiData) {
            return inIndexRange(emojiData, 99, 656,     988, 1024,      1211, 1228);
        }
    };
    public static final EmojiCollector ACCESSORIES_COLLECTOR = new EmojiCollector("quick_text_unicode_accessories.xml") {
        @Override
        protected boolean isMyEmoji(EmojiData emojiData) {
            return inIndexRange(emojiData, 1259, 1286);
        }
    };
    public static final EmojiCollector NATURE_COLLECTOR = new EmojiCollector("quick_text_unicode_nature.xml") {
        @Override
        protected boolean isMyEmoji(EmojiData emojiData) {
            return inIndexRange(emojiData, 1287, 1393,      1656, 1700);
        }
    };
    public static final EmojiCollector FOOD_COLLECTOR = new EmojiCollector("quick_text_unicode_food.xml") {
        @Override
        protected boolean isMyEmoji(EmojiData emojiData) {
            return inIndexRange(emojiData, 1394, 1482);
        }
    };
    public static final EmojiCollector TRANSPORT_COLLECTOR = new EmojiCollector("quick_text_unicode_transport.xml") {
        @Override
        protected boolean isMyEmoji(EmojiData emojiData) {
            return inIndexRange(emojiData, 1547, 1605);
        }
    };
    public static final EmojiCollector ACTIVITY_COLLECTOR = new EmojiCollector("quick_text_unicode_activity.xml") {
        @Override
        protected boolean isMyEmoji(EmojiData emojiData) {
            return inIndexRange(emojiData, 657, 987,    1205, 1210,     1726, 1758,     1771, 1782,     1895, 1903);
        }
    };
    public static final EmojiCollector SCAPE_COLLECTOR = new EmojiCollector("quick_text_unicode_scape.xml") {
        @Override
        protected boolean isMyEmoji(EmojiData emojiData) {
            return inIndexRange(emojiData, 1483, 1546,      1606, 1624);
        }
    };
    public static final EmojiCollector OFFICE_COLLECTOR = new EmojiCollector("quick_text_unicode_office.xml") {
        @Override
        protected boolean isMyEmoji(EmojiData emojiData) {
            return inIndexRange(emojiData, 1783, 1894,      1904, 1920);
        }
    };
    public static final EmojiCollector SIGNS_COLLECTOR = new EmojiCollector("quick_text_unicode_signs.xml") {
        @Override
        protected boolean isMyEmoji(EmojiData emojiData) {
            return inIndexRange(emojiData, 1625, 1655,      1759, 1770,     1921, 2125);
        }
    };
    public static final EmojiCollector OCCASIONS_COLLECTOR = new EmojiCollector("quick_text_unicode_occasions.xml") {
        @Override
        protected boolean isMyEmoji(EmojiData emojiData) {
            return inIndexRange(emojiData, 1701, 1725);
        }
    };
    public static final EmojiCollector FLAGS_COLLECTOR = new EmojiCollector("quick_text_unicode_flags.xml") {
        @Override
        protected boolean isMyEmoji(EmojiData emojiData) {
            return inIndexRange(emojiData, 2126, 2389);
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

    protected void sortEmojis(List<EmojiData> emojiDataList) {
        Collections.sort(emojiDataList, (o1, o2) -> o1.index - o2.index);
    }
}
