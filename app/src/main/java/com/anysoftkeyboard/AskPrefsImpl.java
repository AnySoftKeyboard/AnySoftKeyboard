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
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.support.v4.content.SharedPreferencesCompat;
import android.view.Gravity;

import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.utils.Logger;
import com.menny.android.anysoftkeyboard.BuildConfig;
import com.menny.android.anysoftkeyboard.R;

import java.util.LinkedList;
import java.util.Map;

public class AskPrefsImpl implements AskPrefs, OnSharedPreferenceChangeListener {
    private static final String TAG = "ASK_Cfg";

    static final String CONFIGURATION_VERSION = "configurationVersion";
    static final int CONFIGURATION_LEVEL_VALUE = 11;

    private final Context mContext;

    private String mDomainText = ".com";
    private boolean mAlwaysHideLanguageKey = false;
    private boolean mShowKeyPreview = true;
    private boolean mKeyPreviewAboveKey = true;
    private boolean mSwapPunctuationAndSpace = true;
    private boolean mShowHintTextOnKeys = true;
    private boolean mShowKeyboardNameText = true;
    private boolean mUseCustomHintAlign = true;
    private int mCustomHintAlign = Gravity.BOTTOM;
    private int mCustomHintVAlign = Gravity.TOP;
    private boolean mSwitchKeyboardOnSpace = true;
    private boolean mUseFullScreenInputInLandscape = true;
    private boolean mUseFullScreenInputInPortrait = false;
    private boolean mUseChewbacca = true;
    private boolean mUseKeyRepeat = true;
    private final boolean[] mEnableStateForRowModes = new boolean[]{true, true, true, true};
    private float mKeysHeightFactorInPortrait = 1.0f;
    private float mKeysHeightFactorInLandscape = 1.0f;
    private boolean mInsertSpaceAfterCandidatePick = true;
    private boolean mGestureTyping = false;
    private int mSwipeDistanceThreshold = 240;
    private int mSwipeVelocityThreshold = 400;
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
    //private String mRtlWorkaround ="auto";
    private boolean mIsDoubleSpaceChangesToPeroid = true;
    private boolean mShouldPopupForLanguageSwitch = false;
    private boolean mHideSoftKeyboardWhenPhysicalKeyPressed = true;
    private boolean mUse16KeysSymbolsKeyboard = false;
    private boolean mUseBackword = true;
    //      private boolean mShowIconForSmileyKey = false;
    private boolean mCycleOverAllSymbolsKeyboard = true;
    private boolean mUseVolumeKeyForLeftRight = false;
    private boolean mUseCameraKeyForBackspaceBackword = false;
    private boolean mUseContactsDictionary = true;
    private int mAutoDictionaryInsertionThreshold = 9;
    private boolean mIsStickyExtensionKeyboard = false;
    private AskPrefs.AnimationsLevel mAnimationsLevel = AnimationsLevel.Full;
    private int mLongPressTimeout = 350;
    private int mMultiTapTimeout = 700;

    private boolean mWorkaroundAlwaysUseDrawText = false;

    private boolean mAlwaysUseFallBackUserDictionary = false;

    private long mFirstTimeAppInstalled;
    private long mFirstTimeCurrentVersionInstalled;
    private int mFirstAppVersionInstalled;

    private final LinkedList<OnSharedPreferenceChangeListener> mPreferencesChangedListeners = new LinkedList<>();
    private boolean mAutomaticallySwitchToAppLayout = true;

    public AskPrefsImpl(Context context) {
        mContext = context;

        Logger.i(TAG, "** Version: " + BuildConfig.VERSION_NAME);
        Logger.i(TAG, "** Release code: " + BuildConfig.VERSION_CODE);
        Logger.i(TAG, "** BUILD_TYPE: " + BuildConfig.BUILD_TYPE);
        Logger.i(TAG, "** DEBUG: " + BuildConfig.DEBUG);
        Logger.i(TAG, "** TESTING_BUILD: " + BuildConfig.TESTING_BUILD);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        //setting some statistics
        updateStatistics(sp, mContext);

        customizeSettingValues(mContext.getApplicationContext(), sp);
        upgradeSettingsValues(sp);
        initializeComputedValues(sp);

        onSharedPreferenceChanged(sp, "");
    }

