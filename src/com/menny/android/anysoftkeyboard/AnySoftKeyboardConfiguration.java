package com.menny.android.anysoftkeyboard;

import com.menny.android.anysoftkeyboard.keyboards.AnyKeyboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.preference.PreferenceManager;
import android.util.Log;

public abstract class AnySoftKeyboardConfiguration 
{
	public static final boolean DEBUG = false;
	
	private static final String TAG = "ASK_Cfg";
	private static final AnySoftKeyboardConfiguration msInstance;
	
	static 
	{
		msInstance = new AnySoftKeyboardConfigurationImpl();
	}
		
	public static AnySoftKeyboardConfiguration getInstance() {return msInstance;}
	
	public abstract String getSmileyText();
	
	public abstract String getDomainText();
	
	public abstract String getChangeLayoutKeysSize();
	
	public abstract boolean getShowKeyPreview();

	public abstract boolean getSwitchKeyboardOnSpace();
	
	public abstract boolean getUseFullScreenInputInLandscape();
	
	public abstract boolean getUseFullScreenInputInPortrait();
	
	public abstract boolean getUseRepeatingKeys();
	
	public abstract float getKeysHeightFactorInPortrait();
	
	public abstract float getKeysHeightFactorInLandscape();
	
	public abstract boolean getInsertSpaceAfterCandidatePick();
	
	public abstract int getSwipeUpKeyCode();
	public abstract int getSwipeDownKeyCode();
	public abstract int getSwipeLeftKeyCode();
	public abstract int getSwipeRightKeyCode();
	
	public abstract boolean getActionKeyInvisibleWhenRequested();
	
	public abstract int getDeviceOrientation();
	
	public abstract String getRtlWorkaroundConfiguration();
	
	public abstract boolean isDoubleSpaceChangesToPeriod();
	
	public abstract boolean shouldShowPopupForLanguageSwitch();
	
	public abstract boolean getShowVersionNotification();
	
	public abstract void setShowVersionNotification(boolean show);
	
	public abstract boolean use16KeysSymbolsKeyboards();
	
	public abstract boolean useBackword();
	
	public abstract boolean showIconForSmileyKey();
	
	static class AnySoftKeyboardConfigurationImpl extends AnySoftKeyboardConfiguration
	{
		private static final String CONFIGURATION_VERSION = "configurationVersion";
		private static final String CUSTOMIZATION_LEVEL = "customizationLevel";
		private InputMethodService mIme;
		
		private String mSmileyText = ":-)";
		private String mDomainText = ".com";
		private String mLayoutChangeKeysSize = "Small";
		private boolean mShowKeyPreview = true;
		private boolean mSwitchKeyboardOnSpace = true;
		private boolean mUseFullScreenInputInLandscape = true;
		private boolean mUseFullScreenInputInPortrait = false;
		private boolean mUseKeyRepeat = true;
		private float mKeysHeightFactorInPortrait = 1.0f;
		private float mKeysHeightFactorInLandscape = 1.0f;
		private boolean mInsertSpaceAfterCandidatePick = true;
		private int mSwipeUpKeyCode;
		private int mSwipeDownKeyCode;
		private int mSwipeLeftKeyCode;
		private int mSwipeRightKeyCode;
		private boolean mActionKeyInvisibleWhenRequested = false;
		private String mRtlWorkaround ="auto";
		private boolean mIsDoubleSpaceChangesToPeroid = true;
		private boolean mShouldPopupForLanguageSwitch = false;
		private boolean mShowVersionNotification = true;
		private boolean mUse16KeysSymbolsKeyboard = false;
		private boolean mUseBackword = true;
		private boolean mShowIconForSmileyKey = false;
		
		public AnySoftKeyboardConfigurationImpl()
		{
			
		}
		
		void initializeConfiguration(InputMethodService ime) 
		{
			mIme = ime;
			
			//Log.i(TAG, "** Locale:"+ mIme.getResources().getConfiguration().locale.toString());
			String version = "NONE";
			int releaseNumber = 0;
	        try {
				PackageInfo info = mIme.getApplication().getPackageManager().getPackageInfo(mIme.getApplication().getPackageName(), 0);
				version = info.versionName;
				releaseNumber = info.versionCode;
				//Log.i(TAG, "** Version: "+version);
			} catch (NameNotFoundException e) {
				Log.e(TAG, "Failed to locate package information! This is very weird... I'm installed.");
			}
			
			Log.i(TAG, "** Version: "+version);
			Log.i(TAG, "** Release code: "+releaseNumber);
			Log.i(TAG, "** Debug: "+DEBUG);
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mIme);
			customizeSettingValues(mIme.getApplicationContext(), sp);
			upgradeSettingsValues(sp);
			
