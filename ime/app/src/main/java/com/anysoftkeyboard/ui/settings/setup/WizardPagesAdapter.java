package com.anysoftkeyboard.ui.settings.setup;

import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.util.Supplier;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class WizardPagesAdapter extends FragmentStateAdapter {

  private final List<Supplier<WizardPageBaseFragment>> mFragments;

  WizardPagesAdapter(FragmentActivity activity, boolean withLanguageDownload) {
    super(activity);
    ArrayList<Supplier<WizardPageBaseFragment>> fragments = new ArrayList<>(6);
    fragments.add(WizardPageWelcomeFragment::new);
    fragments.add(WizardPageEnableKeyboardFragment::new);
    fragments.add(WizardPageSwitchToKeyboardFragment::new);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      fragments.add(WizardPermissionsFragment::new);
    }
    if (withLanguageDownload) {
      fragments.add(WizardLanguagePackFragment::new);
    }

    fragments.add(WizardPageDoneAndMoreSettingsFragment::new);

    mFragments = Collections.unmodifiableList(fragments);
  }

  @NonNull
  @Override
  public Fragment createFragment(int position) {
    return mFragments.get(position).get();
  }

  @Override
  public int getItemCount() {
    return mFragments.size();
  }
}
