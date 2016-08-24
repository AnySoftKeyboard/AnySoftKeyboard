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
            return ((emojiData.index >= 99 && emojiData.index <= 290) ||
                    (emojiData.index >= 315 && emojiData.index <= 352) ||
                    (emojiData.index >= 544 && emojiData.index <= 561) ||
                    (emojiData.index >= 358 && emojiData.index <= 537));
        }
    };
    public static final EmojiCollector ACTIVITY_COLLECTOR = new EmojiCollector("quick_text_unicode_activity.xml") {
        @Override
        protected boolean isMyEmoji(EmojiData emojiData) {
            return emojiData.index >= 291 && emojiData.index <= 314;
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
