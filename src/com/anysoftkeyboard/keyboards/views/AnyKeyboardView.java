/*
 * Copyright (C) 2011 AnySoftKeyboard
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.anysoftkeyboard.keyboards.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.os.SystemClock;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.anysoftkeyboard.Configuration;
import com.anysoftkeyboard.Configuration.AnimationsLevel;
import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.keyboardextensions.KeyboardExtension;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.AnyKeyboard.AnyKey;
import com.anysoftkeyboard.keyboards.ExternalAnyKeyboard;
import com.anysoftkeyboard.keyboards.GenericKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.Keyboard.Key;
import com.anysoftkeyboard.keyboards.Keyboard.Row;
import com.anysoftkeyboard.quicktextkeys.QuickTextKey;
import com.anysoftkeyboard.theme.KeyboardTheme;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

public class AnyKeyboardView extends AnyKeyboardBaseView {

	private static final int DELAY_BEFORE_POPING_UP_EXTENSION_KBD = 35;// milliseconds
	private final static String TAG = "AnyKeyboardView";

	private boolean mExtensionVisible = false;
	private final int mExtensionKeyboardYActivationPoint;
	private final int mExtensionKeyboardPopupOffset;
	private final int mExtensionKeyboardYDismissPoint;
	private Key mExtensionKey;
	private Key mUtilityKey;
	private Key mSpaceBarKey = null;
	private Point mFirstTouchPont = new Point(0, 0);
	private boolean mIsFirstDownEventInsideSpaceBar = false;
	private Animation mInAnimation;
	// private Animation mGestureSlideReachedAnimation;

	private TextView mPreviewText;
	private float mGesturePreviewTextSize;
	private int mGesturePreviewTextColor, mGesturePreviewTextColorRed,
			mGesturePreviewTextColorGreen, mGesturePreviewTextColorBlue;
	private boolean mGestureReachedThreshold = false;

	/** Whether we've started dropping move events because we found a big jump */
	// private boolean mDroppingEvents;
	/**
	 * Whether multi-touch disambiguation needs to be disabled if a real
	 * multi-touch event has occured
	 */
	// private boolean mDisableDisambiguation;
	/**
	 * The distance threshold at which we start treating the touch session as a
	 * multi-touch
	 */
	// private int mJumpThresholdSquare = Integer.MAX_VALUE;
	/** The y coordinate of the last row */
	// private int mLastRowY;

	public AnyKeyboardView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public AnyKeyboardView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		mExtensionKeyboardPopupOffset = 0;
		mExtensionKeyboardYActivationPoint = -5;
		mExtensionKeyboardYDismissPoint = getThemedKeyboardDimens()
				.getNormalKeyHeight();

		mInAnimation = null;

		mGesturePreviewTextColorRed = (mGesturePreviewTextColor & 0x00FF0000) >> 16;
		mGesturePreviewTextColorGreen = (mGesturePreviewTextColor & 0x0000FF00) >> 8;
		mGesturePreviewTextColorBlue = mGesturePreviewTextColor & 0x000000FF;
	}

	/*
	 * protected void createGestureSlideAnimation() {
	 * mGestureSlideReachedAnimation =
	 * AnimationUtils.loadAnimation(getContext().getApplicationContext(),
	 * R.anim.gesture_slide_threshold_reached); }
	 */

	protected String getKeyboardViewNameForLogging() {
		return "AnyKeyboardView";
	}

	@Override
	protected ViewGroup inflatePreviewWindowLayout(LayoutInflater inflate) {
		ViewGroup v = super.inflatePreviewWindowLayout(inflate);
		mPreviewText = (TextView) v.findViewById(R.id.key_preview_text);
		return v;
	}

	@Override
	public void setKeyboard(AnyKeyboard newKeyboard) {
		mExtensionKey = null;
		mExtensionVisible = false;

		mUtilityKey = null;
		// final Keyboard oldKeyboard = getKeyboard();
		// if (oldKeyboard instanceof AnyKeyboard) {
		// // Reset old keyboard state before switching to new keyboard.
		// ((AnyKeyboard)oldKeyboard).keyReleased();
		// }
		super.setKeyboard(newKeyboard);
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

		// looking for the spacebar, so I'll be able to detect swipes starting
		// at it
		mSpaceBarKey = null;
		for (Key aKey : newKeyboard.getKeys()) {
			if (aKey.codes[0] == (int) ' ') {
				mSpaceBarKey = aKey;
				if (AnyApplication.DEBUG)
					Log.d(TAG,
							String.format(
									"Created spacebar rect x1 %d, y1 %d, width %d, height %d",
									mSpaceBarKey.x, mSpaceBarKey.y,
									mSpaceBarKey.width, mSpaceBarKey.height));
				break;
			}
		}
	}

	@Override
	public boolean setValueFromTheme(TypedArray a, int[] padding, int attr) {
		switch (attr) {
		case R.styleable.AnySoftKeyboardTheme_previewGestureTextSize:
			mGesturePreviewTextSize = a.getDimensionPixelSize(attr, 0);
			if (AnyApplication.DEBUG)
				Log.d(TAG, "AnySoftKeyboardTheme_previewGestureTextSize "
						+ mGesturePreviewTextSize);
			break;
		case R.styleable.AnySoftKeyboardTheme_previewGestureTextColor:
			mGesturePreviewTextColor = a.getColor(attr, 0xFFF);
			if (AnyApplication.DEBUG)
				Log.d(TAG, "AnySoftKeyboardTheme_previewGestureTextColor "
						+ mGesturePreviewTextColor);
		default:
			return super.setValueFromTheme(a, padding, attr);
		}
		return true;
	}

	@Override
	protected int getKeyboardStyleResId(KeyboardTheme theme) {
		return theme.getThemeResId();
	}

	@Override
	final protected boolean isFirstDownEventInsideSpaceBar() {
		return mIsFirstDownEventInsideSpaceBar;
	}

	public void simulateLongPress(int keyCode) {
		Key key = findKeyByKeyCode(keyCode);
		if (key != null)
			super.onLongPress(getContext(), key, false, true);
	}

	private boolean invokeOnKey(int primaryCode, Key key, int multiTapIndex) {
		getOnKeyboardActionListener().onKey(primaryCode, key, multiTapIndex,
				null, false);
		return true;
	}

	public boolean isShiftLocked() {
		AnyKeyboard keyboard = getKeyboard();
		if (keyboard != null) {
			return keyboard.isShiftLocked();
		}
		return false;
	}

	public boolean setShiftLocked(boolean shiftLocked) {
		AnyKeyboard keyboard = getKeyboard();
		if (keyboard != null) {
			if (keyboard.setShiftLocked(shiftLocked)) {
				invalidateAllKeys();
				return true;
			}
		}
		return false;
	}

	@Override
	protected boolean onLongPress(Context packageContext, Key key,
			boolean isSticky, boolean requireSlideInto) {
		if (key != null && key instanceof AnyKey) {
			AnyKey anyKey = (AnyKey) key;
			if (anyKey.longPressCode != 0) {
				invokeOnKey(anyKey.longPressCode, null, 0);
				return true;
			} else if (anyKey.codes[0] == KeyCodes.QUICK_TEXT) {
				invokeOnKey(KeyCodes.QUICK_TEXT_POPUP, null, 0);
				return true;
			}
		}

		if (mAnimationLevel == AnimationsLevel.None) {
			mMiniKeyboardPopup.setAnimationStyle(0);
		} else if (mExtensionVisible
				&& mMiniKeyboardPopup.getAnimationStyle() != R.style.ExtensionKeyboardAnimation) {
			if (AnyApplication.DEBUG)
				Log.d(TAG,
						"Switching mini-keyboard animation to ExtensionKeyboardAnimation");
			mMiniKeyboardPopup
					.setAnimationStyle(R.style.ExtensionKeyboardAnimation);
		} else if (!mExtensionVisible
				&& mMiniKeyboardPopup.getAnimationStyle() != R.style.MiniKeyboardAnimation) {
			if (AnyApplication.DEBUG)
				Log.d(TAG,
						"Switching mini-keyboard animation to MiniKeyboardAnimation");
			mMiniKeyboardPopup.setAnimationStyle(R.style.MiniKeyboardAnimation);
		}

		return super.onLongPress(packageContext, key, isSticky,
				requireSlideInto);
	}

	private long mExtensionKeyboardAreaEntranceTime = -1;

	@Override
	public boolean onTouchEvent(MotionEvent me) {
		if (me.getAction() == MotionEvent.ACTION_DOWN) {
			mFirstTouchPont.x = (int) me.getX();
			mFirstTouchPont.y = (int) me.getY();
			mIsFirstDownEventInsideSpaceBar = mSpaceBarKey != null
					&& mSpaceBarKey.isInside(mFirstTouchPont.x,
							mFirstTouchPont.y);
			if (AnyApplication.DEBUG)
				Log.d(TAG, "First down point on space-bar: "
						+ mIsFirstDownEventInsideSpaceBar);

		} else if (mIsFirstDownEventInsideSpaceBar) {
			if (me.getAction() == MotionEvent.ACTION_MOVE) {
				if (AnyApplication.DEBUG)
					Log.d(TAG, "Pointer is moving while starting at SPACE BAR.");

				setGesturePreviewText(me);

				return true;
			} else if (me.getAction() == MotionEvent.ACTION_UP) {
				if (AnyApplication.DEBUG)
					Log.d(TAG, "Pointer is up while starting at SPACE BAR.");

				final int slide = getSlideDistance(me);
				final int distance = slide & 0x00FF;// removing direction
				if (distance > SLIDE_RATIO_FOR_GESTURE) {
					// gesture!!
					switch (slide & 0xFF00) {
					case DIRECTION_DOWN:
						mKeyboardActionListener.onSwipeDown(true);
						break;
					case DIRECTION_UP:
						mKeyboardActionListener.onSwipeUp(true);
						break;
					case DIRECTION_LEFT:
						mKeyboardActionListener.onSwipeLeft(true);
						break;
					case DIRECTION_RIGHT:
						mKeyboardActionListener.onSwipeRight(true);
						break;
					}
				} else {
					// just a key press
					super.onTouchEvent(me);
				}
				return true;
			}

		}
		// If the motion event is above the keyboard and it's not an UP event
		// coming
		// even before the first MOVE event into the extension area
		if (!mIsFirstDownEventInsideSpaceBar
				&& me.getY() < mExtensionKeyboardYActivationPoint
				&& !isPopupShowing() && !mExtensionVisible
				&& me.getAction() != MotionEvent.ACTION_UP) {
			if (mExtensionKeyboardAreaEntranceTime <= 0)
				mExtensionKeyboardAreaEntranceTime = System.currentTimeMillis();

			if (System.currentTimeMillis() - mExtensionKeyboardAreaEntranceTime > DELAY_BEFORE_POPING_UP_EXTENSION_KBD) {
				KeyboardExtension extKbd = ((ExternalAnyKeyboard) getKeyboard())
						.getExtensionLayout();
				if (extKbd == null || extKbd.getKeyboardResId() == -1) {
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
					dismissKeyPreview();
					if (mExtensionKey == null) {
						mExtensionKey = new AnyKey(new Row(getKeyboard()),
								getThemedKeyboardDimens());
						mExtensionKey.codes = new int[] { 0 };
						mExtensionKey.edgeFlags = 0;
						mExtensionKey.height = 1;
						mExtensionKey.width = 1;
						mExtensionKey.popupResId = extKbd.getKeyboardResId();
						mExtensionKey.x = getWidth() / 2;
						mExtensionKey.y = mExtensionKeyboardPopupOffset;
					}
					mExtensionKey.x = (int) me.getX();// so the popup will be
														// right above your
														// finger.
					onLongPress(getContext(), mExtensionKey, AnyApplication
							.getConfig().isStickyExtensionKeyboard(),
							!AnyApplication.getConfig()
									.isStickyExtensionKeyboard());
					// it is an extension..
					mMiniKeyboard.setPreviewEnabled(true);
					return true;
				}
			} else {
				return super.onTouchEvent(me);
			}
		} else if (mExtensionVisible
				&& me.getY() > mExtensionKeyboardYDismissPoint) {
			// closing the popup
			dismissPopupKeyboard();

			return true;
		} else {
			return super.onTouchEvent(me);
		}
	}

	private static final int SLIDE_RATIO_FOR_GESTURE = 250;

	private void setGesturePreviewText(MotionEvent me) {
		if (mPreviewText == null)
			return;
		// started at SPACE, so I stick with the position. This is used
		// for showing gesture info on the spacebar.
		// we'll also add the current gesture, with alpha [0...200,255].
		// if any
		final int slide = getSlideDistance(me);
		final int slideDisatance = slide & 0x00FF;// removing direction

		if (slideDisatance >= 20) {
			final boolean isGesture = slideDisatance > SLIDE_RATIO_FOR_GESTURE;

			final boolean justReachedThreashold = isGesture
					&& !mGestureReachedThreshold;
			mGestureReachedThreshold = isGesture;

			final int alpha = isGesture ? 255 : slideDisatance / 2;
			mPreviewText.setTextSize(TypedValue.COMPLEX_UNIT_PX,
					mGesturePreviewTextSize);
			int color = Color
					.argb(alpha, mGesturePreviewTextColorRed,
							mGesturePreviewTextColorGreen,
							mGesturePreviewTextColorBlue);
			mPreviewText.setTextColor(color);
			final int swipeKeyTarget;
			final Configuration cfg = AnyApplication.getConfig();
			switch (slide & 0xFF00) {// removing distance
			case DIRECTION_UP:
				swipeKeyTarget = cfg.getGestureSwipeUpFromSpacebarKeyCode();
				break;
			case DIRECTION_DOWN:
				swipeKeyTarget = cfg.getGestureSwipeDownKeyCode();
				break;
			case DIRECTION_LEFT:
				swipeKeyTarget = cfg.getGestureSwipeLeftKeyCode();
				break;
			case DIRECTION_RIGHT:
				swipeKeyTarget = cfg.getGestureSwipeRightKeyCode();
				break;
			default:
				swipeKeyTarget = KeyCodes.SPACE;
				break;
			}

			String tooltip;
			switch (swipeKeyTarget) {
			case KeyCodes.MODE_ALPHABET:
				// printing the next alpha keyboard name
				tooltip = mAskContext.getKeyboardSwitcher()
						.peekNextAlphabetKeyboard();
				break;
			case KeyCodes.MODE_SYMOBLS:
				// printing the next alpha keyboard name
				tooltip = mAskContext.getKeyboardSwitcher()
						.peekNextSymbolsKeyboard();
				break;
			default:
				tooltip = "";
				break;
			}
			mPreviewText.setText(tooltip);
			/*
			 * if (mGestureSlideReachedAnimation != null &&
			 * justReachedThreashold) {
			 * mPreviewText.startAnimation(mGestureSlideReachedAnimation); }
			 */
		} else {
			mPreviewText.setText("");
		}
	}

	private final static int DIRECTION_UP = 0x0100;
	private final static int DIRECTION_DOWN = 0x0200;
	private final static int DIRECTION_LEFT = 0x0400;
	private final static int DIRECTION_RIGHT = 0x0800;

	private int getSlideDistance(MotionEvent me) {
		final int horizontalSlide = ((int) me.getX()) - mFirstTouchPont.x;
		final int horizontalSlideAbs = Math.abs(horizontalSlide);
		final int verticalSlide = ((int) me.getY()) - mFirstTouchPont.y;
		final int verticalSlideAbs = Math.abs(verticalSlide);

		final int direction;
		final int slide;
		final int maxSlide;

		if (horizontalSlideAbs > verticalSlideAbs) {
			if (horizontalSlide > 0) {
				direction = DIRECTION_RIGHT;
			} else {
				direction = DIRECTION_LEFT;
			}
			maxSlide = mSwipeSpaceXDistanceThreshold;
			slide = Math.min(horizontalSlideAbs, maxSlide);
		} else {
			if (verticalSlide > 0) {
				direction = DIRECTION_DOWN;
			} else {
				direction = DIRECTION_UP;
			}
			maxSlide = mSwipeYDistanceThreshold;
			slide = Math.min(verticalSlideAbs, maxSlide);
		}

		final int slideRatio = (255 * slide) / maxSlide;

		return direction + slideRatio;
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
	protected boolean dismissPopupKeyboard() {
		mExtensionKeyboardAreaEntranceTime = -1;
		mExtensionVisible = false;
		return super.dismissPopupKeyboard();
	}

	public void showQuickTextPopupKeyboard(Context packageContext,
			QuickTextKey key) {
		Key popupKey = findKeyByKeyCode(KeyCodes.QUICK_TEXT);
		popupKey.popupResId = key.getPopupKeyboardResId();
		super.onLongPress(packageContext, popupKey, false, true);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		super.onSharedPreferenceChanged(sharedPreferences, key);
		/*
		 * if (mAnimationLevel == AnimationsLevel.None &&
		 * mGestureSlideReachedAnimation != null) {
		 * mGestureSlideReachedAnimation = null; } else if (mAnimationLevel !=
		 * AnimationsLevel.None && mGestureSlideReachedAnimation == null) {
		 * createGestureSlideAnimation(); }
		 */
	}

	public void openUtilityKeyboard() {
		dismissKeyPreview();
		if (mUtilityKey == null) {
			mUtilityKey = new AnyKey(new Row(getKeyboard()),
					getThemedKeyboardDimens());
			mUtilityKey.codes = new int[] { 0 };
			mUtilityKey.edgeFlags = Keyboard.EDGE_BOTTOM;
			mUtilityKey.height = 0;
			mUtilityKey.width = 0;
			mUtilityKey.popupResId = R.xml.ext_kbd_utility_utility;
			mUtilityKey.x = getWidth() / 2;
			mUtilityKey.y = getHeight()
					- getThemedKeyboardDimens().getSmallKeyHeight();
		}
		super.onLongPress(getContext(), mUtilityKey, true, false);
		mMiniKeyboard.setPreviewEnabled(true);
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
		//switching animation
		if (mAnimationLevel != AnimationsLevel.None && keyboardChanged
				&& (mInAnimation != null)) {
			startAnimation(mInAnimation);
			mInAnimation = null;
		}
		//text pop out animation
		if (mPopOutText != null && mAnimationLevel != AnimationsLevel.None) {
			final int maxVerticalTravel = getHeight()/2;
			final long animationDuration = 1200;
			final long currentAnimationTime = SystemClock.elapsedRealtime() - mPopOutTime;
			if (currentAnimationTime > animationDuration) {
				mPopOutText = null;
				mPopOutStartKey = null;
				mPopOutRTLFixedStaticLayout = null;
				if (AnyApplication.DEBUG) Log.d(TAG, "Drawing text popout done.");
			} else {
				final float animationProgress = ((float)currentAnimationTime)/((float)animationDuration);
				final float animationFactoredProgress = getPopOutAnimationInterpolator(animationProgress);
				final int y = mPopOutStartKey.y - (int)(maxVerticalTravel * animationFactoredProgress);
				final int x = mPopOutStartKey.x + mPopOutStartKey.width/2;
				final int alpha = 255 - (int)(255*animationProgress);
				if (AnyApplication.DEBUG) Log.d(TAG, "Drawing text popout '"+mPopOutText+"' at "+x+","+y+" with alpha "+alpha+". Animation progress is "+animationProgress+", and factor progress is "+animationFactoredProgress);
				//drawing
				setPaintToKeyText(mPaint);
				//will disapear over time
				mPaint.setAlpha(alpha);
				mPaint.setShadowLayer(5, 0, 0, Color.BLACK);
				//will grow over time
				mPaint.setTextSize(mPaint.getTextSize()*(1.0f + animationFactoredProgress));
				canvas.translate(x, y);
				if (mPopOutRTLFixedStaticLayout != null) {
						mPopOutRTLFixedStaticLayout.draw(canvas);
				} else {
					canvas.drawText(mPopOutText, 0, mPopOutText.length(), 0, 0, mPaint);
				}
				canvas.translate(-x, -y);
				//next frame
				postInvalidateDelayed(1000/50);//doing 50 frames per second;
			}
		}
	}
	
	/*
	 * Taken from https://android.googlesource.com/platform/frameworks/base/+/refs/heads/master/core/java/android/view/animation/DecelerateInterpolator.java
	 */
	private float getPopOutAnimationInterpolator(float input) {
        float result;
        if (mPopOutAnimationFactor == 1.0f) {
            result = (float)(1.0f - (1.0f - input) * (1.0f - input));
        } else {
            result = (float)(1.0f - Math.pow((1.0f - input), 2 * mPopOutAnimationFactor));
        }
        return result;
    }

	private CharSequence mPopOutText = null;
	private long mPopOutTime = 0;
	private Key mPopOutStartKey = null;
	private StaticLayout mPopOutRTLFixedStaticLayout = null;
	private float mPopOutAnimationFactor = 1.0f;
	
	public void popTextOutOfKey(CharSequence text, Key key) {
		if (TextUtils.isEmpty(text) || key == null) {
			Log.w(TAG, "Call for popTextOutOfKey with missing arguments!");
			return;
		}
		mPopOutText = text;
		mPopOutTime = SystemClock.elapsedRealtime();
		mPopOutStartKey = key;
		//doing all the simple things before the animation
		if (!AnyApplication.getConfig().workaround_alwaysUseDrawText()) {
			// RTL fix. But it costs
			if (AnyApplication.DEBUG) Log.d(TAG, "Will use RTL fix for drawing pop-out text.");
			final int textWidth = (int)mPaint.measureText(mPopOutText.toString());
			mPopOutRTLFixedStaticLayout = new StaticLayout(mPopOutText,
					new TextPaint(mPaint), (int) textWidth,
					Alignment.ALIGN_NORMAL, 0.0f, 0.0f, false);
		}
		//it is ok to wait for the next loop.
		postInvalidate();
	}
}
