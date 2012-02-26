package com.anysoftkeyboard.ui.tutorials;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ScrollView;
import android.widget.Toast;

import com.anysoftkeyboard.receivers.AnySoftKeyboardInstalledReceiver;
import com.anysoftkeyboard.ui.MainForm;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

public class WelcomeHowToNoticeActivity extends BaseTutorialActivity {

	public static final String ASK_HAS_BEEN_ENABLED_BEFORE = "ask_has_been_enabled_before";
	private static final String TAG = "ASK_WELCOME";

	@Override
	protected int getLayoutResId() {
		return R.layout.welcome_howto;
	}
	
	@Override
	protected int getTitleResId() {
		return R.string.how_to_pointer_title;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		markWelcomeActivityAsShown(getApplicationContext());
		NotificationManager mngr = (NotificationManager)getSystemService(Service.NOTIFICATION_SERVICE);
		mngr.cancel(AnySoftKeyboardInstalledReceiver.INSTALLED_NOTIFICATION_ID);
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId())
		{
		case R.id.howto_button_configure_imes:
			Toast.makeText(getApplicationContext(), R.string.how_to_simple_howto_press_back_to_return_tip, Toast.LENGTH_LONG).show();
			startActivityForResult(new Intent(android.provider.Settings.ACTION_INPUT_METHOD_SETTINGS), 0);
			break;
		case R.id.howto_button_switch_to_ask:
			ScrollView scroller = (ScrollView)findViewById(R.id.howto_scroller);
			scroller.scrollTo(scroller.getWidth(), scroller.getHeight());
			InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            mgr.showInputMethodPicker();
            break;
		case R.id.howto_button_goto_ask_settings:
			MainForm.startSettings(getApplicationContext());
			break;
		default:
			super.onClick(v);
		}
	}

	public static boolean shouldShowWelcomeActivity(Context context)
	{
		if (!linearSearch( Secure.getString(context.getContentResolver(), Secure.ENABLED_INPUT_METHODS), context.getPackageName() ) )
		{
			//ASK is not enabled, but installed. Has the user forgot how to turn it on?
			if (!hasWelcomeActivityShown(context))
			{ 
				return true;
			}
		}
		
		return false;
	}
	
	private static boolean hasWelcomeActivityShown(Context context) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);// context.getSharedPreferences(TutorialsProvider.TUTORIALS_SP_FILENAME, 0);//private
		final boolean hasBeenLoaded = sp.getBoolean(ASK_HAS_BEEN_ENABLED_BEFORE, false);
		
		return hasBeenLoaded;
	}
	
	/**
	 * Search array for an entry BEGINNING with key.
	 * 
	 * @param array the array to search over
	 * @param key the string to search for
	 * @return true if the key was found in the array
	 */
	private static boolean linearSearch( String listOfIme, final String key )
	{
		if (TextUtils.isEmpty(listOfIme) || TextUtils.isEmpty(key))
			return false;
		if (AnyApplication.DEBUG)
			Log.d(TAG, "Currently these are the IME enabled in the OS: "+listOfIme);
		String[] arrayOfIme = listOfIme.split(":");
		if (arrayOfIme == null)
			return false;
		
		for(final String ime : arrayOfIme)
		{
			if (TextUtils.isEmpty(ime)) continue;
			if (AnyApplication.DEBUG)
				Log.d(TAG, "Is '"+ime+"' starts with '"+key+"'?");
			//checking "startsWith" since the OS list is something like this:
			//com.android.inputmethod.latin/.LatinIME:com.menny.android.anysoftkeyboard/.SoftKeyboard
			if (ime.startsWith(key)) return true;
		}
		
		if (AnyApplication.DEBUG)
			Log.d(TAG, "'"+key+"' was not found in the list of IMEs!");
		return false;
	}

	private static void markWelcomeActivityAsShown(Context context) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);// context.getSharedPreferences(TutorialsProvider.TUTORIALS_SP_FILENAME, 0);//private
		Editor e = sp.edit();
		e.putBoolean(ASK_HAS_BEEN_ENABLED_BEFORE, true);
		e.commit();
	}
}
