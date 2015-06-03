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

import com.anysoftkeyboard.IndirectlyInstantiated;
import com.anysoftkeyboard.base.utils.Log;
import net.evendanan.frankenrobot.Diagram;

import java.lang.ref.WeakReference;

@IndirectlyInstantiated
public class DictionaryContentObserver extends ContentObserver {

    public static final class DictionaryContentObserverDiagram extends Diagram<DictionaryContentObserver> {
        private final BTreeDictionary mOwningDictionary;
        public DictionaryContentObserverDiagram(BTreeDictionary owningDictionary) {
            mOwningDictionary = owningDictionary;
        }

        public BTreeDictionary getOwningDictionary() {
            return mOwningDictionary;
        }
    }

    private final static String TAG = "DictionaryContentObserver";
    private final WeakReference<BTreeDictionary> mDictionary;

    public DictionaryContentObserver(DictionaryContentObserverDiagram diagram) {
        super(null);
        mDictionary = new WeakReference<>(diagram.getOwningDictionary());
    }

    @Override
    public void onChange(boolean self) {
        BTreeDictionary dictionary = mDictionary.get();
        if (dictionary == null) return;
        if (self) {
            Log.i(TAG, "I wont notify about self change.");
            return;
        }

        dictionary.onStorageChanged();
    }
}
