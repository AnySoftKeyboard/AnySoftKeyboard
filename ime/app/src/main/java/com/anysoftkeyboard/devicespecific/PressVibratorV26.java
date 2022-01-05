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
import android.os.VibrationEffect;
import android.os.Vibrator;

@TargetApi(26)
public class PressVibratorV26 extends PressVibratorV1 {
    protected VibrationEffect mVibration;
    protected VibrationEffect mLongPressVibration;
    protected static final int AMPLITUDE = VibrationEffect.DEFAULT_AMPLITUDE;

    public PressVibratorV26(Vibrator vibe) {
        super(vibe);
    }

    @Override
    public void setDuration(int duration) {
        this.mDuration = duration;
        mVibration =
                this.mDuration > 0
                        ? VibrationEffect.createOneShot(this.mDuration, AMPLITUDE)
                        : null;
    }

    @Override
    public void setLongPressDuration(int duration) {
        mLongPressDuration = duration;
        mLongPressVibration =
                mLongPressDuration > 0
                        ? VibrationEffect.createOneShot(mLongPressDuration, AMPLITUDE)
                        : null;
    }

    @Override
    public void setUseSystemVibration(boolean system, boolean systemWideHapticEnabled) {
        // not supported
    }

    @Override
    public void vibrate(boolean longPress) {
        VibrationEffect ve = longPress ? mLongPressVibration : mVibration;
        if (ve != null && !checkSuppressed()) {
            mVibe.vibrate(ve);
        }
    }
}
