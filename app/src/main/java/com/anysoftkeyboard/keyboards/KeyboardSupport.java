package com.anysoftkeyboard.keyboards;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.TypedValue;

import com.anysoftkeyboard.base.utils.Logger;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

import java.util.StringTokenizer;

public class KeyboardSupport {
    private static final String TAG = "KeyboardSupport";

    private static int[] parseCSV(String value) {
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
                Logger.e(TAG, "Error parsing keycodes " + value);
            }
        }
        return values;
    }

    public static void updateDrawableBounds(Drawable icon) {
        if (icon == null) return;
        icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
    }

    private static final TypedValue codesValue = new TypedValue();

    @NonNull
    public static int[] getKeyCodesFromTypedArray(TypedArray typedArray, int index) {
        typedArray.getValue(index, codesValue);

        if (codesValue.type == TypedValue.TYPE_INT_DEC || codesValue.type == TypedValue.TYPE_INT_HEX) {
            return new int[]{codesValue.data};
        } else if (codesValue.type == TypedValue.TYPE_STRING) {
            return parseCSV(codesValue.coerceToString().toString());
        } else {
            Logger.w(TAG, "Unknown mCodes values!");
            return new int[0];
        }
    }

    public static int getKeyHeightFromHeightCode(KeyboardDimens keyboardDimens, int heightCode, float heightFactor) {
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

        return (int) (height * heightFactor);
    }

    public static float getKeyboardHeightFactor(Context context) {
        final boolean landscape = context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        return AnyApplication.prefs(context).getParsedString(
                landscape ? R.string.settings_key_landscape_keyboard_height_factor : R.string.settings_key_portrait_keyboard_height_factor,
                landscape ? R.string.settings_default_landscape_keyboard_height_factor : R.string.settings_default_portrait_keyboard_height_factor, Float::parseFloat).map(KeyboardSupport::zoomFactorLimitation).blockingFirst();
    }

    private static float zoomFactorLimitation(float value) {
        if (value > 2.0f) return 2.0f;
        if (value < 0.2f) return 0.2f;
        return value;
    }
}
