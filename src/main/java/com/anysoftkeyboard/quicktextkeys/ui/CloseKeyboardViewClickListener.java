package com.anysoftkeyboard.quicktextkeys.ui;

import android.view.View;

import com.anysoftkeyboard.keyboards.views.OnKeyboardActionListener;

public class CloseKeyboardViewClickListener implements View.OnClickListener {
	private final OnKeyboardActionListener mKeyboardActionListener;

	public CloseKeyboardViewClickListener(OnKeyboardActionListener keyboardActionListener) {
		mKeyboardActionListener = keyboardActionListener;
	}

	@Override
	public void onClick(View v) {
		mKeyboardActionListener.onCancel();
	}
}
