package com.anysoftkeyboard.quicktextkeys.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.DialogPreference;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.anysoftkeyboard.quicktextkeys.QuickTextKey;
import com.anysoftkeyboard.quicktextkeys.QuickTextKeyFactory;
import com.emtronics.dragsortrecycler.DragSortRecycler;
import com.menny.android.anysoftkeyboard.R;

import java.util.ArrayList;

public class QuickKeysOrderedListPreference extends DialogPreference {
	private ArrayList<QuickTextKey> mQuickKeysAddOnsToStore;

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
		mQuickKeysAddOnsToStore = QuickTextKeyFactory.getOrderedEnabledQuickKeys(getContext());
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
				QuickTextKey temp = mQuickKeysAddOnsToStore.remove(from);
				mQuickKeysAddOnsToStore.add(to, temp);
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
			QuickTextKeyFactory.storeOrderedEnabledQuickKeys(getContext(), mQuickKeysAddOnsToStore);
		}
		super.onDialogClosed(positiveResult);
	}

	protected boolean persistString(String value) {
		if (shouldPersist()) {
			String currentValue = getPersistString();
			// Shouldn't store null
			if (currentValue.equals(value)) {
				// It's already there, so the same as persisting
				return true;
			}

			SharedPreferences.Editor editor = getSharedPreferences().edit();
			editor.putString(getKey(), value);
			editor.commit();

			notifyChanged();

			return true;
		}
		return false;
	}

	public void performOnClick() {
		onClick();
	}

	protected String getPersistString() {
		return getSharedPreferences().getString(getKey(), "");
	}

	private static class OrderedListViewHolder extends RecyclerView.ViewHolder {
		public final TextView titleView;
		public final TextView subtitleVew;

		public OrderedListViewHolder(View itemView) {
			super(itemView);
			titleView = (TextView) itemView.findViewById(R.id.orderedListTitle);
			subtitleVew = (TextView) itemView.findViewById(R.id.orderedListSubTitle);
		}
	}

	private class Adapter extends RecyclerView.Adapter<OrderedListViewHolder> {
		private final LayoutInflater mLayoutInflater;

		Adapter() {
			mLayoutInflater = LayoutInflater.from(getContext());
		}

		@Override
		public OrderedListViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
			return new OrderedListViewHolder(mLayoutInflater.inflate(R.layout.ordered_list_item, viewGroup, false));
		}

		@Override
		public void onBindViewHolder(OrderedListViewHolder viewHolder, int position) {
			QuickTextKey value = mQuickKeysAddOnsToStore.get(position);
			viewHolder.titleView.setText(value.getName());
			viewHolder.subtitleVew.setText(value.getDescription());
		}

		@Override
		public int getItemCount() {
			return mQuickKeysAddOnsToStore.size();
		}
	}
}
