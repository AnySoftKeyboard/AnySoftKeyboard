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
import androidx.annotation.Nullable;
import com.anysoftkeyboard.addons.AddOn;
import com.anysoftkeyboard.addons.AddOnImpl;

public class KeyboardAddOnAndBuilder extends AddOnImpl {

  private final int mResId;
  private final int mLandscapeResId;
  private final int mIconResId;
  private final String mDefaultDictionary;
  private final int mQwertyTranslationId;
  private final String mAdditionalIsLetterExceptions;
  private final String mSentenceSeparators;
  private final boolean mHasRadicalInput;
  private final String mRadicalKeyCharacters;
  private final String mRadicalCandidateSelectorKeys;
  private final boolean mAsciiOnlyPopups;
  private final boolean mKeyboardDefaultEnabled;
  private final Context mAskContext;

  public KeyboardAddOnAndBuilder(
      Context askContext,
      Context packageContext,
      int apiVersion,
      CharSequence id,
      CharSequence name,
      int layoutResId,
      int landscapeLayoutResId,
      String defaultDictionary,
      int iconResId,
      int physicalTranslationResId,
      String additionalIsLetterExceptions,
      String sentenceSeparators,
      CharSequence description,
      boolean isHidden,
      int keyboardIndex,
      boolean hasRadicalInput,
      String radicalKeyCharacters,
      String radicalCandidateSelectorKeys,
      boolean asciiOnlyPopups,
      boolean keyboardDefaultEnabled) {
    super(askContext, packageContext, apiVersion, id, name, description, isHidden, keyboardIndex);

    mResId = layoutResId;
    if (landscapeLayoutResId == AddOn.INVALID_RES_ID) {
      mLandscapeResId = mResId;
    } else {
      mLandscapeResId = landscapeLayoutResId;
    }

    mDefaultDictionary = defaultDictionary;
    mIconResId = iconResId;
    mAdditionalIsLetterExceptions = additionalIsLetterExceptions;
    mSentenceSeparators = sentenceSeparators;
    mQwertyTranslationId = physicalTranslationResId;
    mHasRadicalInput = hasRadicalInput;
    mRadicalKeyCharacters = radicalKeyCharacters;
    mRadicalCandidateSelectorKeys = radicalCandidateSelectorKeys;
    mAsciiOnlyPopups = asciiOnlyPopups;
    mKeyboardDefaultEnabled = keyboardDefaultEnabled;
    mAskContext = askContext;
  }

  public boolean getKeyboardDefaultEnabled() {
    return mKeyboardDefaultEnabled;
  }

  public String getKeyboardLocale() {
    return mDefaultDictionary;
  }

  public String getSentenceSeparators() {
    return mSentenceSeparators;
  }

  public boolean getHasRadicalInput() {
    return mHasRadicalInput;
  }

  /**
   * If true, long-press popup characters on this keyboard's keys are stripped to ASCII only. Useful
   * for CJK keyboards where Western diacritics are noise.
   */
  public boolean getAsciiOnlyPopups() {
    return mAsciiOnlyPopups;
  }

  /**
   * Returns the set of non-alphanumeric characters that are valid in radical sequences for this
   * keyboard. For example, Boshiamy uses ",.;[]-/'" while Zhuyin might use none.
   */
  @Nullable
  public String getRadicalKeyCharacters() {
    return mRadicalKeyCharacters;
  }

  /**
   * Returns the per-keyboard ordered list of candidate-selector keys for radical input.
   *
   * <p>When the user has typed a radical sequence with multiple matches and then types one of these
   * keys, that key acts as "select the Nth candidate" instead of being appended to the radical
   * sequence:
   *
   * <ul>
   *   <li>Position 0 (e.g. {@code 'v'}) -> 2nd candidate
   *   <li>Position 1 (e.g. {@code 'r'}) -> 3rd candidate
   *   <li>...
   * </ul>
   *
   * Returns {@code null} (or empty) if the keyboard does not support candidate-selector keys. The
   * 1st candidate is always selected by space, regardless of this attribute.
   */
  @Nullable
  public String getRadicalCandidateSelectorKeys() {
    return mRadicalCandidateSelectorKeys;
  }

  /**
   * Checks if the given key code is a valid radical sequence character for this keyboard. Letters
   * (a-z, A-Z) and digits (0-9) are always valid. Additional characters are defined per-keyboard
   * via the radicalKeyCharacters XML attribute.
   */
  public boolean isRadicalKeyCode(int keyCode) {
    if ((keyCode >= 'a' && keyCode <= 'z')
        || (keyCode >= 'A' && keyCode <= 'Z')
        || (keyCode >= '0' && keyCode <= '9')) {
      return true;
    }
    return mRadicalKeyCharacters != null && mRadicalKeyCharacters.indexOf((char) keyCode) >= 0;
  }

  @Nullable
  public AnyKeyboard createKeyboard(@Keyboard.KeyboardRowModeId int mode) {
    if (getPackageContext() == null) return null;
    return new ExternalAnyKeyboard(
        this,
        mAskContext,
        mResId,
        mLandscapeResId,
        getName(),
        mIconResId,
        mQwertyTranslationId,
        mDefaultDictionary,
        mAdditionalIsLetterExceptions,
        mSentenceSeparators,
        mode);
  }
}
