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

public class FallbackUserDictionary extends SQLiteUserDictionaryBase {

    private final String mLocale;

    public FallbackUserDictionary(Context context, String locale) {
        super("FallbackUserDictionary", context);
        mLocale = locale;
    }

    @Override
    protected DictionarySQLiteConnection createStorage() {
        return new DictionarySQLiteConnection(super.mContext, mLocale);
    }

    @Override
    public synchronized void loadDictionary() {
        //NOT doing it async, why? because my parent (SafeUserDictionary) is doing it async
        loadDictionaryAsync();
    }
}
