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
import android.view.GestureDetector;
import android.view.inputmethod.InputConnection;

import com.anysoftkeyboard.IndirectlyInstantiated;
import com.anysoftkeyboard.base.dictionaries.WordComposer;

@TargetApi(3)
@IndirectlyInstantiated
public class DeviceSpecific_V3 implements DeviceSpecific {
    public DeviceSpecific_V3() {
    }

    @Override
    public String getApiLevel() {
        return "DeviceSpecific_V3";
    }

    @Override
    public MultiTouchSupportLevel getMultiTouchSupportLevel(Context appContext) {
        return MultiTouchSupportLevel.None;
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
}