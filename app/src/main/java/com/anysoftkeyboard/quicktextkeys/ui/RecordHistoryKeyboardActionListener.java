package com.anysoftkeyboard.quicktextkeys.ui;

import android.text.TextUtils;

import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.views.MiniKeyboardActionListener;
import com.anysoftkeyboard.keyboards.views.OnKeyboardActionListener;
import com.anysoftkeyboard.quicktextkeys.HistoryQuickTextKey;

public class RecordHistoryKeyboardActionListener implements OnKeyboardActionListener {
	private final HistoryQuickTextKey mHistoryQuickTextKey;
	private final MiniKeyboardActionListener mKeyboardActionListener;

	public RecordHistoryKeyboardActionListener(HistoryQuickTextKey HistoryQuickTextKey, MiniKeyboardActionListener keyboardActionListener) {
		mHistoryQuickTextKey = HistoryQuickTextKey;
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
	public void onSwipeDown(boolean twoFingers){
		mKeyboardActionListener.onSwipeDown(twoFingers);
	}

	@Override
	public void onSwipeUp(boolean twoFingers) {
		mKeyboardActionListener.onSwipeUp(twoFingers);
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
}
