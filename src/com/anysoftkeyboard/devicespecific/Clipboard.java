package com.anysoftkeyboard.devicespecific;

import android.content.Context;
import net.evendanan.frankenrobot.Diagram;

public interface Clipboard {
	public static class ClipboardDiagram extends Diagram<Clipboard> {
		private final Context mContext;
		public ClipboardDiagram(Context context) {
			mContext = context;
		}
		
		public Context getContext() {
			return mContext;
		}
	}
	
	CharSequence getText();
	void setText(CharSequence text);
}
