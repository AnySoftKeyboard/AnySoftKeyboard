package com.menny.android.anysoftkeyboard.tutorials;

import com.menny.android.anysoftkeyboard.MainForm;
import com.menny.android.anysoftkeyboard.R;
import com.menny.android.anysoftkeyboard.tutorials.TutorialsProvider.TutorialActivityData;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class TutorialActivity extends Activity implements OnClickListener{

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
			View content = getLayoutInflater().inflate(data.LayoutResourceId, null);
			setContentView(content);
			//next one
			TutorialsProvider.showNotificationIcon(getApplicationContext());
			//now to listen on all known buttons
			for(View touchable : content.getTouchables())
			{
				switch(touchable.getId())
				{
				case R.id.goto_settings_button:
				case R.id.market_search_button:
					touchable.setOnClickListener(this);
					break;
				}
			}
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
