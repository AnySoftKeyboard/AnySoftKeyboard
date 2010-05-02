package com.menny.android.anysoftkeyboard.tutorials;

import com.menny.android.anysoftkeyboard.MainForm;
import com.menny.android.anysoftkeyboard.R;
import com.menny.android.anysoftkeyboard.tutorials.TutorialsProvider.TutorialActivityData;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class TutorialActivity extends Activity implements OnClickListener{

	public static final String NAME_RESOURCE_ID = "NameResourceId";
	public static final String LAYOUT_RESOURCE_ID = "LayoutResourceId";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		int titleId = getIntent().getIntExtra(NAME_RESOURCE_ID, -1);
		int layoutId = getIntent().getIntExtra(LAYOUT_RESOURCE_ID, -1);
		
		TutorialActivityData data = null;
		
		if (titleId == -1 || layoutId == -1)
		{
			data = TutorialsProvider.dequeueTutorial();
			
			if (data == null)
			{
				finish();
				return;
			}
			else
			{
				titleId = data.NameResourceId;
				layoutId = data.LayoutResourceId;
			}
		}
		
		setTitle(titleId);
		View content = getLayoutInflater().inflate(layoutId, null);
		
		setContentView(content);
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
		
		//next one
		if (data != null)
			TutorialsProvider.showNotificationIcon(getApplicationContext());
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
