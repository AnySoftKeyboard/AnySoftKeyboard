package com.anysoftkey.overlay;

import static android.content.Context.CONTEXT_IGNORE_SECURITY;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.annotation.VisibleForTesting;
import android.util.TypedValue;

public class OverlyDataCreatorForAndroid implements OverlyDataCreator {

    public static final boolean OS_SUPPORT_FOR_ACCENT = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;

    @VisibleForTesting
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    static final int[] APP_COLORS_ATTRS = {android.R.attr.colorPrimary, android.R.attr.colorPrimaryDark, android.R.attr.textColorPrimary};
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

            fetchRemoteColors(mCurrentOverlayData, context, activityInfo.getThemeResource());

            return mCurrentOverlayData;
        } catch (Exception e) {
            return EMPTY;
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private static void fetchRemoteColors(OverlayData data, Context context, int themeId) {
        TypedArray a = context.obtainStyledAttributes(themeId, APP_COLORS_ATTRS);
        try {
            final Resources resources = context.getResources();
            data.setPrimaryColor(getColorFromStyledAttribute(a, 0, resources, 0));
            data.setPrimaryDarkColor(getColorFromStyledAttribute(a, 1, resources, data.getPrimaryColor()));
            data.setPrimaryTextColor(getColorFromStyledAttribute(a, 2, resources, 0));
        } finally {
            a.recycle();
        }
    }

    private static int getColorFromStyledAttribute(TypedArray a, int index, Resources resources, int defaultColor) {
        final TypedValue typedValue = a.peekValue(index);
        if (typedValue.type == TypedValue.TYPE_REFERENCE) {
            return resources.getColor(typedValue.resourceId);
        } else {
            return a.getColor(index, defaultColor);
        }
    }

    private static class InvalidOverlayData extends OverlayData {
        @Override
        public boolean isValid() {
            return false;
        }
    }
}
