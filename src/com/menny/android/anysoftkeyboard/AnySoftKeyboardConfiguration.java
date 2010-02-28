package com.menny.android.anysoftkeyboard;

import com.menny.android.anysoftkeyboard.keyboards.AnyKeyboard;

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
	private static final AnySoftKeyboardConfiguration msInstance;
	
	static 
	{
		msInstance = new AnySoftKeyboardConfigurationImpl();
	}
		
	public static AnySoftKeyboardConfiguration getInstance() {return msInstance;}
	
	public abstract boolean getDEBUG();
	
	public abstract String getSmileyText();
	
	public abstract String getDomainText();
	
	public abstract String getChangeLayoutKeysSize();
	
	public abstract boolean getShowKeyPreview();

	public abstract boolean getSwitchKeyboardOnSpace();
	
	public abstract boolean getUseFullScreenInput();
	
	public abstract boolean getUseRepeatingKeys();
	
	public abstract float getKeysHeightFactorInPortrait();
	
	public abstract float getKeysHeightFactorInLandscape();
	
	public abstract boolean getInsertSpaceAfterCandidatePick();
	
	public abstract int getSwipeUpKeyCode();
	public abstract int getSwipeDownKeyCode();
	public abstract int getSwipeLeftKeyCode();
	public abstract int getSwipeRightKeyCode();
	
	static class AnySoftKeyboardConfigurationImpl extends AnySoftKeyboardConfiguration
	{
		private InputMethodService mIme;
		//this is determined from the version. It includes "tester", the it will be true
		private boolean mDEBUG = true;

		private String mSmileyText = ":-)";
		private String mDomainText = ".com";
		private String mLayoutChangeKeysSize = "Small";
		private boolean mShowKeyPreview = true;
		private boolean mSwitchKeyboardOnSpace = true;
		private boolean mUseFullScreenInput = true;
		private boolean mUseKeyRepeat = true;
		private float mKeysHeightFactorInPortrait = 1.0f;
		private float mKeysHeightFactorInLandscape = 1.0f;
		private boolean mInsertSpaceAfterCandidatePick = true;
		private int mSwipeUpKeyCode;
		private int mSwipeDownKeyCode;
		private int mSwipeLeftKeyCode;
		private int mSwipeRightKeyCode;
				
		public AnySoftKeyboardConfigurationImpl()
		{
			
		}
		
		void initializeConfiguration(InputMethodService ime) 
		{
			mIme = ime;
			
			Log.i("AnySoftKeyboard", "** Locale:"+ mIme.getResources().getConfiguration().locale.toString());
			String version = "NONE";
			int releaseNumber = 0;
	        try {
				PackageInfo info = mIme.getApplication().getPackageManager().getPackageInfo(mIme.getApplication().getPackageName(), 0);
				version = info.versionName;
				releaseNumber = info.versionCode;
				Log.i("AnySoftKeyboard", "** Version: "+version);
			} catch (NameNotFoundException e) {
				Log.e("AnySoftKeyboard", "Failed to locate package information! This is very weird... I'm installed.");
			}
			
			mDEBUG = ((releaseNumber % 2) == 0);//even versions are TESTERS
			if (mDEBUG)
			{
				//RC versions should not be "debug", but they do not have odd version.
				if (version.contains("RC"))
					mDEBUG = false;
			}
			Log.i("AnySoftKeyboard", "** Version: "+version);
			Log.i("AnySoftKeyboard", "** Release code: "+releaseNumber);
			Log.i("AnySoftKeyboard", "** Debug: "+mDEBUG);
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mIme);
			upgradeSettingsValues(sp);
			
			handleConfigurationChange(sp);
		}
		
		private void upgradeSettingsValues(SharedPreferences sp) {
			Log.d("AnySoftKeyboard", "Checking if configuration upgrade is needed.");
			String currentChangeLayoutKeysSize = sp.getString("keyboard_layout_change_method", "Small");
			if ((currentChangeLayoutKeysSize == null) || (currentChangeLayoutKeysSize.length() == 0) ||
				(currentChangeLayoutKeysSize.equals("1")) || (currentChangeLayoutKeysSize.equals("2")) || (currentChangeLayoutKeysSize.equals("3")))
			{
				String newValue = "Small";
				Log.d("AnySoftKeyboard", "keyboard_layout_change_method holds an old value: "+(currentChangeLayoutKeysSize != null? currentChangeLayoutKeysSize : "NULL"));
				if (currentChangeLayoutKeysSize.equals("1")) newValue = "Small";
				else if (currentChangeLayoutKeysSize.equals("2")) newValue = "None";
				else if (currentChangeLayoutKeysSize.equals("3")) newValue = "Big";
				Editor e = sp.edit();
				Log.d("AnySoftKeyboard", "keyboard_layout_change_method will be changed to: "+newValue);
				e.putString("keyboard_layout_change_method", newValue);
				e.commit();
			}
		}
		
		public boolean handleConfigurationChange(SharedPreferences sp)
		{
			Log.i("AnySoftKeyboard", "**** handleConfigurationChange: ");
			boolean handled = false;
			//if a change in the configuration requires rebuilding the keyboards, 'forceRebuildOfKeyboards' should set to 'true'
			boolean forceRebuildOfKeyboards = false;
			// this change requires the recreation of the keyboards.
			String newLayoutChangeKeysSize = sp.getString("keyboard_layout_change_method", "Small");
			forceRebuildOfKeyboards = forceRebuildOfKeyboards || (!newLayoutChangeKeysSize.equalsIgnoreCase(mLayoutChangeKeysSize));
			mLayoutChangeKeysSize = newLayoutChangeKeysSize;
			Log.i("AnySoftKeyboard", "** mChangeKeysMode: "+mLayoutChangeKeysSize);
			
			String newSmileyText = sp.getString("default_smiley_text", ":-) ");
			handled = handled || (!newSmileyText.equals(mSmileyText));
			mSmileyText = newSmileyText;
			Log.i("AnySoftKeyboard", "** mSmileyText: "+mSmileyText);
			
			String newDomainText = sp.getString("default_domain_text", ".com");
			handled = handled || (!newDomainText.equals(mDomainText));
			mDomainText = newDomainText;
			Log.i("AnySoftKeyboard", "** mDomainText: "+mDomainText);
			
			boolean newShowPreview = sp.getBoolean("key_press_preview_popup", true);
			handled = handled || (newShowPreview != mShowKeyPreview);
			mShowKeyPreview = newShowPreview;
			Log.i("AnySoftKeyboard", "** mShowKeyPreview: "+mShowKeyPreview);

			boolean newSwitchKeyboardOnSpace = sp.getBoolean("switch_keyboard_on_space", false);
			handled = handled || (newSwitchKeyboardOnSpace != mSwitchKeyboardOnSpace);
			mSwitchKeyboardOnSpace = newSwitchKeyboardOnSpace;
			Log.i("AnySoftKeyboard", "** mSwitchKeyboardOnSpace: "+mSwitchKeyboardOnSpace);
			
			boolean newUseFullScreenInput = sp.getBoolean("fullscreen_input_connection_supported", true);
			handled = handled || (newUseFullScreenInput != mUseFullScreenInput);
			mUseFullScreenInput = newUseFullScreenInput;
			Log.i("AnySoftKeyboard", "** mUseFullScreenInput: "+mUseFullScreenInput);
			
			// Fix issue 185
			boolean newUseKeyRepeat = sp.getBoolean("use_keyrepeat", true);
			handled = handled || ( newUseKeyRepeat != mUseKeyRepeat );
			mUseKeyRepeat = newUseKeyRepeat;
			Log.i("AnySoftKeyboard", "** mUseKeyRepeat: "+mUseKeyRepeat);
			
			float newKeyHeightFactorPortrait = getFloatFromString(sp, "zoom_factor_keys_in_portrait");
			forceRebuildOfKeyboards = forceRebuildOfKeyboards || ( newKeyHeightFactorPortrait != mKeysHeightFactorInPortrait );
			mKeysHeightFactorInPortrait = newKeyHeightFactorPortrait;
			Log.i("AnySoftKeyboard", "** mKeysHeightFactorInPortrait: "+mKeysHeightFactorInPortrait);
			
			float newKeyHeightFactorLandscape = getFloatFromString(sp, "zoom_factor_keys_in_landscape");
			forceRebuildOfKeyboards = forceRebuildOfKeyboards || ( newKeyHeightFactorLandscape != mKeysHeightFactorInLandscape );
			mKeysHeightFactorInLandscape = newKeyHeightFactorLandscape;
			Log.i("AnySoftKeyboard", "** mKeysHeightFactorInLandscape: "+mKeysHeightFactorInLandscape);
			
			boolean newInsertSpaceAfterCandidatePick = sp.getBoolean("insert_space_after_word_suggestion_selection", true);
			handled = handled || ( newInsertSpaceAfterCandidatePick != mInsertSpaceAfterCandidatePick );
			mInsertSpaceAfterCandidatePick = newInsertSpaceAfterCandidatePick;
			Log.i("AnySoftKeyboard", "** mInsertSpaceAfterCandidatePick: "+mInsertSpaceAfterCandidatePick);
			
			int newSwipeUpValue = getIntFromSwipeConfiguration(sp, "swipe_up_action", "shift");
			handled = handled || ( newSwipeUpValue != mSwipeUpKeyCode );
			mSwipeUpKeyCode = newSwipeUpValue;
			Log.i("AnySoftKeyboard", "** mSwipeUpKeyCode: "+mSwipeUpKeyCode);
			
			int newSwipeDownValue = getIntFromSwipeConfiguration(sp, "swipe_down_action", "hide");
			handled = handled || ( newSwipeDownValue != mSwipeDownKeyCode );
			mSwipeDownKeyCode = newSwipeDownValue;
			Log.i("AnySoftKeyboard", "** mSwipeDownKeyCode: "+mSwipeDownKeyCode);
			
			int newSwipeLeftValue = getIntFromSwipeConfiguration(sp, "swipe_left_action", "next_symbols");
			handled = handled || ( newSwipeLeftValue != mSwipeLeftKeyCode );
			mSwipeLeftKeyCode = newSwipeLeftValue;
			Log.i("AnySoftKeyboard", "** mSwipeLeftKeyCode: "+mSwipeLeftKeyCode);
			
			int newSwipeRightValue = getIntFromSwipeConfiguration(sp, "swipe_right_action", "next_alphabet");
			handled = handled || ( newSwipeRightValue != mSwipeRightKeyCode );
			mSwipeRightKeyCode = newSwipeRightValue;
			Log.i("AnySoftKeyboard", "** mSwipeRightKeyCode: "+mSwipeRightKeyCode);
			
			return handled && (!forceRebuildOfKeyboards);
		}

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

		public boolean getDEBUG() {return mDEBUG;}

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

		public boolean getUseFullScreenInput() {
			return mUseFullScreenInput;
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
	}
}
