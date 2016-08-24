package emoji;

class EmojiData {
    public final int index;
    public final String output;
    public final String name;
    public final String[] tags;

    EmojiData(int index, String output, String name, String[] tags) {
        this.index = index;
        this.output = output;
        this.name = name;
        this.tags = tags;
    }
}
