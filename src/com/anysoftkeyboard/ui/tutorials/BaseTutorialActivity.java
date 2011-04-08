package com.anysoftkeyboard.ui.tutorials;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public abstract class BaseTutorialActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setTitle(getTitleResId());
		setContentView(getLayoutResId());
	}
	
	protected abstract int getLayoutResId();

	protected abstract int getTitleResId();

	public void onCloseClicked(View view)
	{
		finish();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		finish();
		
		TutorialsProvider.showNotificationIcon(this.getApplicationContext());
	}
}
