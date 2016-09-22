package emoji;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class EmojiData {
    public final int index;
    public final String output;
    public final String name;
    public final String[] tags;

    private final List<String> mVariants = new ArrayList<>();

    EmojiData(int index, String output, String name, String[] tags) {
        this.index = index;
        this.output = output;
        this.name = name;
        this.tags = tags;
    }

    public void addVariant(EmojiData variant) {
        mVariants.add(variant.output);
    }

    public List<String> getVariants() {
        return Collections.unmodifiableList(mVariants);
    }
}
