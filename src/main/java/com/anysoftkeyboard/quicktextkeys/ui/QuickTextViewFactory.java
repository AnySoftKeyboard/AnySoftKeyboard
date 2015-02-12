package com.anysoftkeyboard.quicktextkeys.ui;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.anysoftkeyboard.keyboards.views.OnKeyboardActionListener;
import com.anysoftkeyboard.quicktextkeys.QuickTextKey;
import com.anysoftkeyboard.quicktextkeys.QuickTextKeyFactory;
import com.menny.android.anysoftkeyboard.R;

import java.util.ArrayList;

public class QuickTextViewFactory {
	public static View createQuickTextView(Context context, ViewGroup root, final OnKeyboardActionListener keyboardActionListener) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View rootView = inflater.inflate(R.layout.quick_text_popup_root_view, root, false);
		rootView.findViewById(R.id.quick_keys_popup_close).setOnClickListener(new CloseKeyboardViewClickListener(keyboardActionListener));
		ArrayList<QuickTextKey> list = QuickTextKeyFactory.getAllEnabledQuickKeys(context);
		ViewPager pager = (ViewPager) rootView.findViewById(R.id.quick_text_keyboards_pager);
		QuickKeysPagerAdapter adapter = new QuickKeysPagerAdapter(context, list, keyboardActionListener);
		pager.setAdapter(adapter);

		return rootView;
	}
}
