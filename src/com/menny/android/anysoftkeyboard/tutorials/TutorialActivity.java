package com.menny.android.anysoftkeyboard.tutorials;

import com.menny.android.anysoftkeyboard.MainForm;
import com.menny.android.anysoftkeyboard.R;
import com.menny.android.anysoftkeyboard.tutorials.TutorialsProvider.TutorialActivityData;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class TutorialActivity extends Activity implements OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		TutorialActivityData data = TutorialsProvider.dequeueTutorial();
		
		if (data == null)
		{
			finish();
		}
		else
		{
			setTitle(data.NameResourceId);
			setContentView(data.LayoutResourceId);
			//next one
			TutorialsProvider.showNotificationIcon(getApplicationContext());
		}
	}

	public void onClick(View v) {
		switch(v.getId())
		{
		case R.id.goto_settings_button:
			MainForm.startSettings(getApplicationContext());
			break;
		case R.id.market_search_button:
			MainForm.searchMarketForAddons(getApplicationContext());
			break;
		}
	}

}
