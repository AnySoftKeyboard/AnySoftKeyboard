package com.anysoftkeyboard;

import java.util.LinkedList;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.utils.Workarounds;

import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;

import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

public class ConfigurationImpl implements Configuration, OnSharedPreferenceChangeListener
{
	static final String TAG = "ASK_Cfg";

	private static final String CONFIGURATION_VERSION = "configurationVersion";
	//private static final String CUSTOMIZATION_LEVEL = "customizationLevel";
	private final Context mContext;
	
	private String mDomainText = ".com";
	//private String mLayoutChangeKeysSize = "Small";
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
	private float mKeysHeightFactorInPortrait = 1.0f;
	private float mKeysHeightFactorInLandscape = 1.0f;
	private boolean mInsertSpaceAfterCandidatePick = true;
	private int mSwipeDistanceThreshold = 240;
	private int mSwipeVelocityThreshold = 400;
	private int mSwipeUpKeyCode;
	private int mSwipeUpFromSpacebarKeyCode;
	private int mSwipeDownKeyCode;
	private int mSwipeLeftKeyCode;
	private int mSwipeRightKeyCode;
	private int mPinchKeyCode;
	private int mSeparateKeyCode;
	private boolean mActionKeyInvisibleWhenRequested = false;
	//private String mRtlWorkaround ="auto";
	private boolean mIsDoubleSpaceChangesToPeroid = true;
	private boolean mShouldPopupForLanguageSwitch = false;
	private boolean mHideSoftKeyboardWhenPhysicalKeyPressed = true;
	private boolean mShowVersionNotification = true;
	private boolean mShowTipsNotification = true;
	private boolean mUse16KeysSymbolsKeyboard = false;
	private boolean mUseBackword = true;
//		private boolean mShowIconForSmileyKey = false;
	private boolean mCycleOverAllSymbolsKeyboard = true;
	private boolean mUseVolumeKeyForLeftRight = false;
	private boolean mUseCameraKeyForBackspaceBackword = false;
	private boolean mUseContactsDictionary = true;
	private boolean mUseAutoDictionary = true;
	private boolean mIsStickyExtensionKeyboard = false;
	private boolean mDrawExtensionKeyboardAboveMainKeyboard = true;
	private Configuration.AnimationsLevel mAnimationsLevel = AnimationsLevel.Full;
	private int mLongPressTimeout = 350;
	private int mMultiTapTimeout = 700;
	
	private boolean mWorkaround_alwaysUseDrawText = false;
	
	private String mInitialKeyboardSplitState = "merged_always";
	
	private final LinkedList<OnSharedPreferenceChangeListener> mPreferencesChangedListeners = new LinkedList<SharedPreferences.OnSharedPreferenceChangeListener>();
	
	public ConfigurationImpl(Context context)
	{
		mContext = context;
		
		//Log.i(TAG, "** Locale:"+ mIme.getResources().getConfiguration().locale.toString());
		String version = "NONE";
		int releaseNumber = 0;
        try {
			PackageInfo info = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
			version = info.versionName;
			releaseNumber = info.versionCode;
			//Log.i(TAG, "** Version: "+version);
		} catch (NameNotFoundException e) {
			Log.e(TAG, "Failed to locate package information! This is very weird... I'm installed.");
		}
		
		Log.i(TAG, "** Version: "+version);
		Log.i(TAG, "** Release code: "+releaseNumber);
		Log.i(TAG, "** Debug: "+AnyApplication.DEBUG);
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
		customizeSettingValues(mContext.getApplicationContext(), sp);
		upgradeSettingsValues(sp);
		
		onSharedPreferenceChanged(sp, "");
	}
	
