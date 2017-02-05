package com.anysoftkeyboard.quicktextkeys;

import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;

import com.anysoftkeyboard.ime.AnySoftKeyboardKeyboardTagsSearcher;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class TagsExtractor {

    private final ArrayMap<String, List<CharSequence>> mTagsForOutputs = new ArrayMap<>();

    @NonNull
    private AnySoftKeyboardKeyboardTagsSearcher.TagsSuggestionList mTagSuggestionsList = new AnySoftKeyboardKeyboardTagsSearcher.TagsSuggestionList();

    public TagsExtractor(@NonNull List<List<Keyboard.Key>> listsOfKeys) {
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
    }

    public List<CharSequence> getOutputForTag(@NonNull CharSequence typedTagToSearch) {
        mTagSuggestionsList.setTypedWord(typedTagToSearch);
        String tag = typedTagToSearch.toString().toLowerCase(Locale.US);

        if (mTagsForOutputs.containsKey(tag))
            mTagSuggestionsList.setTagsResults(mTagsForOutputs.get(tag));
        else {
            /*mWordComposer.setTypedTag(wordComposer, typedTagToSearch);
            mTagsDictionary.getWords(mWordComposer, mSuggestionsListBuilder);
            mTagSuggestionsList.setTagsResults(mPossibleTagsFromDictionary);*/
            mTagSuggestionsList.setTagsResults(Collections.<CharSequence>emptyList());
        }

        return mTagSuggestionsList;
    }

    /*private static class MyCodesProvider implements KeyCodesProvider {

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
    }*/
}
