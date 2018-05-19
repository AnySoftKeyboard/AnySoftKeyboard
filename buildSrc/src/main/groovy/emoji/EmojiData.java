package emoji;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class EmojiData {
    public final int position;
    public final String grouping;
    public final String output;
    public final List<String> tags;

    private final List<String> mVariants = new ArrayList<>();

    EmojiData(int position, String grouping, String output, List<String> tags) {
        this.position = position;
        this.grouping = grouping;
        this.output = output;
        this.tags = tags;
    }

    public void addVariant(EmojiData variant) {
        mVariants.add(variant.output);
    }

    public List<String> getVariants() {
        return Collections.unmodifiableList(mVariants);
    }
}
