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
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.Keyboard.Key;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.PopupWindow;

import com.anysoftkeyboard.AnyKeyboardContextProvider;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.ExternalAnyKeyboard;
import com.anysoftkeyboard.keyboards.GenericKeyboard;
import com.anysoftkeyboard.utils.IMEUtil;
import com.menny.android.anysoftkeyboard.R;

public class AnyKeyboardView extends AnyKeyboardBaseView {

	//private final static String TAG = "ASK AnyKeyboardView";
    public static final int KEYCODE_OPTIONS = -100;
    //static final int KEYCODE_OPTIONS_LONGPRESS = -101;
    public static final int KEYCODE_QUICK_TEXT_LONGPRESS = -102;
    
//    static final int KEYCODE_F1 = -103;
//    static final int KEYCODE_NEXT_LANGUAGE = -104;
//    static final int KEYCODE_PREV_LANGUAGE = -105;

    private Keyboard mPhoneKeyboard;

    /** Whether the extension of this keyboard is visible */
    private boolean mExtensionVisible;
    /** The view that is shown as an extension of this keyboard view */
    private AnyKeyboardBaseView mExtension;
    /** The popup window that contains the extension of this keyboard */
    private PopupWindow mExtensionPopup;
    private boolean mFirstEvent;
    
    /** Whether we've started dropping move events because we found a big jump */
    //private boolean mDroppingEvents;
    /**
     * Whether multi-touch disambiguation needs to be disabled if a real multi-touch event has
     * occured
     */
    //private boolean mDisableDisambiguation;
    /** The distance threshold at which we start treating the touch session as a multi-touch */
    //private int mJumpThresholdSquare = Integer.MAX_VALUE;
    /** The y coordinate of the last row */
    //private int mLastRowY;

    public AnyKeyboardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnyKeyboardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setPhoneKeyboard(Keyboard phoneKeyboard) {
        mPhoneKeyboard = phoneKeyboard;
    }
    
    protected String getKeyboardViewNameForLogging()
    {
    	return "AnyKeyboardView";
    }
/*
    @Override
    public void setPreviewEnabled(boolean previewEnabled) {
        if (getKeyboard() == mPhoneKeyboard) {
            // Phone keyboard never shows popup preview (except language switch).
            super.setPreviewEnabled(false);
        } else {
            super.setPreviewEnabled(previewEnabled);
        }
    }
*/
    @Override
    public void setKeyboard(Keyboard newKeyboard) {
        final Keyboard oldKeyboard = getKeyboard();
        if (oldKeyboard instanceof AnyKeyboard) {
            // Reset old keyboard state before switching to new keyboard.
            ((AnyKeyboard)oldKeyboard).keyReleased();
        }
        super.setKeyboard(newKeyboard);
        if (newKeyboard == mPhoneKeyboard) {
            // Phone keyboard never shows popup preview (except language switch).
            super.setPreviewEnabled(false);
        } else {
        	//TODO: for now! This will be replace with the config getter
            super.setPreviewEnabled(true);
        }
        //TODO: For now! should be a calculated value
        //lots of key : true
        //some keys: false
        setProximityCorrectionEnabled(true);
        // One-seventh of the keyboard width seems like a reasonable threshold
        //mJumpThresholdSquare = newKeyboard.getMinWidth() / 7;
        //mJumpThresholdSquare *= mJumpThresholdSquare;
        // Assuming there are 4 rows, this is the coordinate of the last row
        //mLastRowY = (newKeyboard.getHeight() * 3) / 4;
        //setKeyboardLocal(newKeyboard);
    }

//    @Override
//    protected boolean onLongPress(Key key) {
//        int primaryCode = key.codes[0];
//        if (primaryCode == KEYCODE_OPTIONS) {
//            return invokeOnKey(KEYCODE_OPTIONS);
//        } else if (primaryCode == '0' && getKeyboard() == mPhoneKeyboard) {
//            // Long pressing on 0 in phone number keypad gives you a '+'.
//            return invokeOnKey('+');
//        } else {
//            return super.onLongPress(key);
//        }
//    }
    
