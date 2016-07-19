
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

package com.anysoftkeyboard;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.anysoftkeyboard.AskPrefs.AnimationsLevel;
import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.keyboards.views.AnyKeyboardView;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

public class LayoutSwitchAnimationListener implements
        android.view.animation.Animation.AnimationListener, OnSharedPreferenceChangeListener {

    enum AnimationType {
        InPlaceSwitch,
        SwipeLeft,
        SwipeRight
    }

    private final AnySoftKeyboard mIme;

    private Animation mSwitchAnimation = null;
    private Animation mSwitch2Animation = null;
    private Animation mSwipeLeftAnimation = null;
    private Animation mSwipeLeft2Animation = null;
    private Animation mSwipeRightAnimation = null;
    private Animation mSwipeRight2Animation = null;

    private AnimationType mCurrentAnimationType = AnimationType.InPlaceSwitch;
    private int mTargetKeyCode;

    LayoutSwitchAnimationListener(AnySoftKeyboard ime) {
        mIme = ime;

        AnyApplication.getConfig().addChangedListener(this);

        setAnimations();
    }

    private void loadAnimations() {
        mSwitchAnimation = AnimationUtils.loadAnimation(mIme.getApplicationContext(), R.anim.layout_switch_fadeout);
        mSwitchAnimation.setAnimationListener(this);
        mSwitch2Animation = AnimationUtils.loadAnimation(mIme.getApplicationContext(), R.anim.layout_switch_fadein);

        mSwipeLeftAnimation = AnimationUtils.loadAnimation(mIme.getApplicationContext(), R.anim.layout_switch_slide_out_left);
        mSwipeLeftAnimation.setAnimationListener(this);
        mSwipeLeft2Animation = AnimationUtils.loadAnimation(mIme.getApplicationContext(), R.anim.layout_switch_slide_in_right);
        mSwipeRightAnimation = AnimationUtils.loadAnimation(mIme.getApplicationContext(), R.anim.layout_switch_slide_out_right);
        mSwipeRightAnimation.setAnimationListener(this);
        mSwipeRight2Animation = AnimationUtils.loadAnimation(mIme.getApplicationContext(), R.anim.layout_switch_slide_in_left);
    }

    private void unloadAnimations() {
        mSwitchAnimation = null;
        mSwitch2Animation = null;

        mSwipeLeftAnimation = null;
        mSwipeLeft2Animation = null;

        mSwipeRightAnimation = null;
        mSwipeRight2Animation = null;
    }

    void doSwitchAnimation(AnimationType type, int targetKeyCode) {
        mCurrentAnimationType = type;
        mTargetKeyCode = targetKeyCode;
        final AnyKeyboardView view = mIme.getInputView();
        if (mSwitchAnimation != null && view != null && isKeyCodeCanUseAnimation(targetKeyCode)) {
            view.startAnimation(getStartAnimation(mCurrentAnimationType));
        } else {
            mIme.onKey(mTargetKeyCode, null, -1, new int[]{mTargetKeyCode}, false/*not directly pressed the UI key*/);
        }
    }

    public void onAnimationEnd(Animation animation) {
        final AnyKeyboardView view = mIme.getInputView();
        if (view != null)
            view.requestInAnimation(getEndAnimation(mCurrentAnimationType));
        mIme.onKey(mTargetKeyCode, null, -1, new int[]{mTargetKeyCode},  false/*not directly pressed the UI key*/);
    }

    public void onAnimationRepeat(Animation animation) {
    }

    public void onAnimationStart(Animation animation) {
    }

    private Animation getStartAnimation(AnimationType type) {
        switch (type) {
            case SwipeLeft:
                return mSwipeLeftAnimation;
            case SwipeRight:
                return mSwipeRightAnimation;
            case InPlaceSwitch:
            default:
                return mSwitchAnimation;
        }
    }

    private Animation getEndAnimation(AnimationType type) {
        switch (type) {
            case SwipeLeft:
                return mSwipeLeft2Animation;
            case SwipeRight:
                return mSwipeRight2Animation;
            case InPlaceSwitch:
            default:
                return mSwitch2Animation;
        }
    }

    private static boolean isKeyCodeCanUseAnimation(final int keyCode) {
        switch (keyCode) {
            case KeyCodes.KEYBOARD_CYCLE:
            case KeyCodes.KEYBOARD_CYCLE_INSIDE_MODE:
            case KeyCodes.KEYBOARD_MODE_CHANGE:
            case KeyCodes.KEYBOARD_REVERSE_CYCLE:
            case KeyCodes.MODE_ALPHABET:
            case KeyCodes.MODE_SYMOBLS:
                return true;
            default:
                return false;
        }
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        setAnimations();
    }

    private void setAnimations() {
        if (AnyApplication.getConfig().getAnimationsLevel() == AnimationsLevel.Full
                && mSwitchAnimation == null)
            loadAnimations();
        else if (AnyApplication.getConfig().getAnimationsLevel() != AnimationsLevel.Full
                && mSwitchAnimation != null)
            unloadAnimations();
    }

    void onDestroy() {
        unloadAnimations();
        AnyApplication.getConfig().removeChangedListener(this);
    }
}
