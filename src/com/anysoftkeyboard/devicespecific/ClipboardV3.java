package com.anysoftkeyboard.devicespecific;

import android.annotation.TargetApi;
import android.content.Context;
import android.text.ClipboardManager;

@SuppressWarnings("deprecation")
@TargetApi(3)
final class ClipboardV3 implements Clipboard {
	private final ClipboardManager cbV3;

	ClipboardV3(ClipboardDiagram diagram) {
		cbV3 = (android.text.ClipboardManager) diagram.getContext()
				.getSystemService(Context.CLIPBOARD_SERVICE);
	}

	public void setText(CharSequence text) {
		cbV3.setText(text);
	}

	public CharSequence getText() {
		if (cbV3.hasText())
			return cbV3.getText();
		else
			return null;
	}
}