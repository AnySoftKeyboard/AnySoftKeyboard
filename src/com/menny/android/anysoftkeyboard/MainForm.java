package com.menny.android.anysoftkeyboard;

import com.menny.android.anysoftkeyboard.tutorials.TutorialActivity;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TabHost;
import android.widget.TextView;

public class MainForm extends TabActivity implements OnClickListener {

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        String version = "";
        try {
			PackageInfo info = super.getApplication().getPackageManager().getPackageInfo(getApplication().getPackageName(), 0);
			version = info.versionName + " (release "+info.versionCode+")";
		} catch (NameNotFoundException e) {
			Log.e("AnySoftKeyboard", "Failed to locate package information! This is very weird... I'm installed.");
		}
		
		TextView label = (TextView)super.findViewById(R.id.main_title_version);
		label.setText(version);
		
		TabHost mTabHost = getTabHost();
	    
	    mTabHost.addTab(mTabHost.newTabSpec("tab_test1").setIndicator(getString(R.string.main_tab_welcome)).setContent(R.id.main_tab1));
	    mTabHost.addTab(mTabHost.newTabSpec("tab_test2").setIndicator(getString(R.string.main_tab_links)).setContent(R.id.main_tab2));
	    mTabHost.addTab(mTabHost.newTabSpec("tab_test3").setIndicator(getString(R.string.main_tab_credits)).setContent(R.id.main_tab3));
	    
	    mTabHost.setCurrentTab(0);
	    
		super.findViewById(R.id.goto_settings_button).setOnClickListener(this);
		super.findViewById(R.id.goto_changelog_button).setOnClickListener(this);
		
    }

	public void onClick(View v) {
		switch(v.getId())
		{
		case R.id.goto_settings_button:
			startSettings(getApplicationContext());
			break;
		case R.id.market_search_button:
			searchMarketForAddons(getApplicationContext());
			break;
		case R.id.goto_changelog_button:
			showChangelog(getApplicationContext());
			break;
		}
	}
	
	public static void searchMarketForAddons(Context applicationContext) {
		Intent search = new Intent(Intent.ACTION_VIEW);
		search.setData(Uri.parse("market://search?q=AnySoftKeyboard"));
		search.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		applicationContext.startActivity(search);
	}
	
	public static void showChangelog(Context applicationContext) {
		Intent intent = new Intent(applicationContext, TutorialActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(TutorialActivity.LAYOUT_RESOURCE_ID, R.layout.changelog);
		intent.putExtra(TutorialActivity.NAME_RESOURCE_ID, R.string.changelog);
		applicationContext.startActivity(intent);
	}
	
	public static void startSettings(Context applicationContext) {
		Intent intent = new Intent(applicationContext, SoftKeyboardSettings.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		applicationContext.startActivity(intent);
	}
}