    private void updateStatistics(SharedPreferences sp, Context context) {
        boolean firstAppInstall = false;
        boolean firstVersionInstall = false;

        final String FIRST_APP_VERSION_INSTALL = context.getString(R.string.settings_key_first_app_version_installed);
        if (!sp.contains(FIRST_APP_VERSION_INSTALL)) {
            firstAppInstall = true;
        }

        final String LAST_APP_VERSION_INSTALLED = context.getString(R.string.settings_key_last_app_version_installed);
        if (sp.getInt(LAST_APP_VERSION_INSTALLED, 0) != BuildConfig.VERSION_CODE) {
            firstVersionInstall = true;
        }

        if (firstAppInstall || firstVersionInstall) {
            Editor editor = sp.edit();

            final long installTime = System.currentTimeMillis();
            if (firstAppInstall) {
                editor.putInt(FIRST_APP_VERSION_INSTALL, BuildConfig.VERSION_CODE);
                editor.putLong(context.getString(R.string.settings_key_first_time_app_installed), installTime);
            }

            if (firstVersionInstall) {
                editor.putInt(LAST_APP_VERSION_INSTALLED, BuildConfig.VERSION_CODE);
                editor.putLong(context.getString(R.string.settings_key_first_time_current_version_installed), installTime);
            }
            SharedPreferencesCompat.EditorCompat.getInstance().apply(editor);
        }
    }

    private void customizeSettingValues(Context context, SharedPreferences sp) {
//          final int customizationLevel = sp.getInt(CUSTOMIZATION_LEVEL, 0);
//          if (customizationLevel < 1)
//          {
//              Editor e = sp.edit();
//              
//              e.putBoolean(context.getString(R.string.settings_key_lang_key_shows_popup), true);
//              e.putBoolean(context.getString(R.string.settings_key_show_version_notification), false);
//              e.putBoolean(context.getString(R.string.settings_key_use_16_keys_symbols_keyboards), true);
//              e.putBoolean(context.getString(R.string.settings_key_landscape_fullscreen), true);
//              e.putBoolean(context.getString(R.string.settings_key_portrait_fullscreen), true);
//              //enabling 16keys, disabling english
//              e.putBoolean("keyboard_12335055-4aa6-49dc-8456-c7d38a1a5123", true);
//              e.putBoolean("keyboard_c7535083-4fe6-49dc-81aa-c5438a1a343a", false);
//              
//              //enabling external Hebrew
//              e.putBoolean("keyboard_8958fb12-6558-4e96-9aa6-0e90101570b3", true);
//              
//              //sound on
//              e.putBoolean(context.getString(R.string.settings_key_sound_on), true);
//              //custom volume: mid
//              e.putBoolean("use_custom_sound_volume", true);
//              e.putInt("custom_sound_volume", 50);
//              //vibrate on (hard)
//              e.putString(context.getString(R.string.settings_key_vibrate_on_key_press_duration), "50");
//              //no RTL fixes
//              e.putString("rtl_workaround_detection", "no_workaround");
//              //no backword
//              e.putBoolean(context.getString(R.string.settings_key_use_backword), false);
//              //portrait height
//              e.putString("zoom_factor_keys_in_portrait", "1.4");
//              //saving customization level
//              e.putInt(CUSTOMIZATION_LEVEL, 1);
//              e.commit();
//          }
    }

    /**
     * The purpose of this function is to set in the preferences file the computed values.
     * This is required since the Preferences xml UI elements can not take computed values, only static ones, as default.
     * So, the computed default could be one, and the static default may be another!
     * See https://github.com/AnySoftKeyboard/AnySoftKeyboard/issues/110
     */
    private void initializeComputedValues(SharedPreferences sp) {
        boolean drawType = sp.getBoolean(mContext.getString(R.string.settings_key_workaround_disable_rtl_fix),
                getAlwaysUseDrawTextDefault());

        Editor e = sp.edit();
        e.putBoolean(mContext.getString(R.string.settings_key_workaround_disable_rtl_fix), drawType);
        SharedPreferencesCompat.EditorCompat.getInstance().apply(e);
    }

