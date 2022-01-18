/*
 * Copyright (c) 2021 Menny Even-Danan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.anysoftkeyboard.ui.settings.setup;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import app.cash.copper.rx2.RxContentResolver;
import com.anysoftkeyboard.android.PermissionRequestHelper;
import com.anysoftkeyboard.rx.RxSchedulers;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import java.lang.ref.WeakReference;
import net.evendanan.pixel.EdgeEffectHacker;

public class SetupWizardActivity extends AppCompatActivity {

    private static final int KEY_MESSAGE_SCROLL_TO_PAGE = 444;
    private static final int KEY_MESSAGE_UPDATE_FRAGMENTS = 446;
    private final Handler mUiHandler = new WizardHandler(this);
    private ViewPager mWizardPager;
    private boolean mReloadPager = false;
    @NonNull private Disposable mSecureSettingsChangedDisposable = Disposables.empty();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.initial_setup_main_ui);
        mSecureSettingsChangedDisposable =
                RxContentResolver.observeQuery(
                                getContentResolver(),
                                Settings.Secure.CONTENT_URI,
                                null,
                                null,
                                null,
                                null,
                                true)
                        .observeOn(RxSchedulers.mainThread())
                        .forEach(
                                cursor -> {
                                    mUiHandler.removeMessages(KEY_MESSAGE_UPDATE_FRAGMENTS);
                                    mUiHandler.sendMessageDelayed(
                                            mUiHandler.obtainMessage(KEY_MESSAGE_UPDATE_FRAGMENTS),
                                            50);
                                });

        FragmentPagerAdapter wizardPagesAdapter = createPagesAdapter();
        mWizardPager = findViewById(R.id.wizard_pages_pager);
        mWizardPager.setEnabled(false);
        mWizardPager.setAdapter(wizardPagesAdapter);
    }

    @NonNull
    @VisibleForTesting
    protected FragmentPagerAdapter createPagesAdapter() {
        return new WizardPagesAdapter(
                getSupportFragmentManager(),
                !SetupSupport.hasLanguagePackForCurrentLocale(
                        AnyApplication.getKeyboardFactory(getApplicationContext()).getAllAddOns()));
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // applying my very own Edge-Effect color
        EdgeEffectHacker.brandGlowEffect(this, ContextCompat.getColor(this, R.color.app_accent));
    }

    @Override
    public void onResume() {
        super.onResume();
        // checking to see which page should be shown on start
        if (mReloadPager) {
            refreshFragmentsUi();
        } else {
            scrollToPageRequiresSetup();
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
            WizardPageBaseFragment wizardPageBaseFragment =
                    (WizardPageBaseFragment) adapter.getItem(fragmentIndex);
            if (!wizardPageBaseFragment.isStepCompleted(this)) break;
        }

        mUiHandler.removeMessages(KEY_MESSAGE_SCROLL_TO_PAGE);
        mUiHandler.sendMessageDelayed(
                mUiHandler.obtainMessage(KEY_MESSAGE_SCROLL_TO_PAGE, fragmentIndex, 0),
                getResources().getInteger(android.R.integer.config_longAnimTime));
    }

    @Override
    public void onStop() {
        super.onStop();
        // don't scroll if the UI is not visible
        mUiHandler.removeMessages(KEY_MESSAGE_SCROLL_TO_PAGE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSecureSettingsChangedDisposable.dispose();
    }

    private static class WizardHandler extends Handler {

        private final WeakReference<SetupWizardActivity> mActivity;

        WizardHandler(@NonNull SetupWizardActivity activity) {
            super(Looper.getMainLooper());
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final SetupWizardActivity activity = mActivity.get();
            if (activity == null) return;

            switch (msg.what) {
                case KEY_MESSAGE_SCROLL_TO_PAGE:
                    int pageToScrollTo = msg.arg1;
                    activity.mWizardPager.setCurrentItem(pageToScrollTo, true);
                    break;
                case KEY_MESSAGE_UPDATE_FRAGMENTS:
                    activity.refreshFragmentsUi();
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionRequestHelper.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }
}
