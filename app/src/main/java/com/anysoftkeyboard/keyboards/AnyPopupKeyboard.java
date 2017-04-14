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
import android.support.annotation.Nullable;

import com.anysoftkeyboard.addons.AddOn;
import com.anysoftkeyboard.keyboardextensions.KeyboardExtension;
import com.menny.android.anysoftkeyboard.R;

import java.util.List;

public class AnyPopupKeyboard extends AnyKeyboard {

    private int mAdditionalWidth = 0;
    private static final char[] EMPTY_CHAR_ARRAY = new char[0];
    private final CharSequence mKeyboardName;

    public AnyPopupKeyboard(@NonNull AddOn keyboardAddOn, Context askContext, Context context,//note: the context can be from a different package!
                            int xmlLayoutResId,
                            final KeyboardDimens keyboardDimens,
                            CharSequence keyboardName) {
        super(keyboardAddOn, askContext, context, xmlLayoutResId, KEYBOARD_ROW_MODE_NORMAL);
        mKeyboardName = keyboardName;
        loadKeyboard(keyboardDimens);
    }

    public AnyPopupKeyboard(@NonNull AddOn keyboardAddOn, @NonNull Context askContext, CharSequence popupCharacters,
                            final KeyboardDimens keyboardDimens,
                            String keyboardName) {
        super(keyboardAddOn, askContext, askContext, getPopupLayout(popupCharacters));
        mKeyboardName = keyboardName;
        loadKeyboard(keyboardDimens);

        final int rowsCount = getPopupRowsCount(popupCharacters);
        final int keysPerRow = (int) Math.ceil((float) popupCharacters.length() / (float) rowsCount);

        List<Key> keys = getKeys();
        for (int rowIndex = rowsCount - 1; rowIndex >= 0; rowIndex--) {
            int baseKeyIndex = keys.size() - rowIndex - 1;
            addPopupKeysToList(baseKeyIndex, keyboardDimens, keys, popupCharacters, rowIndex * keysPerRow, keysPerRow);
        }
    }

    private void addPopupKeysToList(int baseKeyIndex, KeyboardDimens keyboardDimens, List<Key> keys, CharSequence popupCharacters, int characterOffset, int keysPerRow) {
        int rowWidth = 0;
        AnyKey baseKey = (AnyKey) keys.get(baseKeyIndex);
        Row row = baseKey.row;
        //now adding the popups
        final float y = baseKey.y;
        final float keyHorizontalGap = row.defaultHorizontalGap;
        char popupCharacter = popupCharacters.charAt(characterOffset);
        baseKey.mCodes = new int[]{(int) popupCharacter};
        baseKey.label = Character.toString(popupCharacter);
        char upperCasePopupCharacter = Character.toUpperCase(popupCharacter);
        baseKey.mShiftedCodes = new int[]{(int) upperCasePopupCharacter};
        float x = baseKey.width;
        AnyKey aKey = null;
        for (int popupCharIndex = characterOffset + 1;
             popupCharIndex < characterOffset + keysPerRow && popupCharIndex < popupCharacters.length();
             popupCharIndex++) {
            x += (keyHorizontalGap / 2);

            aKey = new AnyKey(row, keyboardDimens);
            popupCharacter = popupCharacters.charAt(popupCharIndex);
            aKey.mCodes = new int[]{(int) popupCharacter};
            aKey.label = Character.toString(popupCharacter);
            upperCasePopupCharacter = Character.toUpperCase(popupCharacter);
            aKey.mShiftedCodes = new int[]{(int) upperCasePopupCharacter};
            aKey.x = (int) x;
            aKey.width -= keyHorizontalGap;//the gap is on both sides
            aKey.y = (int) y;
            final int xOffset = (int) (aKey.width + keyHorizontalGap + (keyHorizontalGap / 2));
            x += xOffset;
            rowWidth += xOffset;
            keys.add(baseKeyIndex, aKey);
        }
        //adding edge flag to the last key
        baseKey.edgeFlags = EDGE_LEFT;
        //this holds the last key
        if (aKey != null)
            aKey.edgeFlags = EDGE_RIGHT;
        else
            baseKey.edgeFlags |= EDGE_RIGHT;//adding another flag, since the baseKey is the only one in the row

        mAdditionalWidth = Math.max(rowWidth, mAdditionalWidth);
    }

    private static int getPopupLayout(CharSequence popupCharacters) {
        switch (getPopupRowsCount(popupCharacters)) {
            case 1:
                return R.xml.popup_one_row;
            case 2:
                return R.xml.popup_two_rows;
            case 3:
                return R.xml.popup_three_rows;
            default:
                throw new RuntimeException("AnyPopupKeyboard supports 1, 2, and 3 rows only!");
        }
    }

    private static int getPopupRowsCount(CharSequence popupCharacters) {
        final int count = popupCharacters.length();
        if (count <= 8)
            return 1;
        if (count <= 16)
            return 2;
        else
            return 3;
    }


    @Override
    public char[] getSentenceSeparators() {
        return EMPTY_CHAR_ARRAY;
    }

    @Override
    public int getMinWidth() {
        return super.getMinWidth() + mAdditionalWidth;
    }

    @Override
    public String getDefaultDictionaryLocale() {
        return null;
    }

    @Override
    public CharSequence getKeyboardName() {
        return mKeyboardName;
    }

    @Override
    public int getKeyboardIconResId() {
        return -1;
    }

    @NonNull
    @Override
    public CharSequence getKeyboardId() {
        return "keyboard_popup";
    }

    @Override
    protected void addGenericRows(@NonNull KeyboardDimens keyboardDimens, @Nullable KeyboardExtension topRowPlugin, @NonNull KeyboardExtension bottomRowPlugin) {
        //no generic rows in popups, only in main keyboard
    }

    @Override
    public boolean keyboardSupportShift() {
        //forcing this, so the mParent keyboard will determine the shift value
        return true;
    }

    public void mirrorKeys() {
        /* how to mirror?
        width = 55
        [0..15] [20..35] [40..55]
        phase 1: multiple by -1
        [0] [-20] [-40]
        phase 2: add keyboard width
        [55] [35] [15]
        phase 3: subtracting the key's width
        [40] [20] [0]
        cool?
         */
        final int keyboardWidth = getMinWidth();
        for (Key k : getKeys()) {
            k.x = k.x * (-1);//phase 1
            k.x += keyboardWidth;//phase 2
            k.x -= k.width;//phase 3
        }
    }
}
