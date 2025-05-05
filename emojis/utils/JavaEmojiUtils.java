package emojis.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains a few emoji related utils. This file is also used in the buildSrc and in the android
 * app!
 */
public class JavaEmojiUtils {

  private static final StringBuilder msStringBuilder = new StringBuilder(16);

  private static final char SKIN_TONE_PREFIX_CHAR = '\uD83C';
  private static final char ZWJ_CONNECTOR_PREFIX_CHAR = '\u200D';
  private static final char FULLY_QUALIFIED_POSTFIX = '\uFE0F';

  private static final Map<Character, SkinTone> msSkinTones;
  private static final Map<Character, Gender> msGenders;

  static {
    final HashMap<Character, SkinTone> skinTones = new HashMap<>();
    for (SkinTone value : SkinTone.values()) {
      skinTones.put(value.mModifier, value);
    }
    msSkinTones = Collections.unmodifiableMap(skinTones);

    final HashMap<Character, Gender> genders = new HashMap<>();
    for (Gender value : Gender.values()) {
      genders.put(value.mModifier, value);
    }
    msGenders = Collections.unmodifiableMap(genders);
  }

  public static boolean isLabelOfEmoji(CharSequence label) {
    if (label.length() == 0) return false;
    final char hs = label.charAt(0);

    return 0xd800 <= hs && hs <= 0xdbff;
  }

  public static boolean containsSkinTone(CharSequence text) {
    for (int charIndex = 0; charIndex < text.length() - 1; charIndex++) {
      final char c = text.charAt(charIndex);
      if (c == SKIN_TONE_PREFIX_CHAR && msSkinTones.containsKey(text.charAt(charIndex + 1))) {
        return true;
      }
    }

    return false;
  }

  public static boolean containsGender(CharSequence text) {
    for (int charIndex = 0; charIndex < text.length() - 1; charIndex++) {
      final char c = text.charAt(charIndex);
      if (c == ZWJ_CONNECTOR_PREFIX_CHAR && msGenders.containsKey(text.charAt(charIndex + 1))) {
        return true;
      }
    }

    return false;
  }

  public static boolean containsSkinTone(CharSequence text, SkinTone skinTone) {
    for (int charIndex = 0; charIndex < text.length() - 1; charIndex++) {
      final char c = text.charAt(charIndex);
      if (c == SKIN_TONE_PREFIX_CHAR && text.charAt(charIndex + 1) == skinTone.mModifier) {
        return true;
      }
    }

    return false;
  }

  public static CharSequence removeSkinTones(CharSequence text) {
    msStringBuilder.setLength(0);

    for (int charIndex = 0; charIndex < text.length(); charIndex++) {
      final char c = text.charAt(charIndex);
      if (c == SKIN_TONE_PREFIX_CHAR
          && charIndex < (text.length() - 1)
          && msSkinTones.containsKey(text.charAt(charIndex + 1))) {
        charIndex++; // skipping this and next
        if (charIndex < (text.length() - 2)
            && text.charAt(charIndex + 2) == FULLY_QUALIFIED_POSTFIX) {
          charIndex++; // also removing the fully-qualified char
        }
      } else {
        msStringBuilder.append(c);
      }
    }

    return msStringBuilder.toString();
  }

  public static List<SkinTone> getAllSkinTones(CharSequence text) {
    List<SkinTone> skinTones = new ArrayList<>();

    for (int charIndex = 0; charIndex < text.length(); charIndex++) {
      final char c = text.charAt(charIndex);
      if (c == SKIN_TONE_PREFIX_CHAR
          && charIndex < (text.length() - 1)
          && msSkinTones.containsKey(text.charAt(charIndex + 1))) {
        skinTones.add(msSkinTones.get(text.charAt(charIndex + 1)));
        charIndex++; // skipping this and next
        if (charIndex < (text.length() - 1)
            && text.charAt(charIndex + 1) == FULLY_QUALIFIED_POSTFIX) {
          charIndex++; // also removing the fully-qualified char
        }
      }
    }

    return skinTones;
  }

