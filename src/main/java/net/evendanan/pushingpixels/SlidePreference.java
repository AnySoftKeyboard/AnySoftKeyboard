/*
 * Copyright (c) 2013 Menny Even-Danan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* The following code was written by Matthew Wiggins
 * and is released under the APACHE 2.0 license 
 * 
 * additional code was written by Menny Even Danan, and is also released under APACHE 2.0 license 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package net.evendanan.pushingpixels;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.menny.android.anysoftkeyboard.R;


public class SlidePreference extends Preference implements SeekBar.OnSeekBarChangeListener {

    private SeekBar mSeekBar;
    private TextView mMaxValue, mCurrentValue, mMinValue;
    private String mTitle;

    private int mDefault = 50, mMax = 100, mMin = 0, mValue = 0;

    public SlidePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.slide_pref);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.SlidePreferenceAttributes);
        mDefault = array.getInteger(R.styleable.SlidePreferenceAttributes_android_defaultValue, 0);
        mMax = array.getInteger(R.styleable.SlidePreferenceAttributes_slideMaximum, 100);
        mMin = array.getInteger(R.styleable.SlidePreferenceAttributes_slideMinimum, 0);
        int titleResId = array.getResourceId(R.styleable.SlidePreferenceAttributes_android_title, 0);
        if (titleResId == 0)
            mTitle = array.getString(R.styleable.SlidePreferenceAttributes_android_title);
        else
            mTitle = context.getString(titleResId);
        array.recycle();
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        View mySeekBarLayout = super.onCreateView(parent);
        mSeekBar = (SeekBar) mySeekBarLayout.findViewById(R.id.pref_seekbar);
        if (shouldPersist())
            mValue = getPersistedInt(mDefault);

        mCurrentValue = (TextView) mySeekBarLayout.findViewById(R.id.pref_current_value);
        mMaxValue = (TextView) mySeekBarLayout.findViewById(R.id.pref_max_value);
        mMinValue = (TextView) mySeekBarLayout.findViewById(R.id.pref_min_value);
        mCurrentValue.setText(Integer.toString(mValue));
        ((TextView) mySeekBarLayout.findViewById(R.id.pref_title)).setText(mTitle);

        writeBoundaries();

        mSeekBar.setMax(mMax - mMin);
        mSeekBar.setProgress(mValue - mMin);
        mSeekBar.setOnSeekBarChangeListener(this);

        return mySeekBarLayout;
    }

    @Override
    protected void onSetInitialValue(boolean restore, Object defaultValue) {
        super.onSetInitialValue(restore, defaultValue);
        if (restore)
            mValue = shouldPersist() ? getPersistedInt(mDefault) : mMin;
        else
            mValue = (Integer) defaultValue;

        if (mValue > mMax) mValue = mMax;
        if (mValue < mMin) mValue = mMin;

        if (mCurrentValue != null)
            mCurrentValue.setText(Integer.toString(mValue));
    }

    public void onProgressChanged(SeekBar seek, int value, boolean fromTouch) {
        mValue = value + mMin;
        if (mValue > mMax) mValue = mMax;
        if (mValue < mMin) mValue = mMin;

        if (shouldPersist()) persistInt(mValue);
        callChangeListener(Integer.valueOf(mValue));

        if (mCurrentValue != null)
            mCurrentValue.setText(Integer.toString(mValue));
    }

    public void onStartTrackingTouch(SeekBar seek) {
    }

    public void onStopTrackingTouch(SeekBar seek) {
    }

    private void writeBoundaries() {
        mMaxValue.setText(Integer.toString(mMax));
        mMinValue.setText(Integer.toString(mMin));
        if (mValue > mMax) mValue = mMax;
        if (mValue < mMin) mValue = mMin;
        if (mCurrentValue != null)
            mCurrentValue.setText(Integer.toString(mValue));
    }

    public void setMax(int max) {
        mMax = max;
        writeBoundaries();
    }

    public int getMax() {
        return mMax;
    }

    public void setMin(int min) {
        mMin = min;
        writeBoundaries();
    }

    public int getMin() {
        return mMin;
    }

    public void setProgress(int progress) {
        mValue = progress;
        if (mValue > mMax) mValue = mMax;
        if (mValue < mMin) mValue = mMin;

        if (mSeekBar != null) {
            mSeekBar.setProgress(progress - mMin);
            mCurrentValue.setText(Integer.toString(mValue));
        }
    }

    public int getProgress() {
        return mValue;
    }
}

