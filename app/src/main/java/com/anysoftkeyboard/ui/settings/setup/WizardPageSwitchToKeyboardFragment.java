package com.anysoftkeyboard.ui.settings.setup;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import com.menny.android.anysoftkeyboard.R;

public class WizardPageSwitchToKeyboardFragment extends WizardPageBaseFragment {

    @Override
    protected int getPageLayoutId() {
        return R.layout.keyboard_setup_wizard_page_switch_to_layout;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View.OnClickListener showSwitchImeDialog =
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        InputMethodManager mgr =
                                (InputMethodManager)
                                        getActivity()
                                                .getSystemService(Context.INPUT_METHOD_SERVICE);
                        mgr.showInputMethodPicker();
                    }
                };
        view.findViewById(R.id.go_to_switch_keyboard_action)
                .setOnClickListener(showSwitchImeDialog);
        mStateIcon.setOnClickListener(showSwitchImeDialog);
    }

    @Override
    public void refreshFragmentUi() {
        super.refreshFragmentUi();
        if (getActivity() != null) {
            final boolean isActive = isStepCompleted(getActivity());
            mStateIcon.setImageResource(
                    isActive ? R.drawable.ic_wizard_switch_on : R.drawable.ic_wizard_switch_off);
            mStateIcon.setClickable(!isActive);
        }
    }

    @Override
    protected boolean isStepCompleted(@NonNull Context context) {
        return SetupSupport.isThisKeyboardSetAsDefaultIME(context);
    }
}
