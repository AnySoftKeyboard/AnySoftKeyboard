package com.menny.android.anysoftkeyboard.tutorials;

import com.menny.android.anysoftkeyboard.tutorials.TutorialsProvider.TutorialActivityData;

import android.app.Activity;
import android.os.Bundle;

public class TutorialActivity extends Activity {

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
}
