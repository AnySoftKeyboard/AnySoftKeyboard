package com.anysoftkeyboard.devicespecific;


import com.anysoftkeyboard.backup.CloudBackupRequester;
import com.anysoftkeyboard.dictionaries.DictionaryFactory;
import com.anysoftkeyboard.voice.VoiceInput;
import com.menny.android.anysoftkeyboard.R;

import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.text.ClipboardManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Toast;

@SuppressWarnings("deprecation")
class FactoryView_V3 extends FactoryViewBase {

	public FactoryView_V3(Context context) {
		super(context);
	}

	public FactoryView_V3(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	 
	public FactoryView_V3(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
    }
	
	@Override
	public DeviceSpecific createDeviceSpecific() {
		return new DeviceSpecific_V3();
	}
	
	//Has to be 'static' class, since I do no want any reference to the factory view (let GC clean it)
	public static class DeviceSpecific_V3 implements DeviceSpecific
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
		
		public CloudBackupRequester createCloudBackupRequester(String packageName) {
			return null;
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
		
		public Clipboard getClipboard(Context appContext) {
			final ClipboardManager cbV3 = (ClipboardManager)appContext.getSystemService(Context.CLIPBOARD_SERVICE);
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
	}
	
}
