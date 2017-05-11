package com.anysoftkeyboard.ui.settings.setup;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.anysoftkeyboard.ui.settings.widget.AddOnStoreSearchView;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

public class WizardLanguagePackFragment extends WizardPageBaseFragment {

    @Override
    protected boolean isStepCompleted(@NonNull Context context) {
        return SetupSupport.hasLanguagePackForCurrentLocale(AnyApplication.getKeyboardFactory(context).getAllAddOns());
    }

    @Override
    protected boolean isStepPreConditionDone(@NonNull Context context) {
        return SetupSupport.isThisKeyboardSetAsDefaultIME(context);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final View.OnClickListener openPlayStoreAction = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddOnStoreSearchView.startMarketActivity(getContext(), "language");
            }
        };
        view.findViewById(R.id.go_to_download_packs_action).setOnClickListener(openPlayStoreAction);
        mStateIcon.setOnClickListener(openPlayStoreAction);
    }

    @Override
    public void refreshFragmentUi() {
        super.refreshFragmentUi();
        if (getActivity() != null) {
            final boolean isEnabled = isStepCompleted(getActivity());
            mStateIcon.setImageResource(isEnabled ?
                    R.drawable.ic_wizard_download_pack_ready
                    : R.drawable.ic_wizard_download_pack_missing);
            mStateIcon.setClickable(!isEnabled);
        }
    }

    @Override
    protected int getPageLayoutId() {
        return R.layout.keyboard_setup_wizard_page_download_language_pack;
    }
}
