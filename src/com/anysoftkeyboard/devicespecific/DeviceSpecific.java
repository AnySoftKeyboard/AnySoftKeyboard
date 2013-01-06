package com.anysoftkeyboard.devicespecific;

import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.view.GestureDetector;
import android.view.inputmethod.InputConnection;

import com.anysoftkeyboard.WordComposer;
import com.anysoftkeyboard.dictionaries.DictionaryFactory;
import com.anysoftkeyboard.voice.VoiceInput;

public interface DeviceSpecific {

	public String getApiLevel();

	public MultiTouchSupportLevel getMultiTouchSupportLevel(Context appContext);
	
	public GestureDetector createGestureDetector(Context appContext, AskOnGestureListener listener);

	//TODO move to the new FrankenRobot mechanism
	public VoiceInput createVoiceInput(InputMethodService ime);
	
	//TODO move to the new FrankenRobot mechanism
	public Clipboard getClipboard(Context appContext);

	public void commitCorrectionToInputConnection(InputConnection ic, WordComposer word);
}
