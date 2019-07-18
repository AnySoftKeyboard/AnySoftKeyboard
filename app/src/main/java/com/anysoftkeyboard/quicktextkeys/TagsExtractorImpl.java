package com.anysoftkeyboard.quicktextkeys;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.v4.util.ArrayMap;
import com.anysoftkeyboard.dictionaries.DictionaryBackgroundLoader;
import com.anysoftkeyboard.dictionaries.InMemoryDictionary;
import com.anysoftkeyboard.dictionaries.KeyCodesProvider;
import com.anysoftkeyboard.ime.AnySoftKeyboardKeyboardTagsSearcher;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import io.reactivex.disposables.Disposable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

public class TagsExtractorImpl implements TagsExtractor {

    public static final TagsExtractor NO_OP =
            new TagsExtractor() {
                @Override
                public List<CharSequence> getOutputForTag(
                        @NonNull CharSequence typedTagToSearch, KeyCodesProvider wordComposer) {
                    return Collections.emptyList();
                }

                @Override
                public boolean isEnabled() {
                    return false;
                }

                @Override
                public void close() {}
            };

    private final ArrayMap<String, List<CharSequence>> mTagsForOutputs = new ArrayMap<>();

    @NonNull
    private AnySoftKeyboardKeyboardTagsSearcher.TagsSuggestionList mTagSuggestionsList =
            new AnySoftKeyboardKeyboardTagsSearcher.TagsSuggestionList();

    @NonNull @VisibleForTesting final InMemoryDictionary mTagsDictionary;
    private final MyCodesProvider mWordComposer = new MyCodesProvider();
    private final Set<CharSequence> mTempPossibleQuickTextsFromDictionary = new TreeSet<>();
    private final List<CharSequence> mPossibleQuickTextsFromDictionary =
            new ArrayList<>(64 /*I don't believe we'll have more that that*/);

    private final QuickKeyHistoryRecords mQuickKeyHistoryRecords;
    private final Disposable mDictionaryDisposable;

    public TagsExtractorImpl(
            @NonNull Context context,
            @NonNull List<List<Keyboard.Key>> listsOfKeys,
            QuickKeyHistoryRecords quickKeyHistoryRecords) {
        mQuickKeyHistoryRecords = quickKeyHistoryRecords;
        for (List<Keyboard.Key> keys : listsOfKeys) {
            for (Keyboard.Key key : keys) {
                AnyKeyboard.AnyKey anyKey = (AnyKeyboard.AnyKey) key;
                for (String tagFromKey : anyKey.getKeyTags()) {
                    String tag = tagFromKey.toLowerCase(Locale.US);
                    if (!mTagsForOutputs.containsKey(tag)) {
                        mTagsForOutputs.put(tag, new ArrayList<>());
                    }
                    mTagsForOutputs.get(tag).add(anyKey.text);
                }
            }
        }

        mTagsDictionary =
                new InMemoryDictionary(
                        "quick_text_tags_dictionary", context, mTagsForOutputs.keySet(), true);
        mDictionaryDisposable =
                DictionaryBackgroundLoader.loadDictionaryInBackground(mTagsDictionary);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public List<CharSequence> getOutputForTag(
            @NonNull CharSequence typedTagToSearch, @NonNull KeyCodesProvider wordComposer) {
        mTagSuggestionsList.setTypedWord(typedTagToSearch);
        String tag = typedTagToSearch.toString().toLowerCase(Locale.US);

        mPossibleQuickTextsFromDictionary.clear();

        if (tag.length() == 0) {
            for (QuickKeyHistoryRecords.HistoryKey historyKey :
                    mQuickKeyHistoryRecords.getCurrentHistory()) {
                // history is in reverse
                mPossibleQuickTextsFromDictionary.add(0, historyKey.value);
            }
            mTagSuggestionsList.setTagsResults(mPossibleQuickTextsFromDictionary);
        } else {
            mTempPossibleQuickTextsFromDictionary.clear();
            mWordComposer.setTypedTag(wordComposer, typedTagToSearch);
            mTagsDictionary.getWords(
                    mWordComposer,
                    (word, wordOffset, wordLength, frequency, from) -> {
                        // using a Set will ensure we do not have duplication
                        mTempPossibleQuickTextsFromDictionary.addAll(
                                mTagsForOutputs.get(new String(word, wordOffset, wordLength)));
                        return true;
                    });
            mPossibleQuickTextsFromDictionary.addAll(mTempPossibleQuickTextsFromDictionary);
            mTagSuggestionsList.setTagsResults(mPossibleQuickTextsFromDictionary);
        }

        return mTagSuggestionsList;
    }

    @Override
    public void close() {
        mDictionaryDisposable.dispose();
    }

    private static class MyCodesProvider implements KeyCodesProvider {

        private static final int[] SINGLE_CODE = new int[1];

        private KeyCodesProvider mTag = null;
        private CharSequence mTypedWord = "";

        private void setTypedTag(@NonNull KeyCodesProvider tag, @NonNull CharSequence typedWord) {
            mTag = tag;
            mTypedWord = typedWord;
        }

        @Override
        public int length() {
            return mTypedWord.length();
        }

        @Override
        public int[] getCodesAt(int index) {
            SINGLE_CODE[0] = mTag.getCodesAt(index + 1)[0];
            return SINGLE_CODE;
        }

        @Override
        public CharSequence getTypedWord() {
            return mTypedWord;
        }
    }
}
