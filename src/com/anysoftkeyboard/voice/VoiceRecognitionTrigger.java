/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.anysoftkeyboard.voice;

import com.anysoftkeyboard.utils.Log;

import android.content.Intent;
import android.inputmethodservice.InputMethodService;

/**
 * Triggers a voice recognition by using {@link ImeTrigger} or
 * {@link IntentApiTrigger}.
 */
public class VoiceRecognitionTrigger implements VoiceInput {
	private static final String TAG = "ASK_VoiceRecognitionTrigger";

	protected final InputMethodService mInputMethodService;

	private Trigger mTrigger;

	public VoiceRecognitionTrigger(VoiceInputDiagram diagram) {
		mInputMethodService = diagram.getInputMethodService();
		mTrigger = getTrigger();
	}

	protected Trigger getTrigger() {
		if (IntentApiTrigger.isInstalled(mInputMethodService)) {
			return getIntentTrigger();
		} else {
			Log.d(TAG, "IntentApiTrigger is not installed");
			return null;
		}
	}

	private Trigger getIntentTrigger() {
		return new IntentApiTrigger(mInputMethodService);
	}

	/**
	 * Starts a voice recognition. The language of the recognition will match
	 * the voice search language settings, or the locale of the calling IME.
	 */
	public void startVoiceRecognition() {
		startVoiceRecognition(null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.anysoftkeyboard.voice.VoiceInput#startVoiceRecognition(java.lang.
	 * String)
	 */
	public void startVoiceRecognition(String language) {
		// The trigger is refreshed as the system may have changed in the
		// meanwhile.
		mTrigger = getTrigger();
		if (mTrigger != null) {
			mTrigger.startVoiceRecognition(language);
		} else {
			Intent notInstalledActivity = new Intent(
					mInputMethodService.getApplicationContext(),
					VoiceInputNotInstalledActivity.class);
			notInstalledActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mInputMethodService.getApplicationContext().startActivity(
					notInstalledActivity);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.anysoftkeyboard.voice.VoiceInput#onStartInputView()
	 */
	public void onStartInputView() {
		if (mTrigger != null) {
			mTrigger.onStartInputView();
		}
	}
}
