package com.anysoftkeyboard.keyboards;

import android.content.Context;
import android.support.annotation.NonNull;

import com.anysoftkeyboard.addons.AddOn;
import com.menny.android.anysoftkeyboard.R;

import java.util.List;

public class PopupListKeyboard extends AnyPopupKeyboard {
    private final int mAdditionalWidth;

    public PopupListKeyboard(@NonNull AddOn keyboardAddOn, Context askContext, KeyboardDimens keyboardDimens, List<String> keysNames, List<String> keyValues, CharSequence name) {
        super(keyboardAddOn, askContext, askContext, R.xml.quick_text_list_popup, keyboardDimens, name);
        int rowWidth = 0;
        Key baseKey = getKeys().get(0);
        Row row = baseKey.row;
        //now adding the popups
        final float y = baseKey.y;
        final float keyHorizontalGap = row.defaultHorizontalGap;
        baseKey.mCodes = new int[]{0};
        baseKey.label = keysNames.get(0);
        baseKey.text = keyValues.get(0);
        float x = baseKey.width;
        AnyKey aKey = null;
        for (int entryIndex = 1; entryIndex < keysNames.size(); entryIndex++) {
            x += (keyHorizontalGap / 2);

            aKey = new AnyKey(row, keyboardDimens);
            aKey.mCodes = new int[]{0};
            aKey.mShiftedCodes = new int[]{0};
            aKey.label = keysNames.get(entryIndex);
            aKey.text = keyValues.get(entryIndex);
            aKey.x = (int) x;
            aKey.width = (int) (aKey.width - keyHorizontalGap);//the gap is on both sides
            aKey.y = (int) y;
            final int xOffset = (int) (aKey.width + keyHorizontalGap + (keyHorizontalGap / 2));
            x += xOffset;
            rowWidth += xOffset;
            getKeys().add(aKey);
        }
        //adding edge flag to the last key
        baseKey.edgeFlags = EDGE_LEFT;
        //this holds the last key
        if (aKey != null)
            aKey.edgeFlags = EDGE_RIGHT;
        else
            baseKey.edgeFlags |= EDGE_RIGHT;//adding another flag, since the baseKey is the only one in the row

        mAdditionalWidth = rowWidth;
    }

    @Override
    public int getMinWidth() {
        return super.getMinWidth() + mAdditionalWidth;
    }
}
