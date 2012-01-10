package com.anysoftkeyboard.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TabWidget;

import com.anysoftkeyboard.ui.settings.MainSettings;
import com.anysoftkeyboard.ui.tutorials.ChangeLogActivity;
import com.anysoftkeyboard.ui.tutorials.WelcomeHowToNoticeActivity;
import com.menny.android.anysoftkeyboard.R;

public class MainForm extends FragmentActivity implements OnClickListener {

	private static final int NUM_ITEMS = 3; 
	MyAdapter mAdapter;
    ViewPager mPager;

	private TabHost mTabHost;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_fragment);

        mTabHost = (TabHost)findViewById(android.R.id.tabhost);
        mTabHost.setup();
        
        mPager = (ViewPager)findViewById(R.id.main_pager);
        
        mAdapter = new MyAdapter(getSupportFragmentManager(), mTabHost, mPager);
        
        mTabHost.addTab(mTabHost.newTabSpec("tab1").setIndicator(getString(R.string.main_tab_welcome)).setContent(new DummyTabFactory(getApplicationContext())));
        mTabHost.addTab(mTabHost.newTabSpec("tab2").setIndicator(getString(R.string.main_tab_links)).setContent(new DummyTabFactory(getApplicationContext())));
        mTabHost.addTab(mTabHost.newTabSpec("tab3").setIndicator(getString(R.string.main_tab_credits)).setContent(new DummyTabFactory(getApplicationContext())));
        
        if (savedInstanceState != null) {
            mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
        }
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
	
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("tab", mTabHost.getCurrentTabTag());
    }

	public void onClick(View v) {
		switch(v.getId())
		{
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
	
	static class DummyTabFactory implements TabHost.TabContentFactory {
        private final Context mContext;

        public DummyTabFactory(Context context) {
            mContext = context;
        }

        public View createTabContent(String tag) {
            View v = new View(mContext);
            v.setMinimumWidth(0);
            v.setMinimumHeight(0);
            return v;
        }
    }
	
    public static class MyAdapter extends FragmentPagerAdapter
    	implements TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener
    {
    	private final TabHost mTabHost;
        private final ViewPager mViewPager;
        
        public MyAdapter(FragmentManager fm, TabHost tabHost, ViewPager pager) {
            super(fm);
            mTabHost = tabHost;
            mViewPager = pager;
            tabHost.setOnTabChangedListener(this);
            pager.setAdapter(this);
            pager.setOnPageChangeListener(this);
        }

        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        @Override
        public Fragment getItem(int position) {
        	return MainFormFragment.newInstance(position);
        }
        
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        	// TODO Auto-generated method stub
        	
        }
        public void onPageScrollStateChanged(int arg0) {
        	// TODO Auto-generated method stub
        	
        }
        public void onPageSelected(int position) {
        	// Unfortunately when TabHost changes the current tab, it kindly
            // also takes care of putting focus on it when not in touch mode.
            // The jerk.
            // This hack tries to prevent this from pulling focus out of our
            // ViewPager.
            TabWidget widget = mTabHost.getTabWidget();
            int oldFocusability = widget.getDescendantFocusability();
            widget.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
            mTabHost.setCurrentTab(position);
            widget.setDescendantFocusability(oldFocusability);
        }
        
        public void onTabChanged(String tabId) {
        	 int position = mTabHost.getCurrentTab();
             mViewPager.setCurrentItem(position);
        }
    }

    public static class MainFormFragment extends Fragment {
    	static MainFormFragment newInstance(int position) {
        	MainFormFragment f = new MainFormFragment();
        	Bundle args = new Bundle();
        	args.putInt("position", position);
        	f.setArguments(args);
        	
        	return f;
        }

    	private int mLayoutResId;
        /**
         * When creating, retrieve this instance's number from its arguments.
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            final int position = getArguments() != null ? getArguments().getInt("position") : 0;
            switch(position)
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
