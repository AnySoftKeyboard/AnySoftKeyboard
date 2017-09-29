package com.anysoftkeyboard.ui.settings.setup;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.View;

import com.anysoftkeyboard.RobolectricFragmentTestCase;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowView;

public class WizardPageWelcomeFragmentTest extends RobolectricFragmentTestCase<WizardPageWelcomeFragmentTest.TestableWizardPageWelcomeFragment> {

    @NonNull
    @Override
    protected TestableWizardPageWelcomeFragment createFragment() {
        return new TestableWizardPageWelcomeFragment();
    }

    @Test
    public void testClickStart() {
        TestableWizardPageWelcomeFragment fragment = startFragment();
        Assert.assertFalse(fragment.isStepCompleted(RuntimeEnvironment.application));

        final View startView = fragment.getView().findViewById(R.id.go_to_start_setup);
        Assert.assertNotNull(startView);
        Assert.assertTrue(startView.isClickable());
        final ShadowView shadowStartView = Shadows.shadowOf(startView);
        Assert.assertNotNull(shadowStartView.getOnClickListener());
        Assert.assertFalse(fragment.mRefreshPagerCalled);
        startView.performClick();
        Assert.assertTrue(fragment.mRefreshPagerCalled);

        Assert.assertTrue(fragment.isStepCompleted(RuntimeEnvironment.application));
    }

    @Test
    public void testClickPrivacyPolicy() {
        WizardPageWelcomeFragment fragment = startFragment();
        Assert.assertFalse(fragment.isStepCompleted(RuntimeEnvironment.application));

        fragment.getView().findViewById(R.id.setup_wizard_welcome_privacy_action).performClick();

        Intent wikiIntent = ShadowApplication.getInstance().getNextStartedActivity();
        Assert.assertEquals(Intent.ACTION_VIEW, wikiIntent.getAction());
        Assert.assertEquals("https://anysoftkeyboard.github.io/privacy_policy.html", wikiIntent.getData().toString());
    }

    public static class TestableWizardPageWelcomeFragment extends WizardPageWelcomeFragment {
        private boolean mRefreshPagerCalled;

        @Override
        protected void refreshWizardPager() {
            mRefreshPagerCalled = true;
        }
    }
}