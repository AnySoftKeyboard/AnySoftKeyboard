package com.anysoftkeyboard.keyboards;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import androidx.annotation.NonNull;
import com.anysoftkeyboard.base.utils.Logger;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;
import io.reactivex.Observable;
import java.util.StringTokenizer;

public class KeyboardSupport {
  private static final String TAG = "ASKKbdSupport";

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

  @NonNull public static int[] getKeyCodesFromTypedArray(TypedArray typedArray, int index) {
    typedArray.getValue(index, codesValue);

    if (codesValue.type == TypedValue.TYPE_INT_DEC || codesValue.type == TypedValue.TYPE_INT_HEX) {
      return new int[] {codesValue.data};
    } else if (codesValue.type == TypedValue.TYPE_STRING) {
      return parseCSV(codesValue.coerceToString().toString());
    } else {
      Logger.w(TAG, "Unknown mCodes values!");
      return new int[0];
    }
  }

  public static int getKeyHeightFromHeightCode(
      KeyboardDimens keyboardDimens, int heightCode, float heightFactor) {
    int height;
    switch (heightCode) {
      case -1:
        height = keyboardDimens.getNormalKeyHeight();
        break;
      case -2:
        height = keyboardDimens.getSmallKeyHeight();
        break;
      case -3:
        height = keyboardDimens.getLargeKeyHeight();
        break;
      default:
        height = heightCode >= 0 ? heightCode : keyboardDimens.getNormalKeyHeight();
        break;
    }

    return (int) (height * heightFactor);
  }

  public static Observable<Float> getKeyboardHeightFactor(Context context) {
    final boolean landscape =
        context.getResources().getConfiguration().orientation
            == Configuration.ORIENTATION_LANDSCAPE;
    return AnyApplication.prefs(context)
        .getInteger(
            landscape
                ? R.string.settings_key_zoom_percent_in_landscape
                : R.string.settings_key_zoom_percent_in_portrait,
            landscape
                ? R.integer.settings_default_zoom_percent_in_landscape
                : R.integer.settings_default_zoom_percent_in_portrait)
        .asObservable()
        .map(percent -> ((float) percent) / 100f);
  }
}
