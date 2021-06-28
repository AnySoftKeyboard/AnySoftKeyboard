package com.anysoftkeyboard.ui.settings.setup;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.anysoftkeyboard.ui.settings.widget.AddOnStoreSearchView;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

public class WizardLanguagePackFragment extends WizardPageBaseFragment {

    private static final String SKIPPED_PREF_KEY = "setup_wizard_SKIPPED_PREF_KEY";
    private boolean mSkipped;

    @Override
    protected boolean isStepCompleted(@NonNull Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                        .getBoolean(SKIPPED_PREF_KEY, false)
                || SetupSupport.hasLanguagePackForCurrentLocale(
                        AnyApplication.getKeyboardFactory(context).getAllAddOns());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSkipped =
                PreferenceManager.getDefaultSharedPreferences(getContext())
                        .getBoolean(SKIPPED_PREF_KEY, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final View.OnClickListener openPlayStoreAction =
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AddOnStoreSearchView.startMarketActivity(getContext(), "language");
                    }
                };
        view.findViewById(R.id.go_to_download_packs_action).setOnClickListener(openPlayStoreAction);
        mStateIcon.setOnClickListener(openPlayStoreAction);
        view.findViewById(R.id.skip_download_packs_action)
                .setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                mSkipped = true;
                                final SharedPreferences.Editor editor =
                                        PreferenceManager.getDefaultSharedPreferences(getContext())
                                                .edit();
                                editor.putBoolean(SKIPPED_PREF_KEY, true);
                                editor.apply();
                                refreshWizardPager();
                            }
                        });
    }

    @Override
    public void refreshFragmentUi() {
        super.refreshFragmentUi();
        if (getActivity() != null) {
            final boolean isEnabled = isStepCompleted(getActivity());
            mStateIcon.setImageResource(
                    isEnabled && !mSkipped
                            ? R.drawable.ic_wizard_download_pack_ready
                            : R.drawable.ic_wizard_download_pack_missing);
            mStateIcon.setClickable(!isEnabled);
        }
    }

    @Override
    protected int getPageLayoutId() {
        return R.layout.keyboard_setup_wizard_page_download_language_pack;
    }
}
