package com.anysoftkeyboard.quicktextkeys.ui;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.anysoftkeyboard.keyboards.views.OnKeyboardActionListener;
import com.anysoftkeyboard.quicktextkeys.QuickTextKey;
import com.menny.android.anysoftkeyboard.R;

import java.util.List;

public class QuickKeysPagerAdapter extends PagerAdapter {

	@NonNull
	private final Context mContext;
	@NonNull
	private final LayoutInflater mLayoutInflater;
	@NonNull
	private final QuickTextKey[] mQuickTextKeys;

	public QuickKeysPagerAdapter(@NonNull Context context, @NonNull List<QuickTextKey> quickTextKeys, OnKeyboardActionListener keyboardActionListener) {
		mContext = context;
		mQuickTextKeys = quickTextKeys.toArray(new QuickTextKey[quickTextKeys.size()]);
		mLayoutInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return mQuickTextKeys.length;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		TextView root = (TextView) mLayoutInflater.inflate(R.layout.quick_text_popup_keyboard_view, container, false);
		QuickTextKey quickTextKey = mQuickTextKeys[position];
		root.setTag(quickTextKey);

		root.setText(quickTextKey.getName());
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			root.setCompoundDrawablesRelativeWithIntrinsicBounds(quickTextKey.getKeyIconResId(), 0, 0, 0);
		} else {
			root.setCompoundDrawablesWithIntrinsicBounds(quickTextKey.getKeyIconResId(), 0, 0, 0);
		}

		return root;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		View view = (View)object;
		view.setTag(null);
		container.removeView(view);
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return mQuickTextKeys[position].getName();
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view.getTag() == object;
	}
}
