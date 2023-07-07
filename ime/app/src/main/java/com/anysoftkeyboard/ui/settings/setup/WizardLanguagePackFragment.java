package com.anysoftkeyboard.ui.settings.setup;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.anysoftkeyboard.addons.ui.AddOnStoreSearchView;
import com.anysoftkeyboard.prefs.DirectBootAwareSharedPreferences;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

public class WizardLanguagePackFragment extends WizardPageBaseFragment {

  private static final String SKIPPED_PREF_KEY = "setup_wizard_SKIPPED_PREF_KEY";
  private boolean mSkipped;

  @Override
  protected boolean isStepCompleted(@NonNull Context context) {
    // note: we can not use mSharedPrefs, since this method might be
    // called before onAttached is called.
    return (mSharedPrefs == null ? DirectBootAwareSharedPreferences.create(context) : mSharedPrefs)
            .getBoolean(SKIPPED_PREF_KEY, false)
        || SetupSupport.hasLanguagePackForCurrentLocale(
            AnyApplication.getKeyboardFactory(context).getAllAddOns());
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mSkipped = mSharedPrefs.getBoolean(SKIPPED_PREF_KEY, false);
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    final View.OnClickListener openPlayStoreAction =
        v -> AddOnStoreSearchView.startMarketActivity(getContext(), "language");
    view.findViewById(R.id.go_to_download_packs_action).setOnClickListener(openPlayStoreAction);
    mStateIcon.setOnClickListener(openPlayStoreAction);
    view.findViewById(R.id.skip_download_packs_action)
        .setOnClickListener(
            view1 -> {
              mSkipped = true;
              mSharedPrefs.edit().putBoolean(SKIPPED_PREF_KEY, true).apply();
              refreshWizardPager();
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
