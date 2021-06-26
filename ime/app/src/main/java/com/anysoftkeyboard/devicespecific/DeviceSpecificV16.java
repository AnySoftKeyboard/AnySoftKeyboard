/*
 * Copyright (c) 2016 Menny Even-Danan
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

package com.anysoftkeyboard.devicespecific;

import android.annotation.TargetApi;
import android.database.ContentObserver;
import android.os.Build;
import androidx.annotation.NonNull;
import com.anysoftkeyboard.dictionaries.BTreeDictionary;
import com.anysoftkeyboard.dictionaries.DictionaryContentObserverAPI16;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class DeviceSpecificV16 extends DeviceSpecificV15 {
    @Override
    public String getApiLevel() {
        return "DeviceSpecificV16";
    }

    @Override
    public ContentObserver createDictionaryContentObserver(@NonNull BTreeDictionary dictionary) {
        return new DictionaryContentObserverAPI16(dictionary);
    }
}
