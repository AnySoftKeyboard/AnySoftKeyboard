package com.menny.android.anysoftkeyboard;

import android.text.Editable;
import android.text.method.MetaKeyKeyListener;
import android.view.KeyEvent;

import com.menny.android.anysoftkeyboard.keyboards.AnyKeyboard.HardKeyboardAction;

//public class HardKeyboardActionImpl implements HardKeyboardAction
//{
//	private enum MetaKeyState
//	{
//		Off,
//		On,
//		Pressed,
//		Sticky
//	}
//	
//	public MetaKeyState mPhysicalShiftState = MetaKeyState.Off;
//	public MetaKeyState mPhysicalAltState = MetaKeyState.Off;
//	private int mKeyCode = 0;
//	private boolean mChanegd = false;
//	
//	public void resetMetaState()
//	{
//		mPhysicalShiftState = MetaKeyState.Off;
//		mPhysicalAltState = MetaKeyState.Off;
//		mKeyCode = 0;
//		mChanegd = false;
//	}
//	
//	public boolean initializeAction(KeyEvent event)
//	{
//		mChanegd = false;
//		mKeyCode = event.getKeyCode();
//		switch (mKeyCode) 
//		{
//		case KeyEvent.KEYCODE_SHIFT_LEFT:
//		case KeyEvent.KEYCODE_SHIFT_RIGHT:
//			if (event.getRepeatCount() == 0)
//			{
//				mPhysicalShiftState = getNextStateOnMetaKeyPress(mPhysicalShiftState, event.isShiftPressed());
//				if (AnySoftKeyboard.getDEBUG())
//					Log.d("AnySoftKeyboard", "Physical SHIFT was pressed. The new shift state is "+mPhysicalShiftState);
//			}
//			return true;
//		case KeyEvent.KEYCODE_ALT_LEFT:
//		case KeyEvent.KEYCODE_ALT_RIGHT:
//			if (event.getRepeatCount() == 0)
//			{
//				mPhysicalAltState = getNextStateOnMetaKeyPress(mPhysicalAltState, event.isAltPressed());
//				if (AnySoftKeyboard.getDEBUG())
//					Log.d("AnySoftKeyboard", "Physical ALT was pressed. The new ALT state is "+mPhysicalAltState);
//			}
//			return true;
//		default:
//			//if it sticky, then it will stay.
//			//else
//			//if meta-key is pressed, then on else stay as is (may be consumed by the key translation)
//			mPhysicalShiftState = getNextStateOnRegularKey(mPhysicalShiftState, event.isShiftPressed());
//			mPhysicalAltState = getNextStateOnRegularKey(mPhysicalAltState, event.isAltPressed());				
//			return false;
//		}
//	}
//	
//	private static MetaKeyState getNextStateOnRegularKey(MetaKeyState currentState, boolean isPressed) {
//		switch(currentState)
//		{
//		case Off:
//		case Pressed:
//			return isPressed? MetaKeyState.Pressed : MetaKeyState.Off;
//		case On:
//			return isPressed? MetaKeyState.Pressed : MetaKeyState.On;
//		case Sticky:
//			return MetaKeyState.Sticky;
//		default:
//			return MetaKeyState.Off;
//		}
//	}
//
//	private static MetaKeyState getNextStateOnMetaKeyPress(MetaKeyState currentState, boolean isPressed) 
//	{
//		if (isPressed)
//		{
//			switch(currentState)
//			{
//			case Off:
//			case Pressed:
//				return MetaKeyState.On;
//			case On:
//				return MetaKeyState.Sticky;
//			case Sticky:
//				return MetaKeyState.Off;
//			}
//		}
//		
//		return MetaKeyState.Off;
//	}
//
//	public int getKeyCode() {
//		return mKeyCode;
//	}
//	
//	public int consumeKeyCode() {
//		//consuming
//		if (mPhysicalAltState == MetaKeyState.On)
//			mPhysicalAltState = MetaKeyState.Off;
//		if (mPhysicalShiftState == MetaKeyState.On)
//			mPhysicalShiftState = MetaKeyState.Off;
//		
//		return mKeyCode;
//	}
//	
//	public boolean isAltActive() {
//		return mPhysicalAltState != MetaKeyState.Off;
//	}
//
//	public boolean isShiftActive() {
//		return mPhysicalShiftState != MetaKeyState.Off;
//	}
//	
//	public void setNewKeyCode(int keyCode) {
//		mChanegd = true;
//		mKeyCode = keyCode;
//	}
//	
//	public boolean getKeyCodeWasChanged()
//	{
//		return mChanegd;
//	}		
//}

public class HardKeyboardActionImpl implements HardKeyboardAction
{
	private int mKeyCode = 0;
	private boolean mChanegd = false;
	
	public void initializeAction(KeyEvent event)
	{
		mChanegd = false;
		mKeyCode = event.getKeyCode();
	}
	
		public int getKeyCode() {
		return mKeyCode;
	}
	
	public boolean isAltActive() {
		MetaKeyKeyListener.
	}

	public boolean isShiftActive() {
		MetaKeyKeyListener.
	}
	
	public void setNewKeyCode(int keyCode) {
		mChanegd = true;
		mKeyCode = keyCode;
	}
	
	public boolean getKeyCodeWasChanged()
	{
		return mChanegd;
	}		
}

