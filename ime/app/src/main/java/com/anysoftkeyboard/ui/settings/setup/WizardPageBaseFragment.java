package com.anysoftkeyboard.ui.settings.setup;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import com.anysoftkeyboard.android.PermissionRequestHelper;
import com.anysoftkeyboard.prefs.DirectBootAwareSharedPreferences;
import com.menny.android.anysoftkeyboard.R;

public abstract class WizardPageBaseFragment extends Fragment {

    protected ImageView mStateIcon;
    protected SharedPreferences mSharedPrefs;

    /**
     * calculate whether the step has completed. This should check OS configuration.
     *
     * @return true if step setup is valid in OS
     */
    protected abstract boolean isStepCompleted(@NonNull Context context);

    @LayoutRes
    protected abstract int getPageLayoutId();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSharedPrefs = DirectBootAwareSharedPreferences.create(requireContext());
    }

    @Override
    public final View onCreateView(
            LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        NestedScrollView scrollView =
                (NestedScrollView)
                        inflater.inflate(
                                R.layout.keyboard_setup_wizard_page_base_layout, container, false);

        View actualPageView = inflater.inflate(getPageLayoutId(), scrollView, false);

        scrollView.addView(actualPageView);

        return scrollView;
    }

    protected void refreshFragmentUi() {}

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mStateIcon = view.findViewById(R.id.step_state_icon);
    }

    protected void refreshWizardPager() {
        refreshFragmentUi();
        // re-triggering UI update
        SetupWizardActivity owningActivity = (SetupWizardActivity) getActivity();
        if (owningActivity == null) return;
        owningActivity.refreshFragmentsUi();
    }

    @Override
    public void onStart() {
        super.onStart();
        refreshFragmentUi();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionRequestHelper.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }
}
