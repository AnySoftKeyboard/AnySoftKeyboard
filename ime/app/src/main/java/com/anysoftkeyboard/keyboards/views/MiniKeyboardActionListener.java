package com.anysoftkeyboard.keyboards.views;

import androidx.annotation.NonNull;
import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;

public final class MiniKeyboardActionListener implements OnKeyboardActionListener {

    public interface OnKeyboardActionListenerProvider {
        @NonNull
        OnKeyboardActionListener listener();
    }

    @NonNull private final OnKeyboardActionListenerProvider mParentListener;
    @NonNull private final Runnable mKeyboardDismissAction;
    private boolean mInOneShot;

    MiniKeyboardActionListener(
            @NonNull OnKeyboardActionListenerProvider parentListener,
            @NonNull Runnable keyboardDismissAction) {
        mParentListener = parentListener;
        mKeyboardDismissAction = keyboardDismissAction;
    }

    void setInOneShot(boolean inOneShot) {
        mInOneShot = inOneShot;
    }

    @Override
    public void onKey(
            int primaryCode,
            Keyboard.Key key,
            int multiTapIndex,
            int[] nearByKeyCodes,
            boolean fromUI) {
        mParentListener.listener().onKey(primaryCode, key, multiTapIndex, nearByKeyCodes, fromUI);
        if ((mInOneShot && primaryCode != KeyCodes.DELETE) || primaryCode == KeyCodes.ENTER) {
            mKeyboardDismissAction.run();
        }
    }

    @Override
    public void onMultiTapStarted() {
        mParentListener.listener().onMultiTapStarted();
    }

    @Override
    public void onMultiTapEnded() {
        mParentListener.listener().onMultiTapEnded();
    }

    @Override
    public void onText(Keyboard.Key key, CharSequence text) {
        mParentListener.listener().onText(key, text);
        if (mInOneShot) mKeyboardDismissAction.run();
    }

    @Override
    public void onTyping(Keyboard.Key key, CharSequence text) {
        mParentListener.listener().onTyping(key, text);
        if (mInOneShot) mKeyboardDismissAction.run();
    }

    @Override
    public void onCancel() {
        mKeyboardDismissAction.run();
    }

    @Override
    public void onSwipeLeft(boolean twoFingers) {}

    @Override
    public void onSwipeRight(boolean twoFingers) {}

    @Override
    public void onSwipeUp() {}

    @Override
    public void onSwipeDown() {}

    @Override
    public void onPinch() {}

    @Override
    public void onSeparate() {}

    @Override
    public void onPress(int primaryCode) {
        mParentListener.listener().onPress(primaryCode);
    }

    @Override
    public void onRelease(int primaryCode) {
        mParentListener.listener().onRelease(primaryCode);
    }

    @Override
    public void onFirstDownKey(int primaryCode) {
        mParentListener.listener().onFirstDownKey(primaryCode);
    }

    @Override
    public boolean onGestureTypingInputStart(int x, int y, AnyKeyboard.AnyKey key, long eventTime) {
        // no gesture in mini-keyboard
        return false;
    }

    @Override
    public void onGestureTypingInput(int x, int y, long eventTime) {}

    @Override
    public void onGestureTypingInputDone() {}

    @Override
    public void onLongPressDone(@NonNull Keyboard.Key key) {
        mParentListener.listener().onLongPressDone(key);
    }
}
