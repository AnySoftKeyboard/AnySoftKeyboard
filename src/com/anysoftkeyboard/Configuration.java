package com.anysoftkeyboard;

public interface Configuration {
	
	String getDomainText();
	
	String getChangeLayoutKeysSize();
	
	boolean getShowKeyPreview();

	boolean getSwitchKeyboardOnSpace();
	
	boolean getUseFullScreenInputInLandscape();
	
	boolean getUseFullScreenInputInPortrait();
	
	boolean getUseRepeatingKeys();
	
	float getKeysHeightFactorInPortrait();
	
	float getKeysHeightFactorInLandscape();
	
	boolean getInsertSpaceAfterCandidatePick();
	
	int getSwipeUpKeyCode();
	int getSwipeDownKeyCode();
	int getSwipeLeftKeyCode();
	int getSwipeRightKeyCode();
	
	boolean getActionKeyInvisibleWhenRequested();
	
	int getDeviceOrientation();
	
	String getRtlWorkaroundConfiguration();
	
	boolean isDoubleSpaceChangesToPeriod();
	
	boolean shouldShowPopupForLanguageSwitch();
	
	boolean hideSoftKeyboardWhenPhysicalKeyPressed();
	
	boolean getShowVersionNotification();
	
	void setShowVersionNotification(boolean show);
	
	boolean use16KeysSymbolsKeyboards();
	
	boolean useBackword();

	boolean getCycleOverAllSymbols();
	
	boolean useVolumeKeyForLeftRight();
	
	boolean useCameraKeyForBackspaceBackword();
	
	boolean useContactsDictionary();
	
	boolean useAutoDictionary();
	
	boolean isStickyExtensionKeyboard();
	
	boolean drawExtensionKeyboardAboveMainKeyboard();

	int getSwipeVelocityThreshold();
	
	int getSwipeDistanceThreshold();
}
