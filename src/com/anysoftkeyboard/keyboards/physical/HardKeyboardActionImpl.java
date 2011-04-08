package com.anysoftkeyboard.keyboards.physical;

import android.text.method.MetaKeyKeyListener;
import android.view.KeyEvent;

import com.anysoftkeyboard.keyboards.AnyKeyboard.HardKeyboardAction;

public class HardKeyboardActionImpl implements HardKeyboardAction
{
	private int mKeyCode = 0;
	private boolean mChanegd = false;
	private long mMetaState;
	
	private final int META_ACTIVE_ALT = (MetaKeyKeyListener.META_ALT_ON | MetaKeyKeyListener.META_ALT_LOCKED);
	private final int META_ACTIVE_SHIFT = (MetaKeyKeyListener.META_SHIFT_ON | MetaKeyKeyListener.META_CAP_LOCKED);
	
	public void initializeAction(KeyEvent event, long metaState)
	{
		mChanegd = false;
		mKeyCode = event.getKeyCode();
		mMetaState = metaState;
	}
	
		public int getKeyCode() {
		return mKeyCode;
	}
	
	public boolean isAltActive() {
		return (MetaKeyKeyListener.getMetaState(mMetaState) & META_ACTIVE_ALT) != 0;
	}

	public boolean isShiftActive() {
		return (MetaKeyKeyListener.getMetaState(mMetaState) & META_ACTIVE_SHIFT) != 0;
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

