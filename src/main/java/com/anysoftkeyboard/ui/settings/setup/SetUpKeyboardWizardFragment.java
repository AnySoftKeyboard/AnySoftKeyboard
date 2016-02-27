package com.anysoftkeyboard.ui.settings.setup;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

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

        public WizardHandler(SetUpKeyboardWizardFragment setUpKeyboardWizardFragment) {
            mWeakFragment = new WeakReference<>(setUpKeyboardWizardFragment);
        }

        @Override
        public void handleMessage(Message msg) {
            SetUpKeyboardWizardFragment fragment = mWeakFragment.get();
            if (fragment == null) return;

            switch (msg.what) {
                case KEY_MESSAGE_SCROLL_TO_PAGE:
                    int pageToScrollTo = msg.arg1;
                    if (fragment.mWizardPager != null/*meaning, this is a tablet - showing all fragments*/) {
                        fragment.mWizardPager.setCurrentItem(pageToScrollTo, true);
                        fragment.setFullIndicatorTo(pageToScrollTo, 0.0f);
                    }
                    break;
                case KEY_MESSAGE_UPDATE_INDICATOR:
                    int position = msg.arg1;
                    float offset = (Float) msg.obj;
                    fragment.setFullIndicatorTo(position, offset);
                    break;
                case KEY_MESSAGE_UPDATE_FRAGMENTS:
                    if (fragment.isResumed()) {
                        fragment.refreshFragmentsUi();
                    } else {
                        fragment.mReloadPager = true;
                    }
                    break;
            }
        }
    }

    private static final int KEY_MESSAGE_SCROLL_TO_PAGE = 444;
    private static final int KEY_MESSAGE_UPDATE_INDICATOR = 445;
    private static final int KEY_MESSAGE_UPDATE_FRAGMENTS = 446;

    private final Handler mUiHandler = new WizardHandler(this);

    //this is null on tablet!
    @Nullable
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
    private ViewPager.OnPageChangeListener onPageChangedListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            postSetFullIndicatorPosition(position, positionOffset);
        }

        private void postSetFullIndicatorPosition(int position, float positionOffset) {
            mUiHandler.removeMessages(KEY_MESSAGE_UPDATE_INDICATOR);
            mUiHandler.sendMessage(mUiHandler.obtainMessage(KEY_MESSAGE_UPDATE_INDICATOR, position, 0, Float.valueOf(positionOffset)));
        }

        @Override
        public void onPageSelected(int position) {
            postSetFullIndicatorPosition(position, 0.0f);
        }

        @Override
        public void onPageScrollStateChanged(int i) {
        }
    };

    private boolean mReloadPager = false;
    private View mFullIndicator;

    private void setFullIndicatorTo(int position, float offset) {
        if (mFullIndicator == null) return;
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mFullIndicator.getLayoutParams();
        lp.setMargins((int) ((position + offset) * mFullIndicator.getWidth()), 0, 0, 0);
        mFullIndicator.setLayoutParams(lp);
    }

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
        mFullIndicator = view.findViewById(R.id.selected_page_indicator);
        mWizardPager = (ViewPager) view.findViewById(R.id.wizard_pages_pager);
        if (mWizardPager == null/*meaning, this is a tablet - showing all fragments*/) {
            if (savedInstanceState == null) {
                //I to prevent leaks and duplicate ID errors, I must use the getChildFragmentManager
                //to add the inner fragments into the UI.
                //See: https://github.com/AnySoftKeyboard/AnySoftKeyboard/issues/285
                FragmentManager fragmentManager = getChildFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.wizard_step_one, new WizardPageEnableKeyboardFragment())
                        .replace(R.id.wizard_step_two, new WizardPageSwitchToKeyboardFragment())
                        .replace(R.id.wizard_step_three, new WizardPageDoneAndMoreSettingsFragment())
                        .commit();
            }
        } else {
            mWizardPager.setAdapter(new WizardPagesAdapter(getChildFragmentManager()));
            mWizardPager.addOnPageChangeListener(onPageChangedListener);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mWizardPager != null) {
            mWizardPager.removeOnPageChangeListener(onPageChangedListener);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        //checking to see which page should be shown on start
        if (mReloadPager) {
            refreshFragmentsUi();
        }

        mReloadPager = false;
    }

    public void refreshFragmentsUi() {
        if (mWizardPager == null/*meaning, this is a tablet - showing all fragments*/) {
            FragmentManager fragmentManager = getChildFragmentManager();
            refreshFragmentUi(fragmentManager, R.id.wizard_step_one);
            refreshFragmentUi(fragmentManager, R.id.wizard_step_two);
            refreshFragmentUi(fragmentManager, R.id.wizard_step_three);
        } else {
            mWizardPager.getAdapter().notifyDataSetChanged();
            scrollToPageRequiresSetup();
        }
    }

    private void refreshFragmentUi(FragmentManager fragmentManager, int layoutId) {
        Fragment step = fragmentManager.findFragmentById(layoutId);
        if (step != null && step instanceof WizardPageBaseFragment) {
            ((WizardPageBaseFragment) step).refreshFragmentUi();
        }
    }

    private void scrollToPageRequiresSetup() {
        if (mWizardPager == null/*meaning, this is a tablet - showing all fragments*/ ||
            mWizardPager.getAdapter() == null) return;

        FragmentPagerAdapter adapter = (FragmentPagerAdapter) mWizardPager.getAdapter();

        int fragmentIndex = 0;
        for (; fragmentIndex<adapter.getCount();fragmentIndex++) {
            WizardPageBaseFragment wizardPageBaseFragment = (WizardPageBaseFragment) adapter.getItem(fragmentIndex);
            if (!wizardPageBaseFragment.isStepCompleted()) break;
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