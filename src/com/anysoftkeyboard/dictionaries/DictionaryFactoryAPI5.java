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
import com.anysoftkeyboard.utils.Log;

public class DictionaryFactoryAPI5 extends DictionaryFactory {
    private static final String TAG = "ASK DictFctry5";

    @Override
    public synchronized EditableDictionary createContactsDictionary(
            Context context) {
        try {
            Log.d(TAG, "Creating device's contacts suggestions dictionary...");
            ContactsDictionary cdict = createConcreteContactsDictionary(context);
            cdict.loadDictionary();
            return cdict;
        } catch (Exception ex) {
            Log.w(TAG, "Failed to load 'ContactsDictionary'", ex);
            return null;
        }
    }

    protected ContactsDictionary createConcreteContactsDictionary(
            Context context) throws Exception {
        return new ContactsDictionary(context);
    }
}
