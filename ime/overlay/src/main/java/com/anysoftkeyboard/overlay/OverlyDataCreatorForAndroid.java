package com.anysoftkeyboard.overlay;

import static android.content.Context.CONTEXT_IGNORE_SECURITY;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.util.TypedValue;
import androidx.annotation.AttrRes;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import com.anysoftkeyboard.base.utils.Logger;

public class OverlyDataCreatorForAndroid implements OverlyDataCreator {

    public static final boolean OS_SUPPORT_FOR_ACCENT =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;

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
            final ActivityInfo activityInfo =
                    mLocalContext
                            .getPackageManager()
                            .getActivityInfo(remoteApp, PackageManager.GET_META_DATA);
            final Context context =
                    mLocalContext.createPackageContext(
                            remoteApp.getPackageName(), CONTEXT_IGNORE_SECURITY);

            context.setTheme(activityInfo.getThemeResource());
            fetchRemoteColors(mCurrentOverlayData, context);

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

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    protected void fetchRemoteColors(OverlayData data, Context context) {
        // ensuring text colors are completely opaque by apply Color.BLACK
        final TypedValue typedValue = new TypedValue();
        data.setPrimaryColor(
                Color.BLACK
                        | getColorFromThemeAttribute(
                                context, typedValue, android.R.attr.colorPrimary, Color.BLACK));
        data.setPrimaryDarkColor(
                Color.BLACK
                        | getColorFromThemeAttribute(
                                context,
                                typedValue,
                                android.R.attr.colorPrimaryDark,
                                data.getPrimaryColor()));
        data.setAccentColor(
                Color.BLACK
                        | getColorFromThemeAttribute(
                                context,
                                typedValue,
                                android.R.attr.colorAccent,
                                data.getPrimaryColor()));
        data.setPrimaryTextColor(
                Color.BLACK
                        | getColorFromThemeAttribute(
                                context, typedValue, android.R.attr.textColorPrimary, Color.BLACK));
        data.setSecondaryTextColor(
                Color.BLACK
                        | getColorFromThemeAttribute(
                                context,
                                typedValue,
                                android.R.attr.textColorSecondary,
                                data.getPrimaryTextColor()));
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

    private static class InvalidOverlayData extends OverlayData {
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
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        protected void fetchRemoteColors(OverlayData data, Context context) {
            final TypedValue typedValue = new TypedValue();
            data.setPrimaryColor(
                    getColorFromThemeAttribute(
                            context, typedValue, android.R.attr.colorPrimary, 0));
            data.setPrimaryDarkColor(
                    getColorFromThemeAttribute(
                            context,
                            typedValue,
                            android.R.attr.colorPrimaryDark,
                            data.getPrimaryColor()));
            // these will be static
            data.setAccentColor(data.getPrimaryColor());
            data.setPrimaryTextColor(Color.WHITE);
            data.setSecondaryTextColor(Color.LTGRAY);
        }
    }
}
