package emojis;

import java.util.List;

public interface EmojiCollection {

  String getResourceFileName();

  List<EmojiData> generateOwnedEmojis();
}
