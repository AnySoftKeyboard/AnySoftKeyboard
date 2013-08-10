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


import android.test.ActivityInstrumentationTestCase2;
import com.anysoftkeyboard.ui.settings.MainSettings;
import junit.framework.Assert;


public class BTreeDictionaryTest extends ActivityInstrumentationTestCase2<MainSettings>/*need a valid Application object, so I use ActivityInstrumentationTestCase2*/ {

    public BTreeDictionaryTest() {
        super(MainSettings.class);
    }


    public void testLoadDictionary() throws Exception {
        TestableBTreeDictionary dictionary = new TestableBTreeDictionary("TEST", getActivity().getApplicationContext());
        //no words now
        Assert.assertFalse(dictionary.isValidWord((String)TestableBTreeDictionary.STORAGE[0][1]));

        //ok, now yes words
        dictionary.loadDictionary();
        for(int row=0; row<TestableBTreeDictionary.STORAGE.length; row++) {
            final String word = (String)TestableBTreeDictionary.STORAGE[row][1];
            final int freq = ((Integer)TestableBTreeDictionary.STORAGE[row][2]).intValue();
            Assert.assertTrue("Word at row "+row+" ("+word+") should be valid.", dictionary.isValidWord(word));
            Assert.assertEquals(dictionary.getWordFrequency(word), freq);
        }
        //checking validity of the internal structure
        assetNodeArrayIsValid(dictionary.getRoot());
    }

    public void testAddWord() throws Exception {
        TestableBTreeDictionary dictionary = new TestableBTreeDictionary("TEST", getActivity().getApplicationContext());
        dictionary.loadDictionary();

        Assert.assertTrue(dictionary.addWord("new", 23));
        Assert.assertEquals("new", dictionary.wordRequestedToAddedToStorage);
        Assert.assertEquals(23, dictionary.wordFrequencyRequestedToAddedToStorage);
        Assert.assertTrue(dictionary.isValidWord("new"));
        Assert.assertEquals(dictionary.getWordFrequency("new"), 23);
        //checking validity of the internal structure
        assetNodeArrayIsValid(dictionary.getRoot());

        Assert.assertTrue(dictionary.addWord("new", 34));
        Assert.assertEquals("new", dictionary.wordRequestedToAddedToStorage);
        Assert.assertEquals(34, dictionary.wordFrequencyRequestedToAddedToStorage);
        Assert.assertTrue(dictionary.isValidWord("new"));
        Assert.assertEquals(dictionary.getWordFrequency("new"), 34);
        //checking validity of the internal structure
        assetNodeArrayIsValid(dictionary.getRoot());

        Assert.assertTrue(dictionary.addWord("newa", 45));
        Assert.assertTrue(dictionary.isValidWord("newa"));
        Assert.assertEquals(dictionary.getWordFrequency("new"), 34);
        Assert.assertEquals(dictionary.getWordFrequency("newa"), 45);
        //checking validity of the internal structure
        assetNodeArrayIsValid(dictionary.getRoot());

        Assert.assertTrue(dictionary.addWord("nea", 47));
        Assert.assertEquals("nea", dictionary.wordRequestedToAddedToStorage);
        Assert.assertEquals(47, dictionary.wordFrequencyRequestedToAddedToStorage);
        Assert.assertTrue(dictionary.isValidWord("nea"));
        Assert.assertEquals(dictionary.getWordFrequency("new"), 34);
        Assert.assertEquals(dictionary.getWordFrequency("newa"), 45);
        Assert.assertEquals(dictionary.getWordFrequency("nea"), 47);
        //checking validity of the internal structure
        assetNodeArrayIsValid(dictionary.getRoot());

        Assert.assertTrue(dictionary.addWord("neabb", 50));
        Assert.assertEquals("neabb", dictionary.wordRequestedToAddedToStorage);
        Assert.assertEquals(50, dictionary.wordFrequencyRequestedToAddedToStorage);
        Assert.assertTrue(dictionary.isValidWord("neabb"));
        Assert.assertFalse(dictionary.isValidWord("neab"));
        Assert.assertEquals(dictionary.getWordFrequency("new"), 34);
        Assert.assertEquals(dictionary.getWordFrequency("newa"), 45);
        Assert.assertEquals(dictionary.getWordFrequency("nea"), 47);
        Assert.assertEquals(dictionary.getWordFrequency("neabb"), 50);
        Assert.assertEquals(dictionary.getWordFrequency("neab"), 0);
        //checking validity of the internal structure
        assetNodeArrayIsValid(dictionary.getRoot());
    }

    public void testOnStorageChanged() throws Exception {

    }

