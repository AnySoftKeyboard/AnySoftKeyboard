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
import android.os.Build;
import android.view.inputmethod.CorrectionInfo;
import android.view.inputmethod.InputConnection;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DeviceSpecificV11 extends DeviceSpecificLowest {
    @Override
    public String getApiLevel() {
        return "DeviceSpecificV11";
    }


    @Override
    public void commitCorrectionToInputConnection(InputConnection ic, int wordOffsetInInput, CharSequence oldWord, CharSequence newWord) {
        super.commitCorrectionToInputConnection(ic, wordOffsetInInput, oldWord, newWord);
        CorrectionInfo correctionInfo = new CorrectionInfo(wordOffsetInInput, oldWord, newWord);

        ic.commitCorrection(correctionInfo);
    }

    @Override
    public boolean isHardwareAcceleratedCanvas(Canvas canvas) {
        return canvas != null && canvas.isHardwareAccelerated();
    }

    @Override
    public Clipboard createClipboard(Context applicationContext) {
        return new ClipboardV11(applicationContext);
    }
}
