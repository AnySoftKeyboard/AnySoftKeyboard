/*
 * Copyright (c) 2021 Menny Even-Danan
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
import android.os.Vibrator;
import androidx.annotation.NonNull;

@TargetApi(26)
public class DeviceSpecificV26 extends DeviceSpecificV24 {
    @Override
    public String getApiLevel() {
        return "DeviceSpecificV26";
    }

    @Override
    public PressVibrator createPressVibrator(@NonNull Vibrator vibe) {
        return new PressVibratorV26(vibe);
    }
}
