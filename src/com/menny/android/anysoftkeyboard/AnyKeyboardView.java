/*
 * Copyright (C) 2010 AnySoftKeyoard.
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
//
//package com.menny.android.anysoftkeyboard;
//
//import android.content.Context;
//import android.inputmethodservice.Keyboard;
//import android.inputmethodservice.KeyboardView;
//import android.inputmethodservice.Keyboard.Key;
//import android.os.IBinder;
//import android.util.AttributeSet;
//import android.util.Log;
//import android.view.Gravity;
//import android.view.LayoutInflater;
//import android.view.MotionEvent;
//import android.view.View;
//import android.widget.PopupWindow;
//
//import com.menny.android.anysoftkeyboard.keyboards.AnyKeyboard;
//
//public class AnyKeyboardView extends KeyboardView {
//	private PopupWindow mQuickTextPopupKeyboard;
//	private View mMiniKeyboardContainer;
//	private KeyboardView mMiniKeyboard;
//	private boolean mQuickTextKeyboardOnScreen;
//	private int[] mWindowOffset;
//
//	public interface OnAnyKeyboardActionListener extends OnKeyboardActionListener
//	{
//		void startInputConnectionEdit();
//		void endInputConnectionEdit();
//	}
//	private final static String TAG = "ASK AnyKeyboardView";
//	static final int KEYCODE_OPTIONS = -100;
//    //static final int KEYCODE_SHIFT_LONGPRESS = -101;
//	static final int KEYCODE_QUICK_TEXT_LONGPRESS = -102;
//    
//    private Keyboard mPhoneKeyboard;
//
//    public AnyKeyboardView(Context context, AttributeSet attrs) {
//        super(context, attrs);
//
//		mQuickTextPopupKeyboard = new PopupWindow(context);
//        mQuickTextPopupKeyboard.setBackgroundDrawable(null);
//
//        initializeStuff();
//    }
//
//	private void initializeStuff() {
//		setPreviewEnabled(AnyApplication.getConfig().getShowKeyPreview());
//		setProximityCorrectionEnabled(true);
//	}
//
//    public AnyKeyboardView(Context context, AttributeSet attrs, int defStyle) {
//        super(context, attrs, defStyle);
//        initializeStuff();
//    }
//    
//    //private final Object mTouchLock = new Object();
//    @Override
//    public boolean onTouchEvent(MotionEvent me) {
//    	//synchronized (mTouchLock) {
//    		final OnAnyKeyboardActionListener ime = (OnAnyKeyboardActionListener)getOnKeyboardActionListener();
//    		try
//    		{
//    			ime.startInputConnectionEdit();
//				if (mQuickTextKeyboardOnScreen) {
//					//Don't deliver anything to the superclass if quick text popup is active
//					if (me.getAction() == MotionEvent.ACTION_CANCEL) {
//						dismissQuickTextPopupKeyboard();
//					}
//					return true;
//				}
//
//    			return super.onTouchEvent(me);
//    		}
//    		catch(ArrayIndexOutOfBoundsException ex)
//    		{
//    			//due to an Android bug (see KeyboardView class functions 'getKeyIndices' - usage of arrayCopy
//    			//and 'detectAndSendKey' usage of mTapCount) 
//    			Log.w(TAG, "Got ArrayIndexOutOfBoundsException, and ignoring.");
//    			ex.printStackTrace();
//    			return true;
//    		}
//    		finally
//    		{
//    			ime.endInputConnectionEdit();
//    		}
////		}
//    }
////    
////    @Override
////    public boolean onKeyDown(int keyCode, KeyEvent event) {
////    	Log.d(TAG, "onKeyDown:"+keyCode);
////    	return super.onKeyDown(keyCode, event);
////    }
////    
////    @Override
////    public boolean onKeyUp(int keyCode, KeyEvent event) {
////    	Log.d(TAG, "onKeyUp:"+keyCode);
////    	return super.onKeyUp(keyCode, event);
////    }
//    
//    public void setPhoneKeyboard(Keyboard phoneKeyboard) {
//        mPhoneKeyboard = phoneKeyboard;
//    }
//    
//    @Override
//    protected boolean onLongPress(Key key) {
//        if (key.codes[0] == 10) {
//            getOnKeyboardActionListener().onKey(KEYCODE_OPTIONS, null);
//            return true;
//        } else if (key.codes[0] == AnyKeyboard.KEYCODE_QUICK_TEXT) {
//            getOnKeyboardActionListener().onKey(KEYCODE_QUICK_TEXT_LONGPRESS, null);
//            return true;
////        } else if (key.codes[0] == Keyboard.KEYCODE_SHIFT) {
////          getOnKeyboardActionListener().onKey(AnyKeyboard.KEYCODE_LANG_CHANGE, null);
////          return true;
//        }else if (key.codes[0] == AnyKeyboard.KEYCODE_LANG_CHANGE) {
//            getOnKeyboardActionListener().onKey(AnyKeyboard.KEYCODE_LANG_CHANGE, null);
//            return true;
//        } else if (key.codes[0] == '0' && getKeyboard() == mPhoneKeyboard) {
//            // Long pressing on 0 in phone number keypad gives you a '+'.
//            getOnKeyboardActionListener().onKey('+', null);
//            return true;
//        } else {
//            return super.onLongPress(key);
//        }
//    }
//    
//    public void simulateLongPress(int keyCode) {
//		Key key = findKeyByKeyCode(keyCode);
//		if (key != null) super.onLongPress(key);
//    }
//
//	private Key findKeyByKeyCode(int keyCode) {
//		if (super.getKeyboard() == null) {
//			return null;
//		}
//
//		for (Key key : super.getKeyboard().getKeys()) {
//			if (key.codes[0] == keyCode) return key;
//		}
//		return null;
//	}
//    
//    @Override
//    public void setKeyboard(Keyboard keyboard) {
//    	super.setKeyboard(keyboard);
//    	setProximityCorrectionEnabled(((AnyKeyboard)keyboard).requiresProximityCorrection());
//
//		mMiniKeyboardContainer = null;
//		mMiniKeyboard = null;
//    }
//
//    protected void requestSpecialKeysRedraw()
//    {
//    	super.invalidate();
//    }
//    
////    @Override
////    public boolean setShifted(boolean shifted) {
////    	final boolean res = super.setShifted(shifted);
////    	if (isShown())
////    		requestShiftKeyRedraw();
////    	
////    	return res;
////    }
//    
//    public void requestShiftKeyRedraw()
//    {
//    	
//    	if (canInteractWithUi())
//    		super.invalidate();
//    }
//
//	protected boolean canInteractWithUi() {
//		IBinder ib = getWindowToken();
//		return (ib != null && 
//				ib.isBinderAlive() &&//has not been disposed 
//				(this.getWidth() > 0));//the GUI has already been computed
//	}
//
//	/* Why make this so complex? Because if the popup keyboard layout came from another
//	 package, KeyboardView won't be able to find it. This is an almost complete substitution based
//	 on Android 2.3 code. The only problem I detected right now is that the main keyboard
//	 won't be dimmed when this keyboard is up. Unfortunately, I haven't found a way to fix this.
//	 Also motion events are not handled by KeyboardView if popup is present. */
//	public void showQuickTextPopupKeyboard(Context packageContext) {
//		Key popupKey = findKeyByKeyCode(AnyKeyboard.KEYCODE_QUICK_TEXT);
//		if (packageContext == getContext()) {
//			super.onLongPress(popupKey);
//		} else {
//			int popupKeyboardId = popupKey.popupResId;
//			if (mMiniKeyboardContainer == null) {
//				LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
//						Context.LAYOUT_INFLATER_SERVICE);
//				mMiniKeyboardContainer = inflater.inflate(R.layout.keyboard_popup_keyboard, null);
//				mMiniKeyboard = (KeyboardView) mMiniKeyboardContainer.findViewById(
//						android.R.id.keyboardView);
//				View closeButton = mMiniKeyboardContainer.findViewById(
//						android.R.id.closeButton);
//				if (closeButton != null) {
//					closeButton.setOnClickListener(this);
//				}
//				mMiniKeyboard.setOnKeyboardActionListener(new OnKeyboardActionListener() {
//
//					public void onKey(int primaryCode, int[] keyCodes) {
//						getOnKeyboardActionListener().onKey(primaryCode, keyCodes);
//						dismissQuickTextPopupKeyboard();
//					}
//
//					public void onText(CharSequence text) {
//						getOnKeyboardActionListener().onText(text);
//						dismissQuickTextPopupKeyboard();
//					}
//
//					public void swipeLeft() {}
//					public void swipeRight() {}
//					public void swipeUp() {}
//					public void swipeDown() {}
//
//					public void onPress(int primaryCode) {
//						getOnKeyboardActionListener().onPress(primaryCode);
//					}
//
//					public void onRelease(int primaryCode) {
//						getOnKeyboardActionListener().onRelease(primaryCode);
//					}
//				});
//				Keyboard keyboard;
//				if (popupKey.popupCharacters != null) { //TODO: is this branch used?
//					keyboard = new Keyboard(packageContext, popupKeyboardId,
//							popupKey.popupCharacters, -1, getPaddingLeft() + getPaddingRight());
//				} else {
//					keyboard = new Keyboard(packageContext, popupKeyboardId);
//				}
//				mMiniKeyboard.setKeyboard(keyboard);
//				mMiniKeyboard.setPopupParent(this);
//				mMiniKeyboardContainer.measure(
//						MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.AT_MOST),
//						MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.AT_MOST));
//			} else {
//				mMiniKeyboard = (KeyboardView) mMiniKeyboardContainer.findViewById(
//						android.R.id.keyboardView);
//			}
//			if (mWindowOffset == null) {
//				mWindowOffset = new int[2];
//				getLocationInWindow(mWindowOffset);
//			}
//			int mPopupX = popupKey.x + getPaddingLeft();
//			int mPopupY = popupKey.y + getPaddingTop();
//			mPopupX = mPopupX + popupKey.width - mMiniKeyboardContainer.getMeasuredWidth();
//			mPopupY = mPopupY - mMiniKeyboardContainer.getMeasuredHeight();
//			final int x = mPopupX + mMiniKeyboardContainer.getPaddingRight() + mWindowOffset[0];
//			final int y = mPopupY + mMiniKeyboardContainer.getPaddingBottom() + mWindowOffset[1];
//			mMiniKeyboard.setPopupOffset(x < 0 ? 0 : x, y);
//			mMiniKeyboard.setShifted(isShifted());
//			mQuickTextPopupKeyboard.setContentView(mMiniKeyboardContainer);
//			mQuickTextPopupKeyboard.setWidth(mMiniKeyboardContainer.getMeasuredWidth());
//			mQuickTextPopupKeyboard.setHeight(mMiniKeyboardContainer.getMeasuredHeight());
//			mQuickTextPopupKeyboard.showAtLocation(this, Gravity.NO_GRAVITY, x, y);
//			mQuickTextKeyboardOnScreen = true;
//			requestSpecialKeysRedraw();
//		}
//	}
//
//	@Override
//	public void onClick(View v) {
//		dismissQuickTextPopupKeyboard();
//		super.onClick(v);
//	}
//
//	private void dismissQuickTextPopupKeyboard() {
//        if (mQuickTextPopupKeyboard.isShowing()) {
//            mQuickTextPopupKeyboard.dismiss();
//            mQuickTextKeyboardOnScreen = false;
//            requestSpecialKeysRedraw();
//        }
//    }
//
//	@Override
//	public boolean handleBack() {
//        if (mQuickTextPopupKeyboard.isShowing()) {
//            dismissQuickTextPopupKeyboard();
//            return true;
//        }
//        return super.handleBack();
//    }
//}