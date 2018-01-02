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
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.content.SharedPreferencesCompat;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.anysoftkeyboard.addons.AddOn;
import com.anysoftkeyboard.addons.AddOnsFactory;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.KeyboardFactory;
import com.anysoftkeyboard.base.utils.Logger;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ExternalDictionaryFactory extends AddOnsFactory<DictionaryAddOnAndBuilder> {

    private static final String PREFS_KEY_POSTFIX_OVERRIDE_DICTIONARY = "_override_dictionary";
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

    public static String getDictionaryOverrideKey(AnyKeyboard currentKeyboard) {
        return String.format(Locale.US, "%s%s%s", KeyboardFactory.PREF_ID_PREFIX, currentKeyboard.getKeyboardId(), PREFS_KEY_POSTFIX_OVERRIDE_DICTIONARY);
    }

    public static boolean isOverrideDictionaryPrefKey(String key) {
        return !TextUtils.isEmpty(key) &&
                key.startsWith(KeyboardFactory.PREF_ID_PREFIX) && key.endsWith(PREFS_KEY_POSTFIX_OVERRIDE_DICTIONARY);
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
    protected DictionaryAddOnAndBuilder createConcreteAddOn(Context askContext, Context context, int apiVersion, CharSequence prefId, CharSequence name, CharSequence description, boolean isHidden, int sortIndex, AttributeSet attrs) {
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
                creator = new DictionaryAddOnAndBuilder(askContext, context, apiVersion, prefId, name, description, isHidden, sortIndex, language, assets, initialSuggestionsId);
            else
                creator = new DictionaryAddOnAndBuilder(askContext, context, apiVersion, prefId, name, description, isHidden, sortIndex, language, dictionaryResourceId, autoTextResId, initialSuggestionsId);

            return creator;
        }
    }

    @NonNull
    public List<DictionaryAddOnAndBuilder> getBuildersForKeyboard(AnyKeyboard keyboard) {
        List<DictionaryAddOnAndBuilder> builders = new ArrayList<>();
        final String dictionaryValue = mSharedPreferences.getString(getDictionaryOverrideKey(keyboard), null);

        if (TextUtils.isEmpty(dictionaryValue)) {
            final DictionaryAddOnAndBuilder builderByLocale = AnyApplication.getExternalDictionaryFactory(mContext).getDictionaryBuilderByLocale(keyboard.getDefaultDictionaryLocale());
            if (builderByLocale != null) builders.add(builderByLocale);
        } else {
            String[] ids = dictionaryValue.split(":");
            for (String id : ids) {
                final DictionaryAddOnAndBuilder addOnById = AnyApplication.getExternalDictionaryFactory(mContext).getAddOnById(id);
                if (addOnById != null) builders.add(addOnById);
            }
        }

        return builders;
    }

    public void setBuildersForKeyboard(AnyKeyboard keyboard, List<DictionaryAddOnAndBuilder> buildersForKeyboard) {
        final String mappingSettingsKey = getDictionaryOverrideKey(keyboard);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        if (buildersForKeyboard.size() == 0) {
            editor.remove(mappingSettingsKey);
        } else {
            StringBuilder stringBuilder = new StringBuilder(buildersForKeyboard.size() * 24);
            for (DictionaryAddOnAndBuilder builder : buildersForKeyboard) {
                if (stringBuilder.length() > 0) stringBuilder.append(':');
                stringBuilder.append(builder.getId());
            }
            editor.putString(mappingSettingsKey, stringBuilder.toString());
        }
        SharedPreferencesCompat.EditorCompat.getInstance().apply(editor);
    }
}
