/*
 * Copyright (C) 2016 AnySoftKeyboard
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.HashMap;
import java.util.Map;

class WordWithCount implements Comparable<WordWithCount> {
    private final String mKey;
    private final Map<String, Integer> mWordVariants = new HashMap<>();
    private int mFreq;

    public WordWithCount(String word) {
        mKey = word.toLowerCase();
        mWordVariants.put(word, 1);
        mFreq = 0;
    }

    public WordWithCount(String word, int frequency) {
        mKey = word.toLowerCase();
        mWordVariants.put(word, 1);
        mFreq = frequency;
    }

    public String getKey() {
        return mKey;
    }

    public String getWord() {
        String mostUsedWord = mKey;
        int mostUsedValue = Integer.MIN_VALUE;
        for (Map.Entry<String, Integer> variant : mWordVariants.entrySet()) {
            if (variant.getValue() > mostUsedValue) {
                mostUsedValue = variant.getValue();
                mostUsedWord = variant.getKey();
            }
        }

        return mostUsedWord;
    }

    public int getFreq() {
        return mFreq;
    }

    public void addFreq(String word) {
        if (mFreq < Integer.MAX_VALUE) mFreq++;
        mWordVariants.compute(word, (s, usages) -> usages == null? 1 : usages+1);
    }

    public void addOtherWord(WordWithCount wordWithCount) {
        mFreq = Math.max(mFreq, wordWithCount.mFreq);
        for (Map.Entry<String, Integer> variant : mWordVariants.entrySet()) {
            mWordVariants.compute(variant.getKey(), (s, usages) -> usages == null? variant.getValue() : usages+variant.getValue());
        }
    }

    @Override
    public int compareTo(WordWithCount o) {
        return o.mFreq - mFreq;
    }
}
