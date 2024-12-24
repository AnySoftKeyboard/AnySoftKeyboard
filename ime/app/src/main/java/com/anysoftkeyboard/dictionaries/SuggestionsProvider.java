package com.anysoftkeyboard.dictionaries;

import static com.anysoftkeyboard.dictionaries.DictionaryBackgroundLoader.NO_OP_LISTENER;

import android.content.Context;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.dictionaries.content.ContactsDictionary;
import com.anysoftkeyboard.dictionaries.sqlite.AbbreviationsDictionary;
import com.anysoftkeyboard.dictionaries.sqlite.AutoDictionary;
import com.anysoftkeyboard.nextword.NextWordSuggestions;
import com.anysoftkeyboard.prefs.RxSharedPrefs;
import com.anysoftkeyboard.rx.GenericOnError;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.BuildConfig;
import com.menny.android.anysoftkeyboard.R;
import io.reactivex.disposables.CompositeDisposable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class SuggestionsProvider {

  private static final String TAG = "SuggestionsProvider";

  private static final EditableDictionary NullDictionary =
      new EditableDictionary("NULL") {
        @Override
        public boolean addWord(String word, int frequency) {
          return false;
        }

        @Override
        public void deleteWord(String word) {}

        @Override
        public void getLoadedWords(@NonNull GetWordsCallback callback) {
          throw new UnsupportedOperationException();
        }

        @Override
        public void getSuggestions(KeyCodesProvider composer, WordCallback callback) {}

        @Override
        public boolean isValidWord(CharSequence word) {
          return false;
        }

        @Override
        protected void closeAllResources() {}

        @Override
        protected void loadAllResources() {}
      };

  private static final NextWordSuggestions NULL_NEXT_WORD_SUGGESTIONS =
      new NextWordSuggestions() {
        @Override
        @NonNull
        public Iterable<String> getNextWords(
            @NonNull String currentWord, int maxResults, int minWordUsage) {
          return Collections.emptyList();
        }

        @Override
        public void notifyNextTypedWord(@NonNull String currentWord) {}

        @Override
        public void resetSentence() {}
      };

  @NonNull private final Context mContext;
  @NonNull private final List<String> mInitialSuggestionsList = new ArrayList<>();

  @NonNull private CompositeDisposable mDictionaryDisposables = new CompositeDisposable();

  private int mCurrentSetupHashCode;

  @NonNull private final List<Dictionary> mMainDictionary = new ArrayList<>();
  @NonNull private final List<EditableDictionary> mUserDictionary = new ArrayList<>();
  @NonNull private final List<NextWordSuggestions> mUserNextWordDictionary = new ArrayList<>();
  private boolean mQuickFixesEnabled;
  // if true secondary languages will not have autotext on. For primary language is intended the
  // current keyboard layout language
  private boolean mQuickFixesSecondDisabled;
  @NonNull private final List<AutoText> mQuickFixesAutoText = new ArrayList<>();

  private boolean mNextWordEnabled;
  private boolean mAlsoSuggestNextPunctuations;
  private int mMaxNextWordSuggestionsCount;
  private int mMinWordUsage;

  @NonNull private EditableDictionary mAutoDictionary = NullDictionary;
  private boolean mContactsDictionaryEnabled;
  private boolean mUserDictionaryEnabled;
  @NonNull private Dictionary mContactsDictionary = NullDictionary;

  private boolean mIncognitoMode;

  @NonNull private NextWordSuggestions mContactsNextWordDictionary = NULL_NEXT_WORD_SUGGESTIONS;

  private final ContactsDictionaryLoaderListener mContactsDictionaryListener =
      new ContactsDictionaryLoaderListener();

  private class ContactsDictionaryLoaderListener implements DictionaryBackgroundLoader.Listener {

    @NonNull private DictionaryBackgroundLoader.Listener mDelegate = NO_OP_LISTENER;

    @Override
    public void onDictionaryLoadingStarted(Dictionary dictionary) {
      mDelegate.onDictionaryLoadingStarted(dictionary);
    }

    @Override
    public void onDictionaryLoadingDone(Dictionary dictionary) {
      mDelegate.onDictionaryLoadingDone(dictionary);
    }

    @Override
    public void onDictionaryLoadingFailed(Dictionary dictionary, Throwable exception) {
      mDelegate.onDictionaryLoadingFailed(dictionary, exception);
      if (dictionary == mContactsDictionary) {
        mContactsDictionary = NullDictionary;
        mContactsNextWordDictionary = NULL_NEXT_WORD_SUGGESTIONS;
      }
    }
  }

  @NonNull private final List<Dictionary> mAbbreviationDictionary = new ArrayList<>();
  private final CompositeDisposable mPrefsDisposables = new CompositeDisposable();

  public SuggestionsProvider(@NonNull Context context) {
    mContext = context.getApplicationContext();

    final RxSharedPrefs rxSharedPrefs = AnyApplication.prefs(mContext);
    mPrefsDisposables.add(
        rxSharedPrefs
            .getBoolean(R.string.settings_key_quick_fix, R.bool.settings_default_quick_fix)
            .asObservable()
            .subscribe(
                value -> {
                  mCurrentSetupHashCode = 0;
                  mQuickFixesEnabled = value;
                },
                GenericOnError.onError("settings_key_quick_fix")));
    mPrefsDisposables.add(
        rxSharedPrefs
            .getBoolean(
                R.string.settings_key_quick_fix_second_disabled,
                R.bool.settings_default_key_quick_fix_second_disabled)
            .asObservable()
            .subscribe(
                value -> {
                  mCurrentSetupHashCode = 0;
                  mQuickFixesSecondDisabled = value;
                },
                GenericOnError.onError("settings_key_quick_fix_second_disable")));
    mPrefsDisposables.add(
        rxSharedPrefs
            .getBoolean(
                R.string.settings_key_use_contacts_dictionary,
                R.bool.settings_default_contacts_dictionary)
            .asObservable()
            .subscribe(
                value -> {
                  mCurrentSetupHashCode = 0;
                  mContactsDictionaryEnabled = value;
                  if (!mContactsDictionaryEnabled) {
                    mContactsDictionary.close();
                    mContactsDictionary = NullDictionary;
                  }
                },
                GenericOnError.onError("settings_key_use_contacts_dictionary")));
    mPrefsDisposables.add(
        rxSharedPrefs
            .getBoolean(
                R.string.settings_key_use_user_dictionary, R.bool.settings_default_user_dictionary)
            .asObservable()
            .subscribe(
                value -> {
                  mCurrentSetupHashCode = 0;
                  mUserDictionaryEnabled = value;
                },
                GenericOnError.onError("settings_key_use_user_dictionary")));
    mPrefsDisposables.add(
        rxSharedPrefs
            .getString(
                R.string.settings_key_next_word_suggestion_aggressiveness,
                R.string.settings_default_next_word_suggestion_aggressiveness)
            .asObservable()
            .subscribe(
                aggressiveness -> {
                  switch (aggressiveness) {
                    case "medium_aggressiveness":
                      mMaxNextWordSuggestionsCount = 5;
                      mMinWordUsage = 3;
                      break;
                    case "maximum_aggressiveness":
                      mMaxNextWordSuggestionsCount = 8;
                      mMinWordUsage = 1;
                      break;
                    case "minimal_aggressiveness":
                    default:
                      mMaxNextWordSuggestionsCount = 3;
                      mMinWordUsage = 5;
                      break;
                  }
                },
                GenericOnError.onError("settings_key_next_word_suggestion_aggressiveness")));
    mPrefsDisposables.add(
        rxSharedPrefs
            .getString(
                R.string.settings_key_next_word_dictionary_type,
                R.string.settings_default_next_words_dictionary_type)
            .asObservable()
            .subscribe(
                type -> {
                  switch (type) {
                    case "off":
                      mNextWordEnabled = false;
                      mAlsoSuggestNextPunctuations = false;
                      break;
                    case "words_punctuations":
                      mNextWordEnabled = true;
                      mAlsoSuggestNextPunctuations = true;
                      break;
                    case "word":
                    default:
                      mNextWordEnabled = true;
                      mAlsoSuggestNextPunctuations = false;
                      break;
                  }
                },
                GenericOnError.onError("settings_key_next_word_dictionary_type")));
  }

  private static boolean allDictionariesIsValid(
      List<? extends Dictionary> dictionaries, CharSequence word) {
    for (Dictionary dictionary : dictionaries) {
      if (dictionary.isValidWord(word)) return true;
    }

    return false;
  }

  private static void allDictionariesGetWords(
      List<? extends Dictionary> dictionaries,
      KeyCodesProvider wordComposer,
      Dictionary.WordCallback wordCallback) {
    for (Dictionary dictionary : dictionaries) {
      dictionary.getSuggestions(wordComposer, wordCallback);
    }
  }

  public void setupSuggestionsForKeyboard(
      @NonNull List<DictionaryAddOnAndBuilder> dictionaryBuilders,
      @NonNull DictionaryBackgroundLoader.Listener cb) {
    if (BuildConfig.TESTING_BUILD) {
      Logger.d(TAG, "setupSuggestionsFor %d dictionaries", dictionaryBuilders.size());
      for (DictionaryAddOnAndBuilder dictionaryBuilder : dictionaryBuilders) {
        Logger.d(
            TAG,
            " * dictionary %s (%s)",
            dictionaryBuilder.getId(),
            dictionaryBuilder.getLanguage());
      }
    }
    final int newSetupHashCode = calculateHashCodeForBuilders(dictionaryBuilders);
    if (newSetupHashCode == mCurrentSetupHashCode) {
      // no need to load, since we have all the same dictionaries,
      // but, we do need to notify the dictionary loaded listeners.
      final List<Dictionary> dictionariesToSimulateLoad =
          new ArrayList<>(mMainDictionary.size() + mUserDictionary.size() + 1 /*for contacts*/);
      dictionariesToSimulateLoad.addAll(mMainDictionary);
      dictionariesToSimulateLoad.addAll(mUserDictionary);
      if (mContactsDictionaryEnabled) dictionariesToSimulateLoad.add(mContactsDictionary);

      for (Dictionary dictionary : dictionariesToSimulateLoad) {
        cb.onDictionaryLoadingStarted(dictionary);
      }
      for (Dictionary dictionary : dictionariesToSimulateLoad) {
        cb.onDictionaryLoadingDone(dictionary);
      }
      return;
    }

    close();

    mCurrentSetupHashCode = newSetupHashCode;
    final CompositeDisposable disposablesHolder = mDictionaryDisposables;

    for (int i = 0; i < dictionaryBuilders.size(); i++) {
      DictionaryAddOnAndBuilder dictionaryBuilder = dictionaryBuilders.get(i);
      try {
        Logger.d(
            TAG,
            " Creating dictionary %s (%s)...",
            dictionaryBuilder.getId(),
            dictionaryBuilder.getLanguage());
        final Dictionary dictionary = dictionaryBuilder.createDictionary();
        mMainDictionary.add(dictionary);
        Logger.d(
            TAG,
            " Loading dictionary %s (%s)...",
            dictionaryBuilder.getId(),
            dictionaryBuilder.getLanguage());
        disposablesHolder.add(
            DictionaryBackgroundLoader.loadDictionaryInBackground(cb, dictionary));
      } catch (Exception e) {
        Logger.e(TAG, e, "Failed to create dictionary %s", dictionaryBuilder.getId());
      }

      if (mUserDictionaryEnabled) {
        final UserDictionary userDictionary =
            createUserDictionaryForLocale(dictionaryBuilder.getLanguage());
        mUserDictionary.add(userDictionary);
        Logger.d(TAG, " Loading user dictionary for %s...", dictionaryBuilder.getLanguage());
        disposablesHolder.add(
            DictionaryBackgroundLoader.loadDictionaryInBackground(cb, userDictionary));
        mUserNextWordDictionary.add(userDictionary.getUserNextWordGetter());
      } else {
        Logger.d(TAG, " User does not want user dictionary, skipping...");
      }
      // if mQuickFixesEnabled and mQuickFixesSecondDisabled are true
      // it  activates autotext only to the current keyboard layout language
      if (mQuickFixesEnabled && (i == 0 || !mQuickFixesSecondDisabled)) {
        final AutoText autoText = dictionaryBuilder.createAutoText();
        if (autoText != null) {
          mQuickFixesAutoText.add(autoText);
        }
        final AbbreviationsDictionary abbreviationsDictionary =
            new AbbreviationsDictionary(mContext, dictionaryBuilder.getLanguage());
        mAbbreviationDictionary.add(abbreviationsDictionary);
        Logger.d(TAG, " Loading abbr dictionary for %s...", dictionaryBuilder.getLanguage());
        disposablesHolder.add(
            DictionaryBackgroundLoader.loadDictionaryInBackground(abbreviationsDictionary));
      }

      mInitialSuggestionsList.addAll(dictionaryBuilder.createInitialSuggestions());

      // only one auto-dictionary. There is no way to know to which language the typed word
      // belongs.
      mAutoDictionary = new AutoDictionary(mContext, dictionaryBuilder.getLanguage());
      Logger.d(TAG, " Loading auto dictionary for %s...", dictionaryBuilder.getLanguage());
      disposablesHolder.add(DictionaryBackgroundLoader.loadDictionaryInBackground(mAutoDictionary));
    }

    if (mContactsDictionaryEnabled && mContactsDictionary == NullDictionary) {
      mContactsDictionaryListener.mDelegate = cb;
      final ContactsDictionary realContactsDictionary = createRealContactsDictionary();
      mContactsDictionary = realContactsDictionary;
      mContactsNextWordDictionary = realContactsDictionary;
      disposablesHolder.add(
          DictionaryBackgroundLoader.loadDictionaryInBackground(
              mContactsDictionaryListener, mContactsDictionary));
    }
  }

  @NonNull
  @VisibleForTesting
  protected ContactsDictionary createRealContactsDictionary() {
    return new ContactsDictionary(mContext);
  }

  private static int calculateHashCodeForBuilders(
      List<DictionaryAddOnAndBuilder> dictionaryBuilders) {
    return Arrays.hashCode(dictionaryBuilders.toArray());
  }

  @NonNull
  protected UserDictionary createUserDictionaryForLocale(@NonNull String locale) {
    return new UserDictionary(mContext, locale);
  }

  public void removeWordFromUserDictionary(String word) {
    for (EditableDictionary dictionary : mUserDictionary) {
      dictionary.deleteWord(word);
    }
  }

  public boolean addWordToUserDictionary(String word) {
    if (mIncognitoMode) return false;

    if (mUserDictionary.size() > 0) {
      return mUserDictionary.get(0).addWord(word, 128);
    } else {
      return false;
    }
  }

  public boolean isValidWord(CharSequence word) {
    if (TextUtils.isEmpty(word)) {
      return false;
    }

    return allDictionariesIsValid(mMainDictionary, word)
        || allDictionariesIsValid(mUserDictionary, word)
        || mContactsDictionary.isValidWord(word);
  }

  public void setIncognitoMode(boolean incognitoMode) {
    mIncognitoMode = incognitoMode;
  }

  public boolean isIncognitoMode() {
    return mIncognitoMode;
  }

  public void close() {
    Logger.d(TAG, "closeDictionaries");
    mCurrentSetupHashCode = 0;
    mMainDictionary.clear();
    mAbbreviationDictionary.clear();
    mUserDictionary.clear();
    mQuickFixesAutoText.clear();
    mUserNextWordDictionary.clear();
    mInitialSuggestionsList.clear();
    resetNextWordSentence();
    mContactsNextWordDictionary = NULL_NEXT_WORD_SUGGESTIONS;
    mAutoDictionary = NullDictionary;
    mContactsDictionary = NullDictionary;

    mDictionaryDisposables.dispose();
    mDictionaryDisposables = new CompositeDisposable();
  }

  public void destroy() {
    close();
    mPrefsDisposables.dispose();
  }

  public void resetNextWordSentence() {
    for (NextWordSuggestions nextWordSuggestions : mUserNextWordDictionary) {
      nextWordSuggestions.resetSentence();
    }
    mContactsNextWordDictionary.resetSentence();
  }

  public void getSuggestions(KeyCodesProvider wordComposer, Dictionary.WordCallback wordCallback) {
    mContactsDictionary.getSuggestions(wordComposer, wordCallback);
    allDictionariesGetWords(mUserDictionary, wordComposer, wordCallback);
    allDictionariesGetWords(mMainDictionary, wordComposer, wordCallback);
  }

  public void getAbbreviations(
      KeyCodesProvider wordComposer, Dictionary.WordCallback wordCallback) {
    allDictionariesGetWords(mAbbreviationDictionary, wordComposer, wordCallback);
  }

  public void getAutoText(KeyCodesProvider wordComposer, Dictionary.WordCallback wordCallback) {
    final CharSequence word = wordComposer.getTypedWord();
    for (AutoText autoText : mQuickFixesAutoText) {
      final String fix = autoText.lookup(word);
      if (fix != null) wordCallback.addWord(fix.toCharArray(), 0, fix.length(), 255, null);
    }
  }

  public void getNextWords(
      String currentWord, Collection<CharSequence> suggestionsHolder, int maxSuggestions) {
    if (!mNextWordEnabled) return;

    allDictionariesGetNextWord(
        mUserNextWordDictionary, currentWord, suggestionsHolder, maxSuggestions);
    maxSuggestions = maxSuggestions - suggestionsHolder.size();
    if (maxSuggestions == 0) return;

    for (String nextWordSuggestion :
        mContactsNextWordDictionary.getNextWords(
            currentWord, mMaxNextWordSuggestionsCount, mMinWordUsage)) {
      suggestionsHolder.add(nextWordSuggestion);
      maxSuggestions--;
      if (maxSuggestions == 0) return;
    }

    if (mAlsoSuggestNextPunctuations) {
      for (String evenMoreSuggestions : mInitialSuggestionsList) {
        suggestionsHolder.add(evenMoreSuggestions);
        maxSuggestions--;
        if (maxSuggestions == 0) return;
      }
    }
  }

  private void allDictionariesGetNextWord(
      List<NextWordSuggestions> nextWordDictionaries,
      String currentWord,
      Collection<CharSequence> suggestionsHolder,
      int maxSuggestions) {
    for (NextWordSuggestions nextWordDictionary : nextWordDictionaries) {

      if (!mIncognitoMode) nextWordDictionary.notifyNextTypedWord(currentWord);

      for (String nextWordSuggestion :
          nextWordDictionary.getNextWords(
              currentWord, mMaxNextWordSuggestionsCount, mMinWordUsage)) {
        suggestionsHolder.add(nextWordSuggestion);
        maxSuggestions--;
        if (maxSuggestions == 0) return;
      }
    }
  }

  public boolean tryToLearnNewWord(CharSequence newWord, int frequencyDelta) {
    if (mIncognitoMode || !mNextWordEnabled) return false;

    if (!isValidWord(newWord)) {
      return mAutoDictionary.addWord(newWord.toString(), frequencyDelta);
    }

    return false;
  }
}
