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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.prefs.AnimationsLevel;
import com.menny.android.anysoftkeyboard.BuildConfig;
import com.menny.android.anysoftkeyboard.R;

import java.util.ArrayList;
import java.util.List;

public class AskPrefsImpl implements AskPrefs, OnSharedPreferenceChangeListener {
    private static final String TAG = "ASK_Cfg";

    private final Context mContext;

    private boolean mSwapPunctuationAndSpace = true;
    private boolean mSwitchKeyboardOnSpace = true;
    private boolean mUseFullScreenInputInLandscape = true;
    private boolean mUseFullScreenInputInPortrait = false;
    private boolean mUseKeyRepeat = true;
    private final boolean[] mEnableStateForRowModes = new boolean[]{true, true, true, true};
    private float mKeysHeightFactorInPortrait = 1.0f;
    private float mKeysHeightFactorInLandscape = 1.0f;
    private boolean mInsertSpaceAfterCandidatePick = true;
    private boolean mGestureTyping = false;
    private int mSwipeUpKeyCode;
    private int mSwipeUpFromSpaceBarKeyCode;
    private int mSwipeDownKeyCode;
    private int mSwipeLeftKeyCode;
    private int mSwipeRightKeyCode;
    private int mSwipeLeftFromSpaceBarKeyCode;
    private int mSwipeRightFromSpaceBarKeyCode;
    private int mSwipeLeftWithTwoFingersKeyCode;
    private int mSwipeRightWithTwoFingersKeyCode;
    private int mPinchKeyCode;
    private int mSeparateKeyCode;
    private boolean mActionKeyInvisibleWhenRequested = false;
    private boolean mIsDoubleSpaceChangesToPeroid = true;
    private boolean mShouldPopupForLanguageSwitch = false;
    private boolean mHideSoftKeyboardWhenPhysicalKeyPressed = true;
    private boolean mUse16KeysSymbolsKeyboard = false;
    private boolean mUseBackword = true;
    private boolean mCycleOverAllSymbolsKeyboard = true;
    private boolean mUseVolumeKeyForLeftRight = false;
    private boolean mUseCameraKeyForBackspaceBackword = false;
    private boolean mUseContactsDictionary = true;
    private int mAutoDictionaryInsertionThreshold = 9;
    private boolean mIsStickyExtensionKeyboard = false;
    private AnimationsLevel mAnimationsLevel = AnimationsLevel.Full;
    private int mLongPressTimeout = 350;
    private int mMultiTapTimeout = 700;

    private boolean mWorkaroundAlwaysUseDrawText = false;

    private final List<OnSharedPreferenceChangeListener> mPreferencesChangedListeners = new ArrayList<>(10);
    private boolean mAutomaticallySwitchToAppLayout = true;

