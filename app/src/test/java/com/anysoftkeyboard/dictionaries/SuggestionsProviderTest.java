package com.anysoftkeyboard.dictionaries;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.support.annotation.NonNull;

import com.anysoftkeyboard.AnySoftKeyboardTestRunner;
import com.anysoftkeyboard.SharedPrefsHelper;
import com.anysoftkeyboard.base.dictionaries.Dictionary;
import com.anysoftkeyboard.base.dictionaries.KeyCodesProvider;
import com.anysoftkeyboard.base.dictionaries.WordComposer;
import com.anysoftkeyboard.nextword.NextWordSuggestions;
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
import java.util.Collections;
import java.util.List;

@RunWith(AnySoftKeyboardTestRunner.class)
public class SuggestionsProviderTest {

    private List<DictionaryAddOnAndBuilder> mFakeBuilders;
    private FakeBuilder mFakeBuilder;
    private SuggestionsProvider mSuggestionsProvider;
    private WordsHolder mWordsCallback;
    private NextWordSuggestions mSpiedNextWords;

    @Before
    public void setup() {
        mSuggestionsProvider = new SuggestionsProvider(RuntimeEnvironment.application) {
            @NonNull
            @Override
            protected UserDictionary createUserDictionaryForLocale(@NonNull String locale) {
                return new UserDictionary(RuntimeEnvironment.application, "en") {
                    @Override
                    NextWordSuggestions getUserNextWordGetter() {
                        return mSpiedNextWords = Mockito.spy(super.getUserNextWordGetter());
                    }
                };
            }
        };
        mWordsCallback = new WordsHolder();
        mFakeBuilder = Mockito.spy(new FakeBuilder("hell", "hello", "say", "said", "drink"));
        mFakeBuilders = new ArrayList<>();
        mFakeBuilders.add(mFakeBuilder);
    }

    @Test
    public void testDoesNotCreateDictionariesWhenPassingNullBuilder() {
        mSuggestionsProvider.setupSuggestionsForKeyboard(Collections.<DictionaryAddOnAndBuilder>emptyList());
        //zero futures means no load requests
        Assert.assertEquals(0, Robolectric.getBackgroundThreadScheduler().size());
        Assert.assertEquals(0, Robolectric.getForegroundThreadScheduler().size());

        mSuggestionsProvider.getSuggestions(wordFor("hel"), mWordsCallback);
        Assert.assertEquals(0, mWordsCallback.wordsReceived.size());
    }

