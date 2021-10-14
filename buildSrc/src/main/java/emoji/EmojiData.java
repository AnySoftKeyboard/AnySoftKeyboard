package emoji;

import emoji.utils.JavaEmojiUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class EmojiData {
    private static final char FULLY_QUALIFIED_POSTFIX = '\uFE0F';
    public final int position;
    public final String description;
    public final List<JavaEmojiUtils.Gender> orderedGenders;
    public final List<JavaEmojiUtils.SkinTone> orderedSkinTones;
    public final String grouping;
    public final String output;
    public final String baseOutputDescription;
    public final List<String> tags;
    private final int[] uniqueOutput;
    private final List<String> mVariants = new ArrayList<>();

    EmojiData(int position, String description, String grouping, String output, List<String> tags) {
        if (description.contains("older person"))
            description = description.replace("older person", "old person");
        this.position = position;
        this.grouping = grouping;
        this.description = description;
        this.output = output;
        this.tags = tags;
        uniqueOutput = output.chars().filter(i -> i != FULLY_QUALIFIED_POSTFIX).toArray();
        int baseOutputBreaker = description.indexOf(':');
        if (baseOutputBreaker == -1) baseOutputBreaker = description.length();
        baseOutputDescription = description.substring(0, baseOutputBreaker);
        orderedGenders = JavaEmojiUtils.getAllGenders(output);
        // extracting genders from description
        orderedSkinTones = JavaEmojiUtils.getAllSkinTones(output);
    }

    public void addVariant(EmojiData variant) {
        mVariants.add(variant.output);
    }

    public List<String> getVariants() {
        return Collections.unmodifiableList(mVariants);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmojiData emojiData = (EmojiData) o;
        return Arrays.equals(uniqueOutput, emojiData.uniqueOutput);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(uniqueOutput);
    }
}
