package com.anysoftkeyboard.devicespecific;

import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.ClipData.Item;
import android.content.ClipboardManager;
import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.util.AttributeSet;
import android.util.Log;
import android.view.inputmethod.CorrectionInfo;
import android.view.inputmethod.InputConnection;

import com.anysoftkeyboard.WordComposer;
import com.anysoftkeyboard.voice.VoiceInput;
import com.anysoftkeyboard.voice.VoiceRecognitionTriggerV11;

@TargetApi(11)
class FactoryView_V11 extends FactoryView_V8 {

	public FactoryView_V11(Context context) {
		super(context);
	}

	public FactoryView_V11(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	 
	public FactoryView_V11(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
    }
	
	@Override
	public DeviceSpecific createDeviceSpecific() {
		return new DeviceSpecific_V11();
	}

	public static class DeviceSpecific_V11 extends DeviceSpecific_V8
	{		
		@Override
		public String getApiLevel() {
			return "DeviceSpecific_V11";
		}
		@Override
		public VoiceInput createVoiceInput(InputMethodService ime) {
			return new VoiceRecognitionTriggerV11(ime);
		}
		
		@Override
		public void commitCorrectionToInputConnection(InputConnection ic, WordComposer word) {
			super.commitCorrectionToInputConnection(ic, word);
			CorrectionInfo correctionInfo = 
					new CorrectionInfo(word.globalCursorPosition() - word.getTypedWord().length(), word.getTypedWord(), word.getPreferredWord());
			
			ic.commitCorrection(correctionInfo);
		}
	}


	
	public Clipboard getClipboard(final Context appContext) {
		final ClipboardManager cbV11 = (ClipboardManager)appContext.getSystemService(Context.CLIPBOARD_SERVICE);
		return new Clipboard() {
			
			public void setText(CharSequence text) {
				cbV11.setPrimaryClip(ClipData.newPlainText("Styled Text", text));
			}
			
			public CharSequence getText() {
				ClipData cp = cbV11.getPrimaryClip();
				if (cp != null)
				{
					if (cp.getItemCount() > 0)
					{
						Item cpi = cp.getItemAt(0);
						return cpi.coerceToText(appContext);
					}
				}

				return null;
			}
		}; 
	}
}