    @Test
    public void testSetupSingleDictionaryBuilder() throws Exception {
        mSuggestionsProvider.setupSuggestionsForKeyboard(mFakeBuilders);

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
    public void testMultipleSetupSingleDictionaryBuilder() throws Exception {
        FakeBuilder fakeBuilder2 = Mockito.spy(new FakeBuilder("salt", "helll"));
        mFakeBuilders.add(fakeBuilder2);
        mSuggestionsProvider.setupSuggestionsForKeyboard(mFakeBuilders);

        //dictionary creations
        Mockito.verify(mFakeBuilder).createDictionary();
        Mockito.verify(mFakeBuilder).createAutoText();
        Mockito.verify(mFakeBuilder).createInitialSuggestions();
        Mockito.verify(mFakeBuilder, Mockito.atLeastOnce()).getLanguage();
        //second builder
        Mockito.verify(fakeBuilder2).createDictionary();
        Mockito.verify(fakeBuilder2).createAutoText();
        Mockito.verify(fakeBuilder2).createInitialSuggestions();
        Mockito.verify(fakeBuilder2, Mockito.atLeastOnce()).getLanguage();

        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();

        //after loading
        mSuggestionsProvider.getSuggestions(wordFor("hel"), mWordsCallback);
        Assert.assertEquals(3, mWordsCallback.wordsReceived.size());
        Assert.assertTrue(mWordsCallback.wordsReceived.contains("hell"));
        Assert.assertTrue(mWordsCallback.wordsReceived.contains("hello"));
        Assert.assertTrue(mWordsCallback.wordsReceived.contains("helll"));

        Mockito.verify(mFakeBuilder.mSpiedDictionary).getWords(Mockito.any(KeyCodesProvider.class), Mockito.same(mWordsCallback));
        Mockito.verify(fakeBuilder2.mSpiedDictionary).getWords(Mockito.any(KeyCodesProvider.class), Mockito.same(mWordsCallback));
    }

    @Test
    public void testLookupDelegation() throws Exception {
        mSuggestionsProvider.setupSuggestionsForKeyboard(mFakeBuilders);

        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();

        Mockito.verify(mFakeBuilder).createAutoText();

        mSuggestionsProvider.lookupQuickFix("hello");
        Mockito.verify(mFakeBuilder.mSpiedAutoText).lookup(Mockito.eq("hello"));

        mSuggestionsProvider.close();

        Assert.assertNull(mSuggestionsProvider.lookupQuickFix("hell"));
        Mockito.verify(mFakeBuilder.mSpiedAutoText, Mockito.never()).lookup(Mockito.eq("hell"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testDoesNotLearnWhenIncognito() throws Exception {
        mSuggestionsProvider.setupSuggestionsForKeyboard(mFakeBuilders);
        Assert.assertFalse(mSuggestionsProvider.isIncognitoMode());

        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();

        mSuggestionsProvider.setIncognitoMode(true);
        Assert.assertTrue(mSuggestionsProvider.isIncognitoMode());
        Assert.assertFalse(mSuggestionsProvider.addWordToUserDictionary("SECRET"));
        int tries = 10;
        while (tries-- > 0) {
            Assert.assertFalse(mSuggestionsProvider.tryToLearnNewWord("SECRET", 10));
        }
        //sanity: checking that "hello" is a valid word, so it would be checked with next-word
        Assert.assertTrue(mSuggestionsProvider.isValidWord("hello"));
        mSuggestionsProvider.getNextWords("hello", Mockito.mock(List.class), 10);
        Mockito.verify(mSpiedNextWords).getNextWords(Mockito.eq("hello"), Mockito.anyInt(), Mockito.anyInt());
        Mockito.verify(mSpiedNextWords, Mockito.never()).notifyNextTypedWord(Mockito.anyString());

        mSuggestionsProvider.setIncognitoMode(false);
        Assert.assertFalse(mSuggestionsProvider.isIncognitoMode());
        Assert.assertTrue(mSuggestionsProvider.addWordToUserDictionary("SECRET"));

        mSuggestionsProvider.getNextWords("hell", Mockito.mock(List.class), 10);
        Mockito.verify(mSpiedNextWords).getNextWords(Mockito.eq("hell"), Mockito.anyInt(), Mockito.anyInt());
        Mockito.verify(mSpiedNextWords).notifyNextTypedWord("hell");
    }

    @Test
    public void testLookupWhenNullAutoTextDelegation() throws Exception {
        Mockito.doReturn(null).when(mFakeBuilder).createAutoText();

        mSuggestionsProvider.setupSuggestionsForKeyboard(mFakeBuilders);

        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();

        Mockito.verify(mFakeBuilder).createAutoText();

        Assert.assertNull(mSuggestionsProvider.lookupQuickFix("hello"));
        //did not create an auto-text
        Assert.assertNull(mFakeBuilder.mSpiedAutoText);

        mSuggestionsProvider.close();

        Assert.assertNull(mSuggestionsProvider.lookupQuickFix("hell"));
        Assert.assertNull(mFakeBuilder.mSpiedAutoText);
    }

    @Test
    public void testDoesNotCreateAutoText() throws Exception {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_quick_fix, false);
        mSuggestionsProvider.setupSuggestionsForKeyboard(mFakeBuilders);

        //dictionary creations
        Mockito.verify(mFakeBuilder).createDictionary();
        Mockito.verify(mFakeBuilder, Mockito.never()).createAutoText();
        Mockito.verify(mFakeBuilder).createInitialSuggestions();
        Mockito.verify(mFakeBuilder, Mockito.atLeastOnce()).getLanguage();
    }

    @Test
    public void testIsValid() throws Exception {
        Assert.assertFalse(mSuggestionsProvider.isValidWord("hello"));

        mSuggestionsProvider.setupSuggestionsForKeyboard(mFakeBuilders);

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
        mSuggestionsProvider.setupSuggestionsForKeyboard(mFakeBuilders);
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

        public static final String FAKE_BUILDER_ID = "673957f5-835d-4a99-893a-e68950b0a2ba";
        private AutoText mSpiedAutoText;
        private Dictionary mSpiedDictionary;

        public FakeBuilder(String... wordsToLoad) {
            super(RuntimeEnvironment.application, RuntimeEnvironment.application,
                    FAKE_BUILDER_ID, "fake", "fake dictionary", false, 1, "en", R.array.words_dict_array, R.xml.en_autotext, R.array.english_initial_suggestions);
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