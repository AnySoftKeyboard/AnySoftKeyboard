package com.anysoftkeyboard.ui.settings.setup;

import android.support.v4.app.Fragment;
import android.view.View;

import com.menny.android.anysoftkeyboard.R;

public abstract class WizardPageBaseFragment extends Fragment {

    /**
     * calculate whether the step has completed. This should check OS configuration.
     * @return true if step setup is valid in OS
     */
    protected abstract boolean isStepCompleted();

    /**
     * calculate whether the step's pre-configurations are done.
     * @return
     */
    protected abstract boolean isStepPreConditionDone();

    @Override
    public void onStart() {
        super.onStart();
        //enabling or disabling the views.
        refreshFragmentUi();
    }

    public void refreshFragmentUi() {
        final View preStepNotCompeleted = getView().findViewById(R.id.previous_step_not_complete);
        final View thisStepCompeleted = getView().findViewById(R.id.this_step_complete);
        final View thisStepSetup = getView().findViewById(R.id.this_step_needs_setup);

        preStepNotCompeleted.setVisibility(View.GONE);
        thisStepCompeleted.setVisibility(View.GONE);
        thisStepSetup.setVisibility(View.GONE);
        if (!isStepPreConditionDone()) {
            preStepNotCompeleted.setVisibility(View.VISIBLE);
        } else if (isStepCompleted()) {
            thisStepCompeleted.setVisibility(View.VISIBLE);
        } else {
            thisStepSetup.setVisibility(View.VISIBLE);
        }
    }
}
