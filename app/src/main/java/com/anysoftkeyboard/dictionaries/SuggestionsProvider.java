package com.anysoftkeyboard.dictionaries;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.dictionaries.content.ContactsDictionary;
import com.anysoftkeyboard.dictionaries.sqlite.AbbreviationsDictionary;
import com.anysoftkeyboard.dictionaries.sqlite.AutoDictionary;
import com.anysoftkeyboard.nextword.NextWordSuggestions;
import com.anysoftkeyboard.prefs.RxSharedPrefs;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.BuildConfig;
import com.menny.android.anysoftkeyboard.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import io.reactivex.disposables.CompositeDisposable;

public class SuggestionsProvider {

    private static final String TAG = "SuggestionsProvider";

    private static final EditableDictionary NullDictionary = new EditableDictionary("NULL") {
        @Override
        public boolean addWord(String word, int frequency) {
            return false;
        }

        @Override
        public void deleteWord(String word) {
        }

        @Override
        public void getWords(KeyCodesProvider composer, WordCallback callback) {
        }

        @Override
        public boolean isValidWord(CharSequence word) {
            return false;
        }

        @Override
        protected void closeAllResources() {
        }

        @Override
        protected void loadAllResources() {
        }
    };

    private static final NextWordSuggestions NULL_NEXT_WORD_SUGGESTIONS = new NextWordSuggestions() {
        @Override
        @NonNull
        public Iterable<String> getNextWords(@NonNull CharSequence currentWord, int maxResults, int minWordUsage) {
            return Collections.emptyList();
        }

        @Override
        public void notifyNextTypedWord(@NonNull CharSequence currentWord) {
        }

        @Override
        public void resetSentence() {
        }
    };

    @NonNull
    private final Context mContext;
    @NonNull
    private final List<String> mInitialSuggestionsList = new ArrayList<>();

    @NonNull
    private CompositeDisposable mDictionaryDisposables = new CompositeDisposable();
    @NonNull
    private final List<Dictionary> mMainDictionary = new ArrayList<>();
    @NonNull
    private final List<EditableDictionary> mUserDictionary = new ArrayList<>();
    @NonNull
    private final List<NextWordSuggestions> mUserNextWordDictionary = new ArrayList<>();
    private boolean mQuickFixesEnabled;
    @NonNull
    private final List<AutoText> mQuickFixesAutoText = new ArrayList<>();

    private boolean mNextWordEnabled;
    private boolean mAlsoSuggestNextPunctuations;
    private int mMaxNextWordSuggestionsCount;
    private int mMinWordUsage;

    @NonNull
    private EditableDictionary mAutoDictionary = NullDictionary;
    private boolean mContactsDictionaryEnabled;
    @NonNull
    private Dictionary mContactsDictionary = NullDictionary;

    private boolean mIncognitoMode;

    @NonNull
    private NextWordSuggestions mContactsNextWordDictionary = NULL_NEXT_WORD_SUGGESTIONS;

    private final DictionaryBackgroundLoader.Listener mContactsDictionaryListener = new DictionaryBackgroundLoader.Listener() {
        @Override
        public void onDictionaryLoadingDone(Dictionary dictionary) {
        }

        @Override
        public void onDictionaryLoadingFailed(Dictionary dictionary, Throwable exception) {
            if (dictionary == mContactsDictionary) {
                mContactsDictionary = NullDictionary;
                mContactsNextWordDictionary = NULL_NEXT_WORD_SUGGESTIONS;
            }
        }
    };
    @NonNull
    private final List<Dictionary> mAbbreviationDictionary = new ArrayList<>();
    private final CompositeDisposable mPrefsDisposables = new CompositeDisposable();

