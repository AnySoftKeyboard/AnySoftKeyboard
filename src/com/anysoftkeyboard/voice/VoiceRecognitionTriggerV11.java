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

import android.inputmethodservice.InputMethodService;
import android.util.Log;

/**
 * Triggers a voice recognition by using {@link ImeTrigger} or
 * {@link IntentApiTrigger}.
 */
public class VoiceRecognitionTriggerV11 extends VoiceRecognitionTrigger {
	private static final String TAG = "ASK_VoiceRecognitionTriggerV11";
	
	public VoiceRecognitionTriggerV11(InputMethodService inputMethodService) {
		super(inputMethodService);
	}

	protected Trigger getTrigger() {
        if (ImeTrigger.isInstalled(mInputMethodService)) {
            return getImeTrigger();
        } else {
        	Log.d(TAG, "ImeTrigger is not installed");
            return super.getTrigger();
        }
    }
	
	private Trigger getImeTrigger() {
        return new ImeTrigger(mInputMethodService);
    }
}
