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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.WeakHashMap;

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
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Region.Op;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.Layout.Alignment;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.anysoftkeyboard.AnySoftKeyboard;
import com.anysoftkeyboard.devicespecific.AskOnGestureListener;
import com.anysoftkeyboard.devicespecific.WMotionEvent;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.AnyPopupKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.Keyboard.Key;
import com.anysoftkeyboard.theme.KeyboardTheme;
import com.anysoftkeyboard.theme.KeyboardThemeFactory;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

public class AnyKeyboardBaseView extends View implements PointerTracker.UIProxy, OnSharedPreferenceChangeListener {
    static final String TAG = "ASKKbdViewBase";

    public static final int NOT_A_TOUCH_COORDINATE = -1;

    // Timing constants
    private final int mKeyRepeatInterval;

    // Miscellaneous constants
    public static final int NOT_A_KEY = -1;
    
    private static final int[] LONG_PRESSABLE_STATE_SET = { android.R.attr.state_long_pressable };
    //private static final int NUMBER_HINT_VERTICAL_ADJUSTMENT_PIXEL = -1;

    // XML attribute
    private int mKeyTextSize;
    private ColorStateList mKeyTextColor;
    private Typeface mKeyTextStyle = Typeface.DEFAULT;
    private int mLabelTextSize;
    private boolean mInLandscape = false;
    private int mSymbolColorScheme = 0;
    private int mShadowColor;
    private int mShadowRadius;
    private int mShadowOffsetX;
    private int mShadowOffsetY;
    private Drawable mKeyBackground;
    private float mBackgroundDimAmount;
    private float mKeyHysteresisDistance;
    private float mVerticalCorrection;
    private int mPreviewOffset;
    //private int mPreviewHeight;
    //private final int mPopupLayout = R.layout.keyboard_popup;

    // Main keyboard
    private AnyKeyboard mKeyboard;
    private int mMaxKeyboardWidth = 0;
    private Key[] mKeys;
    // TODO this attribute should be gotten from Keyboard.
    private int mKeyboardVerticalGap;

    // Key preview popup
    private TextView mPreviewText;
    private ImageView mPreviewIcon;
    private PopupWindow mPreviewPopup;
    private int mPreviewKeyTextSize;
	private int mPreviewLabelTextSize;
	private int mPreviewPaddingWidth = -1;
	private int mPreviewPaddingHeight = -1;
    //private int mPreviewTextSizeLarge;
    private int[] mOffsetInWindow;
    private int mOldPreviewKeyIndex = NOT_A_KEY;
    private boolean mShowPreview = true;
    private final boolean mShowTouchPoints = false;
    private int mPopupPreviewOffsetX;
    private int mPopupPreviewOffsetY;
    private int mWindowY;
//    private int mPopupPreviewDisplayedY;
    private final int mDelayBeforePreview;
    private final int mDelayAfterPreview;

    // Popup mini keyboard
    private PopupWindow mMiniKeyboardPopup;
    protected AnyKeyboardBaseView mMiniKeyboard;
    private View mMiniKeyboardParent;
    private final WeakHashMap<Key, AnyKeyboardBaseView> mMiniKeyboardCache = new WeakHashMap<Key, AnyKeyboardBaseView>();
    private int mMiniKeyboardOriginX;
    private int mMiniKeyboardOriginY;
    private long mMiniKeyboardPopupTime;
    private int[] mWindowOffset;
    private final float mMiniKeyboardSlideAllowance;
    private int mMiniKeyboardTrackerId;

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
    //private boolean mInScrollGesture = false;

    // Drawing
    /** Whether the keyboard bitmap needs to be redrawn before it's blitted. **/
    private boolean mDrawPending;
    /** The dirty region in the keyboard bitmap */
    private final Rect mDirtyRect = new Rect();
    /** The keyboard bitmap for faster updates */
    private Bitmap mBuffer;
    /** Notes if the keyboard just changed, so that we could possibly reallocate the mBuffer. */
    private boolean mKeyboardChanged;
    private Key mInvalidatedKey;
    /** The canvas for the above mutable keyboard bitmap */
    private Canvas mCanvas;
    private final Paint mPaint;
    private final Rect mPadding;
    private final Rect mClipRegion = new Rect(0, 0, 0, 0);
    // This map caches key label text height in pixel as value and key label text size as map key.
    private final HashMap<Integer, Integer> mTextHeightCache = new HashMap<Integer, Integer>();
    // Distance from horizontal center of the key, proportional to key label text height.
    private final float KEY_LABEL_VERTICAL_ADJUSTMENT_FACTOR = 0.5f;
    private final String KEY_LABEL_HEIGHT_REFERENCE_CHAR = "H";

    private final UIHandler mHandler = new UIHandler();

	private Drawable mPreviewKeyBackground;

