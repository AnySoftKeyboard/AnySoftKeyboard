package com.anysoftkeyboard.quicktextkeys.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.preference.DialogPreference;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class QuickKeysOrderedListPreference extends DialogPreference {
	private static final String TAG = "QuickKeysOrderedListPreference";

	private final HashSet<String> mEnabledAddOns = new HashSet<>();
	private List<QuickTextKey> mAllQuickKeysAddOns;
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

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public QuickKeysOrderedListPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		setDialogLayoutResource(R.layout.ordered_list_pref);
	}

	public QuickKeysOrderedListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		setDialogLayoutResource(R.layout.ordered_list_pref);
	}

	public QuickKeysOrderedListPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		setDialogLayoutResource(R.layout.ordered_list_pref);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public QuickKeysOrderedListPreference(Context context) {
		super(context);
		setDialogLayoutResource(R.layout.ordered_list_pref);
	}

	@Override
	protected void onBindDialogView(@NonNull View view) {
		super.onBindDialogView(view);
		mAllQuickKeysAddOns = QuickTextKeyFactory.getOrderedEnabledQuickKeys(getContext());
		Log.d(TAG, "Got %d enabled quick-key addons", mAllQuickKeysAddOns.size());
		for (QuickTextKey quickTextKey : mAllQuickKeysAddOns) {
			mEnabledAddOns.add(quickTextKey.getId());
			Log.d(TAG, "Adding %s to enabled hash-set", quickTextKey.getId());
		}
		for (QuickTextKey quickTextKey : QuickTextKeyFactory.getAllAvailableQuickKeys(getContext())) {
			Log.d(TAG, "Checking if %s is in enabled hash-set", quickTextKey.getId());
			if (!mEnabledAddOns.contains(quickTextKey.getId())) {
				Log.d(TAG, "%s is not in the enabled list, adding it to the end of the list", quickTextKey.getId());
				mAllQuickKeysAddOns.add(quickTextKey);
			}
		}
		RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
		recyclerView.setHasFixedSize(true);
		recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
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
			}
		});

		recyclerView.addItemDecoration(dragSortRecycler);
		recyclerView.addOnItemTouchListener(dragSortRecycler);
		recyclerView.setOnScrollListener(dragSortRecycler.getScrollListener());
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if (positiveResult) {
			ArrayList<QuickTextKey> enabledAddons = new ArrayList<>(mEnabledAddOns.size());
			for (QuickTextKey key : mAllQuickKeysAddOns) {
				if (mEnabledAddOns.contains(key.getId())) {
					enabledAddons.add(key);
				}
			}
			QuickTextKeyFactory.storeOrderedEnabledQuickKeys(getContext(), enabledAddons);
		}
		super.onDialogClosed(positiveResult);
	}

	public void performOnClick() {
		onClick();
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
			mLayoutInflater = LayoutInflater.from(getContext());
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
