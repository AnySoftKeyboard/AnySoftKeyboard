package com.anysoftkeyboard.voice;

import net.evendanan.frankenrobot.Diagram;
import android.inputmethodservice.InputMethodService;
import android.view.inputmethod.InputMethodSubtype;

public interface VoiceInput {

	public static class VoiceInputDiagram extends Diagram<VoiceInput> {
		private final InputMethodService mIME;

		public VoiceInputDiagram(InputMethodService ime) {
			mIME = ime;
		}
		
		public InputMethodService getInputMethodService() {
			return mIME;
		}
	}
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