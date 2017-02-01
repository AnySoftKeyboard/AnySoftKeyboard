package com.anysoftkeyboard.ui.settings.setup;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
        View.OnClickListener showSwitchImeDialog = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager mgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.showInputMethodPicker();
            }
        };
        view.findViewById(R.id.go_to_switch_keyboard_action).setOnClickListener(showSwitchImeDialog);
        mStateIcon.setOnClickListener(showSwitchImeDialog);
    }

    @Override
    public void refreshFragmentUi() {
        super.refreshFragmentUi();
        if (getActivity() != null) {
            final boolean isActive = isStepCompleted(getActivity());
            final boolean isEnabled = isStepPreConditionDone(getActivity());
            mStateIcon.setImageResource(isActive ?
                    R.drawable.ic_wizard_switch_on
                    : R.drawable.ic_wizard_switch_off);
            mStateIcon.setClickable(isEnabled && !isActive);
        }
    }

    @Override
    protected boolean isStepCompleted(@NonNull Context context) {
        return SetupSupport.isThisKeyboardSetAsDefaultIME(context);
    }

    @Override
    protected boolean isStepPreConditionDone(@NonNull Context context) {
        return SetupSupport.isThisKeyboardEnabled(context);
    }
}
