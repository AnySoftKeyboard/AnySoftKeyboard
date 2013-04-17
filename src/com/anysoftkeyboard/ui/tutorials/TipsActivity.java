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

package com.anysoftkeyboard.ui.tutorials;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import com.anysoftkeyboard.ui.settings.BottomRowSelector;
import com.anysoftkeyboard.ui.settings.TopRowSelector;
import com.anysoftkeyboard.utils.Log;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

import java.util.ArrayList;

public class TipsActivity extends BaseTutorialActivity implements OnCheckedChangeListener {

    private static final String TAG = "ASK TIPS";
    public static final String EXTRA_SHOW_ALL_TIPS = "EXTRA_SHOW_ALL_TIPS";
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
        if (savedInstanceState != null) {
            mCurrentTipIndex = savedInstanceState.getInt("mCurrentTipIndex");
        }

        mTipContainer = (ViewGroup) findViewById(R.id.tips_layout_container);

        mAppPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        final CheckBox showNotifications = (CheckBox) findViewById(R.id.show_tips_next_time);
        showNotifications.setChecked(AnyApplication.getConfig().getShowTipsNotification());

        showNotifications.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                AnyApplication.getConfig().setShowTipsNotification(!AnyApplication.getConfig().getShowTipsNotification());
            }
        });
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
        final boolean showAllTips = getIntent().getBooleanExtra(EXTRA_SHOW_ALL_TIPS, false);
        mLayoutsToShow.clear();
        getTipsLayouts(getApplicationContext(), showAllTips, mLayoutsToShow, mAppPrefs);

        if (mLayoutsToShow.size() == 0) {
            finish();
        } else {
            showTip();
        }
    }

    public static void getTipsLayouts(Context appContext, final boolean showAllTips, ArrayList<Integer> layoutsToShow, SharedPreferences appPrefs) {
        Resources res = appContext.getResources();
        int currentTipLoadingIndex = 1;
        boolean haveMore = true;
        while (haveMore) {
            final String layoutResourceName = "tip_layout_" + currentTipLoadingIndex;
            Log.d(TAG, "Looking for tip " + layoutResourceName);
            final int resId = res.getIdentifier(layoutResourceName, "layout", appContext.getPackageName());
            haveMore = (resId != 0);
            if (resId != 0) {
                if (showAllTips || !appPrefs.getBoolean(layoutResourceName, false)) {
                    Log.d(TAG, "Got a tip #" + currentTipLoadingIndex + " which is " + layoutResourceName);
                    layoutsToShow.add(Integer.valueOf(resId));
                }
            }
            currentTipLoadingIndex++;
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
        resName = resName.substring(resName.lastIndexOf("/") + 1, resName.length());
        Log.d(TAG, "Seen tip " + resName + ".");
        Editor e = mAppPrefs.edit();
        e.putBoolean(resName, true);
        e.commit();
    }

    @Override
    protected void setClickHandler(View content) {
        Log.d(TAG, "v is " + content.getClass().getName());
        if (content instanceof CheckBox) {
            CheckBox checkBox = (CheckBox) content;
            switch (checkBox.getId()) {
                case R.id.settings_key_press_vibration:
                    Log.d(TAG, "Vib listener");
                    int vibrationDuration = Integer.parseInt(mAppPrefs.getString(
                            getString(R.string.settings_key_vibrate_on_key_press_duration),
                            getString(R.string.settings_default_vibrate_on_key_press_duration)));
                    checkBox.setChecked(vibrationDuration > 0);
                    checkBox.setOnCheckedChangeListener(this);
                    break;
                case R.id.settings_key_press_sound:
                    Log.d(TAG, "Sound listener");
                    boolean soundOn = mAppPrefs.getBoolean(getString(R.string.settings_key_sound_on), getResources().getBoolean(R.bool.settings_default_sound_on));
                    checkBox.setChecked(soundOn);
                    checkBox.setOnCheckedChangeListener(this);
                    break;
            }
        } else
            super.setClickHandler(content);
    }

    public void onCheckedChanged(CompoundButton checkBox, boolean isChecked) {
        Editor e = mAppPrefs.edit();
        switch (checkBox.getId()) {
            case R.id.settings_key_press_vibration:
                Log.d(TAG, "Vib touched: " + isChecked);
                e.putString(getString(R.string.settings_key_vibrate_on_key_press_duration),
                        isChecked ? "17" : "0");
                break;
            case R.id.settings_key_press_sound:
                Log.d(TAG, "Sound touched: " + isChecked);
                e.putBoolean(getString(R.string.settings_key_sound_on), isChecked);
                break;
        }
        e.commit();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
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
            case R.id.tips_goto_bottom_row_settings:
                Intent startBottomRowSettingsIntent = new Intent(this, BottomRowSelector.class);
                startActivity(startBottomRowSettingsIntent);
                break;
            //super
            default:
                super.onClick(v);
                break;
        }
    }
}