	private Key findKeyByKeyCode(int keyCode) {
		if (super.getKeyboard() == null) {
			return null;
		}
	
		for (Key key : super.getKeyboard().getKeys()) {
			if (key.codes[0] == keyCode) return key;
		}
		return null;
	}
	
	public void simulateLongPress(int keyCode) {
		Key key = findKeyByKeyCode(keyCode);
		if (key != null) super.onLongPress(key);
	}

    private boolean invokeOnKey(int primaryCode) {
        getOnKeyboardActionListener().onKey(primaryCode, null,
                AnyKeyboardBaseView.NOT_A_TOUCH_COORDINATE,
                AnyKeyboardBaseView.NOT_A_TOUCH_COORDINATE);
        return true;
    }

    @Override
    protected CharSequence adjustCase(CharSequence label) {
        Keyboard keyboard = getKeyboard();
        if (keyboard.isShifted()
                && keyboard instanceof AnyKeyboard
                && (! (keyboard instanceof GenericKeyboard))
                && !TextUtils.isEmpty(label) && label.length() < 3
                && Character.isLowerCase(label.charAt(0))) {
            label = label.toString().toUpperCase();
        }
        return label;
    }

    public boolean setShiftLocked(boolean shiftLocked) {
        Keyboard keyboard = getKeyboard();
        if (keyboard instanceof AnyKeyboard) {
            ((AnyKeyboard)keyboard).setShiftLocked(shiftLocked);
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
                tryGC = IMEUtil.GCUtils.getInstance().tryGCOrWait("AnyKeyboardView", e);
            }
        }
    }
        
    @Override
    protected boolean onLongPress(Key key)
    {
    	if (key != null && key.codes != null && key.codes.length > 0)
    	{
	    	if (key.codes[0] == 10) {
	    		invokeOnKey(KEYCODE_OPTIONS);
	    		return true;
	    	} else if (key.codes[0] == AnyKeyboard.KEYCODE_QUICK_TEXT) {
	    		invokeOnKey(KEYCODE_QUICK_TEXT_LONGPRESS);
	    		return true;
	    	}else if (key.codes[0] == AnyKeyboard.KEYCODE_LANG_CHANGE) {
	    		invokeOnKey(AnyKeyboard.KEYCODE_LANG_CHANGE);
	    		return true;
	    	} else if (key.codes[0] == '0' && getKeyboard() == mPhoneKeyboard) {
	    		// Long pressing on 0 in phone number keypad gives you a '+'.
	    		invokeOnKey('+');
	    		return true;
	    	}
    	}

    	return super.onLongPress(key);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent me) {
    	// If the motion event is above the keyboard and it's not an UP event coming
        // even before the first MOVE event into the extension area
        if (me.getY() < 0 && (mExtensionVisible || me.getAction() != MotionEvent.ACTION_UP)) {
            if (mExtensionVisible) {
                int action = me.getAction();
                if (mFirstEvent) action = MotionEvent.ACTION_DOWN;
                mFirstEvent = false;
                MotionEvent translated = MotionEvent.obtain(me.getEventTime(), me.getEventTime(),
                        action,
                        me.getX(), me.getY() + mExtension.getHeight(), me.getMetaState());
                boolean result = mExtension.onTouchEvent(translated);
                translated.recycle();
                if (me.getAction() == MotionEvent.ACTION_UP
                        || me.getAction() == MotionEvent.ACTION_CANCEL) {
                    closeExtension();
                }
                return result;
            } else {
                if (openExtension()) {
                    MotionEvent cancel = MotionEvent.obtain(me.getDownTime(), me.getEventTime(),
                            MotionEvent.ACTION_CANCEL, me.getX() - 100, me.getY() - 100, 0);
                    super.onTouchEvent(cancel);
                    cancel.recycle();
                    if (mExtension.getHeight() > 0) {
                        MotionEvent translated = MotionEvent.obtain(me.getEventTime(),
                                me.getEventTime(),
                                MotionEvent.ACTION_DOWN,
                                me.getX(), me.getY() + mExtension.getHeight(),
                                me.getMetaState());
                        mExtension.onTouchEvent(translated);
                        translated.recycle();
                    } else {
                        mFirstEvent = true;
                    }
                }
                return true;
            }
        } else if (mExtensionVisible) {
            closeExtension();
            // Send a down event into the main keyboard first
            MotionEvent down = MotionEvent.obtain(me.getEventTime(), me.getEventTime(),
                    MotionEvent.ACTION_DOWN,
                    me.getX(), me.getY(), me.getMetaState());
            super.onTouchEvent(down);
            down.recycle();
            // Send the actual event
            return super.onTouchEvent(me);
        } else {
            return super.onTouchEvent(me);
        }
    }

    private boolean openExtension() {
        // If the current keyboard is not visible, don't show the popup
        if (!isShown()) {
            return false;
        }
        if (((ExternalAnyKeyboard) getKeyboard()).getExtension() == 0) return false;
        makePopupWindow();
        mExtensionVisible = true;
        return true;
    }

    private void makePopupWindow() {
        if (mExtensionPopup == null) {
            int[] windowLocation = new int[2];
            mExtensionPopup = new PopupWindow(getContext());
            mExtensionPopup.setBackgroundDrawable(null);
            LayoutInflater li = (LayoutInflater) getContext().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            mExtension = (AnyKeyboardBaseView) li.inflate(R.layout.input_trans, null);
            mExtension.setOnKeyboardActionListener(
                    new ExtensionKeyboardListener(getOnKeyboardActionListener()));
            mExtension.setPopupParent(this);
            mExtension.setPopupOffset(0, -windowLocation[1]);
            Keyboard keyboard= new GenericKeyboard((AnyKeyboardContextProvider)getContext(),
                    ((ExternalAnyKeyboard) getKeyboard()).getExtension(), 
                    R.string.ime_name, "keyboard_ex", 0);
            
            mExtension.setKeyboard(keyboard);
            mExtensionPopup.setContentView(mExtension);
            mExtensionPopup.setWidth(getWidth());
            mExtensionPopup.setHeight(keyboard.getHeight());
            mExtensionPopup.setAnimationStyle(-1);
            getLocationInWindow(windowLocation);
            // TODO: Fix the "- 30". 
            mExtension.setPopupOffset(0, -windowLocation[1] - 30);
            mExtensionPopup.showAtLocation(this, 0, 0, -keyboard.getHeight()
                    + windowLocation[1]);
        } else {
            mExtension.setVisibility(VISIBLE);
        }
    }

    @Override
    public void closing() {
        super.closing();
        if (mExtensionPopup != null && mExtensionPopup.isShowing()) {
            mExtensionPopup.dismiss();
            mExtensionPopup = null;
        }
    }

    private void closeExtension() {
        mExtension.closing();
        mExtension.setVisibility(INVISIBLE);
        mExtensionVisible = false;
    }

    private static class ExtensionKeyboardListener implements OnKeyboardActionListener {
        private OnKeyboardActionListener mTarget;
        ExtensionKeyboardListener(OnKeyboardActionListener target) {
            mTarget = target;
        }
		public void onKey(int primaryCode, int[] keyCodes, int x, int y) {
			mTarget.onKey(primaryCode, keyCodes, x, y);
		}
        public void onPress(int primaryCode) {
            mTarget.onPress(primaryCode);
        }
        public void onRelease(int primaryCode) {
            mTarget.onRelease(primaryCode);
        }
        public void onText(CharSequence text) {
            mTarget.onText(text);
        }
        public void swipeDown() {
            // Don't pass through
        }
        public void swipeLeft() {
            // Don't pass through
        }
        public void swipeRight() {
            // Don't pass through
        }
        public void swipeUp() {
            // Don't pass through
        }
		public void onCancel() {
			// TODO Auto-generated method stub
			
		}
		public void startInputConnectionEdit() {
			// TODO Auto-generated method stub
			
		}
		public void endInputConnectionEdit() {
			// TODO Auto-generated method stub
			
		}
    }
}
