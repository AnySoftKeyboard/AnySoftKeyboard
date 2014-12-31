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
import android.graphics.Canvas;
import android.view.inputmethod.CorrectionInfo;
import android.view.inputmethod.InputConnection;

import com.anysoftkeyboard.IndirectlyInstantiated;
import com.anysoftkeyboard.WordComposer;

@TargetApi(11)
@IndirectlyInstantiated
public class DeviceSpecific_V11 extends DeviceSpecific_V8 {
    @Override
    public String getApiLevel() {
        return "DeviceSpecific_V11";
    }

    @Override
    public void commitCorrectionToInputConnection(InputConnection ic,
                                                  WordComposer word) {
        super.commitCorrectionToInputConnection(ic, word);
        CorrectionInfo correctionInfo = new CorrectionInfo(
                word.globalCursorPosition() - word.getTypedWord().length(),
                word.getTypedWord(), word.getPreferredWord());

        ic.commitCorrection(correctionInfo);
    }

    @Override
    public boolean isHardwareAcceleratedCanvas(Canvas canvas) {
        return canvas != null || canvas.isHardwareAccelerated();
    }
}
