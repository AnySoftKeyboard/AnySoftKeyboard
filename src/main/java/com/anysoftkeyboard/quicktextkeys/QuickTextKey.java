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

package com.anysoftkeyboard.quicktextkeys;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import com.anysoftkeyboard.addons.AddOnImpl;

/**
 * @author Malcolm
 */
public class QuickTextKey extends AddOnImpl {

    private int mPopupKeyboardResId;
    private String[] mPopupListNames;
    private String[] mPopupListValues;
    private int[] mPopupListIconResIds;
    private String mKeyOutputText;

    private String mKeyLabel;
    private int mKeyIconResId;
    private int mIconPreviewResId;

    public QuickTextKey(Context askContext, Context packageContext, String id, int nameResId, int popupKeyboardResId,
                        int popupListNamesResId, int popupListValuesResId, int popupListIconsResId,
                        int keyIconResId, int keyLabelResId, int keyOutputTextResId, int iconPreviewResId,
                        String description, int sortIndex) {
        super(askContext, packageContext, id, nameResId, description, sortIndex);

        Resources resources = packageContext.getResources();

        this.mPopupKeyboardResId = popupKeyboardResId;
        if (popupKeyboardResId == INVALID_RES_ID) {
            this.mPopupListNames = resources.getStringArray(popupListNamesResId);
            this.mPopupListValues = resources.getStringArray(popupListValuesResId);

            if (popupListIconsResId != INVALID_RES_ID) {
                TypedArray arr = resources.obtainTypedArray(popupListIconsResId);
                mPopupListIconResIds = new int[arr.length()];
                for (int pos = 0; pos < mPopupListIconResIds.length; pos++) {
                    mPopupListIconResIds[pos] = arr.getResourceId(pos, INVALID_RES_ID);
                }
                arr.recycle();
            }
        }
        this.mKeyIconResId = keyIconResId;
        this.mKeyLabel = keyLabelResId != INVALID_RES_ID ? resources.getString(keyLabelResId) : null;
        this.mKeyOutputText = keyOutputTextResId != INVALID_RES_ID ? resources.getString(keyOutputTextResId) : null;
        this.mIconPreviewResId = iconPreviewResId;
    }

    public boolean isPopupKeyboardUsed() {
        return mPopupKeyboardResId != INVALID_RES_ID;
    }

    public int getPopupKeyboardResId() {
        return mPopupKeyboardResId;
    }

    public String[] getPopupListNames() {
        return mPopupListNames;
    }

    public String[] getPopupListValues() {
        return mPopupListValues;
    }

    public int[] getPopupListIconResIds() {
        return mPopupListIconResIds;
    }

    public String getKeyOutputText() {
        return mKeyOutputText;
    }

    public String getKeyLabel() {
        return mKeyLabel;
    }

    public int getKeyIconResId() {
        return mKeyIconResId;
    }

    public int getIconPreviewResId() {
        return mIconPreviewResId;
    }
}