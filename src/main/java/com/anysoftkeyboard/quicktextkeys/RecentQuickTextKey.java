package com.anysoftkeyboard.quicktextkeys;

import android.content.Context;
import android.content.res.Resources;

import com.anysoftkeyboard.addons.AddOn;
import com.menny.android.anysoftkeyboard.R;

public class RecentQuickTextKey extends QuickTextKey {
	public RecentQuickTextKey(Context askContext) {
		super(askContext, askContext, "b0316c86-ffa2-49e9-85f7-6cb6e63e18f9", R.string.recent_quick_text_key_name,
				AddOn.INVALID_RES_ID, AddOn.INVALID_RES_ID, AddOn.INVALID_RES_ID, AddOn.INVALID_RES_ID,
				R.drawable.sym_keyboard_smiley, R.string.quick_text_smiley_key_recent_output, R.string.quick_text_smiley_key_recent_output,
				AddOn.INVALID_RES_ID, askContext.getResources().getString(R.string.recent_quick_text_key_name), 0);
	}

	@Override
	protected String[] getStringArrayFromNamesResId(int popupListNamesResId, Resources resources) {
		return new String[]{"Test" , "XX"};
	}

	@Override
	protected String[] getStringArrayFromValuesResId(int popupListValuesResId, Resources resources) {
		return new String[]{"TESTTESTTEST" , "X-x-X"};
	}
}
