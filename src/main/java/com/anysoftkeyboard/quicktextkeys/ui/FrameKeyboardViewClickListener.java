package com.anysoftkeyboard.quicktextkeys.ui;

import android.view.View;

import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.keyboards.views.OnKeyboardActionListener;
import com.menny.android.anysoftkeyboard.R;

public class FrameKeyboardViewClickListener implements View.OnClickListener {
	private final OnKeyboardActionListener mKeyboardActionListener;

	public FrameKeyboardViewClickListener(OnKeyboardActionListener keyboardActionListener) {
		mKeyboardActionListener = keyboardActionListener;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.quick_keys_popup_close:
				mKeyboardActionListener.onCancel();
				break;
			case R.id.quick_keys_popup_backspace:
				mKeyboardActionListener.onKey(KeyCodes.DELETE, null, 0, null, true);
				break;
			case R.id.quick_keys_popup_return:
				mKeyboardActionListener.onKey(KeyCodes.ENTER, null, 0, null, true);
				break;
		}
	}
}
