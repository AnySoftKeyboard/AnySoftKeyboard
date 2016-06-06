/*
 * Copyright (c) 2016 Menny Even-Danan
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

package com.anysoftkeyboard.ui.settings;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.anysoftkeyboard.addons.AddOn;
import com.anysoftkeyboard.keyboards.views.DemoAnyKeyboardView;
import com.anysoftkeyboard.ui.settings.widget.AddOnStoreSearchView;
import com.anysoftkeyboard.utils.Log;
import com.menny.android.anysoftkeyboard.R;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractKeyboardAddOnsBrowserFragment<E extends AddOn> extends Fragment {

    private final List<String> mEnabledAddOnsIds = new ArrayList<>();
    private List<E> mAllAddOns;
    private RecyclerView mRecyclerView;
    private int mPreviousSingleSelectedItem = -1;

    @Override
    public View onCreateView(LayoutInflater paramLayoutInflater, ViewGroup paramViewGroup, Bundle paramBundle) {
        return paramLayoutInflater.inflate(getRecyclerViewLayoutId(), paramViewGroup, false);
    }

    @LayoutRes
    protected int getRecyclerViewLayoutId() {
        return R.layout.recycler_view_only_layout;
    }

    @NonNull
    protected abstract String getFragmentTag();

    @StringRes
    protected abstract int getFragmentTitleResourceId();

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Context appContext = getActivity().getApplicationContext();
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setLayoutManager(createLayoutManager(appContext));
        mRecyclerView.setAdapter(new DemoKeyboardAdapter());
    }

    @Override
    public void onStart() {
        super.onStart();
        mAllAddOns = getAllAvailableAddOns();
        mEnabledAddOnsIds.clear();
        for (E addOn : getEnabledAddOns()) mEnabledAddOnsIds.add(addOn.getId());
        Log.d(getFragmentTag(), "Got %d available addons and %d enabled addons", mAllAddOns.size(), mEnabledAddOnsIds.size());
        mRecyclerView.getAdapter().notifyDataSetChanged();
        MainSettingsActivity.setActivityTitle(this, getString(getFragmentTitleResourceId()));
    }

    @NonNull
    private LinearLayoutManager createLayoutManager(@NonNull Context appContext) {
        final boolean isLandscape = appContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        return new LinearLayoutManager(appContext, isLandscape ? LinearLayoutManager.HORIZONTAL : LinearLayoutManager.VERTICAL, false);
    }

    @NonNull
    protected abstract List<E> getEnabledAddOns();

    @NonNull
    protected abstract List<E> getAllAvailableAddOns();

    protected abstract void onEnabledAddOnsChanged(@NonNull List<String> newEnabledAddOns);

    protected abstract boolean isSingleSelectedAddOn();

    protected abstract void applyAddOnToDemoKeyboardView(@NonNull final E addOn, @NonNull final DemoAnyKeyboardView demoKeyboardView);

    @Nullable
    protected abstract String getMarketSearchKeyword();
    @StringRes
    protected abstract int getMarketSearchTitle();

    private class KeyboardAddOnViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final DemoAnyKeyboardView mDemoKeyboardView;
        private final ImageView mAddOnEnabledView;
        private final TextView mAddOnTitle;
        private final TextView mAddOnDescription;
        private E mAddOn;

        public KeyboardAddOnViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mDemoKeyboardView = (DemoAnyKeyboardView) itemView.findViewById(R.id.demo_keyboard_view);
            mAddOnEnabledView = (ImageView) itemView.findViewById(R.id.enabled_image);
            mAddOnTitle = (TextView) itemView.findViewById(R.id.title);
            mAddOnDescription = (TextView) itemView.findViewById(R.id.subtitle);
        }

        private void bindToAddOn(@NonNull E addOn) {
            mAddOn = addOn;
            mAddOnTitle.setText(addOn.getName());
            mAddOnDescription.setText(addOn.getDescription());
            final boolean isEnabled = mEnabledAddOnsIds.contains(addOn.getId());
            if (isEnabled) {
                if (isSingleSelectedAddOn()) mAddOnEnabledView.setVisibility(View.VISIBLE);
                mAddOnEnabledView.setImageResource(R.drawable.ic_accept);
            } else {
                if (isSingleSelectedAddOn())
                    mAddOnEnabledView.setVisibility(View.INVISIBLE);
                else
                    mAddOnEnabledView.setImageResource(R.drawable.ic_cancel);
            }
            applyAddOnToDemoKeyboardView(addOn, mDemoKeyboardView);
        }

        @Override
        public void onClick(View v) {
            final boolean isEnabled = mEnabledAddOnsIds.contains(mAddOn.getId());
            if (isSingleSelectedAddOn()) {
                //in this case, only ENABLING is done, and only this clicked item
                if (isEnabled) return;
                mEnabledAddOnsIds.clear();
                mEnabledAddOnsIds.add(mAddOn.getId());
            } else {
                if (isEnabled) {
                    mEnabledAddOnsIds.remove(mAddOn.getId());
                } else {
                    mEnabledAddOnsIds.add(mAddOn.getId());
                }
            }
            onEnabledAddOnsChanged(mEnabledAddOnsIds);
            if (isSingleSelectedAddOn()) {
                //also notifying about the previous item being automatically unselected
                if (mPreviousSingleSelectedItem == -1) mRecyclerView.getAdapter().notifyDataSetChanged();
                else mRecyclerView.getAdapter().notifyItemChanged(mPreviousSingleSelectedItem);
                mPreviousSingleSelectedItem = getAdapterPosition();
            }
            mRecyclerView.getAdapter().notifyItemChanged(getAdapterPosition());
        }
    }

    private class DemoKeyboardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final LayoutInflater mLayoutInflater;

        DemoKeyboardAdapter() {
            mLayoutInflater = LayoutInflater.from(getActivity());
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == 0) {
                View itemView = mLayoutInflater.inflate(R.layout.keyboard_demo_recycler_view_item, parent, false);
                return new KeyboardAddOnViewHolder(itemView);
            } else {
                AddOnStoreSearchView searchView = new AddOnStoreSearchView(getActivity(), null);
                searchView.setTag(getMarketSearchKeyword());
                searchView.setTitle(getText(getMarketSearchTitle()));
                return new RecyclerView.ViewHolder(searchView){/*empty implementation*/};
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof AbstractKeyboardAddOnsBrowserFragment.KeyboardAddOnViewHolder) {
                E addOn = mAllAddOns.get(position);
                ((AbstractKeyboardAddOnsBrowserFragment<E>.KeyboardAddOnViewHolder)holder).bindToAddOn(addOn);
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == mAllAddOns.size()) return 1;
            else return 0;
        }

        @Override
        public int getItemCount() {
            final int extra = getMarketSearchKeyword() != null? 1 : 0;
            return mAllAddOns.size() + extra;
        }
    }
}