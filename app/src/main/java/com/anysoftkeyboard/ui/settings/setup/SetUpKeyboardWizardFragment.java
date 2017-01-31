package com.anysoftkeyboard.ui.settings.setup;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.menny.android.anysoftkeyboard.R;

import java.lang.ref.WeakReference;

/**
 * This fragment will guide the user through the process of enabling, switch to and configuring AnySoftKeyboard.
 * This will be done with three pages, each for a different task:
 * 1) enable
 * 2) switch to
 * 3) additional settings (and saying 'Thank You' for switching to).
 * -) under Marshmallow, we'll also show Permissions
 */
public class SetUpKeyboardWizardFragment extends Fragment {

    private static class WizardHandler extends Handler {

        private final WeakReference<SetUpKeyboardWizardFragment> mWeakFragment;

        WizardHandler(@NonNull SetUpKeyboardWizardFragment setUpKeyboardWizardFragment) {
            mWeakFragment = new WeakReference<>(setUpKeyboardWizardFragment);
        }

        @Override
        public void handleMessage(Message msg) {
            SetUpKeyboardWizardFragment fragment = mWeakFragment.get();
            if (fragment == null) return;

            switch (msg.what) {
                case KEY_MESSAGE_SCROLL_TO_PAGE:
                    int pageToScrollTo = msg.arg1;
                    fragment.mWizardPager.setCurrentItem(pageToScrollTo, true);
                    break;
                case KEY_MESSAGE_UPDATE_FRAGMENTS:
                    if (fragment.isResumed() && fragment.getActivity() != null) {
                        fragment.refreshFragmentsUi();
                    } else {
                        fragment.mReloadPager = true;
                    }
                    break;
            }
        }
    }

    private static final int KEY_MESSAGE_SCROLL_TO_PAGE = 444;
    private static final int KEY_MESSAGE_UPDATE_FRAGMENTS = 446;

    private final Handler mUiHandler = new WizardHandler(this);

    private ViewPager mWizardPager;
    private Context mAppContext;

    private final ContentObserver mSecureSettingsChanged = new ContentObserver(null) {
        @Override
        public boolean deliverSelfNotifications() {
            return false;
        }

        @Override
        public void onChange(boolean selfChange) {
            mUiHandler.removeMessages(KEY_MESSAGE_UPDATE_FRAGMENTS);
            mUiHandler.sendMessageDelayed(mUiHandler.obtainMessage(KEY_MESSAGE_UPDATE_FRAGMENTS), 50);
        }
    };

    private boolean mReloadPager = false;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FragmentActivity activity = getActivity();
        mAppContext = activity.getApplicationContext();
        mAppContext.getContentResolver().registerContentObserver(Settings.Secure.CONTENT_URI, true, mSecureSettingsChanged);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.keyboard_setup_wizard_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        WizardPagesAdapter wizardPagesAdapter = new WizardPagesAdapter(getChildFragmentManager());
        mWizardPager = (ViewPager) view.findViewById(R.id.wizard_pages_pager);
        mWizardPager.setAdapter(wizardPagesAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        //checking to see which page should be shown on start
        if (mReloadPager) {
            refreshFragmentsUi();
        }

        mReloadPager = false;
    }

    public void refreshFragmentsUi() {
        mWizardPager.getAdapter().notifyDataSetChanged();
        scrollToPageRequiresSetup();
    }

    private void scrollToPageRequiresSetup() {
        if (mWizardPager.getAdapter() == null) return;

        FragmentPagerAdapter adapter = (FragmentPagerAdapter) mWizardPager.getAdapter();

        int fragmentIndex = 0;
        for (; fragmentIndex < adapter.getCount(); fragmentIndex++) {
            WizardPageBaseFragment wizardPageBaseFragment = (WizardPageBaseFragment) adapter.getItem(fragmentIndex);
            if (!wizardPageBaseFragment.isStepCompleted(getActivity())) break;
        }

        mUiHandler.removeMessages(KEY_MESSAGE_SCROLL_TO_PAGE);
        mUiHandler.sendMessageDelayed(
                mUiHandler.obtainMessage(KEY_MESSAGE_SCROLL_TO_PAGE, fragmentIndex, 0),
                getResources().getInteger(android.R.integer.config_longAnimTime));
    }

    @Override
    public void onStop() {
        super.onStop();
        mUiHandler.removeMessages(KEY_MESSAGE_SCROLL_TO_PAGE);//don't scroll if the UI is not visible
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAppContext != null)//in case it was destroyed before onActivityCreated was called.
            mAppContext.getContentResolver().unregisterContentObserver(mSecureSettingsChanged);
        mAppContext = null;
    }
}