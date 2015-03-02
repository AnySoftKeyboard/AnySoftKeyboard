package com.anysoftkeyboard.quicktextkeys.ui;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;

import com.anysoftkeyboard.keyboards.views.OnKeyboardActionListener;
import com.anysoftkeyboard.quicktextkeys.QuickTextKey;
import com.anysoftkeyboard.quicktextkeys.QuickTextKeyFactory;
import com.menny.android.anysoftkeyboard.R;

import java.util.List;

public class QuickTextViewFactory {

	public static View createQuickTextView(Context context, final OnKeyboardActionListener keyboardActionListener) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View rootView = inflater.inflate(R.layout.quick_text_popup_root_view, null, false);
		FrameKeyboardViewClickListener frameKeyboardViewClickListener = new FrameKeyboardViewClickListener(keyboardActionListener);
		frameKeyboardViewClickListener.registerOnViews(rootView);
		List<QuickTextKey> list = QuickTextKeyFactory.getOrderedEnabledQuickKeys(context);

		ViewPager pager = (ViewPager) rootView.findViewById(R.id.quick_text_keyboards_pager);
		final int decorationWidthSize = context.getResources().getDimensionPixelSize(R.dimen.quick_key_size);
		PagerAdapter adapter = new QuickKeysKeyboardPagerAdapter(context, list, keyboardActionListener, decorationWidthSize);
		pager.setAdapter(adapter);

		return rootView;
	}
}