    public void testDeleteWord() throws Exception {
        TestableBTreeDictionary dictionary = new TestableBTreeDictionary("TEST", getActivity().getApplicationContext());
        dictionary.loadDictionary();
        //from read storage
        String word = (String)TestableBTreeDictionary.STORAGE[0][1];
        int wordFreq = ((Integer)TestableBTreeDictionary.STORAGE[0][2]).intValue();
        Assert.assertTrue(dictionary.isValidWord(word));
        dictionary.deleteWord(word);
        Assert.assertFalse(dictionary.isValidWord(word));
        Assert.assertEquals(dictionary.wordRequestedToBeDeletedFromStorage, word);
        //checking validity of the internal structure
        assetNodeArrayIsValid(dictionary.getRoot());

        //re-adding
        Assert.assertTrue(dictionary.addWord(word, wordFreq+1));
        Assert.assertTrue(dictionary.isValidWord(word));
        Assert.assertEquals(wordFreq+1, dictionary.getWordFrequency(word));
        dictionary.wordRequestedToBeDeletedFromStorage = null;
        dictionary.deleteWord(word);
        Assert.assertFalse(dictionary.isValidWord(word));
        Assert.assertEquals(dictionary.wordRequestedToBeDeletedFromStorage, word);
        //checking validity of the internal structure
        assetNodeArrayIsValid(dictionary.getRoot());

        //a new one
        word = "new";
        Assert.assertTrue(dictionary.addWord(word, wordFreq));
        Assert.assertTrue(dictionary.isValidWord(word));
        Assert.assertEquals(wordFreq, dictionary.getWordFrequency(word));
        dictionary.wordRequestedToBeDeletedFromStorage = null;
        dictionary.deleteWord(word);
        Assert.assertFalse(dictionary.isValidWord(word));
        Assert.assertEquals(dictionary.wordRequestedToBeDeletedFromStorage, word);
        //checking validity of the internal structure
        assetNodeArrayIsValid(dictionary.getRoot());

        //none existing
        Assert.assertFalse(dictionary.isValidWord("fail"));
        dictionary.deleteWord("fail");
        Assert.assertFalse(dictionary.isValidWord("fail"));
        //checking validity of the internal structure
        assetNodeArrayIsValid(dictionary.getRoot());

        //deleting part of the root
        dictionary.addWord("root", 1);
        dictionary.addWord("rooting", 2);
        dictionary.addWord("rootina", 2);
        Assert.assertFalse(dictionary.isValidWord("roo"));
        Assert.assertFalse(dictionary.isValidWord("rooti"));
        //checking validity of the internal structure
        assetNodeArrayIsValid(dictionary.getRoot());

        dictionary.deleteWord("root");
        Assert.assertFalse(dictionary.isValidWord("roo"));
        Assert.assertFalse(dictionary.isValidWord("root"));
        Assert.assertFalse(dictionary.isValidWord("rooti"));
        Assert.assertTrue(dictionary.isValidWord("rooting"));
        Assert.assertTrue(dictionary.isValidWord("rootina"));
        //checking validity of the internal structure
        assetNodeArrayIsValid(dictionary.getRoot());

        dictionary.deleteWord("rooting");
        Assert.assertFalse(dictionary.isValidWord("root"));
        Assert.assertFalse(dictionary.isValidWord("rooti"));
        Assert.assertFalse(dictionary.isValidWord("rooting"));
        Assert.assertTrue(dictionary.isValidWord("rootina"));
        //checking validity of the internal structure
        assetNodeArrayIsValid(dictionary.getRoot());

        dictionary.addWord("root", 1);
        Assert.assertTrue(dictionary.isValidWord("root"));
        Assert.assertFalse(dictionary.isValidWord("rooting"));
        Assert.assertFalse(dictionary.isValidWord("rooti"));
        Assert.assertTrue(dictionary.isValidWord("rootina"));
        //checking validity of the internal structure
        assetNodeArrayIsValid(dictionary.getRoot());

        dictionary.deleteWord("rootina");
        Assert.assertTrue(dictionary.isValidWord("root"));
        Assert.assertFalse(dictionary.isValidWord("rooting"));
        Assert.assertFalse(dictionary.isValidWord("rooti"));
        Assert.assertFalse(dictionary.isValidWord("rootina"));
        //checking validity of the internal structure
        assetNodeArrayIsValid(dictionary.getRoot());
    }

    private void assetNodeArrayIsValid(BTreeDictionary.NodeArray root) {
        assertTrue(root.length >=0);
        assertTrue(root.length <= root.data.length);
        for (int i=0;i<root.length;i++) {
            assertNotNull(root.data[i]);
            if (root.data[i].children != null)//it may be null.
                assetNodeArrayIsValid(root.data[i].children);
        }
    }

    public void testClose() throws Exception {
        TestableBTreeDictionary dictionary = new TestableBTreeDictionary("TEST", getActivity().getApplicationContext());
        dictionary.loadDictionary();

        Assert.assertTrue(dictionary.isValidWord((String)TestableBTreeDictionary.STORAGE[0][1]));
        dictionary.close();
        Assert.assertTrue(dictionary.storageIsClosed);
        Assert.assertFalse(dictionary.isValidWord((String)TestableBTreeDictionary.STORAGE[0][1]));
        Assert.assertEquals(dictionary.getWordFrequency((String)TestableBTreeDictionary.STORAGE[0][1]), 0);
        Assert.assertFalse(dictionary.addWord("fail", 1));
    }
}
