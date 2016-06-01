package com.anysoftkeyboard.ui.settings.setup;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.View;

import com.menny.android.anysoftkeyboard.R;

public abstract class WizardPageBaseFragment extends Fragment {

    /**
     * calculate whether the step has completed. This should check OS configuration.
     * @return true if step setup is valid in OS
     */
    protected abstract boolean isStepCompleted(@NonNull Context context);

    /**
     * calculate whether the step's pre-configurations are done.
     */
    protected abstract boolean isStepPreConditionDone(@NonNull Context context);

    @Override
    public void onStart() {
        super.onStart();
        //enabling or disabling the views.
        refreshFragmentUi();
    }

    protected void refreshWizardPager() {
        //re-triggering UI update
        Fragment owningFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.main_ui_content);
        if (owningFragment == null) return;
        SetUpKeyboardWizardFragment wizardFragment = (SetUpKeyboardWizardFragment) owningFragment;
        wizardFragment.refreshFragmentsUi();
    }

    public void refreshFragmentUi() {
        if ((!isResumed()) || getActivity() == null) {
            //if the fragment is not shown, we will call refresh in onStart
            return;
        }
        final View pareStepNotCompleted = getView().findViewById(R.id.previous_step_not_complete);
        final View thisStepCompleted = getView().findViewById(R.id.this_step_complete);
        final View thisStepSetup = getView().findViewById(R.id.this_step_needs_setup);

        pareStepNotCompleted.setVisibility(View.GONE);
        thisStepCompleted.setVisibility(View.GONE);
        thisStepSetup.setVisibility(View.GONE);
        if (!isStepPreConditionDone(getActivity())) {
            pareStepNotCompleted.setVisibility(View.VISIBLE);
        } else if (isStepCompleted(getActivity())) {
            thisStepCompleted.setVisibility(View.VISIBLE);
        } else {
            thisStepSetup.setVisibility(View.VISIBLE);
        }
    }
}
