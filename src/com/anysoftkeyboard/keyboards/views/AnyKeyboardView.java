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
import android.graphics.Canvas;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.keyboardextensions.KeyboardExtension;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.AnyKeyboard.AnyKey;
import com.anysoftkeyboard.keyboards.AnyPopupKeyboard;
import com.anysoftkeyboard.keyboards.ExternalAnyKeyboard;
import com.anysoftkeyboard.keyboards.GenericKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.Keyboard.Key;
import com.anysoftkeyboard.keyboards.Keyboard.Row;
import com.anysoftkeyboard.quicktextkeys.QuickTextKey;
import com.anysoftkeyboard.theme.KeyboardTheme;
import com.anysoftkeyboard.utils.IMEUtil;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

public class AnyKeyboardView extends AnyKeyboardBaseView {

	private static final int DELAY_BEFORE_POPING_UP_EXTENSION_KBD = 35;// milliseconds
	private final static String TAG = "AnyKeyboardView";
	//public static final int KEYCODE_OPTIONS = -100;
	// static final int KEYCODE_OPTIONS_LONGPRESS = -101;
	//public static final int KEYCODE_QUICK_TEXT_LONGPRESS = -102;

	// static final int KEYCODE_F1 = -103;
	// static final int KEYCODE_NEXT_LANGUAGE = -104;
	// static final int KEYCODE_PREV_LANGUAGE = -105;

	private boolean mExtensionVisible = false;
	private final int mExtensionKeyboardPopupOffset;
	private Key mExtensionKey;
	private Key mUtilityKey;
	private Key mSpaceBarKey = null;
	private Point mFirstTouchPoint = null;
	private Boolean mCachedIsFirstDownEventInsideSpaceBar = null;

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

