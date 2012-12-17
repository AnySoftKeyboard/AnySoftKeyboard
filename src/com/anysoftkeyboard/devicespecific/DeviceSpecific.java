package com.anysoftkeyboard.devicespecific;

import com.anysoftkeyboard.WordComposer;
import com.anysoftkeyboard.backup.CloudBackupRequester;
import com.anysoftkeyboard.dictionaries.DictionaryFactory;
import com.anysoftkeyboard.voice.VoiceInput;

import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.inputmethod.InputConnection;

public interface DeviceSpecific {

	public abstract String getApiLevel();
	
	public WMotionEvent createMotionEventWrapper(MotionEvent nativeMotionEvent);
	
	public MultiTouchSupportLevel getMultiTouchSupportLevel(Context appContext);
	
	public GestureDetector createGestureDetector(Context appContext, AskOnGestureListener listener);

	public CloudBackupRequester createCloudBackupRequester(Context appContext);

	public DictionaryFactory createDictionaryFactory();

	public VoiceInput createVoiceInput(InputMethodService ime);
	
	public Clipboard getClipboard(Context appContext);

	public void commitCorrectionToInputConnection(InputConnection ic, WordComposer word);
}
