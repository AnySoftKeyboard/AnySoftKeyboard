package com.anysoftkeyboard.quicktextkeys.ui;

import android.content.Intent;
import android.view.View;

import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.keyboards.views.OnKeyboardActionListener;
import com.anysoftkeyboard.ui.settings.AdditionalUiSettingsFragment;
import com.anysoftkeyboard.ui.settings.MainSettingsActivity;
import com.menny.android.anysoftkeyboard.R;

import net.evendanan.pushingpixels.FragmentChauffeurActivity;

public class FrameKeyboardViewClickListener implements View.OnClickListener {
	private final OnKeyboardActionListener mKeyboardActionListener;

	public FrameKeyboardViewClickListener(OnKeyboardActionListener keyboardActionListener) {
		mKeyboardActionListener = keyboardActionListener;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.quick_keys_popup_close:
				mKeyboardActionListener.onCancel();
				break;
			case R.id.quick_keys_popup_backspace:
				mKeyboardActionListener.onKey(KeyCodes.DELETE, null, 0, null, true);
				break;
			case R.id.quick_keys_popup_quick_keys_settings:
				//starting the MainSettings activity, with TipsFragment
				Intent i = new Intent(v.getContext(), MainSettingsActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				FragmentChauffeurActivity.addIntentArgsForAddingFragmentToUi(i, AdditionalUiSettingsFragment.class, AdditionalUiSettingsFragment.createArgsForConfiguringQuickTextKeys());
				v.getContext().startActivity(i);
				mKeyboardActionListener.onCancel();
				break;
		}
	}

	public void registerOnViews(View rootView) {
		rootView.findViewById(R.id.quick_keys_popup_close).setOnClickListener(this);
		rootView.findViewById(R.id.quick_keys_popup_backspace).setOnClickListener(this);
		rootView.findViewById(R.id.quick_keys_popup_quick_keys_settings).setOnClickListener(this);
	}
}
