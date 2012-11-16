package com.anysoftkeyboard.keyboards;

public interface KeyboardDimens {

	int getKeyboardMaxWidth();
	int getKeyMaxWidth();
	
	float getKeyHorizontalGap();
	float getRowVerticalGap();
	
	int getNormalKeyHeight();
	int getSmallKeyHeight();
	int getLargeKeyHeight();
}