	private void customizeSettingValues(Context context, SharedPreferences sp) {
//			final int customizationLevel = sp.getInt(CUSTOMIZATION_LEVEL, 0);
//			if (customizationLevel < 1)
//			{
//				Editor e = sp.edit();
//				
//				e.putBoolean(context.getString(R.string.settings_key_lang_key_shows_popup), true);
//				e.putBoolean(context.getString(R.string.settings_key_show_version_notification), false);
//				e.putBoolean(context.getString(R.string.settings_key_use_16_keys_symbols_keyboards), true);
//				e.putBoolean(context.getString(R.string.settings_key_landscape_fullscreen), true);
//				e.putBoolean(context.getString(R.string.settings_key_portrait_fullscreen), true);
//				//enabling 16keys, disabling english
//				e.putBoolean("keyboard_12335055-4aa6-49dc-8456-c7d38a1a5123", true);
//				e.putBoolean("keyboard_c7535083-4fe6-49dc-81aa-c5438a1a343a", false);
//				
//				//enabling external Hebrew
//				e.putBoolean("keyboard_8958fb12-6558-4e96-9aa6-0e90101570b3", true);
//				
//				//sound on
//				e.putBoolean(context.getString(R.string.settings_key_sound_on), true);
//				//custom volume: mid
//				e.putBoolean("use_custom_sound_volume", true);
//				e.putInt("custom_sound_volume", 50);
//				//vibrate on (hard)
//				e.putString(context.getString(R.string.settings_key_vibrate_on_key_press_duration), "50");
//				//no RTL fixes
//				e.putString("rtl_workaround_detection", "no_workaround");
//				//no backword
//				e.putBoolean(context.getString(R.string.settings_key_use_backword), false);
//				//portrait height
//				e.putString("zoom_factor_keys_in_portrait", "1.4");
//				//saving customization level
//				e.putInt(CUSTOMIZATION_LEVEL, 1);
//				e.commit();
//			}
	}

	private void upgradeSettingsValues(SharedPreferences sp) {
	    if (AnyApplication.DEBUG)Log.d(TAG, "Checking if configuration upgrade is needed.");
//		String topRowNewIdValue = sp.getString(mContext.getString(R.string.settings_key_top_keyboard_row_id), null);
//		String topRowOldIdValue = sp.getString("keyboard_layout_change_method", null);
//		if (topRowNewIdValue == null && topRowOldIdValue != null)
//		{
//		    if (AnyApplication.DEBUG)Log.d(TAG, "Top row type is using the old configuration key. Switching...");
//			Editor e = sp.edit();
//			e.putString(mContext.getString(R.string.settings_key_top_keyboard_row_id), topRowOldIdValue);
//			e.remove("keyboard_layout_change_method");
//			e.commit();
//		}
		
	    //please note: the default value should be the last version.
	    //upgrading should only be done when actually need to be done.
		int configurationVersion = sp.getInt(CONFIGURATION_VERSION, 4);
		if (configurationVersion < 1)
		{
			boolean oldLandscapeFullScreenValue = sp.getBoolean("fullscreen_input_connection_supported", 
					mContext.getResources().getBoolean(R.bool.settings_default_landscape_fullscreen));
			if (AnyApplication.DEBUG)Log.d(TAG, "Replacing landscape-fullscreen key...");
			Editor e = sp.edit();
			e.putBoolean(mContext.getString(R.string.settings_key_landscape_fullscreen), oldLandscapeFullScreenValue);
			e.remove("fullscreen_input_connection_supported");
			//saving config level
			e.putInt(CONFIGURATION_VERSION, 1);
			e.commit();
		}
		
		if (configurationVersion < 2)
		{
			if (AnyApplication.DEBUG)Log.d(TAG, "Reseting key height factor...");
			Editor e = sp.edit();
			e.putString("zoom_factor_keys_in_portrait", mContext.getString(R.string.settings_default_portrait_keyboard_height_factor));
			e.putString("zoom_factor_keys_in_landscape", mContext.getString(R.string.settings_default_landscape_keyboard_height_factor));
			//saving config level
			e.putInt(CONFIGURATION_VERSION, 2);
			e.commit();
		}
		
		if (configurationVersion < 3)
		{
			Editor e = sp.edit();
			if (Workarounds.getApiLevel() <= 7)
			{
				if (AnyApplication.DEBUG)Log.d(TAG, "In API7 or lower, bottom row needs to be changed to not include mic...");
				final String bottomRowKey = mContext.getString(R.string.settings_key_ext_kbd_bottom_row_key);
				String currentBottomRowId = sp.getString(bottomRowKey, mContext.getString(R.string.settings_default_ext_kbd_bottom_row_key));
				String newBottomRowId = "";
				if (currentBottomRowId.equals("09f8f280-dee2-11e0-9572-0800200c9a66"))
				{
					newBottomRowId = "09f8f280-dee2-11e0-9572-0800200c9a55";
				}
				else if (currentBottomRowId.equals("3659b9e0-dee2-11e0-9572-0800200c9a66"))
				{
					newBottomRowId = "3659b9e0-dee2-11e0-9572-0800200c9a55";
				}
				if (!TextUtils.isEmpty(newBottomRowId))
				{
					Log.i(TAG, "Detected API7 (or lower). Switching bottom row from "+currentBottomRowId+" to "+newBottomRowId+"...");
					e.putString(bottomRowKey, newBottomRowId);
				}
			}
			//saving config level
			e.putInt(CONFIGURATION_VERSION, 3);
			e.commit();
		}
		
		if (configurationVersion < 4)
		{
			Editor e = sp.edit();
			if (AnyApplication.DEBUG)Log.d(TAG, "Reseting key landscape fullscreen...");
			//this is done since some people have phones (which are full-screen ON) and tablets (which are full-screen OFF),
			//and the settings get overriden by BackupAgent
			e.putBoolean(mContext.getString(R.string.settings_key_landscape_fullscreen), mContext.getResources().getBoolean(R.bool.settings_default_landscape_fullscreen));
			//saving config level
			e.putInt(CONFIGURATION_VERSION, 4);
			e.commit();
		}
	}
	
