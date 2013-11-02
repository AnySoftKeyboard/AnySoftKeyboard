package com.anysoftkeyboard.keyboards;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.SparseIntArray;
import android.util.TypedValue;

import com.anysoftkeyboard.utils.Log;
import com.menny.android.anysoftkeyboard.AnyApplication;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by menny on 10/29/13.
 */
public class KeyboardSupport {
    private static final String TAG = "KeyboardSupport";

    public static int[] parseCSV(String value) {
        int count = 0;
        int lastIndex = 0;
        if (value.length() > 0) {
            count++;
            while ((lastIndex = value.indexOf(",", lastIndex + 1)) > 0) {
                count++;
            }
        }
        int[] values = new int[count];
        count = 0;
        StringTokenizer st = new StringTokenizer(value, ",");
        while (st.hasMoreTokens()) {
            String nextToken = st.nextToken();
            try {
                // Issue 395
                // default behavior
                if (nextToken.length() != 1) {
                    values[count++] = Integer.parseInt(nextToken);
                } else {
                    // length == 1, assume a char!
                    values[count++] = (int) nextToken.charAt(0);
                }
            } catch (NumberFormatException nfe) {
                Log.e(TAG, "Error parsing keycodes " + value);
            }
        }
        return values;
    }

    public static void updateDrawableBounds(Drawable icon) {
        if (icon == null) return;
        icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
    }

    public static int[] getKeyCodesFromTypedArray(TypedArray typedArray, int index) {
        TypedValue codesValue = new TypedValue();
        typedArray.getValue(index, codesValue);

        if (codesValue.type == TypedValue.TYPE_INT_DEC || codesValue.type == TypedValue.TYPE_INT_HEX) {
            return new int[]{ codesValue.data };
        } else if (codesValue.type == TypedValue.TYPE_STRING) {
            return parseCSV(codesValue.string.toString());
        } else {
            return null;
        }

    }

    public static int[] createBackwardCompatibleStyleable(int[] localStyleableArray, Context localContext, Context remoteContext, SparseIntArray attributeIdMap, boolean reverseMapping) {
        final String remotePackageName = remoteContext.getPackageName();
        if (localContext.getPackageName().equals(remotePackageName)) {
            Log.d(TAG, "This is a local context ("+remotePackageName+"), optimization will be done.");
            //optimization
            if (attributeIdMap != null) {
                for(int attrId : localStyleableArray) {
                    attributeIdMap.put(attrId, attrId);
                }
            }
            return localStyleableArray;
        }
        final Resources localRes = localContext.getResources();
        final Resources remoteRes = remoteContext.getResources();
        List<Integer> styleableIdList = new ArrayList<Integer>(localStyleableArray.length);
        for(int attrId : localStyleableArray) {
            final String attributeName = localRes.getResourceEntryName(attrId);
            final boolean isAndroidAttribute = localRes.getResourcePackageName(attrId).equals("android");
            final int remoteAttrId = remoteRes.getIdentifier(attributeName, "attr", isAndroidAttribute? "android" : remotePackageName);
            Log.d(TAG, "attr "+attributeName+", local id "+attrId+", remote id "+remoteAttrId);
            if (remoteAttrId != 0) {
                if (attributeIdMap != null) {
                    if (reverseMapping)
                        attributeIdMap.put(attrId, remoteAttrId);
                    else
                        attributeIdMap.put(remoteAttrId, attrId);
                }
                styleableIdList.add(remoteAttrId);
            }
        }
        final int[] remoteStyleable = new int[styleableIdList.size()];
        for(int i=0; i<remoteStyleable.length; i++)
            remoteStyleable[i] = styleableIdList.get(i);

        return remoteStyleable;
    }

    public static int getKeyHeightFromHeightCode(KeyboardDimens keyboardDimens, int heightCode, int orientation) {
        int height;
        switch (heightCode) {
            case 0:
                height = 0;
                break;
            case -2:
                height = keyboardDimens.getSmallKeyHeight();
                break;
            case -3:
                height = keyboardDimens.getLargeKeyHeight();
                break;
            default:// -1
                height = keyboardDimens.getNormalKeyHeight();
                break;
        }
        if (orientation == Configuration.ORIENTATION_LANDSCAPE)
            height = (int) (height * AnyApplication.getConfig()
                    .getKeysHeightFactorInLandscape());
        else
            height = (int) (height * AnyApplication.getConfig().getKeysHeightFactorInPortrait());

        return height;
    }
}
