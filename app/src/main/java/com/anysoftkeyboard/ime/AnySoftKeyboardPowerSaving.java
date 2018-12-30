package com.anysoftkeyboard.ime;

import android.content.ComponentName;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.v4.content.ContextCompat;
import android.view.inputmethod.EditorInfo;

import com.anysoftkeyboard.overlay.OverlayData;
import com.anysoftkeyboard.overlay.OverlyDataCreator;
import com.anysoftkeyboard.powersave.PowerSaving;
import com.anysoftkeyboard.rx.GenericOnError;
import com.menny.android.anysoftkeyboard.R;

import java.util.List;

public abstract class AnySoftKeyboardPowerSaving extends AnySoftKeyboardThemeOverlay {

    private static final OverlayData POWER_SAVING_OVERLAY = new OverlayData(
            Color.BLACK,
            Color.BLACK,
            Color.DKGRAY,
            Color.GRAY,
            Color.DKGRAY
    );

    private boolean mPowerState;
    private boolean mThemeInPowerSaving;

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

        addDisposable(PowerSaving.observePowerSavingState(getApplicationContext(), R.string.settings_key_power_save_mode_theme_control, R.bool.settings_default_false)
                .subscribe(themePowerState -> {
                    mThemeInPowerSaving = themePowerState;
                    final EditorInfo currentInputEditorInfo = getCurrentInputEditorInfo();
                    if (currentInputEditorInfo != null) {
                        applyThemeOverlay(currentInputEditorInfo);
                                //mThemeInPowerSaving/*only forcing calculation in power-state, else let the system decide if to calculate or apply invalid*/);
                    }
                }, GenericOnError.onError("Power-Saving theme")));
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
        return new PowerSavingOverlayCreator(super.createOverlayDataCreator());
    }

    @VisibleForTesting
    class PowerSavingOverlayCreator implements OverlyDataCreator {
        private final OverlyDataCreator mOriginalCreator;

        private PowerSavingOverlayCreator(OverlyDataCreator originalCreator) {
            mOriginalCreator = originalCreator;
        }

        @Override
        public OverlayData createOverlayData(ComponentName remoteApp) {
            if (mThemeInPowerSaving) {
                return POWER_SAVING_OVERLAY;
            } else {
                return mOriginalCreator.createOverlayData(remoteApp);
            }
        }
    }
}