    public void addChangedListener(OnSharedPreferenceChangeListener listener) {
		mPreferencesChangedListeners.add(listener);
	}
	
	public void removeChangedListener(OnSharedPreferenceChangeListener listener) {
		mPreferencesChangedListeners.remove(listener);
	}
	
	public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
		Log.i(TAG, "**** onSharedPreferenceChanged: ");
		
		mDomainText = sp.getString("default_domain_text", ".com");
		Log.i(TAG, "** mDomainText: "+mDomainText);
		
		mShowKeyPreview = sp.getBoolean(mContext.getString(R.string.settings_key_key_press_shows_preview_popup), 
				mContext.getResources().getBoolean(R.bool.settings_default_key_press_shows_preview_popup));
		Log.i(TAG, "** mShowKeyPreview: "+mShowKeyPreview);

		mKeyPreviewAboveKey = sp.getString(mContext.getString(R.string.settings_key_key_press_preview_popup_position), 
				mContext.getString(R.string.settings_default_key_press_preview_popup_position)).equals("above_key");
		Log.i(TAG, "** mKeyPreviewAboveKey: "+mKeyPreviewAboveKey);
		
		mShowKeyboardNameText = sp.getBoolean(mContext.getString(R.string.settings_key_show_keyboard_name_text_key), 
				mContext.getResources().getBoolean(R.bool.settings_default_show_keyboard_name_text_value));
		Log.i(TAG, "** mShowKeyboardNameText: "+mShowKeyboardNameText);
		
		mShowHintTextOnKeys = sp.getBoolean(mContext.getString(R.string.settings_key_show_hint_text_key), 
				mContext.getResources().getBoolean(R.bool.settings_default_show_hint_text_value));
		Log.i(TAG, "** mShowHintTextOnKeys: "+mShowHintTextOnKeys);
		
		// preferences to override theme's hint position
		mUseCustomHintAlign = sp.getBoolean(mContext.getString(R.string.settings_key_use_custom_hint_align_key),
				mContext.getResources().getBoolean(R.bool.settings_default_use_custom_hint_align_value));
		Log.i(TAG, "** mUseCustomHintAlign: "+mUseCustomHintAlign);
        mCustomHintAlign = getIntFromString(sp, 
        		mContext.getString(R.string.settings_key_custom_hint_align_key),
                mContext.getString(R.string.settings_default_custom_hint_align_value));
        Log.i(TAG, "** mCustomHintAlign: " + mCustomHintAlign);
        mCustomHintVAlign = getIntFromString(sp, 
        		mContext.getString(R.string.settings_key_custom_hint_valign_key),
                mContext.getString(R.string.settings_default_custom_hint_valign_value));
        Log.i(TAG, "** mCustomHintVAlign: " + mCustomHintVAlign);
 
		mSwitchKeyboardOnSpace = sp.getBoolean("switch_keyboard_on_space", false);
		Log.i(TAG, "** mSwitchKeyboardOnSpace: "+mSwitchKeyboardOnSpace);
		
		mUseFullScreenInputInLandscape = sp.getBoolean(mContext.getString(R.string.settings_key_landscape_fullscreen), 
				mContext.getResources().getBoolean(R.bool.settings_default_landscape_fullscreen));
		Log.i(TAG, "** mUseFullScreenInputInLandscape: "+mUseFullScreenInputInLandscape);
		
		mUseFullScreenInputInPortrait = sp.getBoolean(mContext.getString(R.string.settings_key_portrait_fullscreen), 
				mContext.getResources().getBoolean(R.bool.settings_default_portrait_fullscreen));
		Log.i(TAG, "** mUseFullScreenInputInPortrait: "+mUseFullScreenInputInPortrait);
		
