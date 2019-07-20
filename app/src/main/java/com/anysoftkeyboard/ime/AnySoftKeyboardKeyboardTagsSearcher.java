/*
 * Copyright (c) 2016 Menny Even-Danan
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

package com.anysoftkeyboard.ime;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.XmlResourceParser;
import android.preference.PreferenceManager;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.anysoftkeyboard.addons.AddOn;
import com.anysoftkeyboard.dictionaries.WordComposer;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.KeyboardDimens;
import com.anysoftkeyboard.prefs.RxSharedPrefs;
import com.anysoftkeyboard.quicktextkeys.QuickKeyHistoryRecords;
import com.anysoftkeyboard.quicktextkeys.QuickTextKey;
import com.anysoftkeyboard.quicktextkeys.QuickTextKeyFactory;
import com.anysoftkeyboard.quicktextkeys.TagsExtractor;
import com.anysoftkeyboard.quicktextkeys.TagsExtractorImpl;
import com.anysoftkeyboard.rx.GenericOnError;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

public abstract class AnySoftKeyboardKeyboardTagsSearcher extends AnySoftKeyboardSuggestions {

    public static final String MAGNIFYING_GLASS_CHARACTER = "\uD83D\uDD0D";

    @NonNull private TagsExtractor mTagsExtractor = TagsExtractorImpl.NO_OP;
    private QuickKeyHistoryRecords mQuickKeyHistoryRecords;
    private SharedPreferences mSharedPrefsNotToUse;
    private SharedPreferences.OnSharedPreferenceChangeListener mUpdatedPrefKeysListener =
            (sharedPreferences, key) -> {
                if (key.startsWith(QuickTextKeyFactory.PREF_ID_PREFIX)
                        && mTagsExtractor.isEnabled()) {
                    // forcing reload
                    setupTagsSearcher();
                }
            };

    @Override
    public void onCreate() {
        super.onCreate();
        final RxSharedPrefs prefs = prefs();
        mQuickKeyHistoryRecords = new QuickKeyHistoryRecords(prefs);
        addDisposable(
                prefs.getBoolean(
                                R.string.settings_key_search_quick_text_tags,
                                R.bool.settings_default_search_quick_text_tags)
                        .asObservable()
                        .subscribe(
                                this::updateTagExtractor,
                                GenericOnError.onError("settings_key_search_quick_text_tags")));

        mSharedPrefsNotToUse = PreferenceManager.getDefaultSharedPreferences(this);
        mSharedPrefsNotToUse.registerOnSharedPreferenceChangeListener(mUpdatedPrefKeysListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSharedPrefsNotToUse.unregisterOnSharedPreferenceChangeListener(mUpdatedPrefKeysListener);
    }

    private void updateTagExtractor(boolean enabled) {
        if (enabled && !mTagsExtractor.isEnabled()) {
            setupTagsSearcher();
        } else {
            setTagsSearcher(TagsExtractorImpl.NO_OP);
        }
    }

    private void setupTagsSearcher() {
        setTagsSearcher(
                new TagsExtractorImpl(
                        this,
                        extractKeysListListFromEnabledQuickText(
                                AnyApplication.getQuickTextKeyFactory(this).getEnabledAddOns()),
                        mQuickKeyHistoryRecords));
    }

    private void setTagsSearcher(@NonNull TagsExtractor extractor) {
        mTagsExtractor = extractor;
        getSuggest().setTagsSearcher(mTagsExtractor);
    }

    @Nullable
    protected TagsExtractor getQuickTextTagsSearcher() {
        return mTagsExtractor;
    }

    protected QuickKeyHistoryRecords getQuickKeyHistoryRecords() {
        return mQuickKeyHistoryRecords;
    }

    private List<List<Keyboard.Key>> extractKeysListListFromEnabledQuickText(
            List<QuickTextKey> orderedEnabledQuickKeys) {
        ArrayList<List<Keyboard.Key>> listOfLists = new ArrayList<>();
        for (QuickTextKey quickTextKey : orderedEnabledQuickKeys) {
            if (quickTextKey.isPopupKeyboardUsed()) {
                final Context packageContext = quickTextKey.getPackageContext();
                if (packageContext != null) {
                    Keyboard keyboard =
                            new NoOpKeyboard(
                                    quickTextKey,
                                    getApplicationContext(),
                                    quickTextKey.getPopupKeyboardResId());

                    listOfLists.add(keyboard.getKeys());
                }
            }
        }

        return listOfLists;
    }

    /** Helper to determine if a given character code is alphabetic. */
    @Override
    @CallSuper
    protected boolean isAlphabet(int code) {
        return isTagsSearchCharacter(code);
    }

    private boolean isTagsSearchCharacter(int code) {
        return mTagsExtractor.isEnabled() && code == WordComposer.START_TAGS_SEARCH_CHARACTER;
    }

    @Override
    @CallSuper
    protected boolean isSuggestionAffectingCharacter(int code) {
        return super.isSuggestionAffectingCharacter(code) || isTagsSearchCharacter(code);
    }

    @Override
    public void pickSuggestionManually(
            int index, CharSequence suggestion, boolean withAutoSpaceEnabled) {
        if (mWord.isAtTagsSearchState()) {
            if (index == 0) {
                // this is a special case for tags-searcher
                // since we append a magnifying glass to the suggestions, the "suggestion"
                // value is not a valid output suggestion
                suggestion = mWord.getTypedWord().toString();
            } else {
                // regular emoji. Storing in history.
                getQuickKeyHistoryRecords().store(suggestion.toString(), suggestion.toString());
            }
        }

        super.pickSuggestionManually(index, suggestion, withAutoSpaceEnabled);
    }

    private static class NoOpKeyboard extends Keyboard {
        private static final KeyboardDimens SIMPLE_KEYBOARD_DIMENS = new SimpleKeyboardDimens();

        private NoOpKeyboard(
                @NonNull AddOn keyboardAddOn, @NonNull Context askContext, int xmlLayoutResId) {
            super(keyboardAddOn, askContext, xmlLayoutResId);
            loadKeyboard(SIMPLE_KEYBOARD_DIMENS);
        }

        @Override
        protected Key createKeyFromXml(
                @NonNull AddOn.AddOnResourceMapping resourceMapping,
                Context askContext,
                Context keyboardContext,
                Row parent,
                KeyboardDimens keyboardDimens,
                int x,
                int y,
                XmlResourceParser parser) {
            return new AnyKeyboard.AnyKey(
                    resourceMapping, keyboardContext, parent, keyboardDimens, 1, 1, parser);
        }
    }

    private static class SimpleKeyboardDimens implements KeyboardDimens {

        @Override
        public int getKeyboardMaxWidth() {
            return 1000000;
        }

        @Override
        public float getKeyHorizontalGap() {
            return 0;
        }

        @Override
        public float getRowVerticalGap() {
            return 0;
        }

        @Override
        public int getNormalKeyHeight() {
            return 2;
        }

        @Override
        public int getSmallKeyHeight() {
            return 1;
        }

        @Override
        public int getLargeKeyHeight() {
            return 3;
        }

        @Override
        public float getPaddingBottom() {
            return 0;
        }
    }

    public static class TagsSuggestionList implements List<CharSequence> {

        @NonNull private CharSequence mTypedTag = MAGNIFYING_GLASS_CHARACTER;
        @NonNull private List<CharSequence> mFoundTags = Collections.emptyList();

        public void setTagsResults(@NonNull List<CharSequence> foundTags) {
            mFoundTags = foundTags;
        }

        public void setTypedWord(@NonNull CharSequence typedWord) {
            mTypedTag = MAGNIFYING_GLASS_CHARACTER + typedWord;
        }

        @Override
        public int size() {
            return 1 + mFoundTags.size();
        }

        @Override
        public CharSequence get(int location) {
            if (location == 0) {
                return mTypedTag;
            } else {
                return mFoundTags.get(location - 1);
            }
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @NonNull
        @Override
        public Iterator<CharSequence> iterator() {
            return new Iterator<CharSequence>() {
                private int mCurrentIndex = 0;

                @Override
                public boolean hasNext() {
                    return mCurrentIndex < size();
                }

                @Override
                public CharSequence next() {
                    if (!hasNext()) throw new NoSuchElementException("Called after end of list!");
                    return get(mCurrentIndex++);
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }

        /*NOT IMPLEMENTED BELOW!! */

        @Override
        public void add(int location, CharSequence object) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean add(CharSequence object) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAll(
                int location, @NonNull Collection<? extends CharSequence> collection) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAll(@NonNull Collection<? extends CharSequence> collection) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean contains(Object object) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsAll(@NonNull Collection<?> collection) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int indexOf(Object object) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int lastIndexOf(Object object) {
            throw new UnsupportedOperationException();
        }

        @Override
        @NonNull
        public ListIterator<CharSequence> listIterator() {
            throw new UnsupportedOperationException();
        }

        @NonNull
        @Override
        public ListIterator<CharSequence> listIterator(int location) {
            throw new UnsupportedOperationException();
        }

        @Override
        public CharSequence remove(int location) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(Object object) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeAll(@NonNull Collection<?> collection) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean retainAll(@NonNull Collection<?> collection) {
            throw new UnsupportedOperationException();
        }

        @Override
        public CharSequence set(int location, CharSequence object) {
            throw new UnsupportedOperationException();
        }

        @NonNull
        @Override
        public List<CharSequence> subList(int start, int end) {
            throw new UnsupportedOperationException();
        }

        @NonNull
        @Override
        public Object[] toArray() {
            Object[] items = new Object[size()];
            items[0] = mTypedTag;
            if (items.length > 1) {
                System.arraycopy(mFoundTags.toArray(), 0, items, 1, items.length - 1);
            }

            return items;
        }

        @NonNull
        @Override
        public <T> T[] toArray(@NonNull T[] array) {
            throw new UnsupportedOperationException();
        }
    }
}
