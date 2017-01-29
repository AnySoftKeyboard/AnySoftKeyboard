package com.anysoftkeyboard.ui.settings.setup;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.ImageView;

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

    protected ImageView mStateIcon;

    @Override
    public void onStart() {
        super.onStart();
        //enabling or disabling the views.
        refreshFragmentUi();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mStateIcon = (ImageView) view.findViewById(R.id.step_state_icon);
    }

    protected void refreshWizardPager() {
        //re-triggering UI update
        Fragment owningFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.main_ui_content);
        if (owningFragment == null) return;
        SetUpKeyboardWizardFragment wizardFragment = (SetUpKeyboardWizardFragment) owningFragment;
        wizardFragment.refreshFragmentsUi();
    }

    @CallSuper
    public void refreshFragmentUi() {
        if (getActivity() == null) {
            //if the fragment is not shown, we will call refresh in onStart
            return;
        }
        final View previousStepNotCompleted = getView().findViewById(R.id.previous_step_not_complete);
        final View thisStepCompleted = getView().findViewById(R.id.this_step_complete);
        final View thisStepNeedsSetup = getView().findViewById(R.id.this_step_needs_setup);

        previousStepNotCompleted.setVisibility(View.GONE);
        thisStepCompleted.setVisibility(View.GONE);
        thisStepNeedsSetup.setVisibility(View.GONE);

        if (!isStepPreConditionDone(getActivity())) {
            previousStepNotCompleted.setVisibility(View.VISIBLE);
        } else if (isStepCompleted(getActivity())) {
            thisStepCompleted.setVisibility(View.VISIBLE);
        } else {
            thisStepNeedsSetup.setVisibility(View.VISIBLE);
        }
    }
}
