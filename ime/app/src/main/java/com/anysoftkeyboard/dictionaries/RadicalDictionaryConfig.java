/*
 * Copyright (c) 2024 Menny Even-Danan
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

package com.anysoftkeyboard.dictionaries;

import com.anysoftkeyboard.addons.AddOn;

/**
 * Immutable bundle of resource IDs that describe a radical-input dictionary (Boshiamy, Cangjie,
 * Zhuyin, ...). Replaces a sprawl of 5+ scalar fields and three telescoping {@code
 * setHomophoneResources} overloads on {@link DictionaryAddOnAndBuilder}.
 *
 * <p>Use {@link #EMPTY} for traditional (non-radical) language packs. Use {@link Builder} when
 * constructing from the parsed addon XML attributes.
 */
public final class RadicalDictionaryConfig {

  public static final RadicalDictionaryConfig EMPTY = new Builder().build();

  private final int mRadicalDictResId;
  private final int mRadicalPhrasesResId;
  private final int mHomophonesResId;
  private final int mCharToZhuyinResId;
  private final int mCharToRadicalResId;
  private final int mExcludeHomophoneCharsResId;
  private final int mIncludeCharsResId;
  private final int mCharFrequencyResId;
  private final boolean mSplitMultiCodepointCandidates;

  private RadicalDictionaryConfig(Builder b) {
    mRadicalDictResId = b.mRadicalDictResId;
    mRadicalPhrasesResId = b.mRadicalPhrasesResId;
    mHomophonesResId = b.mHomophonesResId;
    mCharToZhuyinResId = b.mCharToZhuyinResId;
    mCharToRadicalResId = b.mCharToRadicalResId;
    mExcludeHomophoneCharsResId = b.mExcludeHomophoneCharsResId;
    mIncludeCharsResId = b.mIncludeCharsResId;
    mCharFrequencyResId = b.mCharFrequencyResId;
    mSplitMultiCodepointCandidates = b.mSplitMultiCodepointCandidates;
  }

  public int getRadicalDictResId() {
    return mRadicalDictResId;
  }

  public int getRadicalPhrasesResId() {
    return mRadicalPhrasesResId;
  }

  public int getHomophonesResId() {
    return mHomophonesResId;
  }

  public int getCharToZhuyinResId() {
    return mCharToZhuyinResId;
  }

  public int getCharToRadicalResId() {
    return mCharToRadicalResId;
  }

  public int getExcludeHomophoneCharsResId() {
    return mExcludeHomophoneCharsResId;
  }

  /**
   * Resource id of the per-keyboard CJK / kana codepoint whitelist used to filter both bundled and
   * user-imported radical rows: a row is admitted only if every CJK or kana codepoint in its value
   * is present in this set. {@link AddOn#INVALID_RES_ID} disables the filter (used by non-CJK /
   * unfiltered packs).
   */
  public int getIncludeCharsResId() {
    return mIncludeCharsResId;
  }

  public int getCharFrequencyResId() {
    return mCharFrequencyResId;
  }

  /**
   * When true, the loader splits a single multi-codepoint candidate string into one candidate per
   * CJK codepoint. Used by Cangjie and Zhuyin where the source format is {@code key\tchar1char2..}.
   * Boshiamy leaves this off so multi-codepoint outputs such as {@code liu\t嘸蝦米} remain a single
   * candidate.
   */
  public boolean splitMultiCodepointCandidates() {
    return mSplitMultiCodepointCandidates;
  }

  public boolean hasRadicalDictionary() {
    return mRadicalDictResId != AddOn.INVALID_RES_ID;
  }

  public static final class Builder {
    private int mRadicalDictResId = AddOn.INVALID_RES_ID;
    private int mRadicalPhrasesResId = AddOn.INVALID_RES_ID;
    private int mHomophonesResId = AddOn.INVALID_RES_ID;
    private int mCharToZhuyinResId = AddOn.INVALID_RES_ID;
    private int mCharToRadicalResId = AddOn.INVALID_RES_ID;
    private int mExcludeHomophoneCharsResId = AddOn.INVALID_RES_ID;
    private int mIncludeCharsResId = AddOn.INVALID_RES_ID;
    private int mCharFrequencyResId = AddOn.INVALID_RES_ID;
    private boolean mSplitMultiCodepointCandidates = false;

    public Builder radicalDictResId(int v) {
      mRadicalDictResId = v;
      return this;
    }

    public Builder radicalPhrasesResId(int v) {
      mRadicalPhrasesResId = v;
      return this;
    }

    public Builder homophonesResId(int v) {
      mHomophonesResId = v;
      return this;
    }

    public Builder charToZhuyinResId(int v) {
      mCharToZhuyinResId = v;
      return this;
    }

    public Builder charToRadicalResId(int v) {
      mCharToRadicalResId = v;
      return this;
    }

    public Builder excludeHomophoneCharsResId(int v) {
      mExcludeHomophoneCharsResId = v;
      return this;
    }

    public Builder includeCharsResId(int v) {
      mIncludeCharsResId = v;
      return this;
    }

    public Builder charFrequencyResId(int v) {
      mCharFrequencyResId = v;
      return this;
    }

    public Builder splitMultiCodepointCandidates(boolean v) {
      mSplitMultiCodepointCandidates = v;
      return this;
    }

    public RadicalDictionaryConfig build() {
      return new RadicalDictionaryConfig(this);
    }
  }
}
