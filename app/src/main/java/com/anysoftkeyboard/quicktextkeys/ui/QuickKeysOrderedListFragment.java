package com.anysoftkeyboard.quicktextkeys.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.anysoftkeyboard.quicktextkeys.QuickTextKey;
import com.anysoftkeyboard.ui.settings.MainSettingsActivity;
import com.anysoftkeyboard.utils.Logger;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

import java.util.HashSet;
import java.util.List;

public class QuickKeysOrderedListFragment extends Fragment {
    private static final String TAG = "QuickKeysOrderedListFragment";

    private final HashSet<CharSequence> mEnabledAddOns = new HashSet<>();
    private final CompoundButton.OnCheckedChangeListener mOnItemCheckedListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            QuickTextKey key = (QuickTextKey) buttonView.getTag();
            if (isChecked) {
                mEnabledAddOns.add(key.getId());
            } else {
                mEnabledAddOns.remove(key.getId());
            }
        }
    };
    private List<QuickTextKey> mAllQuickKeysAddOns;
    private final ItemTouchHelper.Callback mItemTouchCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            final int to = target.getAdapterPosition();
            final int from = viewHolder.getAdapterPosition();
            QuickTextKey temp = mAllQuickKeysAddOns.remove(from);
            mAllQuickKeysAddOns.add(to, temp);
            //a moved item MUST BE ENABLED
            mEnabledAddOns.add(temp.getId());
            recyclerView.getAdapter().notifyItemMoved(from, to);
            //making sure `to` is visible
            recyclerView.scrollToPosition(to);
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

        }
    };
    private final ItemTouchHelper mRecyclerViewItemTouchHelper = new ItemTouchHelper(mItemTouchCallback);

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.recycler_view_only_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Context appContext = getActivity().getApplicationContext();
        mAllQuickKeysAddOns = AnyApplication.getQuickTextKeyFactory(appContext).getEnabledAddOns();
        Logger.d(TAG, "Got %d enabled quick-key addons", mAllQuickKeysAddOns.size());
        for (QuickTextKey quickTextKey : mAllQuickKeysAddOns) {
            mEnabledAddOns.add(quickTextKey.getId());
            Logger.d(TAG, "Adding %s to enabled hash-set", quickTextKey.getId());
        }
        for (QuickTextKey quickTextKey : AnyApplication.getQuickTextKeyFactory(appContext).getAllAddOns()) {
            Logger.d(TAG, "Checking if %s is in enabled hash-set", quickTextKey.getId());
            if (!mEnabledAddOns.contains(quickTextKey.getId())) {
                Logger.d(TAG, "%s is not in the enabled list, adding it to the end of the list", quickTextKey.getId());
                mAllQuickKeysAddOns.add(quickTextKey);
            }
        }
        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(appContext));
        recyclerView.setAdapter(new Adapter());

        mRecyclerViewItemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    public void onStart() {
        super.onStart();
        MainSettingsActivity.setActivityTitle(this, getString(R.string.quick_text_keys_order_dialog_title));
    }

    @Override
    public void onStop() {
        super.onStop();
        AnyApplication.getQuickTextKeyFactory(getContext()).setAddOnsOrder(mAllQuickKeysAddOns);
    }

    private static class OrderedListViewHolder extends RecyclerView.ViewHolder {
        private final CheckBox mTitle;

        OrderedListViewHolder(View itemView, CompoundButton.OnCheckedChangeListener onItemCheckedListener) {
            super(itemView);
            mTitle = (CheckBox) itemView.findViewById(R.id.orderedListTitle);
            mTitle.setOnCheckedChangeListener(onItemCheckedListener);
        }
    }

    private class Adapter extends RecyclerView.Adapter<OrderedListViewHolder> {
        private final LayoutInflater mLayoutInflater;

        Adapter() {
            mLayoutInflater = LayoutInflater.from(getActivity());
        }

        @Override
        public OrderedListViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            return new OrderedListViewHolder(mLayoutInflater.inflate(R.layout.ordered_list_item, viewGroup, false), mOnItemCheckedListener);
        }

        @Override
        public void onBindViewHolder(OrderedListViewHolder viewHolder, int position) {
            QuickTextKey value = mAllQuickKeysAddOns.get(position);
            viewHolder.mTitle.setTag(value);
            viewHolder.mTitle.setText(value.getKeyOutputText() + " " + value.getName());
            viewHolder.mTitle.setChecked(mEnabledAddOns.contains(value.getId()));
        }

        @Override
        public int getItemCount() {
            return mAllQuickKeysAddOns.size();
        }
    }
}
