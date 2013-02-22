/* The following code was written by Matthew Wiggins 
 * and is released under the APACHE 2.0 license 
 * 
 * additional code was written by Menny Even Danan, and is also released under APACHE 2.0 license 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.anysoftkeyboard.ui.settings;

import com.menny.android.anysoftkeyboard.R;

import android.app.Service;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.preference.Preference;
import android.widget.SeekBar;
import android.widget.TextView;


public class SeekBarPreference extends /*Dialog*/Preference implements SeekBar.OnSeekBarChangeListener
{
	private static final String androidns="http://schemas.android.com/apk/res/android";
	private static final String askns="http://schemas.android.com/apk/res/com.menny.android.anysoftkeyboard";
	
	private SeekBar mSeekBar;
	private TextView mMaxValue,mCurrentValue,mMinValue;
	private String mTitle;
	private Context mContext;

	private int mDefault=50, mMax=100, mMin=0, mValue = 0;
  
	public SeekBarPreference(Context context, AttributeSet attrs) { 
		super(context,attrs); 
		mContext = context;
		mDefault = attrs.getAttributeIntValue(androidns,"defaultValue", 0);
		mMax = attrs.getAttributeIntValue(androidns,"max", 100);
		mMin = attrs.getAttributeIntValue(askns, "min", 0);
		int titleResId = attrs.getAttributeResourceValue(androidns, "title", 0);
		if (titleResId == 0)
			mTitle = attrs.getAttributeValue(androidns, "title");
		else
			mTitle = context.getString(titleResId);
	}
  
	@Override
	protected View onCreateView(ViewGroup parent) {
		LayoutInflater inflator = (LayoutInflater)mContext.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
		ViewGroup mySeekBarLayout = (ViewGroup)inflator.inflate(R.layout.my_seek_bar_pref, null);
		mSeekBar = (SeekBar) mySeekBarLayout.findViewById(R.id.pref_seekbar);
		if (shouldPersist())
			mValue = getPersistedInt(mDefault);

		mCurrentValue = (TextView)mySeekBarLayout.findViewById(R.id.pref_current_value);
		mMaxValue = (TextView)mySeekBarLayout.findViewById(R.id.pref_max_value);
		mMinValue = (TextView)mySeekBarLayout.findViewById(R.id.pref_min_value);
		mCurrentValue.setText(Integer.toString(mValue));
		((TextView)mySeekBarLayout.findViewById(R.id.pref_title)).setText(mTitle);
		
		writeBoundaries();
		
		mSeekBar.setMax(mMax-mMin);
		mSeekBar.setProgress(mValue-mMin);
		mSeekBar.setOnSeekBarChangeListener(this);
		
	    return mySeekBarLayout;
	}

	@Override
	protected void onSetInitialValue(boolean restore, Object defaultValue)  
	{
		super.onSetInitialValue(restore, defaultValue);
		if (restore) 
			mValue = shouldPersist() ? getPersistedInt(mDefault) : mMin;
		else 
			mValue = (Integer)defaultValue;
		
		if (mValue > mMax) mValue=mMax;
		if (mValue < mMin) mValue=mMin;
		
		if (mCurrentValue != null)
			mCurrentValue.setText(Integer.toString(mValue));
	}

  public void onProgressChanged(SeekBar seek, int value, boolean fromTouch)
  {
	  mValue = value+mMin;
	  if (mValue > mMax) mValue=mMax;
	  if (mValue < mMin) mValue=mMin;
	  
	  if (shouldPersist()) persistInt(mValue);
	  callChangeListener(Integer.valueOf(mValue));
	  
	  if (mCurrentValue != null)
		  mCurrentValue.setText(Integer.toString(mValue));
  }
  public void onStartTrackingTouch(SeekBar seek) {}
  public void onStopTrackingTouch(SeekBar seek) {}

  private void writeBoundaries()
  {
	  mMaxValue.setText(Integer.toString(mMax));
	  mMinValue.setText(Integer.toString(mMin));
	  if (mValue > mMax) mValue=mMax;
	  if (mValue < mMin) mValue=mMin;
	  if (mCurrentValue != null)
		  mCurrentValue.setText(Integer.toString(mValue));
  }
  
  public void setMax(int max) { mMax = max; writeBoundaries(); }
  public int getMax() { return mMax; }
  public void setMin(int min) { mMin = min; writeBoundaries();}
  public int getMin() { return mMin; }

  public void setProgress(int progress) { 
	  mValue = progress;
	  if (mValue > mMax) mValue=mMax;
	  if (mValue < mMin) mValue=mMin;
	  
	  if (mSeekBar != null)
	  {
		  mSeekBar.setProgress(progress-mMin);
		  mCurrentValue.setText(Integer.toString(mValue));
	  }
  }
  
  public int getProgress() { return mValue; }
}

