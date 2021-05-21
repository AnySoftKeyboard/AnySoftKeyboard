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

import android.os.Vibrator;
import android.support.annotation.VisibleForTesting;

public abstract class PressVibrator {
    private static boolean mSkip = false;
    protected Vibrator mVibe;

    public PressVibrator(Vibrator vibe) {
        this.mVibe = vibe;
    }

    public abstract void setDuration(int duration);

    public abstract void setLongPressDuration(int duration);

    public void setUseSystemVibration(boolean system) {
        // empty; not supported if not overridden
    }

    public abstract void vibrate(boolean longPress);

    public static void suppressNextVibration() {
        mSkip = true;
    }

    protected static boolean checkSuppressed() {
        boolean result = mSkip;
        mSkip = false;
        return result;
    }

    @VisibleForTesting
    public Vibrator getVibrator() {
        return mVibe;
    }
}
