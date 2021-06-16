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

@TargetApi(1)
public class PressVibratorV1 extends PressVibrator {
    protected int mDuration;
    protected int mLongPressDuration;

    public PressVibratorV1(Vibrator vibe) {
        super(vibe);
    }

    @Override
    public void setDuration(int duration) {
        this.mDuration = duration;
    }

    @Override
    public void setLongPressDuration(int duration) {
        mLongPressDuration = duration;
    }

    @Override
    public void vibrate(boolean longPress) {
        int dur = longPress ? mLongPressDuration : mDuration;
        if (dur > 0 && !checkSuppressed()) {
            mVibe.vibrate(dur);
        }
    }
}
