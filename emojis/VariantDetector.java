package emojis;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

interface VariantDetector {
  boolean isVariant(EmojiData parent, EmojiData child);
}

class None implements VariantDetector {

  @Override
  public boolean isVariant(EmojiData parent, EmojiData child) {
    return false;
  }
}

class PersonDetector implements VariantDetector {

  private static final Map<String, String> msAdditionalPersonGroups;

  static {
    Map<String, String> additional = new HashMap<>();
    addAllSkinTones(additional, "kiss: $[TONE]", "kiss: person, person, $[TONE], ", "$[TONE]");
    msAdditionalPersonGroups = Collections.unmodifiableMap(additional);
  }

  private static void addAllSkinTones(
      Map<String, String> map, String key, String value, String marker) {
    final String[] tones =
        new String[] {
          "light skin tone",
          "medium-light skin tone",
          "medium skin tone",
          "medium-dark skin tone",
          "dark skin tone"
        };
    for (String tone : tones) {
      map.put(key.replaceAll(marker, tone), value.replaceAll(marker, tone));
    }
  }

  @Override
  public boolean isVariant(EmojiData parent, EmojiData child) {
    return !parent.output.equals(child.output)
        && parent.tags.equals(child.tags)
        && !msAdditionalPersonGroups.containsKey(child.description)
        && parent.orderedSkinTones.isEmpty()
        && !child.orderedSkinTones.isEmpty();
  }
}
