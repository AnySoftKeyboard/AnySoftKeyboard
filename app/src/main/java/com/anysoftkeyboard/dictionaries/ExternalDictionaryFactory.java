/*
 * Copyright (c) 2013 Menny Even-Danan
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

package com.anysoftkeyboard.dictionaries;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
import android.util.AttributeSet;

import com.anysoftkeyboard.addons.AddOn;
import com.anysoftkeyboard.addons.AddOnsFactory;
import com.anysoftkeyboard.utils.Logger;
import com.menny.android.anysoftkeyboard.R;

import java.util.Map;

public class ExternalDictionaryFactory extends AddOnsFactory<DictionaryAddOnAndBuilder> {

    private static final String TAG = "ASK ExtDictFctry";

    private static final String XML_LANGUAGE_ATTRIBUTE = "locale";
    private static final String XML_ASSETS_ATTRIBUTE = "dictionaryAssertName";
    private static final String XML_RESOURCE_ATTRIBUTE = "dictionaryResourceId";
    private static final String XML_AUTO_TEXT_RESOURCE_ATTRIBUTE = "autoTextResourceId";
    private static final String XML_INITIAL_SUGGESTIONS_ARRAY_RESOURCE_ATTRIBUTE = "initialSuggestions";

    private final Map<String, DictionaryAddOnAndBuilder> mBuildersByLocale = new ArrayMap<>();

    public ExternalDictionaryFactory(Context context) {
        super(context, TAG, "com.menny.android.anysoftkeyboard.DICTIONARY", "com.menny.android.anysoftkeyboard.dictionaries",
                "Dictionaries", "Dictionary", "dictionary_",
                R.xml.dictionaries, 0, true);
    }

    @Override
    protected synchronized void clearAddOnList() {
        super.clearAddOnList();
        mBuildersByLocale.clear();
    }

    @Override
    protected void loadAddOns() {
        super.loadAddOns();

        for (DictionaryAddOnAndBuilder addOn : getAllAddOns())
            mBuildersByLocale.put(addOn.getLanguage(), addOn);
    }

    public synchronized DictionaryAddOnAndBuilder getDictionaryBuilderByLocale(String locale) {
        if (mBuildersByLocale.size() == 0)
            loadAddOns();

        return mBuildersByLocale.get(locale);
    }

    @Override
    protected boolean isAddOnEnabledByDefault(@NonNull CharSequence addOnId) {
        return true;
    }

    @Override
    public boolean isAddOnEnabled(CharSequence addOnId) {
        return true;
    }

    @Override
    public void setAddOnEnabled(CharSequence addOnId, boolean enabled) {
        throw new UnsupportedOperationException("This is not supported for dictionaries.");
    }

    @Override
    protected DictionaryAddOnAndBuilder createConcreteAddOn(Context askContext, Context context, CharSequence prefId, CharSequence name, CharSequence description, boolean isHidden, int sortIndex, AttributeSet attrs) {

        final String language = attrs.getAttributeValue(null, XML_LANGUAGE_ATTRIBUTE);
        final String assets = attrs.getAttributeValue(null, XML_ASSETS_ATTRIBUTE);
        final int dictionaryResourceId = attrs.getAttributeResourceValue(null, XML_RESOURCE_ATTRIBUTE, AddOn.INVALID_RES_ID);
        final int autoTextResId = attrs.getAttributeResourceValue(null, XML_AUTO_TEXT_RESOURCE_ATTRIBUTE, AddOn.INVALID_RES_ID);
        final int initialSuggestionsId = attrs.getAttributeResourceValue(null, XML_INITIAL_SUGGESTIONS_ARRAY_RESOURCE_ATTRIBUTE, AddOn.INVALID_RES_ID);
        //asserting
        if ((language == null) || ((assets == null) && (dictionaryResourceId == AddOn.INVALID_RES_ID))) {
            Logger.e(TAG, "External dictionary does not include all mandatory details! Will not create dictionary.");
            return null;
        } else {
            final DictionaryAddOnAndBuilder creator;
            if (dictionaryResourceId == AddOn.INVALID_RES_ID)
                creator = new DictionaryAddOnAndBuilder(askContext, context, prefId, name, description, isHidden, sortIndex, language, assets, initialSuggestionsId);
            else
                creator = new DictionaryAddOnAndBuilder(askContext, context, prefId, name, description, isHidden, sortIndex, language, dictionaryResourceId, autoTextResId, initialSuggestionsId);

            return creator;
        }
    }
}
