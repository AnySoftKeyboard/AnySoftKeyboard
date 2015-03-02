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

package com.anysoftkeyboard.keyboards.views;

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
import android.graphics.drawable.NinePatchDrawable;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.view.MotionEventCompat;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.SparseArray;
import android.util.SparseIntArray;
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

import com.anysoftkeyboard.AskPrefs.AnimationsLevel;
import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.devicespecific.AskOnGestureListener;
import com.anysoftkeyboard.devicespecific.MultiTouchSupportLevel;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.AnyKeyboard.AnyKey;
import com.anysoftkeyboard.keyboards.AnyPopupKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.Keyboard.Key;
import com.anysoftkeyboard.keyboards.KeyboardDimens;
import com.anysoftkeyboard.keyboards.KeyboardSupport;
import com.anysoftkeyboard.keyboards.KeyboardSwitcher;
import com.anysoftkeyboard.quicktextkeys.ui.QuickTextViewFactory;
import com.anysoftkeyboard.theme.KeyboardTheme;
import com.anysoftkeyboard.theme.KeyboardThemeFactory;
import com.anysoftkeyboard.utils.IMEUtil.GCUtils;
import com.anysoftkeyboard.utils.IMEUtil.GCUtils.MemRelatedOperation;
import com.anysoftkeyboard.utils.Log;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.BuildConfig;
import com.menny.android.anysoftkeyboard.FeaturesSet;
import com.menny.android.anysoftkeyboard.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class AnyKeyboardBaseView extends View implements
        PointerTracker.UIProxy, OnSharedPreferenceChangeListener {
    static final String TAG = "ASKKbdViewBase";

    public static final int NOT_A_TOUCH_COORDINATE = -1;
    private static final int[] ACTION_KEY_TYPES = new int[]{R.attr.action_done, R.attr.action_search, R.attr.action_go};
    private static final int[] KEY_TYPES = new int[]{R.attr.key_type_function, R.attr.key_type_action};

    // Timing constants
    private final int mKeyRepeatInterval;

    // Miscellaneous constants
    public static final int NOT_A_KEY = -1;

    private static final int[] LONG_PRESSABLE_STATE_SET = {android.R.attr.state_long_pressable};

    private final KeyDrawableStateProvider mDrawableStatesProvider;

    protected KeyboardSwitcher mSwitcher;
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
    private final SparseArray<DrawableBuilder> mKeysIconBuilders = new SparseArray<DrawableBuilder>(32);
    private final SparseArray<Drawable> mKeysIcons = new SparseArray<Drawable>(32);

    private float mBackgroundDimAmount;
    private float mKeyHysteresisDistance;
    private float mVerticalCorrection;
    private int mPreviewOffset;

    // Main keyboard
    private AnyKeyboard mKeyboard;
    private String mKeyboardName;

    private Key[] mKeys;

    // Key preview popup
    private ViewGroup mPreviewLayout;
    private TextView mPreviewText;
    private ImageView mPreviewIcon;
    private PopupWindow mPreviewPopup;
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
    protected AnyKeyboardBaseView mMiniKeyboard = null;
    ;
    private boolean mMiniKeyboardVisible = false;
    private View mMiniKeyboardParent;
    private int mMiniKeyboardOriginX;
    private int mMiniKeyboardOriginY;
    private long mMiniKeyboardPopupTime;
    private int[] mWindowOffset;
    private int mMiniKeyboardTrackerId;
    protected AnimationsLevel mAnimationLevel = AnyApplication.getConfig()
            .getAnimationsLevel();

    /**
     * Listener for {@link OnKeyboardActionListener}.
     */
    protected OnKeyboardActionListener mKeyboardActionListener;
	private final MiniKeyboardActionListener mChildKeyboardActionListener = new MiniKeyboardActionListener(this);

    private final ArrayList<PointerTracker> mPointerTrackers = new ArrayList<>();

    // TODO: Let the PointerTracker class manage this pointer queue
    final PointerQueue mPointerQueue = new PointerQueue();

    private final boolean mHasDistinctMultitouch;
    private static final long TWO_FINGERS_LINGER_TIME = 30;
    private long mLastTimeHadTwoFingers = 0;
    private int mOldPointerCount = 1;

    private final KeyDetector mKeyDetector;

    // Swipe gesture detector
    private GestureDetector mGestureDetector;

    int mSwipeVelocityThreshold;
    int mSwipeXDistanceThreshold;
    int mSwipeYDistanceThreshold;
    int mSwipeSpaceXDistanceThreshold;
    int mScrollXDistanceThreshold;
    int mScrollYDistanceThreshold;

    // Drawing
    /**
     * Whether the keyboard bitmap needs to be redrawn before it's blitted. *
     */
    private boolean mDrawPending;
    /**
     * The dirty region in the keyboard bitmap
     */
    private final Rect mDirtyRect = new Rect();
    /**
     * The keyboard bitmap for faster updates
     */
    private Bitmap mBuffer;
    /**
     * Notes if the keyboard just changed, so that we could possibly reallocate
     * the mBuffer.
     */
    protected boolean mKeyboardChanged;
    private Key mInvalidatedKey;
    /**
     * The canvas for the above mutable keyboard bitmap
     */
    // private Canvas mCanvas;
    protected final Paint mPaint;
    private final Rect mKeyBackgroundPadding;
    private final Rect mClipRegion = new Rect(0, 0, 0, 0);
    /*
     * NOTE: this field EXISTS ONLY AFTER THE CTOR IS FINISHED!
     */

    private final UIHandler mHandler = new UIHandler(this);

    private Drawable mPreviewKeyBackground;

    private int mPreviewKeyTextColor;

    private final KeyboardDimensFromTheme mKeyboardDimens = new KeyboardDimensFromTheme();
    private boolean mTouchesAreDisabledTillLastFingerIsUp = false;

    public boolean areTouchesDisabled() {
        return mTouchesAreDisabledTillLastFingerIsUp;
    }

    public boolean isAtTwoFingersState() {
        //this is a hack, I know.
        //I know that this is a swipe ONLY after the second finger is up, so I already lost the
        //two-fingers count in the motion event.
        return SystemClock.elapsedRealtime() - mLastTimeHadTwoFingers < TWO_FINGERS_LINGER_TIME;
    }

    public void disableTouchesTillFingersAreUp() {
        mHandler.cancelAllMessages();
        mHandler.dismissPreview(0);
        dismissPopupKeyboard();

        for(PointerTracker tracker : mPointerTrackers) {
            Log.d(TAG, "Canceling tracker "+tracker.getKeyIndex());
            sendOnXEvent(MotionEvent.ACTION_CANCEL, 0, 0, 0, tracker);
            tracker.setAlreadyProcessed();
        }

        mTouchesAreDisabledTillLastFingerIsUp = true;
    }

    private static final class MiniKeyboardActionListener implements OnKeyboardActionListener {

	    private boolean mInOneShot;
        private final AnyKeyboardBaseView mParentKeyboard;

        public MiniKeyboardActionListener(AnyKeyboardBaseView parentKeyboard) {
            mParentKeyboard = parentKeyboard;
        }

		public void setInOneShot(boolean inOneShot) {
			mInOneShot = inOneShot;
		}

        public void onKey(int primaryCode, Key key, int multiTapIndex,int[] nearByKeyCodes, boolean fromUI) {
            mParentKeyboard.mKeyboardActionListener.onKey(primaryCode, key, multiTapIndex, nearByKeyCodes, fromUI);
            if (mInOneShot || primaryCode == KeyCodes.ENTER) mParentKeyboard.dismissPopupKeyboard();
        }

        public void onMultiTapStarted() {
            mParentKeyboard.mKeyboardActionListener.onMultiTapStarted();
        }

        public void onMultiTapEnded() {
            mParentKeyboard.mKeyboardActionListener.onMultiTapEnded();
        }

        public void onText(CharSequence text) {
            mParentKeyboard.mKeyboardActionListener.onText(text);
            if (mInOneShot) mParentKeyboard.dismissPopupKeyboard();
        }

        public void onCancel() {
            mParentKeyboard.dismissPopupKeyboard();
        }

        public void onSwipeLeft(boolean onSpaceBar, boolean twoFingers) {
        }

        public void onSwipeRight(boolean onSpaceBar, boolean twoFingers) {
        }

        public void onSwipeUp(boolean onSpaceBar) {
        }

        public void onSwipeDown(boolean onSpaceBar) {
        }

        public void onPinch() {
        }

        public void onSeparate() {
        }

        public void onPress(int primaryCode) {
            mParentKeyboard.mKeyboardActionListener.onPress(primaryCode);
        }

        public void onRelease(int primaryCode) {
            mParentKeyboard.mKeyboardActionListener.onRelease(primaryCode);
        }
    }

    static class UIHandler extends Handler {

        private final WeakReference<AnyKeyboardBaseView> mKeyboard;

        private static final int MSG_POPUP_PREVIEW = 1;
        private static final int MSG_DISMISS_PREVIEW = 2;
        private static final int MSG_REPEAT_KEY = 3;
        private static final int MSG_LONG_PRESS_KEY = 4;

        private boolean mInKeyRepeat;

        public UIHandler(AnyKeyboardBaseView keyboard) {
            mKeyboard = new WeakReference<>(keyboard);
        }

        @Override
        public void handleMessage(Message msg) {
            AnyKeyboardBaseView keyboard = mKeyboard.get();
            if (keyboard == null)
                return;
            switch (msg.what) {
                case MSG_POPUP_PREVIEW:
                    keyboard.showKey(msg.arg1, (PointerTracker) msg.obj);
                    break;
                case MSG_DISMISS_PREVIEW:
                    keyboard.mPreviewPopup.dismiss();
                    break;
                case MSG_REPEAT_KEY: {
                    final PointerTracker tracker = (PointerTracker) msg.obj;
                    tracker.repeatKey(msg.arg1);
                    startKeyRepeatTimer(keyboard.mKeyRepeatInterval, msg.arg1,
                            tracker);
                    break;
                }
                case MSG_LONG_PRESS_KEY: {
                    final PointerTracker tracker = (PointerTracker) msg.obj;
                    keyboard.openPopupIfRequired(msg.arg1, tracker);
                    break;
                }
            }
        }

        public void popupPreview(long delay, int keyIndex,
                                 PointerTracker tracker) {
            AnyKeyboardBaseView keyboard = mKeyboard.get();
            if (keyboard == null)
                return;
            removeMessages(MSG_POPUP_PREVIEW);
            if (keyboard.mPreviewPopup.isShowing()
                    && keyboard.mPreviewLayout.getVisibility() == VISIBLE) {
                // Show right away, if it's already visible and finger is moving
                // around
                keyboard.showKey(keyIndex, tracker);
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
            if (mKeyboard.get().mPreviewPopup.isShowing()) {
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
            removeMessages(MSG_LONG_PRESS_KEY);
            sendMessageDelayed(
                    obtainMessage(MSG_LONG_PRESS_KEY, keyIndex, 0, tracker),
                    delay);
        }

        public void cancelLongPressTimer() {
            removeMessages(MSG_LONG_PRESS_KEY);
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
        private LinkedList<PointerTracker> mQueue = new LinkedList<>();

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
        //creating the KeyDrawableStateProvider, as it suppose to be backward compatible
        int keyTypeFunctionAttrId = R.attr.key_type_function;
        int keyActionAttrId = R.attr.key_type_action;
        int keyActionTypeDoneAttrId = R.attr.action_done;
        int keyActionTypeSearchAttrId = R.attr.action_search;
        int keyActionTypeGoAttrId = R.attr.action_go;

        LayoutInflater inflate = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // int previewLayout = 0;
        mPreviewKeyTextSize = -1;
        mPreviewLabelTextSize = -1;
        mPreviewKeyBackground = null;
        mPreviewKeyTextColor = 0xFFF;
        final int[] padding = new int[]{0, 0, 0, 0};

        KeyboardTheme theme = KeyboardThemeFactory
                .getCurrentKeyboardTheme(context.getApplicationContext());
        final int keyboardThemeStyleResId = getKeyboardStyleResId(theme);
        Log.d(TAG, "Will use keyboard theme " + theme.getName() + " id "
                + theme.getId() + " res " + keyboardThemeStyleResId);

        //creating a mapping from the remote Attribute IDs to my local attribute ID.
        //this is required in order to backward support any build-system (which may cause the attribute IDs to change)
        final SparseIntArray attributeIdMap = new SparseIntArray(
                R.styleable.AnyKeyboardViewTheme.length + R.styleable.AnyKeyboardViewIconsTheme.length +
                        ACTION_KEY_TYPES.length + KEY_TYPES.length);

        final int[] remoteKeyboardThemeStyleable = KeyboardSupport.createBackwardCompatibleStyleable(
                        R.styleable.AnyKeyboardViewTheme, context, theme.getPackageContext(), attributeIdMap);
        final int[] remoteKeyboardIconsThemeStyleable = KeyboardSupport.createBackwardCompatibleStyleable(
                R.styleable.AnyKeyboardViewIconsTheme, context, theme.getPackageContext(), attributeIdMap);

        HashSet<Integer> doneLocalAttributeIds = new HashSet<Integer>();
        TypedArray a = theme.getPackageContext().obtainStyledAttributes(keyboardThemeStyleResId, remoteKeyboardThemeStyleable);
        final int n = a.getIndexCount();
        for (int i = 0; i < n; i++) {
            final int remoteIndex = a.getIndex(i);
            final int localAttrId = attributeIdMap.get(remoteKeyboardThemeStyleable[remoteIndex]);
            if (setValueFromTheme(a, padding, localAttrId, remoteIndex)) {
                doneLocalAttributeIds.add(localAttrId);
                if (localAttrId == R.attr.keyBackground) {
                    //keyTypeFunctionAttrId and keyActionAttrId are remote
                    final int[] keyStateAttributes = KeyboardSupport.createBackwardCompatibleStyleable(
                            KEY_TYPES, context, theme.getPackageContext(), attributeIdMap);
                    keyTypeFunctionAttrId = keyStateAttributes[0];
                    keyActionAttrId = keyStateAttributes[1];
                }
            }
        }
        a.recycle();
        // taking icons
        int iconSetStyleRes = theme.getIconsThemeResId();
        Log.d(TAG, "Will use keyboard icons theme " + theme.getName() + " id "
                + theme.getId() + " res " + iconSetStyleRes);
        if (iconSetStyleRes != 0) {
            a = theme.getPackageContext().obtainStyledAttributes(iconSetStyleRes, remoteKeyboardIconsThemeStyleable);
            final int iconsCount = a.getIndexCount();
            for (int i = 0; i < iconsCount; i++) {
                final int remoteIndex = a.getIndex(i);
                final int localAttrId = attributeIdMap.get(remoteKeyboardIconsThemeStyleable[remoteIndex]);
                if (setKeyIconValueFromTheme(theme, a, localAttrId, remoteIndex)) {
                    doneLocalAttributeIds.add(localAttrId);
                    if (localAttrId == R.attr.iconKeyAction) {
                        //keyActionTypeDoneAttrId and keyActionTypeSearchAttrId and keyActionTypeGoAttrId are remote
                        final int[] keyStateAttributes = KeyboardSupport.createBackwardCompatibleStyleable(
                                ACTION_KEY_TYPES,
                                context, theme.getPackageContext(), attributeIdMap);
                        keyActionTypeDoneAttrId = keyStateAttributes[0];
                        keyActionTypeSearchAttrId = keyStateAttributes[1];
                        keyActionTypeGoAttrId = keyStateAttributes[2];
                    }
                }
            }
            a.recycle();
        }
        // filling what's missing
        KeyboardTheme fallbackTheme = KeyboardThemeFactory.getFallbackTheme(context.getApplicationContext());
        final int keyboardFallbackThemeStyleResId = getKeyboardStyleResId(fallbackTheme);
        Log.d(TAG,
                "Will use keyboard fallback theme " + fallbackTheme.getName()
                        + " id " + fallbackTheme.getId() + " res "
                        + keyboardFallbackThemeStyleResId);
        a = fallbackTheme.getPackageContext().obtainStyledAttributes(
                keyboardFallbackThemeStyleResId,
                R.styleable.AnyKeyboardViewTheme);

        final int fallbackCount = a.getIndexCount();
        for (int i = 0; i < fallbackCount; i++) {
            final int index = a.getIndex(i);
            final int attrId = R.styleable.AnyKeyboardViewTheme[index];
            if (doneLocalAttributeIds.contains(attrId))
                continue;
            Log.d(TAG, "Falling back theme res ID " + index);
            setValueFromTheme(a, padding, attrId, index);
        }
        a.recycle();
        // taking missing icons
        int fallbackIconSetStyleId = fallbackTheme.getIconsThemeResId();
        Log.d(TAG,
                "Will use keyboard fallback icons theme "
                        + fallbackTheme.getName() + " id "
                        + fallbackTheme.getId() + " res "
                        + fallbackIconSetStyleId);
        a = fallbackTheme.getPackageContext().obtainStyledAttributes(
                fallbackIconSetStyleId,
                R.styleable.AnyKeyboardViewIconsTheme);

        final int fallbackIconsCount = a.getIndexCount();
        for (int i = 0; i < fallbackIconsCount; i++) {
            final int index = a.getIndex(i);
            final int attrId = R.styleable.AnyKeyboardViewIconsTheme[index];
            if (doneLocalAttributeIds.contains(attrId))
                continue;
            Log.d(TAG, "Falling back icon res ID " + index);
            setKeyIconValueFromTheme(fallbackTheme, a, attrId, index);
        }
        a.recycle();
        //creating the key-drawable state provider, as we suppose to have the entire data now
        mDrawableStatesProvider = new KeyDrawableStateProvider(
                keyTypeFunctionAttrId, keyActionAttrId, keyActionTypeDoneAttrId, keyActionTypeSearchAttrId, keyActionTypeGoAttrId);

        // settings.
        // don't forget that there are TWO paddings, the theme's and the
        // background image's padding!
        Drawable keyboardBackground = super.getBackground();
        if (keyboardBackground != null) {
            Rect backgroundPadding = new Rect();
            keyboardBackground.getPadding(backgroundPadding);
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
            mPreviewLayout = inflatePreviewWindowLayout(inflate);
            mPreviewText = (TextView) mPreviewLayout.findViewById(R.id.key_preview_text);
            mPreviewText.setTextColor(mPreviewKeyTextColor);
            mPreviewText.setTypeface(mKeyTextStyle);
            mPreviewIcon = (ImageView) mPreviewLayout.findViewById(R.id.key_preview_icon);
            mPreviewPopup.setBackgroundDrawable(mPreviewKeyBackground);
            mPreviewPopup.setContentView(mPreviewLayout);
            mShowPreview = mPreviewLayout != null;
        } else {
            mPreviewLayout = null;
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
        mMiniKeyboardPopup = new PopupWindow(context.getApplicationContext());
        mMiniKeyboardPopup.setBackgroundDrawable(null);

        mMiniKeyboardPopup.setAnimationStyle((mAnimationLevel == AnimationsLevel.None) ? 0 : R.style.MiniKeyboardAnimation);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(mKeyTextSize);
        mPaint.setTextAlign(Align.CENTER);
        mPaint.setAlpha(255);

        mKeyBackgroundPadding = new Rect(0, 0, 0, 0);
        mKeyBackground.getPadding(mKeyBackgroundPadding);

        reloadSwipeThresholdsSettings(res);

	    final float slide = res.getDimension(R.dimen.mini_keyboard_slide_allowance);
		mKeyDetector = createKeyDetector(slide);
        AskOnGestureListener listener = new AskGestureEventsListener(this);

        mGestureDetector = AnyApplication.getDeviceSpecific()
                .createGestureDetector(getContext(), listener);
        mGestureDetector.setIsLongpressEnabled(false);

        MultiTouchSupportLevel multiTouchSupportLevel =
                AnyApplication.getDeviceSpecific().getMultiTouchSupportLevel(getContext());

        mHasDistinctMultitouch = multiTouchSupportLevel == MultiTouchSupportLevel.Distinct;

        mKeyRepeatInterval = 50;

        AnyApplication.getConfig().addChangedListener(this);
    }

	protected KeyDetector createKeyDetector(final float slide) {
		return new MiniKeyboardKeyDetector(slide);
	}

	protected ViewGroup inflatePreviewWindowLayout(LayoutInflater inflate) {
        return (ViewGroup) inflate.inflate(R.layout.key_preview, null);
    }

    public boolean setValueFromTheme(TypedArray remoteTypedArray, final int[] padding,
                                     final int localAttrId, final int remoteTypedArrayIndex) {
        try {
            switch (localAttrId) {
                case android.R.attr.background:
                    Drawable keyboardBackground = remoteTypedArray.getDrawable(remoteTypedArrayIndex);
                    Log.d(TAG, "AnySoftKeyboardTheme_android_background "
                            + (keyboardBackground != null));
                    setBackgroundDrawable(keyboardBackground);
                    break;
                case android.R.attr.paddingLeft:
                    padding[0] = remoteTypedArray.getDimensionPixelSize(remoteTypedArrayIndex, 0);
                    Log.d(TAG, "AnySoftKeyboardTheme_android_paddingLeft "
                            + padding[0]);
                    break;
                case android.R.attr.paddingTop:
                    padding[1] = remoteTypedArray.getDimensionPixelSize(remoteTypedArrayIndex, 0);
                    Log.d(TAG, "AnySoftKeyboardTheme_android_paddingTop "
                            + padding[1]);
                    break;
                case android.R.attr.paddingRight:
                    padding[2] = remoteTypedArray.getDimensionPixelSize(remoteTypedArrayIndex, 0);
                    Log.d(TAG, "AnySoftKeyboardTheme_android_paddingRight "
                            + padding[2]);
                    break;
                case android.R.attr.paddingBottom:
                    padding[3] = remoteTypedArray.getDimensionPixelSize(remoteTypedArrayIndex, 0);
                    Log.d(TAG, "AnySoftKeyboardTheme_android_paddingBottom "
                            + padding[3]);
                    break;
                case R.attr.keyBackground:
                    mKeyBackground = remoteTypedArray.getDrawable(remoteTypedArrayIndex);
                    Log.d(TAG, "AnySoftKeyboardTheme_keyBackground "
                            + (mKeyBackground != null));
                    break;
                case R.attr.keyHysteresisDistance:
                    mKeyHysteresisDistance = remoteTypedArray.getDimensionPixelOffset(remoteTypedArrayIndex, 0);
                    Log.d(TAG, "AnySoftKeyboardTheme_keyHysteresisDistance "
                            + mKeyHysteresisDistance);
                    break;
                case R.attr.verticalCorrection:
                    mVerticalCorrection = remoteTypedArray.getDimensionPixelOffset(remoteTypedArrayIndex, 0);
                    Log.d(TAG, "AnySoftKeyboardTheme_verticalCorrection "
                            + mVerticalCorrection);
                    break;
                case R.attr.keyPreviewBackground:
                    mPreviewKeyBackground = remoteTypedArray.getDrawable(remoteTypedArrayIndex);
                    Log.d(TAG, "AnySoftKeyboardTheme_keyPreviewBackground "
                            + (mPreviewKeyBackground != null));
                    break;
                case R.attr.keyPreviewTextColor:
                    mPreviewKeyTextColor = remoteTypedArray.getColor(remoteTypedArrayIndex, 0xFFF);
                    Log.d(TAG, "AnySoftKeyboardTheme_keyPreviewTextColor "
                            + mPreviewKeyTextColor);
                    break;
                case R.attr.keyPreviewTextSize:
                    mPreviewKeyTextSize = remoteTypedArray.getDimensionPixelSize(remoteTypedArrayIndex, 0);
                    Log.d(TAG, "AnySoftKeyboardTheme_keyPreviewTextSize "
                            + mPreviewKeyTextSize);
                    break;
                case R.attr.keyPreviewLabelTextSize:
                    mPreviewLabelTextSize = remoteTypedArray.getDimensionPixelSize(remoteTypedArrayIndex, 0);
                    Log.d(TAG, "AnySoftKeyboardTheme_keyPreviewLabelTextSize "
                            + mPreviewLabelTextSize);
                    break;
                case R.attr.keyPreviewOffset:
                    mPreviewOffset = remoteTypedArray.getDimensionPixelOffset(remoteTypedArrayIndex, 0);
                    Log.d(TAG, "AnySoftKeyboardTheme_keyPreviewOffset "
                            + mPreviewOffset);
                    break;
                case R.attr.keyTextSize:
                    mKeyTextSize = remoteTypedArray.getDimensionPixelSize(remoteTypedArrayIndex, 18);
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
                    Log.d(TAG, "AnySoftKeyboardTheme_keyTextSize " + mKeyTextSize);
                    break;
                case R.attr.keyTextColor:
                    mKeyTextColor = remoteTypedArray.getColorStateList(remoteTypedArrayIndex);
                    if (mKeyTextColor == null) {
                        Log.d(TAG,
                                "Creating an empty ColorStateList for mKeyTextColor");
                        mKeyTextColor = new ColorStateList(new int[][]{{0}},
                                new int[]{remoteTypedArray.getColor(remoteTypedArrayIndex, 0xFF000000)});
                    }
                    Log.d(TAG, "AnySoftKeyboardTheme_keyTextColor " + mKeyTextColor);
                    break;
                case R.attr.labelTextSize:
                    mLabelTextSize = remoteTypedArray.getDimensionPixelSize(remoteTypedArrayIndex, 14);
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                        mLabelTextSize = mLabelTextSize
                                * AnyApplication.getConfig()
                                .getKeysHeightFactorInLandscape();
                    else
                        mLabelTextSize = mLabelTextSize
                                * AnyApplication.getConfig()
                                .getKeysHeightFactorInPortrait();
                    Log.d(TAG, "AnySoftKeyboardTheme_labelTextSize "
                            + mLabelTextSize);
                    break;
                case R.attr.keyboardNameTextSize:
                    mKeyboardNameTextSize = remoteTypedArray.getDimensionPixelSize(remoteTypedArrayIndex, 10);
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                        mKeyboardNameTextSize = mKeyboardNameTextSize
                                * AnyApplication.getConfig()
                                .getKeysHeightFactorInLandscape();
                    else
                        mKeyboardNameTextSize = mKeyboardNameTextSize
                                * AnyApplication.getConfig()
                                .getKeysHeightFactorInPortrait();
                    Log.d(TAG, "AnySoftKeyboardTheme_keyboardNameTextSize "
                            + mKeyboardNameTextSize);
                    break;
                case R.attr.keyboardNameTextColor:
                    mKeyboardNameTextColor = remoteTypedArray.getColorStateList(remoteTypedArrayIndex);
                    if (mKeyboardNameTextColor == null) {
                        Log.d(TAG,
                                "Creating an empty ColorStateList for mKeyboardNameTextColor");
                        mKeyboardNameTextColor = new ColorStateList(
                                new int[][]{{0}}, new int[]{remoteTypedArray.getColor(remoteTypedArrayIndex,
                                0xFFAAAAAA)});
                    }
                    Log.d(TAG, "AnySoftKeyboardTheme_keyboardNameTextColor "
                            + mKeyboardNameTextColor);
                    break;
                case R.attr.shadowColor:
                    mShadowColor = remoteTypedArray.getColor(remoteTypedArrayIndex, 0);
                    Log.d(TAG, "AnySoftKeyboardTheme_shadowColor " + mShadowColor);
                    break;
                case R.attr.shadowRadius:
                    mShadowRadius = remoteTypedArray.getDimensionPixelOffset(remoteTypedArrayIndex, 0);
                    Log.d(TAG, "AnySoftKeyboardTheme_shadowRadius " + mShadowRadius);
                    break;
                case R.attr.shadowOffsetX:
                    mShadowOffsetX = remoteTypedArray.getDimensionPixelOffset(remoteTypedArrayIndex, 0);
                    Log.d(TAG, "AnySoftKeyboardTheme_shadowOffsetX "
                            + mShadowOffsetX);
                    break;
                case R.attr.shadowOffsetY:
                    mShadowOffsetY = remoteTypedArray.getDimensionPixelOffset(remoteTypedArrayIndex, 0);
                    Log.d(TAG, "AnySoftKeyboardTheme_shadowOffsetY "
                            + mShadowOffsetY);
                    break;
                case R.attr.backgroundDimAmount:
                    mBackgroundDimAmount = remoteTypedArray.getFloat(remoteTypedArrayIndex, 0.5f);
                    Log.d(TAG, "AnySoftKeyboardTheme_backgroundDimAmount "
                            + mBackgroundDimAmount);
                    break;
                case R.attr.keyTextStyle:
                    int textStyle = remoteTypedArray.getInt(remoteTypedArrayIndex, 0);
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
                    Log.d(TAG, "AnySoftKeyboardTheme_keyTextStyle " + mKeyTextStyle);
                    break;
                case R.attr.symbolColorScheme:
                    mSymbolColorScheme = remoteTypedArray.getInt(remoteTypedArrayIndex, 0);
                    Log.d(TAG, "AnySoftKeyboardTheme_symbolColorScheme "
                            + mSymbolColorScheme);
                    break;
                case R.attr.keyHorizontalGap:
                    float themeHorizotalKeyGap = remoteTypedArray.getDimensionPixelOffset(remoteTypedArrayIndex, 0);
                    mKeyboardDimens.setHorizontalKeyGap(themeHorizotalKeyGap);
                    Log.d(TAG, "AnySoftKeyboardTheme_keyHorizontalGap "
                            + themeHorizotalKeyGap);
                    break;
                case R.attr.keyVerticalGap:
                    float themeVerticalRowGap = remoteTypedArray.getDimensionPixelOffset(remoteTypedArrayIndex, 0);
                    mKeyboardDimens.setVerticalRowGap(themeVerticalRowGap);
                    Log.d(TAG, "AnySoftKeyboardTheme_keyVerticalGap "
                            + themeVerticalRowGap);
                    break;
                case R.attr.keyNormalHeight:
                    float themeNormalKeyHeight = remoteTypedArray.getDimensionPixelOffset(remoteTypedArrayIndex, 0);
                    mKeyboardDimens.setNormalKeyHeight(themeNormalKeyHeight);
                    Log.d(TAG, "AnySoftKeyboardTheme_keyNormalHeight "
                            + themeNormalKeyHeight);
                    break;
                case R.attr.keyLargeHeight:
                    float themeLargeKeyHeight = remoteTypedArray.getDimensionPixelOffset(remoteTypedArrayIndex, 0);
                    mKeyboardDimens.setLargeKeyHeight(themeLargeKeyHeight);
                    Log.d(TAG, "AnySoftKeyboardTheme_keyLargeHeight "
                            + themeLargeKeyHeight);
                    break;
                case R.attr.keySmallHeight:
                    float themeSmallKeyHeight = remoteTypedArray.getDimensionPixelOffset(remoteTypedArrayIndex, 0);
                    mKeyboardDimens.setSmallKeyHeight(themeSmallKeyHeight);
                    Log.d(TAG, "AnySoftKeyboardTheme_keySmallHeight "
                            + themeSmallKeyHeight);
                    break;
                case R.attr.hintTextSize:
                    mHintTextSize = remoteTypedArray.getDimensionPixelSize(remoteTypedArrayIndex, 0);
                    Log.d(TAG, "AnySoftKeyboardTheme_hintTextSize " + mHintTextSize);
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                        mHintTextSize = mHintTextSize
                                * AnyApplication.getConfig()
                                .getKeysHeightFactorInLandscape();
                    else
                        mHintTextSize = mHintTextSize
                                * AnyApplication.getConfig()
                                .getKeysHeightFactorInPortrait();
                    Log.d(TAG, "AnySoftKeyboardTheme_hintTextSize with factor "
                            + mHintTextSize);
                    break;
                case R.attr.hintTextColor:
                    mHintTextColor = remoteTypedArray.getColorStateList(remoteTypedArrayIndex);
                    if (mHintTextColor == null) {
                        Log.d(TAG,
                                "Creating an empty ColorStateList for mHintTextColor");
                        mHintTextColor = new ColorStateList(new int[][]{{0}},
                                new int[]{remoteTypedArray.getColor(remoteTypedArrayIndex, 0xFF000000)});
                    }
                    Log.d(TAG, "AnySoftKeyboardTheme_hintTextColor "
                            + mHintTextColor);
                    break;
                case R.attr.hintLabelVAlign:
                    mHintLabelVAlign = remoteTypedArray.getInt(remoteTypedArrayIndex, Gravity.BOTTOM);
                    Log.d(TAG, "AnySoftKeyboardTheme_hintLabelVAlign "
                            + mHintLabelVAlign);
                    break;
                case R.attr.hintLabelAlign:
                    mHintLabelAlign = remoteTypedArray.getInt(remoteTypedArrayIndex, Gravity.RIGHT);
                    Log.d(TAG, "AnySoftKeyboardTheme_hintLabelAlign "
                            + mHintLabelAlign);
                    break;
                case R.attr.hintOverflowLabel:
                    mHintOverflowLabel = remoteTypedArray.getString(remoteTypedArrayIndex);
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

    private boolean setKeyIconValueFromTheme(KeyboardTheme theme, TypedArray remoteTypeArray,
                                             final int localAttrId, final int remoteTypedArrayIndex) {
        final int keyCode;
        try {
            switch (localAttrId) {
                case R.attr.iconKeyShift:
                    keyCode = KeyCodes.SHIFT;
                    break;
                case R.attr.iconKeyControl:
                    keyCode = KeyCodes.CTRL;
                    break;
                case R.attr.iconKeyAction:
                    keyCode = KeyCodes.ENTER;
                    break;
                case R.attr.iconKeyBackspace:
                    keyCode = KeyCodes.DELETE;
                    break;
                case R.attr.iconKeyCancel:
                    keyCode = KeyCodes.CANCEL;
                    break;
                case R.attr.iconKeyGlobe:
                    keyCode = KeyCodes.MODE_ALPHABET;
                    break;
                case R.attr.iconKeySpace:
                    keyCode = KeyCodes.SPACE;
                    break;
                case R.attr.iconKeyTab:
                    keyCode = KeyCodes.TAB;
                    break;
                case R.attr.iconKeyArrowDown:
                    keyCode = KeyCodes.ARROW_DOWN;
                    break;
                case R.attr.iconKeyArrowLeft:
                    keyCode = KeyCodes.ARROW_LEFT;
                    break;
                case R.attr.iconKeyArrowRight:
                    keyCode = KeyCodes.ARROW_RIGHT;
                    break;
                case R.attr.iconKeyArrowUp:
                    keyCode = KeyCodes.ARROW_UP;
                    break;
                case R.attr.iconKeyInputMoveHome:
                    keyCode = KeyCodes.MOVE_HOME;
                    break;
                case R.attr.iconKeyInputMoveEnd:
                    keyCode = KeyCodes.MOVE_END;
                    break;
                case R.attr.iconKeyMic:
                    keyCode = KeyCodes.VOICE_INPUT;
                    break;
                case R.attr.iconKeySettings:
                    keyCode = KeyCodes.SETTINGS;
                    break;
                case R.attr.iconKeyCondenseNormal:
                    keyCode = KeyCodes.MERGE_LAYOUT;
                    break;
                case R.attr.iconKeyCondenseSplit:
                    keyCode = KeyCodes.SPLIT_LAYOUT;
                    break;
                case R.attr.iconKeyCondenseCompactToRight:
                    keyCode = KeyCodes.COMPACT_LAYOUT_TO_RIGHT;
                    break;
                case R.attr.iconKeyCondenseCompactToLeft:
                    keyCode = KeyCodes.COMPACT_LAYOUT_TO_LEFT;
                    break;
                default:
                    keyCode = 0;
            }
            if (keyCode == 0) {
                if (BuildConfig.DEBUG)
                    throw new IllegalArgumentException("No valid keycode for attr "+remoteTypeArray.getResourceId(remoteTypedArrayIndex, 0));
                Log.w(TAG, "No valid keycode for attr %d", remoteTypeArray.getResourceId(remoteTypedArrayIndex, 0));
                return false;
            } else {
                mKeysIconBuilders.put(keyCode, DrawableBuilder.build(theme, remoteTypeArray, remoteTypedArrayIndex));
                Log.d(TAG, "DrawableBuilders size is %d, newest key code %d for resId %d (at index %d)", mKeysIconBuilders.size(), keyCode, remoteTypeArray.getResourceId(remoteTypedArrayIndex, 0), remoteTypedArrayIndex);
                return true;
            }
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
            mSwipeYDistanceThreshold = (int) (mSwipeXDistanceThreshold *
                    (((float) kbd.getHeight()) / ((float) getWidth())));
        } else {
            mSwipeYDistanceThreshold = 0;
        }
        if (mSwipeYDistanceThreshold == 0)
            mSwipeYDistanceThreshold = mSwipeXDistanceThreshold;

        mSwipeSpaceXDistanceThreshold = mSwipeXDistanceThreshold / 2;
        mSwipeYDistanceThreshold = mSwipeYDistanceThreshold / 2;

        mScrollXDistanceThreshold = mSwipeXDistanceThreshold / 8;
        mScrollYDistanceThreshold = mSwipeYDistanceThreshold / 8;
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
     * @param keyboard the keyboard to display in this view
     * @see Keyboard
     * @see #getKeyboard()
     */
    public final void setKeyboard(AnyKeyboard keyboard) {
        setKeyboard(keyboard, mVerticalCorrection);
    }

    public void setKeyboard(AnyKeyboard keyboard, float verticalCorrection) {
        mKeysIcons.clear();
        if (mKeyboard != null) {
            dismissKeyPreview();
        }
        // Remove any pending messages, except dismissing preview
        mHandler.cancelKeyTimers();
        mHandler.cancelPopupPreview();
        mKeyboard = keyboard;
        mKeyboardName = keyboard != null ? keyboard.getKeyboardName() : null;
        mKeys = mKeyDetector.setKeyboard(keyboard);
        mKeyDetector.setCorrection(-getPaddingLeft(), -getPaddingTop() + verticalCorrection);
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
    }

    /**
     * Returns the current keyboard being displayed by this view.
     *
     * @return the currently attached keyboard
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
     * @param control whether or not to enable the state of the control key
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
     * @param previewEnabled whether or not to enable the key feedback popup
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
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            /*setMeasuredDimension(getPaddingLeft() + getPaddingRight(),
                    getPaddingTop() + getPaddingBottom());*/
        } else {
            int width = mKeyboard.getMinWidth() + getPaddingLeft()
                    + getPaddingRight();
            if (MeasureSpec.getSize(widthMeasureSpec) < width + 10) {
                width = MeasureSpec.getSize(widthMeasureSpec);
            }
            int height = mKeyboard.getHeight() + getPaddingTop()
                    + getPaddingBottom();
            setMeasuredDimension(width, height);
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
        for (Key key : keys) {
            dimensionSum += Math.min(key.width, key.height) + key.gap;
        }
        if (dimensionSum < 0 || length == 0)
            return;
        mKeyDetector.setProximityThreshold((int) (dimensionSum * 1.4f / length));
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // Release the buffer, if any and it will be reallocated on the next
        // draw
        releaseDrawBuffer();
    }

    protected void releaseDrawBuffer() {
        if (mBuffer != null)
            mBuffer.recycle();
        mBuffer = null;
    }

    private static class KeyboardDrawOperation implements MemRelatedOperation {

        private final AnyKeyboardBaseView mView;
        private Canvas mCanvas;

        public KeyboardDrawOperation(AnyKeyboardBaseView keyboard) {
            mView = keyboard;
        }

        public void setCanvas(Canvas canvas) {
            mCanvas = canvas;
        }

        public void operation() {
            mView.onBufferDraw(mCanvas);
        }
    }

    // a single instance is enough, there is no need to recreate every draw
    // operation!
    private final KeyboardDrawOperation mDrawOperation = new KeyboardDrawOperation(this);

    @Override
    public void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
        mDrawOperation.setCanvas(canvas);

        if (mDrawPending || mBuffer == null || mKeyboardChanged) {
            GCUtils.getInstance().performOperationWithMemRetry(TAG, mDrawOperation, true);
        }
        // maybe there is no buffer, since drawing was not done.
        if (mBuffer != null)
            canvas.drawBitmap(mBuffer, 0.0f, 0.0f, null);
    }

    private void onBufferDraw(Canvas canvas) {
        if (mKeyboardChanged) {
            invalidateAllKeys();
            mKeyboardChanged = false;
        }

        canvas.getClipBounds(mDirtyRect);

        if (mKeyboard == null)
            return;

        final boolean drawKeyboardNameText = (mKeyboardNameTextSize > 1f)
                && AnyApplication.getConfig().getShowKeyboardNameText();

        final boolean drawHintText = (mHintTextSize > 1)
                && AnyApplication.getConfig().getShowHintTextOnKeys();
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
                new int[][]{{0}}, new int[]{0xFF6666FF})
                : mKeyTextColor;
        // TODO: ? AnyApplication.getConfig().getCustomKeyTextColorOnKeys() :
        // mKeyTextColor;

        final boolean useCustomHintColor = drawHintText && false;
        // TODO: final boolean useCustomHintColor = drawHintText &&
        // AnyApplication.getConfig().getUseCustomHintColorOnKeys();
        final ColorStateList hintColor = useCustomHintColor ? new ColorStateList(
                new int[][]{{0}}, new int[]{0xFFFF6666})
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
            int[] drawableState = key.getCurrentDrawableState(mDrawableStatesProvider);

            if (keyIsSpace)
                paint.setColor(mKeyboardNameTextColor.getColorForState(
                        drawableState, 0xFF000000));
            else
                paint.setColor(keyTextColor.getColorForState(drawableState,
                        0xFF000000));
            keyBackground.setState(drawableState);

            // Switch the character to uppercase if shift is pressed
            CharSequence label = key.label == null ? null : adjustCase(key).toString();

            final Rect bounds = keyBackground.getBounds();
            if ((key.width != bounds.right) || (key.height != bounds.bottom)) {
                keyBackground.setBounds(0, 0, key.width, key.height);
            }
            canvas.translate(key.x + kbdPaddingLeft, key.y + kbdPaddingTop);
            keyBackground.draw(canvas);

            if (TextUtils.isEmpty(label)) {
                Drawable iconToDraw = getIconToDrawForKey(key, false);
                if (iconToDraw != null/* && shouldDrawIcon */) {
                    //http://developer.android.com/reference/android/graphics/drawable/Drawable.html#getCurrent()
                    //http://stackoverflow.com/a/103600/1324235
                    final boolean is9Patch = iconToDraw.getCurrent() instanceof NinePatchDrawable;

                    // Special handing for the upper-right number hint icons
                    final int drawableWidth;
                    final int drawableHeight;
                    final int drawableX;
                    final int drawableY;

                    drawableWidth = is9Patch? key.width : iconToDraw.getIntrinsicWidth();
                    drawableHeight = is9Patch? key.height : iconToDraw.getIntrinsicHeight();
                    drawableX = (key.width + mKeyBackgroundPadding.left
                            - mKeyBackgroundPadding.right - drawableWidth) / 2;
                    drawableY = (key.height + mKeyBackgroundPadding.top
                            - mKeyBackgroundPadding.bottom - drawableHeight) / 2;

                    canvas.translate(drawableX, drawableY);
                    iconToDraw.setBounds(0, 0, drawableWidth, drawableHeight);
                    iconToDraw.draw(canvas);
                    canvas.translate(-drawableX, -drawableY);
                    if (keyIsSpace && drawKeyboardNameText) {
                        // now a little hack, I'll set the label now, so it get
                        // drawn.
                        label = mKeyboardName;
                    }
                } else {
                    // ho... no icon.
                    // I'll try to guess the text
                    label = guessLabelForKey(key.codes[0]);
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
                    paint.setTypeface(Typeface.DEFAULT_BOLD);
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
                    fm = setPaintToKeyText(paint);
                }

                final float labelHeight = -fm.top;
                // Draw a drop shadow for the text
                paint.setShadowLayer(mShadowRadius, mShadowOffsetX,
                        mShadowOffsetY, mShadowColor);

                // (+)This is the trick to get RTL/LTR text correct
                // no matter what: StaticLayout
                // this should be in the top left corner of the key
                float textWidth = paint.measureText(label, 0, label.length());
                // I'm going to try something if the key is too small for the
                // text:
                // 1) divide the text size by 1.5
                // 2) if still too large, divide by 2.5
                // 3) show no text
                if (textWidth > key.width) {
                    Log.d(TAG, "Label '"
                            + label
                            + "' is too large for the key. Reducing by 1.5.");
                    paint.setTextSize(mKeyTextSize / 1.5f);
                    textWidth = paint.measureText(label, 0, label.length());
                    if (textWidth > key.width) {
                        Log.d(TAG,
                                "Label '"
                                        + label
                                        + "' is too large for the key. Reducing by 2.5.");
                        paint.setTextSize(mKeyTextSize / 2.5f);
                        textWidth = paint.measureText(label, 0, label.length());
                        if (textWidth > key.width) {
                            Log.d(TAG,
                                    "Label '"
                                            + label
                                            + "' is too large for the key. Showing no text.");
                            paint.setTextSize(0f);
                            textWidth = paint.measureText(label, 0,
                                    label.length());
                        }
                    }
                }

                // the center of the drawable space, which is value used
                // previously for vertically
                // positioning the key label
                final float centerY = mKeyBackgroundPadding.top
                        + ((key.height - mKeyBackgroundPadding.top - mKeyBackgroundPadding.bottom) / (keyIsSpace ? 3
                        : 2));// the label on the space is a bit higher

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
                        && !AnyApplication.getConfig().workaround_alwaysUseDrawText()) {
                    // calculate Y coordinate of top of text based on center
                    // location
                    textY = centerY - ((labelHeight - paint.descent()) / 2);
                    canvas.translate(textX, textY);
                    Log.d(TAG, "Using RTL fix for key draw '" + label + "'");
                    // RTL fix. But it costs, let do it when in need (more than
                    // 1 character)
                    StaticLayout labelText = new StaticLayout(label,
                            new TextPaint(paint), (int) textWidth,
                            Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
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

        if (FeaturesSet.DEBUG_LOG) {
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

    protected FontMetrics setPaintToKeyText(final Paint paint) {
        final FontMetrics fm;
        paint.setTextSize(mKeyTextSize);
        paint.setTypeface(mKeyTextStyle);
        if (mTextFM == null)
            mTextFM = paint.getFontMetrics();
        fm = mTextFM;
        return fm;
    }

    protected static boolean isSpaceKey(final AnyKey key) {
        return key.codes.length > 0 && key.codes[0] == KeyCodes.SPACE;
    }

    int mKeyboardActionType = EditorInfo.IME_ACTION_UNSPECIFIED;

    public void setKeyboardActionType(final int imeOptions) {
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

    private void setSpecialKeysIconsAndLabels() {
        Key enterKey = findKeyByKeyCode(KeyCodes.ENTER);
        if (enterKey != null) {
            enterKey.icon = null;
            enterKey.iconPreview = null;
            enterKey.label = null;
            ((AnyKey) enterKey).shiftedKeyLabel = null;
            Drawable icon = getIconToDrawForKey(enterKey, false);
            if (icon != null) {
                enterKey.icon = icon;
                enterKey.iconPreview = icon;
            } else {
                CharSequence label = guessLabelForKey(enterKey.codes[0]);
                enterKey.label = label;
                ((AnyKey) enterKey).shiftedKeyLabel = label;
            }
            // making sure something is shown
            if (enterKey.icon == null && TextUtils.isEmpty(enterKey.label)) {
                Log.i(TAG, "Wow. Unknown ACTION ID " + mKeyboardActionType
                        + ". Will default to ENTER icon.");
                // I saw devices (Galaxy Tab 10") which say the action
                // type is 255...
                // D/ASKKbdViewBase( 3594): setKeyboardActionType
                // imeOptions:33554687 action:255
                // which means it is not a known ACTION
                Drawable enterIcon = getIconForKeyCode(KeyCodes.ENTER);
                enterIcon.setState(mDrawableStatesProvider.DRAWABLE_STATE_ACTION_NORMAL);
                enterKey.icon = enterIcon;
                enterKey.iconPreview = enterIcon;
            }
        }
        //these are dynamic keys
        setSpecialKeyIconOrLabel(KeyCodes.MODE_ALPHABET);
        setSpecialKeyIconOrLabel(KeyCodes.MODE_SYMOBLS);
        setSpecialKeyIconOrLabel(KeyCodes.KEYBOARD_MODE_CHANGE);
    }

    private void setSpecialKeyIconOrLabel(int keyCode) {
        Key key = findKeyByKeyCode(keyCode);
        if (key != null) {
            if (TextUtils.isEmpty(key.label)) {
                if (key.dynamicEmblem == Keyboard.KEY_EMBLEM_TEXT) {
                    key.label = guessLabelForKey(key.codes[0]);
                } else {
                    key.icon = getIconForKeyCode(keyCode);
                }
            }
        }
    }

    private CharSequence guessLabelForKey(int keyCode) {
        switch (keyCode) {
            case KeyCodes.ENTER:
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
            case KeyCodes.KEYBOARD_MODE_CHANGE:
                if (mSwitcher.isAlphabetMode())
                    return guessLabelForKey(KeyCodes.MODE_SYMOBLS);
                else
                    return guessLabelForKey(KeyCodes.MODE_ALPHABET);
            case KeyCodes.MODE_ALPHABET:
                String langKeyText = null;
                if (mSwitcher != null)//should show the next keyboard label, not a generic one.
                    langKeyText = mSwitcher.peekNextAlphabetKeyboard();
                if (langKeyText == null)
                    return getResources().getString(R.string.change_lang_regular);
                else
                    return langKeyText;
            case KeyCodes.MODE_SYMOBLS:
                String symKeyText = null;
                if (mSwitcher != null)//should show the next keyboard label, not a generic one.
                    symKeyText = mSwitcher.peekNextSymbolsKeyboard();
                if (symKeyText == null)
                    return getResources().getString(R.string.change_symbols_regular);
                else
                    return symKeyText;
            case KeyCodes.TAB:
                return getContext().getText(R.string.label_tab_key);
            case KeyCodes.MOVE_HOME:
                return getContext().getText(R.string.label_home_key);
            case KeyCodes.MOVE_END:
                return getContext().getText(R.string.label_end_key);
            case KeyCodes.ARROW_DOWN:
                return "\u2193";
            case KeyCodes.ARROW_LEFT:
                return "\u2190";
            case KeyCodes.ARROW_RIGHT:
                return "\u2192";
            case KeyCodes.ARROW_UP:
                return "\u2191";
            default:
                return null;
        }
    }

    private Drawable getIconToDrawForKey(Key key, boolean feedback) {
        if (key.dynamicEmblem == Keyboard.KEY_EMBLEM_TEXT)
            return null;
        
        if (feedback && key.iconPreview != null)
            return key.iconPreview;
        if (key.icon != null)
            return key.icon;

        return getIconForKeyCode(key.codes[0]);
    }

    private Drawable getIconForKeyCode(int keyCode) {
        Drawable icon = mKeysIcons.get(keyCode);

        if (icon == null) {
            // building needed icon
            Log.d(TAG, "Building icon for key-code %d", keyCode);
            DrawableBuilder builder = mKeysIconBuilders.get(keyCode);
            if (builder == null)
                return null;
            icon = builder.buildDrawable();
            mKeysIcons.put(keyCode, icon);
            Log.d(TAG, "Current drawable cache size is %d", mKeysIcons.size());
        }
        // maybe a drawable state is required
        if (icon != null) {
            switch (keyCode) {
                case KeyCodes.ENTER:
                    Log.d(TAG, "Action key action ID is %d", mKeyboardActionType);
                    switch (mKeyboardActionType) {
                        case EditorInfo.IME_ACTION_DONE:
                            icon.setState(mDrawableStatesProvider.DRAWABLE_STATE_ACTION_DONE);
                            break;
                        case EditorInfo.IME_ACTION_GO:
                            icon.setState(mDrawableStatesProvider.DRAWABLE_STATE_ACTION_GO);
                            break;
                        case EditorInfo.IME_ACTION_SEARCH:
                            icon.setState(mDrawableStatesProvider.DRAWABLE_STATE_ACTION_SEARCH);
                            break;
                        case EditorInfo.IME_ACTION_NONE:
                        case EditorInfo.IME_ACTION_UNSPECIFIED:
                            icon.setState(mDrawableStatesProvider.DRAWABLE_STATE_ACTION_NORMAL);
                            break;
                    }
                    break;
                case KeyCodes.SHIFT:
                    if (mKeyboard.isShiftLocked())
                        icon.setState(mDrawableStatesProvider.DRAWABLE_STATE_MODIFIER_LOCKED);
                    else if (mKeyboard.isShifted())
                        icon.setState(mDrawableStatesProvider.DRAWABLE_STATE_MODIFIER_PRESSED);
                    else
                        icon.setState(mDrawableStatesProvider.DRAWABLE_STATE_MODIFIER_NORMAL);
                    break;
                case KeyCodes.CTRL:
                    if (mKeyboard.isControl())
                        icon.setState(mDrawableStatesProvider.DRAWABLE_STATE_MODIFIER_PRESSED);
                    else
                        icon.setState(mDrawableStatesProvider.DRAWABLE_STATE_MODIFIER_NORMAL);
                    break;
            }
        }
        return icon;
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
        final boolean hidePreviewOrShowSpaceKeyPreview = (tracker == null);
        // If key changed and preview is on or the key is space (language switch
        // is enabled)
        if (oldKeyIndex != keyIndex
                && (mShowPreview || (hidePreviewOrShowSpaceKeyPreview && isLanguageSwitchEnabled))) {
            final Key key = hidePreviewOrShowSpaceKeyPreview? null : tracker.getKey(keyIndex);
            //this will ensure that in case the key is marked as NO preview, we will just dismiss the previous popup.
            if (keyIndex == NOT_A_KEY || key == null || !key.showPreview) {
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
        Drawable iconToDraw = getIconToDrawForKey(key, true);
        if (iconToDraw != null) {
            // Here's an annoying bug for you (explanation at the end of the
            // hack)
            mPreviewIcon.setImageState(iconToDraw.getState(), false);
            // end of hack. You see, the drawable comes with a state, this state
            // is overridden by the ImageView. No more.
            mPreviewIcon.setImageDrawable(iconToDraw);
            mPreviewIcon.measure(
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            popupWidth = Math.max(mPreviewIcon.getMeasuredWidth(), key.width);
            popupHeight = Math.max(mPreviewIcon.getMeasuredHeight(), key.height);
            mPreviewText.setText(null);
        } else {
            CharSequence label = tracker.getPreviewText(key, mKeyboard.isShifted());
            if (TextUtils.isEmpty(label)) {
                label = guessLabelForKey(key.codes[0]);
            }
            mPreviewIcon.setImageDrawable(null);
            mPreviewText.setTextColor(mPreviewKeyTextColor);
            setKeyPreviewText(key, label);
            popupWidth = Math.max(mPreviewText.getMeasuredWidth(), key.width);
            popupHeight = Math.max(mPreviewText.getMeasuredHeight(), key.height);
        }

        if (mPreviewPaddingHeight < 0) {
            mPreviewPaddingWidth = mPreviewLayout.getPaddingLeft()
                    + mPreviewLayout.getPaddingRight();
            mPreviewPaddingHeight = mPreviewLayout.getPaddingTop()
                    + mPreviewLayout.getPaddingBottom();

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
            mOffsetInWindow = new int[]{0, 0};
            getLocationInWindow(mOffsetInWindow);
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
            mPreviewPopup.update(popupPreviewX, popupPreviewY, popupWidth,
                    popupHeight);
        } else {
            mPreviewPopup.setWidth(popupWidth);
            mPreviewPopup.setHeight(popupHeight);
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
        // Record pop-up preview position to display mini-keyboard later at the
        // same position
        mPopupPreviewDisplayedY = popupPreviewY
                + (showPopupAboveKey ? 0 : key.y);// the popup keyboard should
        // be
        // placed at the right
        // position.
        // So I'm fixing
        mPreviewLayout.setVisibility(VISIBLE);

        // Set the preview background state
        if (mPreviewKeyBackground != null) {
            mPreviewKeyBackground
                    .setState(key.popupResId != 0 ? LONG_PRESSABLE_STATE_SET
                            : EMPTY_STATE_SET);
        }

        // LayoutParams lp = mPreviewLayout.getLayoutParams();
        // lp.width = popupWidth;
        mPreviewLayout.requestLayout();
        mPreviewLayout.invalidate();
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
     * @param key key in the attached {@link Keyboard}.
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
        boolean result = onLongPress(getKeyboard().getKeyboardContext(), popupKey, false, true);
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

    private AnyPopupKeyboard setupMiniKeyboardContainer(Context packageContext, Key popupKey, boolean isSticky) {
        final AnyPopupKeyboard keyboard;
        if (popupKey.popupCharacters != null) {
            keyboard = new AnyPopupKeyboard(getContext()
                    .getApplicationContext(), popupKey.popupCharacters,
                    mMiniKeyboard.getThemedKeyboardDimens(), null);
        } else {
            keyboard = new AnyPopupKeyboard(getContext().getApplicationContext(),
                    popupKey.externalResourcePopupLayout ? packageContext : getContext().getApplicationContext(),
                    popupKey.popupResId,
                    mMiniKeyboard.getThemedKeyboardDimens(), null);
        }
	    mChildKeyboardActionListener.setInOneShot(!isSticky);

        if (isSticky)
            mMiniKeyboard.setKeyboard(keyboard, mVerticalCorrection);
        else
            mMiniKeyboard.setKeyboard(keyboard);

        mMiniKeyboard.measure(
                MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.AT_MOST),
                MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.AT_MOST));

        return keyboard;
    }

    public KeyboardDimens getThemedKeyboardDimens() {
        return mKeyboardDimens;
    }

    /**
     * Called when a key is long pressed. By default this will open any popup
     * keyboard associated with this key through the attributes popupLayout and
     * popupCharacters.
     *
     * @param popupKey the key that was long pressed
     * @return true if the long press is handled, false otherwise. Subclasses
     *         should call the method on the base class if the subclass doesn't
     *         wish to handle the call.
     */
    protected boolean onLongPress(Context packageContext, Key popupKey,
                                  boolean isSticky, boolean requireSlideInto) {
        if (popupKey.popupResId == 0) return false;

	    ensureMiniKeyboardInitialized();

        AnyPopupKeyboard popupKeyboard = setupMiniKeyboardContainer(packageContext, popupKey, isSticky);
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
        boolean shouldMirrorKeys = false;
        //now we need to see the the popup is positioned correctly:
        //1) if the right edge is off the screen, then we'll try to put the right edge over the popup key
        if (adjustedX > (getMeasuredWidth() - mMiniKeyboard.getMeasuredWidth())) {
            adjustedX = popupKey.x + mWindowOffset[0] - mMiniKeyboard.getMeasuredWidth();
            //adding the width of the key - now the right most popup key is above the finger
            adjustedX += popupKey.width;
            adjustedX += mMiniKeyboard.getPaddingRight();
            shouldMirrorKeys = true;
        }
        //2) if it is still negative, then let's put it at the beginning (shouldn't happen)
        if (adjustedX < 0) {
            adjustedX = 0;
            shouldMirrorKeys = false;
        }
        if (shouldMirrorKeys)
            popupKeyboard.mirrorKeys();

        mMiniKeyboardOriginX =
                adjustedX + mMiniKeyboard.getPaddingLeft() - mWindowOffset[0];
        mMiniKeyboardOriginY =
                y + mMiniKeyboard.getPaddingTop() - mWindowOffset[1];

        //I'm not sure I need to do this, but in any case - this is to sync the popup window
        //to align to the mini-keyboard position
        mMiniKeyboard.setPopupOffset(adjustedX, y);
        // NOTE:I'm checking the main keyboard shift state directly!
        // Not anything else.
        mMiniKeyboard.setShifted(mKeyboard != null && mKeyboard.isShifted());
        // Mini keyboard needs no pop-up key preview displayed.
        mMiniKeyboard.setPreviewEnabled(false);
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

        setPopupKeyboardWithView(adjustedX, y, mMiniKeyboard);
        return true;
    }

	public void showQuickKeysView(Key popupKey) {
		ensureMiniKeyboardInitialized();
		View innerView = QuickTextViewFactory.createQuickTextView(getContext(), mChildKeyboardActionListener);
		innerView.setBackgroundDrawable(mMiniKeyboard.getBackground());

		innerView.measure(
				View.MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.EXACTLY),
				View.MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.AT_MOST));

		mChildKeyboardActionListener.setInOneShot(false);

        if (mWindowOffset == null) {
            mWindowOffset = new int[2];
            getLocationInWindow(mWindowOffset);
        }

        int popupY = popupKey.y + mWindowOffset[1];
	    popupY += popupKey.height;//this is shown at the bottom of the key
        popupY += getPaddingTop();
        popupY -= innerView.getMeasuredHeight();
        popupY -= innerView.getPaddingBottom();

        mMiniKeyboardOriginX = mWindowOffset[0];
        mMiniKeyboardOriginY = popupY - mWindowOffset[1];

        setPopupKeyboardWithView(0, popupY, innerView);
    }

    private void setPopupKeyboardWithView(int x, int y, View contentView) {
        mMiniKeyboardVisible = true;

        mMiniKeyboardPopup.setContentView(contentView);
        mMiniKeyboardPopup.setWidth(contentView.getMeasuredWidth());
        mMiniKeyboardPopup.setHeight(contentView.getMeasuredHeight());
        mMiniKeyboardPopup.showAtLocation(this, Gravity.NO_GRAVITY, x, y);

        invalidateAllKeys();
    }

    public void ensureMiniKeyboardInitialized() {
	    if (mMiniKeyboard != null) return;

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMiniKeyboard = (AnyKeyboardBaseView) inflater.inflate(R.layout.popup_keyboard_layout, null);

        mMiniKeyboard.setPopupParent(this);
        // hack: this will ensure that the key of a popup is no wider than a
        // thumb's width.
        ((KeyboardDimensFromTheme) mMiniKeyboard.getThemedKeyboardDimens()).setKeyMaxWidth(mMiniKeyboard.getThemedKeyboardDimens().getNormalKeyHeight());

        mMiniKeyboard.setOnKeyboardActionListener(mChildKeyboardActionListener);
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

    private MotionEvent generateMiniKeyboardMotionEvent(int action, int x,
                                                        int y, long eventTime) {
        return MotionEvent.obtain(mMiniKeyboardPopupTime, eventTime, action, x
                - mMiniKeyboardOriginX, y - mMiniKeyboardOriginY, 0);
    }

    protected PointerTracker getPointerTracker(final int id) {
        final ArrayList<PointerTracker> pointers = mPointerTrackers;
        final Key[] keys = mKeys;
        final OnKeyboardActionListener listener = mKeyboardActionListener;

        // Create pointer trackers until we can get 'id+1'-th tracker, if
        // needed.
        for (int i = pointers.size(); i <= id; i++) {
            final PointerTracker tracker = new PointerTracker(i, mHandler, mKeyDetector, this, getResources());
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

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent nativeMotionEvent) {
        if (mKeyboard == null)//I mean, if there isn't any keyboard I'm handling, what's the point?
            return false;
        final int action = MotionEventCompat.getActionMasked(nativeMotionEvent);
        final int pointerCount = MotionEventCompat.getPointerCount(nativeMotionEvent);
        final int oldPointerCount = mOldPointerCount;
        mOldPointerCount = pointerCount;
        if (pointerCount > 1)
            mLastTimeHadTwoFingers = SystemClock.elapsedRealtime();//marking the time. Read isAtTwoFingersState()

        if (mTouchesAreDisabledTillLastFingerIsUp) {
            if ( mOldPointerCount == 1 &&
                (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP)) {
                mTouchesAreDisabledTillLastFingerIsUp = false;
            }

            return true;
        }
        // TODO: cleanup this code into a multi-touch to single-touch event
        // converter class?
        // If the device does not have distinct multi-touch support panel,
        // ignore all multi-touch
        // events except a transition from/to single-touch.
        if (!mHasDistinctMultitouch && pointerCount > 1 && oldPointerCount > 1) {
            return true;
        }

        // Gesture detector must be enabled only when mini-keyboard is not
        // on the screen.
        if (!mMiniKeyboardVisible && mGestureDetector != null
                && (mGestureDetector.onTouchEvent(nativeMotionEvent))) {
            Log.d(TAG, "Gesture detected!");
            mHandler.cancelKeyTimers();
            dismissKeyPreview();
            return true;
        }

        final long eventTime = nativeMotionEvent.getEventTime();
        final int index = MotionEventCompat.getActionIndex(nativeMotionEvent);
        final int id = nativeMotionEvent.getPointerId(index);
        final int x = (int) nativeMotionEvent.getX(index);
        final int y = (int) nativeMotionEvent.getY(index);

        // Needs to be called after the gesture detector gets a turn, as it
        // may have
        // displayed the mini keyboard
        if (mMiniKeyboard != null && mMiniKeyboardVisible) {
            final int miniKeyboardPointerIndex = nativeMotionEvent.findPointerIndex(mMiniKeyboardTrackerId);
            if (miniKeyboardPointerIndex >= 0
                    && miniKeyboardPointerIndex < pointerCount) {
                final int miniKeyboardX = (int) nativeMotionEvent.getX(miniKeyboardPointerIndex);
                final int miniKeyboardY = (int) nativeMotionEvent.getY(miniKeyboardPointerIndex);
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
                PointerTracker tracker = getPointerTracker(nativeMotionEvent.getPointerId(i));
                tracker.onMoveEvent((int) nativeMotionEvent.getX(i), (int) nativeMotionEvent.getY(i),
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

    protected void onUpEvent(PointerTracker tracker, int x, int y,
                             long eventTime) {
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
                Log.w(TAG, "onUpEvent: corresponding down event not found for pointer " + tracker.mPointerId);
                return;
            }
        }
        tracker.onUpEvent(x, y, eventTime);
        mPointerQueue.remove(tracker);
    }

    protected void onCancelEvent(PointerTracker tracker, int x, int y,
                                 long eventTime) {
        tracker.onCancelEvent(x, y, eventTime);
        mPointerQueue.remove(tracker);
    }

    protected Key findKeyByKeyCode(int keyCode) {
        if (getKeyboard() == null) {
            return null;
        }

        for (Key key : getKeyboard().getKeys()) {
            if (key.codes != null && key.codes.length > 0
                    && key.codes[0] == keyCode)
                return key;
        }
        return null;
    }

    public boolean closing() {
        mPreviewPopup.dismiss();
        mHandler.cancelAllMessages();

        if (!dismissPopupKeyboard()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // releasing some memory
        for (int i = 0; i < mKeysIcons.size(); i++) {
            Drawable d = mKeysIcons.valueAt(i);
            unbindDrawable(d);
        }
        mKeysIcons.clear();
    }

    private static void unbindDrawable(Drawable d) {
        if (d != null)
            d.setCallback(null);
    }

    public void onViewNotRequired() {
        AnyApplication.getConfig().removeChangedListener(this);
        // cleaning up memory
        unbindDrawable(mPreviewPopup.getBackground());
        unbindDrawable(getBackground());
        for (int i = 0; i < mKeysIcons.size(); i++) {
            Drawable d = mKeysIcons.valueAt(i);
            unbindDrawable(d);
        }
        mKeysIcons.clear();
        mKeysIconBuilders.clear();
        unbindDrawable(mPreviewKeyBackground);
        unbindDrawable(mKeyBackground);
        mMiniKeyboardParent = null;
        if (mMiniKeyboard != null)
            mMiniKeyboard.onViewNotRequired();
        mMiniKeyboard = null;

        mKeyboardActionListener = null;
        mGestureDetector = null;
        mKeyboard = null;

        mSwitcher = null;

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
        mPreviewPopup.setAnimationStyle((mAnimationLevel == AnimationsLevel.None) ? 0 : R.style.KeyPreviewAnimation);
        mMiniKeyboardPopup.setAnimationStyle((mAnimationLevel == AnimationsLevel.None) ? 0 : R.style.MiniKeyboardAnimation);

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
}
