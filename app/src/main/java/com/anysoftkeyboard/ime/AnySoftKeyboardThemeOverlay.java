package com.anysoftkeyboard.ime;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import com.anysoftkeyboard.keyboards.views.CandidateView;
import com.anysoftkeyboard.overlay.OverlayData;
import com.anysoftkeyboard.overlay.OverlayDataNormalizer;
import com.anysoftkeyboard.overlay.OverlayDataOverrider;
import com.anysoftkeyboard.overlay.OverlyDataCreator;
import com.anysoftkeyboard.overlay.OverlyDataCreatorForAndroid;
import com.anysoftkeyboard.rx.GenericOnError;
import com.anysoftkeyboard.theme.KeyboardTheme;
import com.menny.android.anysoftkeyboard.R;

import java.util.Collections;
import java.util.Map;

public abstract class AnySoftKeyboardThemeOverlay extends AnySoftKeyboardIncognito {
    @VisibleForTesting
    static final OverlayData INVALID_OVERLAY_DATA = new EmptyOverlayData();

    private OverlyDataCreator mOverlyDataCreator;
    private CandidateView mCandidateView;

    private static Map<String, OverlayData> createOverridesForOverlays() {
        return Collections.emptyMap();
    }

    private boolean mApplyRemoteAppColors = true;
    @NonNull
    private OverlayData mCurrentOverlayData = INVALID_OVERLAY_DATA;

    @Override
    public void onCreate() {
        super.onCreate();
        mOverlyDataCreator = createOverlayDataCreator();

        addDisposable(prefs().getBoolean(R.string.settings_key_apply_remote_app_colors, R.bool.settings_default_apply_remote_app_colors)
                .asObservable().subscribe(enabled -> mApplyRemoteAppColors = enabled, GenericOnError.onError("settings_key_apply_remote_app_colors"))
        );
    }

    @VisibleForTesting
    protected OverlyDataCreator createOverlayDataCreator() {
        return new OverlayDataOverrider(
                new OverlayDataNormalizer(new OverlyDataCreatorForAndroid.Light(this), 96, true),
                createOverridesForOverlays());
    }

    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
        super.onStartInputView(info, restarting);
        if (OverlyDataCreatorForAndroid.OS_SUPPORT_FOR_ACCENT) {
            final InputViewBinder inputView = getInputView();
            if (inputView != null) {
                mCurrentOverlayData = INVALID_OVERLAY_DATA;
                if (mApplyRemoteAppColors) {
                    final Intent launchIntentForPackage = getPackageManager().getLaunchIntentForPackage(info.packageName);
                    if (launchIntentForPackage != null) {
                        mCurrentOverlayData = mOverlyDataCreator.createOverlayData(launchIntentForPackage.getComponent());
                    }
                }
                inputView.setKeyboardOverlay(mCurrentOverlayData);
                if (mCandidateView != null) {
                    mCandidateView.setOverlayData(mCurrentOverlayData);
                }
            }
        }
    }


    @Override
    public View onCreateInputView() {
        final View view = super.onCreateInputView();

        getInputView().setKeyboardOverlay(mCurrentOverlayData);

        return view;
    }

    @Override
    public void setCandidatesView(@NonNull View view) {
        super.setCandidatesView(view);
        mCandidateView = view.findViewById(R.id.candidates);
    }

    @Override
    protected void setCandidatesTheme(KeyboardTheme theme) {
        if (mCandidateView != null) {
            mCandidateView.setOverlayData(mCurrentOverlayData);
        }
    }

    private static class EmptyOverlayData extends OverlayData {
        @Override
        public boolean isValid() {
            return false;
        }
    }
}