    public SuggestionsProvider(@NonNull Context context) {
        mContext = context.getApplicationContext();

        final RxSharedPrefs rxSharedPrefs = AnyApplication.prefs(context);
        mPrefsDisposables.add(rxSharedPrefs.getBoolean(R.string.settings_key_quick_fix, R.bool.settings_default_quick_fix)
                .asObservable().subscribe(value -> mQuickFixesEnabled = value));
        mPrefsDisposables.add(rxSharedPrefs.getBoolean(R.string.settings_key_use_contacts_dictionary, R.bool.settings_default_contacts_dictionary)
                .asObservable().subscribe(value -> {
                    mContactsDictionaryEnabled = value;
                    if (!mContactsDictionaryEnabled) {
                        mContactsDictionary.close();
                        mContactsDictionary = NullDictionary;
                    }
                }));
        mPrefsDisposables.add(rxSharedPrefs.getString(R.string.settings_key_next_word_suggestion_aggressiveness, R.string.settings_default_next_word_suggestion_aggressiveness)
                .asObservable().subscribe(aggressiveness -> {
                    switch (aggressiveness) {
                        case "minimal_aggressiveness":
                            mMaxNextWordSuggestionsCount = 3;
                            mMinWordUsage = 5;
                            break;
                        case "medium_aggressiveness":
                            mMaxNextWordSuggestionsCount = 5;
                            mMinWordUsage = 3;
                            break;
                        case "maximum_aggressiveness":
                            mMaxNextWordSuggestionsCount = 8;
                            mMinWordUsage = 1;
                            break;
                        default:
                            mMaxNextWordSuggestionsCount = 3;
                            mMinWordUsage = 5;
                            break;
                    }
                }));
        mPrefsDisposables.add(rxSharedPrefs.getString(R.string.settings_key_next_word_dictionary_type, R.string.settings_default_next_words_dictionary_type)
                .asObservable().subscribe(type -> {
                    switch (type) {
                        case "off":
                            mNextWordEnabled = false;
                            mAlsoSuggestNextPunctuations = false;
                            break;
                        case "word":
                            mNextWordEnabled = true;
                            mAlsoSuggestNextPunctuations = false;
                            break;
                        case "words_punctuations":
                            mNextWordEnabled = true;
                            mAlsoSuggestNextPunctuations = true;
                            break;
                        default:
                            mNextWordEnabled = true;
                            mAlsoSuggestNextPunctuations = false;
                            break;
                    }
                }));
    }

    private static boolean allDictionariesIsValid(List<? extends Dictionary> dictionaries, CharSequence word) {
        for (Dictionary dictionary : dictionaries) {
            if (dictionary.isValidWord(word)) return true;
        }

        return false;
    }

    private static void allDictionariesGetWords(List<? extends Dictionary> dictionaries, WordComposer wordComposer, Dictionary.WordCallback wordCallback) {
        for (Dictionary dictionary : dictionaries) {
            dictionary.getWords(wordComposer, wordCallback);
        }
    }

    public void setupSuggestionsForKeyboard(@NonNull List<DictionaryAddOnAndBuilder> dictionaryBuilders) {
        if (BuildConfig.TESTING_BUILD) {
            Logger.d(TAG, "setupSuggestionsFor %d dictionaries", dictionaryBuilders.size());
            for (DictionaryAddOnAndBuilder dictionaryBuilder : dictionaryBuilders) {
                Logger.d(TAG, " * dictionary %s (%s)", dictionaryBuilder.getId(), dictionaryBuilder.getLanguage());
            }
        }

        close();
        final CompositeDisposable disposablesHolder = mDictionaryDisposables;

        for (DictionaryAddOnAndBuilder dictionaryBuilder : dictionaryBuilders) {
            try {
                Logger.d(TAG, " Creating dictionary %s (%s)...", dictionaryBuilder.getId(), dictionaryBuilder.getLanguage());
                final Dictionary dictionary = dictionaryBuilder.createDictionary();
                mMainDictionary.add(dictionary);
                Logger.d(TAG, " Loading dictionary %s (%s)...", dictionaryBuilder.getId(), dictionaryBuilder.getLanguage());
                disposablesHolder.add(DictionaryBackgroundLoader.loadDictionaryInBackground(dictionary));
            } catch (Exception e) {
                Logger.e(TAG, e, "Failed to create dictionary %s", dictionaryBuilder.getId());
            }
            final UserDictionary userDictionary = createUserDictionaryForLocale(dictionaryBuilder.getLanguage());
            mUserDictionary.add(userDictionary);
            Logger.d(TAG, " Loading user dictionary for %s...", dictionaryBuilder.getLanguage());
            disposablesHolder.add(DictionaryBackgroundLoader.loadDictionaryInBackground(userDictionary));
            mUserNextWordDictionary.add(userDictionary.getUserNextWordGetter());

            if (mQuickFixesEnabled) {
                final AutoText autoText = dictionaryBuilder.createAutoText();
                if (autoText != null) {
                    mQuickFixesAutoText.add(autoText);
                }
                final AbbreviationsDictionary abbreviationsDictionary = new AbbreviationsDictionary(mContext, dictionaryBuilder.getLanguage());
                mAbbreviationDictionary.add(abbreviationsDictionary);
                Logger.d(TAG, " Loading abbr dictionary for %s...", dictionaryBuilder.getLanguage());
                disposablesHolder.add(DictionaryBackgroundLoader.loadDictionaryInBackground(abbreviationsDictionary));
            }

            mInitialSuggestionsList.addAll(dictionaryBuilder.createInitialSuggestions());

            //only one auto-dictionary. There is no way to know to which language the typed word belongs.
            mAutoDictionary = new AutoDictionary(mContext, dictionaryBuilder.getLanguage());
            Logger.d(TAG, " Loading auto dictionary for %s...", dictionaryBuilder.getLanguage());
            disposablesHolder.add(DictionaryBackgroundLoader.loadDictionaryInBackground(mAutoDictionary));
        }

        if (mContactsDictionaryEnabled) {
            if (mContactsDictionary == NullDictionary) {
                mContactsDictionary = new ContactsDictionary(mContext);
                mContactsNextWordDictionary = (ContactsDictionary) mContactsDictionary;
                disposablesHolder.add(DictionaryBackgroundLoader.loadDictionaryInBackground(mContactsDictionaryListener, mContactsDictionary));
            }
        }
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

        return allDictionariesIsValid(mMainDictionary, word) || allDictionariesIsValid(mUserDictionary, word) || mContactsDictionary.isValidWord(word);
    }

