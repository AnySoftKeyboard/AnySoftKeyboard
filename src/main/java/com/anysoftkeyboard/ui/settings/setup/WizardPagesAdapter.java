package com.anysoftkeyboard.ui.settings.setup;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

class WizardPagesAdapter extends FragmentPagerAdapter {

    public WizardPagesAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new WizardPageEnableKeyboardFragment();
            case 1:
                return new WizardPageSwitchToKeyboardFragment();
            case 2:
                return new WizardPageDoneAndMoreSettingsFragment();
            default:
                throw new IllegalArgumentException("Position must be between 0 and 2. There are three pages in this wizard!");
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    public int getItemPosition(Object object) {
        //so "notifyDataSetChanged()" will cause recreation
        return POSITION_NONE;
    }
}
