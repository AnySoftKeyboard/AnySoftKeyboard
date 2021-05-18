/*
 * Copyright (c) 2021 Daniel Parks
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
import android.os.VibrationEffect;
import android.os.Vibrator;

@TargetApi(29)
public class PressVibratorV29 extends PressVibratorV26 {
    private boolean systemVibe;
    private static final int PRESS_PREDEFINED = VibrationEffect.EFFECT_CLICK;
    private static final int LONG_PRESS_PREDEFINED = VibrationEffect.EFFECT_HEAVY_CLICK;

    public PressVibratorV29(Vibrator vibe) {
        super(vibe);
    }

    @Override
    public void setDuration(int duration) {
        this.duration = duration;
        if (!systemVibe) vibration = this.duration > 0 ? VibrationEffect.createOneShot(this.duration, AMPLITUDE) : null;
    }

    @Override
    public void setLongPressDuration(int duration) {
        longPressDuration = duration;
        if (!systemVibe) longPressVibration = longPressDuration > 0 ? VibrationEffect.createOneShot(longPressDuration, AMPLITUDE) : null;
    }

    @Override
    public void setUseSystemVibration(boolean system) {
        systemVibe = system;
        if (system) {
            vibration = VibrationEffect.createPredefined(PRESS_PREDEFINED);
            longPressVibration = VibrationEffect.createPredefined(LONG_PRESS_PREDEFINED);
        } else {
            setDuration(duration);
            setLongPressDuration(longPressDuration);
        }
    }
}
