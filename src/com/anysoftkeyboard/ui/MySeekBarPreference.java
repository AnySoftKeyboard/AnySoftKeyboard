/* The following code was written by Matthew Wiggins 
 * and is released under the APACHE 2.0 license 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.anysoftkeyboard.ui;

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


public class MySeekBarPreference extends /*Dialog*/Preference implements SeekBar.OnSeekBarChangeListener
{
	private static final String androidns="http://schemas.android.com/apk/res/android";
	private SeekBar mSeekBar;
	private TextView mCurrentValue;
	private TextView mMaxValue;
	private String mTitle;
	private Context mContext;

	private int mDefault, mMax, mValue = 0;
  
	public MySeekBarPreference(Context context, AttributeSet attrs) { 
		super(context,attrs); 
		mContext = context;
		mDefault = attrs.getAttributeIntValue(androidns,"defaultValue", 0);
		mMax = attrs.getAttributeIntValue(androidns,"max", 100);
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

		mSeekBar.setMax(mMax);
		mSeekBar.setProgress(mValue);
		mSeekBar.setOnSeekBarChangeListener(this);
		mCurrentValue = (TextView)mySeekBarLayout.findViewById(R.id.pref_current_value);
		mMaxValue = (TextView)mySeekBarLayout.findViewById(R.id.pref_max_value);
		mCurrentValue.setText(Integer.toString(mValue));
		mMaxValue.setText(Integer.toString(mMax));
	    
		((TextView)mySeekBarLayout.findViewById(R.id.pref_title)).setText(mTitle);
		
	    return mySeekBarLayout;
	}
  /*
  @Override 
  protected View onCreateDialogView() {
	  LayoutInflater inflator = (LayoutInflater)mContext.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
	  ViewGroup mySeekBarLayout = (ViewGroup)inflator.inflate(R.layout.my_seek_bar_pref, null);
	  mSeekBar = (SeekBar) mySeekBarLayout.findViewById(R.id.pref_seekbar);
	  if (shouldPersist())
		  mValue = getPersistedInt(mDefault);

	  mSeekBar.setMax(mMax);
	  mSeekBar.setProgress(mValue);
	  mSeekBar.setOnSeekBarChangeListener(this);
	  mCurrentValue = (TextView)mySeekBarLayout.findViewById(R.id.pref_current_value);
	  mMaxValue = (TextView)mySeekBarLayout.findViewById(R.id.pref_max_value);
	  mCurrentValue.setText(Integer.toString(mValue));
	  mMaxValue.setText(Integer.toString(mMax));
    
    return mySeekBarLayout;
  }
  
	@Override 
	protected void onBindDialogView(View v) {
		super.onBindDialogView(v);
		mSeekBar.setMax(mMax);
		mMaxValue.setText(Integer.toString(mMax));
		mSeekBar.setProgress(mValue);
		mCurrentValue.setText(Integer.toString(mValue));
	}*/

  @Override
  protected void onSetInitialValue(boolean restore, Object defaultValue)  
  {
    super.onSetInitialValue(restore, defaultValue);
    if (restore) 
      mValue = shouldPersist() ? getPersistedInt(mDefault) : 0;
    else 
      mValue = (Integer)defaultValue;
    
    if (mCurrentValue != null)
    	mCurrentValue.setText(Integer.toString(mValue));
  }

  public void onProgressChanged(SeekBar seek, int value, boolean fromTouch)
  {
    if (shouldPersist())
      persistInt(value);
    
    callChangeListener(new Integer(value));
    
    mValue = value;
    
    if (mCurrentValue != null)
    	mCurrentValue.setText(Integer.toString(mValue));
  }
  public void onStartTrackingTouch(SeekBar seek) {}
  public void onStopTrackingTouch(SeekBar seek) {}

  public void setMax(int max) { mMax = max; }
  public int getMax() { return mMax; }

  public void setProgress(int progress) { 
	  mValue = progress;
	  if (mSeekBar != null)
	  {
		  mSeekBar.setProgress(progress);
		  mCurrentValue.setText(Integer.toString(mValue));
	  }
  }
  
  public int getProgress() { return mValue; }
  
  
}

