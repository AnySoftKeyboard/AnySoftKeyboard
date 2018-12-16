package com.anysoftkeyboard.ime;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import com.anysoftkey.overlay.OverlayData;
import com.anysoftkey.overlay.OverlayDataNormalizer;
import com.anysoftkey.overlay.OverlayDataOverrider;
import com.anysoftkey.overlay.OverlyDataCreator;
import com.anysoftkey.overlay.OverlyDataCreatorForAndroid;

import java.util.Collections;
import java.util.Map;

public abstract class AnySoftKeyboardThemeOverlay extends AnySoftKeyboardIncognito {
    private static final OverlayData EMPTY = new EmptyOverlayData();

    private final OverlyDataCreator mOverlyDataCreator = new OverlayDataOverrider(
            new OverlayDataNormalizer(new OverlyDataCreatorForAndroid(this), 0.7f),
            createOverridesForOverlays());

    private static Map<String, OverlayData> createOverridesForOverlays() {
        return Collections.emptyMap();
    }

    private boolean mApplyRemoteAppColors = true;
    @NonNull
    private OverlayData mCurrentOverlayData = EMPTY;

    @Override
    public void onCreate() {
        super.onCreate();
//        prefs().getBoolean(R.string.settings_key_apply_remote_app_colors, R.bool.settings_default_apply_remote_app_colors)
//        addDisposable();
    }

    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
        if (OverlyDataCreatorForAndroid.OS_SUPPORT_FOR_ACCENT) {
            final InputViewBinder inputView = getInputView();
            if (inputView != null) {
                final Intent launchIntentForPackage = getPackageManager().getLaunchIntentForPackage(info.packageName);
                if (launchIntentForPackage != null) {
                    mCurrentOverlayData = mOverlyDataCreator.createOverlayData(launchIntentForPackage.getComponent());
                } else {
                    mCurrentOverlayData = EMPTY;
                }
                inputView.setKeyboardOverlay(mCurrentOverlayData);
            }
        }
    }


    @Override
    public View onCreateInputView() {
        final View view = super.onCreateInputView();

        final InputViewBinder inputView = getInputView();
        if (inputView != null) {
            inputView.setKeyboardOverlay(mCurrentOverlayData);
        }

        return view;
    }

    private static class EmptyOverlayData extends OverlayData {
        @Override
        public boolean isValid() {
            return false;
        }
    }
}
