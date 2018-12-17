package com.anysoftkey.overlay;

import static android.content.Context.CONTEXT_IGNORE_SECURITY;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;

import com.anysoftkeyboard.base.utils.Logger;

public class OverlyDataCreatorForAndroid implements OverlyDataCreator {

    public static final boolean OS_SUPPORT_FOR_ACCENT = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;

    private static final OverlayData EMPTY = new InvalidOverlayData();

    private final Context mLocalContext;
    private final OverlayData mCurrentOverlayData = new OverlayData();

    public OverlyDataCreatorForAndroid(Context localContext) {
        mLocalContext = localContext;
    }

    @Override
    public OverlayData createOverlayData(ComponentName remoteApp) {
        if (!OS_SUPPORT_FOR_ACCENT) {
            return EMPTY;
        }

        try {
            final ActivityInfo activityInfo = mLocalContext.getPackageManager().getActivityInfo(remoteApp, PackageManager.GET_META_DATA);
            final Context context = mLocalContext.createPackageContext(remoteApp.getPackageName(), CONTEXT_IGNORE_SECURITY);

            context.setTheme(activityInfo.getThemeResource());
            fetchRemoteColors(mCurrentOverlayData, context);

            Logger.d("OverlyDataCreatorForAndroid", "For component %s we fetched %s", remoteApp, mCurrentOverlayData);

            return mCurrentOverlayData;
        } catch (Exception e) {
            Logger.w("OverlyDataCreatorForAndroid", e, "Failed to fetch colors for %s", remoteApp);
            return EMPTY;
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private static void fetchRemoteColors(OverlayData data, Context context) {
        data.setPrimaryColor(getColorFromThemeAttribute(context, new TypedValue(), android.R.attr.colorPrimary, 0));
        data.setPrimaryDarkColor(getColorFromThemeAttribute(context, new TypedValue(), android.R.attr.colorPrimaryDark, data.getPrimaryColor()));
        data.setPrimaryTextColor(getColorFromThemeAttribute(context, new TypedValue(), android.R.attr.textColorPrimary, 0));
    }

    private static int getColorFromThemeAttribute(Context context, TypedValue typedValue, @AttrRes int attr, int defaultColor) {
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

    private static class InvalidOverlayData extends OverlayData {
        @Override
        public boolean isValid() {
            return false;
        }
    }
}