			handleConfigurationChange(sp);
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
			Log.d(TAG, "Checking if configuration upgrade is needed.");
			String topRowNewIdValue = sp.getString(mIme.getString(R.string.settings_key_top_keyboard_row_id), null);
			String topRowOldIdValue = sp.getString("keyboard_layout_change_method", null);
			if (topRowNewIdValue == null && topRowOldIdValue != null)
			{
				Log.d(TAG, "Top row type is using the old configuration key. Switching...");
				Editor e = sp.edit();
				e.putString(mIme.getString(R.string.settings_key_top_keyboard_row_id), topRowOldIdValue);
				e.remove("keyboard_layout_change_method");
				e.commit();
			}
			
			final int configurationVersion = sp.getInt(CONFIGURATION_VERSION, 0);
			if (configurationVersion < 1)
			{
				boolean oldLandscapeFullScreenValue = sp.getBoolean("fullscreen_input_connection_supported", 
						mIme.getResources().getBoolean(R.bool.settings_default_landscape_fullscreen));
				Log.d(TAG, "Replacing landscape-fullscreen key...");
				Editor e = sp.edit();
				e.putBoolean(mIme.getString(R.string.settings_key_landscape_fullscreen), oldLandscapeFullScreenValue);
				e.remove("fullscreen_input_connection_supported");
				//saving config level
				e.putInt(CONFIGURATION_VERSION, 1);
				e.commit();
			}
		}
		
		public void handleConfigurationChange(SharedPreferences sp)
		{
			Log.i(TAG, "**** handleConfigurationChange: ");
			mLayoutChangeKeysSize = sp.getString(mIme.getResources().getString(R.string.settings_key_top_keyboard_row_id), mIme.getResources().getString(R.string.settings_default_top_keyboard_row_id));
			Log.i(TAG, "** mChangeKeysMode: "+mLayoutChangeKeysSize);
			
			mSmileyText = sp.getString("default_smiley_text", ":-) ");
			Log.i(TAG, "** mSmileyText: "+mSmileyText);
			
			mDomainText = sp.getString("default_domain_text", ".com");
			Log.i(TAG, "** mDomainText: "+mDomainText);
			
			mShowKeyPreview = sp.getBoolean("key_press_preview_popup", true);
			Log.i(TAG, "** mShowKeyPreview: "+mShowKeyPreview);

			mSwitchKeyboardOnSpace = sp.getBoolean("switch_keyboard_on_space", false);
			Log.i(TAG, "** mSwitchKeyboardOnSpace: "+mSwitchKeyboardOnSpace);
			
			mUseFullScreenInputInLandscape = sp.getBoolean(mIme.getString(R.string.settings_key_landscape_fullscreen), 
					mIme.getResources().getBoolean(R.bool.settings_default_landscape_fullscreen));
			Log.i(TAG, "** mUseFullScreenInputInLandscape: "+mUseFullScreenInputInLandscape);
			
			mUseFullScreenInputInPortrait = sp.getBoolean(mIme.getString(R.string.settings_key_portrait_fullscreen), 
					mIme.getResources().getBoolean(R.bool.settings_default_portrait_fullscreen));
			Log.i(TAG, "** mUseFullScreenInputInPortrait: "+mUseFullScreenInputInPortrait);
			
			// Fix issue 185
			mUseKeyRepeat = sp.getBoolean("use_keyrepeat", true);
			Log.i(TAG, "** mUseKeyRepeat: "+mUseKeyRepeat);
			
			mKeysHeightFactorInPortrait = getFloatFromString(sp, "zoom_factor_keys_in_portrait");
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
			mKeysHeightFactorInLandscape = getFloatFromString(sp, "zoom_factor_keys_in_landscape");
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
			
			mSwipeDownKeyCode = getIntFromSwipeConfiguration(sp, "swipe_down_action", "hide");
			Log.i(TAG, "** mSwipeDownKeyCode: "+mSwipeDownKeyCode);
			
			mSwipeLeftKeyCode = getIntFromSwipeConfiguration(sp, "swipe_left_action", "next_symbols");
			Log.i(TAG, "** mSwipeLeftKeyCode: "+mSwipeLeftKeyCode);
			
			mSwipeRightKeyCode = getIntFromSwipeConfiguration(sp, "swipe_right_action", "next_alphabet");
			Log.i(TAG, "** mSwipeRightKeyCode: "+mSwipeRightKeyCode);
			
			mActionKeyInvisibleWhenRequested = sp.getBoolean("action_key_invisible_on_disable", false);
			Log.i(TAG, "** mActionKeyInvisibleWhenRequested: "+mActionKeyInvisibleWhenRequested);
			
			mRtlWorkaround = sp.getString("rtl_workaround_detection", "auto");
			Log.i(TAG, "** mRtlWorkaround: "+mRtlWorkaround);
			
			mIsDoubleSpaceChangesToPeroid = sp.getBoolean("double_space_to_period", true);
			Log.i(TAG, "** mIsDoubleSpaceChangesToPeroid: "+mIsDoubleSpaceChangesToPeroid);
			
			mShouldPopupForLanguageSwitch = sp.getBoolean(mIme.getString(R.string.settings_key_lang_key_shows_popup),
					mIme.getResources().getBoolean(R.bool.settings_default_lang_key_shows_popup));
			Log.i(TAG, "** mShouldPopupForLanguageSwitch: "+mShouldPopupForLanguageSwitch);
			
			mShowVersionNotification = sp.getBoolean(mIme.getString(R.string.settings_key_show_version_notification),
					mIme.getResources().getBoolean(R.bool.settings_default_show_version_notification));
			Log.i(TAG, "** mShowVersionNotification: "+mShowVersionNotification);
			
			mUse16KeysSymbolsKeyboard = sp.getBoolean(mIme.getString(R.string.settings_key_use_16_keys_symbols_keyboards),
					mIme.getResources().getBoolean(R.bool.settings_default_use_16_keys_symbols_keyboards));
			Log.i(TAG, "** mUse16KeysSymbolsKeyboard: "+mUse16KeysSymbolsKeyboard);
		
			mUseBackword = sp.getBoolean(mIme.getString(R.string.settings_key_use_backword),
					mIme.getResources().getBoolean(R.bool.settings_default_use_backword));
			Log.i(TAG, "** mUseBackword: "+mUseBackword);
			
			mShowIconForSmileyKey = sp.getBoolean(mIme.getString(R.string.settings_key_smiley_icon_on_smileys_key),
					mIme.getResources().getBoolean(R.bool.settings_default_smiley_icon_on_smileys_key));
			Log.i(TAG, "** mShowIconForSmileyKey: "+mShowIconForSmileyKey);
		}

