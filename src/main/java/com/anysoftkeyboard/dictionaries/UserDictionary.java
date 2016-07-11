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
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.anysoftkeyboard.base.dictionaries.EditableDictionary;
import com.anysoftkeyboard.base.dictionaries.WordComposer;
import com.anysoftkeyboard.base.dictionaries.WordsCursor;
import com.anysoftkeyboard.dictionaries.content.AndroidUserDictionary;
import com.anysoftkeyboard.dictionaries.sqlite.FallbackUserDictionary;
import com.anysoftkeyboard.nextword.NextWordDictionary;
import com.anysoftkeyboard.nextword.Utils;
import com.anysoftkeyboard.utils.Logger;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

import java.util.Arrays;
import java.util.List;

public class UserDictionary extends EditableDictionary {

    private static final String TAG = "ASK_SUD";
    private volatile BTreeDictionary mActualDictionary;
    private NextWordDictionary mNextWordDictionary;

    private final Context mContext;
    private final String mLocale;
    @Utils.NextWordsSuggestionType
    private final String mNextWordSuggestionType;
    private final int mMaxNextWordSuggestionsCount;
    private final int mMinWordUsage;
    private final List<String> mFallbackInitialSuggestions;

    public UserDictionary(Context context, String locale) {
        super("UserDictionary");
        mLocale = locale;
        mContext = context;

        final Resources resources = context.getResources();
        final SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mNextWordSuggestionType = Utils.getNextWordSuggestionTypeFromPrefs(resources, defaultSharedPreferences);
        mMaxNextWordSuggestionsCount = Utils.getNextWordSuggestionCountFromPrefs(resources, defaultSharedPreferences);
        mMinWordUsage = Utils.getNextWordSuggestionMinUsageFromPrefs(resources, defaultSharedPreferences);
        if (Utils.NEXT_WORD_SUGGESTION_WORDS_AND_PUNCTUATIONS.equals(mNextWordSuggestionType)) {
            mFallbackInitialSuggestions = Arrays.asList(resources.getStringArray(R.array.english_initial_suggestions));
        } else {
            mFallbackInitialSuggestions = null;
        }
    }

    @Override
    public final void getWords(WordComposer composer, WordCallback callback) {
        if (mActualDictionary != null) mActualDictionary.getWords(composer, callback);
    }

    public final void resetNextWordMemory() {
        if (mNextWordDictionary != null) mNextWordDictionary.resetSentence();
    }

    public final void getNextWords(String currentWord, int maxSuggestions, List<CharSequence> suggestionsHolder, @Nullable Iterable<String> localeSpecificPunctuations) {
        if (mNextWordDictionary != null) {
            for (String nextWordSuggestion : mNextWordDictionary.getNextWords(currentWord, mMaxNextWordSuggestionsCount, mMinWordUsage)) {
                suggestionsHolder.add(nextWordSuggestion);
                maxSuggestions--;
                if (maxSuggestions == 0) return;
            }

            if (Utils.NEXT_WORD_SUGGESTION_WORDS_AND_PUNCTUATIONS.equals(mNextWordSuggestionType)) {
                if (localeSpecificPunctuations == null)
                    localeSpecificPunctuations = mFallbackInitialSuggestions;
                for (String evenMoreSuggestions : localeSpecificPunctuations) {
                    suggestionsHolder.add(evenMoreSuggestions);
                    maxSuggestions--;
                    if (maxSuggestions == 0) return;
                }
            }
        }
    }

    @Override
    public final boolean isValidWord(CharSequence word) {
        return mActualDictionary != null && mActualDictionary.isValidWord(word);
    }

    @Override
    protected final void closeAllResources() {
        if (mActualDictionary != null) mActualDictionary.close();
        if (mNextWordDictionary != null) mNextWordDictionary.close();
    }

    @Override
    protected final void loadAllResources() {
        mNextWordDictionary = new NextWordDictionary(mContext, mLocale);
        mNextWordDictionary.load();

        AndroidUserDictionary androidBuiltIn = null;
        try {
            //The only reason I see someone uses this, is for development or debugging.
            if (AnyApplication.getConfig().alwaysUseFallBackUserDictionary())
                throw new RuntimeException("User requested to always use fall-back user-dictionary.");

            androidBuiltIn = new AndroidUserDictionary(mContext, mLocale);
            androidBuiltIn.loadDictionary();
            mActualDictionary = androidBuiltIn;
        } catch (Exception e) {
            Logger.w(TAG, "Can not load Android's built-in user dictionary (since '%s'). FallbackUserDictionary to the rescue!", e.getMessage());
            if (androidBuiltIn != null) {
                try {
                    androidBuiltIn.close();
                } catch (Exception buildInCloseException) {
                    // it's an half-baked object, no need to worry about it
                    buildInCloseException.printStackTrace();
                    Logger.w(TAG, "Failed to close the build-in user dictionary properly, but it should be fine.");
                }
            }
            FallbackUserDictionary fallback = new FallbackUserDictionary(mContext, mLocale);
            fallback.loadDictionary();

            mActualDictionary = fallback;
        }
    }

    @Override
    public final boolean addWord(String word, int frequency) {
        if (mActualDictionary != null) {
            return mActualDictionary.addWord(word, frequency);
        } else {
            Logger.d(TAG, "There is no actual dictionary to use for adding word! How come?");
            return false;
        }
    }

    @Override
    public final WordsCursor getWordsCursor() {
        if (mActualDictionary != null)
            return mActualDictionary.getWordsCursor();

        return null;
    }

    @Override
    public final void deleteWord(String word) {
        if (mActualDictionary != null)
            mActualDictionary.deleteWord(word);
    }
}
