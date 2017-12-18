/*
 * Copyright (c) 2016 Menny Even-Danan
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

package com.anysoftkeyboard.ime;

import com.anysoftkeyboard.LayoutSwitchAnimationListener;
import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.utils.Logger;

public abstract class AnySoftKeyboardSwipeListener extends AnySoftKeyboardPopText {

    private int mFirstDownKeyCode;

    private LayoutSwitchAnimationListener mSwitchAnimator;

    @Override
    public void onCreate() {
        super.onCreate();
        mSwitchAnimator = new LayoutSwitchAnimationListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSwitchAnimator.onDestroy();
    }

    @Override
    public void onSwipeRight(boolean twoFingersGesture) {
        final int keyCode;
        if (mFirstDownKeyCode == KeyCodes.DELETE) {
            keyCode = KeyCodes.DELETE_WORD;
        } else {
            keyCode = mAskPrefs.getGestureSwipeRightKeyCode(mFirstDownKeyCode == KeyCodes.SPACE, twoFingersGesture);
        }
        Logger.d(TAG, "onSwipeRight with first-down " + mFirstDownKeyCode + ((twoFingersGesture) ? " + two-fingers" : "") + " => code " + keyCode);
        if (keyCode != 0)
            mSwitchAnimator.doSwitchAnimation(LayoutSwitchAnimationListener.AnimationType.SwipeRight, keyCode);
    }

    @Override
    public void onSwipeLeft(boolean twoFingersGesture) {
        final int keyCode;
        if (mFirstDownKeyCode == KeyCodes.DELETE) {
            keyCode = KeyCodes.DELETE_WORD;
        } else {
            keyCode = mAskPrefs.getGestureSwipeLeftKeyCode(mFirstDownKeyCode == KeyCodes.SPACE, twoFingersGesture);
        }
        Logger.d(TAG, "onSwipeLeft with first-down " + mFirstDownKeyCode + ((twoFingersGesture) ? " + two-fingers" : "") + " => code " + keyCode);
        if (keyCode != 0)
            mSwitchAnimator.doSwitchAnimation(LayoutSwitchAnimationListener.AnimationType.SwipeLeft, keyCode);
    }

    @Override
    public void onSwipeDown() {
        final int keyCode = mAskPrefs.getGestureSwipeDownKeyCode();
        Logger.d(TAG, "onSwipeDown => code " + keyCode);
        if (keyCode != 0)
            onKey(keyCode, null, -1, new int[]{keyCode}, false/*not directly pressed the UI key*/);
    }

    @Override
    public void onSwipeUp() {
        final int keyCode = mAskPrefs.getGestureSwipeUpKeyCode(mFirstDownKeyCode == KeyCodes.SPACE);
        Logger.d(TAG, "onSwipeUp with first-down " + mFirstDownKeyCode + " => code " + keyCode);
        if (keyCode != 0)
            onKey(keyCode, null, -1, new int[]{keyCode}, false/*not directly pressed the UI key*/);
    }

    @Override
    public void onPinch() {
        final int keyCode = mAskPrefs.getGesturePinchKeyCode();
        Logger.d(TAG, "onPinch => code %d", keyCode);
        if (keyCode != 0)
            onKey(keyCode, null, -1, new int[]{keyCode}, false/*not directly pressed the UI key*/);
    }

    @Override
    public void onSeparate() {
        final int keyCode = mAskPrefs.getGestureSeparateKeyCode();
        Logger.d(TAG, "onSeparate => code %d", keyCode);
        if (keyCode != 0)
            onKey(keyCode, null, -1, new int[]{keyCode}, false/*not directly pressed the UI key*/);
    }

    @Override
    public void onFirstDownKey(int primaryCode) {
        mFirstDownKeyCode = primaryCode;
    }
}
