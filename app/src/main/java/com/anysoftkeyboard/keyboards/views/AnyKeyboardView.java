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

package com.anysoftkeyboard.keyboards.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.view.MotionEventCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;

import com.anysoftkeyboard.AskPrefs;
import com.anysoftkeyboard.AskPrefs.AnimationsLevel;
import com.anysoftkeyboard.addons.AddOn;
import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.base.utils.CompatUtils;
import com.anysoftkeyboard.ime.InputViewBinder;
import com.anysoftkeyboard.keyboardextensions.KeyboardExtension;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.AnyKeyboard.AnyKey;
import com.anysoftkeyboard.keyboards.ExternalAnyKeyboard;
import com.anysoftkeyboard.keyboards.GenericKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.Keyboard.Key;
import com.anysoftkeyboard.keyboards.Keyboard.Row;
import com.anysoftkeyboard.quicktextkeys.ui.QuickTextViewFactory;
import com.anysoftkeyboard.theme.KeyboardTheme;
import com.anysoftkeyboard.utils.Logger;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.BuildConfig;
import com.menny.android.anysoftkeyboard.R;

public class AnyKeyboardView extends AnyKeyboardViewWithMiniKeyboard implements InputViewBinder {

    private static final int DELAY_BEFORE_POPPING_UP_EXTENSION_KBD = 35;// milliseconds
    private final static String TAG = "AnyKeyboardView";
    private static final int TEXT_POP_OUT_ANIMATION_DURATION = 1200;

    private boolean mExtensionVisible = false;
    private final int mExtensionKeyboardYActivationPoint;
    private final int mExtensionKeyboardPopupOffset;
    private final int mExtensionKeyboardYDismissPoint;
    private Key mExtensionKey;
    private Key mUtilityKey;
    private Key mSpaceBarKey = null;
    private Point mFirstTouchPoint = new Point(0, 0);
    private boolean mIsFirstDownEventInsideSpaceBar = false;
    private Animation mInAnimation;

    private Paint mBuildTypeSignPaint;
    private final CharSequence mBuildTypeSignText = BuildConfig.TESTING_BUILD ? BuildConfig.DEBUG ? "α\uD83D\uDD25" : "β\uD83D\uDC09" : null;

    protected GestureDetector mGestureDetector;

    public AnyKeyboardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnyKeyboardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mGestureDetector = AnyApplication.getDeviceSpecific().createGestureDetector(getContext(), new AskGestureEventsListener(this));
        mGestureDetector.setIsLongpressEnabled(false);

        mExtensionKeyboardPopupOffset = 0;
        mExtensionKeyboardYActivationPoint = -5;
        mExtensionKeyboardYDismissPoint = getThemedKeyboardDimens().getNormalKeyHeight();