		// Fix issue 185
		mUseKeyRepeat = sp.getBoolean("use_keyrepeat", true);
		Log.i(TAG, "** mUseKeyRepeat: "+mUseKeyRepeat);
		
		mKeysHeightFactorInPortrait = getFloatFromString(sp, "zoom_factor_keys_in_portrait", mContext.getString(R.string.settings_default_portrait_keyboard_height_factor));
		Log.i(TAG, "** mKeysHeightFactorInPortrait: "+mKeysHeightFactorInPortrait);
		if (mKeysHeightFactorInPortrait > 2.0f)
		{
			mKeysHeightFactorInPortrait = 2.0f;
			Log.i(TAG, "** mKeysHeightFactorInPortrait fixed to: "+mKeysHeightFactorInPortrait);
		}
		else if (mKeysHeightFactorInPortrait < 0.2f)
		{
			mKeysHeightFactorInPortrait = 0.2f;
			Log.i(TAG, "** mKeysHeightFactorInPortrait fixed to: "+mKeysHeightFactorInPortrait);
		}
		mKeysHeightFactorInLandscape = getFloatFromString(sp, "zoom_factor_keys_in_landscape", mContext.getString(R.string.settings_default_landscape_keyboard_height_factor));
		Log.i(TAG, "** mKeysHeightFactorInLandscape: "+mKeysHeightFactorInLandscape);
		if (mKeysHeightFactorInLandscape > 2.0f)
		{
			mKeysHeightFactorInLandscape = 2.0f;
			Log.i(TAG, "** mKeysHeightFactorInLandscape fixed to: "+mKeysHeightFactorInLandscape);
		}
		else if (mKeysHeightFactorInPortrait < 0.2f)
		{
			mKeysHeightFactorInPortrait = 0.2f;
			Log.i(TAG, "** mKeysHeightFactorInPortrait fixed to: "+mKeysHeightFactorInLandscape);
		}
		
		mInsertSpaceAfterCandidatePick = sp.getBoolean("insert_space_after_word_suggestion_selection", true);
		Log.i(TAG, "** mInsertSpaceAfterCandidatePick: "+mInsertSpaceAfterCandidatePick);
		
		mSwipeUpKeyCode = getIntFromSwipeConfiguration(sp, "swipe_up_action", "shift");
		Log.i(TAG, "** mSwipeUpKeyCode: "+mSwipeUpKeyCode);
		
		mSwipeUpFromSpacebarKeyCode = getIntFromSwipeConfiguration(sp, "swipe_up_from_spacebar_action", "utility_keyboard");
		Log.i(TAG, "** mSwipeUpFromSpacebarKeyCode: "+mSwipeUpFromSpacebarKeyCode);
		
		mSwipeDownKeyCode = getIntFromSwipeConfiguration(sp, "swipe_down_action", "hide");
		Log.i(TAG, "** mSwipeDownKeyCode: "+mSwipeDownKeyCode);
		
		mSwipeLeftKeyCode = getIntFromSwipeConfiguration(sp, "swipe_left_action", "next_symbols");
		Log.i(TAG, "** mSwipeLeftKeyCode: "+mSwipeLeftKeyCode);
		
		mSwipeRightKeyCode = getIntFromSwipeConfiguration(sp, "swipe_right_action", "next_alphabet");
		Log.i(TAG, "** mSwipeRightKeyCode: "+mSwipeRightKeyCode);
		
		mPinchKeyCode = getIntFromSwipeConfiguration(sp, "pinch_gesture_action", "merge_layout");
		Log.i(TAG, "** mPinchKeyCode: "+mPinchKeyCode);
		
		mSeparateKeyCode = getIntFromSwipeConfiguration(sp, "separate_gesture_action", "split_layout");
		Log.i(TAG, "** mSeparateKeyCode: "+mSeparateKeyCode);
		
		mActionKeyInvisibleWhenRequested = sp.getBoolean("action_key_invisible_on_disable", false);
		Log.i(TAG, "** mActionKeyInvisibleWhenRequested: "+mActionKeyInvisibleWhenRequested);
		
		/*mRtlWorkaround = sp.getString("rtl_workaround_detection", "auto");
		Log.i(TAG, "** mRtlWorkaround: "+mRtlWorkaround);
		*/
		mIsDoubleSpaceChangesToPeroid = sp.getBoolean("double_space_to_period", true);
		Log.i(TAG, "** mIsDoubleSpaceChangesToPeroid: "+mIsDoubleSpaceChangesToPeroid);
		