    public AskPrefsImpl(Context context, SharedPreferences sp) {
        mContext = context;
        onSharedPreferenceChanged(sp, "");
        sp.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void addChangedListener(OnSharedPreferenceChangeListener listener) {
        mPreferencesChangedListeners.add(listener);
    }

    @Override
    public void removeChangedListener(OnSharedPreferenceChangeListener listener) {
        mPreferencesChangedListeners.remove(listener);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
        Logger.d(TAG, "**** onSharedPreferenceChanged: ");

        mSwitchKeyboardOnSpace = sp.getBoolean(
                mContext.getString(R.string.settings_key_switch_keyboard_on_space),
                mContext.getResources().getBoolean(R.bool.settings_default_switch_to_alphabet_on_space));
        Logger.d(TAG, "** mSwitchKeyboardOnSpace: " + mSwitchKeyboardOnSpace);

        mUseFullScreenInputInLandscape = sp.getBoolean(mContext.getString(R.string.settings_key_landscape_fullscreen),
                mContext.getResources().getBoolean(R.bool.settings_default_landscape_fullscreen));
        Logger.d(TAG, "** mUseFullScreenInputInLandscape: " + mUseFullScreenInputInLandscape);

        mUseFullScreenInputInPortrait = sp.getBoolean(mContext.getString(R.string.settings_key_portrait_fullscreen),
                mContext.getResources().getBoolean(R.bool.settings_default_portrait_fullscreen));
        Logger.d(TAG, "** mUseFullScreenInputInPortrait: " + mUseFullScreenInputInPortrait);

        // Fix issue 185
        mUseKeyRepeat = sp.getBoolean("use_keyrepeat", true);
        Logger.d(TAG, "** mUseKeyRepeat: " + mUseKeyRepeat);

        mKeysHeightFactorInPortrait = getFloatFromString(sp, "zoom_factor_keys_in_portrait", mContext.getString(R.string.settings_default_portrait_keyboard_height_factor));
        Logger.d(TAG, "** mKeysHeightFactorInPortrait: " + mKeysHeightFactorInPortrait);
        if (mKeysHeightFactorInPortrait > 2.0f) {
            mKeysHeightFactorInPortrait = 2.0f;
            Logger.d(TAG, "** mKeysHeightFactorInPortrait fixed to: " + mKeysHeightFactorInPortrait);
        } else if (mKeysHeightFactorInPortrait < 0.2f) {
            mKeysHeightFactorInPortrait = 0.2f;
            Logger.d(TAG, "** mKeysHeightFactorInPortrait fixed to: " + mKeysHeightFactorInPortrait);
        }
        mKeysHeightFactorInLandscape = getFloatFromString(sp, "zoom_factor_keys_in_landscape", mContext.getString(R.string.settings_default_landscape_keyboard_height_factor));
        Logger.d(TAG, "** mKeysHeightFactorInLandscape: " + mKeysHeightFactorInLandscape);
        if (mKeysHeightFactorInLandscape > 2.0f) {
            mKeysHeightFactorInLandscape = 2.0f;
            Logger.d(TAG, "** mKeysHeightFactorInLandscape fixed to: " + mKeysHeightFactorInLandscape);
        } else if (mKeysHeightFactorInPortrait < 0.2f) {
            mKeysHeightFactorInPortrait = 0.2f;
            Logger.d(TAG, "** mKeysHeightFactorInPortrait fixed to: " + mKeysHeightFactorInLandscape);
        }

        mInsertSpaceAfterCandidatePick = sp.getBoolean("insert_space_after_word_suggestion_selection", true);
        Logger.d(TAG, "** mInsertSpaceAfterCandidatePick: " + mInsertSpaceAfterCandidatePick);

        mSwipeUpKeyCode = getIntFromSwipeConfiguration(sp, R.string.settings_key_swipe_up_action, R.string.swipe_action_value_shift);
        Logger.d(TAG, "** mSwipeUpKeyCode: " + mSwipeUpKeyCode);

        mSwipeUpFromSpaceBarKeyCode = getIntFromSwipeConfiguration(sp, R.string.settings_key_swipe_up_from_spacebar_action, R.string.swipe_action_value_utility_keyboard);
        Logger.d(TAG, "** mSwipeUpFromSpaceBarKeyCode: " + mSwipeUpFromSpaceBarKeyCode);

        mSwipeDownKeyCode = getIntFromSwipeConfiguration(sp, R.string.settings_key_swipe_down_action, R.string.swipe_action_value_hide);
        Logger.d(TAG, "** mSwipeDownKeyCode: " + mSwipeDownKeyCode);

        mSwipeLeftKeyCode = getIntFromSwipeConfiguration(sp, R.string.settings_key_swipe_left_action, R.string.swipe_action_value_next_symbols);
        Logger.d(TAG, "** mSwipeLeftKeyCode: " + mSwipeLeftKeyCode);

        mSwipeRightKeyCode = getIntFromSwipeConfiguration(sp, R.string.settings_key_swipe_right_action, R.string.swipe_action_value_next_alphabet);
        Logger.d(TAG, "** mSwipeRightKeyCode: " + mSwipeRightKeyCode);

        mPinchKeyCode = getIntFromSwipeConfiguration(sp, R.string.settings_key_pinch_gesture_action, R.string.swipe_action_value_merge_layout);
        Logger.d(TAG, "** mPinchKeyCode: " + mPinchKeyCode);

        mSeparateKeyCode = getIntFromSwipeConfiguration(sp, R.string.settings_key_separate_gesture_action, R.string.swipe_action_value_split_layout);
        Logger.d(TAG, "** mSeparateKeyCode: " + mSeparateKeyCode);

        mSwipeLeftFromSpaceBarKeyCode = getIntFromSwipeConfiguration(sp, R.string.settings_key_swipe_left_space_bar_action, R.string.swipe_action_value_next_symbols);
        Logger.d(TAG, "** mSwipeLeftFromSpaceBarKeyCode: " + mSwipeLeftFromSpaceBarKeyCode);

        mSwipeRightFromSpaceBarKeyCode = getIntFromSwipeConfiguration(sp, R.string.settings_key_swipe_right_space_bar_action, R.string.swipe_action_value_next_alphabet);
        Logger.d(TAG, "** mSwipeRightFromSpaceBarKeyCode: " + mSwipeRightFromSpaceBarKeyCode);

        mSwipeLeftWithTwoFingersKeyCode = getIntFromSwipeConfiguration(sp, R.string.settings_key_swipe_left_two_fingers_action, R.string.swipe_action_value_compact_layout_to_left);
        Logger.d(TAG, "** mSwipeLeftWithTwoFingersKeyCode: " + mSwipeLeftWithTwoFingersKeyCode);

        mSwipeRightWithTwoFingersKeyCode = getIntFromSwipeConfiguration(sp, R.string.settings_key_swipe_right_two_fingers_action, R.string.swipe_action_value_compact_layout_to_right);
        Logger.d(TAG, "** mSwipeRightWithTwoFingersKeyCode: " + mSwipeRightWithTwoFingersKeyCode);

        mActionKeyInvisibleWhenRequested = sp.getBoolean("action_key_invisible_on_disable", false);
        Logger.d(TAG, "** mActionKeyInvisibleWhenRequested: " + mActionKeyInvisibleWhenRequested);

        /*mRtlWorkaround = sp.getString("rtl_workaround_detection", "auto");
        Logger.d(mTag, "** mRtlWorkaround: "+mRtlWorkaround);
        */
        mIsDoubleSpaceChangesToPeroid = sp.getBoolean("double_space_to_period", true);
        Logger.d(TAG, "** mIsDoubleSpaceChangesToPeroid: " + mIsDoubleSpaceChangesToPeroid);

        mShouldPopupForLanguageSwitch = sp.getBoolean(mContext.getString(R.string.settings_key_lang_key_shows_popup),
                mContext.getResources().getBoolean(R.bool.settings_default_lang_key_shows_popup));
        Logger.d(TAG, "** mShouldPopupForLanguageSwitch: " + mShouldPopupForLanguageSwitch);

        mHideSoftKeyboardWhenPhysicalKeyPressed = sp.getBoolean(mContext.getString(R.string.settings_key_hide_soft_when_physical),
                mContext.getResources().getBoolean(R.bool.settings_default_hide_soft_when_physical));
        Logger.d(TAG, "** mHideSoftKeyboardWhenPhysicalKeyPressed: " + mHideSoftKeyboardWhenPhysicalKeyPressed);

        mUse16KeysSymbolsKeyboard = sp.getBoolean(mContext.getString(R.string.settings_key_use_16_keys_symbols_keyboards),
                mContext.getResources().getBoolean(R.bool.settings_default_use_16_keys_symbols_keyboards));
        Logger.d(TAG, "** mUse16KeysSymbolsKeyboard: " + mUse16KeysSymbolsKeyboard);

        mUseBackword = sp.getBoolean(mContext.getString(R.string.settings_key_use_backword),
                mContext.getResources().getBoolean(R.bool.settings_default_use_backword));
        Logger.d(TAG, "** mUseBackword: " + mUseBackword);

        mCycleOverAllSymbolsKeyboard = sp.getBoolean(mContext.getString(R.string.settings_key_cycle_all_symbols),
                mContext.getResources().getBoolean(R.bool.settings_default_cycle_all_symbols));
        Logger.d(TAG, "** mCycleOverAllSymbolsKeyboard: " + mCycleOverAllSymbolsKeyboard);


        mUseCameraKeyForBackspaceBackword = sp.getBoolean(mContext.getString(R.string.settings_key_use_camera_key_for_backspace_backword),
                mContext.getResources().getBoolean(R.bool.settings_default_use_camera_key_for_backspace_backword));
        Logger.d(TAG, "** mUseCameraKeyForBackspaceBackword: " + mUseCameraKeyForBackspaceBackword);

        mUseVolumeKeyForLeftRight = sp.getBoolean(mContext.getString(R.string.settings_key_use_volume_key_for_left_right),
                mContext.getResources().getBoolean(R.bool.settings_default_use_volume_key_for_left_right));
        Logger.d(TAG, "** mUseVolumeKeyForLeftRight: " + mUseVolumeKeyForLeftRight);

        mUseContactsDictionary = sp.getBoolean(mContext.getString(R.string.settings_key_use_contacts_dictionary),
                mContext.getResources().getBoolean(R.bool.settings_default_contacts_dictionary));
        Logger.d(TAG, "** mUseContactsDictionary: " + mUseContactsDictionary);

        mAutoDictionaryInsertionThreshold = getIntFromString(sp,
                mContext.getString(R.string.settings_key_auto_dictionary_threshold),
                mContext.getString(R.string.settings_default_auto_dictionary_add_threshold));
        Logger.d(TAG, "** mAutoDictionaryInsertionThreshold: " + mAutoDictionaryInsertionThreshold);

        mIsStickyExtensionKeyboard = sp.getBoolean(mContext.getString(R.string.settings_key_is_sticky_extesion_keyboard),
                mContext.getResources().getBoolean(R.bool.settings_default_is_sticky_extesion_keyboard));
        Logger.d(TAG, "** mIsStickyExtensionKeyboard: " + mIsStickyExtensionKeyboard);

        //gesture typing only available in DEBUG build!!!
        mGestureTyping = BuildConfig.DEBUG && sp.getBoolean(mContext.getString(R.string.settings_key_gesture_typing),
                mContext.getResources().getBoolean(R.bool.settings_default_gesture_typing));
        Logger.d(TAG, "** mGestureTyping: " + mGestureTyping);

        mLongPressTimeout = getIntFromString(sp,
                mContext.getString(R.string.settings_key_long_press_timeout),
                mContext.getString(R.string.settings_default_long_press_timeout));
        Logger.d(TAG, "** mLongPressTimeout: " + mLongPressTimeout);

        mMultiTapTimeout = getIntFromString(sp,
                mContext.getString(R.string.settings_key_multitap_timeout),
                mContext.getString(R.string.settings_default_multitap_timeout));
        Logger.d(TAG, "** mMultiTapTimeout: " + mMultiTapTimeout);

        mWorkaroundAlwaysUseDrawText = sp.getBoolean(mContext.getString(R.string.settings_key_workaround_disable_rtl_fix),
                getAlwaysUseDrawTextDefault());
        Logger.d(TAG, "** mWorkaroundAlwaysUseDrawText: " + mWorkaroundAlwaysUseDrawText);

        mSwapPunctuationAndSpace = sp.getBoolean(mContext.getString(R.string.settings_key_bool_should_swap_punctuation_and_space),
                mContext.getResources().getBoolean(R.bool.settings_default_bool_should_swap_punctuation_and_space));
        Logger.d(TAG, "** mSwapPunctuationAndSpace: " + mSwapPunctuationAndSpace);

        String animationsLevel = sp.getString(mContext.getString(R.string.settings_key_tweak_animations_level),
                mContext.getString(R.string.settings_default_tweak_animations_level));
        if ("none".equals(animationsLevel))
            mAnimationsLevel = AnimationsLevel.None;
        else if ("some".equals(animationsLevel))
            mAnimationsLevel = AnimationsLevel.Some;
        else
            mAnimationsLevel = AnimationsLevel.Full;
        Logger.d(TAG, "** mAnimationsLevel: " + mAnimationsLevel);

        mAutomaticallySwitchToAppLayout = sp.getBoolean(mContext.getString(R.string.settings_key_persistent_layout_per_package_id),
                mContext.getResources().getBoolean(R.bool.settings_default_persistent_layout_per_package_id));
        Logger.d(TAG, "** mAutomaticallySwitchToAppLayout: " + mAutomaticallySwitchToAppLayout);

        for (int rowModeIndex = 0; rowModeIndex < mEnableStateForRowModes.length; rowModeIndex++) {
            mEnableStateForRowModes[rowModeIndex] = sp.getBoolean(AskPrefs.ROW_MODE_ENABLED_PREFIX + (rowModeIndex + 2), true);
        }
        //Some preferences cause rebuild of the keyboard, hence changing the listeners list
        final List<OnSharedPreferenceChangeListener> disconnectedList = new ArrayList<>(mPreferencesChangedListeners);
        for (OnSharedPreferenceChangeListener listener : disconnectedList) {
            //before notifying, we'll ensure that the listener is still interested in the callback
            if (mPreferencesChangedListeners.contains(listener)) {
                listener.onSharedPreferenceChanged(sp, key);
            }
        }
    }

    private boolean getAlwaysUseDrawTextDefault() {
        return android.os.Build.BRAND.contains("SEMC") ||//SE phones have fix for that, but more important, their StaticLayout class is bugged
                mContext.getResources().getBoolean(R.bool.settings_default_workaround_disable_rtl_fix);
    }

    private int getIntFromSwipeConfiguration(SharedPreferences sp, final int prefKeyResId, final int defaultValueResId) {
        final String prefKey = mContext.getString(prefKeyResId);
        final String defaultValue = mContext.getString(defaultValueResId);

        final String keyValue = sp.getString(prefKey, defaultValue);

        if (keyValue.equalsIgnoreCase(mContext.getString(R.string.swipe_action_value_next_alphabet)))
            return KeyCodes.MODE_ALPHABET;
        else if (keyValue.equalsIgnoreCase(mContext.getString(R.string.swipe_action_value_next_symbols)))
            return KeyCodes.MODE_SYMOBLS;
        else if (keyValue.equalsIgnoreCase(mContext.getString(R.string.swipe_action_value_cycle_keyboards)))
            return KeyCodes.KEYBOARD_CYCLE;
        else if (keyValue.equalsIgnoreCase(mContext.getString(R.string.swipe_action_value_reverse_cycle_keyboards)))
            return KeyCodes.KEYBOARD_REVERSE_CYCLE;
        else if (keyValue.equalsIgnoreCase(mContext.getString(R.string.swipe_action_value_shift)))
            return KeyCodes.SHIFT;
        else if (keyValue.equalsIgnoreCase(mContext.getString(R.string.swipe_action_value_hide)))
            return KeyCodes.CANCEL;
        else if (keyValue.equalsIgnoreCase(mContext.getString(R.string.swipe_action_value_backspace)))
            return KeyCodes.DELETE;
        else if (keyValue.equalsIgnoreCase(mContext.getString(R.string.swipe_action_value_backword)))
            return KeyCodes.DELETE_WORD;
        else if (keyValue.equalsIgnoreCase(mContext.getString(R.string.swipe_action_value_clear_input)))
            return KeyCodes.CLEAR_INPUT;
        else if (keyValue.equalsIgnoreCase(mContext.getString(R.string.swipe_action_value_cursor_up)))
            return KeyCodes.ARROW_UP;
        else if (keyValue.equalsIgnoreCase(mContext.getString(R.string.swipe_action_value_cursor_down)))
            return KeyCodes.ARROW_DOWN;
        else if (keyValue.equalsIgnoreCase(mContext.getString(R.string.swipe_action_value_cursor_left)))
            return KeyCodes.ARROW_LEFT;
        else if (keyValue.equalsIgnoreCase(mContext.getString(R.string.swipe_action_value_cursor_right)))
            return KeyCodes.ARROW_RIGHT;
        else if (keyValue.equalsIgnoreCase(mContext.getString(R.string.swipe_action_value_next_inside_mode)))
            return KeyCodes.KEYBOARD_CYCLE_INSIDE_MODE;
        else if (keyValue.equalsIgnoreCase(mContext.getString(R.string.swipe_action_value_switch_keyboard_mode)))
            return KeyCodes.KEYBOARD_MODE_CHANGE;
        else if (keyValue.equalsIgnoreCase(mContext.getString(R.string.swipe_action_value_split_layout)))
            return KeyCodes.SPLIT_LAYOUT;
        else if (keyValue.equalsIgnoreCase(mContext.getString(R.string.swipe_action_value_merge_layout)))
            return KeyCodes.MERGE_LAYOUT;
        else if (keyValue.equalsIgnoreCase(mContext.getString(R.string.swipe_action_value_compact_layout_to_left)))
            return KeyCodes.COMPACT_LAYOUT_TO_LEFT;
        else if (keyValue.equalsIgnoreCase(mContext.getString(R.string.swipe_action_value_compact_layout_to_right)))
            return KeyCodes.COMPACT_LAYOUT_TO_RIGHT;
        else if (keyValue.equalsIgnoreCase(mContext.getString(R.string.swipe_action_value_utility_keyboard)))
            return KeyCodes.UTILITY_KEYBOARD;

        return 0;//0 means no action
    }

    private static float getFloatFromString(SharedPreferences sp, String prefKey, String defaultValue) {
        String floatValue = sp.getString(prefKey, defaultValue);
        try {
            return Float.parseFloat(floatValue);
        } catch (Exception e) {
            try {
                return Float.parseFloat(defaultValue);
            } catch (Exception e2) {
                return 1.0f;
            }
        }
    }

    private static int getIntFromString(SharedPreferences sp, String prefKey, String defaultValue) {
        String intValue = sp.getString(prefKey, defaultValue);
        try {
            return Integer.parseInt(intValue);
        } catch (Exception e) {
            try {
                return Integer.parseInt(defaultValue);
            } catch (Exception e2) {
                return 500;
            }
        }
    }

    @Override
    public boolean isEnableStateForRowMode(@Keyboard.KeyboardRowModeId int modeId) {
        return mEnableStateForRowModes[modeId - 2];
    }

    @Override
    public boolean getSwitchKeyboardOnSpace() {
        return mSwitchKeyboardOnSpace;
    }

    @Override
    public boolean getUseFullScreenInputInLandscape() {
        return mUseFullScreenInputInLandscape;
    }

    @Override
    public boolean getUseFullScreenInputInPortrait() {
        return mUseFullScreenInputInPortrait;
    }

    @Override
    public boolean getUseRepeatingKeys() {
        return mUseKeyRepeat;
    }

    @Override
    public float getKeysHeightFactorInLandscape() {
        return mKeysHeightFactorInLandscape;
    }

    @Override
    public float getKeysHeightFactorInPortrait() {
        return mKeysHeightFactorInPortrait;
    }

    @Override
    public boolean getInsertSpaceAfterCandidatePick() {
        return mInsertSpaceAfterCandidatePick;
    }

    @Override
    public boolean getGestureTyping() {
        return mGestureTyping;
    }

    @Override
    public int getGestureSwipeUpKeyCode(boolean fromSpaceBar) {
        return fromSpaceBar ? mSwipeUpFromSpaceBarKeyCode : mSwipeUpKeyCode;
    }

    @Override
    public int getGestureSwipeDownKeyCode() {
        return mSwipeDownKeyCode;
    }

    @Override
    public int getGestureSwipeLeftKeyCode(boolean fromSpaceBar, boolean withTwoFingers) {
        return fromSpaceBar ? mSwipeLeftFromSpaceBarKeyCode :
                withTwoFingers ? mSwipeLeftWithTwoFingersKeyCode : mSwipeLeftKeyCode;
    }

    @Override
    public int getGestureSwipeRightKeyCode(boolean fromSpaceBar, boolean withTwoFingers) {
        return fromSpaceBar ? mSwipeRightFromSpaceBarKeyCode :
                withTwoFingers ? mSwipeRightWithTwoFingersKeyCode : mSwipeRightKeyCode;
    }

    @Override
    public int getGesturePinchKeyCode() {
        return mPinchKeyCode;
    }

    @Override
    public int getGestureSeparateKeyCode() {
        return mSeparateKeyCode;
    }

    @Override
    public boolean getActionKeyInvisibleWhenRequested() {
        return mActionKeyInvisibleWhenRequested;
    }

    @Override
    public boolean isDoubleSpaceChangesToPeriod() {
        return mIsDoubleSpaceChangesToPeroid;
    }

    @Override
    public boolean shouldShowPopupForLanguageSwitch() {
        return mShouldPopupForLanguageSwitch;
    }

    @Override
    public boolean hideSoftKeyboardWhenPhysicalKeyPressed() {
        return mHideSoftKeyboardWhenPhysicalKeyPressed;
    }

    @Override
    public boolean use16KeysSymbolsKeyboards() {
        return mUse16KeysSymbolsKeyboard;
    }

    @Override
    public boolean useBackword() {
        return mUseBackword;
    }

    @Override
    public boolean getCycleOverAllSymbols() {
        return mCycleOverAllSymbolsKeyboard;
    }

    @Override
    public boolean useCameraKeyForBackspaceBackword() {
        return mUseCameraKeyForBackspaceBackword;
    }

    @Override
    public boolean useVolumeKeyForLeftRight() {
        return mUseVolumeKeyForLeftRight;
    }

    @Override
    public boolean useContactsDictionary() {
        return mUseContactsDictionary;
    }

    @Override
    public int getAutoDictionaryInsertionThreshold() {
        return mAutoDictionaryInsertionThreshold;
    }

    @Override
    public boolean isStickyExtensionKeyboard() {
        return mIsStickyExtensionKeyboard;
    }

    @Override
    public int getLongPressTimeout() {
        return mLongPressTimeout;
    }

    @Override
    public int getMultiTapTimeout() {
        return mMultiTapTimeout;
    }

    @Override
    public boolean workaround_alwaysUseDrawText() {
        return mWorkaroundAlwaysUseDrawText;
    }

    @Override
    public boolean shouldSwapPunctuationAndSpace() {
        return mSwapPunctuationAndSpace;
    }

    @Override
    public AnimationsLevel getAnimationsLevel() {
        return mAnimationsLevel;
    }

    @Override
    public boolean getPersistLayoutForPackageId() {
        return mAutomaticallySwitchToAppLayout;
    }
}