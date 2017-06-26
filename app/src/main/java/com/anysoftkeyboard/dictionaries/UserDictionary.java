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

import com.anysoftkeyboard.base.dictionaries.EditableDictionary;
import com.anysoftkeyboard.base.dictionaries.KeyCodesProvider;
import com.anysoftkeyboard.dictionaries.content.AndroidUserDictionary;
import com.anysoftkeyboard.dictionaries.sqlite.FallbackUserDictionary;
import com.anysoftkeyboard.nextword.NextWordDictionary;
import com.anysoftkeyboard.nextword.NextWordSuggestions;
import com.anysoftkeyboard.utils.Logger;
import com.menny.android.anysoftkeyboard.AnyApplication;

public class UserDictionary extends EditableDictionary {

    private static final String TAG = "ASK_SUD";
    private final NextWordDictionary mNextWordDictionary;
    private final Context mContext;
    private final String mLocale;
    private volatile BTreeDictionary mActualDictionary;

    public UserDictionary(Context context, String locale) {
        super("UserDictionary");
        mLocale = locale;
        mContext = context;

        mNextWordDictionary = new NextWordDictionary(mContext, mLocale);
    }

    @Override
    public final void getWords(KeyCodesProvider composer, WordCallback callback) {
        if (mActualDictionary != null) mActualDictionary.getWords(composer, callback);
    }

    NextWordSuggestions getUserNextWordGetter() {
        return mNextWordDictionary;
    }

    @Override
    public final boolean isValidWord(CharSequence word) {
        return mActualDictionary != null && mActualDictionary.isValidWord(word);
    }

    @Override
    protected final void closeAllResources() {
        if (mActualDictionary != null) mActualDictionary.close();
        mNextWordDictionary.close();
    }

    @Override
    protected final void loadAllResources() {
        mNextWordDictionary.load();

        BTreeDictionary androidBuiltIn = null;
        try {
            //The only reason I see someone uses this, is for development or debugging.
            if (AnyApplication.getConfig().alwaysUseFallBackUserDictionary())
                throw new RuntimeException("User requested to always use fall-back user-dictionary.");

            androidBuiltIn = createAndroidUserDictionary(mContext, mLocale);
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
            BTreeDictionary fallback = createFallbackUserDictionary(mContext, mLocale);
            fallback.loadDictionary();

            mActualDictionary = fallback;
        }

    }

    @NonNull
    protected FallbackUserDictionary createFallbackUserDictionary(Context context, String locale) {
        return new FallbackUserDictionary(context, locale);
    }

    @NonNull
    protected AndroidUserDictionary createAndroidUserDictionary(Context context, String locale) {
        return new AndroidUserDictionary(context, locale);
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
    public final void deleteWord(String word) {
        if (mActualDictionary != null)
            mActualDictionary.deleteWord(word);
    }

    protected BTreeDictionary getActualDictionary() {
        return mActualDictionary;
    }
}
