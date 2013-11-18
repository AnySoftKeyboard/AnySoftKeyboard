package com.anysoftkeyboard.ui.settings.setup;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.menny.android.anysoftkeyboard.R;

public class WizardPageEnableKeyboardFragment extends WizardPageBaseFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.keyboard_setup_wizard_page_enable_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.go_to_language_settings_action).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity().getApplicationContext(), R.string.how_to_simple_howto_press_back_to_return_tip, Toast.LENGTH_LONG).show();
                startActivity(new Intent(android.provider.Settings.ACTION_INPUT_METHOD_SETTINGS));
            }
        });
    }

    @Override
    protected boolean isStepCompleted() {
        return SetupSupport.isThisKeyboardEnabled(getActivity());
    }

    @Override
    protected boolean isStepPreConditionDone() {
        return true;//the pre-condition is that the App is installed... I guess it does, right?
    }
}
