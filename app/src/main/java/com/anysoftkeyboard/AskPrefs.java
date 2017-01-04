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

package com.anysoftkeyboard;

import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

public interface AskPrefs {

    boolean alwaysUseFallBackUserDictionary();

    enum AnimationsLevel {
        Full,
        Some,
        None
    }

    String getDomainText();

    boolean alwaysHideLanguageKey();

    boolean getShowKeyPreview();

    boolean getShowKeyboardNameText();

    boolean getShowHintTextOnKeys();

    boolean getUseCustomHintAlign();

    int getCustomHintAlign();

    int getCustomHintVAlign();

    AnimationsLevel getAnimationsLevel();

    boolean getSwitchKeyboardOnSpace();

    boolean getUseFullScreenInputInLandscape();

    boolean getUseFullScreenInputInPortrait();

    boolean getUseRepeatingKeys();

    float getKeysHeightFactorInPortrait();

    float getKeysHeightFactorInLandscape();

    boolean getInsertSpaceAfterCandidatePick();

    boolean getGestureTyping();

    int getGestureSwipeUpKeyCode(boolean fromSpaceBar);

    int getGestureSwipeDownKeyCode();

    int getGestureSwipeLeftKeyCode(boolean fromSpaceBar, boolean withTwoFingers);

    int getGestureSwipeRightKeyCode(boolean fromSpaceBar, boolean withTwoFingers);

    int getGesturePinchKeyCode();

    int getGestureSeparateKeyCode();

    boolean getActionKeyInvisibleWhenRequested();

    boolean isDoubleSpaceChangesToPeriod();

    boolean shouldShowPopupForLanguageSwitch();

    boolean hideSoftKeyboardWhenPhysicalKeyPressed();

    boolean use16KeysSymbolsKeyboards();

    boolean useBackword();

    boolean getCycleOverAllSymbols();

    boolean useVolumeKeyForLeftRight();

    boolean useCameraKeyForBackspaceBackword();

    boolean useContactsDictionary();

    int getAutoDictionaryInsertionThreshold();

    boolean isStickyExtensionKeyboard();

    int getSwipeVelocityThreshold();

    int getSwipeDistanceThreshold();

    int getLongPressTimeout();

    int getMultiTapTimeout();

    boolean workaround_alwaysUseDrawText();

    String getInitialKeyboardCondenseState();

    boolean useChewbaccaNotifications();

    boolean showKeyPreviewAboveKey();

    void addChangedListener(OnSharedPreferenceChangeListener listener);

    void removeChangedListener(OnSharedPreferenceChangeListener listener);

    boolean shouldSwapPunctuationAndSpace();

    int getFirstAppVersionInstalled();

    long getFirstTimeAppInstalled();

    long getTimeCurrentVersionInstalled();

    boolean getPersistLayoutForPackageId();

    int getVibrationDuration();
}
