package emojis;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
  private static final String SKIN_TONE = "skin tone";

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
        && !msAdditionalPersonGroups.containsKey(child.description)
        && isSkinToneVariantTag(parent, child);
  }

  private static boolean isSkinToneVariantTag(EmojiData parent, EmojiData child) {
    List<String> filterChild =
        child.tags.stream().filter(s -> !s.contains(SKIN_TONE)).collect(Collectors.toList());

    final boolean isNaiveMatch =
        parent.tags.stream().noneMatch(s -> s.contains(SKIN_TONE))
            && child.tags.stream().anyMatch(s -> s.contains(SKIN_TONE))
            && listsEqual(parent.tags, filterChild);
    final boolean isExceptionMatch =
        msAdditionalPersonGroups.containsKey(parent.description)
            && child.description.startsWith(msAdditionalPersonGroups.get(parent.description));

    return isNaiveMatch || isExceptionMatch;
  }

  private static boolean listsEqual(List<?> genders1, List<?> genders2) {
    if (genders1.size() == genders2.size()) {
      for (int genderIndex = 0; genderIndex < genders1.size(); genderIndex++) {
        if (!genders1.get(genderIndex).equals(genders2.get(genderIndex))) return false;
      }
      return true;
    }

    return false;
  }
}
