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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.anysoftkeyboard.addons.AddOnImpl;
import com.anysoftkeyboard.base.dictionaries.Dictionary;
import com.anysoftkeyboard.dictionaries.jni.BinaryDictionary;
import com.anysoftkeyboard.dictionaries.jni.ResourceBinaryDictionary;
import com.anysoftkeyboard.base.utils.Logger;
import com.menny.android.anysoftkeyboard.BuildConfig;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DictionaryAddOnAndBuilder extends AddOnImpl {

    private static final int INVALID_RES_ID = 0;

    private static final String TAG = "ASK DAOB";

    private final String mLanguage;
    private final String mAssetsFilename;
    private final int mDictionaryResId;
    private final int mAutoTextResId;
    private final int mInitialSuggestionsResId;

    private DictionaryAddOnAndBuilder(Context askContext, Context packageContext, int apiVersion, CharSequence id,
                                      CharSequence name, CharSequence description, boolean isHidden, int sortIndex, String dictionaryLanguage,
                                      String assetsFilename, int dictResId, int autoTextResId, int initialSuggestionsResId) {
        super(askContext, packageContext, apiVersion, id, name, description, isHidden, sortIndex);
        mLanguage = dictionaryLanguage;
        mAssetsFilename = assetsFilename;
        mDictionaryResId = dictResId;
        mAutoTextResId = autoTextResId;
        mInitialSuggestionsResId = initialSuggestionsResId;
    }

    public DictionaryAddOnAndBuilder(Context askContext, Context packageContext, int apiVersion, CharSequence id,
                                     CharSequence name, CharSequence description, boolean isHidden, int sortIndex, String dictionaryLanguage, String assetsFilename, int initialSuggestionsResId) {
        this(askContext, packageContext, apiVersion, id, name, description, isHidden, sortIndex, dictionaryLanguage, assetsFilename, INVALID_RES_ID, INVALID_RES_ID, initialSuggestionsResId);
    }

    public DictionaryAddOnAndBuilder(Context askContext, Context packageContext, int apiVersion, CharSequence id,
                                     CharSequence name, CharSequence description, boolean isHidden, int sortIndex, String dictionaryLanguage, int dictionaryResId, int autoTextResId, int initialSuggestionsResId) {
        this(askContext, packageContext, apiVersion, id, name, description, isHidden, sortIndex, dictionaryLanguage, null, dictionaryResId, autoTextResId, initialSuggestionsResId);
    }

    public String getLanguage() {
        return mLanguage;
    }

    public Dictionary createDictionary() throws Exception {
        if (mDictionaryResId == INVALID_RES_ID)
            return new BinaryDictionary(getPackageContext(), getName(), getPackageContext().getAssets().openFd(mAssetsFilename), BuildConfig.DEBUG);
        else
            return new ResourceBinaryDictionary(getName(), getPackageContext(), mDictionaryResId, BuildConfig.DEBUG);
    }

    @Nullable
    public AutoText createAutoText() {
        if (mAutoTextResId == INVALID_RES_ID) {
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
        if (mInitialSuggestionsResId == INVALID_RES_ID) {
            return Collections.emptyList();
        } else {
            final Context packageContext = getPackageContext();
            if (packageContext == null) return Collections.emptyList();
            return Arrays.asList(packageContext.getResources().getStringArray(mInitialSuggestionsResId));
        }
    }
}
