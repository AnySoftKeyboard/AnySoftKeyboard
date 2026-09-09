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

package com.anysoftkeyboard.dictionaries;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.anysoftkeyboard.addons.AddOn;
import com.anysoftkeyboard.addons.AddOnImpl;
import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.dictionaries.jni.BinaryDictionary;
import com.anysoftkeyboard.dictionaries.jni.ResourceBinaryDictionary;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DictionaryAddOnAndBuilder extends AddOnImpl {

  private static final String TAG = "ASKDictAddOnBuilder";

  private final String mLanguage;
  private final String mAssetsFilename;
  private final int mDictionaryResId;
  private final int mAutoTextResId;
  private final int mInitialSuggestionsResId;
  @NonNull private final RadicalDictionaryConfig mRadicalConfig;

  /**
   * Constructor for assets-backed dictionaries. {@code assetsFilename} may be {@code null} only
   * when {@code radicalConfig.hasRadicalDictionary()} is true (radical-only language pack);
   * otherwise the resulting addon will produce no {@link Dictionary} at all and a warning is
   * logged.
   */
  public DictionaryAddOnAndBuilder(
      Context askContext,
      Context packageContext,
      int apiVersion,
      CharSequence id,
      CharSequence name,
      CharSequence description,
      boolean isHidden,
      int sortIndex,
      String dictionaryLanguage,
      @Nullable String assetsFilename,
      int autoTextResId,
      int initialSuggestionsResId,
      @NonNull RadicalDictionaryConfig radicalConfig) {
    super(askContext, packageContext, apiVersion, id, name, description, isHidden, sortIndex);
    mLanguage = dictionaryLanguage;
    mAssetsFilename = assetsFilename;
    mDictionaryResId = AddOn.INVALID_RES_ID;
    mAutoTextResId = autoTextResId;
    mInitialSuggestionsResId = initialSuggestionsResId;
    mRadicalConfig = radicalConfig;
    if (assetsFilename == null && !radicalConfig.hasRadicalDictionary()) {
      Logger.w(
          TAG,
          "Dictionary addon '%s' has neither assets nor a radical dictionary; will produce no"
              + " suggestions.",
          name);
    }
  }

  /** Constructor for resource-backed dictionaries. */
  public DictionaryAddOnAndBuilder(
      Context askContext,
      Context packageContext,
      int apiVersion,
      CharSequence id,
      CharSequence name,
      CharSequence description,
      boolean isHidden,
      int sortIndex,
      String dictionaryLanguage,
      int dictionaryResId,
      int autoTextResId,
      int initialSuggestionsResId,
      @NonNull RadicalDictionaryConfig radicalConfig) {
    super(askContext, packageContext, apiVersion, id, name, description, isHidden, sortIndex);
    mLanguage = dictionaryLanguage;
    mAssetsFilename = null;
    mDictionaryResId = dictionaryResId;
    mAutoTextResId = autoTextResId;
    mInitialSuggestionsResId = initialSuggestionsResId;
    mRadicalConfig = radicalConfig;
  }

  /**
   * Backwards-compatible constructor for assets-backed dictionaries that do NOT use the radical
   * dictionary feature. Equivalent to passing {@link RadicalDictionaryConfig#EMPTY} to the primary
   * constructor. Kept so downstream forks that programmatically construct dictionary builders
   * continue to compile.
   */
  public DictionaryAddOnAndBuilder(
      Context askContext,
      Context packageContext,
      int apiVersion,
      CharSequence id,
      CharSequence name,
      CharSequence description,
      boolean isHidden,
      int sortIndex,
      String dictionaryLanguage,
      @Nullable String assetsFilename,
      int autoTextResId,
      int initialSuggestionsResId) {
    this(
        askContext,
        packageContext,
        apiVersion,
        id,
        name,
        description,
        isHidden,
        sortIndex,
        dictionaryLanguage,
        assetsFilename,
        autoTextResId,
        initialSuggestionsResId,
        RadicalDictionaryConfig.EMPTY);
  }

  /**
   * Backwards-compatible constructor for resource-backed dictionaries that do NOT use the radical
   * dictionary feature. Equivalent to passing {@link RadicalDictionaryConfig#EMPTY} to the primary
   * constructor.
   */
  public DictionaryAddOnAndBuilder(
      Context askContext,
      Context packageContext,
      int apiVersion,
      CharSequence id,
      CharSequence name,
      CharSequence description,
      boolean isHidden,
      int sortIndex,
      String dictionaryLanguage,
      int dictionaryResId,
      int autoTextResId,
      int initialSuggestionsResId) {
    this(
        askContext,
        packageContext,
        apiVersion,
        id,
        name,
        description,
        isHidden,
        sortIndex,
        dictionaryLanguage,
        dictionaryResId,
        autoTextResId,
        initialSuggestionsResId,
        RadicalDictionaryConfig.EMPTY);
  }

  public String getLanguage() {
    return mLanguage;
  }

  public int getRadicalDictResId() {
    return mRadicalConfig.getRadicalDictResId();
  }

  public boolean hasRadicalDictionary() {
    return mRadicalConfig.hasRadicalDictionary();
  }

  @Nullable
  public RadicalDictionary createRadicalDictionary() {
    if (!mRadicalConfig.hasRadicalDictionary() || getPackageContext() == null) {
      return null;
    }
    return new RadicalDictionary(
        getName() + "_radical",
        getPackageContext(),
        mRadicalConfig.getRadicalDictResId(),
        mRadicalConfig.getRadicalPhrasesResId(),
        mRadicalConfig.getHomophonesResId(),
        mRadicalConfig.getCharToZhuyinResId(),
        mRadicalConfig.getCharToRadicalResId(),
        mRadicalConfig.getExcludeHomophoneCharsResId(),
        mRadicalConfig.getIncludeCharsResId(),
        mRadicalConfig.getCharFrequencyResId(),
        mRadicalConfig.splitMultiCodepointCandidates(),
        buildRadicalOverlay());
  }

  /**
   * Builds the per-keyboard {@link RadicalOverlay} that lets users supply their own table data.
   * Delegates entirely to {@link RadicalOverlayRepository#buildOverlayForKeyboard}, see that method
   * for resolution order (SAF tree URI -> file-based default under {@code
   * <addon-package-external-files-dir>/radical/}, with a read-only fallback to the legacy {@code
   * /boshiamy/} subdir for alpha-cohort users), cache wiring, and invalidation rules.
   *
   * <p>Returns {@link RadicalOverlay#NONE} if no host/package context is available.
   */
  @NonNull
  private RadicalOverlay buildRadicalOverlay() {
    final Context pkg = getPackageContext();
    final Context askContext = getAskContext();
    if (pkg == null || askContext == null) return RadicalOverlay.NONE;
    return RadicalOverlayRepository.buildOverlayForKeyboard(askContext, pkg, getId());
  }

  /**
   * Builds the word-suggestion {@link Dictionary} for this addon, or {@code null} if this is a
   * radical-only language pack that has no traditional next-word/auto-correct dictionary backing it
   * (i.e. neither {@code dictionaryResourceId} nor {@code dictionaryAssertName} was declared in the
   * addon XML).
   *
   * <p>Radical-input keyboards (Boshiamy, Zhuyin, Cangjie, ...) are allowed to ship the radical
   * dictionary alone, in which case callers must tolerate a {@code null} return.
   */
  @Nullable
  public Dictionary createDictionary() throws Exception {
    if (mDictionaryResId == AddOn.INVALID_RES_ID && mAssetsFilename == null) {
      return null;
    }
    if (mDictionaryResId == AddOn.INVALID_RES_ID)
      return new BinaryDictionary(
          getPackageContext(), getName(), getPackageContext().getAssets().openFd(mAssetsFilename));
    else {
      return new ResourceBinaryDictionary(getName(), getPackageContext(), mDictionaryResId);
    }
  }

  @Nullable
  public AutoText createAutoText() {
    if (mAutoTextResId == AddOn.INVALID_RES_ID) {
      return null;
    } else {
      try {
        return new AutoTextImpl(getPackageContext().getResources(), mAutoTextResId);
      } catch (OutOfMemoryError e) {
        Logger.i(TAG, "Failed to create the AutoText dictionary.");
        return null;
      }
    }
  }

  @NonNull
  public List<String> createInitialSuggestions() {
    if (mInitialSuggestionsResId == AddOn.INVALID_RES_ID) {
      return Collections.emptyList();
    } else {
      final Context packageContext = getPackageContext();
      if (packageContext == null) return Collections.emptyList();
      return Arrays.asList(packageContext.getResources().getStringArray(mInitialSuggestionsResId));
    }
  }
}
