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
            return inIndexRange(emojiData, 1, 107,       1232, 1418,     1444, 1474);
        }
    };
    public static final EmojiCollector PEOPLE_COLLECTOR = new EmojiCollector("quick_text_unicode_people.xml") {
        @Override
        protected boolean isMyEmoji(EmojiData emojiData) {
            return inIndexRange(emojiData, 108, 797,     1195, 1231,      1425, 1443);
        }
    };
    public static final EmojiCollector ACCESSORIES_COLLECTOR = new EmojiCollector("quick_text_unicode_accessories.xml") {
        @Override
        protected boolean isMyEmoji(EmojiData emojiData) {
            return inIndexRange(emojiData, 1475, 1507);
        }
    };
    public static final EmojiCollector NATURE_COLLECTOR = new EmojiCollector("quick_text_unicode_nature.xml") {
        @Override
        protected boolean isMyEmoji(EmojiData emojiData) {
            return inIndexRange(emojiData, 1508, 1620,      1885, 1929);
        }
    };
    public static final EmojiCollector FOOD_COLLECTOR = new EmojiCollector("quick_text_unicode_food.xml") {
        @Override
        protected boolean isMyEmoji(EmojiData emojiData) {
            return inIndexRange(emojiData, 1621, 1722);
        }
    };
    public static final EmojiCollector TRANSPORT_COLLECTOR = new EmojiCollector("quick_text_unicode_transport.xml") {
        @Override
        protected boolean isMyEmoji(EmojiData emojiData) {
            return inIndexRange(emojiData, 1787, 1846);
        }
    };
    public static final EmojiCollector SIGNS_COLLECTOR = new EmojiCollector("quick_text_unicode_signs.xml") {
        @Override
        protected boolean isMyEmoji(EmojiData emojiData) {
            return inIndexRange(emojiData, 1854, 1884,      1990, 2001,     2152, 2356);
        }
    };
    public static final EmojiCollector SCAPE_COLLECTOR = new EmojiCollector("quick_text_unicode_scape.xml") {
        @Override
        protected boolean isMyEmoji(EmojiData emojiData) {
            return inIndexRange(emojiData, 1723, 1786,      1847, 1853);
        }
    };
    public static final EmojiCollector ACTIVITY_COLLECTOR = new EmojiCollector("quick_text_unicode_activity.xml") {
        @Override
        protected boolean isMyEmoji(EmojiData emojiData) {
            return inIndexRange(emojiData, 798, 1194,    1419, 1424,     1955, 1989,     2002, 2013,     2126, 2134);
        }
    };
    public static final EmojiCollector OFFICE_COLLECTOR = new EmojiCollector("quick_text_unicode_office.xml") {
        @Override
        protected boolean isMyEmoji(EmojiData emojiData) {
            return inIndexRange(emojiData, 2014, 2151);
        }
    };
    public static final EmojiCollector OCCASIONS_COLLECTOR = new EmojiCollector("quick_text_unicode_occasions.xml") {
        @Override
        protected boolean isMyEmoji(EmojiData emojiData) {
            return inIndexRange(emojiData, 1930, 1954);
        }
    };
    public static final EmojiCollector FLAGS_COLLECTOR = new EmojiCollector("quick_text_unicode_flags.xml") {
        @Override
        protected boolean isMyEmoji(EmojiData emojiData) {
            return inIndexRange(emojiData, 2357, 2623);
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
