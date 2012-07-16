
package com.anysoftkeyboard;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.anysoftkeyboard.Configuration.AnimationsLevel;
import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.keyboards.views.AnyKeyboardView;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

public class LayoutSwitchAnimationListener implements
        android.view.animation.Animation.AnimationListener {

    static enum AnimationType {
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
    private final Configuration mConfig;

    LayoutSwitchAnimationListener(AnySoftKeyboard ime) {
        mIme = ime;
        mConfig = AnyApplication.getConfig();

        mSwitchAnimation = AnimationUtils.loadAnimation(mIme.getApplicationContext(),
                R.anim.layout_switch_fadeout);
        mSwitchAnimation.setAnimationListener(this);
        mSwitch2Animation = AnimationUtils.loadAnimation(mIme.getApplicationContext(),
                R.anim.layout_switch_fadein);
        
        mSwipeLeftAnimation = AnimationUtils.loadAnimation(mIme.getApplicationContext(),
                R.anim.layout_switch_slide_out_left);
        mSwipeLeftAnimation.setAnimationListener(this);
        mSwipeLeft2Animation = AnimationUtils.loadAnimation(mIme.getApplicationContext(),
                R.anim.layout_switch_slide_in_right);
        
        mSwipeRightAnimation = AnimationUtils.loadAnimation(mIme.getApplicationContext(),
                R.anim.layout_switch_slide_out_right);
        mSwipeRightAnimation.setAnimationListener(this);
        mSwipeRight2Animation = AnimationUtils.loadAnimation(mIme.getApplicationContext(),
                R.anim.layout_switch_slide_in_left);
    }

    void doSwitchAnimation(AnimationType type, int targetKeyCode) {
        mCurrentAnimationType = type;
        mTargetKeyCode = targetKeyCode;
        final AnyKeyboardView view = mIme.getInputView();
        if (mConfig.getAnimationsLevel() == AnimationsLevel.Full && view != null
                && isKeyCodeCanUseAnimation(targetKeyCode)) {
            view.startAnimation(getStartAnimation(mCurrentAnimationType));
        } else {
            mIme.onKey(mTargetKeyCode, null, -1, new int[] {
                    mTargetKeyCode
            }, false);
        }
    }

    public void onAnimationEnd(Animation animation) {
        final AnyKeyboardView view = mIme.getInputView();
        if (view != null)
            view.requestInAnimation(getEndAnimation(mCurrentAnimationType));
        mIme.onKey(mTargetKeyCode, null, -1, new int[] {
                mTargetKeyCode
        }, false);
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
}
