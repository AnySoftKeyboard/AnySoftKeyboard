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
import com.anysoftkeyboard.keyboards.KeyboardDimens;
import com.anysoftkeyboard.keyboards.views.OnKeyboardActionListener;
import com.anysoftkeyboard.quicktextkeys.QuickTextKey;
import com.menny.android.anysoftkeyboard.R;

import java.util.List;

public class QuickKeysAsTextViewsPagerAdapter extends PagerAdapter {

	private static final KeyboardDimens msEmptyDimens = new KeyboardDimens() {
		@Override
		public int getKeyboardMaxWidth() {
			return 1;
		}

		@Override
		public int getKeyMaxWidth() {
			return 1;
		}

		@Override
		public float getKeyHorizontalGap() {
			return 0;
		}

		@Override
		public float getRowVerticalGap() {
			return 0;
		}

		@Override
		public int getNormalKeyHeight() {
			return 1;
		}

		@Override
		public int getSmallKeyHeight() {
			return 1;
		}

		@Override
		public int getLargeKeyHeight() {
			return 1;
		}
	};

	@NonNull
	private final Context mContext;
	@NonNull
	private final OnKeyboardActionListener mKeyboardActionListener;
	@NonNull
	private final LayoutInflater mLayoutInflater;
	@NonNull
	private final AnyPopupKeyboard[] mPopupKeyboards;
	@NonNull
	private final QuickTextKey[] mAddOns;
	private final int mKeySize;
	private final int mKeysPerRow;

	private final View.OnClickListener mKeyViewClickedHandler = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Keyboard.Key key = (Keyboard.Key) v.getTag();
			mKeyboardActionListener.onText(key.text);
		}
	};

	public QuickKeysAsTextViewsPagerAdapter(@NonNull Context context, @NonNull List<QuickTextKey> keyAddOns, @NonNull OnKeyboardActionListener keyboardActionListener) {
		mContext = context;
		mKeyboardActionListener = keyboardActionListener;
		mAddOns = keyAddOns.toArray(new QuickTextKey[keyAddOns.size()]);
		mPopupKeyboards = new AnyPopupKeyboard[mAddOns.length];
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
		QuickTextKey addOn = mAddOns[position];
		AnyPopupKeyboard keyboard = mPopupKeyboards[position];
		if (keyboard == null) {
			mPopupKeyboards[position] = new AnyPopupKeyboard(mContext, addOn.getPackageContext(), addOn.getPopupKeyboardResId(), msEmptyDimens, addOn.getName());
			keyboard = mPopupKeyboards[position];
		}
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
		QuickTextKey key = mAddOns[position];
		return mContext.getResources().getString(R.string.quick_text_tab_title_template, key.getKeyOutputText(), key.getName());
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == object;
	}
}
