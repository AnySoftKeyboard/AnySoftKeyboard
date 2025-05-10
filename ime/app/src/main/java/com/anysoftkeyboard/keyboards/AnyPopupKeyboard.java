/*
 * Copyright (c) 2013 Menny Even-Danan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.anysoftkeyboard.keyboards;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.graphics.Paint;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Predicate;
import com.anysoftkeyboard.addons.AddOn;
import com.anysoftkeyboard.keyboardextensions.KeyboardExtension;
import com.anysoftkeyboard.utils.EmojiUtils;
import com.menny.android.anysoftkeyboard.R;
import java.util.List;

public class AnyPopupKeyboard extends AnyKeyboard {

  private static final char[] EMPTY_CHAR_ARRAY = new char[0];
  private static final int MAX_KEYS_PER_ROW = 5;
  private final CharSequence mKeyboardName;
  @Nullable private final EmojiUtils.SkinTone mDefaultSkinTone;
  @Nullable private final EmojiUtils.Gender mDefaultGender;
  private final Paint mPaint = new Paint();
  private int mAdditionalWidth = 0;

  public AnyPopupKeyboard(
      @NonNull AddOn keyboardAddOn,
      Context askContext,
      int xmlLayoutResId,
      @NonNull final KeyboardDimens keyboardDimens,
      @NonNull CharSequence keyboardName,
      @Nullable EmojiUtils.SkinTone defaultSkinTone,
      @Nullable EmojiUtils.Gender defaultGender) {
    super(keyboardAddOn, askContext, xmlLayoutResId, KEYBOARD_ROW_MODE_NORMAL);
    mDefaultSkinTone = defaultSkinTone;
    mDefaultGender = defaultGender;
    mKeyboardName = keyboardName;
    loadKeyboard(keyboardDimens);
  }

  public AnyPopupKeyboard(
      @NonNull AddOn keyboardAddOn,
      @NonNull Context askContext,
      CharSequence popupCharacters,
      @NonNull final KeyboardDimens keyboardDimens,
      @NonNull String keyboardName) {
    super(keyboardAddOn, askContext, askContext, getPopupLayout(popupCharacters));
    mDefaultSkinTone = null;
    mDefaultGender = null;
    mKeyboardName = keyboardName;
    loadKeyboard(keyboardDimens);

    final int rowsCount = getPopupRowsCount(popupCharacters);
    final int popupCharactersLength =
        Character.codePointCount(popupCharacters, 0, popupCharacters.length());

    List<Key> keys = getKeys();
    for (int rowIndex = 0; rowIndex < rowsCount; rowIndex++) {
      // initially, the layout is populated (via the xml layout)
      // with one key per row. This initial key will set the
      // base X and Y to use for the following keys in the row.
      // In addPopupKeysToList we inserting keys by rows, at the correct
      // insert index.
      int characterOffset = rowIndex * MAX_KEYS_PER_ROW;
      addPopupKeysToList(
          keyboardDimens,
          keys,
          popupCharacters,
          rowsCount - rowIndex - 1,
          characterOffset,
          Math.min(MAX_KEYS_PER_ROW, popupCharactersLength - characterOffset));
    }
  }

  private static int getPopupLayout(CharSequence popupCharacters) {
    return switch (getPopupRowsCount(popupCharacters)) {
      case 1 -> R.xml.popup_one_row;
      case 2 -> R.xml.popup_two_rows;
      case 3 -> R.xml.popup_three_rows;
      default -> throw new RuntimeException("AnyPopupKeyboard supports 1, 2, and 3 rows only!");
    };
  }

  @Override
  public boolean isAlphabetKeyboard() {
    return false;
  }

  private static int getPopupRowsCount(CharSequence popupCharacters) {
    final double count = Character.codePointCount(popupCharacters, 0, popupCharacters.length());
    return (int) Math.min(3.0 /*no more than three rows*/, Math.ceil(count / MAX_KEYS_PER_ROW));
  }

  @Nullable
  private static Key findKeyWithSkinToneAndGender(
      List<Key> keys, @Nullable EmojiUtils.SkinTone skinTone, @Nullable EmojiUtils.Gender gender) {
    final Predicate<AnyKey> checker;
    if (skinTone != null && gender != null) {
      checker = aKey -> aKey.getSkinTones().contains(skinTone)
      /*&& EmojiUtils.containsGender(text, gender)*/ ;
    } else if (skinTone != null) {
      checker = aKey -> aKey.getSkinTones().contains(skinTone);
    } /*else if (gender != null) {
          checker = text -> EmojiUtils.containsGender(text, gender);
      } */ else {
      throw new IllegalArgumentException("can not have both skin-tone and gender set to null!");
    }

    return findKeyWithPredicate(keys, checker);
  }

  @Nullable
  private static AnyKey findKeyWithPredicate(List<Key> keys, Predicate<AnyKey> checker) {
    for (Key key : keys) {
      var aKey = (AnyKey) key;
      if (checker.test(aKey)) {
        return aKey;
      }
    }

    return null;
  }

  private void addPopupKeysToList(
      KeyboardDimens keyboardDimens,
      List<Key> keys,
      CharSequence popupCharacters,
      int baseKeyIndex,
      int characterOffset,
      int keysPerRow) {
    int rowWidth = 0;
    // the base key is the same index as the character offset
    // since we are starting with an empty row
    AnyKey baseKey = (AnyKey) keys.get(baseKeyIndex);
    Row row = baseKey.row;
    // now adding the popups
    final float y = baseKey.y;
    final float keyHorizontalGap = row.defaultHorizontalGap;
    int popupCharacter =
        Character.codePointAt(
            popupCharacters, Character.offsetByCodePoints(popupCharacters, 0, characterOffset));
    baseKey.mCodes = new int[] {popupCharacter};
    baseKey.label = new String(new int[] {popupCharacter}, 0, 1);
    int upperCasePopupCharacter = Character.toUpperCase(popupCharacter);
    baseKey.mShiftedCodes = new int[] {upperCasePopupCharacter};
    float x = baseKey.width;
    AnyKey aKey = null;
    final int popupCharactersLength =
        Character.codePointCount(popupCharacters, 0, popupCharacters.length());
    for (int popupCharIndex = characterOffset + 1;
        popupCharIndex < characterOffset + keysPerRow && popupCharIndex < popupCharactersLength;
        popupCharIndex++) {
      x += (keyHorizontalGap / 2);

      aKey = new AnyKey(row, keyboardDimens);
      popupCharacter =
          Character.codePointAt(
              popupCharacters, Character.offsetByCodePoints(popupCharacters, 0, popupCharIndex));
      aKey.mCodes = new int[] {popupCharacter};
      aKey.label = new String(new int[] {popupCharacter}, 0, 1);
      upperCasePopupCharacter = Character.toUpperCase(popupCharacter);
      aKey.mShiftedCodes = new int[] {upperCasePopupCharacter};
      aKey.width = (int) (aKey.width - keyHorizontalGap); // the gap is on both sides
      aKey.x = (int) x;
      aKey.y = (int) y;
      final int xOffset = (int) (aKey.width + keyHorizontalGap + (keyHorizontalGap / 2));
      x += xOffset;
      rowWidth += xOffset;
      keys.add(aKey);
    }
    // adding edge flag to the last key
    baseKey.edgeFlags = EDGE_LEFT;
    // this holds the last key
    if (aKey != null) {
      aKey.edgeFlags = EDGE_RIGHT;
    } else {
      baseKey.edgeFlags |=
          EDGE_RIGHT; // adding another flag, since the baseKey is the only one in the row
    }

    mAdditionalWidth = Math.max(rowWidth, mAdditionalWidth);
  }

  @Override
  public char[] getSentenceSeparators() {
    return EMPTY_CHAR_ARRAY;
  }

  @Override
  public int getMinWidth() {
    return super.getMinWidth() + mAdditionalWidth;
  }

  @Override
  public String getDefaultDictionaryLocale() {
    return null;
  }

  @Override
  @NonNull
  public CharSequence getKeyboardName() {
    return mKeyboardName;
  }

  @Override
  public int getKeyboardIconResId() {
    return -1;
  }

  @NonNull
  @Override
  public String getKeyboardId() {
    return "keyboard_popup";
  }

  @Override
  protected void addGenericRows(
      @NonNull KeyboardDimens keyboardDimens,
      @Nullable KeyboardExtension topRowPlugin,
      @NonNull KeyboardExtension bottomRowPlugin) {
    // no generic rows in popups, only in main keyboard
  }

  @Override
  public boolean keyboardSupportShift() {
    // forcing this, so the mParent keyboard will determine the shift value
    return true;
  }

  @Override
  protected Key createKeyFromXml(
      @NonNull AddOn.AddOnResourceMapping resourceMapping,
      Context askContext,
      Context keyboardContext,
      Row parent,
      KeyboardDimens keyboardDimens,
      int x,
      int y,
      XmlResourceParser parser) {
    AnyKey key =
        (AnyKey)
            super.createKeyFromXml(
                resourceMapping, askContext, keyboardContext, parent, keyboardDimens, x, y, parser);

    if (!TextUtils.isEmpty(key.text) && !EmojiUtils.isRenderable(mPaint, key.text)) {
      key.disable();
      key.width = 0;
      key.text = "";
      key.label = "";
      key.mShiftedCodes = EMPTY_INT_ARRAY;
    }

    if ((mDefaultSkinTone != null || mDefaultGender != null)
        && key.popupResId != 0
        && TextUtils.isEmpty(key.popupCharacters)
        && !TextUtils.isEmpty(key.text)
        && EmojiUtils.isLabelOfEmoji(key.text)) {
      AnyPopupKeyboard popupKeyboard =
          new AnyPopupKeyboard(
              getKeyboardAddOn(), askContext, key.popupResId, keyboardDimens, "temp", null, null);
      Key skinToneKey =
          findKeyWithSkinToneAndGender(popupKeyboard.getKeys(), mDefaultSkinTone, mDefaultGender);
      if (skinToneKey != null) {
        key.text = skinToneKey.text;
        key.label = skinToneKey.label;
      }
    }

    return key;
  }

  public void mirrorKeys() {
    /* how to mirror?
    width = 55
    [0..15] [20..35] [40..55]
    phase 1: multiple by -1
    [0] [-20] [-40]
    phase 2: add keyboard width
    [55] [35] [15]
    phase 3: subtracting the key's width
    [40] [20] [0]
    cool?
     */
    final int keyboardWidth = getMinWidth();
    for (Key k : getKeys()) {
      var newX = -1 * k.x; // phase 1
      newX += keyboardWidth; // phase 2
      newX -= k.width; // phase 3
      k.x = newX;
    }
  }
}
