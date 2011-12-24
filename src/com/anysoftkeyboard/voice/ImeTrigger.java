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

import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.os.Build;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;

import java.util.List;
import java.util.Map;

/**
 * Triggers a voice recognition using Google voice typing.
 */
class ImeTrigger implements Trigger {

    private static final String VOICE_IME_SUBTYPE_MODE = "voice";

    private static final String VOICE_IME_PACKAGE_PREFIX = "com.google.android";

    private final InputMethodService mInputMethodService;

    public ImeTrigger(InputMethodService inputMethodService) {
        mInputMethodService = inputMethodService;
    }

    /**
     * Switches to Voice IME.
     */
    public void startVoiceRecognition(String language) {
        InputMethodManager inputMethodManager = getInputMethodManager(mInputMethodService);

        InputMethodInfo inputMethodInfo = getVoiceImeInputMethodInfo(inputMethodManager);

        if (inputMethodInfo == null) {
            return;
        }

        inputMethodManager.setInputMethodAndSubtype(mInputMethodService.getWindow().getWindow()
                .getAttributes().token,
                inputMethodInfo.getId(),
                getVoiceImeSubtype(inputMethodManager, inputMethodInfo));
    }

    private static InputMethodManager getInputMethodManager(InputMethodService inputMethodService) {
        return (InputMethodManager) inputMethodService
                .getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    private InputMethodSubtype getVoiceImeSubtype(
            InputMethodManager inputMethodManager, InputMethodInfo inputMethodInfo)
            throws SecurityException,
            IllegalArgumentException {
        Map<InputMethodInfo, List<InputMethodSubtype>> map = inputMethodManager
                .getShortcutInputMethodsAndSubtypes();
        List<InputMethodSubtype> list = map.get(inputMethodInfo);
        if (list != null && list.size() > 0) {
            return list.get(0);
        } else {
            return null;
        }
    }

    private static InputMethodInfo getVoiceImeInputMethodInfo(InputMethodManager inputMethodManager)
            throws SecurityException, IllegalArgumentException {
        for (InputMethodInfo inputMethodInfo : inputMethodManager.getEnabledInputMethodList()) {
            for (int i = 0; i < inputMethodInfo.getSubtypeCount(); i++) {
                InputMethodSubtype subtype = inputMethodInfo.getSubtypeAt(i);
                if (VOICE_IME_SUBTYPE_MODE.equals(subtype.getMode())) {
                    if (inputMethodInfo.getComponent().getPackageName()
                            .startsWith(VOICE_IME_PACKAGE_PREFIX)) {
                        return inputMethodInfo;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Returns true if an implementation of Voice IME is installed.
     */
    public static boolean isInstalled(InputMethodService inputMethodService) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            return false;
        }

        InputMethodInfo inputMethodInfo = getVoiceImeInputMethodInfo(
                getInputMethodManager(inputMethodService));

        if (inputMethodInfo == null) {
            return false;
        }

        return inputMethodInfo.getSubtypeCount() > 0;
    }

    public void onStartInputView() {
        // Empty. Voice IME pastes the recognition result directly into the text
        // view
    }
}
