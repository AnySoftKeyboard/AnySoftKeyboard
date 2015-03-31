package com.anysoftkeyboard.keyboards.views;

import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.keyboards.Keyboard;

/**
* Created by menny on 3/30/15.
*/
final class MiniKeyboardActionListener implements OnKeyboardActionListener {

	private boolean mInOneShot;
    private final AnyKeyboardBaseView mParentKeyboard;

    public MiniKeyboardActionListener(AnyKeyboardBaseView parentKeyboard) {
        mParentKeyboard = parentKeyboard;
    }

		public void setInOneShot(boolean inOneShot) {
			mInOneShot = inOneShot;
		}

    public void onKey(int primaryCode, Keyboard.Key key, int multiTapIndex,int[] nearByKeyCodes, boolean fromUI) {
        mParentKeyboard.mKeyboardActionListener.onKey(primaryCode, key, multiTapIndex, nearByKeyCodes, fromUI);
        if (mInOneShot || primaryCode == KeyCodes.ENTER) mParentKeyboard.dismissPopupKeyboard();
    }

    public void onMultiTapStarted() {
        mParentKeyboard.mKeyboardActionListener.onMultiTapStarted();
    }

    public void onMultiTapEnded() {
        mParentKeyboard.mKeyboardActionListener.onMultiTapEnded();
    }

    public void onText(CharSequence text) {
        mParentKeyboard.mKeyboardActionListener.onText(text);
        if (mInOneShot) mParentKeyboard.dismissPopupKeyboard();
    }

    public void onCancel() {
        mParentKeyboard.dismissPopupKeyboard();
    }

    public void onSwipeLeft(boolean onSpaceBar, boolean twoFingers) {
    }

    public void onSwipeRight(boolean onSpaceBar, boolean twoFingers) {
    }

    public void onSwipeUp(boolean onSpaceBar) {
    }

    public void onSwipeDown(boolean onSpaceBar) {
    }

    public void onPinch() {
    }

    public void onSeparate() {
    }

    public void onPress(int primaryCode) {
        mParentKeyboard.mKeyboardActionListener.onPress(primaryCode);
    }

    public void onRelease(int primaryCode) {
        mParentKeyboard.mKeyboardActionListener.onRelease(primaryCode);
    }
}
