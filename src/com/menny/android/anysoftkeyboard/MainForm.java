package com.menny.android.anysoftkeyboard;

import android.app.TabActivity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.widget.TabHost;
import android.widget.TextView;

public class MainForm extends TabActivity {

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
		label.setText(label.getText()+version);
		
		TabHost mTabHost = getTabHost();
	    
	    mTabHost.addTab(mTabHost.newTabSpec("tab_test1").setIndicator("Welcome").setContent(R.id.main_tab1));
	    mTabHost.addTab(mTabHost.newTabSpec("tab_test2").setIndicator("Links").setContent(R.id.main_tab2));
	    mTabHost.addTab(mTabHost.newTabSpec("tab_test3").setIndicator("Credits").setContent(R.id.main_tab3));
	    
	    mTabHost.setCurrentTab(0);
    }
}
