package com.anysoftkeyboard.quicktextkeys.ui;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.anysoftkeyboard.keyboards.AnyPopupKeyboard;
import com.anysoftkeyboard.keyboards.KeyboardDimens;
import com.anysoftkeyboard.keyboards.views.OnKeyboardActionListener;
import com.anysoftkeyboard.quicktextkeys.QuickTextKey;
import com.anysoftkeyboard.quicktextkeys.QuickTextKeyFactory;
import com.menny.android.anysoftkeyboard.R;

import java.util.ArrayList;

public class QuickTextViewFactory {


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

	public static View createQuickTextView(Context context, ViewGroup root, final OnKeyboardActionListener keyboardActionListener) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View rootView = inflater.inflate(R.layout.quick_text_popup_root_view, root, false);
		FrameKeyboardViewClickListener frameKeyboardViewClickListener = new FrameKeyboardViewClickListener(keyboardActionListener);
		frameKeyboardViewClickListener.registerOnViews(rootView);
		ArrayList<QuickTextKey> list = QuickTextKeyFactory.getOrderedEnabledQuickKeys(context);
		AnyPopupKeyboard[] keyboards = new AnyPopupKeyboard[list.size()];
		for (int keyboardIndex=0; keyboardIndex<list.size(); keyboardIndex++) {
			QuickTextKey key = list.get(keyboardIndex);
			keyboards[keyboardIndex] = new AnyPopupKeyboard(context, key.getPackageContext(), key.getPopupKeyboardResId(), msEmptyDimens, key.getName());
		}


		ViewPager pager = (ViewPager) rootView.findViewById(R.id.quick_text_keyboards_pager);
		QuickKeysPagerAdapter adapter = new QuickKeysPagerAdapter(context, keyboards, keyboardActionListener);
		pager.setAdapter(adapter);

		return rootView;
	}
}