		mShouldPopupForLanguageSwitch = sp.getBoolean(mContext.getString(R.string.settings_key_lang_key_shows_popup),
				mContext.getResources().getBoolean(R.bool.settings_default_lang_key_shows_popup));
		Log.i(TAG, "** mShouldPopupForLanguageSwitch: "+mShouldPopupForLanguageSwitch);
		
		mHideSoftKeyboardWhenPhysicalKeyPressed = sp.getBoolean(mContext.getString(R.string.settings_key_hide_soft_when_physical), 
				mContext.getResources().getBoolean(R.bool.settings_default_hide_soft_when_physical));
		Log.i(TAG, "** mHideSoftKeyboardWhenPhysicalKeyPressed: "+mHideSoftKeyboardWhenPhysicalKeyPressed);

		mShowVersionNotification = sp.getBoolean(mContext.getString(R.string.settings_key_show_version_notification),
				mContext.getResources().getBoolean(R.bool.settings_default_show_version_notification));
		Log.i(TAG, "** mShowVersionNotification: "+mShowVersionNotification);
		
		mShowTipsNotification = sp.getBoolean(mContext.getString(R.string.settings_key_show_tips_notification),
				mContext.getResources().getBoolean(R.bool.settings_default_show_tips_notification));
		Log.i(TAG, "** mShowTipsNotification: "+mShowTipsNotification);
		
		mUse16KeysSymbolsKeyboard = sp.getBoolean(mContext.getString(R.string.settings_key_use_16_keys_symbols_keyboards),
				mContext.getResources().getBoolean(R.bool.settings_default_use_16_keys_symbols_keyboards));
		Log.i(TAG, "** mUse16KeysSymbolsKeyboard: "+mUse16KeysSymbolsKeyboard);
	
		mUseBackword = sp.getBoolean(mContext.getString(R.string.settings_key_use_backword),
				mContext.getResources().getBoolean(R.bool.settings_default_use_backword));
		Log.i(TAG, "** mUseBackword: "+mUseBackword);
		
/*			mShowIconForSmileyKey = sp.getBoolean(mContext.getString(R.string.settings_key_smiley_icon_on_smileys_key),
					mContext.getResources().getBoolean(R.bool.settings_default_smiley_icon_on_smileys_key));
			Log.i(TAG, "** mShowIconForSmileyKey: "+mShowIconForSmileyKey);*/
		
		mCycleOverAllSymbolsKeyboard = sp.getBoolean(mContext.getString(R.string.settings_key_cycle_all_symbols),
				mContext.getResources().getBoolean(R.bool.settings_default_cycle_all_symbols));
		Log.i(TAG, "** mCycleOverAllSymbolsKeyboard: "+mCycleOverAllSymbolsKeyboard);
		
		
	    mUseCameraKeyForBackspaceBackword = sp.getBoolean(mContext.getString(R.string.settings_key_use_camera_key_for_backspace_backword),
                    mContext.getResources().getBoolean(R.bool.settings_default_use_camera_key_for_backspace_backword));
	    Log.i(TAG, "** mUseCameraKeyForBackspaceBackword: "+mUseCameraKeyForBackspaceBackword);
            
        mUseVolumeKeyForLeftRight = sp.getBoolean(mContext.getString(R.string.settings_key_use_volume_key_for_left_right),
                    mContext.getResources().getBoolean(R.bool.settings_default_use_volume_key_for_left_right));
        Log.i(TAG, "** mUseVolumeKeyForLeftRight: "+mUseVolumeKeyForLeftRight);
            
        mUseContactsDictionary = sp.getBoolean(mContext.getString(R.string.settings_key_use_contacts_dictionary),
                    mContext.getResources().getBoolean(R.bool.settings_default_contacts_dictionary));
        Log.i(TAG, "** mUseContactsDictionary: " + mUseContactsDictionary);

        mUseAutoDictionary = sp.getBoolean(mContext.getString(R.string.settings_key_use_auto_dictionary),
                mContext.getResources().getBoolean(R.bool.settings_default_auto_dictionary));
        Log.i(TAG, "** mUseAutoDictionary: " + mUseAutoDictionary);
		    
        mIsStickyExtensionKeyboard = sp.getBoolean(mContext.getString(R.string.settings_key_is_sticky_extesion_keyboard),
                mContext.getResources().getBoolean(R.bool.settings_default_is_sticky_extesion_keyboard));
        Log.i(TAG, "** mIsStickyExtensionKeyboard: " + mIsStickyExtensionKeyboard);
        
