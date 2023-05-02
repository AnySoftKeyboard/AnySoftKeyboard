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

package com.anysoftkeyboard;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.anysoftkeyboard.api.KeyCodes;
import com.menny.android.anysoftkeyboard.R;

public class LayoutSwitchAnimationListener
        implements android.view.animation.Animation.AnimationListener {

    public enum AnimationType {
        InPlaceSwitch,
        SwipeLeft,
        SwipeRight
    }

    public interface InputViewProvider {
        @Nullable View getInputView();
    }

    public interface OnKeyAction {
        void onKey(int keyCode);
    }

    @NonNull private final InputViewProvider mInputViewProvider;
    @NonNull private final OnKeyAction mOnKeyAction;
    @NonNull private final Context mAppContext;

    private Animation mSwitchAnimation = null;
    private Animation mSwitch2Animation = null;
    private Animation mSwipeLeftAnimation = null;
    private Animation mSwipeLeft2Animation = null;
    private Animation mSwipeRightAnimation = null;
    private Animation mSwipeRight2Animation = null;

    private AnimationType mCurrentAnimationType = AnimationType.InPlaceSwitch;
    private int mTargetKeyCode;

    public LayoutSwitchAnimationListener(
            @NonNull Context context,
            @NonNull InputViewProvider inputViewProvider,
            @NonNull OnKeyAction onKeyAction) {
        mInputViewProvider = inputViewProvider;
        mOnKeyAction = onKeyAction;
        mAppContext = context;
    }

    private void loadAnimations() {
        mSwitchAnimation = AnimationUtils.loadAnimation(mAppContext, R.anim.layout_switch_fadeout);
        mSwitchAnimation.setAnimationListener(this);
        mSwitch2Animation = AnimationUtils.loadAnimation(mAppContext, R.anim.layout_switch_fadein);

        mSwipeLeftAnimation =
                AnimationUtils.loadAnimation(mAppContext, R.anim.layout_switch_slide_out_left);
        mSwipeLeftAnimation.setAnimationListener(this);
        mSwipeLeft2Animation =
                AnimationUtils.loadAnimation(mAppContext, R.anim.layout_switch_slide_in_right);
        mSwipeRightAnimation =
                AnimationUtils.loadAnimation(mAppContext, R.anim.layout_switch_slide_out_right);
        mSwipeRightAnimation.setAnimationListener(this);
        mSwipeRight2Animation =
                AnimationUtils.loadAnimation(mAppContext, R.anim.layout_switch_slide_in_left);
    }

    private void unloadAnimations() {
        mSwitchAnimation = null;
        mSwitch2Animation = null;

        mSwipeLeftAnimation = null;
        mSwipeLeft2Animation = null;

        mSwipeRightAnimation = null;
        mSwipeRight2Animation = null;
    }

    public void doSwitchAnimation(AnimationType type, int targetKeyCode) {
        if (targetKeyCode == 0) return;
        mCurrentAnimationType = type;
        mTargetKeyCode = targetKeyCode;
        final View view = mInputViewProvider.getInputView();
        if (mSwitchAnimation != null && view != null && isKeyCodeCanUseAnimation(targetKeyCode)) {
            view.startAnimation(getStartAnimation(mCurrentAnimationType));
        } else {
            mOnKeyAction.onKey(targetKeyCode);
        }
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        final com.anysoftkeyboard.keyboards.views.AnyKeyboardView view =
                (com.anysoftkeyboard.keyboards.views.AnyKeyboardView)
                        mInputViewProvider.getInputView();
        if (view != null) view.requestInAnimation(getEndAnimation(mCurrentAnimationType));
        mOnKeyAction.onKey(mTargetKeyCode);
    }

    @Override
    public void onAnimationRepeat(Animation animation) {}

    @Override
    public void onAnimationStart(Animation animation) {}

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
            case KeyCodes.MODE_SYMBOLS:
                return true;
            default:
                return false;
        }
    }

    public void setAnimations(boolean enabled) {
        if (enabled && mSwitchAnimation == null) loadAnimations();
        else if (!enabled && mSwitchAnimation != null) unloadAnimations();
    }

    public void onDestroy() {
        unloadAnimations();
    }
}
