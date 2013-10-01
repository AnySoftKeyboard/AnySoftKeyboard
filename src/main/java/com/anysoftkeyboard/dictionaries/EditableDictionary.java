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


public abstract class EditableDictionary extends Dictionary {

    protected EditableDictionary(String dictionaryName) {
        super(dictionaryName);
    }

    /**
     * Adds a word to the dictionary and makes it persistent.
     *
     * @param word      the word to add. If the word is capitalized, then the dictionary will
     *                  recognize it as a capitalized word when searched.
     * @param frequency the frequency of occurrence of the word. A frequency of 255 is considered
     *                  the highest.
     * @TODO use a higher or float range for frequency
     */
    public abstract boolean addWord(String word, int frequency);

    public abstract WordsCursor getWordsCursor();

    public abstract void deleteWord(String word);
}
