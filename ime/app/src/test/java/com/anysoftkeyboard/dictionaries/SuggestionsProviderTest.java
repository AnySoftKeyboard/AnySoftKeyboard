package com.anysoftkeyboard.dictionaries;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.menny.android.anysoftkeyboard.R.array.english_initial_suggestions;
import static com.menny.android.anysoftkeyboard.R.integer.anysoftkeyboard_api_version_code;
import static com.menny.android.anysoftkeyboard.R.xml.english_autotext;

import androidx.annotation.NonNull;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.dictionaries.content.ContactsDictionary;
import com.anysoftkeyboard.nextword.NextWordSuggestions;
import com.anysoftkeyboard.rx.TestRxSchedulers;
import com.anysoftkeyboard.test.SharedPrefsHelper;
import com.menny.android.anysoftkeyboard.R;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mockito;

@SuppressWarnings("ResultOfMethodCallIgnored")
@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class SuggestionsProviderTest {

  private List<DictionaryAddOnAndBuilder> mFakeBuilders;
  private FakeBuilder mFakeBuilder;
  private SuggestionsProvider mSuggestionsProvider;
  private WordsHolder mWordsCallback;
  private NextWordSuggestions mSpiedNextWords;
  private DictionaryBackgroundLoader.Listener mMockListener;
  private UserDictionary mTestUserDictionary;
  private ContactsDictionary mFakeContactsDictionary;

  @Before
  public void setup() {
    mMockListener = Mockito.mock(DictionaryBackgroundLoader.Listener.class);
    mTestUserDictionary =
        Mockito.spy(
            new UserDictionary(getApplicationContext(), "en") {
              @Override
              NextWordSuggestions getUserNextWordGetter() {
                return mSpiedNextWords = Mockito.spy(super.getUserNextWordGetter());
              }
            });

    mSuggestionsProvider =
        new SuggestionsProvider(getApplicationContext()) {
          @NonNull
          @Override
          protected UserDictionary createUserDictionaryForLocale(@NonNull String locale) {
            return mTestUserDictionary;
          }

          @NonNull
          @Override
          protected ContactsDictionary createRealContactsDictionary() {
            return mFakeContactsDictionary = Mockito.mock(ContactsDictionary.class);
          }
        };
    mWordsCallback = new WordsHolder();
    mFakeBuilder = Mockito.spy(new FakeBuilder("hell", "hello", "say", "said", "drink"));
    mFakeBuilders = new ArrayList<>();
    mFakeBuilders.add(mFakeBuilder);
  }

  @Test
  public void testDoesNotCreateDictionariesWhenPassingNullBuilder() {
    mSuggestionsProvider.setupSuggestionsForKeyboard(Collections.emptyList(), mMockListener);
    TestRxSchedulers.drainAllTasks();
    mSuggestionsProvider.getSuggestions(wordFor("hel"), mWordsCallback);
    Assert.assertEquals(0, mWordsCallback.wordsReceived.size());
  }

  @Test
  public void testSetupSingleDictionaryBuilder() throws Exception {
    mSuggestionsProvider.setupSuggestionsForKeyboard(mFakeBuilders, mMockListener);

    // dictionary creations
    Mockito.verify(mFakeBuilder).createDictionary();
    Mockito.verify(mFakeBuilder).createAutoText();
    Mockito.verify(mFakeBuilder).createInitialSuggestions();
    Mockito.verify(mFakeBuilder, Mockito.atLeastOnce()).getLanguage();

    TestRxSchedulers.drainAllTasks();

    // after loading
    mSuggestionsProvider.getSuggestions(wordFor("hel"), mWordsCallback);
    Assert.assertEquals(2, mWordsCallback.wordsReceived.size());

    Mockito.verify(mFakeBuilder.mSpiedDictionary)
        .getSuggestions(Mockito.any(KeyCodesProvider.class), Mockito.same(mWordsCallback));
  }

  @Test
  public void testDiscardIfNoChangesInDictionaries() throws Exception {
    mSuggestionsProvider.setupSuggestionsForKeyboard(mFakeBuilders, mMockListener);

    TestRxSchedulers.drainAllTasks();

    Mockito.verify(mFakeBuilder).createDictionary();
    Mockito.verify(mFakeBuilder.mSpiedDictionary, Mockito.never()).close();

    Mockito.reset(mFakeBuilder, mFakeBuilder.mSpiedDictionary);

    mSuggestionsProvider.setupSuggestionsForKeyboard(mFakeBuilders, mMockListener);

    TestRxSchedulers.drainAllTasks();

    Mockito.verify(mFakeBuilder, Mockito.never()).createDictionary();
    Mockito.verify(mFakeBuilder.mSpiedDictionary, Mockito.never()).close();
  }

  @Test
  public void testDoesNotDiscardIfPrefQuickFixChanged() throws Exception {
    mSuggestionsProvider.setupSuggestionsForKeyboard(mFakeBuilders, mMockListener);

    TestRxSchedulers.drainAllTasks();

    Mockito.verify(mFakeBuilder).createDictionary();
    Mockito.verify(mFakeBuilder.mSpiedDictionary, Mockito.never()).close();

    Mockito.reset(mFakeBuilder, mFakeBuilder.mSpiedDictionary);

    SharedPrefsHelper.setPrefsValue(R.string.settings_key_quick_fix, false);

    mSuggestionsProvider.setupSuggestionsForKeyboard(mFakeBuilders, mMockListener);

    TestRxSchedulers.drainAllTasks();

    Mockito.verify(mFakeBuilder).createDictionary();
    Mockito.verify(mFakeBuilder.mSpiedDictionary).close();
  }

  @Test
  public void testDoesNotDiscardIfPrefContactsChanged() throws Exception {
    mSuggestionsProvider.setupSuggestionsForKeyboard(mFakeBuilders, mMockListener);

    TestRxSchedulers.drainAllTasks();

    Mockito.verify(mFakeBuilder).createDictionary();
    Mockito.verify(mFakeBuilder.mSpiedDictionary, Mockito.never()).close();

    Mockito.reset(mFakeBuilder, mFakeBuilder.mSpiedDictionary);

    SharedPrefsHelper.setPrefsValue(R.string.settings_key_use_contacts_dictionary, false);

    mSuggestionsProvider.setupSuggestionsForKeyboard(mFakeBuilders, mMockListener);

    TestRxSchedulers.drainAllTasks();

    Mockito.verify(mFakeBuilder).createDictionary();
    Mockito.verify(mFakeBuilder.mSpiedDictionary).close();
  }

  @Test
  public void testDoesNotDiscardIfCloseCalled() throws Exception {
    mSuggestionsProvider.setupSuggestionsForKeyboard(mFakeBuilders, mMockListener);

    TestRxSchedulers.drainAllTasks();

    Mockito.verify(mFakeBuilder).createDictionary();
    Mockito.verify(mFakeBuilder.mSpiedDictionary, Mockito.never()).close();

    mSuggestionsProvider.close();
    TestRxSchedulers.drainAllTasks();

    Mockito.reset(mFakeBuilder, mFakeBuilder.mSpiedDictionary);

    mSuggestionsProvider.setupSuggestionsForKeyboard(mFakeBuilders, mMockListener);

    TestRxSchedulers.drainAllTasks();

    Mockito.verify(mFakeBuilder).createDictionary();
    Mockito.verify(mFakeBuilder.mSpiedDictionary, Mockito.never()).close();
  }

  @Test
  public void testMultipleSetupSingleDictionaryBuilder() throws Exception {
    FakeBuilder fakeBuilder2 = Mockito.spy(new FakeBuilder("salt", "helll"));
    mFakeBuilders.add(fakeBuilder2);
    mSuggestionsProvider.setupSuggestionsForKeyboard(mFakeBuilders, mMockListener);

    // dictionary creations
    Mockito.verify(mFakeBuilder).createDictionary();
    Mockito.verify(mFakeBuilder).createAutoText();
    Mockito.verify(mFakeBuilder).createInitialSuggestions();
    Mockito.verify(mFakeBuilder, Mockito.atLeastOnce()).getLanguage();
    // second builder
    Mockito.verify(fakeBuilder2).createDictionary();
    Mockito.verify(fakeBuilder2).createAutoText();
    Mockito.verify(fakeBuilder2).createInitialSuggestions();
    Mockito.verify(fakeBuilder2, Mockito.atLeastOnce()).getLanguage();

    TestRxSchedulers.drainAllTasks();

    // after loading
    final WordComposer wordComposer = wordFor("hel");
    mSuggestionsProvider.getSuggestions(wordComposer, mWordsCallback);

    Mockito.verify(mFakeBuilder.mSpiedDictionary)
        .getSuggestions(Mockito.same(wordComposer), Mockito.same(mWordsCallback));
    Mockito.verify(fakeBuilder2.mSpiedDictionary)
        .getSuggestions(Mockito.same(wordComposer), Mockito.same(mWordsCallback));

    Assert.assertEquals(3, mWordsCallback.wordsReceived.size());
    Assert.assertTrue(mWordsCallback.wordsReceived.contains("hell"));
    Assert.assertTrue(mWordsCallback.wordsReceived.contains("hello"));
    Assert.assertTrue(mWordsCallback.wordsReceived.contains("helll"));
  }

  @Test
  public void testLookupDelegation() throws Exception {
    mSuggestionsProvider.setupSuggestionsForKeyboard(mFakeBuilders, mMockListener);

    TestRxSchedulers.drainAllTasks();

    Mockito.verify(mFakeBuilder).createAutoText();

    mSuggestionsProvider.getAutoText(wordFor("hello"), mWordsCallback);
    Mockito.verify(mFakeBuilder.mSpiedAutoText).lookup(Mockito.eq("hello"));

    mSuggestionsProvider.close();

    mSuggestionsProvider.getAutoText(wordFor("hell"), mWordsCallback);
    Mockito.verify(mFakeBuilder.mSpiedAutoText, Mockito.never()).lookup(Mockito.eq("hell"));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testDoesNotLearnWhenIncognito() throws Exception {
    mSuggestionsProvider.setupSuggestionsForKeyboard(mFakeBuilders, mMockListener);
    Assert.assertFalse(mSuggestionsProvider.isIncognitoMode());

    TestRxSchedulers.drainAllTasks();

    mSuggestionsProvider.setIncognitoMode(true);
    Assert.assertTrue(mSuggestionsProvider.isIncognitoMode());
    Assert.assertFalse(mSuggestionsProvider.addWordToUserDictionary("SECRET"));
    int tries = 10;
    while (tries-- > 0) {
      Assert.assertFalse(mSuggestionsProvider.tryToLearnNewWord("SECRET", 10));
    }
    // sanity: checking that "hello" is a valid word, so it would be checked with next-word
    Assert.assertTrue(mSuggestionsProvider.isValidWord("hello"));
    mSuggestionsProvider.getNextWords("hello", Mockito.mock(List.class), 10);
    Mockito.verify(mSpiedNextWords)
        .getNextWords(Mockito.eq("hello"), Mockito.anyInt(), Mockito.anyInt());
    Mockito.verify(mSpiedNextWords, Mockito.never()).notifyNextTypedWord(Mockito.anyString());

    mSuggestionsProvider.setIncognitoMode(false);
    Assert.assertFalse(mSuggestionsProvider.isIncognitoMode());
    Assert.assertTrue(mSuggestionsProvider.addWordToUserDictionary("SECRET"));

    mSuggestionsProvider.getNextWords("hell", Mockito.mock(List.class), 10);
    Mockito.verify(mSpiedNextWords)
        .getNextWords(Mockito.eq("hell"), Mockito.anyInt(), Mockito.anyInt());
    Mockito.verify(mSpiedNextWords).notifyNextTypedWord("hell");
  }

  @Test
  public void testLookupWhenNullAutoTextDelegation() throws Exception {
    Mockito.doReturn(null).when(mFakeBuilder).createAutoText();

    mSuggestionsProvider.setupSuggestionsForKeyboard(mFakeBuilders, mMockListener);

    TestRxSchedulers.drainAllTasks();

    Mockito.verify(mFakeBuilder).createAutoText();

    mSuggestionsProvider.getAutoText(wordFor("hello"), mWordsCallback);
    // did not create an auto-text
    Assert.assertNull(mFakeBuilder.mSpiedAutoText);

    mSuggestionsProvider.close();

    mSuggestionsProvider.getAutoText(wordFor("hell"), mWordsCallback);
    Assert.assertNull(mFakeBuilder.mSpiedAutoText);
  }

  @Test
  public void testDoesNotCreateAutoText() throws Exception {
    SharedPrefsHelper.setPrefsValue(R.string.settings_key_quick_fix, false);
    mSuggestionsProvider.setupSuggestionsForKeyboard(mFakeBuilders, mMockListener);

    // dictionary creations
    Mockito.verify(mFakeBuilder).createDictionary();
    Mockito.verify(mFakeBuilder, Mockito.never()).createAutoText();
    Mockito.verify(mFakeBuilder).createInitialSuggestions();
    Mockito.verify(mFakeBuilder, Mockito.atLeastOnce()).getLanguage();
  }

  @Test
  public void testDoesNotCreateAutoTextForSecondaries() throws Exception {
    SharedPrefsHelper.setPrefsValue(R.string.settings_key_quick_fix, true);
    SharedPrefsHelper.setPrefsValue(R.string.settings_key_quick_fix_second_disabled, true);

    mFakeBuilders.add(Mockito.spy(new FakeBuilder("hell", "hello", "say", "said", "drink")));
    mFakeBuilders.add(Mockito.spy(new FakeBuilder("salt", "helll")));
    mFakeBuilders.add(Mockito.spy(new FakeBuilder("ciao", "come")));
    mFakeBuilders.add(Mockito.spy(new FakeBuilder("hola", "como")));

    mSuggestionsProvider.setupSuggestionsForKeyboard(mFakeBuilders, mMockListener);

    for (int i = 0; i < mFakeBuilders.size(); i++) {
      DictionaryAddOnAndBuilder fakeB = mFakeBuilders.get(i);
      if (i != 0) {
        Mockito.verify(fakeB).createDictionary();
        Mockito.verify(fakeB, Mockito.never()).createAutoText();
        Mockito.verify(fakeB).createInitialSuggestions();
        Mockito.verify(fakeB, Mockito.atLeastOnce()).getLanguage();
      }
    }
  }

  @Test
  public void testDoesCreateAutoTextForSecondaries() throws Exception {
    SharedPrefsHelper.setPrefsValue(R.string.settings_key_quick_fix, true);
    SharedPrefsHelper.setPrefsValue(R.string.settings_key_quick_fix_second_disabled, false);
    mFakeBuilders.add(Mockito.spy(new FakeBuilder("hell", "hello", "say", "said", "drink")));
    mFakeBuilders.add(Mockito.spy(new FakeBuilder("salt", "helll")));
    mFakeBuilders.add(Mockito.spy(new FakeBuilder("ciao", "come")));
    mFakeBuilders.add(Mockito.spy(new FakeBuilder("hola", "como")));
    mSuggestionsProvider.setupSuggestionsForKeyboard(mFakeBuilders, mMockListener);

    for (int i = 0; i < mFakeBuilders.size(); i++) {
      DictionaryAddOnAndBuilder fakeB = mFakeBuilders.get(i);
      if (i != 0) {
        Mockito.verify(fakeB).createDictionary();
        Mockito.verify(fakeB).createAutoText();
        Mockito.verify(fakeB).createInitialSuggestions();
        Mockito.verify(fakeB, Mockito.atLeastOnce()).getLanguage();
      }
    }
  }

  @Test
  public void testIsValid() throws Exception {
    Assert.assertFalse(mSuggestionsProvider.isValidWord("hello"));

    mSuggestionsProvider.setupSuggestionsForKeyboard(mFakeBuilders, mMockListener);

    TestRxSchedulers.drainAllTasks();

    Assert.assertTrue(mSuggestionsProvider.isValidWord("hello"));

    Mockito.verify(mFakeBuilder.mSpiedDictionary).isValidWord(Mockito.eq("hello"));
    Mockito.reset(mFakeBuilder.mSpiedDictionary);

    mSuggestionsProvider.close();

    Assert.assertFalse(mSuggestionsProvider.isValidWord("hello"));
    Mockito.verify(mFakeBuilder.mSpiedDictionary, Mockito.never())
        .isValidWord(Mockito.any(CharSequence.class));
  }

  private WordComposer wordFor(String word) {
    WordComposer wordComposer = new WordComposer();
    for (char c : word.toCharArray()) wordComposer.add(c, new int[] {c});

    return wordComposer;
  }

  @Test
  public void testCloseWillConvertAllDictionariesToEmptyDictionaries() {
    mSuggestionsProvider.setupSuggestionsForKeyboard(mFakeBuilders, mMockListener);
    TestRxSchedulers.drainAllTasks();
    mSuggestionsProvider.close();

    mSuggestionsProvider.getSuggestions(wordFor("hell"), mWordsCallback);
    Assert.assertEquals(0, mWordsCallback.wordsReceived.size());
  }

  @Test
  public void testDoesNotCrashIfCloseIsCalledBeforeLoadIsDone() throws Exception {
    mSuggestionsProvider.setupSuggestionsForKeyboard(mFakeBuilders, mMockListener);

    // created instance
    Mockito.verify(mFakeBuilder).createDictionary();
    // but was not loaded yet
    Mockito.verify(mFakeBuilder.mSpiedDictionary, Mockito.never()).loadDictionary();

    // closing
    mSuggestionsProvider.close();
    // close was not called
    Mockito.verify(mFakeBuilder.mSpiedDictionary, Mockito.never()).close();

    TestRxSchedulers.drainAllTasks();

    Mockito.verify(mFakeBuilder.mSpiedDictionary).close();
  }

  @Test
  public void testClearDictionariesBeforeClosingDictionaries() throws Exception {
    mSuggestionsProvider.setupSuggestionsForKeyboard(mFakeBuilders, mMockListener);
    TestRxSchedulers.drainAllTasks();
    Mockito.verify(mFakeBuilder).createDictionary();
    Mockito.verify(mFakeBuilder.mSpiedDictionary).loadDictionary();

    mSuggestionsProvider.getSuggestions(wordFor("hell"), mWordsCallback);
    Assert.assertNotEquals(0, mWordsCallback.wordsReceived.size());
    // closing
    mSuggestionsProvider.close();
    // close was not called
    Mockito.verify(mFakeBuilder.mSpiedDictionary, Mockito.never()).close();

    mWordsCallback.wordsReceived.clear();
    mSuggestionsProvider.getSuggestions(wordFor("hell"), mWordsCallback);
    Assert.assertEquals(0, mWordsCallback.wordsReceived.size());

    TestRxSchedulers.drainAllTasks();

    Mockito.verify(mFakeBuilder.mSpiedDictionary).close();
  }

  @Test
  public void testPassesWordsLoadedListenerToDictionaries() throws Exception {
    mSuggestionsProvider.setupSuggestionsForKeyboard(mFakeBuilders, mMockListener);

    TestRxSchedulers.drainAllTasks();

    Assert.assertNotNull(mFakeContactsDictionary);

    final InOrder inOrder = Mockito.inOrder(mMockListener);

    inOrder.verify(mMockListener).onDictionaryLoadingStarted(mFakeBuilder.mSpiedDictionary);
    inOrder.verify(mMockListener).onDictionaryLoadingStarted(mTestUserDictionary);
    inOrder.verify(mMockListener).onDictionaryLoadingStarted(mFakeContactsDictionary);
    inOrder.verify(mMockListener).onDictionaryLoadingDone(mFakeBuilder.mSpiedDictionary);
    inOrder.verify(mMockListener).onDictionaryLoadingDone(mTestUserDictionary);
    inOrder.verify(mMockListener).onDictionaryLoadingDone(mFakeContactsDictionary);

    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void testPassesWordsLoadedListenerToDictionariesEvenIfSameBuilders() throws Exception {
    mSuggestionsProvider.setupSuggestionsForKeyboard(mFakeBuilders, mMockListener);
    TestRxSchedulers.drainAllTasks();

    DictionaryBackgroundLoader.Listener listener2 =
        Mockito.mock(DictionaryBackgroundLoader.Listener.class);
    mSuggestionsProvider.setupSuggestionsForKeyboard(mFakeBuilders, listener2);
    TestRxSchedulers.drainAllTasks();

    final InOrder inOrder =
        Mockito.inOrder(
            mMockListener,
            listener2,
            mFakeBuilder.mSpiedDictionary,
            mTestUserDictionary,
            mFakeContactsDictionary);

    inOrder.verify(mMockListener).onDictionaryLoadingStarted(mFakeBuilder.mSpiedDictionary);
    inOrder.verify(mMockListener).onDictionaryLoadingStarted(mTestUserDictionary);
    inOrder.verify(mMockListener).onDictionaryLoadingStarted(mFakeContactsDictionary);
    inOrder.verify(mFakeBuilder.mSpiedDictionary).loadDictionary();
    inOrder.verify(mMockListener).onDictionaryLoadingDone(mFakeBuilder.mSpiedDictionary);
    inOrder.verify(mTestUserDictionary).loadDictionary();
    inOrder.verify(mMockListener).onDictionaryLoadingDone(mTestUserDictionary);
    inOrder.verify(mFakeContactsDictionary).loadDictionary();
    inOrder.verify(mMockListener).onDictionaryLoadingDone(mFakeContactsDictionary);

    inOrder.verify(listener2).onDictionaryLoadingStarted(mFakeBuilder.mSpiedDictionary);
    inOrder.verify(listener2).onDictionaryLoadingStarted(mTestUserDictionary);
    inOrder.verify(listener2).onDictionaryLoadingStarted(mFakeContactsDictionary);
    inOrder.verify(listener2).onDictionaryLoadingDone(mFakeBuilder.mSpiedDictionary);
    inOrder.verify(listener2).onDictionaryLoadingDone(mTestUserDictionary);
    inOrder.verify(listener2).onDictionaryLoadingDone(mFakeContactsDictionary);

    inOrder.verifyNoMoreInteractions();
  }

  private static class WordsHolder implements Dictionary.WordCallback {
    public final List<String> wordsReceived = new ArrayList<>();

    @Override
    public boolean addWord(
        char[] word, int wordOffset, int wordLength, int frequency, Dictionary from) {
      wordsReceived.add(new String(word, wordOffset, wordLength));
      return true;
    }
  }

  private static class FakeBuilder extends DictionaryAddOnAndBuilder {

    public static final String FAKE_BUILDER_ID = "673957f5-835d-4a99-893a-e68950b0a2ba";
    private AutoText mSpiedAutoText;
    private Dictionary mSpiedDictionary;

    public FakeBuilder(String... wordsToLoad) {
      super(
          getApplicationContext(),
          getApplicationContext(),
          getApplicationContext().getResources().getInteger(anysoftkeyboard_api_version_code),
          FAKE_BUILDER_ID,
          "fake",
          "fake dictionary",
          false,
          1,
          "en",
          R.array.english_words_dict_array,
          english_autotext,
          english_initial_suggestions);
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
      super("fake_dict", getApplicationContext());
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
    protected void deleteWordFromStorage(String word) {}

    @Override
    protected void addWordToStorage(String word, int frequency) {}

    @Override
    protected void closeStorage() {}
  }
}
