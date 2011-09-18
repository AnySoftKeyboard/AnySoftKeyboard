package com.anysoftkeyboard.ui.tutorials;

import com.anysoftkeyboard.ui.MainForm;
import com.anysoftkeyboard.ui.settings.BottomRowSelector;
import com.anysoftkeyboard.ui.settings.KeyboardThemeSelector;
import com.anysoftkeyboard.ui.settings.TopRowSelector;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;

public class ChangeLogActivity extends BaseTutorialActivity implements OnClickListener{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		View content = getWindow().getDecorView();
		//now to listen on all known buttons
		for(View touchable : content.getTouchables())
		{
			if (touchable instanceof Button)
			{
				touchable.setOnClickListener(this);
			}
		}
		
		final CheckBox showNotifications = (CheckBox)findViewById(R.id.show_notifications_next_time);
		showNotifications.setChecked(AnyApplication.getConfig().getShowVersionNotification());
		
		showNotifications.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				AnyApplication.getConfig().setShowVersionNotification(!AnyApplication.getConfig().getShowVersionNotification());
			}
		});
	}

	public void onClick(View v) {
		switch(v.getId())
		{
		case R.id.goto_settings_button:
			MainForm.startSettings(getApplicationContext());
			break;
		case R.id.market_search_button:
			try
			{
				MainForm.searchMarketForAddons(getApplicationContext(), "");
			}
			catch(Exception ex)
			{
				Log.e("ASK - Tutorial", "Failed to launch Market!", ex);
			}
			break;
		case R.id.settings_bottom_row_button:
			Intent bottomRowSettings = new Intent(this, BottomRowSelector.class);
			bottomRowSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(bottomRowSettings);
			break;
		case R.id.settings_top_row_button:
			Intent topRowSettings = new Intent(this, TopRowSelector.class);
			topRowSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(topRowSettings);
			break;
		case R.id.settings_keyboard_theme_button:
			Intent themeSelector = new Intent(this, KeyboardThemeSelector.class);
			themeSelector.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(themeSelector);
			break;
		}
	}

	@Override
	protected int getLayoutResId() {
		return R.layout.changelog;
	}

	@Override
	protected int getTitleResId() {
		return R.string.changelog;
	}
}
