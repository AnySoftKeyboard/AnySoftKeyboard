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
import android.os.AsyncTask;
import com.anysoftkeyboard.WordComposer;
import com.anysoftkeyboard.dictionaries.content.AndroidUserDictionary;
import com.anysoftkeyboard.dictionaries.sqlite.FallbackUserDictionary;
import com.anysoftkeyboard.utils.Log;

public class UserDictionary extends EditableDictionary {

    private static final String TAG = "ASK_SUD";
    private BTreeDictionary mActualDictionary;

    private final Context mContext;
    private final String mLocale;

    public UserDictionary(Context context, String locale) {
        super("UserDictionary");
        mLocale = locale;
        mContext = context;
    }

    @Override
    public final void getWords(WordComposer composer,
                                      WordCallback callback) {
        if (mActualDictionary != null)
            mActualDictionary.getWords(composer, callback);
    }

    @Override
    public final boolean isValidWord(CharSequence word) {
        if (mActualDictionary != null)
            return mActualDictionary.isValidWord(word);
        else
            return false;
    }

    @Override
    protected final void closeAllResources() {
        if (mActualDictionary != null)
            mActualDictionary.close();
    }

    @Override
    protected final void loadAllResources() {
        AndroidUserDictionary androidBuiltIn = null;
        try {
            androidBuiltIn = new AndroidUserDictionary(mContext, mLocale);
            androidBuiltIn.loadDictionary();
            mActualDictionary = androidBuiltIn;
        } catch (Exception e) {
            Log.w(TAG,
                    "Failed to load Android's built-in user dictionary. No matter, I'll use a fallback.");
            if (androidBuiltIn != null) {
                try {
                    androidBuiltIn.close();
                } catch (Exception buildInCloseException) {
                    // it's an half-baked object, no need to worry about it
                    buildInCloseException.printStackTrace();
                    Log.w(TAG,
                            "Failed to close the build-in user dictionary properly, but it should be fine.");
                }
            }
            FallbackUserDictionary fallback = new FallbackUserDictionary(
                    mContext, mLocale);
            fallback.loadDictionary();

            mActualDictionary = fallback;
        }
    }

    @Override
    public final boolean addWord(String word, int frequency) {
        if (mActualDictionary != null)
            return mActualDictionary.addWord(word, frequency);
        else
            return false;
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
