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

import com.anysoftkeyboard.AnySoftKeyboardTestRunner;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AnySoftKeyboardTestRunner.class)
public class BTreeDictionaryTest {

    private TestableBTreeDictionary mDictionaryUnderTest;

    @Before
    public void setup() throws Exception {
        mDictionaryUnderTest = new TestableBTreeDictionary("TEST", RuntimeEnvironment.application);
    }

    @Test
    public void testLoadDictionary() throws Exception {
        //no words now
        Assert.assertFalse(mDictionaryUnderTest.isValidWord((String) TestableBTreeDictionary.STORAGE[0][1]));

        //ok, now yes words
        mDictionaryUnderTest.loadDictionary();
        for (int row = 0; row < TestableBTreeDictionary.STORAGE.length; row++) {
            final String word = (String) TestableBTreeDictionary.STORAGE[row][1];
            final int freq = ((Integer) TestableBTreeDictionary.STORAGE[row][2]).intValue();
            assertTrue("Word at row " + row + " (" + word + ") should be valid.", mDictionaryUnderTest.isValidWord(word));
            Assert.assertEquals(mDictionaryUnderTest.getWordFrequency(word), freq);
        }
        //checking validity of the internal structure
        assetNodeArrayIsValid(mDictionaryUnderTest.getRoot());
    }

    @Test
    public void testAddWord() throws Exception {
        mDictionaryUnderTest.loadDictionary();

        assertTrue(mDictionaryUnderTest.addWord("new", 23));
        Assert.assertEquals("new", mDictionaryUnderTest.wordRequestedToAddedToStorage);
        Assert.assertEquals(23, mDictionaryUnderTest.wordFrequencyRequestedToAddedToStorage);
        assertTrue(mDictionaryUnderTest.isValidWord("new"));
        Assert.assertEquals(mDictionaryUnderTest.getWordFrequency("new"), 23);
        //checking validity of the internal structure
        assetNodeArrayIsValid(mDictionaryUnderTest.getRoot());

        assertTrue(mDictionaryUnderTest.addWord("new", 34));
        Assert.assertEquals("new", mDictionaryUnderTest.wordRequestedToAddedToStorage);
        Assert.assertEquals(34, mDictionaryUnderTest.wordFrequencyRequestedToAddedToStorage);
        assertTrue(mDictionaryUnderTest.isValidWord("new"));
        Assert.assertEquals(mDictionaryUnderTest.getWordFrequency("new"), 34);
        //checking validity of the internal structure
        assetNodeArrayIsValid(mDictionaryUnderTest.getRoot());

        assertTrue(mDictionaryUnderTest.addWord("newa", 45));
        assertTrue(mDictionaryUnderTest.isValidWord("newa"));
        Assert.assertEquals(mDictionaryUnderTest.getWordFrequency("new"), 34);
        Assert.assertEquals(mDictionaryUnderTest.getWordFrequency("newa"), 45);
        //checking validity of the internal structure
        assetNodeArrayIsValid(mDictionaryUnderTest.getRoot());

        assertTrue(mDictionaryUnderTest.addWord("nea", 47));
        Assert.assertEquals("nea", mDictionaryUnderTest.wordRequestedToAddedToStorage);
        Assert.assertEquals(47, mDictionaryUnderTest.wordFrequencyRequestedToAddedToStorage);
        assertTrue(mDictionaryUnderTest.isValidWord("nea"));
        Assert.assertEquals(mDictionaryUnderTest.getWordFrequency("new"), 34);
        Assert.assertEquals(mDictionaryUnderTest.getWordFrequency("newa"), 45);
        Assert.assertEquals(mDictionaryUnderTest.getWordFrequency("nea"), 47);
        //checking validity of the internal structure
        assetNodeArrayIsValid(mDictionaryUnderTest.getRoot());

        assertTrue(mDictionaryUnderTest.addWord("neabb", 50));
        Assert.assertEquals("neabb", mDictionaryUnderTest.wordRequestedToAddedToStorage);
        Assert.assertEquals(50, mDictionaryUnderTest.wordFrequencyRequestedToAddedToStorage);
        assertTrue(mDictionaryUnderTest.isValidWord("neabb"));
        Assert.assertFalse(mDictionaryUnderTest.isValidWord("neab"));
        Assert.assertEquals(mDictionaryUnderTest.getWordFrequency("new"), 34);
        Assert.assertEquals(mDictionaryUnderTest.getWordFrequency("newa"), 45);
        Assert.assertEquals(mDictionaryUnderTest.getWordFrequency("nea"), 47);
        Assert.assertEquals(mDictionaryUnderTest.getWordFrequency("neabb"), 50);
        Assert.assertEquals(mDictionaryUnderTest.getWordFrequency("neab"), 0);
        //checking validity of the internal structure
        assetNodeArrayIsValid(mDictionaryUnderTest.getRoot());
    }