		/*
public boolean handleConfigurationChange(SharedPreferences sp)
		{
			Log.i(TAG, "**** handleConfigurationChange: ");
			boolean handled = false;
			//if a change in the configuration requires rebuilding the keyboards, 'forceRebuildOfKeyboards' should set to 'true'
			boolean forceRebuildOfKeyboards = false;
			// this change requires the recreation of the keyboards.
			String newLayoutChangeKeysSize = sp.getString(mIme.getResources().getString(R.string.settings_key_keyboard_layout_change_method), mIme.getResources().getString(R.string.settings_default_keyboard_layout_change_method));
			forceRebuildOfKeyboards = forceRebuildOfKeyboards || (!newLayoutChangeKeysSize.equalsIgnoreCase(mLayoutChangeKeysSize));
			mLayoutChangeKeysSize = newLayoutChangeKeysSize;
			Log.i(TAG, "** mChangeKeysMode: "+mLayoutChangeKeysSize);
			
			String newSmileyText = sp.getString("default_smiley_text", ":-) ");
			handled = handled || (!newSmileyText.equals(mSmileyText));
			mSmileyText = newSmileyText;
			Log.i(TAG, "** mSmileyText: "+mSmileyText);
			
			String newDomainText = sp.getString("default_domain_text", ".com");
			handled = handled || (!newDomainText.equals(mDomainText));
			mDomainText = newDomainText;
			Log.i(TAG, "** mDomainText: "+mDomainText);
			
			boolean newShowPreview = sp.getBoolean("key_press_preview_popup", true);
			handled = handled || (newShowPreview != mShowKeyPreview);
			mShowKeyPreview = newShowPreview;
			Log.i(TAG, "** mShowKeyPreview: "+mShowKeyPreview);

			boolean newSwitchKeyboardOnSpace = sp.getBoolean("switch_keyboard_on_space", false);
			handled = handled || (newSwitchKeyboardOnSpace != mSwitchKeyboardOnSpace);
			mSwitchKeyboardOnSpace = newSwitchKeyboardOnSpace;
			Log.i(TAG, "** mSwitchKeyboardOnSpace: "+mSwitchKeyboardOnSpace);
			
			boolean newUseFullScreenInput = sp.getBoolean("fullscreen_input_connection_supported", true);
			handled = handled || (newUseFullScreenInput != mUseFullScreenInput);
			mUseFullScreenInput = newUseFullScreenInput;
			Log.i(TAG, "** mUseFullScreenInput: "+mUseFullScreenInput);
			
			// Fix issue 185
			boolean newUseKeyRepeat = sp.getBoolean("use_keyrepeat", true);
			handled = handled || ( newUseKeyRepeat != mUseKeyRepeat );
			mUseKeyRepeat = newUseKeyRepeat;
			Log.i(TAG, "** mUseKeyRepeat: "+mUseKeyRepeat);
			
			float newKeyHeightFactorPortrait = getFloatFromString(sp, "zoom_factor_keys_in_portrait");
			forceRebuildOfKeyboards = forceRebuildOfKeyboards || ( newKeyHeightFactorPortrait != mKeysHeightFactorInPortrait );
			mKeysHeightFactorInPortrait = newKeyHeightFactorPortrait;
			Log.i(TAG, "** mKeysHeightFactorInPortrait: "+mKeysHeightFactorInPortrait);
			
			float newKeyHeightFactorLandscape = getFloatFromString(sp, "zoom_factor_keys_in_landscape");
			forceRebuildOfKeyboards = forceRebuildOfKeyboards || ( newKeyHeightFactorLandscape != mKeysHeightFactorInLandscape );
			mKeysHeightFactorInLandscape = newKeyHeightFactorLandscape;
			Log.i(TAG, "** mKeysHeightFactorInLandscape: "+mKeysHeightFactorInLandscape);
			
			boolean newInsertSpaceAfterCandidatePick = sp.getBoolean("insert_space_after_word_suggestion_selection", true);
			handled = handled || ( newInsertSpaceAfterCandidatePick != mInsertSpaceAfterCandidatePick );
			mInsertSpaceAfterCandidatePick = newInsertSpaceAfterCandidatePick;
			Log.i(TAG, "** mInsertSpaceAfterCandidatePick: "+mInsertSpaceAfterCandidatePick);
			
			int newSwipeUpValue = getIntFromSwipeConfiguration(sp, "swipe_up_action", "shift");
			handled = handled || ( newSwipeUpValue != mSwipeUpKeyCode );
			mSwipeUpKeyCode = newSwipeUpValue;
			Log.i(TAG, "** mSwipeUpKeyCode: "+mSwipeUpKeyCode);
			
			int newSwipeDownValue = getIntFromSwipeConfiguration(sp, "swipe_down_action", "hide");
			handled = handled || ( newSwipeDownValue != mSwipeDownKeyCode );
			mSwipeDownKeyCode = newSwipeDownValue;
			Log.i(TAG, "** mSwipeDownKeyCode: "+mSwipeDownKeyCode);
			
			int newSwipeLeftValue = getIntFromSwipeConfiguration(sp, "swipe_left_action", "next_symbols");
			handled = handled || ( newSwipeLeftValue != mSwipeLeftKeyCode );
			mSwipeLeftKeyCode = newSwipeLeftValue;
			Log.i(TAG, "** mSwipeLeftKeyCode: "+mSwipeLeftKeyCode);
			
			int newSwipeRightValue = getIntFromSwipeConfiguration(sp, "swipe_right_action", "next_alphabet");
			handled = handled || ( newSwipeRightValue != mSwipeRightKeyCode );
			mSwipeRightKeyCode = newSwipeRightValue;
			Log.i(TAG, "** mSwipeRightKeyCode: "+mSwipeRightKeyCode);
			
			boolean newActionKeyInvisibleWhenRequested = sp.getBoolean("action_key_invisible_on_disable", false);
			handled = handled || ( newActionKeyInvisibleWhenRequested != mActionKeyInvisibleWhenRequested);
			mActionKeyInvisibleWhenRequested = newActionKeyInvisibleWhenRequested;
			Log.i(TAG, "** mActionKeyInvisibleWhenRequested: "+mActionKeyInvisibleWhenRequested);
			
			String newRtlWorkaround = sp.getString("rtl_workaround_detection", "auto");
			handled = handled || (!newRtlWorkaround.equals(mRtlWorkaround));
			mRtlWorkaround = newRtlWorkaround;
			Log.i(TAG, "** mRtlWorkaround: "+mRtlWorkaround);
			
			boolean newIsDoubleSpaceChangesToPeroid = sp.getBoolean("double_space_to_period", true);
			handled = handled || ( newIsDoubleSpaceChangesToPeroid != mIsDoubleSpaceChangesToPeroid);
			mIsDoubleSpaceChangesToPeroid = newIsDoubleSpaceChangesToPeroid;
			Log.i(TAG, "** mIsDoubleSpaceChangesToPeroid: "+mIsDoubleSpaceChangesToPeroid);
			
			boolean newShouldPopupForLanguageSwitch = sp.getBoolean(mIme.getString(R.string.settings_key_lang_key_shows_popup),
					mIme.getResources().getBoolean(R.bool.settings_default_lang_key_shows_popup));
			handled = handled || ( newShouldPopupForLanguageSwitch != mShouldPopupForLanguageSwitch);
			mShouldPopupForLanguageSwitch = newShouldPopupForLanguageSwitch;
			Log.i(TAG, "** mShouldPopupForLanguageSwitch: "+mShouldPopupForLanguageSwitch);
			
			return handled && (!forceRebuildOfKeyboards);
		}
		 */
		
