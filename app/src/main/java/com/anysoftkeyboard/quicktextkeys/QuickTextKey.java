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
import android.support.annotation.Nullable;
import com.anysoftkeyboard.addons.AddOnImpl;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Arrays;
import java.util.List;

/** @author Malcolm */
public class QuickTextKey extends AddOnImpl {

    private int mPopupKeyboardResId;
    private String[] mPopupListNames;
    private String[] mPopupListValues;
    private int[] mPopupListIconResIds;
    private CharSequence mKeyOutputText;

    private CharSequence mKeyLabel;

    @SuppressFBWarnings(
            value = "URF_UNREAD_FIELD",
            justification = "At some point, we might want to do that")
    @SuppressWarnings("UnusedVariable")
    private int mKeyIconResId;

    @SuppressFBWarnings(
            value = "URF_UNREAD_FIELD",
            justification = "At some point, we might want to do that")
    @SuppressWarnings("UnusedVariable")
    private int mIconPreviewResId;

    public QuickTextKey(
            Context askContext,
            Context packageContext,
            int apiVersion,
            CharSequence id,
            CharSequence name,
            int popupKeyboardResId,
            int popupListNamesResId,
            int popupListValuesResId,
            int popupListIconsResId,
            int keyIconResId,
            CharSequence keyLabel,
            CharSequence keyOutput,
            int iconPreviewResId,
            boolean isHidden,
            CharSequence description,
            int sortIndex) {
        super(askContext, packageContext, apiVersion, id, name, description, isHidden, sortIndex);

        Resources resources = packageContext.getResources();

        this.mPopupKeyboardResId = popupKeyboardResId;
        if (popupKeyboardResId == INVALID_RES_ID) {
            this.mPopupListNames = getStringArrayFromNamesResId(popupListNamesResId, resources);
            this.mPopupListValues = getStringArrayFromValuesResId(popupListValuesResId, resources);

            if (popupListIconsResId != INVALID_RES_ID) {
                TypedArray arr = resources.obtainTypedArray(popupListIconsResId);
                mPopupListIconResIds = new int[arr.length()];
                for (int pos = 0; pos < mPopupListIconResIds.length; pos++) {
                    mPopupListIconResIds[pos] = arr.getResourceId(pos, INVALID_RES_ID);
                }
                arr.recycle();
            }
        }
        mKeyIconResId = keyIconResId;
        mKeyLabel = keyLabel;
        mKeyOutputText = keyOutput;
        mIconPreviewResId = iconPreviewResId;
    }

    protected String[] getStringArrayFromValuesResId(
            int popupListValuesResId, Resources resources) {
        return resources.getStringArray(popupListValuesResId);
    }

    protected String[] getStringArrayFromNamesResId(int popupListNamesResId, Resources resources) {
        return resources.getStringArray(popupListNamesResId);
    }

    public boolean isPopupKeyboardUsed() {
        return mPopupKeyboardResId != INVALID_RES_ID;
    }

    public int getPopupKeyboardResId() {
        return mPopupKeyboardResId;
    }

    public List<String> getPopupListNames() {
        return Arrays.asList(mPopupListNames);
    }

    public List<String> getPopupListValues() {
        return Arrays.asList(mPopupListValues);
    }

    public CharSequence getKeyOutputText() {
        return mKeyOutputText;
    }

    @Nullable
    public CharSequence getKeyLabel() {
        return mKeyLabel;
    }
}
