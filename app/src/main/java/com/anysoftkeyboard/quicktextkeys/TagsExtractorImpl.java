package com.anysoftkeyboard.quicktextkeys;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;

import com.anysoftkeyboard.dictionaries.Dictionary;
import com.anysoftkeyboard.dictionaries.KeyCodesProvider;
import com.anysoftkeyboard.dictionaries.InMemoryDictionary;
import com.anysoftkeyboard.ime.AnySoftKeyboardKeyboardTagsSearcher;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class TagsExtractorImpl implements TagsExtractor {

    public static final TagsExtractor NO_OP = new TagsExtractor() {
        @Override
        public List<CharSequence> getOutputForTag(@NonNull CharSequence typedTagToSearch, KeyCodesProvider wordComposer) {
            return Collections.emptyList();
        }

        @Override
        public boolean isEnabled() {
            return false;
        }
    };

    private final ArrayMap<String, List<CharSequence>> mTagsForOutputs = new ArrayMap<>();

    @NonNull
    private AnySoftKeyboardKeyboardTagsSearcher.TagsSuggestionList mTagSuggestionsList = new AnySoftKeyboardKeyboardTagsSearcher.TagsSuggestionList();
    @NonNull
    private final InMemoryDictionary mTagsDictionary;
    private final MyCodesProvider mWordComposer = new MyCodesProvider();
    private final Set<CharSequence> mTempPossibleQuickTextsFromDictionary = new HashSet<>(64/*I don't believe we'll have more that that*/);
    private final List<CharSequence> mPossibleQuickTextsFromDictionary = new ArrayList<>(64/*I don't believe we'll have more that that*/);
    private final Dictionary.WordCallback mSuggestionsListBuilder = new Dictionary.WordCallback() {
        @Override
        public boolean addWord(char[] word, int wordOffset, int wordLength, int frequency, Dictionary from) {
            final String possibleTag = new String(word, wordOffset, wordLength);
            //using a Set will ensure we do not have duplication
            mTempPossibleQuickTextsFromDictionary.addAll(mTagsForOutputs.get(possibleTag));
            return true;
        }
    };
    private final QuickKeyHistoryRecords mQuickKeyHistoryRecords;

    public TagsExtractorImpl(@NonNull Context context, @NonNull List<List<Keyboard.Key>> listsOfKeys, QuickKeyHistoryRecords quickKeyHistoryRecords) {
        mQuickKeyHistoryRecords = quickKeyHistoryRecords;
        for (List<Keyboard.Key> keys : listsOfKeys) {
            for (Keyboard.Key key : keys) {
                AnyKeyboard.AnyKey anyKey = (AnyKeyboard.AnyKey) key;
                for (String tagFromKey : anyKey.getKeyTags()) {
                    String tag = tagFromKey.toLowerCase(Locale.US);
                    if (!mTagsForOutputs.containsKey(tag))
                        mTagsForOutputs.put(tag, new ArrayList<CharSequence>());
                    mTagsForOutputs.get(tag).add(anyKey.text);
                }
            }
        }

        mTagsDictionary = new InMemoryDictionary("quick_text_tags_dictionary", context, mTagsForOutputs.keySet());
        mTagsDictionary.loadDictionary();
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public List<CharSequence> getOutputForTag(@NonNull CharSequence typedTagToSearch, KeyCodesProvider wordComposer) {
        mTagSuggestionsList.setTypedWord(typedTagToSearch);
        String tag = typedTagToSearch.toString().toLowerCase(Locale.US);

        if (mTagsForOutputs.containsKey(tag)) {
            mTagSuggestionsList.setTagsResults(mTagsForOutputs.get(tag));
        } else if (tag.length() == 0) {
            for (QuickKeyHistoryRecords.HistoryKey historyKey : mQuickKeyHistoryRecords.getCurrentHistory()) {
                //history is in reverse
                mPossibleQuickTextsFromDictionary.add(0, historyKey.value);
            }
            mTagSuggestionsList.setTagsResults(mPossibleQuickTextsFromDictionary);
        } else {
            mTempPossibleQuickTextsFromDictionary.clear();
            mPossibleQuickTextsFromDictionary.clear();
            mWordComposer.setTypedTag(wordComposer, typedTagToSearch);
            mTagsDictionary.getWords(mWordComposer, mSuggestionsListBuilder);
            mPossibleQuickTextsFromDictionary.addAll(mTempPossibleQuickTextsFromDictionary);
            mTagSuggestionsList.setTagsResults(mPossibleQuickTextsFromDictionary);
        }

        return mTagSuggestionsList;
    }

    private static class MyCodesProvider implements KeyCodesProvider {

        private KeyCodesProvider mTag = null;
        private CharSequence mTypedWord = "";

        private void setTypedTag(KeyCodesProvider tag, CharSequence typedWord) {
            mTag = tag;
            mTypedWord = typedWord;
        }

        @Override
        public int length() {
            return mTypedWord.length();
        }

        @Override
        public int[] getCodesAt(int index) {
            return mTag.getCodesAt(index + 1);
        }

        @Override
        public CharSequence getTypedWord() {
            return mTypedWord;
        }
    }
}
