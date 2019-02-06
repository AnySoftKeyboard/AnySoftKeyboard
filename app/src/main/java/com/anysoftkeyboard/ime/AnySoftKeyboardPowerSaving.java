package com.anysoftkeyboard.ime;

import android.content.ComponentName;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.inputmethod.EditorInfo;

import com.anysoftkeyboard.android.PowerSaving;
import com.anysoftkeyboard.overlay.OverlayData;
import com.anysoftkeyboard.overlay.OverlyDataCreator;
import com.anysoftkeyboard.rx.GenericOnError;
import com.menny.android.anysoftkeyboard.R;

import java.util.List;

public abstract class AnySoftKeyboardPowerSaving extends AnySoftKeyboardNightMode {
    private boolean mPowerState;
    private ToggleOverlayCreator mToggleOverlayCreator;

    @Override
    public void onCreate() {
        super.onCreate();

        addDisposable(PowerSaving.observePowerSavingState(getApplicationContext(), 0).subscribe(
                powerState -> {
                    mPowerState = powerState;
                    setupInputViewWatermark();
                },
                GenericOnError.onError("Power-Saving icon")
        ));

        addDisposable(PowerSaving.observePowerSavingState(getApplicationContext(), R.string.settings_key_power_save_mode_theme_control, R.bool.settings_default_true)
                .subscribe(mToggleOverlayCreator::setToggle, GenericOnError.onError("Power-Saving theme")));
    }

    @NonNull
    @Override
    protected List<Drawable> generateWatermark() {
        final List<Drawable> watermark = super.generateWatermark();
        if (mPowerState) {
            watermark.add(ContextCompat.getDrawable(this, R.drawable.ic_watermark_power_saving));
        }
        return watermark;
    }

    @Override
    protected OverlyDataCreator createOverlayDataCreator() {
        return mToggleOverlayCreator = new ToggleOverlayCreator(super.createOverlayDataCreator(), this,
                new OverlayData(
                        Color.BLACK,
                        Color.BLACK,
                        Color.DKGRAY,
                        Color.GRAY,
                        Color.DKGRAY
                ));
    }

    static class ToggleOverlayCreator implements OverlyDataCreator {
        private final OverlyDataCreator mOriginalCreator;
        private final OverlayData mOverrideData;
        private final AnySoftKeyboardThemeOverlay mOverlayController;
        private boolean mUseOverride;

        ToggleOverlayCreator(OverlyDataCreator originalCreator, AnySoftKeyboardThemeOverlay overlayController, OverlayData overrideData) {
            mOriginalCreator = originalCreator;
            mOverlayController = overlayController;
            mOverrideData = overrideData;
        }

        void setToggle(boolean useOverride) {
            mUseOverride = useOverride;

            final EditorInfo currentInputEditorInfo = mOverlayController.getCurrentInputEditorInfo();
            if (currentInputEditorInfo != null) {
                mOverlayController.applyThemeOverlay(currentInputEditorInfo);
            }
        }

        @Override
        public OverlayData createOverlayData(ComponentName remoteApp) {
            if (mUseOverride) {
                return mOverrideData;
            } else {
                return mOriginalCreator.createOverlayData(remoteApp);
            }
        }
    }
}
