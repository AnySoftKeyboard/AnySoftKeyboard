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

public class ModifierKeyState {
	private static final int RELEASING = 0;
	private int mPhysicalState = RELEASING;
	private static final int PRESSING = 1;
	private static final int INACTIVE = 0;
	private int mLogicalState = INACTIVE;
	private static final int ACTIVE = 1;
	private static final int LOCKED = 2;
	private long mActiveStateStartTime = 0l;
	private boolean mMomentaryPress = false;

	public void onPress() {
		mPhysicalState = PRESSING;
	}

	public void onOtherKeyPressed() {
		if (mPhysicalState == PRESSING) {
			mMomentaryPress = true;
		}
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
					break;
				case ACTIVE:
					if (doubleClickTime > (SystemClock.elapsedRealtime() - mActiveStateStartTime)) {
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
		mActiveStateStartTime = 0l;
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
			mActiveStateStartTime = SystemClock.elapsedRealtime();
		}
	}
}
