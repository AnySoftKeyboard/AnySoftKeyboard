package com.anysoftkeyboard.quicktextkeys.ui;

import android.text.TextUtils;

import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.views.OnKeyboardActionListener;
import com.anysoftkeyboard.quicktextkeys.HistoryQuickTextKey;

/*package*/ class RecordHistoryKeyboardActionListener implements OnKeyboardActionListener {
    private final HistoryQuickTextKey mHistoryQuickTextKey;
    private final OnKeyboardActionListener mKeyboardActionListener;

    public RecordHistoryKeyboardActionListener(HistoryQuickTextKey historyQuickTextKey, OnKeyboardActionListener keyboardActionListener) {
        mHistoryQuickTextKey = historyQuickTextKey;
        mKeyboardActionListener = keyboardActionListener;
    }

    @Override
    public void onPress(int primaryCode) {
        mKeyboardActionListener.onPress(primaryCode);
    }

    @Override
    public void onRelease(int primaryCode) {
        mKeyboardActionListener.onRelease(primaryCode);
    }

    @Override
    public void onKey(int primaryCode, Keyboard.Key key, int multiTapIndex, int[] nearByKeyCodes, boolean fromUI) {
        mKeyboardActionListener.onKey(primaryCode, key, multiTapIndex, nearByKeyCodes, fromUI);
    }

    @Override
    public void onMultiTapStarted() {
        mKeyboardActionListener.onMultiTapStarted();
    }

    @Override
    public void onMultiTapEnded() {
        mKeyboardActionListener.onMultiTapEnded();
    }

    @Override
    public void onText(Keyboard.Key key, CharSequence text) {
        mKeyboardActionListener.onText(key, text);
        if (TextUtils.isEmpty(key.label) || TextUtils.isEmpty(text)) return;
        String name = String.valueOf(key.label);
        String value = String.valueOf(text);

        mHistoryQuickTextKey.recordUsedKey(name, value);
    }

    @Override
    public void onCancel() {
        mKeyboardActionListener.onCancel();
    }

    @Override
    public void onSwipeLeft(boolean twoFingers) {
        mKeyboardActionListener.onSwipeLeft(twoFingers);
    }

    @Override
    public void onSwipeRight(boolean twoFingers) {
        mKeyboardActionListener.onSwipeRight(twoFingers);
    }

    @Override
    public void onSwipeDown() {
        mKeyboardActionListener.onSwipeDown();
    }

    @Override
    public void onSwipeUp() {
        mKeyboardActionListener.onSwipeUp();
    }

    @Override
    public void onPinch() {
        mKeyboardActionListener.onPinch();
    }

    @Override
    public void onSeparate() {
        mKeyboardActionListener.onSeparate();
    }

    @Override
    public void onFirstDownKey(int primaryCode) {
        mKeyboardActionListener.onFirstDownKey(primaryCode);
    }

    @Override
    public boolean isValidGestureTypingStart(int x, int y) { return false; }

    @Override
    public void onGestureTypingInputStart(int x, int y, long eventTime) {}

    @Override
    public void onGestureTypingInput(int x, int y, long eventTime) {}

    @Override
    public void onGestureTypingInputDone() {}
}
