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
import com.anysoftkeyboard.base.dictionaries.WordComposer;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.KeyboardDimens;
import com.anysoftkeyboard.quicktextkeys.QuickTextKey;
import com.anysoftkeyboard.quicktextkeys.QuickTextKeyFactory;
import com.anysoftkeyboard.quicktextkeys.TagsExtractor;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public abstract class AnySoftKeyboardKeyboardTagsSearcher extends AnySoftKeyboardKeyboardSwitchedListener {

    public static final String MAGNIFYING_GLASS_CHARACTER = "\uD83D\uDD0D";

    private String mTagExtractorPrefKey;
    private boolean mTagExtractorDefaultValue;

    @Nullable
    private TagsExtractor mEmojiTagsSearcher;

    @Override
    public void onCreate() {
        super.onCreate();
        mTagExtractorPrefKey = getString(R.string.settings_key_search_quick_text_tags);
        mTagExtractorDefaultValue = getResources().getBoolean(R.bool.settings_default_search_quick_text_tags);
        updateTagExtractor(PreferenceManager.getDefaultSharedPreferences(this));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        super.onSharedPreferenceChanged(sharedPreferences, key);
        if (mTagExtractorPrefKey.equals(key)) {
            updateTagExtractor(sharedPreferences);
        } else if (key.startsWith(QuickTextKeyFactory.PREF_ID_PREFIX) && isQuickTextTagSearchEnabled()) {
            //forcing reload
            setTagsSearcher(new TagsExtractor(this, extractKeysListListFromEnabledQuickText(AnyApplication.getQuickTextKeyFactory(this).getEnabledAddOns())));
        }
    }

    private void updateTagExtractor(SharedPreferences sharedPreferences) {
        final boolean enabled = sharedPreferences.getBoolean(mTagExtractorPrefKey, mTagExtractorDefaultValue);
        if (enabled && mEmojiTagsSearcher == null) {
            setTagsSearcher(new TagsExtractor(this, extractKeysListListFromEnabledQuickText(AnyApplication.getQuickTextKeyFactory(this).getEnabledAddOns())));
        } else if (!enabled) {
            setTagsSearcher(null);
        }
    }

    protected boolean isQuickTextTagSearchEnabled() {
        return mEmojiTagsSearcher != null;
    }

    private void setTagsSearcher(@Nullable TagsExtractor extractor) {
        mEmojiTagsSearcher = extractor;
        mSuggest.setTagsSearcher(extractor);
    }

    @Nullable
    protected TagsExtractor getQuickTextTagsSearcher() {
        return mEmojiTagsSearcher;
    }

    private List<List<Keyboard.Key>> extractKeysListListFromEnabledQuickText(List<QuickTextKey> orderedEnabledQuickKeys) {
        ArrayList<List<Keyboard.Key>> listOfLists = new ArrayList<>();
        for (QuickTextKey quickTextKey : orderedEnabledQuickKeys) {
            if (quickTextKey.isPopupKeyboardUsed()) {
                Keyboard keyboard = new NoOpKeyboard(quickTextKey, getApplicationContext(), quickTextKey.getPackageContext(), quickTextKey.getPopupKeyboardResId());

                listOfLists.add(keyboard.getKeys());
            }
        }

        return listOfLists;
    }

    /**
     * Helper to determine if a given character code is alphabetic.
     */
    @Override
    @CallSuper
    protected boolean isAlphabet(int code) {
        return isTagsSearchCharacter(code);
    }

    private boolean isTagsSearchCharacter(int code) {
        return isQuickTextTagSearchEnabled() && code == WordComposer.START_TAGS_SEARCH_CHARACTER;
    }

    @Override
    @CallSuper
    protected boolean isSuggestionAffectingCharacter(int code) {
        return isTagsSearchCharacter(code);
    }

    private static class NoOpKeyboard extends Keyboard {
        private static final KeyboardDimens SIMPLE_KEYBOARD_DIMENS = new SimpleKeyboardDimens();

        private NoOpKeyboard(@NonNull AddOn keyboardAddOn, @NonNull Context askContext, @NonNull Context context, int xmlLayoutResId) {
            super(keyboardAddOn, askContext, context, xmlLayoutResId);
            loadKeyboard(SIMPLE_KEYBOARD_DIMENS);
        }

        @Override
        protected Key createKeyFromXml(@NonNull AddOn.AddOnResourceMapping resourceMapping, Context askContext, Context keyboardContext, Row parent, KeyboardDimens keyboardDimens, int x, int y, XmlResourceParser parser) {
            return new AnyKeyboard.AnyKey(resourceMapping, askContext, keyboardContext, parent, keyboardDimens, 1, 1, parser);
        }
    }

    public static class SimpleKeyboardDimens implements KeyboardDimens {

        @Override
        public int getKeyboardMaxWidth() {
            return 100000;
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
    }

    public static class TagsSuggestionList implements List<CharSequence> {

        @NonNull
        private CharSequence mTypedTag = MAGNIFYING_GLASS_CHARACTER;
        @NonNull
        private List<CharSequence> mFoundTags = Collections.emptyList();

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
        public boolean addAll(int location, @NonNull Collection<? extends CharSequence> collection) {
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
