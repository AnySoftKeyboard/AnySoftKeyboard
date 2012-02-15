package com.anysoftkeyboard.ui.tutorials;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ScrollView;
import android.widget.Toast;

import com.anysoftkeyboard.ui.MainForm;
import com.menny.android.anysoftkeyboard.R;

public class WelcomeHowToNoticeActivity extends BaseTutorialActivity {

	private static final String ASK_HAS_BEEN_ENABLED_BEFORE = "ask_has_been_enabled_before";

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

	public static boolean hasWelcomeActivityShown(Context context) {
		SharedPreferences sp = context.getSharedPreferences(TutorialsProvider.TUTORIALS_SP_FILENAME, 0);//private
		final boolean hasBeenLoaded = sp.getBoolean(ASK_HAS_BEEN_ENABLED_BEFORE, false);
		
		return hasBeenLoaded;
	}

	private static void markWelcomeActivityAsShown(Context context) {
		SharedPreferences sp = context.getSharedPreferences(TutorialsProvider.TUTORIALS_SP_FILENAME, 0);//private
		Editor e = sp.edit();
		e.putBoolean(ASK_HAS_BEEN_ENABLED_BEFORE, true);
		e.commit();
	}
}
