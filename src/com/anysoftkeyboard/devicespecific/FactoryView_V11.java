package com.anysoftkeyboard.devicespecific;

import com.anysoftkeyboard.devicespecific.FactoryView_V8.DeviceSpecific_V8;
import com.anysoftkeyboard.voice.VoiceInput;
import com.anysoftkeyboard.voice.VoiceRecognitionTriggerV11;

import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.ClipData.Item;
import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.content.ClipboardManager;
import android.util.AttributeSet;

@TargetApi(11)
class FactoryView_V11 extends FactoryView_V7 {

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
