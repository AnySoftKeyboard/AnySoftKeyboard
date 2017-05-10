package com.anysoftkeyboard.dictionaries;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.anysoftkeyboard.base.dictionaries.Dictionary;
import com.anysoftkeyboard.base.dictionaries.EditableDictionary;
import com.anysoftkeyboard.base.dictionaries.KeyCodesProvider;
import com.anysoftkeyboard.base.dictionaries.WordComposer;
import com.anysoftkeyboard.dictionaries.content.ContactsDictionary;
import com.anysoftkeyboard.dictionaries.sqlite.AbbreviationsDictionary;
import com.anysoftkeyboard.dictionaries.sqlite.AutoDictionary;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.nextword.NextWordGetter;
import com.anysoftkeyboard.nextword.Utils;
import com.anysoftkeyboard.utils.Logger;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
    private static final AutoText NullAutoText = new AutoText() {
        @Override
        public String lookup(CharSequence word) {
            return null;
        }
    };
    private static final NextWordGetter NullNextWordGetter = new NextWordGetter() {
        @Override
        public Iterable<String> getNextWords(CharSequence currentWord, int maxResults, int minWordUsage) {
            return Collections.emptyList();
        }

        @Override
        public void resetSentence() {
        }
    };

    @NonNull
    private final Context mContext;
    private final ExternalDictionaryFactory mExternalDictionaryFactory;
    @NonNull
    private final List<String> mInitialSuggestionsList = new ArrayList<>();
    private final String mQuickFixesPrefId;
    private final String mContactsDictionaryPrefId;
    private final boolean mContactsDictionaryEnabledDefaultValue;
    private int mMinWordUsage;
    @NonNull
    private Dictionary mMainDictionary = NullDictionary;
    private boolean mQuickFixesEnabled;
    @NonNull
    private AutoText mQuickFixesAutoText = NullAutoText;
    @NonNull
    private EditableDictionary mUserDictionary = NullDictionary;

    @Utils.NextWordsSuggestionType
    private String mNextWordSuggestionType = Utils.NEXT_WORD_SUGGESTION_WORDS;
    private int mMaxNextWordSuggestionsCount;
    @NonNull
    private NextWordGetter mUserNextWordDictionary = NullNextWordGetter;
    @NonNull
    private EditableDictionary mAutoDictionary = NullDictionary;
    private boolean mContactsDictionaryEnabled;
    @NonNull
    private Dictionary mContactsDictionary = NullDictionary;
    private final SharedPreferences.OnSharedPreferenceChangeListener mPrefsChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            final Resources resources = mContext.getResources();

            mQuickFixesEnabled = sharedPreferences.getBoolean(mQuickFixesPrefId, true);
            mContactsDictionaryEnabled = sharedPreferences.getBoolean(mContactsDictionaryPrefId, mContactsDictionaryEnabledDefaultValue);
            if (!mContactsDictionaryEnabled) {
                mContactsDictionary.close();
                mContactsDictionary = NullDictionary;
            }
            mMinWordUsage = Utils.getNextWordSuggestionMinUsageFromPrefs(resources, sharedPreferences);
            mNextWordSuggestionType = Utils.getNextWordSuggestionTypeFromPrefs(resources, sharedPreferences);
            mMaxNextWordSuggestionsCount = Utils.getNextWordSuggestionCountFromPrefs(resources, sharedPreferences);
        }
    };
    private final DictionaryASyncLoader.Listener mContactsDictionaryListener = new DictionaryASyncLoader.Listener() {
        @Override
        public void onDictionaryLoadingDone(Dictionary dictionary) {}

        @Override
        public void onDictionaryLoadingFailed(Dictionary dictionary, Exception exception) {
            if (dictionary == mContactsDictionary) {
                mContactsDictionary = NullDictionary;
                mContactsNextWordDictionary = NullNextWordGetter;
            }
        }
    };

    @NonNull
    private NextWordGetter mContactsNextWordDictionary = NullNextWordGetter;

    @NonNull
    private Dictionary mAbbreviationDictionary = NullDictionary;

    public SuggestionsProvider(@NonNull Context context) {
        mContext = context.getApplicationContext();
        mExternalDictionaryFactory = AnyApplication.getExternalDictionaryFactory(mContext);

        final Resources resources = context.getResources();
        mQuickFixesPrefId = resources.getString(R.string.settings_key_quick_fix);
        mContactsDictionaryPrefId = resources.getString(R.string.settings_key_use_contacts_dictionary);
        mContactsDictionaryEnabledDefaultValue = resources.getBoolean(R.bool.settings_default_contacts_dictionary);

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mPrefsChangeListener.onSharedPreferenceChanged(sharedPreferences, null);

        AnyApplication.getConfig().addChangedListener(mPrefsChangeListener);
    }

    public void setupSuggestionsForKeyboard(@Nullable DictionaryAddOnAndBuilder dictionaryBuilder) {
        Logger.d(TAG, "setupSuggestionsFor %s", dictionaryBuilder);

        close();

        if (dictionaryBuilder != null) {
            try {
                mMainDictionary = dictionaryBuilder.createDictionary();
            } catch (Exception e) {
                Logger.e(TAG, e, "Failed to create dictionary %s", dictionaryBuilder.getId());
                e.printStackTrace();
                mMainDictionary = NullDictionary;
            }
            DictionaryASyncLoader.executeLoaderParallel(mMainDictionary);
            mUserDictionary = new UserDictionary(mContext, dictionaryBuilder.getLanguage());
            DictionaryASyncLoader.executeLoaderParallel(mUserDictionary);
            mUserNextWordDictionary = ((UserDictionary) mUserDictionary).getUserNextWordGetter();

            if (mQuickFixesEnabled) {
                mQuickFixesAutoText = dictionaryBuilder.createAutoText();
                mAbbreviationDictionary = new AbbreviationsDictionary(mContext, dictionaryBuilder.getLanguage());
                DictionaryASyncLoader.executeLoaderParallel(mAbbreviationDictionary);
            } else {
                mQuickFixesAutoText = NullAutoText;
                mAbbreviationDictionary = NullDictionary;
            }
            mInitialSuggestionsList.addAll(dictionaryBuilder.createInitialSuggestions());

            if (AnyApplication.getConfig().getAutoDictionaryInsertionThreshold() > 0) {
                mAutoDictionary = new AutoDictionary(mContext, dictionaryBuilder.getLanguage());
                DictionaryASyncLoader.executeLoaderParallel(mAutoDictionary);
            }
        }

        if (mContactsDictionaryEnabled) {
            if (mContactsDictionary == NullDictionary) {
                mContactsDictionary = new ContactsDictionary(mContext);
                mContactsNextWordDictionary = (ContactsDictionary) mContactsDictionary;
                DictionaryASyncLoader.executeLoaderParallel(mContactsDictionaryListener, mContactsDictionary);

            }
        } else {
            mContactsDictionary.close();
            mContactsDictionary = NullDictionary;
        }

    }

    public void removeWordFromUserDictionary(String word) {
        mUserDictionary.deleteWord(word);
    }

    public boolean addWordToUserDictionary(String word) {
        return mUserDictionary.addWord(word, 128);
    }

    public boolean isValidWord(CharSequence word) {
        if (TextUtils.isEmpty(word)) {
            return false;
        }

        return mMainDictionary.isValidWord(word) || mUserDictionary.isValidWord(word) || mContactsDictionary.isValidWord(word);
    }

    public void close() {
        Logger.d(TAG, "closeDictionaries");
        mMainDictionary.close();
        mMainDictionary = NullDictionary;
        mAbbreviationDictionary.close();
        mAbbreviationDictionary = NullDictionary;
        mAutoDictionary.close();
        mAutoDictionary = NullDictionary;
        mContactsDictionary.close();
        mContactsDictionary = NullDictionary;
        mUserDictionary.close();
        mUserDictionary = NullDictionary;
        mQuickFixesAutoText = NullAutoText;
        mContactsNextWordDictionary.resetSentence();
        mContactsNextWordDictionary = NullNextWordGetter;
        mUserNextWordDictionary.resetSentence();
        mUserNextWordDictionary = NullNextWordGetter;
        mInitialSuggestionsList.clear();
        System.gc();
    }

    public void resetNextWordSentence() {
        mUserNextWordDictionary.resetSentence();
        mContactsNextWordDictionary.resetSentence();
    }

    public void getSuggestions(WordComposer wordComposer, Dictionary.WordCallback wordCallback) {
        mContactsDictionary.getWords(wordComposer, wordCallback);
        mUserDictionary.getWords(wordComposer, wordCallback);
        mMainDictionary.getWords(wordComposer, wordCallback);
    }

    public void getAbbreviations(WordComposer wordComposer, Dictionary.WordCallback wordCallback) {
        mAbbreviationDictionary.getWords(wordComposer, wordCallback);
    }

    public CharSequence lookupQuickFix(String word) {
        return mQuickFixesAutoText.lookup(word);
    }

    public void getNextWords(String currentWord, Collection<CharSequence> suggestionsHolder, int maxSuggestions) {
        for (String nextWordSuggestion : mUserNextWordDictionary.getNextWords(currentWord, mMaxNextWordSuggestionsCount, mMinWordUsage)) {
            suggestionsHolder.add(nextWordSuggestion);
            maxSuggestions--;
            if (maxSuggestions == 0) return;
        }

        for (String nextWordSuggestion : mContactsNextWordDictionary.getNextWords(currentWord, mMaxNextWordSuggestionsCount, mMinWordUsage)) {
            suggestionsHolder.add(nextWordSuggestion);
            maxSuggestions--;
            if (maxSuggestions == 0) return;
        }

        if (Utils.NEXT_WORD_SUGGESTION_WORDS_AND_PUNCTUATIONS.equals(mNextWordSuggestionType)) {
            for (String evenMoreSuggestions : mInitialSuggestionsList) {
                suggestionsHolder.add(evenMoreSuggestions);
                maxSuggestions--;
                if (maxSuggestions == 0) return;
            }
        }
    }

    public boolean tryToLearnNewWord(String newWord, int frequencyDelta) {
        if (!isValidWord(newWord)) {
            return mAutoDictionary.addWord(newWord, frequencyDelta);
        }

        return false;
    }
}
