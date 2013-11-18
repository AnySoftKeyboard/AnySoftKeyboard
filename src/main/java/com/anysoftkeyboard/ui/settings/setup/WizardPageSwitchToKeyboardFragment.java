package com.anysoftkeyboard.ui.settings.setup;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.menny.android.anysoftkeyboard.R;

public class WizardPageSwitchToKeyboardFragment extends WizardPageBaseFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.keyboard_setup_wizard_page_switch_to_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.go_to_switch_keyboard_action).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager mgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.showInputMethodPicker();
            }
        });
    }

    @Override
    protected boolean isStepCompleted() {
        return SetupSupport.isThisKeyboardSetAsDefaultIME(getActivity());
    }

    @Override
    protected boolean isStepPreConditionDone() {
        return SetupSupport.isThisKeyboardEnabled(getActivity());
    }
}