    @Test
    public void testDeleteWord() throws Exception {
        mDictionaryUnderTest.loadDictionary();
        //from read storage
        String word = (String) TestableBTreeDictionary.STORAGE[0][1];
        int wordFreq = ((Integer) TestableBTreeDictionary.STORAGE[0][2]).intValue();
        assertTrue(mDictionaryUnderTest.isValidWord(word));
        mDictionaryUnderTest.deleteWord(word);
        Assert.assertFalse(mDictionaryUnderTest.isValidWord(word));
        Assert.assertEquals(mDictionaryUnderTest.wordRequestedToBeDeletedFromStorage, word);
        //checking validity of the internal structure
        assetNodeArrayIsValid(mDictionaryUnderTest.getRoot());

        //re-adding
        assertTrue(mDictionaryUnderTest.addWord(word, wordFreq + 1));
        assertTrue(mDictionaryUnderTest.isValidWord(word));
        Assert.assertEquals(wordFreq + 1, mDictionaryUnderTest.getWordFrequency(word));
        mDictionaryUnderTest.wordRequestedToBeDeletedFromStorage = null;
        mDictionaryUnderTest.deleteWord(word);
        Assert.assertFalse(mDictionaryUnderTest.isValidWord(word));
        Assert.assertEquals(mDictionaryUnderTest.wordRequestedToBeDeletedFromStorage, word);
        //checking validity of the internal structure
        assetNodeArrayIsValid(mDictionaryUnderTest.getRoot());

        //a new one
        word = "new";
        assertTrue(mDictionaryUnderTest.addWord(word, wordFreq));
        assertTrue(mDictionaryUnderTest.isValidWord(word));
        Assert.assertEquals(wordFreq, mDictionaryUnderTest.getWordFrequency(word));
        mDictionaryUnderTest.wordRequestedToBeDeletedFromStorage = null;
        mDictionaryUnderTest.deleteWord(word);
        Assert.assertFalse(mDictionaryUnderTest.isValidWord(word));
        Assert.assertEquals(mDictionaryUnderTest.wordRequestedToBeDeletedFromStorage, word);
        //checking validity of the internal structure
        assetNodeArrayIsValid(mDictionaryUnderTest.getRoot());

        //none existing
        Assert.assertFalse(mDictionaryUnderTest.isValidWord("fail"));
        mDictionaryUnderTest.deleteWord("fail");
        Assert.assertFalse(mDictionaryUnderTest.isValidWord("fail"));
        //checking validity of the internal structure
        assetNodeArrayIsValid(mDictionaryUnderTest.getRoot());

        //deleting part of the root
        mDictionaryUnderTest.addWord("root", 1);
        mDictionaryUnderTest.addWord("rooting", 2);
        mDictionaryUnderTest.addWord("rootina", 2);
        Assert.assertFalse(mDictionaryUnderTest.isValidWord("roo"));
        Assert.assertFalse(mDictionaryUnderTest.isValidWord("rooti"));
        //checking validity of the internal structure
        assetNodeArrayIsValid(mDictionaryUnderTest.getRoot());

        mDictionaryUnderTest.deleteWord("root");
        Assert.assertFalse(mDictionaryUnderTest.isValidWord("roo"));
        Assert.assertFalse(mDictionaryUnderTest.isValidWord("root"));
        Assert.assertFalse(mDictionaryUnderTest.isValidWord("rooti"));
        assertTrue(mDictionaryUnderTest.isValidWord("rooting"));
        assertTrue(mDictionaryUnderTest.isValidWord("rootina"));
        //checking validity of the internal structure
        assetNodeArrayIsValid(mDictionaryUnderTest.getRoot());

        mDictionaryUnderTest.deleteWord("rooting");
        Assert.assertFalse(mDictionaryUnderTest.isValidWord("root"));
        Assert.assertFalse(mDictionaryUnderTest.isValidWord("rooti"));
        Assert.assertFalse(mDictionaryUnderTest.isValidWord("rooting"));
        assertTrue(mDictionaryUnderTest.isValidWord("rootina"));
        //checking validity of the internal structure
        assetNodeArrayIsValid(mDictionaryUnderTest.getRoot());

        mDictionaryUnderTest.addWord("root", 1);
        assertTrue(mDictionaryUnderTest.isValidWord("root"));
        Assert.assertFalse(mDictionaryUnderTest.isValidWord("rooting"));
        Assert.assertFalse(mDictionaryUnderTest.isValidWord("rooti"));
        assertTrue(mDictionaryUnderTest.isValidWord("rootina"));
        //checking validity of the internal structure
        assetNodeArrayIsValid(mDictionaryUnderTest.getRoot());

        mDictionaryUnderTest.deleteWord("rootina");
        assertTrue(mDictionaryUnderTest.isValidWord("root"));
        Assert.assertFalse(mDictionaryUnderTest.isValidWord("rooting"));
        Assert.assertFalse(mDictionaryUnderTest.isValidWord("rooti"));
        Assert.assertFalse(mDictionaryUnderTest.isValidWord("rootina"));
        //checking validity of the internal structure
        assetNodeArrayIsValid(mDictionaryUnderTest.getRoot());
    }