  public static CharSequence removeSkinTone(CharSequence text, SkinTone skinTone) {
    msStringBuilder.setLength(0);

    for (int charIndex = 0; charIndex < text.length(); charIndex++) {
      final char c = text.charAt(charIndex);
      if (c == SKIN_TONE_PREFIX_CHAR
          && charIndex < (text.length() - 1)
          && text.charAt(charIndex + 1) == skinTone.mModifier) {
        charIndex++; // skipping this and next
        if (charIndex < (text.length() - 2)
            && text.charAt(charIndex + 2) == FULLY_QUALIFIED_POSTFIX) {
          charIndex++; // also removing the fully-qualified char
        }
      } else {
        msStringBuilder.append(c);
      }
    }

    return msStringBuilder.toString();
  }

  public static boolean containsGender(CharSequence text, Gender gender) {
    for (int charIndex = 0; charIndex < text.length() - 1; charIndex++) {
      final char c = text.charAt(charIndex);
      if (c == ZWJ_CONNECTOR_PREFIX_CHAR && text.charAt(charIndex + 1) == gender.mModifier) {
        return true;
      }
    }

    return false;
  }

  public static CharSequence removeGenders(CharSequence text) {
    msStringBuilder.setLength(0);

    for (int charIndex = 0; charIndex < text.length(); charIndex++) {
      final char c = text.charAt(charIndex);
      if (c == ZWJ_CONNECTOR_PREFIX_CHAR
          && charIndex < (text.length() - 1)
          && msGenders.containsKey(text.charAt(charIndex + 1))) {
        charIndex++; // skipping this and next
        if (charIndex < (text.length() - 1)
            && text.charAt(charIndex + 1) == FULLY_QUALIFIED_POSTFIX) {
          charIndex++; // also removing the fully-qualified char
        }
      } else {
        msStringBuilder.append(c);
      }
    }

    return msStringBuilder.toString();
  }

  public static List<Gender> getAllGenders(CharSequence text) {
    List<Gender> genders = new ArrayList<>();

    for (int charIndex = 0; charIndex < text.length(); charIndex++) {
      final char c = text.charAt(charIndex);
      if (c == ZWJ_CONNECTOR_PREFIX_CHAR
          && charIndex < (text.length() - 1)
          && msGenders.containsKey(text.charAt(charIndex + 1))) {
        genders.add(msGenders.get(text.charAt(charIndex + 1)));
        charIndex++; // skipping this and next
        if (charIndex < (text.length() - 1)
            && text.charAt(charIndex + 1) == FULLY_QUALIFIED_POSTFIX) {
          charIndex++; // also removing the fully-qualified char
        }
      }
    }

    return genders;
  }

  public static CharSequence removeGender(CharSequence text, Gender gender) {
    msStringBuilder.setLength(0);

    for (int charIndex = 0; charIndex < text.length(); charIndex++) {
      final char c = text.charAt(charIndex);
      if (c == ZWJ_CONNECTOR_PREFIX_CHAR
          && charIndex < (text.length() - 1)
          && text.charAt(charIndex + 1) == gender.mModifier) {
        charIndex++; // skipping this and next
        if (charIndex < (text.length() - 1)
            && text.charAt(charIndex + 1) == FULLY_QUALIFIED_POSTFIX) {
          charIndex++; // also removing the fully-qualified char
        }
      } else {
        msStringBuilder.append(c);
      }
    }

    return msStringBuilder.toString();
  }

  public enum SkinTone {
    // Fitzpatrick_1('\uDFFA'),//does not exist
    Fitzpatrick_2('\uDFFB'),
    Fitzpatrick_3('\uDFFC'),
    Fitzpatrick_4('\uDFFD'),
    Fitzpatrick_5('\uDFFE'),
    Fitzpatrick_6('\uDFFF');

    private final char mModifier;

    SkinTone(char modifier) {
      mModifier = modifier;
    }
  }

  public enum Gender {
    Woman('\u2640'),
    Man('\u2642');

    private final char mModifier;

    Gender(char modifier) {
      mModifier = modifier;
    }
  }
}
