package com.anysoftkeyboard.voice;

import android.view.inputmethod.InputMethodSubtype;

public interface VoiceInput {

	/**
	 * Starts a voice recognition
	 *
	 * @param language The language in which the recognition should be done. If
	 *            the recognition is done through the Google voice typing, the
	 *            parameter is ignored and the recognition is done using the
	 *            locale of the calling IME.
	 * @see InputMethodSubtype
	 */
	public abstract void startVoiceRecognition(String language);

	public abstract void onStartInputView();

}