    private void upgradeSettingsValues(SharedPreferences sp) {
        Logger.d(TAG, "Checking if configuration upgrade is needed.");
        //please note: the default value should be the last version.
        //upgrading should only be done when actually need to be done.
        final int configurationVersion = sp.getInt(CONFIGURATION_VERSION, CONFIGURATION_LEVEL_VALUE);

        if (configurationVersion < 1) {
            boolean oldLandscapeFullScreenValue = sp.getBoolean("fullscreen_input_connection_supported",
                    mContext.getResources().getBoolean(R.bool.settings_default_landscape_fullscreen));
            Logger.i(TAG, "Replacing landscape-fullscreen key...");
            Editor e = sp.edit();
            e.putBoolean(mContext.getString(R.string.settings_key_landscape_fullscreen), oldLandscapeFullScreenValue);
            e.remove("fullscreen_input_connection_supported");
            SharedPreferencesCompat.EditorCompat.getInstance().apply(e);
        }

        if (configurationVersion < 2) {
            Logger.i(TAG, "Resetting key height factor...");
            Editor e = sp.edit();
            e.putString("zoom_factor_keys_in_portrait", mContext.getString(R.string.settings_default_portrait_keyboard_height_factor));
            e.putString("zoom_factor_keys_in_landscape", mContext.getString(R.string.settings_default_landscape_keyboard_height_factor));
            SharedPreferencesCompat.EditorCompat.getInstance().apply(e);
        }

        //3 was removed due to refactor

        if (configurationVersion < 4) {
            Editor e = sp.edit();
            Logger.i(TAG, "Resetting key landscape fullscreen...");
            //this is done since some people have phones (which are full-screen ON) and tablets (which are full-screen OFF),
            //and the settings get over-written by BackupAgent
            e.putBoolean(mContext.getString(R.string.settings_key_landscape_fullscreen), mContext.getResources().getBoolean(R.bool.settings_default_landscape_fullscreen));
            SharedPreferencesCompat.EditorCompat.getInstance().apply(e);
        }

        if (configurationVersion < 5) {
            Editor e = sp.edit();
            Logger.i(TAG, "Resetting RTL drawing workaround...");
            //read issue https://github.com/AnySoftKeyboard/AnySoftKeyboard/issues/110
            e.putBoolean(mContext.getString(R.string.settings_key_workaround_disable_rtl_fix),
                    getAlwaysUseDrawTextDefault());
            SharedPreferencesCompat.EditorCompat.getInstance().apply(e);
        }

        if (configurationVersion < 6) {
            Editor e = sp.edit();
            Logger.i(TAG, "Resetting settings_default_allow_suggestions_restart...");
            //read issue https://github.com/AnySoftKeyboard/AnySoftKeyboard/issues/299
            e.remove(mContext.getString(R.string.settings_key_allow_suggestions_restart));
            SharedPreferencesCompat.EditorCompat.getInstance().apply(e);
        }

        //7 was removed due to refactor

        if (configurationVersion < 8) {
            final boolean autoPick = sp.getBoolean("auto_complete", true);
            Editor e = sp.edit();
            Logger.i(TAG, "Converting auto_complete to settings_key_next_word_suggestion_aggressiveness...");
            //read issue https://github.com/AnySoftKeyboard/AnySoftKeyboard/issues/510
            e.remove("auto_complete");
            if (autoPick) {
                e.putString(mContext.getString(R.string.settings_key_next_word_suggestion_aggressiveness),
                        mContext.getString(R.string.settings_default_auto_pick_suggestion_aggressiveness));
                Logger.i(TAG, "settings_key_next_word_suggestion_aggressiveness is ON...");
            } else {
                e.putString(mContext.getString(R.string.settings_key_next_word_suggestion_aggressiveness), "none");
                Logger.i(TAG, "settings_key_next_word_suggestion_aggressiveness is OFF...");
            }
            SharedPreferencesCompat.EditorCompat.getInstance().apply(e);
        }

        if (configurationVersion < 9) {
            final boolean swapSpace = sp.getString("settings_key_should_swap_punctuation_and_space", "yes").equals("yes");
            Editor e = sp.edit();
            Logger.i(TAG, "Converting settings_key_should_swap_punctuation_and_space to settings_key_bool_should_swap_punctuation_and_space...");
            e.remove("settings_key_should_swap_punctuation_and_space");
            e.putBoolean(mContext.getString(R.string.settings_key_bool_should_swap_punctuation_and_space), swapSpace);
            SharedPreferencesCompat.EditorCompat.getInstance().apply(e);
        }
        //10 was removed due to refactor

        if (configurationVersion < 11) {
            //converting quick-text-key
            //settings_key_active_quick_text_key value -> quick_text_[value]
            final Editor editor = sp.edit();
            final Map<String, ?> allValues = sp.getAll();

            //QUICK-TEXT
            if (allValues.containsKey("settings_key_ordered_active_quick_text_keys")) {
                String orderedIds = allValues.get("settings_key_ordered_active_quick_text_keys").toString();
                //order
                editor.putString("quick_text_AddOnsFactory_order_key", orderedIds);
                //enabled
                String[] addonIds = orderedIds.split(",");
                for (String addonId : addonIds) {
                    editor.putBoolean("quick_text_" + addonId, true);
                }
            }

            //THEME
            if (allValues.containsKey("settings_key_keyboard_theme_key")) {
                String themeId = allValues.get("settings_key_keyboard_theme_key").toString();
                //enabled
                editor.putBoolean("theme_" + themeId, true);
            }

            //bottom row
            if (allValues.containsKey("settings_key_ext_kbd_bottom_row_key")) {
                String id = allValues.get("settings_key_ext_kbd_bottom_row_key").toString();
                //enabled
                editor.putBoolean("ext_kbd_enabled_1_" + id, true);
            }

            //top row
            if (allValues.containsKey("settings_key_ext_kbd_top_row_key")) {
                String id = allValues.get("settings_key_ext_kbd_top_row_key").toString();
                //enabled
                editor.putBoolean("ext_kbd_enabled_2_" + id, true);
            }

            //ext keyboard
            if (allValues.containsKey("settings_key_ext_kbd_ext_ketboard_key")) {
                String id = allValues.get("settings_key_ext_kbd_ext_ketboard_key").toString();
                //enabled
                editor.putBoolean("ext_kbd_enabled_3_" + id, true);
            }

            SharedPreferencesCompat.EditorCompat.getInstance().apply(editor);
        }

        //saving config level
        Editor e = sp.edit();
        e.putInt(CONFIGURATION_VERSION, CONFIGURATION_LEVEL_VALUE);
        SharedPreferencesCompat.EditorCompat.getInstance().apply(e);
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

        //statistics
        mFirstAppVersionInstalled = sp.getInt(mContext.getString(R.string.settings_key_first_app_version_installed), 0);
        mFirstTimeAppInstalled = sp.getLong(mContext.getString(R.string.settings_key_first_time_app_installed), 0);
        mFirstTimeCurrentVersionInstalled = sp.getLong(mContext.getString(R.string.settings_key_first_time_current_version_installed), 0);

        //now real settings
        mDomainText = sp.getString("default_domain_text", ".com");
        Logger.d(TAG, "** mDomainText: " + mDomainText);

        mShowKeyPreview = sp.getBoolean(mContext.getString(R.string.settings_key_key_press_shows_preview_popup),
                mContext.getResources().getBoolean(R.bool.settings_default_key_press_shows_preview_popup));
        Logger.d(TAG, "** mShowKeyPreview: " + mShowKeyPreview);

        mKeyPreviewAboveKey = sp.getString(mContext.getString(R.string.settings_key_key_press_preview_popup_position),
                mContext.getString(R.string.settings_default_key_press_preview_popup_position)).equals("above_key");
        Logger.d(TAG, "** mKeyPreviewAboveKey: " + mKeyPreviewAboveKey);

        mShowKeyboardNameText = sp.getBoolean(mContext.getString(R.string.settings_key_show_keyboard_name_text_key),
                mContext.getResources().getBoolean(R.bool.settings_default_show_keyboard_name_text_value));
        Logger.d(TAG, "** mShowKeyboardNameText: " + mShowKeyboardNameText);

        mShowHintTextOnKeys = sp.getBoolean(mContext.getString(R.string.settings_key_show_hint_text_key),
                mContext.getResources().getBoolean(R.bool.settings_default_show_hint_text_value));
        Logger.d(TAG, "** mShowHintTextOnKeys: " + mShowHintTextOnKeys);

        // preferences to override theme's hint position
        mUseCustomHintAlign = sp.getBoolean(mContext.getString(R.string.settings_key_use_custom_hint_align_key),
                mContext.getResources().getBoolean(R.bool.settings_default_use_custom_hint_align_value));
        Logger.d(TAG, "** mUseCustomHintAlign: " + mUseCustomHintAlign);
        mCustomHintAlign = getIntFromString(sp,
                mContext.getString(R.string.settings_key_custom_hint_align_key),
                mContext.getString(R.string.settings_default_custom_hint_align_value));
        Logger.d(TAG, "** mCustomHintAlign: " + mCustomHintAlign);
        mCustomHintVAlign = getIntFromString(sp,
                mContext.getString(R.string.settings_key_custom_hint_valign_key),
                mContext.getString(R.string.settings_default_custom_hint_valign_value));
        Logger.d(TAG, "** mCustomHintVAlign: " + mCustomHintVAlign);

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

        mSwipeDistanceThreshold = getIntFromString(sp,
                mContext.getString(R.string.settings_key_swipe_distance_threshold),
                mContext.getString(R.string.settings_default_swipe_distance_threshold));
        Logger.d(TAG, "** mSwipeDistanceThreshold: " + mSwipeDistanceThreshold);
        mSwipeVelocityThreshold = getIntFromString(sp,
                mContext.getString(R.string.settings_key_swipe_velocity_threshold),
                mContext.getString(R.string.settings_default_swipe_velocity_threshold));
        Logger.d(TAG, "** mSwipeVelocityThreshold: " + mSwipeVelocityThreshold);

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

        mUseChewbacca = sp.getBoolean(mContext.getString(R.string.settings_key_show_chewbacca),
                mContext.getResources().getBoolean(R.bool.settings_default_show_chewbacca));
        Logger.d(TAG, "** mUseChewbacca: " + mUseChewbacca);

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

        mAlwaysUseFallBackUserDictionary = sp.getBoolean(mContext.getString(R.string.settings_key_always_use_fallback_user_dictionary),
                mContext.getResources().getBoolean(R.bool.settings_default_always_use_fallback_user_dictionary));
        Logger.d(TAG, "** mAlwaysUseFallBackUserDictionary: " + mAlwaysUseFallBackUserDictionary);

        mAutomaticallySwitchToAppLayout = sp.getBoolean(mContext.getString(R.string.settings_key_persistent_layout_per_package_id),
                mContext.getResources().getBoolean(R.bool.settings_default_persistent_layout_per_package_id));
        Logger.d(TAG, "** mAutomaticallySwitchToAppLayout: " + mAutomaticallySwitchToAppLayout);

        mAlwaysHideLanguageKey = sp.getBoolean(mContext.getString(R.string.settings_key_always_hide_language_key),
                mContext.getResources().getBoolean(R.bool.settings_default_always_hide_language_key));
        Logger.d(TAG, "** mAlwaysHideLanguageKey: " + mAutomaticallySwitchToAppLayout);

        for (int rowModeIndex = 0; rowModeIndex < mEnableStateForRowModes.length; rowModeIndex++) {
            mEnableStateForRowModes[rowModeIndex] = sp.getBoolean(AskPrefs.ROW_MODE_ENABLED_PREFIX + (rowModeIndex + 2), true);
        }
        //Some preferences cause rebuild of the keyboard, hence changing the listeners list
        final LinkedList<OnSharedPreferenceChangeListener> disconnectedList = new LinkedList<>(mPreferencesChangedListeners);
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
    public String getDomainText() {
        return mDomainText;
    }

    @Override
    public boolean getShowKeyPreview() {
        return mShowKeyPreview;
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
    public int getSwipeDistanceThreshold() {
        return mSwipeDistanceThreshold;
    }

    @Override
    public int getSwipeVelocityThreshold() {
        return mSwipeVelocityThreshold;
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
    public boolean getShowHintTextOnKeys() {
        return mShowHintTextOnKeys;
    }

    @Override
    public boolean getUseCustomHintAlign() {
        return mUseCustomHintAlign;
    }

    @Override
    public int getCustomHintAlign() {
        return mCustomHintAlign;
    }

    @Override
    public int getCustomHintVAlign() {
        return mCustomHintVAlign;
    }

    @Override
    public boolean alwaysHideLanguageKey() {
        return mAlwaysHideLanguageKey;
    }

    @Override
    public boolean getShowKeyboardNameText() {
        return mShowKeyboardNameText;
    }

    @Override
    public boolean useChewbaccaNotifications() {
        return mUseChewbacca;
    }

    @Override
    public boolean showKeyPreviewAboveKey() {
        return mKeyPreviewAboveKey;
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
    public int getFirstAppVersionInstalled() {
        return mFirstAppVersionInstalled;
    }

    @Override
    public long getFirstTimeAppInstalled() {
        return mFirstTimeAppInstalled;
    }

    @Override
    public long getTimeCurrentVersionInstalled() {
        return mFirstTimeCurrentVersionInstalled;
    }

    @Override
    public boolean alwaysUseFallBackUserDictionary() {
        return mAlwaysUseFallBackUserDictionary;
    }

    @Override
    public boolean getPersistLayoutForPackageId() {
        return mAutomaticallySwitchToAppLayout;
    }
}