		private int getIntFromSwipeConfiguration(SharedPreferences sp, final String prefKey, final String defaultValue) {
			final String keyValue = sp.getString(prefKey, defaultValue);
			
			if (keyValue.equalsIgnoreCase("next_alphabet"))
				return AnyKeyboard.KEYCODE_LANG_CHANGE;
			else if (keyValue.equalsIgnoreCase("next_symbols"))
				return Keyboard.KEYCODE_MODE_CHANGE;
			else if (keyValue.equalsIgnoreCase("cycle_keyboards"))
				return AnyKeyboard.KEYCODE_KEYBOARD_CYCLE;
			else if (keyValue.equalsIgnoreCase("reverse_cycle_keyboards"))
                return AnyKeyboard.KEYCODE_KEYBOARD_REVERSE_CYCLE;
			else if (keyValue.equalsIgnoreCase("shift"))
				return Keyboard.KEYCODE_SHIFT;
			else if (keyValue.equalsIgnoreCase("hide"))
				return Keyboard.KEYCODE_CANCEL;
			else if (keyValue.equalsIgnoreCase("backspace"))
				return Keyboard.KEYCODE_DELETE;
			else if (keyValue.equalsIgnoreCase("cursor_up"))
				return AnyKeyboard.KEYCODE_UP;
			else if (keyValue.equalsIgnoreCase("cursor_down"))
				return AnyKeyboard.KEYCODE_DOWN;
			else if (keyValue.equalsIgnoreCase("cursor_left"))
				return AnyKeyboard.KEYCODE_LEFT;
			else if (keyValue.equalsIgnoreCase("cursor_right"))
				return AnyKeyboard.KEYCODE_RIGHT;
			
			return 0;//0 means no action
		}

