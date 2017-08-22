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

import android.database.ContentObserver;

import com.anysoftkeyboard.utils.Logger;

import java.lang.ref.WeakReference;

public class DictionaryContentObserver extends ContentObserver {

    private static final String TAG = "DictionaryContentObserver";
    private final WeakReference<BTreeDictionary> mDictionary;

    public DictionaryContentObserver(BTreeDictionary dictionary) {
        super(null);
        mDictionary = new WeakReference<>(dictionary);
    }

    @Override
    public void onChange(boolean self) {
        BTreeDictionary dictionary = mDictionary.get();
        if (dictionary == null) return;
        if (self) {
            Logger.i(TAG, "I wont notify about self change.");
            return;
        }

        dictionary.onStorageChanged();
    }
}