    private void assetNodeArrayIsValid(BTreeDictionary.NodeArray root) {
        assertTrue(root.length >= 0);
        assertTrue(root.length <= root.data.length);
        for (int i = 0; i < root.length; i++) {
            assertNotNull(root.data[i]);
            if (root.data[i].children != null)//it may be null.
                assetNodeArrayIsValid(root.data[i].children);
        }
    }

    @Test
    public void testClose() throws Exception {
        mDictionaryUnderTest.loadDictionary();

        assertTrue(mDictionaryUnderTest.isValidWord((String) TestableBTreeDictionary.STORAGE[0][1]));
        mDictionaryUnderTest.close();
        assertTrue(mDictionaryUnderTest.storageIsClosed);
        Assert.assertFalse(mDictionaryUnderTest.isValidWord((String) TestableBTreeDictionary.STORAGE[0][1]));
        Assert.assertEquals(mDictionaryUnderTest.getWordFrequency((String) TestableBTreeDictionary.STORAGE[0][1]), 0);
        Assert.assertFalse(mDictionaryUnderTest.addWord("fail", 1));
    }

    @Test
    public void testReadWordsFromStorageLimit() throws Exception {
        final AtomicInteger atomicInteger = new AtomicInteger(0);
        final int maxWordsToRead = RuntimeEnvironment.application.getResources().getInteger(R.integer.maximum_dictionary_words_to_load);
        Assert.assertEquals(5000, maxWordsToRead);
        TestableBTreeDictionary dictionary = new TestableBTreeDictionary("test", RuntimeEnvironment.application) {

            @Override
            protected void readWordsFromActualStorage(WordReadListener listener) {
                Random r = new Random();
                while (listener.onWordRead("w" + Integer.toHexString(r.nextInt()), 1 + r.nextInt(200))) {
                }
            }

            @Override
            protected void addWordFromStorageToMemory(String word, int frequency) {
                atomicInteger.addAndGet(1);
            }
        };
        dictionary.loadDictionary();

        Assert.assertEquals(maxWordsToRead, atomicInteger.get());
    }

    @Test
    public void testDoesNotAddInvalidWords() throws Exception {
        final AtomicInteger atomicInteger = new AtomicInteger(0);
        TestableBTreeDictionary dictionary = new TestableBTreeDictionary("test", RuntimeEnvironment.application) {

            @Override
            protected void readWordsFromActualStorage(WordReadListener listener) {
                listener.onWordRead("valid", 1);
                listener.onWordRead("invalid", 0);
                listener.onWordRead("", 1);
                listener.onWordRead(null, 1);
                listener.onWordRead("alsoInvalid", -1);
            }

            @Override
            protected void addWordFromStorageToMemory(String word, int frequency) {
                super.addWordFromStorageToMemory(word, frequency);
                atomicInteger.addAndGet(1);
            }
        };
        dictionary.loadDictionary();

        Assert.assertEquals(1, atomicInteger.get());
        Assert.assertTrue(dictionary.isValidWord("valid"));
        Assert.assertFalse(dictionary.isValidWord("invalid"));
        Assert.assertFalse(dictionary.isValidWord("alsoInvalid"));
    }
}
