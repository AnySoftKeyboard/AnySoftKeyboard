package com.menny.android.anysoftkeyboard.tutorials;

import android.app.Activity;
import android.os.Bundle;

public class TutorialActivity extends Activity {

	public static final String NAME_RESOURCE_ID = "NameResourceId";
	public static final String LAYOUT_RESOURCE_ID = "LayoutResourceId";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		int titleId = getIntent().getIntExtra(NAME_RESOURCE_ID, -1);
		int layoutId = getIntent().getIntExtra(LAYOUT_RESOURCE_ID, -1);
		
		setTitle(titleId);
		setContentView(layoutId);
	}
}