        mInAnimation = null;
    }

    @Override
    protected KeyDetector createKeyDetector(final float slide) {
        return new ProximityKeyDetector();
    }

    @Override
    protected boolean onLongPress(AddOn keyboardAddOn, Key key, boolean isSticky, @NonNull PointerTracker tracker) {
        if (mAnimationLevel == AskPrefs.AnimationsLevel.None) {
            mMiniKeyboardPopup.setAnimationStyle(0);
        } else if (mExtensionVisible && mMiniKeyboardPopup.getAnimationStyle() != R.style.ExtensionKeyboardAnimation) {
            mMiniKeyboardPopup.setAnimationStyle(R.style.ExtensionKeyboardAnimation);
        } else if (!mExtensionVisible && mMiniKeyboardPopup.getAnimationStyle() != R.style.MiniKeyboardAnimation) {
            mMiniKeyboardPopup.setAnimationStyle(R.style.MiniKeyboardAnimation);
        }
        return super.onLongPress(keyboardAddOn, key, isSticky, tracker);
    }

    @Override
    protected void setKeyboard(AnyKeyboard newKeyboard, float verticalCorrection) {
        mExtensionKey = null;
        mExtensionVisible = false;

        mUtilityKey = null;
        super.setKeyboard(newKeyboard, verticalCorrection);
        if (newKeyboard != null && newKeyboard instanceof GenericKeyboard
                && ((GenericKeyboard) newKeyboard).disableKeyPreviews()) {
            // Phone keyboard never shows popup preview (except language
            // switch).
            setPreviewEnabled(false);
        } else {
            setPreviewEnabled(AnyApplication.getConfig().getShowKeyPreview());
        }
        // TODO: For now! should be a calculated value
        // lots of key : true
        // some keys: false
        setProximityCorrectionEnabled(true);
        // One-seventh of the keyboard width seems like a reasonable threshold
        // mJumpThresholdSquare = newKeyboard.getMinWidth() / 7;
        // mJumpThresholdSquare *= mJumpThresholdSquare;
        // Assuming there are 4 rows, this is the coordinate of the last row
        // mLastRowY = (newKeyboard.getHeight() * 3) / 4;
        // setKeyboardLocal(newKeyboard);

        // looking for the space-bar, so I'll be able to detect swipes starting
        // at it
        mSpaceBarKey = null;
        if (newKeyboard != null) {
            for (Key aKey : newKeyboard.getKeys()) {
                if (aKey.getPrimaryCode() == KeyCodes.SPACE) {
                    mSpaceBarKey = aKey;
                    break;
                }
            }
        }
    }

    @Override
    public boolean setValueFromTheme(TypedArray remoteTypedArray, int[] padding, int localAttrId, int remoteTypedArrayIndex) {
        if (BuildConfig.TESTING_BUILD) {
            if (mBuildTypeSignPaint == null) {
                mBuildTypeSignPaint = new Paint();
                mBuildTypeSignPaint.setColor(Color.RED);
            }
            switch (localAttrId) {
                case R.attr.keyTextSize:
                    final float textSize = remoteTypedArray.getDimensionPixelSize(remoteTypedArrayIndex, 18);
                    mBuildTypeSignPaint.setTextSize(textSize / 2f);
                    break;
            }
        }
        return super.setValueFromTheme(remoteTypedArray, padding, localAttrId, remoteTypedArrayIndex);
    }

    @Override
    protected int getKeyboardStyleResId(KeyboardTheme theme) {
        return theme.getThemeResId();
    }

    @Override
    final protected boolean isFirstDownEventInsideSpaceBar() {
        return mIsFirstDownEventInsideSpaceBar;
    }

    public void showQuickKeysView(Key popupKey) {
        ensureMiniKeyboardInitialized();
        View innerView = QuickTextViewFactory.createQuickTextView(getContext(), mChildKeyboardActionListener, (int) getMiniKeyboard().getLabelTextSize(), getMiniKeyboard().getKeyTextColor());
        CompatUtils.setViewBackgroundDrawable(innerView, getMiniKeyboard().getBackground());

        innerView.measure(
                View.MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.AT_MOST));

        final int[] locationInWindow = getLocationInWindow();
        int popupY = popupKey.y + locationInWindow[1];
        popupY += popupKey.height;//this is shown at the bottom of the key
        popupY += getPaddingTop();
        popupY -= innerView.getMeasuredHeight();
        popupY -= innerView.getPaddingBottom();

        setPopupKeyboardWithView(0, popupY, locationInWindow[0], popupY - locationInWindow[1], innerView);
        setPopupStickinessValues(true, false, 0, 0);
    }

    private long mExtensionKeyboardAreaEntranceTime = -1;

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent me) {
        if (getKeyboard() == null)//I mean, if there isn't any keyboard I'm handling, what's the point?
            return false;

        if (areTouchesDisabled()) return super.onTouchEvent(me);

        final int action = MotionEventCompat.getActionMasked(me);

        // Gesture detector must be enabled only when mini-keyboard is not
        // on the screen.
        if (!mMiniKeyboardPopup.isShowing() && mGestureDetector != null && mGestureDetector.onTouchEvent(me)) {
            Logger.d(TAG, "Gesture detected!");
            mKeyPressTimingHandler.cancelAllMessages();
            dismissAllKeyPreviews();
            return true;
        }

        if (action == MotionEvent.ACTION_DOWN) {
            mFirstTouchPoint.x = (int) me.getX();
            mFirstTouchPoint.y = (int) me.getY();
            mIsFirstDownEventInsideSpaceBar = mSpaceBarKey != null && mSpaceBarKey.isInside(mFirstTouchPoint.x, mFirstTouchPoint.y);
        }
        // If the motion event is above the keyboard and it's a MOVE event
        // coming even before the first MOVE event into the extension area
        if (!mIsFirstDownEventInsideSpaceBar
                && me.getY() < mExtensionKeyboardYActivationPoint
                && !mMiniKeyboardPopup.isShowing()
                && !mExtensionVisible
                && action == MotionEvent.ACTION_MOVE) {
            if (mExtensionKeyboardAreaEntranceTime <= 0)
                mExtensionKeyboardAreaEntranceTime = SystemClock.uptimeMillis();

            if (SystemClock.uptimeMillis() - mExtensionKeyboardAreaEntranceTime > DELAY_BEFORE_POPPING_UP_EXTENSION_KBD) {
                KeyboardExtension extKbd = ((ExternalAnyKeyboard) getKeyboard()).getExtensionLayout();
                if (extKbd == null || extKbd.getKeyboardResId() == AddOn.INVALID_RES_ID) {
                    Logger.i(TAG, "No extension keyboard");
                    return super.onTouchEvent(me);
                } else {
                    // telling the main keyboard that the last touch was
                    // canceled
                    MotionEvent cancel = MotionEvent.obtain(me.getDownTime(),
                            me.getEventTime(), MotionEvent.ACTION_CANCEL,
                            me.getX(), me.getY(), 0);
                    super.onTouchEvent(cancel);
                    cancel.recycle();

                    mExtensionVisible = true;
                    dismissAllKeyPreviews();
                    if (mExtensionKey == null) {
                        mExtensionKey = new AnyKey(new Row(getKeyboard()), getThemedKeyboardDimens());
                        mExtensionKey.edgeFlags = 0;
                        mExtensionKey.height = 1;
                        mExtensionKey.width = 1;
                        mExtensionKey.popupResId = extKbd.getKeyboardResId();
                        mExtensionKey.externalResourcePopupLayout = mExtensionKey.popupResId != 0;
                        mExtensionKey.x = getWidth() / 2;
                        mExtensionKey.y = mExtensionKeyboardPopupOffset;
                    }
                    // so the popup will be right above your finger.
                    mExtensionKey.x = (int) me.getX();

                    onLongPress(extKbd, mExtensionKey, AnyApplication.getConfig().isStickyExtensionKeyboard(), getPointerTracker(me));
                    // it is an extension..
                    getMiniKeyboard().setPreviewEnabled(true);
                    return true;
                }
            } else {
                return super.onTouchEvent(me);
            }
        } else if (mExtensionVisible && me.getY() > mExtensionKeyboardYDismissPoint) {
            // closing the popup
            dismissPopupKeyboard();
            return true;
        } else {
            return super.onTouchEvent(me);
        }
    }

    @Override
    public void onViewNotRequired() {
        super.onViewNotRequired();
        mGestureDetector = null;
    }

    @Override
    protected void onUpEvent(PointerTracker tracker, int x, int y,
                             long eventTime) {
        super.onUpEvent(tracker, x, y, eventTime);
        mIsFirstDownEventInsideSpaceBar = false;
    }

    protected void onCancelEvent(PointerTracker tracker, int x, int y,
                                 long eventTime) {
        super.onCancelEvent(tracker, x, y, eventTime);
        mIsFirstDownEventInsideSpaceBar = false;
    }

    @Override
    public boolean dismissPopupKeyboard() {
        mExtensionKeyboardAreaEntranceTime = -1;
        mExtensionVisible = false;
        return super.dismissPopupKeyboard();
    }

    public void openUtilityKeyboard() {
        dismissAllKeyPreviews();
        if (mUtilityKey == null) {
            mUtilityKey = new AnyKey(new Row(getKeyboard()), getThemedKeyboardDimens());
            mUtilityKey.edgeFlags = Keyboard.EDGE_BOTTOM;
            mUtilityKey.height = 0;
            mUtilityKey.width = 0;
            mUtilityKey.popupResId = R.xml.ext_kbd_utility_utility;
            mUtilityKey.externalResourcePopupLayout = false;
            mUtilityKey.x = getWidth() / 2;
            mUtilityKey.y = getHeight() - getThemedKeyboardDimens().getSmallKeyHeight();
        }
        showMiniKeyboardForPopupKey(mDefaultAddOn, mUtilityKey, true);
        getMiniKeyboard().setPreviewEnabled(true);
    }

    public void requestInAnimation(Animation animation) {
        if (mAnimationLevel != AnimationsLevel.None)
            mInAnimation = animation;
        else
            mInAnimation = null;
    }

    @Override
    public void onDraw(Canvas canvas) {
        final boolean keyboardChanged = mKeyboardChanged;
        super.onDraw(canvas);
        // switching animation
        if (mAnimationLevel != AnimationsLevel.None && keyboardChanged
                && (mInAnimation != null)) {
            startAnimation(mInAnimation);
            mInAnimation = null;
        }
        // text pop out animation
        if (mPopOutText != null && mAnimationLevel != AnimationsLevel.None) {
            final int maxVerticalTravel = getHeight() / 2;
            final long currentAnimationTime = SystemClock.elapsedRealtime() - mPopOutTime;
            if (currentAnimationTime > TEXT_POP_OUT_ANIMATION_DURATION) {
                mPopOutText = null;
            } else {
                final float popOutPositionProgress = ((float) currentAnimationTime) / ((float) TEXT_POP_OUT_ANIMATION_DURATION);
                final float animationProgress = mPopOutTextReverting ? 1f - popOutPositionProgress : popOutPositionProgress;
                final float animationInterpolatorPosition = getPopOutAnimationInterpolator(false, animationProgress);
                final int y = mPopOutStartPoint.y - (int) (maxVerticalTravel * animationInterpolatorPosition);
                final int x = mPopOutStartPoint.x;
                final int alpha = mPopOutTextReverting ?
                        (int) (255 * animationProgress)
                        : 255 - (int) (255 * animationProgress);
                // drawing
                setPaintToKeyText(mPaint);
                // will disappear over time
                mPaint.setAlpha(alpha);
                mPaint.setShadowLayer(5, 0, 0, Color.BLACK);
                // will grow over time
                mPaint.setTextSize(
                        mPaint.getTextSize() * (1.0f + animationInterpolatorPosition));
                canvas.translate(x, y);
                canvas.drawText(mPopOutText, 0, mPopOutText.length(), 0, 0, mPaint);
                canvas.translate(-x, -y);
                //we're doing reverting twice much faster
                if (mPopOutTextReverting) {
                    mPopOutTime = mPopOutTime - (int) (60 * popOutPositionProgress);
                }
                // next frame
                postInvalidateDelayed(1000 / 60);// doing 60 frames per second;
            }
        }
        //showing alpha/beta icon if needed
        if (BuildConfig.TESTING_BUILD) {
            final float textSizeForBuildSign = mPaint.getTextSize() / 2f;
            final float x = getWidth() - (mBuildTypeSignText.length() * textSizeForBuildSign);
            final float y = getHeight() - textSizeForBuildSign;
            canvas.translate(x, y);
            canvas.drawText(mBuildTypeSignText, 0, mBuildTypeSignText.length(), 0, 0, mBuildTypeSignPaint);
            canvas.translate(-x, -y);
        }
    }

    /*
     * Taken from Android's DecelerateInterpolator.java and AccelerateInterpolator.java
     */
    private static float getPopOutAnimationInterpolator(final boolean isAccelerating, final float input) {
        return isAccelerating ?
                input * input :
                (1.0f - (1.0f - input) * (1.0f - input));
    }

    private boolean mPopOutTextReverting = false;
    private CharSequence mPopOutText = null;
    private long mPopOutTime = 0;
    private final Point mPopOutStartPoint = new Point();

    public void revertPopTextOutOfKey() {
        if (TextUtils.isEmpty(mPopOutText)) return;

        if (!mPopOutTextReverting) {
            mPopOutTextReverting = true;
            //re-setting the mPopOutTime to reflect the time required to revert back
            final long currentAnimationTime = SystemClock.elapsedRealtime() - mPopOutTime;
            final long animationTimeLeft = TEXT_POP_OUT_ANIMATION_DURATION - currentAnimationTime;
            mPopOutTime = SystemClock.elapsedRealtime() - animationTimeLeft;
        }
    }

    public void popTextOutOfKey(CharSequence text) {
        if (TextUtils.isEmpty(text)) {
            Logger.w(TAG, "Call for popTextOutOfKey with missing text argument!");
            return;
        }
        if (!AnyApplication.getConfig().workaround_alwaysUseDrawText())
            return;// not doing it with StaticLayout

        mPopOutTextReverting = false;
        //performing "toString" so we'll have a separate copy of the CharSequence,
        // and not the original object which I fear is a reference copy (hence may be changed).
        mPopOutText = text.toString();
        mPopOutTime = SystemClock.elapsedRealtime();
        mPopOutStartPoint.x = mFirstTouchPoint.x;
        mPopOutStartPoint.y = mFirstTouchPoint.y;
        // it is ok to wait for the next loop.
        postInvalidate();
    }
}
