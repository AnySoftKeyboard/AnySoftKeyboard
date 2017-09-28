package com.anysoftkeyboard.ui.settings.setup;

import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class WizardPagesAdapter extends FragmentPagerAdapter {

    private final List<WizardPageBaseFragment> mFragments;

    WizardPagesAdapter(FragmentManager fragmentManager, boolean withLanguageDownload) {
        super(fragmentManager);
        ArrayList<WizardPageBaseFragment> fragments = new ArrayList<>(6);
        fragments.add(new WizardPageWelcomeFragment());
        fragments.add(new WizardPageEnableKeyboardFragment());
        fragments.add(new WizardPageSwitchToKeyboardFragment());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            fragments.add(new WizardPermissionsFragment());
        }
        if (withLanguageDownload) {
            fragments.add(new WizardLanguagePackFragment());
        }

        fragments.add(new WizardPageDoneAndMoreSettingsFragment());

        mFragments = Collections.unmodifiableList(fragments);
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    public int getItemPosition(Object object) {
        //so "notifyDataSetChanged()" will cause recreation
        return POSITION_NONE;
    }
}