		mExtensionKeyboardPopupOffset = context.getResources()
				.getDimensionPixelSize(R.dimen.extension_keyboard_popup_offset);
	}

	protected String getKeyboardViewNameForLogging() {
		return "AnyKeyboardView";
	}

	/*
	 * @Override public void setPreviewEnabled(boolean previewEnabled) { if
	 * (getKeyboard() == mPhoneKeyboard) { // Phone keyboard never shows popup
	 * preview (except language switch). super.setPreviewEnabled(false); } else
	 * { super.setPreviewEnabled(previewEnabled); } }
	 */
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
				if (AnyApplication.DEBUG) Log.d(TAG, 
						String.format("Created spacebar rect x1 %d, y1 %d, width %d, height %d",
								mSpaceBarKey.x, mSpaceBarKey.y, mSpaceBarKey.width, mSpaceBarKey.height));
				break;
			}
		}
	}

	@Override
	protected int getKeyboardStyleResId(KeyboardTheme theme) {
		return theme.getThemeResId();
	}
	
	@Override
	final protected boolean isFirstDownEventInsideSpaceBar() {
		if (mCachedIsFirstDownEventInsideSpaceBar != null) return mCachedIsFirstDownEventInsideSpaceBar.booleanValue();
		mCachedIsFirstDownEventInsideSpaceBar = mSpaceBarKey != null && mFirstTouchPoint != null
				&& mSpaceBarKey.isInside(mFirstTouchPoint.x, mFirstTouchPoint.y-(mSpaceBarKey.height/4));
		
		return mCachedIsFirstDownEventInsideSpaceBar.booleanValue(); 
	}

	public void simulateLongPress(int keyCode) {
		Key key = findKeyByKeyCode(keyCode);
		if (key != null)
			super.onLongPress(getContext(), key);
	}

	private boolean invokeOnKey(int primaryCode) {
		getOnKeyboardActionListener().onKey(primaryCode, null,
				AnyKeyboardBaseView.NOT_A_TOUCH_COORDINATE,
				AnyKeyboardBaseView.NOT_A_TOUCH_COORDINATE);
		return true;
	}
	
	public boolean isShiftLocked()
	{
		AnyKeyboard keyboard = getKeyboard();
		if (keyboard != null)
		{
            return keyboard.isShiftLocked();
        }
        return false;
	}

	public boolean setShiftLocked(boolean shiftLocked) {
		AnyKeyboard keyboard = getKeyboard();
		if (keyboard != null)
		{
			keyboard.setShiftLocked(shiftLocked);
			invalidateAllKeys();
			return true;
		}
		return false;
	}

	@Override
	public void draw(Canvas c) {
		IMEUtil.GCUtils.getInstance().reset();
		boolean tryGC = true;
		for (int i = 0; i < IMEUtil.GCUtils.GC_TRY_LOOP_MAX && tryGC; ++i) {
			try {
				super.draw(c);
				tryGC = false;
			} catch (OutOfMemoryError e) {
				tryGC = IMEUtil.GCUtils.getInstance().tryGCOrWait(TAG, e);
			}
		}
	}

	@Override
	protected boolean onLongPress(Context packageContext, Key key) {
		if (key != null && key instanceof AnyKey) {
			AnyKey anyKey = (AnyKey) key;
			if (anyKey.longPressCode != 0) {
				invokeOnKey(anyKey.longPressCode);
				return true;
			}
		}

		return super.onLongPress(packageContext, key);
	}

	private long mExtensionKeyboardAreaEntranceTime = -1;

	@Override
    public boolean onTouchEvent(MotionEvent me) {
    	if (me.getAction() == MotionEvent.ACTION_DOWN)
    	{
    		mCachedIsFirstDownEventInsideSpaceBar = null;
    		mFirstTouchPoint = new Point((int)me.getX(), (int)me.getY());
    		if (AnyApplication.DEBUG) Log.d(TAG, 
					String.format("Created first down point x %d, y %d",
							mFirstTouchPoint.x, mFirstTouchPoint.y));
    		
    	}
    	// If the motion event is above the keyboard and it's not an UP event coming
        // even before the first MOVE event into the extension area
        if (me.getY() < -mExtensionKeyboardPopupOffset && !isPopupShowing() && !mExtensionVisible && me.getAction() != MotionEvent.ACTION_UP) {
        	if (mExtensionKeyboardAreaEntranceTime <= 0)
        		mExtensionKeyboardAreaEntranceTime = System.currentTimeMillis();
        	
        	if (System.currentTimeMillis() - mExtensionKeyboardAreaEntranceTime > DELAY_BEFORE_POPING_UP_EXTENSION_KBD)
        	{
        		KeyboardExtension extKbd = ((ExternalAnyKeyboard)getKeyboard()).getExtensionLayout();
	        	if (extKbd == null || extKbd.getKeyboardResId() == -1)
	        	{
	        		return super.onTouchEvent(me);
	        	}
	        	else
	        	{
	        		//telling the main keyboard that the last touch was canceled
		        	MotionEvent cancel = MotionEvent.obtain(me.getDownTime(), me.getEventTime(),
	                        MotionEvent.ACTION_CANCEL, me.getX(), me.getY(), 0);
	                super.onTouchEvent(cancel);
	                cancel.recycle();
	                
	        		mExtensionVisible = true;
		        	if (mExtensionKey == null)
		        	{
			        	mExtensionKey = new AnyKey(new Row(getKeyboard()), getThemedKeyboardDimens());
			        	mExtensionKey.codes = new int[]{0};
			        	mExtensionKey.edgeFlags = Keyboard.EDGE_TOP;
			        	mExtensionKey.height = 0;
			        	mExtensionKey.width = 0;
			        	mExtensionKey.popupResId = extKbd.getKeyboardResId();
			        	mExtensionKey.x = getWidth()/2;
			        	mExtensionKey.y = -mExtensionKeyboardPopupOffset;
		        	}
		        	onLongPress(getContext(), mExtensionKey);
		        	//it is an extension..
		        	mMiniKeyboard.setPreviewEnabled(true);
		        	Keyboard miniKeyboardObject = mMiniKeyboard.getKeyboard();
		        	if (miniKeyboardObject instanceof AnyPopupKeyboard)
		        		((AnyPopupKeyboard)miniKeyboardObject).setIsOneKeyEventPopup(!AnyApplication.getConfig().isStickyExtensionKeyboard());
		        	return true;
	        	}
        	} else {
                return super.onTouchEvent(me);
            }
        } else if (mExtensionVisible && me.getY() > mExtensionKeyboardPopupOffset) {
        	//closing the popup
        	dismissPopupKeyboard();
        	
        	return true;
        } else {
        	mExtensionKeyboardAreaEntranceTime = -1;
            return super.onTouchEvent(me);
        }
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
		super.onLongPress(packageContext, popupKey);
	}

	public void openUtilityKeyboard() {
		if (mUtilityKey == null)
    	{
			mUtilityKey = new AnyKey(new Row(getKeyboard()), getThemedKeyboardDimens());
			mUtilityKey.codes = new int[]{0};
			mUtilityKey.edgeFlags = Keyboard.EDGE_BOTTOM;
			mUtilityKey.height = 0;
			mUtilityKey.width = 0;
			mUtilityKey.popupResId = R.xml.ext_kbd_utility_utility;
			mUtilityKey.x = getWidth()/2;
			mUtilityKey.y = getHeight()/2;
    	}
    	onLongPress(getContext(), mUtilityKey);
    	mMiniKeyboard.setPreviewEnabled(true);
    	Keyboard miniKeyboardObject = mMiniKeyboard.getKeyboard();
    	if (miniKeyboardObject instanceof AnyPopupKeyboard)
    		((AnyPopupKeyboard)miniKeyboardObject).setIsOneKeyEventPopup(false);
	}
}
