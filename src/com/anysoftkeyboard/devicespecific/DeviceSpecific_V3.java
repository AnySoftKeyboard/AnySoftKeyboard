package com.anysoftkeyboard.devicespecific;


import android.annotation.TargetApi;
import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.inputmethod.InputConnection;
import android.widget.Toast;

import com.anysoftkeyboard.WordComposer;
import com.anysoftkeyboard.backup.CloudBackupRequester;
import com.anysoftkeyboard.dictionaries.DictionaryFactory;
import com.anysoftkeyboard.voice.VoiceInput;
import com.menny.android.anysoftkeyboard.R;

@TargetApi(3)
public class DeviceSpecific_V3 implements DeviceSpecific
{
	public DeviceSpecific_V3()
	{
		Log.d(getApiLevel(), "Just created DeviceSpecific of type "+getApiLevel());
	}

	public String getApiLevel() {
		return "DeviceSpecific_V3";
	}

	public WMotionEvent createMotionEventWrapper(MotionEvent nativeMotionEvent) {
		return new WMotionEvent(nativeMotionEvent);
	}

	public MultiTouchSupportLevel getMultiTouchSupportLevel(Context appContext) {
		return MultiTouchSupportLevel.None;
	}

	public GestureDetector createGestureDetector(Context appContext, 
			AskOnGestureListener listener) {
		return new GestureDetector(appContext, listener, null);
	}
	
	public DictionaryFactory createDictionaryFactory() {
		return new DictionaryFactory();
	}
	
	public VoiceInput createVoiceInput(InputMethodService ime) {
		final Context appContext = ime.getApplicationContext();
		return new VoiceInput() {
			public void startVoiceRecognition(String language) {
				Toast.makeText(appContext, appContext.getText(R.string.voice_input_not_support_in_this_os_version), Toast.LENGTH_LONG).show();
			}
			
			public void onStartInputView() {
				
			}
		};
	}

	@SuppressWarnings("deprecation")
	public Clipboard getClipboard(Context appContext) {
		final android.text.ClipboardManager cbV3 = (android.text.ClipboardManager)appContext.getSystemService(Context.CLIPBOARD_SERVICE);
		return new Clipboard() {
			
			public void setText(CharSequence text) {
				cbV3.setText(text);
			}
			
			public CharSequence getText() {
				if (cbV3.hasText())
					return cbV3.getText();
				else
					return null;
			}
		}; 
	}
	
	public void commitCorrectionToInputConnection(InputConnection ic, WordComposer word) {
		ic.commitText(word.getPreferredWord(), 1);
	}
}