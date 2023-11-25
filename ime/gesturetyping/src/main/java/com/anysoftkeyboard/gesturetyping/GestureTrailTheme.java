package com.anysoftkeyboard.gesturetyping;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.annotation.VisibleForTesting;
import com.anysoftkeyboard.addons.AddOn;
import com.anysoftkeyboard.base.utils.Logger;

public class GestureTrailTheme {
  final int maxTrailLength;
  @ColorInt @VisibleForTesting final int mTrailStartColor;
  @ColorInt @VisibleForTesting final int mTrailEndColor;
  @VisibleForTesting final float mStartStrokeSize;
  @VisibleForTesting final float mEndStrokeSize;
  @VisibleForTesting final float mTrailFraction;

  public GestureTrailTheme(
      @ColorInt int trailStartColor,
      @ColorInt int trailEndColor,
      float startStrokeSize,
      float endStrokeSize,
      int maxTrailLength) {
    this.mTrailStartColor = trailStartColor;
    this.mTrailEndColor = trailEndColor;
    this.mStartStrokeSize = startStrokeSize;
    this.mEndStrokeSize = endStrokeSize;
    this.maxTrailLength = maxTrailLength;
    this.mTrailFraction = 1f / ((float) maxTrailLength);
  }

  public static GestureTrailTheme fromThemeResource(
      @NonNull Context askContext,
      @NonNull Context themeContext,
      @NonNull AddOn.AddOnResourceMapping mapper,
      @StyleRes int resId) {

    // filling in the defaults
    TypedArray defaultValues =
        askContext.obtainStyledAttributes(
            R.style.AnyKeyboardGestureTrailTheme, R.styleable.GestureTypingTheme);
    int maxTrailLength =
        defaultValues.getInt(R.styleable.GestureTypingTheme_gestureTrailMaxSectionsLength, 32);
    @ColorInt
    int trailStartColor =
        defaultValues.getColor(R.styleable.GestureTypingTheme_gestureTrailStartColor, Color.BLUE);
    @ColorInt
    int trailEndColor =
        defaultValues.getColor(R.styleable.GestureTypingTheme_gestureTrailEndColor, Color.BLACK);
    float startStrokeSize =
        defaultValues.getDimension(R.styleable.GestureTypingTheme_gestureTrailStartStrokeSize, 0f);
    float endStrokeSize =
        defaultValues.getDimension(R.styleable.GestureTypingTheme_gestureTrailEndStrokeSize, 0f);
    defaultValues.recycle();

    final int[] remoteStyleableArray =
        mapper.getRemoteStyleableArrayFromLocal(R.styleable.GestureTypingTheme);
    TypedArray a = themeContext.obtainStyledAttributes(resId, remoteStyleableArray);
    final int resolvedAttrsCount = a.getIndexCount();
    for (int attrIndex = 0; attrIndex < resolvedAttrsCount; attrIndex++) {
      final int remoteIndex = a.getIndex(attrIndex);
      try {
        int localAttrId = mapper.getLocalAttrId(remoteStyleableArray[remoteIndex]);
        if (localAttrId == R.attr.gestureTrailMaxSectionsLength)
          maxTrailLength = a.getInt(remoteIndex, maxTrailLength);
        else if (localAttrId == R.attr.gestureTrailStartColor)
          trailStartColor = a.getColor(remoteIndex, trailStartColor);
        else if (localAttrId == R.attr.gestureTrailEndColor)
          trailEndColor = a.getColor(remoteIndex, trailEndColor);
        else if (localAttrId == R.attr.gestureTrailStartStrokeSize)
          startStrokeSize = a.getDimension(remoteIndex, startStrokeSize);
        else if (localAttrId == R.attr.gestureTrailEndStrokeSize)
          endStrokeSize = a.getDimension(remoteIndex, endStrokeSize);
      } catch (Exception e) {
        Logger.w("ASK_GESTURE_THEME", "Got an exception while reading gesture theme data", e);
      }
    }
    a.recycle();

    return new GestureTrailTheme(
        trailStartColor, trailEndColor, startStrokeSize, endStrokeSize, maxTrailLength);
  }

  private static int shiftColor(float start, float end, float fraction) {
    return (int) (start - (start - end) * fraction);
  }

  public float strokeSizeFor(int elementIndex) {
    return mStartStrokeSize - (mStartStrokeSize - mEndStrokeSize) * elementIndex * mTrailFraction;
  }

  @ColorInt
  public int strokeColorFor(int elementIndex) {
    final float fractionShift = elementIndex * mTrailFraction;
    final int r = shiftColor(Color.red(mTrailStartColor), Color.red(mTrailEndColor), fractionShift);
    final int g =
        shiftColor(Color.green(mTrailStartColor), Color.green(mTrailEndColor), fractionShift);
    final int b =
        shiftColor(Color.blue(mTrailStartColor), Color.blue(mTrailEndColor), fractionShift);
    final int a =
        shiftColor(Color.alpha(mTrailStartColor), Color.alpha(mTrailEndColor), fractionShift);
    return Color.argb(a, r, g, b);
  }
}