        mDrawExtensionKeyboardAboveMainKeyboard = sp.getBoolean(mContext.getString(R.string.settings_key_is_extesion_keyboard_above_keyboard),
                mContext.getResources().getBoolean(R.bool.settings_default_is_extesion_keyboard_above_keyboard));
        Log.i(TAG, "** mDrawExtensionKeyboardAboveMainKeyboard: " + mDrawExtensionKeyboardAboveMainKeyboard);
        
        mSwipeDistanceThreshold = getIntFromString(sp, 
        		mContext.getString(R.string.settings_key_swipe_distance_threshold),
                mContext.getString(R.string.settings_default_swipe_distance_threshold));
        Log.i(TAG, "** mSwipeDistanceThreshold: " + mSwipeDistanceThreshold);
        mSwipeVelocityThreshold = getIntFromString(sp, 
        		mContext.getString(R.string.settings_key_swipe_velocity_threshold),
                mContext.getString(R.string.settings_default_swipe_velocity_threshold));
        Log.i(TAG, "** mSwipeVelocityThreshold: " + mSwipeVelocityThreshold);
        
        mLongPressTimeout = getIntFromString(sp, 
        		mContext.getString(R.string.settings_key_long_press_timeout),
                mContext.getString(R.string.settings_default_long_press_timeout));
        Log.i(TAG, "** mLongPressTimeout: " + mLongPressTimeout);
        
        mMultiTapTimeout = getIntFromString(sp, 
        		mContext.getString(R.string.settings_key_multitap_timeout),
                mContext.getString(R.string.settings_default_multitap_timeout));
        Log.i(TAG, "** mMultiTapTimeout: " + mMultiTapTimeout);
        
        mWorkaround_alwaysUseDrawText = sp.getBoolean(mContext.getString(R.string.settings_key_workaround_disable_rtl_fix),
                getAlwaysUseDrawTextDefault());
        Log.i(TAG, "** mWorkaround_alwaysUseDrawText: " + mWorkaround_alwaysUseDrawText);
        
        mInitialKeyboardSplitState = sp.getString(mContext.getString(R.string.settings_key_default_split_state), 
        		mContext.getString(R.string.settings_default_default_split_state));
        Log.i(TAG, "** mInitialKeyboardSplitState: " + mInitialKeyboardSplitState);

        mUseChewbacca = sp.getBoolean(mContext.getString(R.string.settings_key_show_chewbacca),
                mContext.getResources().getBoolean(R.bool.settings_default_show_chewbacca));
        Log.i(TAG, "** mUseChewbacca: " + mUseChewbacca);
        
        String shouldSwapType = sp.getString(mContext.getString(R.string.settings_key_should_swap_punctuation_and_space),
                mContext.getString(R.string.settings_default_should_swap_punctuation_and_space));
        mSwapPunctuationAndSpace = shouldSwapType.equals("yes");
        Log.i(TAG, "** mSwapPunctuationAndSpace: " + mSwapPunctuationAndSpace);
        