    public void setIncognitoMode(boolean incognitoMode) {
        mIncognitoMode = incognitoMode;
    }

    public boolean isIncognitoMode() {
        return mIncognitoMode;
    }

    public void close() {
        Logger.d(TAG, "closeDictionaries");
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

    public void getSuggestions(WordComposer wordComposer, Dictionary.WordCallback wordCallback) {
        mContactsDictionary.getWords(wordComposer, wordCallback);
        allDictionariesGetWords(mUserDictionary, wordComposer, wordCallback);
        allDictionariesGetWords(mMainDictionary, wordComposer, wordCallback);
    }

    public void getAbbreviations(WordComposer wordComposer, Dictionary.WordCallback wordCallback) {
        allDictionariesGetWords(mAbbreviationDictionary, wordComposer, wordCallback);
    }

    public CharSequence lookupQuickFix(String word) {
        for (AutoText autoText : mQuickFixesAutoText) {
            final String fix = autoText.lookup(word);
            if (fix != null) return fix;
        }

        return null;
    }

    public void getNextWords(String currentWord, Collection<CharSequence> suggestionsHolder, int maxSuggestions) {
        if (!mNextWordEnabled) return;

        allDictionariesGetNextWord(mUserNextWordDictionary, currentWord, suggestionsHolder, maxSuggestions);
        maxSuggestions = maxSuggestions - suggestionsHolder.size();
        if (maxSuggestions == 0) return;

        for (String nextWordSuggestion : mContactsNextWordDictionary.getNextWords(currentWord, mMaxNextWordSuggestionsCount, mMinWordUsage)) {
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

    private void allDictionariesGetNextWord(List<NextWordSuggestions> nextWordDictionaries, String currentWord, Collection<CharSequence> suggestionsHolder, int maxSuggestions) {
        for (NextWordSuggestions nextWordDictionary : nextWordDictionaries) {

            if (!mIncognitoMode) nextWordDictionary.notifyNextTypedWord(currentWord);

            for (String nextWordSuggestion : nextWordDictionary.getNextWords(currentWord, mMaxNextWordSuggestionsCount, mMinWordUsage)) {
                suggestionsHolder.add(nextWordSuggestion);
                maxSuggestions--;
                if (maxSuggestions == 0) return;
            }
        }
    }

    public boolean tryToLearnNewWord(String newWord, int frequencyDelta) {
        if (mIncognitoMode || !mNextWordEnabled) return false;

        if (!isValidWord(newWord)) {
            return mAutoDictionary.addWord(newWord, frequencyDelta);
        }

        return false;
    }
}
