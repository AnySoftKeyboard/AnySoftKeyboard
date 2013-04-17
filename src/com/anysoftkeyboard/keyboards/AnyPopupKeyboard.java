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
import com.menny.android.anysoftkeyboard.R;

import java.util.HashSet;
import java.util.List;

public class AnyPopupKeyboard extends AnyKeyboard {

    private int mAdditionalWidth = 0;
    private boolean mOneKeyPressPopup = true;
    private static final HashSet<Character> msEmptySet = new HashSet<Character>(0);

    public AnyPopupKeyboard(Context askContext, Context context,//note: the context can be from a different package!
                            int xmlLayoutResId,
                            final KeyboardDimens keyboardDimens) {
        super(askContext, context, xmlLayoutResId, -1);
        loadKeyboard(keyboardDimens);
    }

    public AnyPopupKeyboard(Context askContext, CharSequence popupCharacters,
                            final KeyboardDimens keyboardDimens) {
        super(askContext, askContext, R.xml.popup);

        loadKeyboard(keyboardDimens);

        final float rowVerticalGap = keyboardDimens.getRowVerticalGap();
        final float keyHorizontalGap = keyboardDimens.getKeyHorizontalGap();

        List<Key> keys = getKeys();
        //now adding the popups
        final float y = rowVerticalGap;
        Key baseKey = keys.get(0);
        Row row = baseKey.row;
        baseKey.codes = new int[]{(int) popupCharacters.charAt(0)};
        baseKey.label = "" + popupCharacters.charAt(0);
        baseKey.edgeFlags |= EDGE_LEFT;
        float x = baseKey.width + row.defaultHorizontalGap;
        for (int popupCharIndex = 1; popupCharIndex < popupCharacters.length(); popupCharIndex++) {
            x += (keyHorizontalGap / 2);

            Key aKey = new AnyKey(row, keyboardDimens);
            aKey.codes = new int[]{(int) popupCharacters.charAt(popupCharIndex)};
            aKey.label = "" + popupCharacters.charAt(popupCharIndex);
            aKey.x = (int) x;
            aKey.width -= keyHorizontalGap;//the gap is on both sides
            aKey.y = (int) y;
            final int xOffset = (int) (aKey.width + row.defaultHorizontalGap + (keyHorizontalGap / 2));
            x += xOffset;
            mAdditionalWidth += xOffset;
            keys.add(aKey);
        }
        //adding edge flag to the last key
        keys.get(0).edgeFlags |= EDGE_LEFT;
        keys.get(keys.size() - 1).edgeFlags |= EDGE_RIGHT;
    }

    @Override
    public HashSet<Character> getSentenceSeparators() {
        return msEmptySet;
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
    public String getKeyboardName() {
        return null;
    }

    @Override
    public int getKeyboardIconResId() {
        return -1;
    }

    @Override
    public String getKeyboardPrefId() {
        return "keyboard_popup";
    }

    public boolean isOneKeyEventPopup() {
        return mOneKeyPressPopup;
    }

    public void setIsOneKeyEventPopup(boolean oneKey) {
        mOneKeyPressPopup = oneKey;
    }

    @Override
    protected void addGenericRows(Context askContext, Context context, int mode, KeyboardDimens keyboardDimens) {
        //no generic rows in popups, only in main keyboard
    }

    @Override
    protected boolean keyboardSupportShift() {
        return true;
    }
}
