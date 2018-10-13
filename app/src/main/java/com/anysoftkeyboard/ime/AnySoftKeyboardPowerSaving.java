package com.anysoftkeyboard.ime;

import android.graphics.drawable.Drawable;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.anysoftkeyboard.powersave.PowerSaving;
import com.anysoftkeyboard.rx.GenericOnError;
import com.anysoftkeyboard.theme.KeyboardTheme;
import com.anysoftkeyboard.theme.KeyboardThemeFactory;
import com.menny.android.anysoftkeyboard.R;

import java.util.List;

public abstract class AnySoftKeyboardPowerSaving extends AnySoftKeyboardRxPrefs {

    private KeyboardTheme mCurrentKeyboardTheme;
    private boolean mPowerState;

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

        addDisposable(KeyboardThemeFactory.observeCurrentTheme(getApplicationContext())
                .subscribe(this::onKeyboardThemeChanged, GenericOnError.onError("Failed to observeCurrentTheme")));
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

    @CallSuper
    protected void onKeyboardThemeChanged(@NonNull KeyboardTheme theme) {
        mCurrentKeyboardTheme = theme;

        resetInputViews();
    }

    @Override
    protected void resetInputViews() {
        super.resetInputViews();
        final InputViewBinder inputView = getInputView();
        if (inputView != null) {
            inputView.setKeyboardTheme(mCurrentKeyboardTheme);
            setCandidatesTheme(mCurrentKeyboardTheme);
        }
    }

    protected abstract void setCandidatesTheme(KeyboardTheme theme);

    @Override
    public View onCreateInputView() {
        final View view = super.onCreateInputView();
        getInputView().setKeyboardTheme(mCurrentKeyboardTheme);
        return view;
    }

    protected final KeyboardTheme getCurrentKeyboardTheme() {
        return mCurrentKeyboardTheme;
    }

}
