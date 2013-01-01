package com.anysoftkeyboard.devicespecific;

import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.ClipData.Item;
import android.content.ClipboardManager;
import android.content.Context;

import com.anysoftkeyboard.dictionaries.DictionaryFactory;
import com.anysoftkeyboard.dictionaries.DictionaryFactoryAPI16;

@TargetApi(16)
public class DeviceSpecific_V16 extends DeviceSpecific_V8 {
	@Override
	public String getApiLevel() {
		return "DeviceSpecific_V16";
	}

	@Override
	public DictionaryFactory createDictionaryFactory() {
		return new DictionaryFactoryAPI16();
	}

	@Override
	public Clipboard getClipboard(final Context appContext) {
		final ClipboardManager cbV11 = (ClipboardManager) appContext
				.getSystemService(Context.CLIPBOARD_SERVICE);
		return new Clipboard() {

			public void setText(CharSequence text) {
				cbV11.setPrimaryClip(ClipData.newPlainText("Styled Text", text));
			}

			public CharSequence getText() {
				ClipData cp = cbV11.getPrimaryClip();
				if (cp != null) {
					if (cp.getItemCount() > 0) {
						Item cpi = cp.getItemAt(0);
						return cpi.coerceToText(appContext);
					}
				}

				return null;
			}
		};
	}
}