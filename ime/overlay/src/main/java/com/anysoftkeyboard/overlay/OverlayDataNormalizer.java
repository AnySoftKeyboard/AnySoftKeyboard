package com.anysoftkeyboard.overlay;

import android.content.ComponentName;
import android.graphics.Color;
import androidx.annotation.ColorInt;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

public class OverlayDataNormalizer implements OverlyDataCreator {

  private static final int GRAY_LUM = luminance(Color.GRAY);

  private final OverlyDataCreator mOriginal;
  private final int mRequiredTextColorDiff;

  public OverlayDataNormalizer(
      OverlyDataCreator original, @IntRange(from = 1, to = 250) int textColorDiff) {
    mOriginal = original;
    mRequiredTextColorDiff = textColorDiff;
  }

  @NonNull
  public static OverlayData normalize(
      @NonNull OverlayData original,
      @IntRange(from = 1, to = 250) int textColorDiff,
      @ColorInt int primaryColor,
      @ColorInt int primaryTextColor) {
    final int backgroundLuminance = luminance(primaryColor);
    final int diff = backgroundLuminance - luminance(primaryTextColor);
    if (textColorDiff > Math.abs(diff) || !original.isValid()) {
      if (backgroundLuminance > GRAY_LUM) {
        // closer to white, text will be black
        return new OverlayDataNormalized(original, Color.BLACK, Color.DKGRAY);
      } else {
        return new OverlayDataNormalized(original, Color.WHITE, Color.LTGRAY);
      }
    }
    return original;
  }

  @Override
  public OverlayData createOverlayData(ComponentName remoteApp) {
    final OverlayData original = mOriginal.createOverlayData(remoteApp);
    return normalize(
        original,
        mRequiredTextColorDiff,
        original.getPrimaryColor(),
        original.getPrimaryTextColor());
  }

  /** Code taken (mostly) from AOSP Color class. */
  @VisibleForTesting
  static int luminance(@ColorInt int color) {
    double r = Color.red(color) * 0.2126;
    double g = Color.green(color) * 0.7152;
    double b = Color.blue(color) * 0.0722;

    return (int) Math.ceil(r + g + b);
  }

  private static class OverlayDataNormalized implements OverlayData {

    private final OverlayData mOriginal;
    private final int mPrimaryTextColor;
    private final int mSecondaryTextColor;

    private OverlayDataNormalized(
        OverlayData original, @ColorInt int primaryTextColor, @ColorInt int secondaryTextColor) {
      mOriginal = original;
      mPrimaryTextColor = primaryTextColor;
      mSecondaryTextColor = secondaryTextColor;
    }

    @Override
    public int getPrimaryTextColor() {
      return mPrimaryTextColor;
    }

    @Override
    public int getSecondaryTextColor() {
      return mSecondaryTextColor;
    }

    @Override
    public int getAccentColor() {
      return mOriginal.getAccentColor();
    }

    @Override
    public int getPrimaryColor() {
      return mOriginal.getPrimaryColor();
    }

    @Override
    public int getPrimaryDarkColor() {
      return mOriginal.getPrimaryDarkColor();
    }

    @Override
    public boolean isValid() {
      return (getPrimaryColor() != mPrimaryTextColor)
          && (getPrimaryDarkColor() != mPrimaryTextColor);
    }
  }
}
