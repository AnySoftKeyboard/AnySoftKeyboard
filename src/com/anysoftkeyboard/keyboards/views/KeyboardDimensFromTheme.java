package com.anysoftkeyboard.keyboards.views;

import com.anysoftkeyboard.keyboards.KeyboardDimens;

class KeyboardDimensFromTheme implements KeyboardDimens {

	private int mMaxKeyboardWidth;
	private float mKeyHorizontalGap;
	private float mRowVerticalGap;
	private int mNormalKeyHeight;
	private int mSmallKeyHeight;
	private int mLargeKeyHeight;
	private int mMaxKeyWidth = Integer.MAX_VALUE;

	KeyboardDimensFromTheme()
	{
	}
	
	public int getKeyboardMaxWidth() {
		return mMaxKeyboardWidth;
	}
	
	public int getKeyMaxWidth() {
		return mMaxKeyWidth;
	}
	
	public float getKeyHorizontalGap() {
		return mKeyHorizontalGap;
	}

	public float getRowVerticalGap() {
		return mRowVerticalGap;
	}

	public int getNormalKeyHeight() {
		return mNormalKeyHeight;
	}

	public int getSmallKeyHeight() {
		return mSmallKeyHeight;
	}

	public int getLargeKeyHeight() {
		return mLargeKeyHeight;
	}

	void setKeyboardMaxWidth(int maxKeyboardWidth) {
		mMaxKeyboardWidth = maxKeyboardWidth;
	}
	
	void setHorizontalKeyGap(float themeHorizotalKeyGap) {
		mKeyHorizontalGap = themeHorizotalKeyGap;
	}

	void setVerticalRowGap(float themeVerticalRowGap) {
		mRowVerticalGap = themeVerticalRowGap;
	}

	void setNormalKeyHeight(float themeNormalKeyHeight) {
		mNormalKeyHeight = (int)themeNormalKeyHeight;
	}

	void setLargeKeyHeight(float themeLargeKeyHeight) {
		mLargeKeyHeight = (int)themeLargeKeyHeight;
	}

	void setSmallKeyHeight(float themeSmallKeyHeight) {
		mSmallKeyHeight = (int)themeSmallKeyHeight;
	}
	
	void setKeyMaxWidth(int keyMaxWidth) {
		mMaxKeyWidth = keyMaxWidth;
	}

}
