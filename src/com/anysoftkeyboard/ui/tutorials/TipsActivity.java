package com.anysoftkeyboard.ui.tutorials;

import java.util.ArrayList;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.anysoftkeyboard.ui.settings.TopRowSelector;
import com.menny.android.anysoftkeyboard.R;

public class TipsActivity extends BaseTutorialActivity {

	private static final String TAG = "ASK TIPS";
	private final ArrayList<Integer> mLayoutsToShow = new ArrayList<Integer>();
	private int mCurrentTipIndex = 0;
	
	private SharedPreferences mAppPrefs;
	
	private ViewGroup mTipContainer;
	@Override
	protected int getLayoutResId() {
		return R.layout.tips_layout;
	}
	
	@Override
	protected int getTitleResId() {
		return R.string.tips_title;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null)
		{
			mCurrentTipIndex = savedInstanceState.getInt("mCurrentTipIndex");
		}
		
		mTipContainer = (ViewGroup)findViewById(R.id.tips_layout_container);
		
		mAppPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("mCurrentTipIndex", mCurrentTipIndex);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		//looking for tips to show
		boolean haveMore = true;
		mLayoutsToShow.clear();
		Resources res = getResources();
		int currentTipLoadingIndex = 1;
		while(haveMore)
		{
			final String layoutResourceName = "tip_layout_"+currentTipLoadingIndex;
			Log.d(TAG, "Looking for tip "+layoutResourceName);
			final int resId = res.getIdentifier(layoutResourceName, "layout", getPackageName());
			haveMore = (resId != 0);
			if (resId != 0)
			{
				if (!mAppPrefs.getBoolean(layoutResourceName, false))
				{
					Log.d(TAG, "Got a tip #"+currentTipLoadingIndex+" which is "+layoutResourceName);
					mLayoutsToShow.add(new Integer(resId));
				}
			}
			currentTipLoadingIndex++;
		}
		
		if (mLayoutsToShow.size() == 0)
		{
			finish();
		}
		else
		{
			showTip();
		}
	}

	private void showTip() {
		if (mCurrentTipIndex >= mLayoutsToShow.size())
			mCurrentTipIndex = mLayoutsToShow.size() - 1;
		
		mTipContainer.removeAllViews();
		final int resId = mLayoutsToShow.get(mCurrentTipIndex).intValue();
		View newTip = getLayoutInflater().inflate(resId, null);
		mTipContainer.addView(newTip);
		
		setClickHandler(newTip);
		
		findViewById(R.id.previous_tip_button).setEnabled(mCurrentTipIndex != 0);
		findViewById(R.id.next_tip_button).setEnabled(mCurrentTipIndex != mLayoutsToShow.size() - 1);
		
		String resName = getResources().getResourceName(resId);
		resName = resName.substring(resName.lastIndexOf("/")+1, resName.length());
		Log.d(TAG, "Seen tip "+resName+".");
		Editor e = mAppPrefs.edit();
		e.putBoolean(resName, true);
		e.commit();
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId())
		{
		case R.id.next_tip_button:
			mCurrentTipIndex++;
			showTip();
			break;
		case R.id.previous_tip_button:
			mCurrentTipIndex--;
			showTip();
			break;
		//special tips buttons
		case R.id.tips_goto_top_row_settings:
			Intent startTopRowSettingsIntent = new Intent(this, TopRowSelector.class);
			startActivity(startTopRowSettingsIntent);
			break;
		//super
		default:
			super.onClick(v);
			break;
		}
	}
}
