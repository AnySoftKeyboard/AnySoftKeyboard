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

package com.anysoftkeyboard.devicespecific;


import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.GestureDetector;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

import com.anysoftkeyboard.keyboards.KeyboardAddOnAndBuilder;

import java.util.List;

@TargetApi(3)
public class DeviceSpecificV3 implements DeviceSpecific {
    public DeviceSpecificV3() {
    }

    @Override
    public String getApiLevel() {
        return "DeviceSpecificV3";
    }

    @Override
    public GestureDetector createGestureDetector(Context appContext,
                                                 AskOnGestureListener listener) {
        return new GestureDetector(appContext, listener, null);
    }

    @Override
    public void commitCorrectionToInputConnection(InputConnection ic, int wordOffsetInInput, CharSequence oldWord, CharSequence newWord) {
        ic.commitText(newWord, 1);
    }

    @Override
    public boolean isHardwareAcceleratedCanvas(Canvas canvas) {
        return false;
    }

    @Override
    public void reportInputMethodSubtypes(@NonNull InputMethodManager inputMethodManager, @NonNull String imeId, @NonNull List<KeyboardAddOnAndBuilder> builders) {
        //no-op till API 14
    }

    @Override
    public void reportCurrentInputMethodSubtypes(@NonNull InputMethodManager inputMethodManager, @NonNull String imeId, @NonNull IBinder token, @Nullable String keyboardLocale, @NonNull CharSequence keyboardId) {
        //no-op till API 14
    }
}