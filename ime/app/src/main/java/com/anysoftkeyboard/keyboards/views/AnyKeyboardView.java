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
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.animation.Animation;
import androidx.annotation.NonNull;
import com.anysoftkeyboard.addons.AddOn;
import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.gesturetyping.GestureTrailTheme;
import com.anysoftkeyboard.gesturetyping.GestureTypingPathDraw;
import com.anysoftkeyboard.gesturetyping.GestureTypingPathDrawHelper;
import com.anysoftkeyboard.ime.InputViewBinder;
import com.anysoftkeyboard.keyboardextensions.KeyboardExtension;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.AnyKeyboard.AnyKey;
import com.anysoftkeyboard.keyboards.ExternalAnyKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.Keyboard.Row;
import com.anysoftkeyboard.prefs.AnimationsLevel;
import com.anysoftkeyboard.rx.GenericOnError;
import com.anysoftkeyboard.theme.KeyboardTheme;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;
import java.util.ArrayList;
import java.util.List;
import net.evendanan.pixel.MainChild;

public class AnyKeyboardView extends AnyKeyboardViewWithExtraDraw
    implements InputViewBinder, ActionsStripSupportedChild, MainChild {

  private static final int DELAY_BEFORE_POPPING_UP_EXTENSION_KBD = 35; // milliseconds
  private static final String TAG = "ASKKbdView";
  private final int mExtensionKeyboardPopupOffset;
  private final Point mFirstTouchPoint = new Point(0, 0);
  private final GestureDetector mGestureDetector;
  private final int mWatermarkDimen;
  private final int mWatermarkMargin;
  private final int mMinimumKeyboardBottomPadding;
  private final List<Drawable> mWatermarks = new ArrayList<>();
  private AnimationsLevel mAnimationLevel;
  private boolean mExtensionVisible = false;
  private int mExtensionKeyboardYActivationPoint;
  private int mExtensionKeyboardYDismissPoint;
  private int mDismissYValue = Integer.MAX_VALUE;
  private Keyboard.Key mExtensionKey;
  private Keyboard.Key mUtilityKey;
  private Keyboard.Key mSpaceBarKey = null;
  private boolean mIsFirstDownEventInsideSpaceBar = false;
  private Animation mInAnimation;
  // List of motion events for tracking gesture typing
  private GestureTypingPathDraw mGestureDrawingHelper;
  private boolean mGestureTypingPathShouldBeDrawn = false;
  private boolean mIsStickyExtensionKeyboard;
  private int mExtraBottomOffset;
  private int mWatermarkEdgeX = 0;
  private long mExtensionKeyboardAreaEntranceTime = -1;

  public AnyKeyboardView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public AnyKeyboardView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);

    mWatermarkDimen = getResources().getDimensionPixelOffset(R.dimen.watermark_size);
    mWatermarkMargin = getResources().getDimensionPixelOffset(R.dimen.watermark_margin);
    mMinimumKeyboardBottomPadding = mWatermarkDimen + mWatermarkMargin;
    mExtraBottomOffset = mMinimumKeyboardBottomPadding;
    mGestureDetector =
        AnyApplication.getDeviceSpecific()
            .createGestureDetector(getContext(), new AskGestureEventsListener(this));
    mGestureDetector.setIsLongpressEnabled(false);

    mExtensionKeyboardPopupOffset = 0;
    mDisposables.add(
        AnyApplication.prefs(context)
            .getBoolean(
                R.string.settings_key_extension_keyboard_enabled,
                R.bool.settings_default_extension_keyboard_enabled)
            .asObservable()
            .subscribe(
                enabled -> {
                  if (enabled) {
                    mExtensionKeyboardYActivationPoint =
                        getResources()
                            .getDimensionPixelOffset(R.dimen.extension_keyboard_reveal_point);
                  } else {
                    mExtensionKeyboardYActivationPoint = Integer.MIN_VALUE;
                  }
                },
                GenericOnError.onError("settings_key_extension_keyboard_enabled")));

    mInAnimation = null;

    mDisposables.add(
        mAnimationLevelSubject.subscribe(
            value -> mAnimationLevel = value, GenericOnError.onError("mAnimationLevelSubject")));
    mDisposables.add(
        AnyApplication.prefs(context)
            .getBoolean(
                R.string.settings_key_is_sticky_extesion_keyboard,
                R.bool.settings_default_is_sticky_extesion_keyboard)
            .asObservable()
            .subscribe(
                sticky -> mIsStickyExtensionKeyboard = sticky,
                GenericOnError.onError("settings_key_is_sticky_extesion_keyboard")));
  }

  @Override
  public void setBottomOffset(int extraBottomOffset) {
    mExtraBottomOffset = Math.max(extraBottomOffset, mMinimumKeyboardBottomPadding);
    setPadding(
        getPaddingLeft(),
        getPaddingTop(),
        getPaddingRight(),
        (int) Math.max(mExtraBottomOffset, getThemedKeyboardDimens().getPaddingBottom()));
    requestLayout();
  }

  @Override
  public void setPadding(int left, int top, int right, int bottom) {
    // this will ensure that even if something is setting the padding (say, in setTheme
    // function)
    // we will still keep the bottom-offset requirement.
    super.setPadding(left, top, right, Math.max(mExtraBottomOffset, bottom));
  }

  @Override
  public void setKeyboardTheme(@NonNull KeyboardTheme theme) {
    super.setKeyboardTheme(theme);

    mExtensionKeyboardYDismissPoint = getThemedKeyboardDimens().getNormalKeyHeight();

    mGestureDrawingHelper =
        GestureTypingPathDrawHelper.create(
            this::invalidate,
            GestureTrailTheme.fromThemeResource(
                getContext(),
                theme.getPackageContext(),
                theme.getResourceMapping(),
                theme.getGestureTrailThemeResId()));
  }

  @Override
  protected KeyDetector createKeyDetector(final float slide) {
    return new ProximityKeyDetector();
  }

  @Override
  protected boolean onLongPress(
      AddOn keyboardAddOn, Keyboard.Key key, boolean isSticky, @NonNull PointerTracker tracker) {
    if (mAnimationLevel == AnimationsLevel.None) {
      mMiniKeyboardPopup.setAnimationStyle(0);
    } else if (mExtensionVisible
        && mMiniKeyboardPopup.getAnimationStyle() != R.style.ExtensionKeyboardAnimation) {
      mMiniKeyboardPopup.setAnimationStyle(R.style.ExtensionKeyboardAnimation);
    } else if (!mExtensionVisible
        && mMiniKeyboardPopup.getAnimationStyle() != R.style.MiniKeyboardAnimation) {
      mMiniKeyboardPopup.setAnimationStyle(R.style.MiniKeyboardAnimation);
    }
    return super.onLongPress(keyboardAddOn, key, isSticky, tracker);
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
    for (Keyboard.Key aKey : newKeyboard.getKeys()) {
      if (aKey.getPrimaryCode() == KeyCodes.SPACE) {
        mSpaceBarKey = aKey;
        break;
      }
    }

    final Keyboard.Key lastKey = newKeyboard.getKeys().get(newKeyboard.getKeys().size() - 1);
    mWatermarkEdgeX = Keyboard.Key.getEndX(lastKey);
    mDismissYValue =
        newKeyboard.getHeight()
            + getResources().getDimensionPixelOffset(R.dimen.dismiss_keyboard_point);
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

  @Override
  public boolean onTouchEvent(@NonNull MotionEvent me) {
    if (getKeyboard() == null) {
      // I mean, if there isn't any keyboard I'm handling, what's the point?
      return false;
    }

    if (areTouchesDisabled(me)) {
      mGestureTypingPathShouldBeDrawn = false;
      return super.onTouchEvent(me);
    }

    final int action = me.getActionMasked();

    PointerTracker pointerTracker = getPointerTracker(me);
    mGestureTypingPathShouldBeDrawn = pointerTracker.isInGestureTyping();
    mGestureDrawingHelper.handleTouchEvent(me);
    // Gesture detector must be enabled only when mini-keyboard is not
    // on the screen.
    if (!mMiniKeyboardPopup.isShowing()
        && !mGestureTypingPathShouldBeDrawn
        && mGestureDetector.onTouchEvent(me)) {
      Logger.d(TAG, "Gesture detected!");
      mKeyPressTimingHandler.cancelAllMessages();
      dismissAllKeyPreviews();
      return true;
    }

    if (action == MotionEvent.ACTION_DOWN) {
      mGestureTypingPathShouldBeDrawn = false;

      mFirstTouchPoint.x = (int) me.getX();
      mFirstTouchPoint.y = (int) me.getY();
      mIsFirstDownEventInsideSpaceBar =
          mSpaceBarKey != null && mSpaceBarKey.isInside(mFirstTouchPoint.x, mFirstTouchPoint.y);
    } else if (action != MotionEvent.ACTION_MOVE) {
      mGestureTypingPathShouldBeDrawn = false;
    }

    // If the motion event is outside (up or down) the keyboard and it's a MOVE event
    // coming even before the first MOVE event into the extension/bottom area
    if (action == MotionEvent.ACTION_MOVE && me.getY() > mDismissYValue) {
      MotionEvent cancel =
          MotionEvent.obtain(
              me.getDownTime(),
              me.getEventTime(),
              MotionEvent.ACTION_CANCEL,
              me.getX(),
              me.getY(),
              0);
      super.onTouchEvent(cancel);
      mGestureDetector.onTouchEvent(cancel);
      cancel.recycle();
      mKeyboardActionListener.onSwipeDown();
      // Touch handled
      return true;
    }
    if (!mIsFirstDownEventInsideSpaceBar
        && me.getY() < mExtensionKeyboardYActivationPoint
        && !mMiniKeyboardPopup.isShowing()
        && !mExtensionVisible
        && action == MotionEvent.ACTION_MOVE) {
      if (mExtensionKeyboardAreaEntranceTime <= 0) {
        mExtensionKeyboardAreaEntranceTime = SystemClock.uptimeMillis();
      }

      if (SystemClock.uptimeMillis() - mExtensionKeyboardAreaEntranceTime
          > DELAY_BEFORE_POPPING_UP_EXTENSION_KBD) {
        KeyboardExtension extKbd = ((ExternalAnyKeyboard) getKeyboard()).getExtensionLayout();
        if (extKbd == null || extKbd.getKeyboardResId() == AddOn.INVALID_RES_ID) {
          Logger.i(TAG, "No extension keyboard");
          return super.onTouchEvent(me);
        } else {
          // telling the main keyboard that the last touch was
          // canceled
          MotionEvent cancel =
              MotionEvent.obtain(
                  me.getDownTime(),
                  me.getEventTime(),
                  MotionEvent.ACTION_CANCEL,
                  me.getX(),
                  me.getY(),
                  0);
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
          mExtensionKey.x = ((int) me.getX());

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
  protected void onUpEvent(PointerTracker tracker, int x, int y, long eventTime) {
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
      mUtilityKey.y =
          getHeight() - getPaddingBottom() - getThemedKeyboardDimens().getSmallKeyHeight();
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
    if (mAnimationLevel != AnimationsLevel.None && keyboardChanged && (mInAnimation != null)) {
      startAnimation(mInAnimation);
      mInAnimation = null;
    }

    if (mGestureTypingPathShouldBeDrawn) {
      mGestureDrawingHelper.draw(canvas);
    }

    // showing any requested watermark
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
