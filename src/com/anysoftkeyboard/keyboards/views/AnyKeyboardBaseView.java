/*
 * Copyright (C) 2011 AnySoftKeyboard
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.anysoftkeyboard.keyboards.views;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.anysoftkeyboard.AnyKeyboardContextProvider;
import com.anysoftkeyboard.Configuration.AnimationsLevel;
import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.devicespecific.AskOnGestureListener;
import com.anysoftkeyboard.devicespecific.WMotionEvent;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.AnyKeyboard.AnyKey;
import com.anysoftkeyboard.keyboards.AnyPopupKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.Keyboard.Key;
import com.anysoftkeyboard.keyboards.KeyboardDimens;
import com.anysoftkeyboard.theme.KeyboardTheme;
import com.anysoftkeyboard.theme.KeyboardThemeFactory;
import com.anysoftkeyboard.utils.IMEUtil.GCUtils;
import com.anysoftkeyboard.utils.IMEUtil.GCUtils.MemRelatedOperation;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

public class AnyKeyboardBaseView extends View implements
		PointerTracker.UIProxy, OnSharedPreferenceChangeListener {
	static final String TAG = "ASKKbdViewBase";

	public static final int NOT_A_TOUCH_COORDINATE = -1;

	// Timing constants
	private final int mKeyRepeatInterval;

	// Miscellaneous constants
	public static final int NOT_A_KEY = -1;

	private static final int[] LONG_PRESSABLE_STATE_SET = { android.R.attr.state_long_pressable };
	// private static final int NUMBER_HINT_VERTICAL_ADJUSTMENT_PIXEL = -1;

	private static final int[] DRAWABLE_STATE_MODIFIER_NORMAL = new int[] {};
	private static final int[] DRAWABLE_STATE_MODIFIER_PRESSED = new int[] { android.R.attr.state_pressed };
	private static final int[] DRAWABLE_STATE_MODIFIER_LOCKED = new int[] { android.R.attr.state_checked };

	private static final int[] DRAWABLE_STATE_ACTION_NORMAL = new int[] {};
	private static final int[] DRAWABLE_STATE_ACTION_DONE = new int[] { R.attr.action_done };
	private static final int[] DRAWABLE_STATE_ACTION_SEARCH = new int[] { R.attr.action_search };
	private static final int[] DRAWABLE_STATE_ACTION_GO = new int[] { R.attr.action_go };

	// XML attribute
	private float mKeyTextSize;
	private FontMetrics mTextFM;
	private ColorStateList mKeyTextColor;
	private Typeface mKeyTextStyle = Typeface.DEFAULT;
	private float mLabelTextSize;
	private FontMetrics mLabelFM;
	private float mKeyboardNameTextSize;
	private FontMetrics mKeyboardNameFM;
	private ColorStateList mKeyboardNameTextColor;
	private float mHintTextSize;
	private ColorStateList mHintTextColor;
	private FontMetrics mHintTextFM;
	private int mHintLabelAlign;
	private int mHintLabelVAlign;
	private String mHintOverflowLabel = null;
	private int mSymbolColorScheme = 0;
	private int mShadowColor;
	private int mShadowRadius;
	private int mShadowOffsetX;
	private int mShadowOffsetY;
	private Drawable mKeyBackground;
	/* keys icons */
	private Drawable mShiftIcon;
	private Drawable mControlIcon;
	private Drawable mActionKeyIcon;
	private Drawable mDeleteKeyIcon;
	private Drawable mSpaceKeyIcon;
	private Drawable mTabKeyIcon;
	private Drawable mCancelKeyIcon;
	private Drawable mGlobeKeyIcon;
	private Drawable mMicKeyIcon;
	private Drawable mSettingsKeyIcon;

	private Drawable mArrowRightKeyIcon;
	private Drawable mArrowLeftKeyIcon;
	private Drawable mArrowUpKeyIcon;
	private Drawable mArrowDownKeyIcon;

	private Drawable mMoveHomeKeyIcon;
	private Drawable mMoveEndKeyIcon;

	private float mBackgroundDimAmount;
	private float mKeyHysteresisDistance;
	private float mVerticalCorrection;
	private int mPreviewOffset;
	// private int mPreviewHeight;
	// private final int mPopupLayout = R.layout.keyboard_popup;

	// Main keyboard
	private AnyKeyboard mKeyboard;
	private String mKeyboardName;

	private Key[] mKeys;

	// Key preview popup
	private ViewGroup mPreviewLayut;
	private TextView mPreviewText;
	private ImageView mPreviewIcon;
	private PopupWindow mPreviewPopup;
	private boolean mStaticLocationPopupWindow = false;
	private int mPreviewKeyTextSize;
	private int mPreviewLabelTextSize;
	private int mPreviewPaddingWidth = -1;
	private int mPreviewPaddingHeight = -1;
	// private int mPreviewTextSizeLarge;
	private int[] mOffsetInWindow;
	private int mOldPreviewKeyIndex = NOT_A_KEY;
	private boolean mShowPreview = true;
	private final boolean mShowTouchPoints = false;
	private int mPopupPreviewOffsetX;
	private int mPopupPreviewOffsetY;
	private int mWindowY;
	private int mPopupPreviewDisplayedY;
	private final int mDelayBeforePreview;
	private final int mDelayAfterPreview;

	// Popup mini keyboard
	protected PopupWindow mMiniKeyboardPopup;
	protected AnyKeyboardBaseView mMiniKeyboard = null;;
	private boolean mMiniKeyboardVisible = false;
	private View mMiniKeyboardParent;
	// private final WeakHashMap<Key, AnyKeyboardBaseView> mMiniKeyboardCache =
	// new WeakHashMap<Key, AnyKeyboardBaseView>();
	private int mMiniKeyboardOriginX;
	private int mMiniKeyboardOriginY;
	private long mMiniKeyboardPopupTime;
	private int[] mWindowOffset;
	private final float mMiniKeyboardSlideAllowance;
	private int mMiniKeyboardTrackerId;
	protected AnimationsLevel mAnimationLevel = AnyApplication.getConfig()
			.getAnimationsLevel();

	/** Listener for {@link OnKeyboardActionListener}. */
	OnKeyboardActionListener mKeyboardActionListener;

	private final ArrayList<PointerTracker> mPointerTrackers = new ArrayList<PointerTracker>();

	// TODO: Let the PointerTracker class manage this pointer queue
	final PointerQueue mPointerQueue = new PointerQueue();

	private final boolean mHasDistinctMultitouch;
	private int mOldPointerCount = 1;

	protected KeyDetector mKeyDetector = new ProximityKeyDetector();

	// Swipe gesture detector
	private GestureDetector mGestureDetector;

	private final SwipeTracker mSwipeTracker = new SwipeTracker();
	int mSwipeVelocityThreshold;
	int mSwipeXDistanceThreshold;
	int mSwipeYDistanceThreshold;
	int mSwipeSpaceXDistanceThreshold;
	int mScrollXDistanceThreshold;
	int mScrollYDistanceThreshold;
	final boolean mDisambiguateSwipe;
	// private boolean mInScrollGesture = false;

	// Drawing
	/** Whether the keyboard bitmap needs to be redrawn before it's blitted. **/
	private boolean mDrawPending;
	/** The dirty region in the keyboard bitmap */
	private final Rect mDirtyRect = new Rect();
	/** The keyboard bitmap for faster updates */
	private Bitmap mBuffer;
	/**
	 * Notes if the keyboard just changed, so that we could possibly reallocate
	 * the mBuffer.
	 */
	protected boolean mKeyboardChanged;
	private Key mInvalidatedKey;
	/** The canvas for the above mutable keyboard bitmap */
	// private Canvas mCanvas;
	private final Paint mPaint;
	private final Rect mKeyBackgroundPadding;
	private final Rect mClipRegion = new Rect(0, 0, 0, 0);
	/*
	 * NOTE: this field EXISTS ONLY AFTER THE CTOR IS FINISHED!
	 */
	private AnyKeyboardContextProvider mAskContext;

	private final UIHandler mHandler = new UIHandler();

	private Drawable mPreviewKeyBackground;

	private int mPreviewKeyTextColor;

	private final KeyboardDimensFromTheme mKeyboardDimens = new KeyboardDimensFromTheme();

	class UIHandler extends Handler {
		private static final int MSG_POPUP_PREVIEW = 1;
		private static final int MSG_DISMISS_PREVIEW = 2;
		private static final int MSG_REPEAT_KEY = 3;
		private static final int MSG_LONGPRESS_KEY = 4;

		private boolean mInKeyRepeat;

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_POPUP_PREVIEW:
				showKey(msg.arg1, (PointerTracker) msg.obj);
				break;
			case MSG_DISMISS_PREVIEW:
				mPreviewPopup.dismiss();
				break;
			case MSG_REPEAT_KEY: {
				final PointerTracker tracker = (PointerTracker) msg.obj;
				tracker.repeatKey(msg.arg1);
				startKeyRepeatTimer(mKeyRepeatInterval, msg.arg1, tracker);
				break;
			}
			case MSG_LONGPRESS_KEY: {
				final PointerTracker tracker = (PointerTracker) msg.obj;
				openPopupIfRequired(msg.arg1, tracker);
				break;
			}
			}
		}

		public void popupPreview(long delay, int keyIndex,
				PointerTracker tracker) {
			removeMessages(MSG_POPUP_PREVIEW);
			if (mPreviewPopup.isShowing()
					&& mPreviewLayut.getVisibility() == VISIBLE) {
				// Show right away, if it's already visible and finger is moving
				// around
				showKey(keyIndex, tracker);
			} else {
				sendMessageDelayed(
						obtainMessage(MSG_POPUP_PREVIEW, keyIndex, 0, tracker),
						delay);
			}
		}

		public void cancelPopupPreview() {
			removeMessages(MSG_POPUP_PREVIEW);
		}

		public void dismissPreview(long delay) {
			if (mPreviewPopup.isShowing()) {
				sendMessageDelayed(obtainMessage(MSG_DISMISS_PREVIEW), delay);
			}
		}

		public void cancelDismissPreview() {
			removeMessages(MSG_DISMISS_PREVIEW);
		}

		public void startKeyRepeatTimer(long delay, int keyIndex,
				PointerTracker tracker) {
			mInKeyRepeat = true;
			sendMessageDelayed(
					obtainMessage(MSG_REPEAT_KEY, keyIndex, 0, tracker), delay);
		}

		public void cancelKeyRepeatTimer() {
			mInKeyRepeat = false;
			removeMessages(MSG_REPEAT_KEY);
		}

		public boolean isInKeyRepeat() {
			return mInKeyRepeat;
		}

		public void startLongPressTimer(long delay, int keyIndex,
				PointerTracker tracker) {
			removeMessages(MSG_LONGPRESS_KEY);
			sendMessageDelayed(
					obtainMessage(MSG_LONGPRESS_KEY, keyIndex, 0, tracker),
					delay);
		}

		public void cancelLongPressTimer() {
			removeMessages(MSG_LONGPRESS_KEY);
		}

		public void cancelKeyTimers() {
			cancelKeyRepeatTimer();
			cancelLongPressTimer();
		}

		public void cancelAllMessages() {
			cancelKeyTimers();
			cancelPopupPreview();
			cancelDismissPreview();
		}
	}

	static class PointerQueue {
		private LinkedList<PointerTracker> mQueue = new LinkedList<PointerTracker>();

		public void add(PointerTracker tracker) {
			mQueue.add(tracker);
		}

		public int lastIndexOf(PointerTracker tracker) {
			LinkedList<PointerTracker> queue = mQueue;
			for (int index = queue.size() - 1; index >= 0; index--) {
				PointerTracker t = queue.get(index);
				if (t == tracker)
					return index;
			}
			return -1;
		}

		public void releaseAllPointersOlderThan(PointerTracker tracker,
				long eventTime) {
			LinkedList<PointerTracker> queue = mQueue;
			int oldestPos = 0;
			for (PointerTracker t = queue.get(oldestPos); t != tracker; t = queue
					.get(oldestPos)) {
				if (t.isModifier()) {
					oldestPos++;
				} else {
					t.onUpEvent(t.getLastX(), t.getLastY(), eventTime);
					t.setAlreadyProcessed();
					queue.remove(oldestPos);
				}
			}
		}

		public void releaseAllPointersExcept(PointerTracker tracker,
				long eventTime) {
			for (PointerTracker t : mQueue) {
				if (t == tracker)
					continue;
				t.onUpEvent(t.getLastX(), t.getLastY(), eventTime);
				t.setAlreadyProcessed();
			}
			mQueue.clear();
			if (tracker != null)
				mQueue.add(tracker);
		}

		public void remove(PointerTracker tracker) {
			mQueue.remove(tracker);
		}

		public boolean isInSlidingKeyInput() {
			for (final PointerTracker tracker : mQueue) {
				if (tracker.isInSlidingKeyInput())
					return true;
			}
			return false;
		}

		public void cancelAllTrackers() {
			final long time = System.currentTimeMillis();
			for (PointerTracker t : mQueue) {
				t.onCancelEvent(NOT_A_TOUCH_COORDINATE, NOT_A_TOUCH_COORDINATE,
						time);
			}
			mQueue.clear();
		}
	}

	public AnyKeyboardBaseView(Context context, AttributeSet attrs) {
		this(context, attrs, R.style.PlainLightAnySoftKeyboard);
	}

	public AnyKeyboardBaseView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		LayoutInflater inflate = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		// int previewLayout = 0;
		mPreviewKeyTextSize = -1;
		mPreviewLabelTextSize = -1;
		mPreviewKeyBackground = null;
		mPreviewKeyTextColor = 0xFFF;
		final int[] padding = new int[] { 0, 0, 0, 0 };

		KeyboardTheme theme = KeyboardThemeFactory
				.getCurrentKeyboardTheme(context.getApplicationContext());
		final int keyboardThemeStyleResId = getKeyboardStyleResId(theme);
		Log.d(TAG, "Will use keyboard theme " + theme.getName() + " id "
				+ theme.getId() + " res " + keyboardThemeStyleResId);
		HashSet<Integer> doneStylesIndexes = new HashSet<Integer>();
		TypedArray a = theme.getPackageContext().obtainStyledAttributes(null,
				R.styleable.AnySoftKeyboardTheme, 0, keyboardThemeStyleResId);

		final int n = a.getIndexCount();
		for (int i = 0; i < n; i++) {
			final int attr = a.getIndex(i);
			if (setValueFromTheme(a, padding, attr))
				doneStylesIndexes.add(attr);
		}
		a.recycle();
		// taking icons
		int iconSetStyleRes = theme.getIconsThemeResId();
		HashSet<Integer> doneIconsStylesIndexes = new HashSet<Integer>();
		Log.d(TAG, "Will use keyboard icons theme " + theme.getName() + " id "
				+ theme.getId() + " res " + iconSetStyleRes);
		if (iconSetStyleRes != 0) {
			a = theme.getPackageContext().obtainStyledAttributes(null,
					R.styleable.AnySoftKeyboardThemeKeyIcons, 0,
					iconSetStyleRes);
			final int iconsCount = a.getIndexCount();
			for (int i = 0; i < iconsCount; i++) {
				final int attr = a.getIndex(i);
				if (setKeyIconValueFromTheme(a, attr))
					doneIconsStylesIndexes.add(attr);
			}
			a.recycle();
		}
		// filling what's missing
		KeyboardTheme fallbackTheme = KeyboardThemeFactory
				.getFallbackTheme(context.getApplicationContext());
		final int keyboardFallbackThemeStyleResId = getKeyboardStyleResId(fallbackTheme);
		Log.d(TAG,
				"Will use keyboard fallback theme " + fallbackTheme.getName()
						+ " id " + fallbackTheme.getId() + " res "
						+ keyboardFallbackThemeStyleResId);
		a = fallbackTheme.getPackageContext().obtainStyledAttributes(null,
				R.styleable.AnySoftKeyboardTheme, 0,
				keyboardFallbackThemeStyleResId);

		final int fallbackCount = a.getIndexCount();
		for (int i = 0; i < fallbackCount; i++) {
			final int attr = a.getIndex(i);
			if (doneStylesIndexes.contains(attr))
				continue;
			if (AnyApplication.DEBUG)
				Log.d(TAG, "Falling back theme res ID " + attr);
			setValueFromTheme(a, padding, attr);
		}
		a.recycle();
		// taking missing icons
		int fallbackIconSetStyleId = fallbackTheme.getIconsThemeResId();
		Log.d(TAG,
				"Will use keyboard fallback icons theme "
						+ fallbackTheme.getName() + " id "
						+ fallbackTheme.getId() + " res "
						+ fallbackIconSetStyleId);
		a = fallbackTheme.getPackageContext().obtainStyledAttributes(null,
				R.styleable.AnySoftKeyboardThemeKeyIcons, 0,
				fallbackIconSetStyleId);

		final int fallbackIconsCount = a.getIndexCount();
		for (int i = 0; i < fallbackIconsCount; i++) {
			final int attr = a.getIndex(i);
			if (doneIconsStylesIndexes.contains(attr))
				continue;
			if (AnyApplication.DEBUG)
				Log.d(TAG, "Falling back icon res ID " + attr);
			setKeyIconValueFromTheme(a, attr);
		}
		a.recycle();

		// settings.
		// don't forget that there are TWO paddings, the theme's and the
		// background image's padding!
		Drawable keyboardBabground = super.getBackground();
		if (keyboardBabground != null) {
			Rect backgroundPadding = new Rect();
			keyboardBabground.getPadding(backgroundPadding);
			padding[0] += backgroundPadding.left;
			padding[1] += backgroundPadding.top;
			padding[2] += backgroundPadding.right;
			padding[3] += backgroundPadding.bottom;
		}
		super.setPadding(padding[0], padding[1], padding[2], padding[3]);

		final Resources res = getResources();
		mKeyboardDimens.setKeyboardMaxWidth(res.getDisplayMetrics().widthPixels
				- padding[0] - padding[2]);

		mPreviewPopup = new PopupWindow(context);
		if (mPreviewKeyTextSize > 0) {
			if (mPreviewLabelTextSize <= 0)
				mPreviewLabelTextSize = mPreviewKeyTextSize;
			mPreviewLayut = (ViewGroup) inflate.inflate(R.layout.key_preview,
					null);
			mPreviewText = (TextView) mPreviewLayut
					.findViewById(R.id.key_preview_text);
			mPreviewText.setTextColor(mPreviewKeyTextColor);
			mPreviewText.setTypeface(mKeyTextStyle);
			mPreviewIcon = (ImageView) mPreviewLayut
					.findViewById(R.id.key_preview_icon);
			mPreviewPopup.setBackgroundDrawable(mPreviewKeyBackground);
			mPreviewPopup.setContentView(mPreviewLayut);
			mShowPreview = true;
		} else {
			mPreviewLayut = null;
			mPreviewText = null;
			mShowPreview = false;
		}
		mPreviewPopup.setTouchable(false);
		mPreviewPopup
				.setAnimationStyle((mAnimationLevel == AnimationsLevel.None) ? 0
						: R.style.KeyPreviewAnimation);
		mDelayBeforePreview = 0;
		mDelayAfterPreview = 10;

		mMiniKeyboardParent = this;
		mMiniKeyboardPopup = new PopupWindow(context);
		mMiniKeyboardPopup.setBackgroundDrawable(null);

		mMiniKeyboardPopup
				.setAnimationStyle((mAnimationLevel == AnimationsLevel.None) ? 0
						: R.style.MiniKeyboardAnimation);

		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setTextSize(mKeyTextSize);
		mPaint.setTextAlign(Align.CENTER);
		mPaint.setAlpha(255);

		mKeyBackgroundPadding = new Rect(0, 0, 0, 0);
		mKeyBackground.getPadding(mKeyBackgroundPadding);

		if (AnyApplication.DEBUG)
			Log.d(TAG, "mKeyBackgroundPadding(L,R,T,B) "
					+ mKeyBackgroundPadding.left + ","
					+ mKeyBackgroundPadding.right + ","
					+ mKeyBackgroundPadding.top + ","
					+ mKeyBackgroundPadding.bottom);

		reloadSwipeThresholdsSettings(res);

		mDisambiguateSwipe = res.getBoolean(R.bool.config_swipeDisambiguation);
		mMiniKeyboardSlideAllowance = res
				.getDimension(R.dimen.mini_keyboard_slide_allowance);

		AskOnGestureListener listener = new AskGestureEventsListener(this,
				mSwipeTracker);

		mGestureDetector = AnyApplication.getDeviceSpecific()
				.createGestureDetector(getContext(), listener);
		mGestureDetector.setIsLongpressEnabled(false);

		// MultiTouchSupportLevel multiTouchSupportLevel =
		// AnyApplication.getDeviceSpecific().getMultiTouchSupportLevel(getContext());
		mHasDistinctMultitouch = true;/*
									 * (multiTouchSupportLevel ==
									 * MultiTouchSupportLevel.Basic)
									 * ||(multiTouchSupportLevel ==
									 * MultiTouchSupportLevel.Distinct);
									 */
		mKeyRepeatInterval = 50;

		AnyApplication.getConfig().addChangedListener(this);
	}

	public void setAnySoftKeyboardContext(AnyKeyboardContextProvider askContext) {
		mAskContext = askContext;
	}

	public boolean setValueFromTheme(TypedArray a, final int[] padding,
			final int attr) {
		try {
			switch (attr) {
			case R.styleable.AnySoftKeyboardTheme_android_background:
				Drawable keyboardBackground = a.getDrawable(attr);
				if (AnyApplication.DEBUG)
					Log.d(TAG, "AnySoftKeyboardTheme_android_background "
							+ (keyboardBackground != null));
				super.setBackgroundDrawable(keyboardBackground);
				break;
			case R.styleable.AnySoftKeyboardTheme_android_paddingLeft:
				padding[0] = a.getDimensionPixelSize(attr, 0);
				if (AnyApplication.DEBUG)
					Log.d(TAG, "AnySoftKeyboardTheme_android_paddingLeft "
							+ padding[0]);
				break;
			case R.styleable.AnySoftKeyboardTheme_android_paddingTop:
				padding[1] = a.getDimensionPixelSize(attr, 0);
				if (AnyApplication.DEBUG)
					Log.d(TAG, "AnySoftKeyboardTheme_android_paddingTop "
							+ padding[1]);
				break;
			case R.styleable.AnySoftKeyboardTheme_android_paddingRight:
				padding[2] = a.getDimensionPixelSize(attr, 0);
				if (AnyApplication.DEBUG)
					Log.d(TAG, "AnySoftKeyboardTheme_android_paddingRight "
							+ padding[2]);
				break;
			case R.styleable.AnySoftKeyboardTheme_android_paddingBottom:
				padding[3] = a.getDimensionPixelSize(attr, 0);
				if (AnyApplication.DEBUG)
					Log.d(TAG, "AnySoftKeyboardTheme_android_paddingBottom "
							+ padding[3]);
				break;
			case R.styleable.AnySoftKeyboardTheme_keyBackground:
				mKeyBackground = a.getDrawable(attr);
				if (AnyApplication.DEBUG)
					Log.d(TAG, "AnySoftKeyboardTheme_keyBackground "
							+ (mKeyBackground != null));
				break;
			case R.styleable.AnySoftKeyboardTheme_keyHysteresisDistance:
				mKeyHysteresisDistance = a.getDimensionPixelOffset(attr, 0);
				if (AnyApplication.DEBUG)
					Log.d(TAG, "AnySoftKeyboardTheme_keyHysteresisDistance "
							+ mKeyHysteresisDistance);
				break;
			case R.styleable.AnySoftKeyboardTheme_verticalCorrection:
				mVerticalCorrection = a.getDimensionPixelOffset(attr, 0);
				if (AnyApplication.DEBUG)
					Log.d(TAG, "AnySoftKeyboardTheme_verticalCorrection "
							+ mVerticalCorrection);
				break;
			case R.styleable.AnySoftKeyboardTheme_keyPreviewBackground:
				mPreviewKeyBackground = a.getDrawable(attr);
				if (AnyApplication.DEBUG)
					Log.d(TAG, "AnySoftKeyboardTheme_keyPreviewBackground "
							+ (mPreviewKeyBackground != null));
				break;
			case R.styleable.AnySoftKeyboardTheme_keyPreviewTextColor:
				mPreviewKeyTextColor = a.getColor(attr, 0xFFF);
				if (AnyApplication.DEBUG)
					Log.d(TAG, "AnySoftKeyboardTheme_keyPreviewTextColor "
							+ mPreviewKeyTextColor);
				break;
			case R.styleable.AnySoftKeyboardTheme_keyPreviewTextSize:
				mPreviewKeyTextSize = a.getDimensionPixelSize(attr, 0);
				if (AnyApplication.DEBUG)
					Log.d(TAG, "AnySoftKeyboardTheme_keyPreviewTextSize "
							+ mPreviewKeyTextSize);
				break;
			case R.styleable.AnySoftKeyboardTheme_keyPreviewLabelTextSize:
				mPreviewLabelTextSize = a.getDimensionPixelSize(attr, 0);
				if (AnyApplication.DEBUG)
					Log.d(TAG, "AnySoftKeyboardTheme_keyPreviewLabelTextSize "
							+ mPreviewLabelTextSize);
				break;
			case R.styleable.AnySoftKeyboardTheme_keyPreviewOffset:
				mPreviewOffset = a.getDimensionPixelOffset(attr, 0);
				if (AnyApplication.DEBUG)
					Log.d(TAG, "AnySoftKeyboardTheme_keyPreviewOffset "
							+ mPreviewOffset);
				break;
			case R.styleable.AnySoftKeyboardTheme_keyTextSize:
				mKeyTextSize = a.getDimensionPixelSize(attr, 18);
				// you might ask yourself "why did Menny sqrt root the factor?"
				// I'll tell you; the factor is mostly for the height, not the
				// font size,
				// but I also factorize the font size because I want the text to
				// be a little like
				// the key size.
				// the whole factor maybe too much, so I ease that a bit.
				if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
					mKeyTextSize = mKeyTextSize
							* FloatMath.sqrt(AnyApplication.getConfig()
									.getKeysHeightFactorInLandscape());
				else
					mKeyTextSize = mKeyTextSize
							* FloatMath.sqrt(AnyApplication.getConfig()
									.getKeysHeightFactorInPortrait());
				if (AnyApplication.DEBUG)
					Log.d(TAG, "AnySoftKeyboardTheme_keyTextSize "
							+ mKeyTextSize);
				break;
			case R.styleable.AnySoftKeyboardTheme_keyTextColor:
				mKeyTextColor = a.getColorStateList(attr);
				if (mKeyTextColor == null) {
					if (AnyApplication.DEBUG)
						Log.d(TAG,
								"Creating an empty ColorStateList for mKeyTextColor");
					mKeyTextColor = new ColorStateList(new int[][] { { 0 } },
							new int[] { a.getColor(attr, 0xFF000000) });
				}
				if (AnyApplication.DEBUG)
					Log.d(TAG, "AnySoftKeyboardTheme_keyTextColor "
							+ mKeyTextColor);
				break;
			case R.styleable.AnySoftKeyboardTheme_labelTextSize:
				mLabelTextSize = a.getDimensionPixelSize(attr, 14);
				if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
					mLabelTextSize = mLabelTextSize
							* AnyApplication.getConfig()
									.getKeysHeightFactorInLandscape();
				else
					mLabelTextSize = mLabelTextSize
							* AnyApplication.getConfig()
									.getKeysHeightFactorInPortrait();
				if (AnyApplication.DEBUG)
					Log.d(TAG, "AnySoftKeyboardTheme_labelTextSize "
							+ mLabelTextSize);
				break;
			case R.styleable.AnySoftKeyboardTheme_keyboardNameTextSize:
				mKeyboardNameTextSize = a.getDimensionPixelSize(attr, 10);
				if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
					mKeyboardNameTextSize = mKeyboardNameTextSize
							* AnyApplication.getConfig()
									.getKeysHeightFactorInLandscape();
				else
					mKeyboardNameTextSize = mKeyboardNameTextSize
							* AnyApplication.getConfig()
									.getKeysHeightFactorInPortrait();
				if (AnyApplication.DEBUG)
					Log.d(TAG, "AnySoftKeyboardTheme_keyboardNameTextSize "
							+ mKeyboardNameTextSize);
				break;
			case R.styleable.AnySoftKeyboardTheme_keyboardNameTextColor:
				mKeyboardNameTextColor = a.getColorStateList(attr);
				if (mKeyboardNameTextColor == null) {
					if (AnyApplication.DEBUG)
						Log.d(TAG,
								"Creating an empty ColorStateList for mKeyboardNameTextColor");
					mKeyboardNameTextColor = new ColorStateList(new int[][] { { 0 } },
							new int[] { a.getColor(attr, 0xFFAAAAAA) });
				}
				if (AnyApplication.DEBUG)
					Log.d(TAG, "AnySoftKeyboardTheme_keyboardNameTextColor "
							+ mKeyboardNameTextColor);
				break;
			case R.styleable.AnySoftKeyboardTheme_shadowColor:
				mShadowColor = a.getColor(attr, 0);
				if (AnyApplication.DEBUG)
					Log.d(TAG, "AnySoftKeyboardTheme_shadowColor "
							+ mShadowColor);
				break;
			case R.styleable.AnySoftKeyboardTheme_shadowRadius:
				mShadowRadius = a.getDimensionPixelOffset(attr, 0);
				if (AnyApplication.DEBUG)
					Log.d(TAG, "AnySoftKeyboardTheme_shadowRadius "
							+ mShadowRadius);
				break;
			case R.styleable.AnySoftKeyboardTheme_shadowOffsetX:
				mShadowOffsetX = a.getDimensionPixelOffset(attr, 0);
				if (AnyApplication.DEBUG)
					Log.d(TAG, "AnySoftKeyboardTheme_shadowOffsetX "
							+ mShadowOffsetX);
				break;
			case R.styleable.AnySoftKeyboardTheme_shadowOffsetY:
				mShadowOffsetY = a.getDimensionPixelOffset(attr, 0);
				if (AnyApplication.DEBUG)
					Log.d(TAG, "AnySoftKeyboardTheme_shadowOffsetY "
							+ mShadowOffsetY);
				break;
			case R.styleable.AnySoftKeyboardTheme_backgroundDimAmount:
				mBackgroundDimAmount = a.getFloat(attr, 0.5f);
				if (AnyApplication.DEBUG)
					Log.d(TAG, "AnySoftKeyboardTheme_backgroundDimAmount "
							+ mBackgroundDimAmount);
				break;
			case R.styleable.AnySoftKeyboardTheme_keyTextStyle:
				int textStyle = a.getInt(attr, 0);
				switch (textStyle) {
				case 0:
					mKeyTextStyle = Typeface.DEFAULT;
					break;
				case 1:
					mKeyTextStyle = Typeface.DEFAULT_BOLD;
					break;
				case 2:
					mKeyTextStyle = Typeface.defaultFromStyle(Typeface.ITALIC);
					break;
				default:
					mKeyTextStyle = Typeface.defaultFromStyle(textStyle);
					break;
				}
				if (AnyApplication.DEBUG)
					Log.d(TAG, "AnySoftKeyboardTheme_keyTextStyle "
							+ mKeyTextStyle);
				break;
			case R.styleable.AnySoftKeyboardTheme_symbolColorScheme:
				mSymbolColorScheme = a.getInt(attr, 0);
				if (AnyApplication.DEBUG)
					Log.d(TAG, "AnySoftKeyboardTheme_symbolColorScheme "
							+ mSymbolColorScheme);
				break;
			case R.styleable.AnySoftKeyboardTheme_keyHorizontalGap:
				float themeHorizotalKeyGap = a.getDimensionPixelOffset(attr, 0);
				mKeyboardDimens.setHorizontalKeyGap(themeHorizotalKeyGap);
				if (AnyApplication.DEBUG)
					Log.d(TAG, "AnySoftKeyboardTheme_keyHorizontalGap "
							+ themeHorizotalKeyGap);
				break;
			case R.styleable.AnySoftKeyboardTheme_keyVerticalGap:
				float themeVerticalRowGap = a.getDimensionPixelOffset(attr, 0);
				mKeyboardDimens.setVerticalRowGap(themeVerticalRowGap);
				if (AnyApplication.DEBUG)
					Log.d(TAG, "AnySoftKeyboardTheme_keyVerticalGap "
							+ themeVerticalRowGap);
				break;
			case R.styleable.AnySoftKeyboardTheme_keyNormalHeight:
				float themeNormalKeyHeight = a.getDimensionPixelOffset(attr, 0);
				mKeyboardDimens.setNormalKeyHeight(themeNormalKeyHeight);
				if (AnyApplication.DEBUG)
					Log.d(TAG, "AnySoftKeyboardTheme_keyNormalHeight "
							+ themeNormalKeyHeight);
				break;
			case R.styleable.AnySoftKeyboardTheme_keyLargeHeight:
				float themeLargeKeyHeight = a.getDimensionPixelOffset(attr, 0);
				mKeyboardDimens.setLargeKeyHeight(themeLargeKeyHeight);
				if (AnyApplication.DEBUG)
					Log.d(TAG, "AnySoftKeyboardTheme_keyLargeHeight "
							+ themeLargeKeyHeight);
				break;
			case R.styleable.AnySoftKeyboardTheme_keySmallHeight:
				float themeSmallKeyHeight = a.getDimensionPixelOffset(attr, 0);
				mKeyboardDimens.setSmallKeyHeight(themeSmallKeyHeight);
				if (AnyApplication.DEBUG)
					Log.d(TAG, "AnySoftKeyboardTheme_keySmallHeight "
							+ themeSmallKeyHeight);
				break;
			case R.styleable.AnySoftKeyboardTheme_hintTextSize:
				mHintTextSize = a.getDimensionPixelSize(attr, 0);
				if (AnyApplication.DEBUG)
					Log.d(TAG, "AnySoftKeyboardTheme_hintTextSize "
							+ mHintTextSize);
				if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
					mHintTextSize = mHintTextSize
							* AnyApplication.getConfig()
									.getKeysHeightFactorInLandscape();
				else
					mHintTextSize = mHintTextSize
							* AnyApplication.getConfig()
									.getKeysHeightFactorInPortrait();
				if (AnyApplication.DEBUG)
					Log.d(TAG, "AnySoftKeyboardTheme_hintTextSize with factor "
							+ mHintTextSize);
				break;
			case R.styleable.AnySoftKeyboardTheme_hintTextColor:
				mHintTextColor = a.getColorStateList(attr);
				if (mHintTextColor == null) {
					if (AnyApplication.DEBUG)
						Log.d(TAG,
								"Creating an empty ColorStateList for mHintTextColor");
					mHintTextColor = new ColorStateList(new int[][] { { 0 } },
							new int[] { a.getColor(attr, 0xFF000000) });
				}
				if (AnyApplication.DEBUG)
					Log.d(TAG, "AnySoftKeyboardTheme_hintTextColor "
							+ mHintTextColor);
				break;
			case R.styleable.AnySoftKeyboardTheme_hintLabelVAlign:
				mHintLabelVAlign = a.getInt(attr, Gravity.BOTTOM);
				if (AnyApplication.DEBUG)
					Log.d(TAG, "AnySoftKeyboardTheme_hintLabelVAlign "
							+ mHintLabelVAlign);
				break;
			case R.styleable.AnySoftKeyboardTheme_hintLabelAlign:
				mHintLabelAlign = a.getInt(attr, Gravity.RIGHT);
				if (AnyApplication.DEBUG)
					Log.d(TAG, "AnySoftKeyboardTheme_hintLabelAlign "
							+ mHintLabelAlign);
				break;
			case R.styleable.AnySoftKeyboardTheme_hintOverflowLabel:
				mHintOverflowLabel = a.getString(attr);
				if (AnyApplication.DEBUG)
					Log.d(TAG, "AnySoftKeyboardTheme_hintOverflowLabel "
							+ mHintOverflowLabel);
				break;
			}
			return true;
		} catch (Exception e) {
			// on API changes, so the incompatible themes wont crash me..
			e.printStackTrace();
			return false;
		}
	}

	public boolean setKeyIconValueFromTheme(TypedArray a, final int attr) {
		try {
			switch (attr) {
			case R.styleable.AnySoftKeyboardThemeKeyIcons_iconKeyShift:
				mShiftIcon = a.getDrawable(attr);
				if (AnyApplication.DEBUG)
					Log.d(TAG, "AnySoftKeyboardThemeKeyIcons_iconKeyShift "
							+ (mShiftIcon != null));
				break;
			case R.styleable.AnySoftKeyboardThemeKeyIcons_iconKeyControl:
				mControlIcon = a.getDrawable(attr);
				if (AnyApplication.DEBUG)
					Log.d(TAG, "AnySoftKeyboardThemeKeyIcons_iconKeyControl "
							+ (mControlIcon != null));
				break;
			case R.styleable.AnySoftKeyboardThemeKeyIcons_iconKeyAction:
				mActionKeyIcon = a.getDrawable(attr);
				if (AnyApplication.DEBUG)
					Log.d(TAG, "AnySoftKeyboardThemeKeyIcons_iconKeyAction "
							+ (mActionKeyIcon != null));
				break;
			case R.styleable.AnySoftKeyboardThemeKeyIcons_iconKeyBackspace:
				mDeleteKeyIcon = a.getDrawable(attr);
				if (AnyApplication.DEBUG)
					Log.d(TAG, "AnySoftKeyboardThemeKeyIcons_iconKeyBackspace "
							+ (mDeleteKeyIcon != null));
				break;
			case R.styleable.AnySoftKeyboardThemeKeyIcons_iconKeyCancel:
				mCancelKeyIcon = a.getDrawable(attr);
				if (AnyApplication.DEBUG)
					Log.d(TAG, "AnySoftKeyboardThemeKeyIcons_iconKeyCancel "
							+ (mCancelKeyIcon != null));
				break;
			case R.styleable.AnySoftKeyboardThemeKeyIcons_iconKeyGlobe:
				mGlobeKeyIcon = a.getDrawable(attr);
				if (AnyApplication.DEBUG)
					Log.d(TAG, "AnySoftKeyboardThemeKeyIcons_iconKeyGlobe "
							+ (mGlobeKeyIcon != null));
				break;
			case R.styleable.AnySoftKeyboardThemeKeyIcons_iconKeySpace:
				mSpaceKeyIcon = a.getDrawable(attr);
				if (AnyApplication.DEBUG)
					Log.d(TAG, "AnySoftKeyboardThemeKeyIcons_iconKeySpace "
							+ (mSpaceKeyIcon != null));
				break;
			case R.styleable.AnySoftKeyboardThemeKeyIcons_iconKeyTab:
				mTabKeyIcon = a.getDrawable(attr);
				if (AnyApplication.DEBUG)
					Log.d(TAG, "AnySoftKeyboardThemeKeyIcons_iconKeyTab "
							+ (mTabKeyIcon != null));
				break;
			case R.styleable.AnySoftKeyboardThemeKeyIcons_iconKeyArrowDown:
				mArrowDownKeyIcon = a.getDrawable(attr);
				if (AnyApplication.DEBUG)
					Log.d(TAG, "AnySoftKeyboardThemeKeyIcons_iconKeyArrowDown "
							+ (mArrowDownKeyIcon != null));
				break;
			case R.styleable.AnySoftKeyboardThemeKeyIcons_iconKeyArrowLeft:
				mArrowLeftKeyIcon = a.getDrawable(attr);
				if (AnyApplication.DEBUG)
					Log.d(TAG, "AnySoftKeyboardThemeKeyIcons_iconKeyArrowLeft "
							+ (mArrowLeftKeyIcon != null));
				break;
			case R.styleable.AnySoftKeyboardThemeKeyIcons_iconKeyArrowRight:
				mArrowRightKeyIcon = a.getDrawable(attr);
				if (AnyApplication.DEBUG)
					Log.d(TAG,
							"AnySoftKeyboardThemeKeyIcons_iconKeyArrowRight "
									+ (mArrowRightKeyIcon != null));
				break;
			case R.styleable.AnySoftKeyboardThemeKeyIcons_iconKeyArrowUp:
				mArrowUpKeyIcon = a.getDrawable(attr);
				if (AnyApplication.DEBUG)
					Log.d(TAG, "AnySoftKeyboardThemeKeyIcons_iconKeyArrowUp "
							+ (mArrowUpKeyIcon != null));
				break;
			case R.styleable.AnySoftKeyboardThemeKeyIcons_iconKeyInputMoveHome:
				mMoveHomeKeyIcon = a.getDrawable(attr);
				if (AnyApplication.DEBUG)
					Log.d(TAG,
							"AnySoftKeyboardThemeKeyIcons_iconKeyInputMoveHome "
									+ (mMoveHomeKeyIcon != null));
				break;
			case R.styleable.AnySoftKeyboardThemeKeyIcons_iconKeyInputMoveEnd:
				mMoveEndKeyIcon = a.getDrawable(attr);
				if (AnyApplication.DEBUG)
					Log.d(TAG,
							"AnySoftKeyboardThemeKeyIcons_iconKeyInputMoveEnd "
									+ (mMoveEndKeyIcon != null));
				break;
			case R.styleable.AnySoftKeyboardThemeKeyIcons_iconKeyMic:
				mMicKeyIcon = a.getDrawable(attr);
				if (AnyApplication.DEBUG)
					Log.d(TAG, "AnySoftKeyboardThemeKeyIcons_iconKeyMic "
							+ (mMicKeyIcon != null));
				break;
			case R.styleable.AnySoftKeyboardThemeKeyIcons_iconKeySettings:
				mSettingsKeyIcon = a.getDrawable(attr);
				if (AnyApplication.DEBUG)
					Log.d(TAG, "AnySoftKeyboardThemeKeyIcons_iconKeySettings "
							+ (mSettingsKeyIcon != null));
				break;
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	protected int getKeyboardStyleResId(KeyboardTheme theme) {
		return theme.getPopupThemeResId();
	}

	/*
	 * public int getKeyboardMaxWidth() { return mMaxKeyboardWidth; } public int
	 * getThemeVerticalRowGap() { return mThemeVerticalRowGap; } public int
	 * getThemeHorizontalKeyGap() { return mThemeHorizotalKeyGap; }
	 */
	private void reloadSwipeThresholdsSettings(final Resources res) {
		final float density = res.getDisplayMetrics().density;
		mSwipeVelocityThreshold = (int) (AnyApplication.getConfig()
				.getSwipeVelocityThreshold() * density);
		mSwipeXDistanceThreshold = (int) (AnyApplication.getConfig()
				.getSwipeDistanceThreshold() * density);
		Keyboard kbd = getKeyboard();
		if (kbd != null) {
			mSwipeYDistanceThreshold = (int) (mSwipeXDistanceThreshold * (((float) kbd
					.getHeight()) / ((float) getWidth())));
		} else {
			mSwipeYDistanceThreshold = 0;
		}
		if (mSwipeYDistanceThreshold == 0)
			mSwipeYDistanceThreshold = mSwipeXDistanceThreshold;

		mSwipeSpaceXDistanceThreshold = mSwipeXDistanceThreshold / 2;
		mSwipeYDistanceThreshold = mSwipeYDistanceThreshold / 2;

		mScrollXDistanceThreshold = mSwipeXDistanceThreshold / 8;
		mScrollYDistanceThreshold = mSwipeYDistanceThreshold / 8;
		if (AnyApplication.DEBUG) {
			Log.d(TAG,
					String.format(
							"Swipe thresholds: Velocity %d, X-Distance %d, Y-Distance %d",
							mSwipeVelocityThreshold, mSwipeXDistanceThreshold,
							mSwipeYDistanceThreshold));
		}
	}

	public void setOnKeyboardActionListener(OnKeyboardActionListener listener) {
		mKeyboardActionListener = listener;
		for (PointerTracker tracker : mPointerTrackers) {
			tracker.setOnKeyboardActionListener(listener);
		}
	}

	/**
	 * Returns the {@link OnKeyboardActionListener} object.
	 * 
	 * @return the listener attached to this keyboard
	 */
	protected OnKeyboardActionListener getOnKeyboardActionListener() {
		return mKeyboardActionListener;
	}

	/**
	 * Attaches a keyboard to this view. The keyboard can be switched at any
	 * time and the view will re-layout itself to accommodate the keyboard.
	 * 
	 * @see Keyboard
	 * @see #getKeyboard()
	 * @param keyboard
	 *            the keyboard to display in this view
	 */
	public void setKeyboard(AnyKeyboard keyboard) {
		setKeyboard(keyboard, mVerticalCorrection);
	}

	public void setKeyboard(AnyKeyboard keyboard, float verticalCorrection) {
		if (mKeyboard != null) {
			dismissKeyPreview();
		}
		// Remove any pending messages, except dismissing preview
		mHandler.cancelKeyTimers();
		mHandler.cancelPopupPreview();
		mKeyboard = keyboard;
		mKeyboardName = keyboard != null? keyboard.getKeyboardName() : null;
		// ImeLogger.onSetKeyboard(keyboard);
		mKeys = mKeyDetector.setKeyboard(keyboard);
		mKeyDetector.setCorrection(-getPaddingLeft(), -getPaddingTop()
				+ verticalCorrection);
		// mKeyboardVerticalGap =
		// (int)getResources().getDimension(R.dimen.key_bottom_gap);
		for (PointerTracker tracker : mPointerTrackers) {
			tracker.setKeyboard(mKeys, mKeyHysteresisDistance);
		}
		// setting the icon/text
		setSpecialKeysIconsAndLabels();

		requestLayout();
		// Hint to reallocate the buffer if the size changed
		mKeyboardChanged = true;
		invalidateAllKeys();
		computeProximityThreshold(keyboard);
		// mMiniKeyboardCache.clear();
		super.invalidate();
	}

	/**
	 * Returns the current keyboard being displayed by this view.
	 * 
	 * @return the currently attached keyboard
	 * @see #setKeyboard(Keyboard)
	 */
	public AnyKeyboard getKeyboard() {
		return mKeyboard;
	}

	/**
	 * Return whether the device has distinct multi-touch panel.
	 * 
	 * @return true if the device has distinct multi-touch panel.
	 */
	public boolean hasDistinctMultitouch() {
		return mHasDistinctMultitouch;
	}

	/**
	 * Sets the state of the shift key of the keyboard, if any.
	 * 
	 * @param shifted
	 *            whether or not to enable the state of the shift key
	 * @return true if the shift key state changed, false if there was no change
	 */
	public boolean setShifted(boolean shifted) {
		if (mKeyboard != null) {
			if (mKeyboard.setShifted(shifted)) {
				// The whole keyboard probably needs to be redrawn
				invalidateAllKeys();
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the state of the shift key of the keyboard, if any.
	 * 
	 * @return true if the shift is in a pressed state, false otherwise. If
	 *         there is no shift key on the keyboard or there is no keyboard
	 *         attached, it returns false.
	 */
	public boolean isShifted() {
		if (isPopupShowing())
			return mMiniKeyboard.isShifted();

		if (mKeyboard != null)
			return mKeyboard.isShifted();

		return false;
	}

	/**
	 * Sets the state of the control key of the keyboard, if any.
	 * 
	 * @param control
	 *            whether or not to enable the state of the control key
	 * @return true if the control key state changed, false if there was no
	 *         change
	 */
	public boolean setControl(boolean control) {
		if (mKeyboard != null) {
			if (mKeyboard.setControl(control)) {
				// The whole keyboard probably needs to be redrawn
				invalidateAllKeys();
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the state of the control key of the keyboard, if any.
	 * 
	 * @return true if the control is in a pressed state, false otherwise. If
	 *         there is no control key on the keyboard or there is no keyboard
	 *         attached, it returns false.
	 */
	public boolean isControl() {
		if (mKeyboard != null) {
			return mKeyboard.isControl();
		}
		return false;
	}

	/**
	 * Enables or disables the key feedback popup. This is a popup that shows a
	 * magnified version of the depressed key. By default the preview is
	 * enabled.
	 * 
	 * @param previewEnabled
	 *            whether or not to enable the key feedback popup
	 * @see #isPreviewEnabled()
	 */
	protected void setPreviewEnabled(boolean previewEnabled) {
		mShowPreview = mPreviewText != null && previewEnabled;
	}

	// /**
	// * Returns the enabled state of the key feedback popup.
	// * @return whether or not the key feedback popup is enabled
	// * @see #setPreviewEnabled(boolean)
	// */
	// public boolean isPreviewEnabled() {
	// return mShowPreview;
	// }

	public int getSymbolColorScheme() {
		return mSymbolColorScheme;
	}

	public void setPopupParent(View v) {
		mMiniKeyboardParent = v;
	}

	public void setPopupOffset(int x, int y) {
		mPopupPreviewOffsetX = x;
		mPopupPreviewOffsetY = y;
		mPreviewPopup.dismiss();
	}

	/**
	 * When enabled, calls to {@link OnKeyboardActionListener#onKey} will
	 * include key codes for adjacent keys. When disabled, only the primary key
	 * code will be reported.
	 * 
	 * @param enabled
	 *            whether or not the proximity correction is enabled
	 */
	public void setProximityCorrectionEnabled(boolean enabled) {
		mKeyDetector.setProximityCorrectionEnabled(enabled);
	}

	/**
	 * Returns true if proximity correction is enabled.
	 */
	public boolean isProximityCorrectionEnabled() {
		return mKeyDetector.isProximityCorrectionEnabled();
	}

	private CharSequence adjustCase(AnyKey key) {
		// if (mKeyboard.isShifted() && label != null && label.length() < 3
		// && Character.isLowerCase(label.charAt(0))) {
		// label = label.toString().toUpperCase();
		// }
		CharSequence label = key.label;
		// if (mKeyboard.isShifted() &&
		// (!TextUtils.isEmpty(label)) &&
		// Character.isLowerCase(label.charAt(0))) {
		// label = label.toString().toUpperCase();
		// }
		if (mKeyboard.isShifted()) {
			if (!TextUtils.isEmpty(key.shiftedKeyLabel))
				label = key.shiftedKeyLabel;
			else if (!TextUtils.isEmpty(label)
					&& Character.isLowerCase(label.charAt(0)))
				label = label.toString().toUpperCase();
		}
		return label;
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// Round up a little
		if (mKeyboard == null) {
			setMeasuredDimension(getPaddingLeft() + getPaddingRight(),
					getPaddingTop() + getPaddingBottom());
		} else {
			int width = mKeyboard.getMinWidth() + getPaddingLeft()
					+ getPaddingRight();
			if (MeasureSpec.getSize(widthMeasureSpec) < width + 10) {
				width = MeasureSpec.getSize(widthMeasureSpec);
			}
			setMeasuredDimension(width, mKeyboard.getHeight() + getPaddingTop()
					+ getPaddingBottom());
		}
	}

	/**
	 * Compute the average distance between adjacent keys (horizontally and
	 * vertically) and square it to get the proximity threshold. We use a square
	 * here and in computing the touch distance from a key's center to avoid
	 * taking a square root.
	 * 
	 * @param keyboard
	 */
	private void computeProximityThreshold(Keyboard keyboard) {
		if (keyboard == null)
			return;
		final Key[] keys = mKeys;
		if (keys == null)
			return;
		int length = keys.length;
		int dimensionSum = 0;
		for (int i = 0; i < length; i++) {
			Key key = keys[i];
			dimensionSum += Math.min(key.width, key.height/*
														 * +
														 * mKeyboardVerticalGap
														 */) + key.gap;
		}
		if (dimensionSum < 0 || length == 0)
			return;
		mKeyDetector
				.setProximityThreshold((int) (dimensionSum * 1.4f / length));
	}

	@Override
	public void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		// Release the buffer, if any and it will be reallocated on the next
		// draw
		mBuffer = null;
	}

	@Override
	public void onDraw(final Canvas canvas) {
		super.onDraw(canvas);
		// mCanvas = canvas;
		if (mDrawPending || mBuffer == null || mKeyboardChanged) {
			GCUtils.getInstance().peformOperationWithMemRetry(TAG, new MemRelatedOperation() {
				
				public void operation() {
					onBufferDraw(canvas);
				}
			}, true);
		}
		// maybe there is no buffer, since drawing was not done.
		if (mBuffer != null)
			canvas.drawBitmap(mBuffer, 0, 0, null);
	}
							
	private void onBufferDraw(Canvas canvas) {
		if (mKeyboardChanged) {
			invalidateAllKeys();
			mKeyboardChanged = false;
		}

		canvas.getClipBounds(mDirtyRect);

		if (mKeyboard == null)
			return;

		final boolean drawKeyboardNameText = (mKeyboardNameTextSize > 1)
				&& AnyApplication.getConfig().getShowKeyboardNameText();
		
		final boolean drawHintText = (mHintTextSize > 1)
				&& AnyApplication.getConfig().getShowHintTextOnKeys();
		if (AnyApplication.DEBUG && !drawHintText) {
			Log.d(TAG, "drawHintText is false. mHintTextSize: " + mHintTextSize
					+ ", getShowHintTextOnKeys: "
					+ AnyApplication.getConfig().getShowHintTextOnKeys());
		}
		// TODO: calls to AnyApplication.getConfig().getXXXXX() functions are
		// not yet implemented,
		// but need to when allowing preferences to override theme settings of
		// these values
		// right now just using what should be the default values for these
		// unimplemented preferences

		final boolean useCustomKeyTextColor = false;
		// TODO: final boolean useCustomKeyTextColor =
		// AnyApplication.getConfig().getUseCustomTextColorOnKeys();
		final ColorStateList keyTextColor = useCustomKeyTextColor ? new ColorStateList(
				new int[][] { { 0 } }, new int[] { 0xFF6666FF })
				: mKeyTextColor;
		// TODO: ? AnyApplication.getConfig().getCustomKeyTextColorOnKeys() :
		// mKeyTextColor;

		final boolean useCustomHintColor = drawHintText && false;
		// TODO: final boolean useCustomHintColor = drawHintText &&
		// AnyApplication.getConfig().getUseCustomHintColorOnKeys();
		final ColorStateList hintColor = useCustomHintColor ? new ColorStateList(
				new int[][] { { 0 } }, new int[] { 0xFFFF6666 })
				: mHintTextColor;
		// TODO: ? AnyApplication.getConfig().getCustomHintColorOnKeys() :
		// mHintTextColor;

		// allow preferences to override theme settings for hint text position
		final boolean useCustomHintAlign = drawHintText
				&& AnyApplication.getConfig().getUseCustomHintAlign();
		final int hintAlign = useCustomHintAlign ? AnyApplication.getConfig()
				.getCustomHintAlign() : mHintLabelAlign;
		final int hintVAlign = useCustomHintAlign ? AnyApplication.getConfig()
				.getCustomHintVAlign() : mHintLabelVAlign;

		final Paint paint = mPaint;
		final Drawable keyBackground = mKeyBackground;
		final Rect clipRegion = mClipRegion;
		final int kbdPaddingLeft = getPaddingLeft();
		final int kbdPaddingTop = getPaddingTop();
		final Key[] keys = mKeys;
		final Key invalidKey = mInvalidatedKey;

		boolean drawSingleKey = false;
		if (invalidKey != null && canvas.getClipBounds(clipRegion)) {
			// TODO we should use Rect.inset and Rect.contains here.
			// Is clipRegion completely contained within the invalidated key?
			if (invalidKey.x + kbdPaddingLeft - 1 <= clipRegion.left
					&& invalidKey.y + kbdPaddingTop - 1 <= clipRegion.top
					&& invalidKey.x + invalidKey.width + kbdPaddingLeft + 1 >= clipRegion.right
					&& invalidKey.y + invalidKey.height + kbdPaddingTop + 1 >= clipRegion.bottom) {
				drawSingleKey = true;
			}
		}
		// canvas.drawColor(0x00000000, PorterDuff.Mode.CLEAR);
		final int keyCount = keys.length;
		for (int i = 0; i < keyCount; i++) {
			final AnyKey key = (AnyKey) keys[i];
			final boolean keyIsSpace = isSpaceKey(key);

			if (drawSingleKey && (invalidKey != key)) {
				continue;
			}
			if (!mDirtyRect.intersects(key.x + kbdPaddingLeft, key.y
					+ kbdPaddingTop, key.x + key.width + kbdPaddingLeft, key.y
					+ key.height + kbdPaddingTop)) {
				continue;
			}
			int[] drawableState = key.getCurrentDrawableState();

			if (keyIsSpace)
				paint.setColor(mKeyboardNameTextColor.getColorForState(drawableState,
					0xFF000000));
			else
				paint.setColor(keyTextColor.getColorForState(drawableState,
						0xFF000000));
			keyBackground.setState(drawableState);

			// Switch the character to uppercase if shift is pressed
			CharSequence label = key.label == null ? null : adjustCase(key)
					.toString();

			final Rect bounds = keyBackground.getBounds();
			if ((key.width != bounds.right) || (key.height != bounds.bottom)) {
				keyBackground.setBounds(0, 0, key.width, key.height);
			}
			canvas.translate(key.x + kbdPaddingLeft, key.y + kbdPaddingTop);
			keyBackground.draw(canvas);

			if (TextUtils.isEmpty(label)) {
				Drawable iconToDraw = getIconToDrawForKey(key, false);
				if (iconToDraw != null/* && shouldDrawIcon */) {
					// Special handing for the upper-right number hint icons
					final int drawableWidth;
					final int drawableHeight;
					final int drawableX;
					final int drawableY;

					drawableWidth = iconToDraw.getIntrinsicWidth();
					drawableHeight = iconToDraw.getIntrinsicHeight();
					drawableX = (key.width + mKeyBackgroundPadding.left
							- mKeyBackgroundPadding.right - drawableWidth) / 2;
					drawableY = (key.height + mKeyBackgroundPadding.top
							- mKeyBackgroundPadding.bottom - drawableHeight) / 2;

					canvas.translate(drawableX, drawableY);
					iconToDraw.setBounds(0, 0, drawableWidth, drawableHeight);
					iconToDraw.draw(canvas);
					canvas.translate(-drawableX, -drawableY);
					if (keyIsSpace && drawKeyboardNameText) {
						//now a little hack, I'll set the label now, so it get drawn.
						label = mKeyboardName;
					}
				} else {
					// ho... no icon.
					// I'll try to guess the text
					label = guessLabelForKey(key);
					if (TextUtils.isEmpty(label)) {
						Log.w(TAG, "That's unfortunate, for key "
								+ key.codes[0] + " at (" + key.x + ", " + key.y
								+ ") there is no icon nor label. Action ID is "
								+ mKeyboardActionType);
					}
				}
			}

			if (label != null) {
				// For characters, use large font. For labels like "Done", use
				// small font.
				final FontMetrics fm;
				if (keyIsSpace) {
					paint.setTextSize(mKeyboardNameTextSize);
					paint.setTypeface(Typeface.DEFAULT);
					if (mKeyboardNameFM == null)
						mKeyboardNameFM = paint.getFontMetrics();
					fm = mKeyboardNameFM;
				} else if (label.length() > 1 && key.codes.length < 2) {
					paint.setTextSize(mLabelTextSize);
					paint.setTypeface(Typeface.DEFAULT_BOLD);
					if (mLabelFM == null)
						mLabelFM = paint.getFontMetrics();
					fm = mLabelFM;
				} else {
					paint.setTextSize(mKeyTextSize);
					paint.setTypeface(mKeyTextStyle);
					if (mTextFM == null)
						mTextFM = paint.getFontMetrics();
					fm = mTextFM;
				}

				final float labelHeight = -fm.top;
				// Draw a drop shadow for the text
				paint.setShadowLayer(mShadowRadius, mShadowOffsetX,
						mShadowOffsetY, mShadowColor);

				// (+)This is the trick to get RTL/LTR text correct
				// no matter what: StaticLayout
				// this should be in the top left corner of the key
				float textWidth = paint.measureText(label, 0, label.length());
				//I'm going to try something if the key is too small for the text:
				//1) divide the text size by 1.5
				//2) if still too large, divide by 2.5
				//3) show no text
				if (textWidth > key.width) {
					if (AnyApplication.DEBUG) Log.d(TAG, "Label '"+label+"' is too large for the key. Reducing by 1.5.");
					paint.setTextSize(mKeyTextSize/1.5f);
					textWidth = paint.measureText(label, 0, label.length());
					if (textWidth > key.width) {
						if (AnyApplication.DEBUG) Log.d(TAG, "Label '"+label+"' is too large for the key. Reducing by 2.5.");
						paint.setTextSize(mKeyTextSize/2.5f);
						textWidth = paint.measureText(label, 0, label.length());
						if (textWidth > key.width) {
							if (AnyApplication.DEBUG) Log.d(TAG, "Label '"+label+"' is too large for the key. Showing no text.");
							paint.setTextSize(0f);
							textWidth = paint.measureText(label, 0, label.length());
						}
					}
				}

				// the center of the drawable space, which is value used
				// previously for vertically
				// positioning the key label
				final float centerY = mKeyBackgroundPadding.top
						+ (key.height - mKeyBackgroundPadding.top - mKeyBackgroundPadding.bottom)
						/ 2;

				// the X coordinate for the center of the main label text is
				// unaffected by the hints
				final float centerX = mKeyBackgroundPadding.left
						+ (key.width - mKeyBackgroundPadding.left - mKeyBackgroundPadding.right)
						/ 2;

				final float textX = centerX;
				final float textY;
				// Some devices (mostly pre-Honeycomb, have issues with RTL text
				// drawing.
				// Of course, there is no issue with a single character :)
				// so, we'll use the RTL secured drawing (via StaticLayout) for
				// labels.
				if (label.length() > 1
						&& !AnyApplication.getConfig()
								.workaround_alwaysUseDrawText()) {
					// calculate Y coordinate of top of text based on center
					// location
					textY = centerY - ((labelHeight - paint.descent()) / 2);
					canvas.translate(textX, textY);
					if (AnyApplication.DEBUG)
						Log.d(TAG, "Using RTL fix for key draw '" + label + "'");
					// RTL fix. But it costs, let do it when in need (more than
					// 1 character)
					StaticLayout labelText = new StaticLayout(label,
							new TextPaint(paint), (int) textWidth,
							Alignment.ALIGN_NORMAL, 0.0f, 0.0f, false);
					labelText.draw(canvas);
				} else {
					// to get Y coordinate of baseline from center of text,
					// first add half the height (to get to
					// bottom of text), then subtract the part below the
					// baseline. Note that fm.top is negative.
					textY = centerY + ((labelHeight - paint.descent()) / 2);
					canvas.translate(textX, textY);
					canvas.drawText(label, 0, label.length(), 0, 0, paint);
				}
				canvas.translate(-textX, -textY);
				// (-)

				// Turn off drop shadow
				paint.setShadowLayer(0, 0, 0, 0);
			}

			if (drawHintText) {
				if ((key.popupCharacters != null && key.popupCharacters
						.length() > 0)
						|| (key.popupResId != 0)
						|| (key.longPressCode != 0)) {
					Paint.Align oldAlign = paint.getTextAlign();

					String hintText = null;

					if (key.hintLabel != null && key.hintLabel.length() > 0) {
						hintText = key.hintLabel.toString();
						// it is the responsibility of the keyboard layout
						// designer to ensure that they do
						// not put too many characters in the hint label...
					} else if (key.longPressCode != 0) {
						if (Character.isLetterOrDigit(key.longPressCode))
							hintText = Character
									.toString((char) key.longPressCode);
					} else if (key.popupCharacters != null) {
						final String hintString = key.popupCharacters
								.toString();
						final int hintLength = hintString.length();
						if (hintLength <= 3)
							hintText = hintString;
					}

					// if hintText is still null, it means it didn't fit one of
					// the above
					// cases, so we should provide the hint using the default
					if (hintText == null) {
						if (mHintOverflowLabel != null)
							hintText = mHintOverflowLabel.toString();
						else {
							// theme does not provide a defaultHintLabel
							// use ˙˙˙ if hints are above, ... if hints are
							// below
							// (to avoid being too close to main label/icon)
							if (hintVAlign == Gravity.TOP)
								hintText = "˙˙˙";
							else
								hintText = "...";
						}
					}

					if (mKeyboard.isShifted())
						hintText = hintText.toUpperCase();

					// now draw hint
					paint.setTypeface(Typeface.DEFAULT);
					paint.setColor(hintColor.getColorForState(drawableState,
							0xFF000000));
					paint.setTextSize(mHintTextSize);
					// get the hint text font metrics so that we know the size
					// of the hint when
					// we try to position the main label (to try to make sure
					// they don't overlap)
					if (mHintTextFM == null) {
						mHintTextFM = paint.getFontMetrics();
					}

					final float hintX;
					final float hintY;

					// the (float) 0.5 value is added or subtracted to just give
					// a little more room
					// in case the theme designer didn't account for the hint
					// label location
					if (hintAlign == Gravity.LEFT) {
						// left
						paint.setTextAlign(Paint.Align.LEFT);
						hintX = mKeyBackgroundPadding.left + (float) 0.5;
					} else if (hintAlign == Gravity.CENTER) {
						// center
						paint.setTextAlign(Paint.Align.CENTER);
						hintX = mKeyBackgroundPadding.left
								+ (key.width - mKeyBackgroundPadding.left - mKeyBackgroundPadding.right)
								/ 2;
					} else {
						// right
						paint.setTextAlign(Paint.Align.RIGHT);
						hintX = key.width - mKeyBackgroundPadding.right
								- (float) 0.5;
					}

					if (hintVAlign == Gravity.TOP) {
						// above
						hintY = mKeyBackgroundPadding.top - mHintTextFM.top
								+ (float) 0.5;
					} else {
						// below
						hintY = key.height - mKeyBackgroundPadding.bottom
								- mHintTextFM.bottom - (float) 0.5;
					}

					canvas.drawText(hintText, hintX, hintY, paint);
					paint.setTextAlign(oldAlign);
				}
			}

			canvas.translate(-key.x - kbdPaddingLeft, -key.y - kbdPaddingTop);
		}
		mInvalidatedKey = null;
		// Overlay a dark rectangle to dim the keyboard
		if (mMiniKeyboard != null && mMiniKeyboardVisible) {
			paint.setColor((int) (mBackgroundDimAmount * 0xFF) << 24);
			canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
		}

		if (AnyApplication.DEBUG) {
			if (mShowTouchPoints) {
				for (PointerTracker tracker : mPointerTrackers) {
					int startX = tracker.getStartX();
					int startY = tracker.getStartY();
					int lastX = tracker.getLastX();
					int lastY = tracker.getLastY();
					paint.setAlpha(128);
					paint.setColor(0xFFFF0000);
					canvas.drawCircle(startX, startY, 3, paint);
					canvas.drawLine(startX, startY, lastX, lastY, paint);
					paint.setColor(0xFF0000FF);
					canvas.drawCircle(lastX, lastY, 3, paint);
					paint.setColor(0xFF00FF00);
					canvas.drawCircle((startX + lastX) / 2,
							(startY + lastY) / 2, 2, paint);
				}
			}
		}

		mDrawPending = false;
		mDirtyRect.setEmpty();
	}

	private static boolean isSpaceKey(final AnyKey key) {
		return key.codes.length > 0 && key.codes[0] == KeyCodes.SPACE;
	}

	int mKeyboardActionType = EditorInfo.IME_ACTION_UNSPECIFIED;

	public void setKeyboardActionType(final int imeOptions) {
		if (AnyApplication.DEBUG)
			Log.d(TAG, "setKeyboardActionType imeOptions:" + imeOptions
					+ " action:" + (imeOptions & EditorInfo.IME_MASK_ACTION));
		if ((imeOptions & EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0)// this is
																	// usually a
																	// multi-line
																	// edittext
																	// box
			mKeyboardActionType = EditorInfo.IME_ACTION_UNSPECIFIED;
		else
			mKeyboardActionType = (imeOptions & EditorInfo.IME_MASK_ACTION);

		// setting the icon/text
		setSpecialKeysIconsAndLabels();
	}

	public void setSpecialKeysIconsAndLabels() {
		List<Key> keys = (mKeyboard != null) ? mKeyboard.getKeys() : null;
		if (keys != null) {
			for (Key key : keys) {
				if (key.codes != null && key.codes.length > 0
						&& key.codes[0] == KeyCodes.ENTER) {
					key.icon = null;
					key.iconPreview = null;
					key.label = null;
					((AnyKey) key).shiftedKeyLabel = null;
					Drawable icon = getIconToDrawForKey(key, false);
					if (icon != null) {
						key.icon = icon;
						key.iconPreview = icon;
					} else {
						CharSequence label = guessLabelForKey((AnyKey) key);
						key.label = label;
						((AnyKey) key).shiftedKeyLabel = label;
					}
					// making sure something is shown
					if (key.icon == null && TextUtils.isEmpty(key.label)) {
						Log.i(TAG, "Wow. Unknown ACTION ID "
								+ mKeyboardActionType
								+ ". Will default to ENTER icon.");
						// I saw devices (Galaxy Tab 10") which say the action
						// type is 255...
						// D/ASKKbdViewBase( 3594): setKeyboardActionType
						// imeOptions:33554687 action:255
						// which means it is not a known ACTION
						mActionKeyIcon.setState(DRAWABLE_STATE_ACTION_NORMAL);
						key.icon = mActionKeyIcon;
						key.iconPreview = mActionKeyIcon;
					}
				}
			}
		}
	}

	private CharSequence guessLabelForKey(AnyKey key) {
		switch (key.codes[0]) {
		case KeyCodes.ENTER:
			if (AnyApplication.DEBUG)
				Log.d(TAG, "Action key action ID is: " + mKeyboardActionType);
			switch (mKeyboardActionType) {
			case EditorInfo.IME_ACTION_DONE:
				return getContext().getText(R.string.label_done_key);
			case EditorInfo.IME_ACTION_GO:
				return getContext().getText(R.string.label_go_key);
			case EditorInfo.IME_ACTION_NEXT:
				return getContext().getText(R.string.label_next_key);
			case 0x00000007:// API 11: EditorInfo.IME_ACTION_PREVIOUS:
				return getContext().getText(R.string.label_previous_key);
			case EditorInfo.IME_ACTION_SEARCH:
				return getContext().getText(R.string.label_search_key);
			case EditorInfo.IME_ACTION_SEND:
				return getContext().getText(R.string.label_send_key);
			default:
				return "";
			}
		case KeyCodes.MODE_ALPHABET:
			return getContext().getText(R.string.change_lang_regular);
		case KeyCodes.TAB:
			return getContext().getText(R.string.label_tab_key);
		case KeyCodes.MOVE_HOME:
			return getContext().getText(R.string.label_home_key);
		case KeyCodes.MOVE_END:
			return getContext().getText(R.string.label_end_key);
		default:
			return null;
		}
	}

	private Drawable getIconToDrawForKey(Key key, boolean feedback) {
		if (feedback && key.iconPreview != null)
			return key.iconPreview;
		if (key.icon != null)
			return key.icon;

		switch (key.codes[0]) {
		case KeyCodes.ENTER:
			if (AnyApplication.DEBUG)
				Log.d(TAG, "Action key action ID is: " + mKeyboardActionType);
			Drawable actionKeyDrawable = null;
			switch (mKeyboardActionType) {
			case EditorInfo.IME_ACTION_DONE:
				mActionKeyIcon.setState(DRAWABLE_STATE_ACTION_DONE);
				actionKeyDrawable = mActionKeyIcon;
				break;
			case EditorInfo.IME_ACTION_GO:
				mActionKeyIcon.setState(DRAWABLE_STATE_ACTION_GO);
				actionKeyDrawable = mActionKeyIcon;
				break;
			case EditorInfo.IME_ACTION_SEARCH:
				mActionKeyIcon.setState(DRAWABLE_STATE_ACTION_SEARCH);
				actionKeyDrawable = mActionKeyIcon;
				break;
			case EditorInfo.IME_ACTION_NONE:
			case EditorInfo.IME_ACTION_UNSPECIFIED:
				mActionKeyIcon.setState(DRAWABLE_STATE_ACTION_NORMAL);
				actionKeyDrawable = mActionKeyIcon;
				break;
			}
			return actionKeyDrawable;
		case KeyCodes.DELETE:
			return mDeleteKeyIcon;
		case KeyCodes.SPACE:
			return mSpaceKeyIcon;
		case KeyCodes.SHIFT:
			if (mKeyboard.isShiftLocked())
				mShiftIcon.setState(DRAWABLE_STATE_MODIFIER_LOCKED);
			else if (mKeyboard.isShifted())
				mShiftIcon.setState(DRAWABLE_STATE_MODIFIER_PRESSED);
			else
				mShiftIcon.setState(DRAWABLE_STATE_MODIFIER_NORMAL);
			return mShiftIcon;
		case KeyCodes.CANCEL:
			return mCancelKeyIcon;
		case KeyCodes.MODE_ALPHABET:
			if (key.label == null)
				return mGlobeKeyIcon;
			else
				return null;
		case KeyCodes.CTRL:
			/*
			 * if (mKeyboard.isControlLocked())
			 * mControlIcon.setState(DRAWABLE_STATE_MODIFIER_LOCKED); else
			 */if (mKeyboard.isControl())
				mControlIcon.setState(DRAWABLE_STATE_MODIFIER_PRESSED);
			else
				mControlIcon.setState(DRAWABLE_STATE_MODIFIER_NORMAL);
			return mControlIcon;
		case KeyCodes.TAB:
			return mTabKeyIcon;
		case KeyCodes.ARROW_DOWN:
			return mArrowDownKeyIcon;
		case KeyCodes.ARROW_LEFT:
			return mArrowLeftKeyIcon;
		case KeyCodes.ARROW_RIGHT:
			return mArrowRightKeyIcon;
		case KeyCodes.ARROW_UP:
			return mArrowUpKeyIcon;
		case KeyCodes.MOVE_HOME:
			return mMoveHomeKeyIcon;
		case KeyCodes.MOVE_END:
			return mMoveEndKeyIcon;
		case KeyCodes.VOICE_INPUT:
			return mMicKeyIcon;
		case KeyCodes.SETTINGS:
			return mSettingsKeyIcon;
		default:
			return null;
		}
	}

	void dismissKeyPreview() {
		for (PointerTracker tracker : mPointerTrackers)
			tracker.updateKey(NOT_A_KEY);
		showPreview(NOT_A_KEY, null);
	}

	public void showPreview(int keyIndex, PointerTracker tracker) {
		int oldKeyIndex = mOldPreviewKeyIndex;
		mOldPreviewKeyIndex = keyIndex;
		final boolean isLanguageSwitchEnabled = false;
		// We should re-draw popup preview when 1) we need to hide the preview,
		// 2) we will show
		// the space key preview and 3) pointer moves off the space key to other
		// letter key, we
		// should hide the preview of the previous key.
		final boolean hidePreviewOrShowSpaceKeyPreview = (tracker == null)/*
																		 * ||
																		 * tracker
																		 * .
																		 * isSpaceKey
																		 * (
																		 * keyIndex
																		 * ) ||
																		 * tracker
																		 * .
																		 * isSpaceKey
																		 * (
																		 * oldKeyIndex
																		 * )
																		 */;
		// If key changed and preview is on or the key is space (language switch
		// is enabled)
		if (oldKeyIndex != keyIndex
				&& (mShowPreview || (hidePreviewOrShowSpaceKeyPreview && isLanguageSwitchEnabled))) {
			if (keyIndex == NOT_A_KEY) {
				mHandler.cancelPopupPreview();
				mHandler.dismissPreview(mDelayAfterPreview);
			} else if (tracker != null) {
				mHandler.popupPreview(mDelayBeforePreview, keyIndex, tracker);
			}
		}
	}

	private void showKey(final int keyIndex, PointerTracker tracker) {
		Key key = tracker.getKey(keyIndex);
		if (key == null || !mShowPreview)
			return;
		int popupWidth = 0;
		int popupHeight = 0;
		// Should not draw hint icon in key preview
		CharSequence label = tracker.getPreviewText(key, mKeyboard.isShifted());
		if (TextUtils.isEmpty(label)) {
			Drawable iconToDraw = getIconToDrawForKey(key, true);
			// mPreviewText.setCompoundDrawables(null, null, null,
			// key.iconPreview != null ? key.iconPreview : key.icon);
			mPreviewIcon.setImageDrawable(iconToDraw);
			if (key.codes.length > 0 && key.codes[0] == KeyCodes.SPACE) {
				// in the spacebar we'll also add the current gesture, with alpha [0...128,255].
				//if any
				//TODO: the above
				//mPreviewText.setTextColor(color+alpha)
				mPreviewText.setText(null);
			} else {
				mPreviewText.setText(null);
			}
			mPreviewIcon.measure(
					MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
					MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
			popupWidth = Math.max(mPreviewIcon.getMeasuredWidth(), key.width);
			popupHeight = Math
					.max(mPreviewIcon.getMeasuredHeight(), key.height);
		} else {
			// mPreviewText.setCompoundDrawables(null, null, null, null);
			mPreviewIcon.setImageDrawable(null);
			setKeyPreviewText(key, label);
			popupWidth = Math.max(mPreviewText.getMeasuredWidth(), key.width);
			popupHeight = Math
					.max(mPreviewText.getMeasuredHeight(), key.height);
		}

		if (mPreviewPaddingHeight < 0) {
			mPreviewPaddingWidth = mPreviewLayut.getPaddingLeft()
					+ mPreviewLayut.getPaddingRight();
			mPreviewPaddingHeight = mPreviewLayut.getPaddingTop()
					+ mPreviewLayut.getPaddingBottom();

			if (mPreviewKeyBackground != null) {
				Rect padding = new Rect();
				mPreviewKeyBackground.getPadding(padding);
				mPreviewPaddingWidth += (padding.left + padding.right);
				mPreviewPaddingHeight += (padding.top + padding.bottom);
			}
		}
		popupWidth += mPreviewPaddingWidth;
		popupHeight += mPreviewPaddingHeight;

		// and checking that the width and height are big enough for the
		// background.
		if (mPreviewKeyBackground != null) {
			popupWidth = Math.max(mPreviewKeyBackground.getMinimumWidth(),
					popupWidth);
			popupHeight = Math.max(mPreviewKeyBackground.getMinimumHeight(),
					popupHeight);
		}

		final boolean showPopupAboveKey = AnyApplication.getConfig()
				.showKeyPreviewAboveKey();
		int popupPreviewX = showPopupAboveKey ? key.x
				- ((popupWidth - key.width) / 2)
				: (getWidth() - popupWidth) / 2;
		int popupPreviewY = (showPopupAboveKey ? key.y : 0) - popupHeight
				- mPreviewOffset;

		mHandler.cancelDismissPreview();
		if (mOffsetInWindow == null) {
			mOffsetInWindow = new int[] { 0, 0 };
			getLocationInWindow(mOffsetInWindow);
			if (AnyApplication.DEBUG)
				Log.d(TAG, "mOffsetInWindow " + mOffsetInWindow[0] + ", "
						+ mOffsetInWindow[1]);
			mOffsetInWindow[0] += mPopupPreviewOffsetX; // Offset may be zero
			mOffsetInWindow[1] += mPopupPreviewOffsetY; // Offset may be zero
			int[] windowLocation = new int[2];
			getLocationOnScreen(windowLocation);
			mWindowY = windowLocation[1];
		}
		popupPreviewX += mOffsetInWindow[0];
		popupPreviewY += mOffsetInWindow[1];

		// If the popup cannot be shown above the key, put it on the side
		if (popupPreviewY + mWindowY < 0) {
			// If the key you're pressing is on the left side of the keyboard,
			// show the popup on
			// the right, offset by enough to see at least one key to the
			// left/right.
			if (key.x + key.width <= getWidth() / 2) {
				popupPreviewX += (int) (key.width * 2.5);
			} else {
				popupPreviewX -= (int) (key.width * 2.5);
			}
			popupPreviewY += popupHeight;
		}

		if (mPreviewPopup.isShowing()) {
			if (mStaticLocationPopupWindow) {
				// started at SPACE, so I stick with the position. This is used
				// for
				// showing gesture info

			} else {
				mPreviewPopup.update(popupPreviewX, popupPreviewY, popupWidth,
						popupHeight);
			}
		} else {
			mPreviewPopup.setWidth(popupWidth);
			mPreviewPopup.setHeight(popupHeight);
			// if started at SPACE, so I stick with the position. This is used
			// for
			// showing gesture info
			mStaticLocationPopupWindow = key.codes.length > 0
					&& key.codes[0] == KeyCodes.SPACE;
			try {
				// https://github.com/AnySoftKeyboard/AnySoftKeyboard/issues/6
				// I don't understand why this should happen, and only with MIUI
				// ROMs.
				// anyhow, it easy to hide :)
				mPreviewPopup.showAtLocation(mMiniKeyboardParent,
						Gravity.NO_GRAVITY, popupPreviewX, popupPreviewY);
			} catch (RuntimeException e) {
				// nothing to do here. I think.
			}

		}
		// mPreviewPopup.getContentView().invalidate();
		// Record popup preview position to display mini-keyboard later at the
		// same positon
		mPopupPreviewDisplayedY = popupPreviewY
				+ (showPopupAboveKey ? 0 : key.y);// the popup keyboard should
													// be
													// placed at the right
													// position.
													// So I'm fixing
		mPreviewLayut.setVisibility(VISIBLE);

		// Set the preview background state
		if (mPreviewKeyBackground != null) {
			mPreviewKeyBackground
					.setState(key.popupResId != 0 ? LONG_PRESSABLE_STATE_SET
							: EMPTY_STATE_SET);
		}

		// LayoutParams lp = mPreviewLayut.getLayoutParams();
		// lp.width = popupWidth;
	}

	private void setKeyPreviewText(Key key, CharSequence label) {
		mPreviewText.setText(label);
		if (label.length() > 1 && key.codes.length < 2) {
			mPreviewText.setTextSize(TypedValue.COMPLEX_UNIT_PX,
					mPreviewLabelTextSize);
		} else {
			mPreviewText.setTextSize(TypedValue.COMPLEX_UNIT_PX,
					mPreviewKeyTextSize);
		}

		mPreviewText.measure(
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
	}

	/**
	 * Requests a redraw of the entire keyboard. Calling {@link #invalidate} is
	 * not sufficient because the keyboard renders the keys to an off-screen
	 * buffer and an invalidate() only draws the cached buffer.
	 * 
	 * @see #invalidateKey(Key)
	 */
	public void invalidateAllKeys() {
		mDirtyRect.union(0, 0, getWidth(), getHeight());
		mDrawPending = true;
		invalidate();
	}

	/**
	 * Invalidates a key so that it will be redrawn on the next repaint. Use
	 * this method if only one key is changing it's content. Any changes that
	 * affect the position or size of the key may not be honored.
	 * 
	 * @param key
	 *            key in the attached {@link Keyboard}.
	 * @see #invalidateAllKeys
	 */
	public void invalidateKey(Key key) {
		if (key == null)
			return;
		mInvalidatedKey = key;
		// TODO we should clean up this and record key's region to use in
		// onBufferDraw.
		mDirtyRect.union(key.x + getPaddingLeft(), key.y + getPaddingTop(),
				key.x + key.width + getPaddingLeft(), key.y + key.height
						+ getPaddingTop());
		// doOnBufferDrawWithMemProtection(mCanvas);
		invalidate(key.x + getPaddingLeft(), key.y + getPaddingTop(), key.x
				+ key.width + getPaddingLeft(), key.y + key.height
				+ getPaddingTop());
	}

	private boolean openPopupIfRequired(int keyIndex, PointerTracker tracker) {
		/*
		 * this is a uselss code.. // Check if we have a popup layout specified
		 * first. if (mPopupLayout == 0) { return false; }
		 */
		Key popupKey = tracker.getKey(keyIndex);
		if (popupKey == null)
			return false;
		boolean result = onLongPress(getContext(), popupKey, false, true);
		if (result) {
			dismissKeyPreview();
			mMiniKeyboardTrackerId = tracker.mPointerId;
			// Mark this tracker "already processed" and remove it from the
			// pointer queue
			tracker.setAlreadyProcessed();
			mPointerQueue.remove(tracker);
		}
		return result;
	}

	private void setupMiniKeyboardContainer(Context packageContext,
			CharSequence popupCharacters, int popupKeyboardId, boolean isSticky) {
		final AnyPopupKeyboard keyboard;
		if (popupCharacters != null) {
			keyboard = new AnyPopupKeyboard(mAskContext, popupCharacters,
					mMiniKeyboard.getThemedKeyboardDimens());
		} else {
			keyboard = new AnyPopupKeyboard(mAskContext, packageContext,
					popupKeyboardId, mMiniKeyboard.getThemedKeyboardDimens());
		}
		keyboard.setIsOneKeyEventPopup(!isSticky);

		if (isSticky)
			mMiniKeyboard.setKeyboard(keyboard, mVerticalCorrection);
		else
			mMiniKeyboard.setKeyboard(keyboard);

		mMiniKeyboard.measure(
				MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.AT_MOST),
				MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.AT_MOST));
	}

	public KeyboardDimens getThemedKeyboardDimens() {
		return mKeyboardDimens;
	}

	/*
	 * private static boolean isOneRowKeys(List<Key> keys) { if (keys.size() ==
	 * 0) return false; final int edgeFlags = keys.get(0).edgeFlags; // HACK:
	 * The first key of mini keyboard which was inflated from xml and has
	 * multiple rows, // does not have both top and bottom edge flags on at the
	 * same time. On the other hand, // the first key of mini keyboard that was
	 * created with popupCharacters must have both top // and bottom edge flags
	 * on. // When you want to use one row mini-keyboard from xml file, make
	 * sure that the row has // both top and bottom edge flags set. return
	 * (edgeFlags & Keyboard.EDGE_TOP) != 0 && (edgeFlags &
	 * Keyboard.EDGE_BOTTOM) != 0; }
	 */
	/**
	 * Called when a key is long pressed. By default this will open any popup
	 * keyboard associated with this key through the attributes popupLayout and
	 * popupCharacters.
	 * 
	 * @param popupKey
	 *            the key that was long pressed
	 * @return true if the long press is handled, false otherwise. Subclasses
	 *         should call the method on the base class if the subclass doesn't
	 *         wish to handle the call.
	 */
	protected boolean onLongPress(Context packageContext, Key popupKey,
			boolean isSticky, boolean requireSlideInto) {
		if (popupKey.popupResId == 0)
			return false;

		if (mMiniKeyboard == null) {
			createMiniKeyboard();
		}
		setupMiniKeyboardContainer(packageContext, popupKey.popupCharacters,
				popupKey.popupResId, isSticky);
		mMiniKeyboardVisible = true;
		if (mWindowOffset == null) {
			mWindowOffset = new int[2];
			getLocationInWindow(mWindowOffset);
		}

		int popupX = popupKey.x + mWindowOffset[0];
		popupX -= mMiniKeyboard.getPaddingLeft();
		int popupY = popupKey.y + mWindowOffset[1];
		popupY += getPaddingTop();
		popupY -= mMiniKeyboard.getMeasuredHeight();
		popupY -= mMiniKeyboard.getPaddingBottom();
		final int x = popupX;
		final int y = mShowPreview && mOldPreviewKeyIndex != NOT_A_KEY
				&& isOneRowKeys(mMiniKeyboard.getKeyboard().getKeys()) ? mPopupPreviewDisplayedY
				: popupY;

		int adjustedX = x;
		if (x < 0) {
			adjustedX = 0;
		} else if (x > (getMeasuredWidth() - mMiniKeyboard.getMeasuredWidth())) {
			adjustedX = getMeasuredWidth() - mMiniKeyboard.getMeasuredWidth();
		}
		mMiniKeyboardOriginX = adjustedX + mMiniKeyboard.getPaddingLeft()
				- mWindowOffset[0];
		mMiniKeyboardOriginY = y + mMiniKeyboard.getPaddingTop()
				- mWindowOffset[1];
		mMiniKeyboard.setPopupOffset(adjustedX, y);
		// NOTE:I'm checking the main keyboard shift state directly!
		// Not anything else.
		mMiniKeyboard.setShifted(mKeyboard != null ? mKeyboard.isShifted()
				: false);
		// Mini keyboard needs no pop-up key preview displayed.
		mMiniKeyboard.setPreviewEnabled(false);
		// animation switching required?
		mMiniKeyboardPopup.setContentView(mMiniKeyboard);
		mMiniKeyboardPopup.setWidth(mMiniKeyboard.getMeasuredWidth());
		mMiniKeyboardPopup.setHeight(mMiniKeyboard.getMeasuredHeight());
		mMiniKeyboardPopup.showAtLocation(this, Gravity.NO_GRAVITY, adjustedX,
				y);

		if (requireSlideInto) {
			// Inject down event on the key to mini keyboard.
			long eventTime = SystemClock.uptimeMillis();
			mMiniKeyboardPopupTime = eventTime;
			MotionEvent downEvent = generateMiniKeyboardMotionEvent(
					MotionEvent.ACTION_DOWN, popupKey.x + popupKey.width / 2,
					popupKey.y + popupKey.height / 2, eventTime);
			mMiniKeyboard.onTouchEvent(downEvent);
			downEvent.recycle();
		}

		invalidateAllKeys();
		return true;
	}

	public void createMiniKeyboard() {
		LayoutInflater inflater = (LayoutInflater) getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mMiniKeyboard = (AnyKeyboardBaseView) inflater.inflate(
				R.layout.popup_keyboard_layout, null);

		mMiniKeyboard.setPopupParent(this);

		mMiniKeyboard
				.setOnKeyboardActionListener(new OnKeyboardActionListener() {

					private final AnyKeyboardBaseView mParent = mMiniKeyboard;

					private AnyPopupKeyboard getMyKeyboard() {
						return (AnyPopupKeyboard) mParent.getKeyboard();
					}

					public void onKey(int primaryCode, Key key,
							int multiTapIndex, int[] nearByKeyCodes,
							boolean fromUI) {
						mKeyboardActionListener.onKey(primaryCode, key,
								multiTapIndex, nearByKeyCodes, fromUI);
						if (getMyKeyboard().isOneKeyEventPopup())
							dismissPopupKeyboard();
					}

					public void onMultiTapStarted() {
						mKeyboardActionListener.onMultiTapStarted();
					}

					public void onMultiTapEndeded() {
						mKeyboardActionListener.onMultiTapEndeded();
					}

					public void onText(CharSequence text) {
						mKeyboardActionListener.onText(text);
						if (getMyKeyboard().isOneKeyEventPopup())
							dismissPopupKeyboard();
					}

					public void onCancel() {
						mKeyboardActionListener.onCancel();
						dismissPopupKeyboard();
					}

					public void onSwipeLeft(boolean onSpacebar) {
					}

					public void onSwipeRight(boolean onSpacebar) {
					}

					public void onSwipeUp(boolean onSpacebar) {
					}

					public void onSwipeDown(boolean onSpacebar) {
					}

					public void onPinch() {
					}

					public void onSeparate() {
					}

					public void onPress(int primaryCode) {
						mKeyboardActionListener.onPress(primaryCode);
					}

					public void onRelease(int primaryCode) {
						mKeyboardActionListener.onRelease(primaryCode);
					}
				});
		// Override default ProximityKeyDetector.
		mMiniKeyboard.mKeyDetector = new MiniKeyboardKeyDetector(
				mMiniKeyboardSlideAllowance);
		// Remove gesture detector on mini-keyboard
		mMiniKeyboard.mGestureDetector = null;
	}

	private static boolean isOneRowKeys(List<Key> keys) {
		if (keys.size() == 0)
			return false;
		final int edgeFlags = keys.get(0).edgeFlags;
		// HACK: The first key of mini keyboard which was inflated from xml and
		// has multiple rows,
		// does not have both top and bottom edge flags on at the same time. On
		// the other hand,
		// the first key of mini keyboard that was created with popupCharacters
		// must have both top
		// and bottom edge flags on.
		// When you want to use one row mini-keyboard from xml file, make sure
		// that the row has
		// both top and bottom edge flags set.
		return (edgeFlags & Keyboard.EDGE_TOP) != 0
				&& (edgeFlags & Keyboard.EDGE_BOTTOM) != 0;
	}

	// private static boolean hasMultiplePopupChars(Key key) {
	// if (key.popupCharacters != null && key.popupCharacters.length() > 1) {
	// return true;
	// }
	// return false;
	// }

	// private boolean shouldDrawIconFully(Key key) {
	// return isNumberAtEdgeOfPopupChars(key) /*|| isLatinF1Key(key)
	// || LatinKeyboard.hasPuncOrSmileysPopup(key)*/;
	// }
	//
	// private boolean shouldDrawLabelAndIcon(Key key) {
	// return isNumberAtEdgeOfPopupChars(key) /*|| isNonMicLatinF1Key(key)
	// || AnyKeyboard.hasPuncOrSmileysPopup(key)*/;
	// }

	// private boolean isLatinF1Key(Key key) {
	// return (mKeyboard instanceof AnyKeyboard) &&
	// ((AnyKeyboard)mKeyboard).isF1Key(key);
	// }

	// private boolean isNonMicLatinF1Key(Key key) {
	// return isLatinF1Key(key) && key.label != null;
	// }

	// private static boolean isNumberAtEdgeOfPopupChars(Key key) {
	// return isNumberAtLeftmostPopupChar(key) ||
	// isNumberAtRightmostPopupChar(key);
	// }

	// /* package */ static boolean isNumberAtLeftmostPopupChar(Key key) {
	// if (key.popupCharacters != null && key.popupCharacters.length() > 0
	// && isAsciiDigit(key.popupCharacters.charAt(0))) {
	// return true;
	// }
	// return false;
	// }

	// /* package */ static boolean isNumberAtRightmostPopupChar(Key key) {
	// if (key.popupCharacters != null && key.popupCharacters.length() > 0
	// && isAsciiDigit(key.popupCharacters.charAt(key.popupCharacters.length() -
	// 1))) {
	// return true;
	// }
	// return false;
	// }
	//
	// private static boolean isAsciiDigit(char c) {
	// return (c < 0x80) && Character.isDigit(c);
	// }

	private MotionEvent generateMiniKeyboardMotionEvent(int action, int x,
			int y, long eventTime) {
		return MotionEvent.obtain(mMiniKeyboardPopupTime, eventTime, action, x
				- mMiniKeyboardOriginX, y - mMiniKeyboardOriginY, 0);
	}

	private PointerTracker getPointerTracker(final int id) {
		final ArrayList<PointerTracker> pointers = mPointerTrackers;
		final Key[] keys = mKeys;
		final OnKeyboardActionListener listener = mKeyboardActionListener;

		// Create pointer trackers until we can get 'id+1'-th tracker, if
		// needed.
		for (int i = pointers.size(); i <= id; i++) {
			final PointerTracker tracker = new PointerTracker(i, mHandler,
					mKeyDetector, this, getResources());
			if (keys != null)
				tracker.setKeyboard(keys, mKeyHysteresisDistance);
			if (listener != null)
				tracker.setOnKeyboardActionListener(listener);
			pointers.add(tracker);
		}

		return pointers.get(id);
	}

	public boolean isInSlidingKeyInput() {
		if (mMiniKeyboard != null && mMiniKeyboardVisible) {
			return mMiniKeyboard.isInSlidingKeyInput();
		} else {
			return mPointerQueue.isInSlidingKeyInput();
		}
	}

	public int getPointerCount() {
		return mOldPointerCount;
	}

	@Override
	public boolean onTouchEvent(MotionEvent nativeMotionEvent) {
		WMotionEvent me = AnyApplication.getDeviceSpecific()
				.createMotionEventWrapper(nativeMotionEvent);
		final int action = me.getActionMasked();
		final int pointerCount = me.getPointerCount();
		final int oldPointerCount = mOldPointerCount;
		mOldPointerCount = pointerCount;

		// TODO: cleanup this code into a multi-touch to single-touch event
		// converter class?
		// If the device does not have distinct multi-touch support panel,
		// ignore all multi-touch
		// events except a transition from/to single-touch.
		if (!mHasDistinctMultitouch && pointerCount > 1 && oldPointerCount > 1) {
			return true;
		}

		// Track the last few movements to look for spurious swipes.
		mSwipeTracker.addMovement(me.getNativeMotionEvent());

		// Gesture detector must be enabled only when mini-keyboard is not
		// on the screen.
		if (!mMiniKeyboardVisible
				&& mGestureDetector != null
				&& (mGestureDetector.onTouchEvent(me.getNativeMotionEvent()) /*
																			 * ||
																			 * mInScrollGesture
																			 */)) {
			if (AnyApplication.DEBUG)
				Log.d(TAG, "Gesture detected!");
			// mHandler.cancelAllMessages();
			mHandler.cancelKeyTimers();
			dismissKeyPreview();
			return true;
		}

		final long eventTime = me.getEventTime();
		final int index = me.getActionIndex();
		final int id = me.getPointerId(index);
		final int x = (int) me.getX(index);
		final int y = (int) me.getY(index);

		// Needs to be called after the gesture detector gets a turn, as it
		// may have
		// displayed the mini keyboard
		if (mMiniKeyboard != null && mMiniKeyboardVisible) {
			final int miniKeyboardPointerIndex = me
					.findPointerIndex(mMiniKeyboardTrackerId);
			if (miniKeyboardPointerIndex >= 0
					&& miniKeyboardPointerIndex < pointerCount) {
				final int miniKeyboardX = (int) me
						.getX(miniKeyboardPointerIndex);
				final int miniKeyboardY = (int) me
						.getY(miniKeyboardPointerIndex);
				MotionEvent translated = generateMiniKeyboardMotionEvent(
						action, miniKeyboardX, miniKeyboardY, eventTime);
				mMiniKeyboard.onTouchEvent(translated);
				translated.recycle();
			}
			return true;
		}

		if (mHandler.isInKeyRepeat()) {
			// It will keep being in the key repeating mode while the key is
			// being pressed.
			if (action == MotionEvent.ACTION_MOVE) {
				return true;
			}
			final PointerTracker tracker = getPointerTracker(id);
			// Key repeating timer will be canceled if 2 or more keys are in
			// action, and current
			// event (UP or DOWN) is non-modifier key.
			if (pointerCount > 1 && !tracker.isModifier()) {
				mHandler.cancelKeyRepeatTimer();
			}
			// Up event will pass through.
		}

		// TODO: cleanup this code into a multi-touch to single-touch event
		// converter class?
		// Translate mutli-touch event to single-touch events on the device
		// that has no distinct
		// multi-touch panel.
		if (!mHasDistinctMultitouch) {
			// Use only main (id=0) pointer tracker.
			PointerTracker tracker = getPointerTracker(0);
			if (pointerCount == 1 && oldPointerCount == 2) {
				// Multi-touch to single touch transition.
				// Send a down event for the latest pointer.
				tracker.onDownEvent(x, y, eventTime);
			} else if (pointerCount == 2 && oldPointerCount == 1) {
				// Single-touch to multi-touch transition.
				// Send an up event for the last pointer.
				tracker.onUpEvent(tracker.getLastX(), tracker.getLastY(),
						eventTime);
			} else if (pointerCount == 1 && oldPointerCount == 1) {
				tracker.onTouchEvent(action, x, y, eventTime);
			} else {
				Log.w(TAG, "Unknown touch panel behavior: pointer count is "
						+ pointerCount + " (old " + oldPointerCount + ")");
			}
			return true;
		}

		if (action == MotionEvent.ACTION_MOVE) {
			for (int i = 0; i < pointerCount; i++) {
				PointerTracker tracker = getPointerTracker(me.getPointerId(i));
				tracker.onMoveEvent((int) me.getX(i), (int) me.getY(i),
						eventTime);
			}
		} else {
			PointerTracker tracker = getPointerTracker(id);
			sendOnXEvent(action, eventTime, x, y, tracker);
		}

		return true;
	}

	protected boolean isFirstDownEventInsideSpaceBar() {
		return false;
	}

	private void sendOnXEvent(final int action, final long eventTime,
			final int x, final int y, PointerTracker tracker) {
		switch (action) {
		case MotionEvent.ACTION_DOWN:
		case 0x00000005:// MotionEvent.ACTION_POINTER_DOWN:
			onDownEvent(tracker, x, y, eventTime);
			break;
		case MotionEvent.ACTION_UP:
		case 0x00000006:// MotionEvent.ACTION_POINTER_UP:
			onUpEvent(tracker, x, y, eventTime);
			break;
		case MotionEvent.ACTION_CANCEL:
			onCancelEvent(tracker, x, y, eventTime);
			break;
		}
	}

	protected void onDownEvent(PointerTracker tracker, int x, int y,
			long eventTime) {
		if (tracker.isOnModifierKey(x, y)) {
			// Before processing a down event of modifier key, all pointers
			// already being tracked
			// should be released.
			mPointerQueue.releaseAllPointersExcept(tracker, eventTime);
		}
		tracker.onDownEvent(x, y, eventTime);
		mPointerQueue.add(tracker);
	}

	private void onUpEvent(PointerTracker tracker, int x, int y, long eventTime) {
		if (tracker.isModifier()) {
			// Before processing an up event of modifier key, all pointers
			// already being tracked
			// should be released.
			mPointerQueue.releaseAllPointersExcept(tracker, eventTime);
		} else {
			int index = mPointerQueue.lastIndexOf(tracker);
			if (index >= 0) {
				mPointerQueue.releaseAllPointersOlderThan(tracker, eventTime);
			} else {
				Log.w(TAG,
						"onUpEvent: corresponding down event not found for pointer "
								+ tracker.mPointerId);
				return;
			}
		}
		tracker.onUpEvent(x, y, eventTime);
		mPointerQueue.remove(tracker);
	}

	private void onCancelEvent(PointerTracker tracker, int x, int y,
			long eventTime) {
		tracker.onCancelEvent(x, y, eventTime);
		mPointerQueue.remove(tracker);
	}

	/*
	 * protected void swipeRight(boolean onSpacebar) {
	 * mKeyboardActionListener.onSwipeRight(onSpacebar); } protected void
	 * swipeLeft(boolean onSpacebar) {
	 * mKeyboardActionListener.onSwipeLeft(onSpacebar); } protected void
	 * swipeUp(boolean onSpacebar) {
	 * mKeyboardActionListener.onSwipeUp(onSpacebar); } protected void
	 * swipeDown(boolean onSpacebar) {
	 * mKeyboardActionListener.onSwipeDown(onSpacebar); }
	 */
	/*
	 * protected void scrollGestureStarted(float dX, float dY) {
	 * mInScrollGesture = true; } protected void scrollGestureEnded() {
	 * mInScrollGesture = false; }
	 */
	protected Key findKeyByKeyCode(int keyCode) {
		if (getKeyboard() == null) {
			return null;
		}

		for (Key key : getKeyboard().getKeys()) {
			if (key.codes[0] == keyCode)
				return key;
		}
		return null;
	}

	public boolean closing() {
		mPreviewPopup.dismiss();
		mHandler.cancelAllMessages();

		if (!dismissPopupKeyboard()) {
			// mBuffer = null;
			// mCanvas = null;
			// mMiniKeyboardCache.clear();
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		AnyApplication.getConfig().removeChangedListener(this);
		closing();
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		Resources res = getResources();

		if (key.equals(res
				.getString(R.string.settings_key_swipe_distance_threshold))
				|| key.equals(res
						.getString(R.string.settings_key_swipe_velocity_threshold))) {
			reloadSwipeThresholdsSettings(res);
		} else if (key.equals(res
				.getString(R.string.settings_key_long_press_timeout))
				|| key.equals(res
						.getString(R.string.settings_key_multitap_timeout))) {
			closing();
			mPointerTrackers.clear();
		}

		mAnimationLevel = AnyApplication.getConfig().getAnimationsLevel();
		mPreviewPopup
				.setAnimationStyle((mAnimationLevel == AnimationsLevel.None) ? 0
						: R.style.KeyPreviewAnimation);
		mMiniKeyboardPopup
				.setAnimationStyle((mAnimationLevel == AnimationsLevel.None) ? 0
						: R.style.MiniKeyboardAnimation);

	}

	protected boolean isPopupShowing() {
		return mMiniKeyboardPopup != null && mMiniKeyboardVisible;
	}

	protected boolean dismissPopupKeyboard() {
		if (isPopupShowing()) {
			mMiniKeyboardPopup.dismiss();
			mMiniKeyboardVisible = false;
			mMiniKeyboardOriginX = 0;
			mMiniKeyboardOriginY = 0;
			invalidateAllKeys();
			return true;
		} else
			return false;
	}

	public boolean handleBack() {
		if (mMiniKeyboardPopup.isShowing()) {
			dismissPopupKeyboard();
			return true;
		}
		return false;
	}

	// helper classes to translate the hint positioning integer values from the
	// theme or settings into the Paint.Align and boolean values used in
	// onBufferDraw()
}
