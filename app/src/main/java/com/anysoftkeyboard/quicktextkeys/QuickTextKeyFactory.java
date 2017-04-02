/*
 * Copyright (c) 2015 Menny Even-Danan
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

package com.anysoftkeyboard.quicktextkeys;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;

import com.anysoftkeyboard.addons.AddOn;
import com.anysoftkeyboard.addons.AddOnsFactory;
import com.menny.android.anysoftkeyboard.R;

import java.util.Locale;

public class QuickTextKeyFactory extends AddOnsFactory.MultipleAddOnsFactory<QuickTextKey> {

    private static final String XML_POPUP_KEYBOARD_RES_ID_ATTRIBUTE = "popupKeyboard";
    private static final String XML_POPUP_LIST_TEXT_RES_ID_ATTRIBUTE = "popupListText";
    private static final String XML_POPUP_LIST_OUTPUT_RES_ID_ATTRIBUTE = "popupListOutput";
    private static final String XML_POPUP_LIST_ICONS_RES_ID_ATTRIBUTE = "popupListIcons";
    private static final String XML_ICON_RES_ID_ATTRIBUTE = "keyIcon";
    private static final String XML_KEY_LABEL_RES_ID_ATTRIBUTE = "keyLabel";
    private static final String XML_KEY_OUTPUT_TEXT_RES_ID_ATTRIBUTE = "keyOutputText";
    private static final String XML_ICON_PREVIEW_RES_ID_ATTRIBUTE = "iconPreview";
    public static final String PREF_ID_PREFIX = "quick_text_";

    public QuickTextKeyFactory(@NonNull Context context) {
        super(context, "ASK_QKF", "com.anysoftkeyboard.plugin.QUICK_TEXT_KEY",
                "com.anysoftkeyboard.plugindata.quicktextkeys",
                "QuickTextKeys", "QuickTextKey", PREF_ID_PREFIX,
                R.xml.quick_text_keys, R.string.settings_default_quick_text_key_id, true);
    }

    @Override
    protected QuickTextKey createConcreteAddOn(Context askContext, Context context, CharSequence prefId, CharSequence name, CharSequence description, boolean isHidden, int sortIndex, AttributeSet attrs) {
        final int popupKeyboardResId = attrs.getAttributeResourceValue(null, XML_POPUP_KEYBOARD_RES_ID_ATTRIBUTE, AddOn.INVALID_RES_ID);
        final int popupListTextResId = attrs.getAttributeResourceValue(null, XML_POPUP_LIST_TEXT_RES_ID_ATTRIBUTE, AddOn.INVALID_RES_ID);
        final int popupListOutputResId = attrs.getAttributeResourceValue(null, XML_POPUP_LIST_OUTPUT_RES_ID_ATTRIBUTE, AddOn.INVALID_RES_ID);
        final int popupListIconsResId = attrs.getAttributeResourceValue(null, XML_POPUP_LIST_ICONS_RES_ID_ATTRIBUTE, AddOn.INVALID_RES_ID);
        final int iconResId = attrs.getAttributeResourceValue(null, XML_ICON_RES_ID_ATTRIBUTE, AddOn.INVALID_RES_ID); // Maybe should make a default

        final CharSequence keyLabel = getTextFromResourceOrText(context, attrs, XML_KEY_LABEL_RES_ID_ATTRIBUTE);
        final CharSequence keyOutputText = getTextFromResourceOrText(context, attrs, XML_KEY_OUTPUT_TEXT_RES_ID_ATTRIBUTE);
        final int keyIconPreviewResId = attrs.getAttributeResourceValue(null, XML_ICON_PREVIEW_RES_ID_ATTRIBUTE, AddOn.INVALID_RES_ID);

        if (((popupKeyboardResId == AddOn.INVALID_RES_ID) && ((popupListTextResId == AddOn.INVALID_RES_ID) || (popupListOutputResId == AddOn.INVALID_RES_ID)))
                || ((iconResId == AddOn.INVALID_RES_ID) && (keyLabel == null))
                || (keyOutputText == null)) {
            String detailMessage = String.format(Locale.US, "Missing details for creating QuickTextKey! prefId %s, popupKeyboardResId: %d, popupListTextResId: %d, popupListOutputResId: %d, (iconResId: %d, keyLabel: %s), keyOutputText: %s",
                    prefId, popupKeyboardResId, popupListTextResId,
                    popupListOutputResId, iconResId, keyLabel, keyOutputText);

            throw new RuntimeException(detailMessage);
        }
        return new QuickTextKey(askContext, context, prefId, name, popupKeyboardResId,
                popupListTextResId, popupListOutputResId, popupListIconsResId,
                iconResId, keyLabel, keyOutputText,
                keyIconPreviewResId, isHidden, description, sortIndex);
    }

    @Override
    protected boolean isAddOnEnabledByDefault(@NonNull CharSequence addOnId) {
        return true;//all quick-text addons are enabled by default.
    }
}
