package net.evendanan.pixel;
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

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.widget.SeekBar;
import android.widget.TextView;

public class SlidePreference extends Preference implements SeekBar.OnSeekBarChangeListener {

    private TextView mMaxValue;
    private TextView mCurrentValue;
    private TextView mMinValue;
    private String mTitle;

    private final int mDefault;
    private final int mMax;
    private final int mMin;
    private int mValue;

    public SlidePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.slide_pref);
        TypedArray array =
                context.obtainStyledAttributes(attrs, R.styleable.SlidePreferenceAttributes);
        mDefault = array.getInteger(R.styleable.SlidePreferenceAttributes_android_defaultValue, 0);
        mMax = array.getInteger(R.styleable.SlidePreferenceAttributes_slideMaximum, 100);
        mMin = array.getInteger(R.styleable.SlidePreferenceAttributes_slideMinimum, 0);
        int titleResId =
                array.getResourceId(R.styleable.SlidePreferenceAttributes_android_title, 0);
        if (titleResId == 0)
            mTitle = array.getString(R.styleable.SlidePreferenceAttributes_android_title);
        else mTitle = context.getString(titleResId);
        array.recycle();
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        if (shouldPersist()) mValue = getPersistedInt(mDefault);

        mCurrentValue = (TextView) holder.findViewById(R.id.pref_current_value);
        mMaxValue = (TextView) holder.findViewById(R.id.pref_max_value);
        mMinValue = (TextView) holder.findViewById(R.id.pref_min_value);
        mCurrentValue.setText(Integer.toString(mValue));
        ((TextView) holder.findViewById(R.id.pref_title)).setText(mTitle);

        writeBoundaries();

        SeekBar seekBar = (SeekBar) holder.findViewById(R.id.pref_seekbar);
        seekBar.setMax(mMax - mMin);
        seekBar.setProgress(mValue - mMin);
        seekBar.setOnSeekBarChangeListener(this);
    }

    @Override
    protected void onSetInitialValue(boolean restore, Object defaultValue) {
        super.onSetInitialValue(restore, defaultValue);
        if (restore) mValue = shouldPersist() ? getPersistedInt(mDefault) : mMin;
        else mValue = (Integer) defaultValue;

        if (mValue > mMax) mValue = mMax;
        if (mValue < mMin) mValue = mMin;

        if (mCurrentValue != null) mCurrentValue.setText(Integer.toString(mValue));
    }

    @Override
    public void onProgressChanged(SeekBar seek, int value, boolean fromTouch) {
        mValue = value + mMin;
        if (mValue > mMax) mValue = mMax;
        if (mValue < mMin) mValue = mMin;

        if (shouldPersist()) persistInt(mValue);
        callChangeListener(mValue);

        if (mCurrentValue != null) mCurrentValue.setText(Integer.toString(mValue));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seek) {}

    @Override
    public void onStopTrackingTouch(SeekBar seek) {}

    private void writeBoundaries() {
        mMaxValue.setText(Integer.toString(mMax));
        mMinValue.setText(Integer.toString(mMin));
        if (mValue > mMax) mValue = mMax;
        if (mValue < mMin) mValue = mMin;
        if (mCurrentValue != null) mCurrentValue.setText(Integer.toString(mValue));
    }

    public int getMax() {
        return mMax;
    }

    public int getMin() {
        return mMin;
    }

    public int getValue() {
        return mValue;
    }
}
