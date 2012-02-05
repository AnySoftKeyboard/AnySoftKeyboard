package com.anysoftkeyboard.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.anysoftkeyboard.ui.settings.MainSettings;
import com.anysoftkeyboard.ui.tutorials.ChangeLogActivity;
import com.anysoftkeyboard.ui.tutorials.TipsActivity;
import com.anysoftkeyboard.ui.tutorials.WelcomeHowToNoticeActivity;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

public class MainForm extends Activity implements OnClickListener {

	private ViewFlipper mPager;
    private Drawable mSelectedTabBottomDrawable;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_fragment);
        
        mSelectedTabBottomDrawable =  getResources().getDrawable(R.drawable.selected_tab);
        mSelectedTabBottomDrawable.setBounds(0, 0, getWindowManager().getDefaultDisplay().getWidth(), getResources().getDimensionPixelOffset(R.dimen.selected_tab_drawable_height));
        
        mPager = (ViewFlipper)findViewById(R.id.main_pager);
        
        findViewById(R.id.main_tab_text_1).setOnClickListener(this);
        findViewById(R.id.main_tab_text_2).setOnClickListener(this);
        findViewById(R.id.main_tab_text_3).setOnClickListener(this);
	    
		findViewById(R.id.goto_tips_form).setOnClickListener(this);
		findViewById(R.id.goto_changelog_button).setOnClickListener(this);
		findViewById(R.id.goto_howto_form).setOnClickListener(this);
        
		CheckBox showTipsNotifications = (CheckBox) findViewById(R.id.show_tips_next_time);
		showTipsNotifications.setChecked(AnyApplication.getConfig().getShowTipsNotification());
		showTipsNotifications.setOnClickListener(this);
		
		CheckBox showVersionNotifications = (CheckBox) findViewById(R.id.show_notifications_next_time);
		showVersionNotifications.setChecked(AnyApplication.getConfig().getShowVersionNotification());
		showVersionNotifications.setOnClickListener(this);
		
        setSelectedTab(0);
    }
	

	void setSelectedTab(int index) {
        ((TextView)findViewById(R.id.main_tab_text_1)).setCompoundDrawables(null, null, null, index == 0? mSelectedTabBottomDrawable : null);
        ((TextView)findViewById(R.id.main_tab_text_2)).setCompoundDrawables(null, null, null, index == 1? mSelectedTabBottomDrawable : null);
        ((TextView)findViewById(R.id.main_tab_text_3)).setCompoundDrawables(null, null, null, index == 2? mSelectedTabBottomDrawable : null);
        
        if (mPager.getDisplayedChild() != index)
        	mPager.setDisplayedChild(index);
}
	
	public void onClick(View v) {
		switch(v.getId())
		{
		case R.id.main_tab_text_1:
            setSelectedTab(0);
            break;
	    case R.id.main_tab_text_2:
	            setSelectedTab(1);
	            break;
	    case R.id.main_tab_text_3:
            setSelectedTab(2);
            break;
		case R.id.goto_howto_form:
			Intent i = new Intent(getApplicationContext(), WelcomeHowToNoticeActivity.class);
			startActivity(i);
			break;
		case R.id.goto_tips_form:
			Intent tipActivity= new Intent(getApplicationContext(), TipsActivity.class);
			tipActivity.putExtra(TipsActivity.EXTRA_SHOW_ALL_TIPS, true);
			startActivity(tipActivity);
			break;
		case R.id.goto_changelog_button:
			Intent changelog = new Intent(this, ChangeLogActivity.class);
			changelog.putExtra(ChangeLogActivity.EXTRA_SHOW_ALL_LOGS, true);
			startActivity(changelog);
			break;
		case R.id.show_tips_next_time:
			AnyApplication.getConfig().setShowTipsNotification(!AnyApplication.getConfig().getShowTipsNotification());
			break;
		case R.id.show_notifications_next_time:
			AnyApplication.getConfig().setShowVersionNotification(!AnyApplication.getConfig().getShowVersionNotification());
			break;
		}
	}

	public static void searchMarketForAddons(Context applicationContext, String additionalQueryString) throws android.content.ActivityNotFoundException {
		Intent search = new Intent(Intent.ACTION_VIEW);
		search.setData(Uri.parse("market://search?q=AnySoftKeyboard"+additionalQueryString));
		search.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		applicationContext.startActivity(search);
	}
	
	public static void startSettings(Context applicationContext) {
		Intent intent = new Intent(applicationContext, MainSettings.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		applicationContext.startActivity(intent);
	}
}
