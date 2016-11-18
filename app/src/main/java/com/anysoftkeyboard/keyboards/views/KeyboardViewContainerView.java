package com.anysoftkeyboard.keyboards.views;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.anysoftkeyboard.ime.InputViewBinder;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.KeyboardDimens;

public class KeyboardViewContainerView extends FrameLayout implements InputViewBinder {
    private InputViewBinder mActualView;
    private OnKeyboardActionListener mKeyboardActionListener;

    public KeyboardViewContainerView(Context context) {
        super(context);
    }

    public KeyboardViewContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public KeyboardViewContainerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public KeyboardViewContainerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void onViewAdded(View child) {
        super.onViewAdded(child);
        mActualView = getTopChild();
        if (mKeyboardActionListener != null)
            mActualView.setOnKeyboardActionListener(mKeyboardActionListener);
    }

    private InputViewBinder getTopChild() {
        return (InputViewBinder) getChildAt(getChildCount()-1);
    }

    @NonNull
    @Override
    public KeyboardDimens getThemedKeyboardDimens() {
        return mActualView.getThemedKeyboardDimens();
    }

    @Override
    public void onViewNotRequired() {
        mActualView.onViewNotRequired();
        mActualView = null;
    }

    @Override
    public void onViewRemoved(View child) {
        super.onViewRemoved(child);
        mActualView = getTopChild();
    }

    @Override
    public boolean setControl(boolean active) {
        return mActualView.setControl(active);
    }

    @Override
    public boolean setShifted(boolean active) {
        return mActualView.setShifted(active);
    }

    @Override
    public boolean isShifted() {
        return mActualView.isShifted();
    }

    @Override
    public boolean setShiftLocked(boolean locked) {
        return mActualView.setShiftLocked(locked);
    }

    @Override
    public boolean closing() {
        return mActualView.closing();
    }

    @Override
    public void setKeyboard(AnyKeyboard currentKeyboard, String nextAlphabetKeyboard, String nextSymbolsKeyboard) {
        mActualView.setKeyboard(currentKeyboard, nextAlphabetKeyboard, nextSymbolsKeyboard);
    }

    @Override
    public void setOnKeyboardActionListener(OnKeyboardActionListener keyboardActionListener) {
        mKeyboardActionListener = keyboardActionListener;
        mActualView.setOnKeyboardActionListener(keyboardActionListener);
    }

    @Override
    public void setKeyboardActionType(int imeOptions) {
        mActualView.setKeyboardActionType(imeOptions);
    }

    @Override
    public void popTextOutOfKey(CharSequence wordToAnimatePopping) {
        mActualView.popTextOutOfKey(wordToAnimatePopping);
    }

    @Override
    public void revertPopTextOutOfKey() {
        mActualView.revertPopTextOutOfKey();
    }

    /*@Override
    public void showQuickKeysView(Keyboard.Key key) {
        mActualView.showQuickKeysView(key);
    }*/

    @Override
    public boolean dismissPopupKeyboard() {
        return mActualView.dismissPopupKeyboard();
    }

    @Override
    public boolean handleBack() {
        return mActualView.handleBack();
    }

    @Override
    public void openUtilityKeyboard() {
        mActualView.openUtilityKeyboard();
    }
}
