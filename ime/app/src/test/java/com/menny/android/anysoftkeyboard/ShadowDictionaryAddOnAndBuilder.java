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

package com.menny.android.anysoftkeyboard;

import androidx.core.util.Pair;
import androidx.test.core.app.ApplicationProvider;
import com.anysoftkeyboard.dictionaries.Dictionary;
import com.anysoftkeyboard.dictionaries.DictionaryAddOnAndBuilder;
import com.anysoftkeyboard.dictionaries.InMemoryDictionary;
import java.util.Arrays;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

@Implements(
    value = DictionaryAddOnAndBuilder.class,
    isInAndroidSdk = false,
    callThroughByDefault = true)
public class ShadowDictionaryAddOnAndBuilder {

  @RealObject DictionaryAddOnAndBuilder mOriginalBuilder;

  /** Shadows the native-dictionary creation. */
  public Dictionary createDictionary() throws Exception {
    return new InMemoryDictionary(
        mOriginalBuilder.getName(),
        ApplicationProvider.getApplicationContext(),
        // frequencies were taken from the original English AOSP file.
        Arrays.asList(
            Pair.create("he", 187),
            Pair.create("he'll", 94),
            Pair.create("hell", 108),
            Pair.create("hello", 120),
            Pair.create("face", 141)),
        true);
  }
}
