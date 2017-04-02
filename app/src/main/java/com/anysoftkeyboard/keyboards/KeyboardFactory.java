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

package com.anysoftkeyboard.keyboards;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.anysoftkeyboard.addons.AddOn;
import com.anysoftkeyboard.addons.AddOnsFactory;
import com.anysoftkeyboard.utils.Logger;
import com.menny.android.anysoftkeyboard.BuildConfig;
import com.menny.android.anysoftkeyboard.R;


public class KeyboardFactory extends AddOnsFactory.MultipleAddOnsFactory<KeyboardAddOnAndBuilder> {
    private static final String TAG = "ASK_KF";

    private static final String XML_LAYOUT_RES_ID_ATTRIBUTE = "layoutResId";
    private static final String XML_LANDSCAPE_LAYOUT_RES_ID_ATTRIBUTE = "landscapeResId";
    private static final String XML_ICON_RES_ID_ATTRIBUTE = "iconResId";
    private static final String XML_DICTIONARY_NAME_ATTRIBUTE = "defaultDictionaryLocale";
    private static final String XML_ADDITIONAL_IS_LETTER_EXCEPTIONS_ATTRIBUTE = "additionalIsLetterExceptions";
    private static final String XML_SENTENCE_SEPARATOR_CHARACTERS_ATTRIBUTE = "sentenceSeparators";
    private static final String DEFAULT_SENTENCE_SEPARATORS = ".,!?)]:;";
    private static final String XML_PHYSICAL_TRANSLATION_RES_ID_ATTRIBUTE = "physicalKeyboardMappingResId";
    private static final String XML_DEFAULT_ATTRIBUTE = "defaultEnabled";
    public static final String PREF_ID_PREFIX = "keyboard_";

    public KeyboardFactory(@NonNull Context context) {
        super(context, TAG, "com.menny.android.anysoftkeyboard.KEYBOARD", "com.menny.android.anysoftkeyboard.keyboards",
                "Keyboards", "Keyboard", PREF_ID_PREFIX,
                R.xml.keyboards, R.string.settings_default_keyboard_id, true);
    }

    @Override
    protected KeyboardAddOnAndBuilder createConcreteAddOn(Context askContext, Context context, CharSequence prefId, CharSequence name, CharSequence description, boolean isHidden, int sortIndex, AttributeSet attrs) {
        final int layoutResId = attrs.getAttributeResourceValue(null, XML_LAYOUT_RES_ID_ATTRIBUTE, AddOn.INVALID_RES_ID);
        final int landscapeLayoutResId = attrs.getAttributeResourceValue(null, XML_LANDSCAPE_LAYOUT_RES_ID_ATTRIBUTE, AddOn.INVALID_RES_ID);
        final int iconResId = attrs.getAttributeResourceValue(null, XML_ICON_RES_ID_ATTRIBUTE, R.drawable.sym_keyboard_notification_icon);
        final String defaultDictionary = attrs.getAttributeValue(null, XML_DICTIONARY_NAME_ATTRIBUTE);
        final String additionalIsLetterExceptions = attrs.getAttributeValue(null, XML_ADDITIONAL_IS_LETTER_EXCEPTIONS_ATTRIBUTE);
        String sentenceSeparators = attrs.getAttributeValue(null, XML_SENTENCE_SEPARATOR_CHARACTERS_ATTRIBUTE);
        if (TextUtils.isEmpty(sentenceSeparators)) sentenceSeparators = DEFAULT_SENTENCE_SEPARATORS;
        final int physicalTranslationResId = attrs.getAttributeResourceValue(null, XML_PHYSICAL_TRANSLATION_RES_ID_ATTRIBUTE, AddOn.INVALID_RES_ID);
        // A keyboard is enabled by default if it is the first one (index==1)
        final boolean keyboardDefault = attrs.getAttributeBooleanValue(null, XML_DEFAULT_ATTRIBUTE, sortIndex == 1);

        // asserting
        if (layoutResId == AddOn.INVALID_RES_ID) {
            Logger.e(TAG, "External Keyboard does not include all mandatory details! Will not create keyboard.");
            return null;
        } else {
            if (BuildConfig.DEBUG) {
                Logger.d(TAG,
                        "External keyboard details: prefId:" + prefId + " nameId:"
                                + name + " resId:" + layoutResId
                                + " landscapeResId:" + landscapeLayoutResId
                                + " iconResId:" + iconResId + " defaultDictionary:"
                                + defaultDictionary);
            }
            return new KeyboardAddOnAndBuilder(askContext, context,
                    prefId, name, layoutResId, landscapeLayoutResId,
                    defaultDictionary, iconResId, physicalTranslationResId,
                    additionalIsLetterExceptions, sentenceSeparators,
                    description, isHidden, sortIndex, keyboardDefault);
        }
    }

    public boolean hasMultipleAlphabets() {
        return getEnabledIds().size() > 1;
    }

    @Override
    protected boolean isAddOnEnabledByDefault(@NonNull CharSequence addOnId) {
        final KeyboardAddOnAndBuilder addOnById = getAddOnById(addOnId);
        return super.isAddOnEnabledByDefault(addOnId) || (addOnById != null && addOnById.getKeyboardDefaultEnabled());
    }
}
