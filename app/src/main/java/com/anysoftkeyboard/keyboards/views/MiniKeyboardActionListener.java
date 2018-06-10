package com.anysoftkeyboard.keyboards.views;

import android.support.annotation.NonNull;

import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.keyboards.Keyboard;

public final class MiniKeyboardActionListener implements OnKeyboardActionListener {

    private final AnyKeyboardViewWithMiniKeyboard mParentKeyboard;
    private boolean mInOneShot;

    MiniKeyboardActionListener(AnyKeyboardViewWithMiniKeyboard parentKeyboard) {
        mParentKeyboard = parentKeyboard;
    }

    public void setInOneShot(boolean inOneShot) {
        mInOneShot = inOneShot;
    }

    @Override
    public void onKey(int primaryCode, Keyboard.Key key, int multiTapIndex, int[] nearByKeyCodes, boolean fromUI) {
        mParentKeyboard.mKeyboardActionListener.onKey(primaryCode, key, multiTapIndex, nearByKeyCodes, fromUI);
        if ((mInOneShot && primaryCode != KeyCodes.DELETE) || primaryCode == KeyCodes.ENTER)
            mParentKeyboard.dismissPopupKeyboard();
    }

    @Override
    public void onMultiTapStarted() {
        mParentKeyboard.mKeyboardActionListener.onMultiTapStarted();
    }

    @Override
    public void onMultiTapEnded() {
        mParentKeyboard.mKeyboardActionListener.onMultiTapEnded();
    }

    @Override
    public void onText(Keyboard.Key key, CharSequence text) {
        mParentKeyboard.mKeyboardActionListener.onText(key, text);
        if (mInOneShot) mParentKeyboard.dismissPopupKeyboard();
    }

    @Override
    public void onCancel() {
        mParentKeyboard.dismissPopupKeyboard();
    }

    @Override
    public void onSwipeLeft(boolean twoFingers) {
    }

    @Override
    public void onSwipeRight(boolean twoFingers) {
    }

    @Override
    public void onSwipeUp() {
    }

    @Override
    public void onSwipeDown() {
    }

    @Override
    public void onPinch() {
    }

    @Override
    public void onSeparate() {
    }

    @Override
    public void onPress(int primaryCode) {
        mParentKeyboard.mKeyboardActionListener.onPress(primaryCode);
    }

    @Override
    public void onRelease(int primaryCode) {
        mParentKeyboard.mKeyboardActionListener.onRelease(primaryCode);
    }

    @Override
    public void onFirstDownKey(int primaryCode) {
        mParentKeyboard.mKeyboardActionListener.onFirstDownKey(primaryCode);
    }

    @Override
    public boolean isValidGestureTypingStart(int x, int y) { return false; }

    @Override
    public void onGestureTypingInputStart(int x, int y, long eventTime) {}

    @Override
    public void onGestureTypingInput(int x, int y, long eventTime) {}

    @Override
    public void onGestureTypingInputDone() {}

    @Override
    public boolean isPerformingGesture() {
        return false;
    }

    @Override
    public void onLongPressDone(@NonNull Keyboard.Key key) {
        mParentKeyboard.mKeyboardActionListener.onLongPressDone(key);
    }
}
