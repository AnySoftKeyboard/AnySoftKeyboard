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

package com.anysoftkeyboard.utils;

import android.os.SystemClock;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ModifierKeyState {
    @IntDef({INACTIVE, ACTIVE, LOCKED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface LogicalState {
    }

    @IntDef({RELEASING, PRESSING})
    @Retention(RetentionPolicy.SOURCE)
    public @interface PhysicalState {
    }

    private static final int RELEASING = 0;
    private static final int PRESSING = 1;
    @PhysicalState
    private int mPhysicalState = RELEASING;

    private static final int INACTIVE = 0;
    private static final int ACTIVE = 1;
    private static final int LOCKED = 2;
    @LogicalState
    private int mLogicalState = INACTIVE;

    private long mActiveStateStartTime = 0L;
    private boolean mMomentaryPress = false;
    private boolean mConsumed = false;

    private final boolean mSupportsLockedState;

    public ModifierKeyState(boolean supportsLockedState) {
        mSupportsLockedState = supportsLockedState;
    }

    public void onPress() {
        mPhysicalState = PRESSING;
        mConsumed = false;
    }

    public void onOtherKeyPressed() {
        if (mPhysicalState == PRESSING) {
            mMomentaryPress = true;
        } else if (mLogicalState == ACTIVE) {
            mConsumed = true;
        }
    }

    public boolean onOtherKeyReleased() {
        if (mPhysicalState != PRESSING && mLogicalState == ACTIVE && mConsumed) {
            //another key was pressed and release while this key was active:
            //it means that this modifier key was consumed
            mLogicalState = INACTIVE;
            return true;
        }
        return false;
    }

    public void onRelease(final int doubleClickTime) {
        mPhysicalState = RELEASING;
        if (mMomentaryPress) {
            mLogicalState = INACTIVE;
        } else {
            switch (mLogicalState) {
                case INACTIVE:
                    mLogicalState = ACTIVE;
                    mActiveStateStartTime = SystemClock.elapsedRealtime();
                    mConsumed = false;
                    break;
                case ACTIVE:
                    if (mSupportsLockedState && doubleClickTime > (SystemClock.elapsedRealtime() - mActiveStateStartTime)) {
                        mLogicalState = LOCKED;
                    } else {
                        mLogicalState = INACTIVE;
                    }
                    break;
                case LOCKED:
                    mLogicalState = INACTIVE;
                    break;
            }
        }
        mMomentaryPress = false;
    }

    public void reset() {
        mPhysicalState = RELEASING;
        mMomentaryPress = false;
        mLogicalState = INACTIVE;
        mActiveStateStartTime = 0L;
        mConsumed = false;
    }

    public boolean isPressed() {
        return mPhysicalState == PRESSING;
    }

    public boolean isActive() {
        return mPhysicalState == PRESSING || mLogicalState != INACTIVE;
    }

    public boolean isLocked() {
        return mPhysicalState != PRESSING && mLogicalState == LOCKED;
    }

    /**
     * Sets the modifier state to active (or inactive) if possible.
     * By possible, I mean, if it is LOCKED, it will stay locked.
     */
    public void setActiveState(boolean active) {
        if (mLogicalState == LOCKED) return;
        mLogicalState = active ? ACTIVE : INACTIVE;

        if (mLogicalState == ACTIVE) {
            //setting the start time to zero, so LOCKED state will not
            //be activated without actual user's double-clicking
            mActiveStateStartTime = 0;
            mConsumed = false;
        }
    }

    public void toggleLocked() {
        final boolean toUnLock = mLogicalState == LOCKED;
        reset();
        if (toUnLock) {
            mLogicalState = INACTIVE;
        } else {
            mLogicalState = LOCKED;
        }
    }
}
