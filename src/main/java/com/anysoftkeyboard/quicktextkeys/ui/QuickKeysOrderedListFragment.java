package com.anysoftkeyboard.quicktextkeys.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.anysoftkeyboard.quicktextkeys.QuickTextKey;
import com.anysoftkeyboard.quicktextkeys.QuickTextKeyFactory;
import com.anysoftkeyboard.utils.Log;
import com.emtronics.dragsortrecycler.DragSortRecycler;
import com.menny.android.anysoftkeyboard.R;

import net.evendanan.pushingpixels.PassengerFragmentSupport;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class QuickKeysOrderedListFragment extends Fragment {
	private static final String TAG = "QuickKeysOrderedListFragment";

	private final HashSet<String> mEnabledAddOns = new HashSet<>();
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

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.ordered_list_pref, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		Context appContext = getActivity().getApplicationContext();
		mAllQuickKeysAddOns = QuickTextKeyFactory.getOrderedEnabledQuickKeys(appContext);
		Log.d(TAG, "Got %d enabled quick-key addons", mAllQuickKeysAddOns.size());
		for (QuickTextKey quickTextKey : mAllQuickKeysAddOns) {
			mEnabledAddOns.add(quickTextKey.getId());
			Log.d(TAG, "Adding %s to enabled hash-set", quickTextKey.getId());
		}
		for (QuickTextKey quickTextKey : QuickTextKeyFactory.getAllAvailableQuickKeys(appContext)) {
			Log.d(TAG, "Checking if %s is in enabled hash-set", quickTextKey.getId());
			if (!mEnabledAddOns.contains(quickTextKey.getId())) {
				Log.d(TAG, "%s is not in the enabled list, adding it to the end of the list", quickTextKey.getId());
				mAllQuickKeysAddOns.add(quickTextKey);
			}
		}
		RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
		recyclerView.setHasFixedSize(true);
		recyclerView.setLayoutManager(new LinearLayoutManager(appContext));
		recyclerView.setAdapter(new Adapter());

		recyclerView.setItemAnimator(null);

		DragSortRecycler dragSortRecycler = new DragSortRecycler();
		dragSortRecycler.setViewHandleId(R.id.orderedListSlider);

		dragSortRecycler.setOnItemMovedListener(new DragSortRecycler.OnItemMovedListener() {
			@Override
			public void onItemMoved(RecyclerView rv, int from, int to) {
				QuickTextKey temp = mAllQuickKeysAddOns.remove(from);
				mAllQuickKeysAddOns.add(to, temp);
				//a moved item MUST BE ENABLED
				mEnabledAddOns.add(temp.getId());
				rv.getAdapter().notifyItemMoved(from, to);
				//making sure `to` is visible
				rv.scrollToPosition(to);
			}
		});

		recyclerView.addItemDecoration(dragSortRecycler);
		recyclerView.addOnItemTouchListener(dragSortRecycler);
		recyclerView.setOnScrollListener(dragSortRecycler.getScrollListener());
	}

	@Override
	public void onStart() {
		super.onStart();
		PassengerFragmentSupport.setActivityTitle(this, getString(R.string.quick_text_keys_order_dialog_title));
	}

	@Override
	public void onStop() {
		super.onStop();
		ArrayList<QuickTextKey> enabledAddons = new ArrayList<>(mEnabledAddOns.size());
		for (QuickTextKey key : mAllQuickKeysAddOns) {
			if (mEnabledAddOns.contains(key.getId())) {
				enabledAddons.add(key);
			}
		}
		QuickTextKeyFactory.storeOrderedEnabledQuickKeys(getActivity(), enabledAddons);
	}

	private static class OrderedListViewHolder extends RecyclerView.ViewHolder {
		private final CheckBox title;

		public OrderedListViewHolder(View itemView, CompoundButton.OnCheckedChangeListener onItemCheckedListener) {
			super(itemView);
			title = (CheckBox) itemView.findViewById(R.id.orderedListTitle);
			title.setOnCheckedChangeListener(onItemCheckedListener);
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
			viewHolder.title.setTag(value);
			viewHolder.title.setText(value.getName());
			viewHolder.title.setChecked(mEnabledAddOns.contains(value.getId()));
		}

		@Override
		public int getItemCount() {
			return mAllQuickKeysAddOns.size();
		}
	}
}
