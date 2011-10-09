package com.anysoftkeyboard.ui.tutorials;

import com.menny.android.anysoftkeyboard.R;

public class WelcomeHowToNoticeActivity extends BaseTutorialActivity {

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
		TutorialsProvider.markWelcomeActivityAsShown(getApplicationContext());
	}
}
