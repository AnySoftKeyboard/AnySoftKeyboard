package com.anysoftkeyboard.ui.settings.setup;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

class WizardPagesAdapter extends FragmentPagerAdapter {
    private final Context mContext;

    public WizardPagesAdapter(Context context, FragmentManager fm) {
        super(fm);
        this.mContext = context;
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
}