		private static float getFloatFromString(SharedPreferences sp, String prefKey) {
			String floatValue = sp.getString(prefKey, "1.0");
			try
			{
				return Float.parseFloat(floatValue);
			}
			catch(Exception e)
			{
				return 1.0f;
			}
		}

		public String getDomainText() {
			return mDomainText;
		}

		public String getSmileyText() {
			return mSmileyText;
		}

		public String getChangeLayoutKeysSize() {
			return mLayoutChangeKeysSize;
		}
		
		public boolean getShowKeyPreview()
		{
			return mShowKeyPreview;
		}
		
		public boolean getSwitchKeyboardOnSpace()
		{
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
		public int getSwipeUpKeyCode() {return mSwipeUpKeyCode;}
		@Override
		public int getSwipeDownKeyCode() {return mSwipeDownKeyCode;}
		@Override
		public int getSwipeLeftKeyCode() {return mSwipeLeftKeyCode;}
		@Override
		public int getSwipeRightKeyCode() {return mSwipeRightKeyCode;}
		
		@Override
		public boolean getActionKeyInvisibleWhenRequested() {
			return mActionKeyInvisibleWhenRequested;
		}

		@Override
		public int getDeviceOrientation() {
			return mIme.getApplicationContext().getResources().getConfiguration().orientation;
		}

		@Override
		public String getRtlWorkaroundConfiguration() {
			return mRtlWorkaround;
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
		public boolean getShowVersionNotification() {
			return mShowVersionNotification;
		}
		
		@Override
		public void setShowVersionNotification(boolean show) {
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mIme);
			Editor e = sp.edit();
			e.putBoolean(mIme.getString(R.string.settings_key_show_version_notification), show);
			mShowVersionNotification = show;
			e.commit();
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
		public boolean showIconForSmileyKey() {
			return mShowIconForSmileyKey;
		}
	}
}
