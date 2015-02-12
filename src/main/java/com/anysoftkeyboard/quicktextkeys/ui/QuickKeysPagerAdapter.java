package com.anysoftkeyboard.quicktextkeys.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
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

public class QuickKeysPagerAdapter extends PagerAdapter {

	private final OnKeyboardActionListener mKeyboardActionListener;
	@NonNull
	private final LayoutInflater mLayoutInflater;
	@NonNull
	private final QuickTextKey[] mQuickTextKeys;
	private final int mKeySize;
	private final int mKeysPerRow;

	private final KeyboardDimens mEmptyDimens = new KeyboardDimens() {
		@Override
		public int getKeyboardMaxWidth() {
			return mKeySize;
		}

		@Override
		public int getKeyMaxWidth() {
			return mKeySize;
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
			return mKeySize;
		}

		@Override
		public int getSmallKeyHeight() {
			return mKeySize;
		}

		@Override
		public int getLargeKeyHeight() {
			return mKeySize;
		}
	};

	private final View.OnClickListener mKeyViewClickedHandler = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Keyboard.Key key = (Keyboard.Key) v.getTag();
			mKeyboardActionListener.onText(key.text);
		}
	};

	public QuickKeysPagerAdapter(@NonNull Context context, @NonNull List<QuickTextKey> quickTextKeys, OnKeyboardActionListener keyboardActionListener) {
		mKeyboardActionListener = keyboardActionListener;
		mQuickTextKeys = quickTextKeys.toArray(new QuickTextKey[quickTextKeys.size()]);
		mLayoutInflater = LayoutInflater.from(context);
		mKeySize = context.getResources().getDimensionPixelSize(R.dimen.quick_key_size);
		mKeysPerRow = context.getResources().getInteger(R.integer.quick_keys_per_row);
	}

	@Override
	public int getCount() {
		return mQuickTextKeys.length;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		View root = mLayoutInflater.inflate(R.layout.quick_text_popup_keyboard_view, container, false);
		container.addView(root);

		final ViewGroup rowsContainer = (ViewGroup) root.findViewById(R.id.keys_container);
		QuickTextKey quickTextKey = mQuickTextKeys[position];
		root.setTag(quickTextKey);

		AnyPopupKeyboard popupKeyboard = new AnyPopupKeyboard(container.getContext(), quickTextKey.getPackageContext(),
											quickTextKey.getPopupKeyboardResId(), mEmptyDimens);

		final List<Keyboard.Key> keys = popupKeyboard.getKeys();

		LinearLayout row = createRowView(rowsContainer, mKeysPerRow);
		for (int keyIndex=0; keyIndex<keys.size(); keyIndex++) {
			if (row.getChildCount() == mKeysPerRow) row = createRowView(rowsContainer, mKeysPerRow);

			Keyboard.Key key = keys.get(keyIndex);
			TextView keyView = new TextView(container.getContext());
			keyView.setTextAppearance(container.getContext(), R.style.Ask_Text_Large);
			keyView.setTag(key);
			keyView.setText(key.label);
			keyView.setLayoutParams(new LinearLayout.LayoutParams(0, mKeySize, 1.0f));
			keyView.setOnClickListener(mKeyViewClickedHandler);
			row.addView(keyView);
		}
		return root;
	}

	private LinearLayout createRowView(ViewGroup container, int keysPerRow) {
		LinearLayout row = new LinearLayout(container.getContext());
		row.setOrientation(LinearLayout.HORIZONTAL);
		row.setWeightSum(keysPerRow);
		container.addView(row);
		return row;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		View view = (View)object;
		view.setTag(null);
		container.removeView(view);
	}

	@Override
	public CharSequence getPageTitle(int position) {
		QuickTextKey quickTextKey = mQuickTextKeys[position];
		SpannableStringBuilder builder = new SpannableStringBuilder();
		builder.append("  ").append(quickTextKey.getName());
		builder.setSpan(new ImageSpan(quickTextKey.getPackageContext(), quickTextKey.getKeyIconResId()),
				0, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		return builder;
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == object;
	}
}