        String animationsLevel = sp.getString(mContext.getString(R.string.settings_key_tweak_animations_level),
                mContext.getString(R.string.settings_default_tweak_animations_level));
        if ("none".equals(animationsLevel))
            mAnimationsLevel = AnimationsLevel.None;
        else if ("some".equals(animationsLevel))
            mAnimationsLevel = AnimationsLevel.Some;
        else
            mAnimationsLevel = AnimationsLevel.Full;
        Log.i(TAG, "** mAnimationsLevel: " + mAnimationsLevel);
        //Some preferences cause rebuild of the keyboard, hence changing the listeners list
        final LinkedList<OnSharedPreferenceChangeListener> disconnectedList = new LinkedList<SharedPreferences.OnSharedPreferenceChangeListener>(mPreferencesChangedListeners);
		for(OnSharedPreferenceChangeListener listener : disconnectedList)
		{
			listener.onSharedPreferenceChanged(sp, key);
		}
	}

	private boolean getAlwaysUseDrawTextDefault() {
		if (	android.os.Build.BRAND.contains("SEMC")//SE phones have fix for that, but more important, their StaticLayout class is bugged 
				|| Workarounds.getApiLevel() > 11) //Android has native fix for API level 11! Ya
			return true;
		else
			return mContext.getResources().getBoolean(R.bool.settings_default_workaround_disable_rtl_fix);
	}
	
	private int getIntFromSwipeConfiguration(SharedPreferences sp, final String prefKey, final String defaultValue) {
		final String keyValue = sp.getString(prefKey, defaultValue);
		
		if (keyValue.equalsIgnoreCase("next_alphabet"))
			return KeyCodes.MODE_ALPHABET;
		else if (keyValue.equalsIgnoreCase("next_symbols"))
			return KeyCodes.MODE_SYMOBLS;
		else if (keyValue.equalsIgnoreCase("cycle_keyboards"))
			return KeyCodes.KEYBOARD_CYCLE;
		else if (keyValue.equalsIgnoreCase("reverse_cycle_keyboards"))
            return KeyCodes.KEYBOARD_REVERSE_CYCLE;
		else if (keyValue.equalsIgnoreCase("shift"))
			return KeyCodes.SHIFT;
		else if (keyValue.equalsIgnoreCase("hide"))
			return KeyCodes.CANCEL;
		else if (keyValue.equalsIgnoreCase("backspace"))
			return KeyCodes.DELETE;
		else if (keyValue.equalsIgnoreCase("backword"))
			return KeyCodes.DELETE_WORD;
		else if (keyValue.equalsIgnoreCase("clear_input"))
			return KeyCodes.CLEAR_INPUT;
		else if (keyValue.equalsIgnoreCase("cursor_up"))
			return KeyCodes.ARROW_UP;
		else if (keyValue.equalsIgnoreCase("cursor_down"))
			return KeyCodes.ARROW_DOWN;
		else if (keyValue.equalsIgnoreCase("cursor_left"))
			return KeyCodes.ARROW_LEFT;
		else if (keyValue.equalsIgnoreCase("cursor_right"))
			return KeyCodes.ARROW_RIGHT;
		else if (keyValue.equalsIgnoreCase("next_inside_mode"))
			return KeyCodes.KEYBOARD_CYCLE_INSIDE_MODE;
		else if (keyValue.equalsIgnoreCase("other_keyboards_mode"))
			return KeyCodes.KEYBOARD_MODE_CHANGE;
		else if (keyValue.equalsIgnoreCase("split_layout"))
			return KeyCodes.SPLIT_LAYOUT;
		else if (keyValue.equalsIgnoreCase("merge_layout"))
			return KeyCodes.MERGE_LAYOUT;
		else if (keyValue.equalsIgnoreCase("utility_keyboard"))
			return KeyCodes.UTILITY_KEYBOARD;
		
		return 0;//0 means no action
	}

	private static float getFloatFromString(SharedPreferences sp, String prefKey, String defaultValue) {
		String floatValue = sp.getString(prefKey, defaultValue);
		try
		{
			return Float.parseFloat(floatValue);
		}
		catch(Exception e)
		{
			try
			{
				return Float.parseFloat(defaultValue);
			}
			catch(Exception e2)
			{
				return 1.0f;
			}
		}
	}
	
	private static int getIntFromString(SharedPreferences sp, String prefKey, String defaultValue) {
		String intValue = sp.getString(prefKey, defaultValue);
		try
		{
			return Integer.parseInt(intValue);
		}
		catch(Exception e)
		{
			try
			{
				return Integer.parseInt(defaultValue);
			}
			catch(Exception e2)
			{
				return 500;
			}
		}
	}

	public String getDomainText() {
		return mDomainText;
	}

//	public String getChangeLayoutKeysSize() {
//		return mLayoutChangeKeysSize;
//	}
//	
	public boolean getShowKeyPreview()
	{
		return mShowKeyPreview;
	}
	
	public boolean getSwitchKeyboardOnSpace()
	{
	    return mSwitchKeyboardOnSpace;
	}

	public boolean getUseFullScreenInputInLandscape() {
		return mUseFullScreenInputInLandscape;
	}
	
	public boolean getUseFullScreenInputInPortrait() {
		return mUseFullScreenInputInPortrait;
	}

	public boolean getUseRepeatingKeys() {
		return mUseKeyRepeat;
	}

	public float getKeysHeightFactorInLandscape() {
		return mKeysHeightFactorInLandscape;
	}

	public float getKeysHeightFactorInPortrait() {
		return mKeysHeightFactorInPortrait;
	}	
	
	public boolean getInsertSpaceAfterCandidatePick() {
		return mInsertSpaceAfterCandidatePick;
	}
	
	public int getGestureSwipeUpKeyCode() {return mSwipeUpKeyCode;}
	public int getGestureSwipeDownKeyCode() {return mSwipeDownKeyCode;}
	public int getGestureSwipeLeftKeyCode() {return mSwipeLeftKeyCode;}
	public int getGestureSwipeRightKeyCode() {return mSwipeRightKeyCode;}
	public int getGesturePinchKeyCode() {return mPinchKeyCode;}
	public int getGestureSeparateKeyCode() {return mSeparateKeyCode;}
	public int getGestureSwipeUpFromSpacebarKeyCode() {return mSwipeUpFromSpacebarKeyCode;}
	
	public boolean getActionKeyInvisibleWhenRequested() {
		return mActionKeyInvisibleWhenRequested;
	}

	public int getDeviceOrientation() {
		return mContext.getApplicationContext().getResources().getConfiguration().orientation;
	}
