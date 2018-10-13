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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.animation.Animation;

import com.anysoftkeyboard.addons.AddOn;
import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.gesturetyping.GestureTypingPathDrawHelper;
import com.anysoftkeyboard.ime.InputViewBinder;
import com.anysoftkeyboard.keyboardextensions.KeyboardExtension;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.AnyKeyboard.AnyKey;
import com.anysoftkeyboard.keyboards.ExternalAnyKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.Keyboard.Key;
import com.anysoftkeyboard.keyboards.Keyboard.Row;
import com.anysoftkeyboard.keyboards.views.preview.KeyPreviewsController;
import com.anysoftkeyboard.keyboards.views.preview.KeyPreviewsManager;
import com.anysoftkeyboard.keyboards.views.preview.PreviewPopupTheme;
import com.anysoftkeyboard.prefs.AnimationsLevel;
import com.anysoftkeyboard.rx.GenericOnError;
import com.anysoftkeyboard.theme.KeyboardTheme;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

import java.util.ArrayList;
import java.util.List;

public class AnyKeyboardView extends AnyKeyboardViewWithExtraDraw implements InputViewBinder {

    private static final int DELAY_BEFORE_POPPING_UP_EXTENSION_KBD = 35;// milliseconds
    private static final String TAG = "AnyKeyboardView";
    public static final int DEFAULT_EXTENSION_POINT = -5;
    private AnimationsLevel mAnimationLevel;

    private boolean mExtensionVisible = false;
    private int mExtensionKeyboardYActivationPoint;
    private final int mExtensionKeyboardPopupOffset;
    private final int mExtensionKeyboardYDismissPoint;
    private Key mExtensionKey;
    private Key mUtilityKey;
    private Key mSpaceBarKey = null;
    private Point mFirstTouchPoint = new Point(0, 0);
    private boolean mIsFirstDownEventInsideSpaceBar = false;
    private Animation mInAnimation;

    // List of motion events for tracking gesture typing
    private final GestureTypingPathDrawHelper mGestureDrawingHelper;
    private boolean mGestureTypingPathShouldBeDrawn = false;
    private final Paint mGesturePaint = new Paint();

    protected GestureDetector mGestureDetector;
    private boolean mIsStickyExtensionKeyboard;
    private final int mWatermarkDimen;
    private final int mWatermarkMargin;
    private int mWatermarkEdgeX = 0;
    //private int mWatermarkEdgeY = 0;
    private final List<Drawable> mWatermarks = new ArrayList<>();

    public AnyKeyboardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnyKeyboardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mWatermarkDimen = getResources().getDimensionPixelOffset(R.dimen.watermark_size);
        mWatermarkMargin = getResources().getDimensionPixelOffset(R.dimen.watermark_margin);
        mGestureDetector = AnyApplication.getDeviceSpecific().createGestureDetector(getContext(), new AskGestureEventsListener(this));
        mGestureDetector.setIsLongpressEnabled(false);

        mExtensionKeyboardPopupOffset = 0;
        mDisposables.add(AnyApplication.prefs(context).getBoolean(R.string.settings_key_extension_keyboard_enabled, R.bool.settings_default_extension_keyboard_enabled)
                .asObservable().subscribe(enabled -> {
                    if (enabled) {
                        mExtensionKeyboardYActivationPoint = DEFAULT_EXTENSION_POINT;
                    } else {
                        mExtensionKeyboardYActivationPoint = Integer.MIN_VALUE;
                    }
                }, GenericOnError.onError("settings_key_extension_keyboard_enabled")));
        mExtensionKeyboardYDismissPoint = getThemedKeyboardDimens().getNormalKeyHeight();

        mInAnimation = null;

        //TODO: should come from a theme
        mGesturePaint.setColor(Color.GREEN);
        mGesturePaint.setStrokeWidth(10);
        mGesturePaint.setStyle(Paint.Style.STROKE);
        mGesturePaint.setStrokeJoin(Paint.Join.BEVEL);
        mGesturePaint.setStrokeCap(Paint.Cap.BUTT);

        mGestureDrawingHelper = new GestureTypingPathDrawHelper(context, AnyKeyboardView.this::invalidate, mGesturePaint);