	private int mPreviewKeyTextColor;

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
                    showKey(msg.arg1, (PointerTracker)msg.obj);
                    break;
                case MSG_DISMISS_PREVIEW:
                    mPreviewPopup.dismiss();
                    break;
                case MSG_REPEAT_KEY: {
                    final PointerTracker tracker = (PointerTracker)msg.obj;
                    tracker.repeatKey(msg.arg1);
                    startKeyRepeatTimer(mKeyRepeatInterval, msg.arg1, tracker);
                    break;
                }
                case MSG_LONGPRESS_KEY: {
                    final PointerTracker tracker = (PointerTracker)msg.obj;
                    openPopupIfRequired(msg.arg1, tracker);
                    break;
                }
            }
        }
        


        public void popupPreview(long delay, int keyIndex, PointerTracker tracker) {
            removeMessages(MSG_POPUP_PREVIEW);
            if (mPreviewPopup.isShowing() && mPreviewText.getVisibility() == VISIBLE) {
                // Show right away, if it's already visible and finger is moving around
                showKey(keyIndex, tracker);
            } else {
                sendMessageDelayed(obtainMessage(MSG_POPUP_PREVIEW, keyIndex, 0, tracker),
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

        public void startKeyRepeatTimer(long delay, int keyIndex, PointerTracker tracker) {
            mInKeyRepeat = true;
            sendMessageDelayed(obtainMessage(MSG_REPEAT_KEY, keyIndex, 0, tracker), delay);
        }

        public void cancelKeyRepeatTimer() {
            mInKeyRepeat = false;
            removeMessages(MSG_REPEAT_KEY);
        }

        public boolean isInKeyRepeat() {
            return mInKeyRepeat;
        }

        public void startLongPressTimer(long delay, int keyIndex, PointerTracker tracker) {
            removeMessages(MSG_LONGPRESS_KEY);
            sendMessageDelayed(obtainMessage(MSG_LONGPRESS_KEY, keyIndex, 0, tracker), delay);
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

        public void releaseAllPointersOlderThan(PointerTracker tracker, long eventTime) {
            LinkedList<PointerTracker> queue = mQueue;
            int oldestPos = 0;
            for (PointerTracker t = queue.get(oldestPos); t != tracker; t = queue.get(oldestPos)) {
                if (t.isModifier()) {
                    oldestPos++;
                } else {
                    t.onUpEvent(t.getLastX(), t.getLastY(), eventTime);
                    t.setAlreadyProcessed();
                    queue.remove(oldestPos);
                }
            }
        }

        public void releaseAllPointersExcept(PointerTracker tracker, long eventTime) {
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
        
        public void cancelAllTrackers()
        {
        	final long time = System.currentTimeMillis();
        	for (PointerTracker t : mQueue) {
                t.onCancelEvent(NOT_A_TOUCH_COORDINATE, NOT_A_TOUCH_COORDINATE, time);
            }
            mQueue.clear();
        }
    }

    public AnyKeyboardBaseView(Context context, AttributeSet attrs) {
        this(context, attrs, R.style.PlainLightAnySoftKeyboard);
    }

    public AnyKeyboardBaseView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mInLandscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        
        LayoutInflater inflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //int previewLayout = 0;
        mPreviewKeyTextSize = -1;
        mPreviewLabelTextSize = -1;
        mPreviewKeyBackground = null;
        mPreviewKeyTextColor = 0xFFF;
        final int[] padding = new int[] { -1,-1,-1,-1 };

        HashSet<Integer> doneStylesIndexes = new HashSet<Integer>();
        KeyboardTheme theme = KeyboardThemeFactory.getCurrentKeyboardTheme(AnySoftKeyboard.getInstance());
        final int keyboardThemeStyleResId = getKeyboardStyleResId(theme);
        Log.d(TAG, "Will use keyboard theme "+theme.getName()+" id "+theme.getId()+" res "+keyboardThemeStyleResId);
        TypedArray a = theme.getPackageContext().obtainStyledAttributes(
        		attrs, 
        		R.styleable.AnyKeyboardBaseView, 
        		0, 
        		keyboardThemeStyleResId);
        
        final int n = a.getIndexCount();
        for (int i = 0; i < n; i++) {
            final int attr = a.getIndex(i);
            doneStylesIndexes.add(new Integer(attr));
            setValueFromTheme(a, padding, attr);
        }
        a.recycle();
        //filling what's missing
        KeyboardTheme fallbackTheme = KeyboardThemeFactory.getFallbackTheme(AnySoftKeyboard.getInstance());
        final int keyboardFallbackThemeStyleResId = getKeyboardStyleResId(fallbackTheme);
        Log.d(TAG, "Will use keyboard fallback theme "+fallbackTheme.getName()+" id "+fallbackTheme.getId()+" res "+keyboardFallbackThemeStyleResId);
        a = fallbackTheme.getPackageContext().obtainStyledAttributes(
        		attrs, 
        		R.styleable.AnyKeyboardBaseView, 
        		0, 
        		keyboardFallbackThemeStyleResId);
        
        final int fallbackCount = a.getIndexCount();
        for (int i = 0; i < fallbackCount; i++) {
        	final int attr = a.getIndex(i);
            if (doneStylesIndexes.contains(new Integer(attr))) continue;
            if (AnyApplication.DEBUG) Log.d(TAG, "Falling back theme res ID "+attr);
            setValueFromTheme(a, padding, attr);
        }
        a.recycle();
        //settings
        super.setPadding(padding[0], padding[1], padding[2], padding[3]);
        
        final Resources res = getResources();

        mMaxKeyboardWidth = res.getDisplayMetrics().widthPixels - padding[0] - padding[2];
        
        mPreviewPopup = new PopupWindow(context);
        if (mPreviewKeyTextSize > 0) {
        	if (mPreviewLabelTextSize <= 0) mPreviewLabelTextSize = mPreviewKeyTextSize;
        	ViewGroup keyPreview = (ViewGroup) inflate.inflate(R.layout.key_preview, null);
            mPreviewText = (TextView) keyPreview.findViewById(R.id.key_preview_text);
            mPreviewText.setTextColor(mPreviewKeyTextColor);
            mPreviewText.setTypeface(mKeyTextStyle);
            mPreviewIcon = (ImageView) keyPreview.findViewById(R.id.key_preview_icon);
            mPreviewPopup.setBackgroundDrawable(mPreviewKeyBackground);
            mPreviewPopup.setContentView(keyPreview);
            mShowPreview = true;
        } else {
        	mPreviewText = null;
            mShowPreview = false;
        }
        mPreviewPopup.setTouchable(false);
        mPreviewPopup.setAnimationStyle(R.style.KeyPreviewAnimation);
        mDelayBeforePreview = 0;
        mDelayAfterPreview = 10;

        mMiniKeyboardParent = this;
        mMiniKeyboardPopup = new PopupWindow(context);
        mMiniKeyboardPopup.setBackgroundDrawable(null);
        mMiniKeyboardPopup.setAnimationStyle(R.style.MiniKeyboardAnimation);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(mKeyTextSize);
        mPaint.setTextAlign(Align.CENTER);
        mPaint.setAlpha(255);

        mPadding = new Rect(0, 0, 0, 0);
        mKeyBackground.getPadding(mPadding);

        reloadSwipeThresholdsSettings(res);
        
        mDisambiguateSwipe = res.getBoolean(R.bool.config_swipeDisambiguation);
        mMiniKeyboardSlideAllowance = res.getDimension(R.dimen.mini_keyboard_slide_allowance);

        AskOnGestureListener listener = new AskGestureEventsListener(this, mSwipeTracker);

        mGestureDetector = AnyApplication.getDeviceSpecific().createGestureDetector(getContext(), listener);
        mGestureDetector.setIsLongpressEnabled(false);

        //MultiTouchSupportLevel multiTouchSupportLevel = AnyApplication.getDeviceSpecific().getMultiTouchSupportLevel(getContext());
        mHasDistinctMultitouch = true;/*(multiTouchSupportLevel == MultiTouchSupportLevel.Basic) ||(multiTouchSupportLevel == MultiTouchSupportLevel.Distinct);*/
        mKeyRepeatInterval = 50;
        
        PreferenceManager.getDefaultSharedPreferences(getContext()).registerOnSharedPreferenceChangeListener(this);
    }

	public void setValueFromTheme(TypedArray a, final int[] padding,
			final int attr) {
		switch (attr) {
		case R.styleable.AnyKeyboardBaseView_android_background:
			Drawable keyboardBackground = a.getDrawable(attr);
			if (AnyApplication.DEBUG) Log.d(TAG, "AnyKeyboardBaseView_android_background "+(keyboardBackground!=null));
			super.setBackgroundDrawable(keyboardBackground);
			break;
		case R.styleable.AnyKeyboardBaseView_android_paddingLeft:
			padding[0] = a.getDimensionPixelSize(attr, 0);
			if (AnyApplication.DEBUG) Log.d(TAG, "AnyKeyboardBaseView_android_paddingLeft "+padding[0]);
			break;
		case R.styleable.AnyKeyboardBaseView_android_paddingTop:
			padding[1] = a.getDimensionPixelSize(attr, 0);
			if (AnyApplication.DEBUG) Log.d(TAG, "AnyKeyboardBaseView_android_paddingTop "+padding[1]);
			break;
		case R.styleable.AnyKeyboardBaseView_android_paddingRight:
			padding[2] = a.getDimensionPixelSize(attr, 0);
			if (AnyApplication.DEBUG) Log.d(TAG, "AnyKeyboardBaseView_android_paddingRight "+padding[2]);
			break;
		case R.styleable.AnyKeyboardBaseView_android_paddingBottom:
			padding[3] = a.getDimensionPixelSize(attr, 0);
			if (AnyApplication.DEBUG) Log.d(TAG, "AnyKeyboardBaseView_android_paddingBottom "+padding[3]);
			break;
		case R.styleable.AnyKeyboardBaseView_keyBackground:
			mKeyBackground = a.getDrawable(attr);
			if (AnyApplication.DEBUG) Log.d(TAG, "AnyKeyboardBaseView_keyBackground "+(mKeyBackground != null));
			break;
		case R.styleable.AnyKeyboardBaseView_keyHysteresisDistance:
			mKeyHysteresisDistance = a.getDimensionPixelOffset(attr, 0);
			if (AnyApplication.DEBUG) Log.d(TAG, "AnyKeyboardBaseView_keyHysteresisDistance "+mKeyHysteresisDistance);
			break;
		case R.styleable.AnyKeyboardBaseView_verticalCorrection:
			mVerticalCorrection = a.getDimensionPixelOffset(attr, 0);
			if (AnyApplication.DEBUG) Log.d(TAG, "AnyKeyboardBaseView_verticalCorrection "+mVerticalCorrection);
			break;
		case R.styleable.AnyKeyboardBaseView_keyPreviewBackground:
			mPreviewKeyBackground = a.getDrawable(attr);
			if (AnyApplication.DEBUG) Log.d(TAG, "AnyKeyboardBaseView_keyPreviewBackground "+(mPreviewKeyBackground != null));
			break;
		case R.styleable.AnyKeyboardBaseView_keyPreviewTextColor:
			mPreviewKeyTextColor = a.getColor(attr, 0xFFF);
			if (AnyApplication.DEBUG) Log.d(TAG, "AnyKeyboardBaseView_keyPreviewTextColor "+mPreviewKeyTextColor);
			break;
		case R.styleable.AnyKeyboardBaseView_keyPreviewTextSize:
			mPreviewKeyTextSize = a.getDimensionPixelSize(attr, 0);
			if (AnyApplication.DEBUG) Log.d(TAG, "AnyKeyboardBaseView_keyPreviewTextSize "+mPreviewKeyTextSize);
			break;
		case R.styleable.AnyKeyboardBaseView_keyPreviewLabelTextSize:
			mPreviewLabelTextSize = a.getDimensionPixelSize(attr, 0);
			if (AnyApplication.DEBUG) Log.d(TAG, "AnyKeyboardBaseView_keyPreviewLabelTextSize "+mPreviewLabelTextSize);
			break;
		case R.styleable.AnyKeyboardBaseView_keyPreviewOffset:
			mPreviewOffset = a.getDimensionPixelOffset(attr, 0);
			if (AnyApplication.DEBUG) Log.d(TAG, "AnyKeyboardBaseView_keyPreviewOffset "+mPreviewOffset);
			break;
		case R.styleable.AnyKeyboardBaseView_keyTextSize:
			mKeyTextSize = a.getDimensionPixelSize(attr, 18);
			if (AnyApplication.DEBUG) Log.d(TAG, "AnyKeyboardBaseView_keyTextSize "+mKeyTextSize);
			break;
		case R.styleable.AnyKeyboardBaseView_keyTextColor:
			mKeyTextColor = a.getColorStateList(attr);
			if (mKeyTextColor == null)
			{
				if (AnyApplication.DEBUG) Log.d(TAG, "Creating an empty ColorStateList");
				mKeyTextColor = new ColorStateList(new int[][]{{0}}, new int[]{a.getColor(attr, 0xFF000000)});
			}
			if (AnyApplication.DEBUG) Log.d(TAG, "AnyKeyboardBaseView_keyTextColor "+mKeyTextColor);
			break;
		case R.styleable.AnyKeyboardBaseView_labelTextSize:
			mLabelTextSize = a.getDimensionPixelSize(attr, 14);
		    if (AnyApplication.DEBUG) Log.d(TAG, "AnyKeyboardBaseView_labelTextSize "+mLabelTextSize);
			break;
//            case R.styleable.AnyKeyboardBaseView_popupLayout:
//                mPopupLayout = a.getResourceId(attr, 0);
//                break;
		case R.styleable.AnyKeyboardBaseView_shadowColor:
			mShadowColor = a.getColor(attr, 0);
			if (AnyApplication.DEBUG) Log.d(TAG, "AnyKeyboardBaseView_shadowColor "+mShadowColor);
			break;
		case R.styleable.AnyKeyboardBaseView_shadowRadius:
			mShadowRadius = a.getDimensionPixelOffset(attr, 0);
			if (AnyApplication.DEBUG) Log.d(TAG, "AnyKeyboardBaseView_shadowRadius "+mShadowRadius);
			break;
		case R.styleable.AnyKeyboardBaseView_shadowOffsetX:
			mShadowOffsetX = a.getDimensionPixelOffset(attr, 0);
			if (AnyApplication.DEBUG) Log.d(TAG, "AnyKeyboardBaseView_shadowOffsetX "+mShadowOffsetX);
			break;
		case R.styleable.AnyKeyboardBaseView_shadowOffsetY:
			mShadowOffsetY = a.getDimensionPixelOffset(attr, 0);
			if (AnyApplication.DEBUG) Log.d(TAG, "AnyKeyboardBaseView_shadowOffsetY "+mShadowOffsetY);
			break;
		case R.styleable.AnyKeyboardBaseView_backgroundDimAmount:
			mBackgroundDimAmount = a.getFloat(attr, 0.5f);
			if (AnyApplication.DEBUG) Log.d(TAG, "AnyKeyboardBaseView_backgroundDimAmount "+mBackgroundDimAmount);
			break;
		case R.styleable.AnyKeyboardBaseView_keyTextStyle:
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
			if (AnyApplication.DEBUG) Log.d(TAG, "AnyKeyboardBaseView_keyTextStyle "+mKeyTextStyle);
			break;
		case R.styleable.AnyKeyboardBaseView_symbolColorScheme:
			mSymbolColorScheme = a.getInt(attr, 0);
			if (AnyApplication.DEBUG) Log.d(TAG, "AnyKeyboardBaseView_symbolColorScheme "+mSymbolColorScheme);
			break;
		}
	}

	protected int getKeyboardStyleResId(KeyboardTheme theme) {
		return theme.getPopupThemeResId();
	}

	public int getKeyboardMaxWidth()
	{
		return mMaxKeyboardWidth;
	}
	
	private void reloadSwipeThresholdsSettings(final Resources res) {
		final float density = res.getDisplayMetrics().density;
		mSwipeVelocityThreshold = (int) (AnyApplication.getConfig().getSwipeVelocityThreshold() * density);
		mSwipeXDistanceThreshold = (int) (AnyApplication.getConfig().getSwipeDistanceThreshold() * density);
		Keyboard kbd = getKeyboard();
		if (kbd != null)
		{
			mSwipeYDistanceThreshold = (int) (mSwipeXDistanceThreshold * (((float)kbd.getHeight())/((float)getWidth())));
		}
		else
		{
			mSwipeYDistanceThreshold = 0;
		}
		if (mSwipeYDistanceThreshold == 0)
			mSwipeYDistanceThreshold = mSwipeXDistanceThreshold;
		
		mSwipeSpaceXDistanceThreshold = mSwipeXDistanceThreshold/2;
		
		mScrollXDistanceThreshold = mSwipeXDistanceThreshold / 8;
		mScrollYDistanceThreshold = mSwipeYDistanceThreshold / 8;
		if (AnyApplication.DEBUG)
		{
			Log.d(TAG, String.format("Swipe thresholds: Velocity %d, X-Distance %d, Y-Distance %d", mSwipeVelocityThreshold, mSwipeXDistanceThreshold, mSwipeYDistanceThreshold));
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
     * @return the listener attached to this keyboard
     */
    protected OnKeyboardActionListener getOnKeyboardActionListener() {
        return mKeyboardActionListener;
    }

    /**
     * Attaches a keyboard to this view. The keyboard can be switched at any time and the
     * view will re-layout itself to accommodate the keyboard.
     * @see Keyboard
     * @see #getKeyboard()
     * @param keyboard the keyboard to display in this view
     */
    public void setKeyboard(AnyKeyboard keyboard) {
        if (mKeyboard != null) {
            dismissKeyPreview();
        }
        // Remove any pending messages, except dismissing preview
        mHandler.cancelKeyTimers();
        mHandler.cancelPopupPreview();
        mKeyboard = keyboard;
        //ImeLogger.onSetKeyboard(keyboard);
        mKeys = mKeyDetector.setKeyboard(keyboard, -getPaddingLeft(),
                -getPaddingTop() + mVerticalCorrection);
        mKeyboardVerticalGap = (int)getResources().getDimension(R.dimen.key_bottom_gap);
        for (PointerTracker tracker : mPointerTrackers) {
            tracker.setKeyboard(mKeys, mKeyHysteresisDistance);
        }
        requestLayout();
        // Hint to reallocate the buffer if the size changed
        mKeyboardChanged = true;
        invalidateAllKeys();
        computeProximityThreshold(keyboard);
        mMiniKeyboardCache.clear();
        super.invalidate();
    }

    /**
     * Returns the current keyboard being displayed by this view.
     * @return the currently attached keyboard
     * @see #setKeyboard(Keyboard)
     */
    public AnyKeyboard getKeyboard() {
        return mKeyboard;
    }

    /**
     * Return whether the device has distinct multi-touch panel.
     * @return true if the device has distinct multi-touch panel.
     */
    public boolean hasDistinctMultitouch() {
        return mHasDistinctMultitouch;
    }

    /**
     * Sets the state of the shift key of the keyboard, if any.
     * @param shifted whether or not to enable the state of the shift key
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
     * @return true if the shift is in a pressed state, false otherwise. If there is
     * no shift key on the keyboard or there is no keyboard attached, it returns false.
     */
    public boolean isShifted() {
        if (mKeyboard != null) {
            return mKeyboard.isShifted();
        }
        return false;
    }
    
    /**
     * Sets the state of the control key of the keyboard, if any.
     * @param control whether or not to enable the state of the control key
     * @return true if the control key state changed, false if there was no change
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
     * Returns the state of the shift key of the keyboard, if any.
     * @return true if the shift is in a pressed state, false otherwise. If there is
     * no shift key on the keyboard or there is no keyboard attached, it returns false.
     */
    public boolean isControl() {
        if (mKeyboard != null) {
            return mKeyboard.isControl();
        }
        return false;
    }

    /**
     * Enables or disables the key feedback popup. This is a popup that shows a magnified
     * version of the depressed key. By default the preview is enabled.
     * @param previewEnabled whether or not to enable the key feedback popup
     * @see #isPreviewEnabled()
     */
    protected void setPreviewEnabled(boolean previewEnabled) {
        mShowPreview = mPreviewText != null && previewEnabled;
    }

//    /**
//     * Returns the enabled state of the key feedback popup.
//     * @return whether or not the key feedback popup is enabled
//     * @see #setPreviewEnabled(boolean)
//     */
//    public boolean isPreviewEnabled() {
//        return mShowPreview;
//    }

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
     * When enabled, calls to {@link OnKeyboardActionListener#onKey} will include key
     * codes for adjacent keys.  When disabled, only the primary key code will be
     * reported.
     * @param enabled whether or not the proximity correction is enabled
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

    private CharSequence adjustCase(CharSequence label) {
//        if (mKeyboard.isShifted() && label != null && label.length() < 3
//                && Character.isLowerCase(label.charAt(0))) {
//            label = label.toString().toUpperCase();
//        }
    	if (mKeyboard.isShifted() && (!TextUtils.isEmpty(label)) && Character.isLowerCase(label.charAt(0))) {
    		label = label.toString().toUpperCase();
        }
        return label;
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Round up a little
        if (mKeyboard == null) {
            setMeasuredDimension(
                    getPaddingLeft() + getPaddingRight(), getPaddingTop() + getPaddingBottom());
        } else {
            int width = mKeyboard.getMinWidth() + getPaddingLeft() + getPaddingRight();
            if (MeasureSpec.getSize(widthMeasureSpec) < width + 10) {
                width = MeasureSpec.getSize(widthMeasureSpec);
            }
            setMeasuredDimension(
                    width, mKeyboard.getHeight() + getPaddingTop() + getPaddingBottom());
        }
    }

    /**
     * Compute the average distance between adjacent keys (horizontally and vertically)
     * and square it to get the proximity threshold. We use a square here and in computing
     * the touch distance from a key's center to avoid taking a square root.
     * @param keyboard
     */
    private void computeProximityThreshold(Keyboard keyboard) {
        if (keyboard == null) return;
        final Key[] keys = mKeys;
        if (keys == null) return;
        int length = keys.length;
        int dimensionSum = 0;
        for (int i = 0; i < length; i++) {
            Key key = keys[i];
            dimensionSum += Math.min(key.width, key.height + mKeyboardVerticalGap) + key.gap;
        }
        if (dimensionSum < 0 || length == 0) return;
        mKeyDetector.setProximityThreshold((int) (dimensionSum * 1.4f / length));
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // Release the buffer, if any and it will be reallocated on the next draw
        mBuffer = null;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mDrawPending || mBuffer == null || mKeyboardChanged) {
            onBufferDraw();
        }
        canvas.drawBitmap(mBuffer, 0, 0, null);
    }

    private void onBufferDraw() {
        if (mBuffer == null || mKeyboardChanged) {
            if (mBuffer == null || mKeyboardChanged &&
                    (mBuffer.getWidth() != getWidth() || mBuffer.getHeight() != getHeight())) {
                // Make sure our bitmap is at least 1x1
                final int width = Math.max(1, getWidth());
                final int height = Math.max(1, getHeight());
                Log.d(TAG, "Buffer dimensions: w:"+width+" h:"+height);
                mBuffer = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                mCanvas = new Canvas(mBuffer);
                /*Drawable background = super.getBackground();
                background.setBounds(0, 0, width, height);
                background.draw(mCanvas);*/
            }
            invalidateAllKeys();
            mKeyboardChanged = false;
        }
        final Canvas canvas = mCanvas;
        canvas.clipRect(mDirtyRect, Op.REPLACE);

        if (mKeyboard == null) return;

        final Paint paint = mPaint;
        final Drawable keyBackground = mKeyBackground;
        final Rect clipRegion = mClipRegion;
        final Rect padding = mPadding;
        final int kbdPaddingLeft = getPaddingLeft();
        final int kbdPaddingTop = getPaddingTop();
        final Key[] keys = mKeys;
        final Key invalidKey = mInvalidatedKey;

        boolean drawSingleKey = false;
        if (invalidKey != null && canvas.getClipBounds(clipRegion)) {
            // TODO we should use Rect.inset and Rect.contains here.
            // Is clipRegion completely contained within the invalidated key?
            if (invalidKey.x + kbdPaddingLeft - 1 <= clipRegion.left &&
                    invalidKey.y + kbdPaddingTop - 1 <= clipRegion.top &&
                    invalidKey.x + invalidKey.width + kbdPaddingLeft + 1 >= clipRegion.right &&
                    invalidKey.y + invalidKey.height + kbdPaddingTop + 1 >= clipRegion.bottom) {
                drawSingleKey = true;
            }
        }
        canvas.drawColor(0x00000000, PorterDuff.Mode.CLEAR);
        final int keyCount = keys.length;
        for (int i = 0; i < keyCount; i++) {
            final Key key = keys[i];
    		
            if (drawSingleKey && invalidKey != key) {
                continue;
            }
            int[] drawableState = key.getCurrentDrawableState();
            
            paint.setColor(mKeyTextColor.getColorForState(drawableState, 0xFF000000));
            keyBackground.setState(drawableState);

            // Switch the character to uppercase if shift is pressed
            String label = key.label == null? null : adjustCase(key.label).toString();
            
            final Rect bounds = keyBackground.getBounds();
            if (key.width != bounds.right || key.height != bounds.bottom) {
                keyBackground.setBounds(0, 0, key.width, key.height);
            }
            canvas.translate(key.x + kbdPaddingLeft, key.y + kbdPaddingTop);
            keyBackground.draw(canvas);

            //boolean shouldDrawIcon = true;
            if (label != null) {
                // For characters, use large font. For labels like "Done", use small font.
                final int labelSize;
                if (label.length() > 1 && key.codes.length < 2) {
                    labelSize = mLabelTextSize;
                    paint.setTypeface(Typeface.DEFAULT_BOLD);
                } else {
                    labelSize = mKeyTextSize;
                    paint.setTypeface(mKeyTextStyle);
                }
                if (mInLandscape)
                	paint.setTextSize(labelSize * AnyApplication.getConfig().getKeysHeightFactorInLandscape());
                else
                	paint.setTextSize(labelSize * AnyApplication.getConfig().getKeysHeightFactorInPortrait());

                Integer labelHeightValue = mTextHeightCache.get(labelSize);
                final int labelHeight;
                if (labelHeightValue != null) {
                    labelHeight = labelHeightValue;
                } else {
                    Rect textBounds = new Rect();
                    paint.getTextBounds(KEY_LABEL_HEIGHT_REFERENCE_CHAR, 0, 1, textBounds);
                    labelHeight = textBounds.height();
                    mTextHeightCache.put(labelSize, labelHeight);
                }

                // Draw a drop shadow for the text
                paint.setShadowLayer(mShadowRadius, mShadowOffsetX, mShadowOffsetY, mShadowColor);
                
                
                //(+)This is the trick to get RTL/LTR text correct
				// no matter what: StaticLayout
                //this should be in the top left corner of the key
            	float textWidth =  paint.measureText(label);
                
                if (label.length() > 1 && !AnyApplication.getConfig().workaround_alwaysUseDrawText())
                {
                	if (AnyApplication.DEBUG) Log.d(TAG, "Using RTL fix for key draw '"+label+"'");
                	//RTL fix. But it costs, let do it when in need (more than 1 character)
	                final float centerX = padding.left + (key.width - padding.left - padding.right)/2;
	                final float centerY = padding.top + (key.height - padding.top - padding.bottom)/2;
	                
	                final float textX = centerX;
	            	final float textY = centerY - labelHeight + paint.descent();
	            	
	            	canvas.translate(textX , textY);	
	        	    
	            	TextPaint labelPaint = new TextPaint(paint);
	            	StaticLayout labelText = 
	            		new StaticLayout(
	            				label, labelPaint, 
	            				(int)textWidth, Alignment.ALIGN_NORMAL, 
	            				0.0f, 0.0f, false);
	            	labelText.draw(canvas);
	            	
	        	    canvas.translate(-textX , -textY);
                }
                else
                {
					final int centerX = (key.width + padding.left - padding.right) / 2;
					final int centerY = (key.height + padding.top - padding.bottom) / 2;
					final float baseline = centerY + labelHeight * KEY_LABEL_VERTICAL_ADJUSTMENT_FACTOR;
					canvas.drawText(label, centerX, baseline, paint);	
                }
				//(-)      
                
                                
                // Turn off drop shadow
                paint.setShadowLayer(0, 0, 0, 0);

                // Usually don't draw icon if label is not null, but we draw icon for the number
                // hint and popup hint.
                //shouldDrawIcon = shouldDrawLabelAndIcon(key);
            }
            if (key.icon != null/* && shouldDrawIcon*/) {
                // Special handing for the upper-right number hint icons
                final int drawableWidth;
                final int drawableHeight;
                final int drawableX;
                final int drawableY;
                /*if (shouldDrawIconFully(key)) {
                    drawableWidth = key.width;
                    drawableHeight = key.height;
                    drawableX = 0;
                    drawableY = NUMBER_HINT_VERTICAL_ADJUSTMENT_PIXEL;
                } else*/ {
                    drawableWidth = key.icon.getIntrinsicWidth();
                    drawableHeight = key.icon.getIntrinsicHeight();
                    drawableX = (key.width + padding.left - padding.right - drawableWidth) / 2;
                    drawableY = (key.height + padding.top - padding.bottom - drawableHeight) / 2;
                }
                canvas.translate(drawableX, drawableY);
                key.icon.setBounds(0, 0, drawableWidth, drawableHeight);
                key.icon.draw(canvas);
                canvas.translate(-drawableX, -drawableY);
            }
            canvas.translate(-key.x - kbdPaddingLeft, -key.y - kbdPaddingTop);
        }
        mInvalidatedKey = null;
        // Overlay a dark rectangle to dim the keyboard
        if (mMiniKeyboard != null) {
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
                    canvas.drawCircle((startX + lastX) / 2, (startY + lastY) / 2, 2, paint);
                }
            }
        }

        mDrawPending = false;
        mDirtyRect.setEmpty();
    }

    // TODO: clean up this method.
    void dismissKeyPreview() {
        for (PointerTracker tracker : mPointerTrackers)
            tracker.updateKey(NOT_A_KEY);
        showPreview(NOT_A_KEY, null);
    }

    public void showPreview(int keyIndex, PointerTracker tracker) {
        int oldKeyIndex = mOldPreviewKeyIndex;
        mOldPreviewKeyIndex = keyIndex;
        final boolean isLanguageSwitchEnabled = false;
        // We should re-draw popup preview when 1) we need to hide the preview, 2) we will show
        // the space key preview and 3) pointer moves off the space key to other letter key, we
        // should hide the preview of the previous key.
        final boolean hidePreviewOrShowSpaceKeyPreview = (tracker == null)
                || tracker.isSpaceKey(keyIndex) || tracker.isSpaceKey(oldKeyIndex);
        // If key changed and preview is on or the key is space (language switch is enabled)
        if (oldKeyIndex != keyIndex
                && (mShowPreview
                        || (hidePreviewOrShowSpaceKeyPreview && isLanguageSwitchEnabled))) {
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
        if (key.icon != null/* && !shouldDrawLabelAndIcon(key)*/) {
            //mPreviewText.setCompoundDrawables(null, null, null, key.iconPreview != null ? key.iconPreview : key.icon);
        	mPreviewIcon.setImageDrawable(key.iconPreview != null ? key.iconPreview : key.icon);
            mPreviewText.setText(null);
            mPreviewIcon.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            popupWidth = Math.max(mPreviewIcon.getMeasuredWidth(), key.width);
            popupHeight = Math.max(mPreviewIcon.getMeasuredHeight(), key.height);
            
        } else {
            //mPreviewText.setCompoundDrawables(null, null, null, null);
        	mPreviewIcon.setImageDrawable(null);
            mPreviewText.setText(adjustCase(tracker.getPreviewText(key, mKeyboard.isShifted())));
            if (key.label.length() > 1 && key.codes.length < 2) {
                mPreviewText.setTextSize(TypedValue.COMPLEX_UNIT_PX, mPreviewLabelTextSize);
            } else {
                mPreviewText.setTextSize(TypedValue.COMPLEX_UNIT_PX, mPreviewKeyTextSize);
            }
            
            mPreviewText.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            popupWidth = Math.max(mPreviewText.getMeasuredWidth(), key.width);
            popupHeight = Math.max(mPreviewText.getMeasuredHeight(), key.height);
        }
        
        
        if (mPreviewPaddingHeight < 0)
        {
        	mPreviewPaddingWidth = mPreviewText.getPaddingLeft() + mPreviewText.getPaddingRight();
        	mPreviewPaddingHeight = mPreviewText.getPaddingTop() + mPreviewText.getPaddingBottom();
        	
        	Drawable popupDrawable = mPreviewPopup.getBackground();
        	if (popupDrawable != null)
        	{
        		Rect padding = new Rect();
        		popupDrawable.getPadding(padding);
        		mPreviewPaddingWidth += (padding.left + padding.right);
        		mPreviewPaddingHeight += (padding.top + padding.bottom);
        	}
        }
        popupWidth += mPreviewPaddingWidth;
        popupHeight += mPreviewPaddingHeight;
        /*LayoutParams lp = mPreviewText.getLayoutParams();
        if (lp != null) {
            lp.width = popupWidth;
            lp.height = popupHeight;
        }
        mPreviewText.requestLayout();*/
        
        int popupPreviewX = key.x - ((popupWidth - key.width) / 2);
        int popupPreviewY = key.y - popupHeight - mPreviewOffset;

        mHandler.cancelDismissPreview();
        if (mOffsetInWindow == null) {
            mOffsetInWindow = new int[]{0,0};
            getLocationInWindow(mOffsetInWindow);
            if (AnyApplication.DEBUG) Log.d(TAG, "mOffsetInWindow "+mOffsetInWindow[0]+", "+mOffsetInWindow[1]);
            mOffsetInWindow[0] += mPopupPreviewOffsetX; // Offset may be zero
            mOffsetInWindow[1] += mPopupPreviewOffsetY; // Offset may be zero
            int[] windowLocation = new int[2];
            getLocationOnScreen(windowLocation);
            mWindowY = windowLocation[1];
        }
        // Set the preview background state
        Drawable keyPreviewBackground = mPreviewPopup.getBackground();
        if (keyPreviewBackground != null)
        {
        	keyPreviewBackground.setState(
                key.popupResId != 0 ? LONG_PRESSABLE_STATE_SET : EMPTY_STATE_SET);
        }
        popupPreviewX += mOffsetInWindow[0];
        popupPreviewY += mOffsetInWindow[1];
        

        // If the popup cannot be shown above the key, put it on the side
        if (popupPreviewY + mWindowY < 0) {
            // If the key you're pressing is on the left side of the keyboard, show the popup on
            // the right, offset by enough to see at least one key to the left/right.
            if (key.x + key.width <= getWidth() / 2) {
                popupPreviewX += (int) (key.width * 2.5);
            } else {
                popupPreviewX -= (int) (key.width * 2.5);
            }
            popupPreviewY += popupHeight;
        }

        if (mPreviewPopup.isShowing()) {
            mPreviewPopup.update(popupPreviewX, popupPreviewY, popupWidth, popupHeight);
        } else {
            mPreviewPopup.setWidth(popupWidth);
            mPreviewPopup.setHeight(popupHeight);
            mPreviewPopup.showAtLocation(mMiniKeyboardParent, Gravity.NO_GRAVITY,
                    popupPreviewX, popupPreviewY);
        }
        // Record popup preview position to display mini-keyboard later at the same positon
//        mPopupPreviewDisplayedY = popupPreviewY;
        mPreviewText.setVisibility(VISIBLE);
    }

    /**
     * Requests a redraw of the entire keyboard. Calling {@link #invalidate} is not sufficient
     * because the keyboard renders the keys to an off-screen buffer and an invalidate() only
     * draws the cached buffer.
     * @see #invalidateKey(Key)
     */
    public void invalidateAllKeys() {
        mDirtyRect.union(0, 0, getWidth(), getHeight());
        mDrawPending = true;
        invalidate();
    }

    /**
     * Invalidates a key so that it will be redrawn on the next repaint. Use this method if only
     * one key is changing it's content. Any changes that affect the position or size of the key
     * may not be honored.
     * @param key key in the attached {@link Keyboard}.
     * @see #invalidateAllKeys
     */
    public void invalidateKey(Key key) {
        if (key == null)
            return;
        mInvalidatedKey = key;
        // TODO we should clean up this and record key's region to use in onBufferDraw.
        mDirtyRect.union(key.x + getPaddingLeft(), key.y + getPaddingTop(),
                key.x + key.width + getPaddingLeft(), key.y + key.height + getPaddingTop());
        onBufferDraw();
        invalidate(key.x + getPaddingLeft(), key.y + getPaddingTop(),
                key.x + key.width + getPaddingLeft(), key.y + key.height + getPaddingTop());
    }

    private boolean openPopupIfRequired(int keyIndex, PointerTracker tracker) {
    	/*this is a uselss code..
        // Check if we have a popup layout specified first.
        if (mPopupLayout == 0) {
            return false;
        }
    	 */
        Key popupKey = tracker.getKey(keyIndex);
        if (popupKey == null)
            return false;
        boolean result = onLongPress(getContext(), popupKey);
        if (result) {
            dismissKeyPreview();
            mMiniKeyboardTrackerId = tracker.mPointerId;
            // Mark this tracker "already processed" and remove it from the pointer queue
            tracker.setAlreadyProcessed();
            mPointerQueue.remove(tracker);
        }
        return result;
    }

    private AnyKeyboardBaseView inflateMiniKeyboardContainer(Context packageContext, CharSequence popupCharacters, int popupKeyboardId) {
        //int popupKeyboardId = popupKey.popupResId;
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        AnyKeyboardBaseView miniKeyboard = (AnyKeyboardBaseView)inflater.inflate(R.layout.popup_keyboard_layout, null);
        if (miniKeyboard == null)
            throw new NullPointerException();

        final AnyPopupKeyboard keyboard;
        if (popupCharacters != null) {
            keyboard = new AnyPopupKeyboard(AnySoftKeyboard.getInstance(), popupCharacters, miniKeyboard.getKeyboardMaxWidth());
        } else {
            keyboard = new AnyPopupKeyboard(AnySoftKeyboard.getInstance(), packageContext, popupKeyboardId, miniKeyboard.getKeyboardMaxWidth());
        }
        
        miniKeyboard.setOnKeyboardActionListener(new OnKeyboardActionListener() {
        	
        	public void onKey(int primaryCode, int[] keyCodes, int x, int y) {
                mKeyboardActionListener.onKey(primaryCode, keyCodes, x, y);
                if (keyboard.isOneKeyEventPopup()) dismissPopupKeyboard();
            }
        	
        	public void onMultiTap() {
        		mKeyboardActionListener.onMultiTap();
        	}

            public void onText(CharSequence text) {
            	if (keyboard.isOneKeyEventPopup()) mKeyboardActionListener.onText(text);
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
            
            public void endInputConnectionEdit() {
            	mKeyboardActionListener.endInputConnectionEdit();
            }
            
            public void startInputConnectionEdit() {
            	mKeyboardActionListener.startInputConnectionEdit();
            }
        });
        // Override default ProximityKeyDetector.
        miniKeyboard.mKeyDetector = new MiniKeyboardKeyDetector(mMiniKeyboardSlideAllowance);
        // Remove gesture detector on mini-keyboard
        miniKeyboard.mGestureDetector = null;

        miniKeyboard.setKeyboard(keyboard);
        miniKeyboard.setPopupParent(this);

        miniKeyboard.measure(MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.AT_MOST),
                MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.AT_MOST));

        return miniKeyboard;
    }
/*
    private static boolean isOneRowKeys(List<Key> keys) {
        if (keys.size() == 0) return false;
        final int edgeFlags = keys.get(0).edgeFlags;
        // HACK: The first key of mini keyboard which was inflated from xml and has multiple rows,
        // does not have both top and bottom edge flags on at the same time.  On the other hand,
        // the first key of mini keyboard that was created with popupCharacters must have both top
        // and bottom edge flags on.
        // When you want to use one row mini-keyboard from xml file, make sure that the row has
        // both top and bottom edge flags set.
        return (edgeFlags & Keyboard.EDGE_TOP) != 0 && (edgeFlags & Keyboard.EDGE_BOTTOM) != 0;
    }
*/
    /**
     * Called when a key is long pressed. By default this will open any popup keyboard associated
     * with this key through the attributes popupLayout and popupCharacters.
     * @param popupKey the key that was long pressed
     * @return true if the long press is handled, false otherwise. Subclasses should call the
     * method on the base class if the subclass doesn't wish to handle the call.
     */
    protected boolean onLongPress(Context packageContext, Key popupKey) {
        // TODO if popupKey.popupCharacters has only one letter, send it as key without opening
        // mini keyboard.

        if (popupKey.popupResId == 0)
            return false;

        AnyKeyboardBaseView miniKeyboardView = mMiniKeyboardCache.get(popupKey);
        if (miniKeyboardView == null) {
        	miniKeyboardView = inflateMiniKeyboardContainer(packageContext, popupKey.popupCharacters, popupKey.popupResId);
            mMiniKeyboardCache.put(popupKey, miniKeyboardView);
        }
        mMiniKeyboard = miniKeyboardView;
        if (mWindowOffset == null) {
            mWindowOffset = new int[2];
            getLocationInWindow(mWindowOffset);
        }

        // Get width of a key in the mini popup keyboard = "miniKeyWidth".
        // On the other hand, "popupKey.width" is width of the pressed key on the main keyboard.
        // We adjust the position of mini popup keyboard with the edge key in it:
        //  a) When we have the leftmost key in popup keyboard directly above the pressed key
        //     Right edges of both keys should be aligned for consistent default selection
        //  b) When we have the rightmost key in popup keyboard directly above the pressed key
        //     Left edges of both keys should be aligned for consistent default selection
        final List<Key> miniKeys = mMiniKeyboard.getKeyboard().getKeys();
        final int miniKeyWidth = miniKeys.size() > 0 ? miniKeys.get(0).width : 0;

        // HACK: Have the leftmost number in the popup characters right above the key
//        boolean isNumberAtLeftmost =
//                hasMultiplePopupChars(popupKey) && isNumberAtLeftmostPopupChar(popupKey);
        int popupX = popupKey.x + mWindowOffset[0];
        popupX += getPaddingLeft();
        /*if (isNumberAtLeftmost) {
            popupX += popupKey.width - miniKeyWidth;  // adjustment for a) described above
            popupX -= container.getPaddingLeft();
        } else*/ {
            popupX += miniKeyWidth;  // adjustment for b) described above
            popupX -= mMiniKeyboard.getMeasuredWidth();
            popupX += mMiniKeyboard.getPaddingRight();
        }
        int popupY = popupKey.y + mWindowOffset[1];
        popupY += getPaddingTop();
        popupY -= mMiniKeyboard.getMeasuredHeight();
        //popupY += mMiniKeyboard.getPaddingBottom();
        final int x = popupX;
        //final int y = mShowPreview && isOneRowKeys(miniKeys) ? mPopupPreviewDisplayedY : popupY;
        final int y = popupY;
        
        int adjustedX = x;
        if (x < 0) {
            adjustedX = 0;
        } else if (x > (getMeasuredWidth() - mMiniKeyboard.getMeasuredWidth())) {
            adjustedX = getMeasuredWidth() - mMiniKeyboard.getMeasuredWidth();
        }
        mMiniKeyboardOriginX = adjustedX + mMiniKeyboard.getPaddingLeft() - mWindowOffset[0];
        mMiniKeyboardOriginY = y + mMiniKeyboard.getPaddingBottom() + mMiniKeyboard.getPaddingBottom()/* - mWindowOffset[1]*/;
        mMiniKeyboard.setPopupOffset(adjustedX, y);
        mMiniKeyboard.setShifted(isShifted());
        // Mini keyboard needs no pop-up key preview displayed.
        mMiniKeyboard.setPreviewEnabled(false);
        mMiniKeyboardPopup.setContentView(mMiniKeyboard);
        mMiniKeyboardPopup.setWidth(mMiniKeyboard.getMeasuredWidth());
        mMiniKeyboardPopup.setHeight(mMiniKeyboard.getMeasuredHeight());
        mMiniKeyboardPopup.showAtLocation(this, Gravity.NO_GRAVITY, x, y);

        // Inject down event on the key to mini keyboard.
        long eventTime = SystemClock.uptimeMillis();
        mMiniKeyboardPopupTime = eventTime;
        MotionEvent downEvent = generateMiniKeyboardMotionEvent(MotionEvent.ACTION_DOWN, popupKey.x
                + popupKey.width / 2, popupKey.y + popupKey.height / 2, eventTime);
        mMiniKeyboard.onTouchEvent(downEvent);
        downEvent.recycle();

        invalidateAllKeys();
        return true;
    }

//    private static boolean hasMultiplePopupChars(Key key) {
//        if (key.popupCharacters != null && key.popupCharacters.length() > 1) {
//            return true;
//        }
//        return false;
//    }

//    private boolean shouldDrawIconFully(Key key) {
//        return isNumberAtEdgeOfPopupChars(key) /*|| isLatinF1Key(key)
//                || LatinKeyboard.hasPuncOrSmileysPopup(key)*/;
//    }
//
//    private boolean shouldDrawLabelAndIcon(Key key) {
//        return isNumberAtEdgeOfPopupChars(key) /*|| isNonMicLatinF1Key(key)
//                || AnyKeyboard.hasPuncOrSmileysPopup(key)*/;
//    }

//    private boolean isLatinF1Key(Key key) {
//        return (mKeyboard instanceof AnyKeyboard) && ((AnyKeyboard)mKeyboard).isF1Key(key);
//    }

//    private boolean isNonMicLatinF1Key(Key key) {
//        return isLatinF1Key(key) && key.label != null;
//    }

//    private static boolean isNumberAtEdgeOfPopupChars(Key key) {
//        return isNumberAtLeftmostPopupChar(key) || isNumberAtRightmostPopupChar(key);
//    }

//    /* package */ static boolean isNumberAtLeftmostPopupChar(Key key) {
//        if (key.popupCharacters != null && key.popupCharacters.length() > 0
//                && isAsciiDigit(key.popupCharacters.charAt(0))) {
//            return true;
//        }
//        return false;
//    }

//    /* package */ static boolean isNumberAtRightmostPopupChar(Key key) {
//        if (key.popupCharacters != null && key.popupCharacters.length() > 0
//                && isAsciiDigit(key.popupCharacters.charAt(key.popupCharacters.length() - 1))) {
//            return true;
//        }
//        return false;
//    }
//
//    private static boolean isAsciiDigit(char c) {
//        return (c < 0x80) && Character.isDigit(c);
//    }

    private MotionEvent generateMiniKeyboardMotionEvent(int action, int x, int y, long eventTime) {
        return MotionEvent.obtain(mMiniKeyboardPopupTime, eventTime, action,
                    x - mMiniKeyboardOriginX, y - mMiniKeyboardOriginY, 0);
    }

    private PointerTracker getPointerTracker(final int id) {
        final ArrayList<PointerTracker> pointers = mPointerTrackers;
        final Key[] keys = mKeys;
        final OnKeyboardActionListener listener = mKeyboardActionListener;

        // Create pointer trackers until we can get 'id+1'-th tracker, if needed.
        for (int i = pointers.size(); i <= id; i++) {
            final PointerTracker tracker =
                new PointerTracker(i, mHandler, mKeyDetector, this, getResources());
            if (keys != null)
                tracker.setKeyboard(keys, mKeyHysteresisDistance);
            if (listener != null)
                tracker.setOnKeyboardActionListener(listener);
            pointers.add(tracker);
        }

        return pointers.get(id);
    }

    public boolean isInSlidingKeyInput() {
        if (mMiniKeyboard != null) {
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
    	final OnKeyboardActionListener ime = (OnKeyboardActionListener)getOnKeyboardActionListener();
    	
    	try
    	{
    		ime.startInputConnectionEdit();
    		WMotionEvent me = AnyApplication.getDeviceSpecific().createMotionEventWrapper(nativeMotionEvent);
    		final int action = me.getActionMasked();
    		final int pointerCount = me.getPointerCount();
	        final int oldPointerCount = mOldPointerCount;
	        mOldPointerCount = pointerCount;
    		
	        // TODO: cleanup this code into a multi-touch to single-touch event converter class?
	        // If the device does not have distinct multi-touch support panel, ignore all multi-touch
	        // events except a transition from/to single-touch.
	        if (!mHasDistinctMultitouch && pointerCount > 1 && oldPointerCount > 1) {
	            return true;
	        }
	
	        // Track the last few movements to look for spurious swipes.
	        mSwipeTracker.addMovement(me.getNativeMotionEvent());
	
	        // Gesture detector must be enabled only when mini-keyboard is not on the screen.
	        if (mMiniKeyboard == null && mGestureDetector != null && (mGestureDetector.onTouchEvent(me.getNativeMotionEvent()) /*|| mInScrollGesture*/)) {
	        	if (AnyApplication.DEBUG) Log.d(TAG, "Gesture detected!");
	        	//mHandler.cancelAllMessages();
	        	mHandler.cancelKeyTimers();
	            dismissKeyPreview();
	            return true;
	        }
	
	        final long eventTime = me.getEventTime();
	        final int index = me.getActionIndex();
	        final int id = me.getPointerId(index);
	        final int x = (int)me.getX(index);
	        final int y = (int)me.getY(index);
	
	        // Needs to be called after the gesture detector gets a turn, as it may have
	        // displayed the mini keyboard
	        if (mMiniKeyboard != null) {
	            final int miniKeyboardPointerIndex = me.findPointerIndex(mMiniKeyboardTrackerId);
	            if (miniKeyboardPointerIndex >= 0 && miniKeyboardPointerIndex < pointerCount) {
	                final int miniKeyboardX = (int)me.getX(miniKeyboardPointerIndex);
	                final int miniKeyboardY = (int)me.getY(miniKeyboardPointerIndex);
	                MotionEvent translated = generateMiniKeyboardMotionEvent(action,
	                        miniKeyboardX, miniKeyboardY, eventTime);
	                mMiniKeyboard.onTouchEvent(translated);
	                translated.recycle();
	            }
	            return true;
	        }
	
	        if (mHandler.isInKeyRepeat()) {
	            // It will keep being in the key repeating mode while the key is being pressed.
	            if (action == MotionEvent.ACTION_MOVE) {
	                return true;
	            }
	            final PointerTracker tracker = getPointerTracker(id);
	            // Key repeating timer will be canceled if 2 or more keys are in action, and current
	            // event (UP or DOWN) is non-modifier key.
	            if (pointerCount > 1 && !tracker.isModifier()) {
	                mHandler.cancelKeyRepeatTimer();
	            }
	            // Up event will pass through.
	        }
	
	        // TODO: cleanup this code into a multi-touch to single-touch event converter class?
	        // Translate mutli-touch event to single-touch events on the device that has no distinct
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
	                tracker.onUpEvent(tracker.getLastX(), tracker.getLastY(), eventTime);
	            } else if (pointerCount == 1 && oldPointerCount == 1) {
	                tracker.onTouchEvent(action, x, y, eventTime);
	            } else {
	                Log.w(TAG, "Unknown touch panel behavior: pointer count is " + pointerCount
	                        + " (old " + oldPointerCount + ")");
	            }
	            return true;
	        }
	
	        if (action == MotionEvent.ACTION_MOVE) {
	            for (int i = 0; i < pointerCount; i++) {
	                PointerTracker tracker = getPointerTracker(me.getPointerId(i));
	                tracker.onMoveEvent((int)me.getX(i), (int)me.getY(i), eventTime);
	            }
	        } else {
	            PointerTracker tracker = getPointerTracker(id);
	            sendOnXEvent(action, eventTime, x, y, tracker);
	        }
	        
	        return true;
    	}
    	finally
    	{
    		ime.endInputConnectionEdit();
    	}
    }

	protected boolean isFirstDownEventInsideSpaceBar() {
		return false;
	}

    private void sendOnXEvent(final int action, final long eventTime,
			final int x, final int y, PointerTracker tracker) {
		switch (action) {
		case MotionEvent.ACTION_DOWN:
		case 0x00000005://MotionEvent.ACTION_POINTER_DOWN:
		    onDownEvent(tracker, x, y, eventTime);
		    break;
		case MotionEvent.ACTION_UP:
		case 0x00000006://MotionEvent.ACTION_POINTER_UP:
		    onUpEvent(tracker, x, y, eventTime);
		    break;
		case MotionEvent.ACTION_CANCEL:
		    onCancelEvent(tracker, x, y, eventTime);
		    break;
		}
	}
    
    protected void onDownEvent(PointerTracker tracker, int x, int y, long eventTime) {
        if (tracker.isOnModifierKey(x, y)) {
            // Before processing a down event of modifier key, all pointers already being tracked
            // should be released.
            mPointerQueue.releaseAllPointersExcept(tracker, eventTime);
        }
        tracker.onDownEvent(x, y, eventTime);
        mPointerQueue.add(tracker);
    }

    private void onUpEvent(PointerTracker tracker, int x, int y, long eventTime) {
        if (tracker.isModifier()) {
            // Before processing an up event of modifier key, all pointers already being tracked
            // should be released.
            mPointerQueue.releaseAllPointersExcept(tracker, eventTime);
        } else {
            int index = mPointerQueue.lastIndexOf(tracker);
            if (index >= 0) {
                mPointerQueue.releaseAllPointersOlderThan(tracker, eventTime);
            } else {
                Log.w(TAG, "onUpEvent: corresponding down event not found for pointer "
                        + tracker.mPointerId);
                return;
            }
        }
        tracker.onUpEvent(x, y, eventTime);
        mPointerQueue.remove(tracker);
    }

    private void onCancelEvent(PointerTracker tracker, int x, int y, long eventTime) {
        tracker.onCancelEvent(x, y, eventTime);
        mPointerQueue.remove(tracker);
    }
    /*
    protected void swipeRight(boolean onSpacebar) {
        mKeyboardActionListener.onSwipeRight(onSpacebar);
    }

    protected void swipeLeft(boolean onSpacebar) {
        mKeyboardActionListener.onSwipeLeft(onSpacebar);
    }

    protected void swipeUp(boolean onSpacebar) {
        mKeyboardActionListener.onSwipeUp(onSpacebar);
    }

    protected void swipeDown(boolean onSpacebar) {
        mKeyboardActionListener.onSwipeDown(onSpacebar);
    }
    */
    /*
    protected void scrollGestureStarted(float dX, float dY) {
    	mInScrollGesture = true;
    }

    protected void scrollGestureEnded() {
    	mInScrollGesture = false;
    }
    */
    protected Key findKeyByKeyCode(int keyCode) {
		if (getKeyboard() == null) {
			return null;
		}
	
		for (Key key : getKeyboard().getKeys()) {
			if (key.codes[0] == keyCode) return key;
		}
		return null;
	}
    public void closing() {
        mPreviewPopup.dismiss();
        mHandler.cancelAllMessages();

        dismissPopupKeyboard();
        mBuffer = null;
        mCanvas = null;
        mMiniKeyboardCache.clear();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        PreferenceManager.getDefaultSharedPreferences(getContext()).unregisterOnSharedPreferenceChangeListener(this);
        closing();
    }
    
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    	Resources res = getResources();
		
    	if (
    			key.equals(res.getString(R.string.settings_key_swipe_distance_threshold)) ||
    			key.equals(res.getString(R.string.settings_key_swipe_velocity_threshold)))
    	{
        	reloadSwipeThresholdsSettings(res);
    	}
    	else if (
    			key.equals(res.getString(R.string.settings_key_long_press_timeout)) ||
    			key.equals(res.getString(R.string.settings_key_multitap_timeout)))
    	{
    		closing();
    		mPointerTrackers.clear();
    	}
    }

    protected boolean isPopupShowing()
    {
    	return mMiniKeyboardPopup != null && mMiniKeyboardPopup.isShowing();
    }
    
    protected void dismissPopupKeyboard() {
        if (isPopupShowing()) {
            mMiniKeyboardPopup.dismiss();
            mMiniKeyboard = null;
            mMiniKeyboardOriginX = 0;
            mMiniKeyboardOriginY = 0;
            invalidateAllKeys();
        }
    }

    public boolean handleBack() {
        if (mMiniKeyboardPopup.isShowing()) {
            dismissPopupKeyboard();
            return true;
        }
        return false;
    }
}