/*
	public String getRtlWorkaroundConfiguration() {
		return mRtlWorkaround;
	}
*/
	public boolean isDoubleSpaceChangesToPeriod() {
		return mIsDoubleSpaceChangesToPeroid;
	}

	public boolean shouldShowPopupForLanguageSwitch() {
		return mShouldPopupForLanguageSwitch;
	}
	
	public boolean hideSoftKeyboardWhenPhysicalKeyPressed() {
		return mHideSoftKeyboardWhenPhysicalKeyPressed;
	}
	
	public boolean getShowVersionNotification() {
		return mShowVersionNotification;
	}
	
	public void setShowVersionNotification(boolean show) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
		Editor e = sp.edit();
		e.putBoolean(mContext.getString(R.string.settings_key_show_version_notification), show);
		mShowVersionNotification = show;
		e.commit();
	}
	
	public boolean getShowTipsNotification() {
		return mShowTipsNotification;
	}
	
	public void setShowTipsNotification(boolean show) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
		Editor e = sp.edit();
		e.putBoolean(mContext.getString(R.string.settings_key_show_tips_notification), show);
		mShowTipsNotification = show;
		e.commit();
	}
	
	

	public boolean use16KeysSymbolsKeyboards() {
		return mUse16KeysSymbolsKeyboard;
	}
	
	public boolean useBackword() {
		return mUseBackword;
	}
	
/*		@Override
		public boolean showIconForSmileyKey() {
			return mShowIconForSmileyKey;
		}*/
	
	public boolean getCycleOverAllSymbols() {
		return mCycleOverAllSymbolsKeyboard;
	}

    public boolean useCameraKeyForBackspaceBackword() {
        return mUseCameraKeyForBackspaceBackword;
    }

    public boolean useVolumeKeyForLeftRight() {
        return mUseVolumeKeyForLeftRight;
    }

    public boolean useContactsDictionary() {
        return mUseContactsDictionary;
    }

    public boolean useAutoDictionary() {
        return mUseAutoDictionary;
    }	
    
    public boolean isStickyExtensionKeyboard() {
    	return mIsStickyExtensionKeyboard;
    }
    
    public boolean drawExtensionKeyboardAboveMainKeyboard() {
    	return mDrawExtensionKeyboardAboveMainKeyboard;
    }
    
    public int getSwipeDistanceThreshold() {
    	return mSwipeDistanceThreshold;
    }
    
    public int getSwipeVelocityThreshold() {
    	return mSwipeVelocityThreshold;
    }
    
    public int getLongPressTimeout() {
    	return mLongPressTimeout;
    }
    
    public int getMultiTapTimeout() {
    	return mMultiTapTimeout;
    }
    
    public boolean workaround_alwaysUseDrawText() {
    	return mWorkaround_alwaysUseDrawText;
    }
    
    public String getInitialKeyboardSplitState() {
    	return mInitialKeyboardSplitState;
    }
    
    public boolean getShowHintTextOnKeys() {
    	return mShowHintTextOnKeys;
    }
    
    public boolean getUseCustomHintAlign() {
    	return mUseCustomHintAlign;
    }
    
    public int getCustomHintAlign() {
    	return mCustomHintAlign;
    }
    
    public int getCustomHintVAlign() {
    	return mCustomHintVAlign;
    }
    
    public boolean getShowKeyboardNameText() {
    	return mShowKeyboardNameText;
    }
    
    public boolean useChewbaccaNotifications() {
    	return mUseChewbacca;
    }
    
    public boolean showKeyPreviewAboveKey() {
    	return mKeyPreviewAboveKey;
    }
    
    public boolean shouldswapPunctuationAndSpace() {
    	return mSwapPunctuationAndSpace;
    }
    
    public AnimationsLevel getAnimationsLevel() {
        return mAnimationsLevel;
    }
}