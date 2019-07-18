package emoji;

import java.util.List;

public interface EmojiCollection {

    String getResourceFileName();

    List<EmojiData> getOwnedEmjois();
}
