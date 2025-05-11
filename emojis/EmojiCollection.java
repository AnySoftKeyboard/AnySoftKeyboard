package emojis;

import java.util.List;

public interface EmojiCollection {

  String getResourceFileName();

  String getKeyboardId();

  String getNameResId();

  String getIconResId();

  String getLabelResId();

  String getDefaultOutputResId();

  String getDescription();

  List<EmojiData> generateOwnedEmojis();
}
