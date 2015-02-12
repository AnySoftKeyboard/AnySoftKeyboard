package com.anysoftkeyboard.quicktextkeys.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.anysoftkeyboard.keyboards.AnyPopupKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.views.OnKeyboardActionListener;
import com.menny.android.anysoftkeyboard.R;

import java.util.List;

public class QuickKeysPagerAdapter extends PagerAdapter {

	@NonNull
	private final Context mContext;
	@NonNull
	private final OnKeyboardActionListener mKeyboardActionListener;
	@NonNull
	private final LayoutInflater mLayoutInflater;
	@NonNull
	private final AnyPopupKeyboard[] mPopupKeyboards;
	private final int mKeySize;
	private final int mKeysPerRow;

	private final View.OnClickListener mKeyViewClickedHandler = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Keyboard.Key key = (Keyboard.Key) v.getTag();
			mKeyboardActionListener.onText(key.text);
		}
	};

	public QuickKeysPagerAdapter(@NonNull Context context, @NonNull AnyPopupKeyboard[] keyboards, OnKeyboardActionListener keyboardActionListener) {
		mContext = context;
		mKeyboardActionListener = keyboardActionListener;
		mPopupKeyboards = keyboards;
		mLayoutInflater = LayoutInflater.from(context);
		mKeySize = context.getResources().getDimensionPixelSize(R.dimen.quick_key_size);
		mKeysPerRow = context.getResources().getInteger(R.integer.quick_keys_per_row);
	}

	@Override
	public int getCount() {
		return mPopupKeyboards.length;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		View root = mLayoutInflater.inflate(R.layout.quick_text_popup_keyboard_view, container, false);
		container.addView(root);

		final ViewGroup rowsContainer = (ViewGroup) root.findViewById(R.id.keys_container);
		AnyPopupKeyboard keyboard = mPopupKeyboards[position];
		root.setTag(keyboard);

		final List<Keyboard.Key> keys = keyboard.getKeys();

		LinearLayout row = createRowView(rowsContainer, mKeysPerRow);
		for (int keyIndex = 0; keyIndex < keys.size(); keyIndex++) {
			if (row.getChildCount() == mKeysPerRow) row = createRowView(rowsContainer, mKeysPerRow);

			Keyboard.Key key = keys.get(keyIndex);
			TextView keyView = new TextView(mContext);
			keyView.setTextAppearance(mContext, R.style.Ask_Text_Large);
			keyView.setTag(key);
			keyView.setText(key.label);
			keyView.setLayoutParams(new LinearLayout.LayoutParams(0, mKeySize, 1.0f));
			keyView.setOnClickListener(mKeyViewClickedHandler);
			row.addView(keyView);
		}
		return root;
	}

	private LinearLayout createRowView(ViewGroup container, int keysPerRow) {
		LinearLayout row = new LinearLayout(mContext);
		row.setOrientation(LinearLayout.HORIZONTAL);
		row.setWeightSum(keysPerRow);
		container.addView(row);
		return row;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		View view = (View) object;
		view.setTag(null);
		container.removeView(view);
	}

	@Override
	public CharSequence getPageTitle(int position) {
		AnyPopupKeyboard keyboard = mPopupKeyboards[position];
		Keyboard.Key firstKey = keyboard.getKeys().get(0);

		return mContext.getResources().getString(R.string.quick_text_tab_title_template, firstKey.label, keyboard.getKeyboardName());
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == object;
	}
}
