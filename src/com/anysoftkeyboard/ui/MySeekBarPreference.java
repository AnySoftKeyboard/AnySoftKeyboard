/* The following code was written by Matthew Wiggins 
 * and is released under the APACHE 2.0 license 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.anysoftkeyboard.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.preference.DialogPreference;
import android.widget.SeekBar;
import android.widget.LinearLayout;


public class MySeekBarPreference extends DialogPreference implements SeekBar.OnSeekBarChangeListener
{
  private static final String androidns="http://schemas.android.com/apk/res/android";

  private SeekBar mSeekBar;
  private Context mContext;

  private int mDefault, mMax, mValue = 0;

  public MySeekBarPreference(Context context, AttributeSet attrs) { 
    super(context,attrs); 
    mContext = context;

    mDefault = attrs.getAttributeIntValue(androidns,"defaultValue", 0);
    mMax = attrs.getAttributeIntValue(androidns,"max", 100);
  }
  
  @Override 
  protected View onCreateDialogView() {
    LinearLayout layout = new LinearLayout(mContext);
    layout.setOrientation(LinearLayout.VERTICAL);
    layout.setPadding(6,6,6,6);
    
    mSeekBar = new SeekBar(mContext);
    mSeekBar.setOnSeekBarChangeListener(this);
    layout.addView(mSeekBar, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

    if (shouldPersist())
      mValue = getPersistedInt(mDefault);

    mSeekBar.setMax(mMax);
    mSeekBar.setProgress(mValue);
    
    return layout;
  }
  @Override 
  protected void onBindDialogView(View v) {
    super.onBindDialogView(v);
    mSeekBar.setMax(mMax);
    mSeekBar.setProgress(mValue);    
  }
  @Override
  protected void onSetInitialValue(boolean restore, Object defaultValue)  
  {
    super.onSetInitialValue(restore, defaultValue);
    if (restore) 
      mValue = shouldPersist() ? getPersistedInt(mDefault) : 0;
    else 
      mValue = (Integer)defaultValue;
  }

  public void onProgressChanged(SeekBar seek, int value, boolean fromTouch)
  {
//    String t = String.valueOf(value);
//    mValueText.setText(mSuffix == null ? t : t.concat(mSuffix));
    if (shouldPersist())
      persistInt(value);
    
    callChangeListener(new Integer(value));
  }
  public void onStartTrackingTouch(SeekBar seek) {}
  public void onStopTrackingTouch(SeekBar seek) {}

  public void setMax(int max) { mMax = max; }
  public int getMax() { return mMax; }

  public void setProgress(int progress) { 
    mValue = progress;
    if (mSeekBar != null)
      mSeekBar.setProgress(progress); 
  }
  
  public int getProgress() { return mValue; }
}

