package com.anysoftkeyboard.ime;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.view.View;

import com.anysoftkeyboard.powersave.PowerSaving;
import com.anysoftkeyboard.rx.GenericOnError;
import com.anysoftkeyboard.theme.KeyboardTheme;
import com.anysoftkeyboard.theme.KeyboardThemeFactory;

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
    protected String generateWatermark() {
        return super.generateWatermark() + (mPowerState ? "\uD83D\uDD0B" : "");
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
