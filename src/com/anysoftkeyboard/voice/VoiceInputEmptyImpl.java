package com.anysoftkeyboard.voice;

import android.content.Context;
import android.widget.Toast;

import com.menny.android.anysoftkeyboard.R;

public class VoiceInputEmptyImpl implements VoiceInput {

	protected final Context mContext;
	
	public VoiceInputEmptyImpl(VoiceInputDiagram diagram) {
		mContext = diagram.getInputMethodService().getApplicationContext();
	}
	public void startVoiceRecognition(String language) {
		Toast.makeText(mContext, mContext.getText(R.string.voice_input_not_support_in_this_os_version), Toast.LENGTH_LONG).show();
	}
	
	public void onStartInputView() {
		
	}
}