        mDisposables.add(mAnimationLevelSubject.subscribe(value -> mAnimationLevel = value, GenericOnError.onError("mAnimationLevelSubject")));
        mDisposables.add(AnyApplication.prefs(context).getBoolean(R.string.settings_key_is_sticky_extesion_keyboard, R.bool.settings_default_is_sticky_extesion_keyboard)
                .asObservable().subscribe(sticky -> mIsStickyExtensionKeyboard = sticky, GenericOnError.onError("settings_key_is_sticky_extesion_keyboard")));
    }

    @Override
    protected KeyDetector createKeyDetector(final float slide) {
        return new ProximityKeyDetector();
    }

    @Override
    protected boolean onLongPress(AddOn keyboardAddOn, Key key, boolean isSticky, @NonNull PointerTracker tracker) {
        if (mAnimationLevel == AnimationsLevel.None) {
            mMiniKeyboardPopup.setAnimationStyle(0);
        } else if (mExtensionVisible && mMiniKeyboardPopup.getAnimationStyle() != R.style.ExtensionKeyboardAnimation) {
            mMiniKeyboardPopup.setAnimationStyle(R.style.ExtensionKeyboardAnimation);
        } else if (!mExtensionVisible && mMiniKeyboardPopup.getAnimationStyle() != R.style.MiniKeyboardAnimation) {
            mMiniKeyboardPopup.setAnimationStyle(R.style.MiniKeyboardAnimation);
        }
        return super.onLongPress(keyboardAddOn, key, isSticky, tracker);
    }

    @Override
    protected KeyPreviewsController createKeyPreviewManager(Context context, PreviewPopupTheme previewPopupTheme) {
        return new KeyPreviewsManager(context, this, mPreviewPopupTheme);
    }

    @Override
    protected void setKeyboard(@NonNull AnyKeyboard newKeyboard, float verticalCorrection) {
        mExtensionKey = null;
        mExtensionVisible = false;

        mUtilityKey = null;
        super.setKeyboard(newKeyboard, verticalCorrection);
        setProximityCorrectionEnabled(true);

        // looking for the space-bar, so I'll be able to detect swipes starting
        // at it
        mSpaceBarKey = null;
        for (Key aKey : newKeyboard.getKeys()) {
            if (aKey.getPrimaryCode() == KeyCodes.SPACE) {
                mSpaceBarKey = aKey;
                break;
            }
        }

        final Key lastKey = newKeyboard.getKeys().get(newKeyboard.getKeys().size() - 1);
        mWatermarkEdgeX = lastKey.x + lastKey.width;
        //mWatermarkEdgeY = lastKey.y + lastKey.height - mWatermarkMargin - mWatermarkDimen;
    }

    @Override
    protected int getKeyboardStyleResId(KeyboardTheme theme) {
        return theme.getThemeResId();
    }

    @Override
    protected int getKeyboardIconsStyleResId(KeyboardTheme theme) {
        return theme.getIconsThemeResId();
    }

    @Override
    protected final boolean isFirstDownEventInsideSpaceBar() {
        return mIsFirstDownEventInsideSpaceBar;
    }

    private long mExtensionKeyboardAreaEntranceTime = -1;

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent me) {
        if (getKeyboard() == null) {
            //I mean, if there isn't any keyboard I'm handling, what's the point?
            return false;
        }

        if (areTouchesDisabled(me)) {
            mGestureTypingPathShouldBeDrawn = false;
            return super.onTouchEvent(me);
        }

        final int action = MotionEventCompat.getActionMasked(me);

        PointerTracker pointerTracker = getPointerTracker(me);
        mGestureTypingPathShouldBeDrawn = pointerTracker.isInGestureTyping();
        if (mGestureTypingPathShouldBeDrawn) {
            mGestureDrawingHelper.handleTouchEvent(me);
        }
        // Gesture detector must be enabled only when mini-keyboard is not
        // on the screen.
        if (!mMiniKeyboardPopup.isShowing() && (!mGestureTypingPathShouldBeDrawn) && mGestureDetector != null && mGestureDetector.onTouchEvent(me)) {
            Logger.d(TAG, "Gesture detected!");
            mKeyPressTimingHandler.cancelAllMessages();
            dismissAllKeyPreviews();
            return true;
        }

        if (action == MotionEvent.ACTION_DOWN) {
            mGestureTypingPathShouldBeDrawn = false;

            mFirstTouchPoint.x = (int) me.getX();
            mFirstTouchPoint.y = (int) me.getY();
            mIsFirstDownEventInsideSpaceBar = mSpaceBarKey != null && mSpaceBarKey.isInside(mFirstTouchPoint.x, mFirstTouchPoint.y);
        } else if (action != MotionEvent.ACTION_MOVE) {
            mGestureTypingPathShouldBeDrawn = false;
        }

        // If the motion event is above the keyboard and it's a MOVE event
        // coming even before the first MOVE event into the extension area
        if (!mIsFirstDownEventInsideSpaceBar
                && me.getY() < mExtensionKeyboardYActivationPoint
                && !mMiniKeyboardPopup.isShowing()
                && !mExtensionVisible
                && action == MotionEvent.ACTION_MOVE) {
            if (mExtensionKeyboardAreaEntranceTime <= 0) {
                mExtensionKeyboardAreaEntranceTime = SystemClock.uptimeMillis();
            }

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

                    onLongPress(extKbd, mExtensionKey, mIsStickyExtensionKeyboard, getPointerTracker(me));
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

    @Override
    protected void onCancelEvent(PointerTracker tracker) {
        super.onCancelEvent(tracker);
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
    }

    public void requestInAnimation(Animation animation) {
        if (mAnimationLevel != AnimationsLevel.None) {
            mInAnimation = animation;
        } else {
            mInAnimation = null;
        }
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

        if (mGestureTypingPathShouldBeDrawn) {
            mGestureDrawingHelper.draw(canvas);
        }

        //showing any requested watermark
        float watermarkX = mWatermarkEdgeX;
        final float watermarkY = getHeight() - mWatermarkDimen - mWatermarkMargin;
        for (Drawable watermark : mWatermarks) {
            watermarkX -= (mWatermarkDimen + mWatermarkMargin);
            canvas.translate(watermarkX, watermarkY);
            watermark.draw(canvas);
            canvas.translate(-watermarkX, -watermarkY);
        }
    }

    @Override
    public void setWatermark(@NonNull List<Drawable> watermarks) {
        mWatermarks.clear();
        mWatermarks.addAll(watermarks);
        for (Drawable watermark : mWatermarks) {
            watermark.setBounds(0, 0, mWatermarkDimen, mWatermarkDimen);
        }
        invalidate();
    }
}
