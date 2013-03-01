/*
 * Copyright (c) 2013 Menny Even-Danan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.anysoftkeyboard.voice;

import android.inputmethodservice.InputMethodService;
import android.view.inputmethod.InputMethodSubtype;
import net.evendanan.frankenrobot.Diagram;

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
     *                 the recognition is done through the Google voice typing, the
     *                 parameter is ignored and the recognition is done using the
     *                 locale of the calling IME.
     * @see InputMethodSubtype
     */
    public abstract void startVoiceRecognition(String language);

    public abstract void onStartInputView();

}