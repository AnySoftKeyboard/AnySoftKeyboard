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

import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.anysoftkeyboard.utils.Log;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

import net.evendanan.pushingpixels.PassengerFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TipsFragment extends PassengerFragment {

    private static final String TAG = "ASK TIPS";

    private static final String EXTRA_TIPS_TO_SHOW = "EXTRA_TIPS_TO_SHOW";

    public static final int SHOW_ALL_TIPS = -1;
    public static final int SHOW_UNVIEWED_TIPS = -2;

    public static Bundle createArgs(int tipsToShow) {
        Bundle b = new Bundle();
        b.putInt(EXTRA_TIPS_TO_SHOW, tipsToShow);
        return b;
    }

    public static TipsFragment createFragment(int tipsToShow) {
        TipsFragment fragment = new TipsFragment();
        fragment.setArguments(createArgs(tipsToShow));

        return fragment;
    }

    private final ArrayList<Integer> mLayoutsToShow = new ArrayList<Integer>();

    private ViewPager mPager;

    private int mLogToShow = SHOW_ALL_TIPS;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLogToShow = getArguments().getInt(EXTRA_TIPS_TO_SHOW);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tips_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mLayoutsToShow.clear();
        TipLayoutsSupport.getAvailableTipsLayouts(getActivity().getApplicationContext(), mLayoutsToShow);
        if (mLogToShow == SHOW_UNVIEWED_TIPS)
            TipLayoutsSupport.filterOutViewedTips(getResources(), mLayoutsToShow,
                    PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()));

        mPager = (ViewPager)view.findViewById(R.id.tips_pager);
        mPager.setAdapter(new TipFragmentAdapter(getChildFragmentManager()));

        final CheckBox showNotifications = (CheckBox) view.findViewById(R.id.show_tips_next_time);
        showNotifications.setChecked(AnyApplication.getConfig().getShowTipsNotification());

        showNotifications.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                AnyApplication.getConfig().setShowTipsNotification(!AnyApplication.getConfig().getShowTipsNotification());
            }
        });

        view.findViewById(R.id.tips_pager_swipe_hint).setVisibility(View.VISIBLE);
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().setTitle(getString(R.string.tips_title));
    }

    private class TipFragmentAdapter extends FragmentStatePagerAdapter {

        public TipFragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            int tipResId = mLayoutsToShow.get(i);
            return TipFragment.create(tipResId);
        }

        @Override
        public int getCount() {
            return mLayoutsToShow.size();
        }
    }

    public static class TipFragment extends Fragment {

        private static final String TIP_RES_ID = "TIP_RES_ID";

        public static TipFragment create(int tipResId) {
            Bundle b = new Bundle();
            b.putInt(TIP_RES_ID, tipResId);
            TipFragment fragment = new TipFragment();
            fragment.setArguments(b);

            return fragment;
        }

        private int mTipResId;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mTipResId = getArguments() != null? getArguments().getInt(TIP_RES_ID) : 0;
            if (mTipResId == 0) {
                mTipResId = getTipToUseOnNoneGiven();
            }
        }

        protected int getTipToUseOnNoneGiven() {
            throw new IllegalArgumentException("Missing tip res ID!");
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(mTipResId, container, false);
        }

        @Override
        public void onStart() {
            super.onStart();
            String resName = getResources().getResourceName(mTipResId);
            resName = resName.substring(resName.lastIndexOf("/") + 1, resName.length());
            Log.d(TAG, "Seen tip " + resName + ".");
            Editor e = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).edit();
            e.putBoolean(resName, true);
            e.commit();
        }
    }

    public static class RandomTipFragment extends TipFragment {
        @Override
        protected int getTipToUseOnNoneGiven() {
            //picking a random tip
            List<Integer> tipResIds = new ArrayList<>();
            TipLayoutsSupport.getAvailableTipsLayouts(getActivity().getApplicationContext(), tipResIds);
            int randomIndex = new Random().nextInt(tipResIds.size());
            return tipResIds.get(randomIndex);
        }
    }
}
