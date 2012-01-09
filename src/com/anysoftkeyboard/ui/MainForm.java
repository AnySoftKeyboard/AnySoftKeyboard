package com.anysoftkeyboard.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.anysoftkeyboard.ui.settings.MainSettings;
import com.anysoftkeyboard.ui.tutorials.ChangeLogActivity;
import com.anysoftkeyboard.ui.tutorials.WelcomeHowToNoticeActivity;
import com.menny.android.anysoftkeyboard.R;

public class MainForm extends FragmentActivity implements OnClickListener {

	private static final int NUM_ITEMS = 3; 
	MyAdapter mAdapter;
    ViewPager mPager;
	private Drawable mSelectedTabBottomDrawable;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_fragment);

        mSelectedTabBottomDrawable =  getResources().getDrawable(R.drawable.selected_tab);
        mSelectedTabBottomDrawable.setBounds(0, 0, getWindowManager().getDefaultDisplay().getWidth(), 8);
        
        mAdapter = new MyAdapter(getSupportFragmentManager());
        
        mPager = (ViewPager)findViewById(R.id.main_pager);
        mPager.setAdapter(mAdapter);

        mPager.setOnPageChangeListener(new OnPageChangeListener() {
			
			public void onPageSelected(int arg0) {
				setSelectedTab(arg0);
			}
			
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}
			
			public void onPageScrollStateChanged(int arg0) {
			}
		});
        
		setSelectedTab(0);
        
        findViewById(R.id.main_tab_text_1).setOnClickListener(this);
        findViewById(R.id.main_tab_text_2).setOnClickListener(this);
        findViewById(R.id.main_tab_text_3).setOnClickListener(this);
        
        /*
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
		super.findViewById(R.id.goto_howto_form).setOnClickListener(this);
		*/
        
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
		case R.id.goto_settings_button:
			startSettings(getApplicationContext());
			break;
		case R.id.market_search_button:
			try
			{
				searchMarketForAddons(getApplicationContext(), "");
			}
			catch(Exception ex)
			{
				Log.e("MainForm", "Failed to launch Market! ", ex);
			}
			break;
		case R.id.goto_changelog_button:
			showChangelog(getApplicationContext());
			break;
		}
	}

	void setSelectedTab(int index) {
		((TextView)findViewById(R.id.main_tab_text_1)).setCompoundDrawables(null, null, null, index == 0? mSelectedTabBottomDrawable : null);
		((TextView)findViewById(R.id.main_tab_text_2)).setCompoundDrawables(null, null, null, index == 1? mSelectedTabBottomDrawable : null);
		((TextView)findViewById(R.id.main_tab_text_3)).setCompoundDrawables(null, null, null, index == 2? mSelectedTabBottomDrawable : null);
	}
	
    public static class MyAdapter extends FragmentPagerAdapter {
        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        @Override
        public Fragment getItem(int position) {
        	return MainFormFragment.newInstance(position);
        }
    }

    public static class MainFormFragment extends Fragment {
        private int mLayoutResId;
        private int mPosition;

        static MainFormFragment newInstance(int position) {
        	MainFormFragment f = new MainFormFragment();
        	Bundle args = new Bundle();
        	args.putInt("position", position);
        	f.setArguments(args);
        	
        	return f;
        }

        /**
         * When creating, retrieve this instance's number from its arguments.
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mPosition = getArguments() != null ? getArguments().getInt("position") : 0;
            switch(mPosition)
            {
            case 1:
            	mLayoutResId = R.layout.main_fragment_2;
            	break;
            case 2:
            	mLayoutResId = R.layout.main_fragment_3;
            	break;
            default:
            	mLayoutResId = R.layout.main_fragment_1;
            	break;		
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        	
            View v = inflater.inflate(mLayoutResId, container, false);

            return v;
        }
    }
    
	public static void searchMarketForAddons(Context applicationContext, String additionalQueryString) throws android.content.ActivityNotFoundException {
		Intent search = new Intent(Intent.ACTION_VIEW);
		search.setData(Uri.parse("market://search?q=AnySoftKeyboard"+additionalQueryString));
		search.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		applicationContext.startActivity(search);
	}
	
	public static void showChangelog(Context applicationContext) {
		Intent intent = new Intent(applicationContext, ChangeLogActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		applicationContext.startActivity(intent);
	}
	
	public static void startSettings(Context applicationContext) {
		Intent intent = new Intent(applicationContext, MainSettings.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		applicationContext.startActivity(intent);
	}
}
