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
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.anysoftkeyboard.addons.AddOn;
import com.anysoftkeyboard.addons.AddOnsFactory;
import com.menny.android.anysoftkeyboard.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class QuickTextKeyFactory extends AddOnsFactory<QuickTextKey> {

	private static final QuickTextKeyFactory msInstance;

	static {
		msInstance = new QuickTextKeyFactory();
	}

	private static final String XML_POPUP_KEYBOARD_RES_ID_ATTRIBUTE = "popupKeyboard";
	private static final String XML_POPUP_LIST_TEXT_RES_ID_ATTRIBUTE = "popupListText";
	private static final String XML_POPUP_LIST_OUTPUT_RES_ID_ATTRIBUTE = "popupListOutput";
	private static final String XML_POPUP_LIST_ICONS_RES_ID_ATTRIBUTE = "popupListIcons";
	private static final String XML_ICON_RES_ID_ATTRIBUTE = "keyIcon";
	private static final String XML_KEY_LABEL_RES_ID_ATTRIBUTE = "keyLabel";
	private static final String XML_KEY_OUTPUT_TEXT_RES_ID_ATTRIBUTE = "keyOutputText";
	private static final String XML_ICON_PREVIEW_RES_ID_ATTRIBUTE = "iconPreview";

	private QuickTextKeyFactory() {
		super("ASK_QKF", "com.anysoftkeyboard.plugin.QUICK_TEXT_KEY",
				"com.anysoftkeyboard.plugindata.quicktextkeys",
				"QuickTextKeys", "QuickTextKey", R.xml.quick_text_keys, true);
	}

	public static QuickTextKey getCurrentQuickTextKey(Context context) {
		return getOrderedEnabledQuickKeys(context).get(0);
	}

	public static List<QuickTextKey> getAllAvailableQuickKeys(Context applicationContext) {
		List<QuickTextKey> list = msInstance.getAllAddOns(applicationContext);
		//for now, only supporting popup-keyboard addons.
		List<QuickTextKey> filteredList = new ArrayList<>(list.size());
		for (QuickTextKey quickTextKey : list) {
			filteredList.add(quickTextKey);
		}

		return Collections.unmodifiableList(filteredList);
	}

	public static void storeOrderedEnabledQuickKeys(Context applicationContext, ArrayList<QuickTextKey> orderedKeys) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
		String settingKey = applicationContext.getString(R.string.settings_key_ordered_active_quick_text_keys);

		List<String> quickKeyIdOrder = new ArrayList<>(orderedKeys.size());
		for (QuickTextKey key : orderedKeys) {
			quickKeyIdOrder.add(key.getId());
		}
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(settingKey, TextUtils.join(",", quickKeyIdOrder)).commit();
	}

	public static List<QuickTextKey> getOrderedEnabledQuickKeys(Context applicationContext) {
		List<QuickTextKey> quickTextKeys = new ArrayList<>(getAllAvailableQuickKeys(applicationContext));

		//now, reading the ordered array of active keys
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
		String settingKey = applicationContext.getString(R.string.settings_key_ordered_active_quick_text_keys);

		List<String> quickKeyIdDefaultOrder = new ArrayList<>(quickTextKeys.size());
		for (QuickTextKey key : quickTextKeys) {
			quickKeyIdDefaultOrder.add(key.getId());
		}
		String quickKeyIdsOrderValue = sharedPreferences.getString(settingKey, TextUtils.join(",", quickKeyIdDefaultOrder));
		String[] quickKeyIdsOrder = TextUtils.split(quickKeyIdsOrderValue, ",");

		ArrayList<QuickTextKey> orderedQuickTextKeys = new ArrayList<>(quickKeyIdsOrder.length);
		for (String keyId : quickKeyIdsOrder) {
			Iterator<QuickTextKey> iterator = quickTextKeys.iterator();
			while (iterator.hasNext()) {
				QuickTextKey nextQuickKey = iterator.next();
				if (nextQuickKey.getId().equals(keyId)) {
					orderedQuickTextKeys.add(nextQuickKey);
					iterator.remove();
					break;
				}
			}
		}

		//forcing at least one key
		if (orderedQuickTextKeys.size() == 0) orderedQuickTextKeys.add(quickTextKeys.get(0));

		return orderedQuickTextKeys;
	}

	@Override
	protected QuickTextKey createConcreteAddOn(Context askContext, Context context, String prefId, int nameResId, String description, int sortIndex, AttributeSet attrs) {
		final int popupKeyboardResId = attrs.getAttributeResourceValue(null, XML_POPUP_KEYBOARD_RES_ID_ATTRIBUTE, AddOn.INVALID_RES_ID);
		final int popupListTextResId = attrs.getAttributeResourceValue(null, XML_POPUP_LIST_TEXT_RES_ID_ATTRIBUTE, AddOn.INVALID_RES_ID);
		final int popupListOutputResId = attrs.getAttributeResourceValue(null, XML_POPUP_LIST_OUTPUT_RES_ID_ATTRIBUTE, AddOn.INVALID_RES_ID);
		final int popupListIconsResId = attrs.getAttributeResourceValue(null, XML_POPUP_LIST_ICONS_RES_ID_ATTRIBUTE, AddOn.INVALID_RES_ID);
		final int iconResId = attrs.getAttributeResourceValue(null, XML_ICON_RES_ID_ATTRIBUTE, AddOn.INVALID_RES_ID); // Maybe should make a default
		// icon
		final int keyLabelResId = attrs.getAttributeResourceValue(null, XML_KEY_LABEL_RES_ID_ATTRIBUTE, AddOn.INVALID_RES_ID);
		final int keyOutputTextResId = attrs.getAttributeResourceValue(null, XML_KEY_OUTPUT_TEXT_RES_ID_ATTRIBUTE, AddOn.INVALID_RES_ID);
		final int keyIconPreviewResId = attrs.getAttributeResourceValue(null, XML_ICON_PREVIEW_RES_ID_ATTRIBUTE, AddOn.INVALID_RES_ID);

		if (((popupKeyboardResId == AddOn.INVALID_RES_ID) && ((popupListTextResId == AddOn.INVALID_RES_ID) || (popupListOutputResId == AddOn.INVALID_RES_ID)))
				|| ((iconResId == AddOn.INVALID_RES_ID) && (keyLabelResId == AddOn.INVALID_RES_ID))
				|| (keyOutputTextResId == AddOn.INVALID_RES_ID)) {
			String detailMessage = String.format("Missing details for creating QuickTextKey! prefId %s, popupKeyboardResId: %d, popupListTextResId: %d, popupListOutputResId: %d, (iconResId: %d, keyLabelResId: %d), keyOutputTextResId: %d",
					prefId, popupKeyboardResId, popupListTextResId,
					popupListOutputResId, iconResId, keyLabelResId,
					keyOutputTextResId);

			throw new RuntimeException(detailMessage);
		}
		return new QuickTextKey(askContext, context, prefId, nameResId, popupKeyboardResId,
				popupListTextResId, popupListOutputResId, popupListIconsResId,
				iconResId, keyLabelResId, keyOutputTextResId,
				keyIconPreviewResId, description, sortIndex);
	}
}
