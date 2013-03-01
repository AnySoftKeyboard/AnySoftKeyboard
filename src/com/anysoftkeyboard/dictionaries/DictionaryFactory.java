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
import com.anysoftkeyboard.AnySoftKeyboard;
import com.anysoftkeyboard.utils.Log;
import com.menny.android.anysoftkeyboard.AnyApplication;

public class DictionaryFactory {
    private static final String TAG = "ASK DictFctry";

    private AutoDictionary mAutoDictionary = null;
    private String mUserDictionaryLocale = null;
    private EditableDictionary mUserDictionary = null;

    public DictionaryFactory() {
    }

    public synchronized EditableDictionary createUserDictionary(
            Context context, String locale) {
        if (mUserDictionary != null
                && equalsString(mUserDictionaryLocale, locale)) {
            Log.d(TAG, "Returning cached user-dictionary for locale "
                    + mUserDictionaryLocale);
            return mUserDictionary;
        }
        Log.d(TAG, "Creating a new UserDictionart for locale " + locale);
        mUserDictionary = new SafeUserDictionary(context, locale);
        mUserDictionary.loadDictionary();

        mUserDictionaryLocale = locale;
        return mUserDictionary;
    }

    public synchronized EditableDictionary createContactsDictionary(
            Context context) {
        return null;
    }

    public boolean equalsString(String a, String b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return a.equals(b);
    }

    public synchronized AutoDictionary createAutoDictionary(Context context,
                                                            AnySoftKeyboard ime, String currentAutoDictionaryLocale) {
        if (AnyApplication.getConfig().getAutoDictionaryInsertionThreshold() < 0)
            return null;

        if (mAutoDictionary != null
                && equalsString(mAutoDictionary.getLocale(),
                currentAutoDictionaryLocale)) {
            return mAutoDictionary;
        }

        Log.d(TAG, "Creating AutoDictionary for locale: "
                + currentAutoDictionaryLocale);
        mAutoDictionary = new AutoDictionary(context, ime,
                currentAutoDictionaryLocale);
        mAutoDictionary.loadDictionary();

        return mAutoDictionary;
    }
}
