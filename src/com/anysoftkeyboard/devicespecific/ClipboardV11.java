package com.anysoftkeyboard.devicespecific;

import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.ClipData.Item;
import android.content.ClipboardManager;
import android.content.Context;

@TargetApi(11)
final class ClipboardV11 implements Clipboard {
	private final ClipboardManager cbV11;
	private final Context mAppContext;

	ClipboardV11(ClipboardDiagram diagram) {
		mAppContext = diagram.getContext();
		cbV11 = (ClipboardManager) mAppContext
				.getSystemService(Context.CLIPBOARD_SERVICE);
	}

	public void setText(CharSequence text) {
		cbV11.setPrimaryClip(ClipData.newPlainText("Styled Text", text));
	}

	public CharSequence getText() {
		ClipData cp = cbV11.getPrimaryClip();
		if (cp != null) {
			if (cp.getItemCount() > 0) {
				Item cpi = cp.getItemAt(0);
				return cpi.coerceToText(mAppContext);
			}
		}

		return null;
	}

}