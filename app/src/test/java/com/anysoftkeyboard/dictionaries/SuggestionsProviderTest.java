package com.anysoftkeyboard.dictionaries;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.support.annotation.NonNull;

import com.anysoftkeyboard.AnySoftKeyboardTestRunner;
import com.anysoftkeyboard.SharedPrefsHelper;
import com.anysoftkeyboard.base.dictionaries.Dictionary;
import com.anysoftkeyboard.base.dictionaries.KeyCodesProvider;
import com.anysoftkeyboard.base.dictionaries.WordComposer;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.AnyRoboApplication;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(AnySoftKeyboardTestRunner.class)
public class SuggestionsProviderTest {

    private FakeBuilder mFakeBuilder;
    private AnyRoboApplication mApplication;
    private SuggestionsProvider mSuggestionsProvider;
    private ExternalDictionaryFactory mDictionaryFactory;
    private WordsHolder mWordsCallback;

    @Before
    public void setup() {
        mApplication = (AnyRoboApplication) RuntimeEnvironment.application;
        mSuggestionsProvider = new SuggestionsProvider(mApplication);
        mDictionaryFactory = AnyApplication.getExternalDictionaryFactory(mApplication);
        mWordsCallback = new WordsHolder();
        mFakeBuilder = Mockito.spy(new FakeBuilder("hell", "hello", "say", "said", "drink"));
    }

    @Test
    public void testDoesNotCreateDictionariesWhenPassingNullBuilder() {
        mSuggestionsProvider.setupSuggestionsForKeyboard(null);
        //zero futures means no load requests
        Assert.assertEquals(0, Robolectric.getBackgroundThreadScheduler().size());
        Assert.assertEquals(0, Robolectric.getForegroundThreadScheduler().size());

        mSuggestionsProvider.getSuggestions(wordFor("hel"), mWordsCallback);
        Assert.assertEquals(0, mWordsCallback.wordsReceived.size());
    }

    @Test
    public void testSetupSingleDictionaryBuilder() throws Exception {
        mSuggestionsProvider.setupSuggestionsForKeyboard(mFakeBuilder);

        //dictionary creations
        Mockito.verify(mFakeBuilder).createDictionary();
        Mockito.verify(mFakeBuilder).createAutoText();
        Mockito.verify(mFakeBuilder).createInitialSuggestions();
        Mockito.verify(mFakeBuilder, Mockito.atLeastOnce()).getLanguage();

        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();

        //after loading
        mSuggestionsProvider.getSuggestions(wordFor("hel"), mWordsCallback);
        Assert.assertEquals(2, mWordsCallback.wordsReceived.size());

        Mockito.verify(mFakeBuilder.mSpiedDictionary).getWords(Mockito.any(KeyCodesProvider.class), Mockito.same(mWordsCallback));
    }

    @Test
    public void testLookupDelegation() throws Exception {
        mSuggestionsProvider.setupSuggestionsForKeyboard(mFakeBuilder);

        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();

        Mockito.verify(mFakeBuilder).createAutoText();

        mSuggestionsProvider.lookupQuickFix("hello");
        Mockito.verify(mFakeBuilder.mSpiedAutoText).lookup(Mockito.eq("hello"));

        mSuggestionsProvider.close();

        mSuggestionsProvider.lookupQuickFix("hell");
        Mockito.verify(mFakeBuilder.mSpiedAutoText, Mockito.never()).lookup(Mockito.eq("hell"));
    }

    @Test
    public void testDoesNotCreateAutoText() throws Exception {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_quick_fix, false);
        mSuggestionsProvider.setupSuggestionsForKeyboard(mFakeBuilder);

        //dictionary creations
        Mockito.verify(mFakeBuilder).createDictionary();
        Mockito.verify(mFakeBuilder, Mockito.never()).createAutoText();
        Mockito.verify(mFakeBuilder).createInitialSuggestions();
        Mockito.verify(mFakeBuilder, Mockito.atLeastOnce()).getLanguage();
    }

    @Test
    public void testIsValid() throws Exception {
        Assert.assertFalse(mSuggestionsProvider.isValidWord("hello"));

        mSuggestionsProvider.setupSuggestionsForKeyboard(mFakeBuilder);

        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();

        Assert.assertTrue(mSuggestionsProvider.isValidWord("hello"));

        Mockito.verify(mFakeBuilder.mSpiedDictionary).isValidWord(Mockito.eq("hello"));
        Mockito.reset(mFakeBuilder.mSpiedDictionary);

        mSuggestionsProvider.close();

        Assert.assertFalse(mSuggestionsProvider.isValidWord("hello"));
        Mockito.verify(mFakeBuilder.mSpiedDictionary, Mockito.never()).isValidWord(Mockito.any(CharSequence.class));
    }

    private WordComposer wordFor(String word) {
        WordComposer wordComposer = new WordComposer();
        for (char c : word.toCharArray()) wordComposer.add(c, new int[]{c});

        return wordComposer;
    }

    @Test
    public void testCloseWillConvertAllDictionariesToEmptyDictionaries() {
        mSuggestionsProvider.setupSuggestionsForKeyboard(mFakeBuilder);
        Robolectric.flushBackgroundThreadScheduler();
        mSuggestionsProvider.close();


        mSuggestionsProvider.getSuggestions(wordFor("hello"), mWordsCallback);
        Assert.assertEquals(0, mWordsCallback.wordsReceived.size());
    }

    private static class WordsHolder implements Dictionary.WordCallback {
        public final List<String> wordsReceived = new ArrayList<>();

        @Override
        public boolean addWord(char[] word, int wordOffset, int wordLength, int frequency, Dictionary from) {
            wordsReceived.add(new String(word, wordOffset, wordLength));
            return true;
        }
    }

    private static class FakeBuilder extends DictionaryAddOnAndBuilder {

        public static final String FAKE_BUIILDER_ID = "673957f5-835d-4a99-893a-e68950b0a2ba";
        private AutoText mSpiedAutoText;
        private Dictionary mSpiedDictionary;

        public FakeBuilder(String... wordsToLoad) {
            super(RuntimeEnvironment.application, RuntimeEnvironment.application,
                    FAKE_BUIILDER_ID, "fake", "fake dictionary", false, 1, "en", R.array.words_dict_array, R.xml.en_autotext, R.array.english_initial_suggestions);
            mSpiedDictionary = Mockito.spy(new FakeBTreeDictionary(wordsToLoad));
        }

        @Override
        public AutoText createAutoText() {
            return mSpiedAutoText = Mockito.spy(super.createAutoText());
        }

        @Override
        public Dictionary createDictionary() throws Exception {
            return mSpiedDictionary;
        }

        @NonNull
        @Override
        public List<String> createInitialSuggestions() {
            return Arrays.asList(";", ".");
        }
    }

    private static class FakeBTreeDictionary extends BTreeDictionary {

        private final String[] mWordsToLoad;

        FakeBTreeDictionary(String... words) {
            super("fake_dict", RuntimeEnvironment.application);
            mWordsToLoad = words;
        }

        @Override
        protected void readWordsFromActualStorage(WordReadListener wordReadListener) {
            int freq = 1;
            for (String word : mWordsToLoad) {
                wordReadListener.onWordRead(word, freq++);
            }
        }

        @Override
        protected void deleteWordFromStorage(String word) {

        }

        @Override
        protected void registerObserver(ContentObserver dictionaryContentObserver, ContentResolver contentResolver) {

        }

        @Override
        protected void addWordToStorage(String word, int frequency) {

        }

        @Override
        protected void closeStorage() {

        }
    }

}