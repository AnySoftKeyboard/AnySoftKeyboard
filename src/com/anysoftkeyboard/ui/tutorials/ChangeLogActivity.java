package com.anysoftkeyboard.ui.tutorials;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;

import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

public class ChangeLogActivity extends BaseTutorialActivity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		final CheckBox showNotifications = (CheckBox)findViewById(R.id.show_notifications_next_time);
		showNotifications.setChecked(AnyApplication.getConfig().getShowVersionNotification());
		
		showNotifications.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				AnyApplication.getConfig().setShowVersionNotification(!AnyApplication.getConfig().getShowVersionNotification());
			}
		});
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
