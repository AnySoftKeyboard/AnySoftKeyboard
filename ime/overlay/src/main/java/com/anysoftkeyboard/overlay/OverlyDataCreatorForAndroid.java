package com.anysoftkeyboard.overlay;

import static android.content.Context.CONTEXT_IGNORE_SECURITY;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.util.TypedValue;
import androidx.annotation.AttrRes;
import androidx.core.content.ContextCompat;
import com.anysoftkeyboard.base.utils.Logger;

public class OverlyDataCreatorForAndroid implements OverlyDataCreator {

  private static final OverlayData EMPTY = new InvalidOverlayData();

  private final Context mLocalContext;
  protected final OverlayDataImpl mCurrentOverlayData = new OverlayDataImpl();

  public OverlyDataCreatorForAndroid(Context localContext) {
    mLocalContext = localContext;
  }

  @Override
  public OverlayData createOverlayData(ComponentName remoteApp) {
    try {
      final ActivityInfo activityInfo =
          mLocalContext
              .getPackageManager()
              .getActivityInfo(remoteApp, PackageManager.GET_META_DATA);
      final Context context =
          mLocalContext.createPackageContext(remoteApp.getPackageName(), CONTEXT_IGNORE_SECURITY);

      context.setTheme(activityInfo.getThemeResource());
      fetchRemoteColors(context);

      Logger.d(
          "OverlyDataCreatorForAndroid",
          "For component %s we fetched %s",
          remoteApp,
          mCurrentOverlayData);

      return mCurrentOverlayData;
    } catch (Exception e) {
      Logger.w("OverlyDataCreatorForAndroid", e, "Failed to fetch colors for %s", remoteApp);
      return EMPTY;
    }
  }

  protected void fetchRemoteColors(Context context) {
    // ensuring text colors are completely opaque by apply Color.BLACK
    final TypedValue typedValue = new TypedValue();
    mCurrentOverlayData.setPrimaryColor(
        Color.BLACK
            | getColorFromThemeAttribute(
                context, typedValue, android.R.attr.colorPrimary, Color.BLACK));
    mCurrentOverlayData.setPrimaryDarkColor(
        Color.BLACK
            | getColorFromThemeAttribute(
                context,
                typedValue,
                android.R.attr.colorPrimaryDark,
                mCurrentOverlayData.getPrimaryColor()));
    mCurrentOverlayData.setAccentColor(
        Color.BLACK
            | getColorFromThemeAttribute(
                context,
                typedValue,
                android.R.attr.colorAccent,
                mCurrentOverlayData.getPrimaryColor()));
    mCurrentOverlayData.setPrimaryTextColor(
        Color.BLACK
            | getColorFromThemeAttribute(
                context, typedValue, android.R.attr.textColorPrimary, Color.BLACK));
    mCurrentOverlayData.setSecondaryTextColor(
        Color.BLACK
            | getColorFromThemeAttribute(
                context,
                typedValue,
                android.R.attr.textColorSecondary,
                mCurrentOverlayData.getPrimaryTextColor()));
  }

  private static int getColorFromThemeAttribute(
      Context context, TypedValue typedValue, @AttrRes int attr, int defaultColor) {
    if (context.getTheme().resolveAttribute(attr, typedValue, true)) {
      if (typedValue.type == TypedValue.TYPE_REFERENCE) {
        return ContextCompat.getColor(context, typedValue.resourceId);
      } else {
        return typedValue.data;
      }
    } else {
      return defaultColor;
    }
  }

  private static class InvalidOverlayData extends OverlayDataImpl {
    @Override
    public boolean isValid() {
      return false;
    }
  }

  public static class Light extends OverlyDataCreatorForAndroid {

    public Light(Context localContext) {
      super(localContext);
    }

    @Override
    protected void fetchRemoteColors(Context context) {
      final TypedValue typedValue = new TypedValue();
      mCurrentOverlayData.setPrimaryColor(
          getColorFromThemeAttribute(context, typedValue, android.R.attr.colorPrimary, 0));
      mCurrentOverlayData.setPrimaryDarkColor(
          getColorFromThemeAttribute(
              context,
              typedValue,
              android.R.attr.colorPrimaryDark,
              mCurrentOverlayData.getPrimaryColor()));
      // these will be static
      mCurrentOverlayData.setAccentColor(mCurrentOverlayData.getPrimaryColor());
      mCurrentOverlayData.setPrimaryTextColor(Color.WHITE);
      mCurrentOverlayData.setSecondaryTextColor(Color.LTGRAY);
    }
  }
}
