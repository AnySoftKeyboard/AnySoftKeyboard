package com.anysoftkeyboard.ime;

import android.content.ComponentName;
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
import com.anysoftkeyboard.theme.KeyboardThemeFactory;
import com.menny.android.anysoftkeyboard.R;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public abstract class AnySoftKeyboardThemeOverlay extends AnySoftKeyboardKeyboardTagsSearcher {
    @VisibleForTesting
    static final OverlayData INVALID_OVERLAY_DATA = new EmptyOverlayData();

    private OverlyDataCreator mOverlyDataCreator;
    private CandidateView mCandidateView;
    private String mLastOverlayPackage = "";
    private KeyboardTheme mCurrentTheme;

    private static Map<String, OverlayData> createOverridesForOverlays() {
        return Collections.emptyMap();
    }

    private boolean mApplyRemoteAppColors;
    @NonNull
    private OverlayData mCurrentOverlayData = INVALID_OVERLAY_DATA;

    @Override
    public void onCreate() {
        super.onCreate();
        mOverlyDataCreator = createOverlayDataCreator();

        addDisposable(KeyboardThemeFactory.observeCurrentTheme(getApplicationContext())
                .subscribe(this::onThemeChanged, GenericOnError.onError("KeyboardThemeFactory.observeCurrentTheme")));

        addDisposable(prefs().getBoolean(R.string.settings_key_apply_remote_app_colors, R.bool.settings_default_apply_remote_app_colors)
                .asObservable().subscribe(enabled -> {
                    mApplyRemoteAppColors = enabled;
                    mCurrentOverlayData = INVALID_OVERLAY_DATA;
                    mLastOverlayPackage = "";
                    hideWindow();
                }, GenericOnError.onError("settings_key_apply_remote_app_colors"))
        );
    }

    @Override
    protected void onThemeChanged(@NonNull KeyboardTheme theme) {
        mCurrentTheme = theme;

        if (mCandidateView != null) {
            mCandidateView.setKeyboardTheme(theme);
            mCandidateView.setOverlayData(mCurrentOverlayData);
        }

        final InputViewBinder inputView = getInputView();
        if (inputView != null) {
            inputView.setKeyboardTheme(theme);
            inputView.setKeyboardOverlay(mCurrentOverlayData);
        }

        super.onThemeChanged(theme);
    }

    protected OverlyDataCreator createOverlayDataCreator() {
        if (OverlyDataCreatorForAndroid.OS_SUPPORT_FOR_ACCENT) {
            return new OverlyDataCreator() {
                private final OverlyDataCreator mActualCreator = new OverlayDataOverrider(
                        new OverlayDataNormalizer(new OverlyDataCreatorForAndroid.Light(AnySoftKeyboardThemeOverlay.this), 96, true),
                        createOverridesForOverlays());

                @Override
                public OverlayData createOverlayData(ComponentName remoteApp) {
                    if (mApplyRemoteAppColors) {
                        if (Objects.equals(remoteApp.getPackageName(), mLastOverlayPackage)) {
                            return mCurrentOverlayData;
                        } else {
                            mLastOverlayPackage = remoteApp.getPackageName();
                            return mActualCreator.createOverlayData(remoteApp);
                        }
                    } else {
                        return INVALID_OVERLAY_DATA;
                    }
                }
            };
        } else {
            return remoteApp -> INVALID_OVERLAY_DATA;
        }
    }

    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
        super.onStartInputView(info, restarting);

        applyThemeOverlay(info);
    }

    protected void applyThemeOverlay(EditorInfo info) {
        final Intent launchIntentForPackage = info.packageName == null ? null : getPackageManager().getLaunchIntentForPackage(info.packageName);
        if (launchIntentForPackage != null) {
            mCurrentOverlayData = mOverlyDataCreator.createOverlayData(launchIntentForPackage.getComponent());
        } else {
            mCurrentOverlayData = INVALID_OVERLAY_DATA;
            mLastOverlayPackage = "";
        }
        final InputViewBinder inputView = getInputView();
        if (inputView != null) {
            inputView.setKeyboardOverlay(mCurrentOverlayData);
            if (mCandidateView != null) {
                mCandidateView.setOverlayData(mCurrentOverlayData);
            }
        }
    }

    @Override
    public void onAddOnsCriticalChange() {
        mLastOverlayPackage = "";
        super.onAddOnsCriticalChange();
    }

    @Override
    public View onCreateInputView() {
        mLastOverlayPackage = "";
        final View view = super.onCreateInputView();

        final InputViewBinder inputView = getInputView();
        if (inputView != null) {
            inputView.setKeyboardOverlay(mCurrentOverlayData);
        }

        return view;
    }

    @Override
    public void setCandidatesView(@NonNull View view) {
        super.setCandidatesView(view);
        mCandidateView = view.findViewById(R.id.candidates);

        mCandidateView.setOverlayData(mCurrentOverlayData);
        mCandidateView.setKeyboardTheme(mCurrentTheme);
    }

    private static class EmptyOverlayData extends OverlayData {
        @Override
        public boolean isValid() {
            return false;
        }
    }
}
