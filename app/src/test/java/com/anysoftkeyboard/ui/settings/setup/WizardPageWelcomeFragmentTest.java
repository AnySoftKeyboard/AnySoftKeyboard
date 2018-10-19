package com.anysoftkeyboard.ui.settings.setup;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.app.Application;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.View;

import com.anysoftkeyboard.RobolectricFragmentTestCase;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.KeyboardDimens;
import com.anysoftkeyboard.keyboards.views.DemoAnyKeyboardView;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Test;
import org.robolectric.Robolectric;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowView;

import androidx.test.core.app.ApplicationProvider;

public class WizardPageWelcomeFragmentTest extends RobolectricFragmentTestCase<WizardPageWelcomeFragmentTest.TestableWizardPageWelcomeFragment> {

    @NonNull
    @Override
    protected TestableWizardPageWelcomeFragment createFragment() {
        return new TestableWizardPageWelcomeFragment();
    }

    @Test
    public void testClickStart() {
        TestableWizardPageWelcomeFragment fragment = startFragment();
        Assert.assertFalse(fragment.isStepCompleted(getApplicationContext()));

        final View startView = fragment.getView().findViewById(R.id.go_to_start_setup);
        Assert.assertNotNull(startView);
        Assert.assertTrue(startView.isClickable());
        final ShadowView shadowStartView = Shadows.shadowOf(startView);
        Assert.assertNotNull(shadowStartView.getOnClickListener());
        Assert.assertFalse(fragment.mRefreshPagerCalled);
        startView.performClick();
        Assert.assertTrue(fragment.mRefreshPagerCalled);

        Assert.assertTrue(fragment.isStepCompleted(getApplicationContext()));
    }

    @Test
    public void testClickPrivacyPolicy() {
        WizardPageWelcomeFragment fragment = startFragment();
        Assert.assertFalse(fragment.isStepCompleted(getApplicationContext()));

        fragment.getView().findViewById(R.id.setup_wizard_welcome_privacy_action).performClick();

        Intent wikiIntent = Shadows.shadowOf((Application) ApplicationProvider.getApplicationContext()).getNextStartedActivity();
        Assert.assertEquals(Intent.ACTION_VIEW, wikiIntent.getAction());
        Assert.assertEquals("https://anysoftkeyboard.github.io/privacy_policy.html", wikiIntent.getData().toString());
    }

    @Test
    public void testDemoRotate() {
        WizardPageWelcomeFragment fragment = startFragment();
        DemoAnyKeyboardView demoAnyKeyboardView = fragment.getView().findViewById(R.id.demo_keyboard_view);
        int timesDemoChanged = 0;
        final int runsToMake = 10;
        for (int tests = 0; tests < runsToMake; tests++) {
            final long startDemoDescription = describeDemoKeyboard(demoAnyKeyboardView);
            final long startTime = Robolectric.getForegroundThreadScheduler().getCurrentTime();

            Assert.assertTrue(Robolectric.getForegroundThreadScheduler().advanceToLastPostedRunnable());

            Assert.assertNotEquals(startTime, Robolectric.getForegroundThreadScheduler().getCurrentTime());

            if (startDemoDescription != describeDemoKeyboard(demoAnyKeyboardView)) {
                timesDemoChanged++;
            }
        }

        //making sure that the demo view changed more than half the times.
        Assert.assertTrue(timesDemoChanged > runsToMake/2);
    }

    private long describeDemoKeyboard(DemoAnyKeyboardView demoAnyKeyboardView) {
        long description = 0;
        for (Keyboard.Key key : demoAnyKeyboardView.getKeyboard().getKeys()) {
            description += key.getPrimaryCode();
        }

        KeyboardDimens themedKeyboardDimens = demoAnyKeyboardView.getThemedKeyboardDimens();
        description += themedKeyboardDimens.getKeyboardMaxWidth();
        description += themedKeyboardDimens.getLargeKeyHeight();
        description += themedKeyboardDimens.getNormalKeyHeight();
        description += themedKeyboardDimens.getSmallKeyHeight();
        description += (int) themedKeyboardDimens.getKeyHorizontalGap();
        description += (int) themedKeyboardDimens.getRowVerticalGap();

        return description;
    }

    public static class TestableWizardPageWelcomeFragment extends WizardPageWelcomeFragment {
        private boolean mRefreshPagerCalled;

        @Override
        protected void refreshWizardPager() {
            mRefreshPagerCalled = true;
        }
    }
}