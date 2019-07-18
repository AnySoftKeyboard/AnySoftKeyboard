package com.anysoftkeyboard.ui.settings.setup;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.database.ContentObserver;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.RobolectricFragmentTestCase;
import com.menny.android.anysoftkeyboard.BuildConfig;
import com.menny.android.anysoftkeyboard.R;
import com.menny.android.anysoftkeyboard.SoftKeyboard;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowContentResolver;
import org.robolectric.util.Scheduler;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class SetUpKeyboardWizardFragmentTest
        extends RobolectricFragmentTestCase<SetUpKeyboardWizardFragment> {

    @NonNull
    @Override
    protected SetUpKeyboardWizardFragment createFragment() {
        return new SetUpKeyboardWizardFragment();
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Test
    @Config(sdk = Build.VERSION_CODES.M)
    public void testHasPermissionsPage() {
        SetUpKeyboardWizardFragment fragment = startFragment();
        // ensuring we registered for Secure settings observing
        final Collection<ContentObserver> contentObservers =
                Shadows.shadowOf(fragment.getActivity().getContentResolver())
                        .getContentObservers(Settings.Secure.CONTENT_URI);
        Assert.assertEquals(1, contentObservers.size());

        final ViewPager pager =
                (ViewPager) fragment.getView().findViewById(R.id.wizard_pages_pager);
        Assert.assertNotNull(pager);
        Assert.assertEquals(5, pager.getAdapter().getCount());
        Assert.assertTrue(
                ((FragmentPagerAdapter) pager.getAdapter()).getItem(3)
                        instanceof WizardPermissionsFragment);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Test
    @Config(
            sdk =
                    Build.VERSION_CODES
                            .LOLLIPOP /*I don't want to also verify the permissions page too*/)
    public void testHappyPath() {
        SetUpKeyboardWizardFragment fragment = startFragment();
        // ensuring we registered for Secure settings observing
        final ShadowContentResolver shadowContentResolver =
                Shadows.shadowOf(fragment.getActivity().getContentResolver());
        final Collection<ContentObserver> contentObservers =
                shadowContentResolver.getContentObservers(Settings.Secure.CONTENT_URI);
        Assert.assertEquals(1, contentObservers.size());

        final ViewPager pager =
                (ViewPager) fragment.getView().findViewById(R.id.wizard_pages_pager);
        Assert.assertNotNull(pager);
        Assert.assertEquals(4, pager.getAdapter().getCount());
        // starts at page one - welcome keyboard
        Assert.assertEquals(0, pager.getCurrentItem());
        Assert.assertTrue(
                ((FragmentPagerAdapter) pager.getAdapter()).getItem(0)
                        instanceof WizardPageWelcomeFragment);
        Robolectric.getForegroundThreadScheduler().setIdleState(Scheduler.IdleState.PAUSED);
        ((FragmentPagerAdapter) pager.getAdapter())
                .getItem(0)
                .getView()
                .findViewById(R.id.go_to_start_setup)
                .performClick();
        ensureAllScheduledJobsAreDone();
        Robolectric.getForegroundThreadScheduler().setIdleState(Scheduler.IdleState.UNPAUSED);

        // page two - enable ASK
        Assert.assertEquals(1, pager.getCurrentItem());
        // now, lets say that ASK was enabled.
        getFragmentController().pause().stop();
        ensureAllScheduledJobsAreDone();

        final String flatASKComponent =
                new ComponentName(BuildConfig.APPLICATION_ID, SoftKeyboard.class.getName())
                        .flattenToString();
        Settings.Secure.putString(
                fragment.getActivity().getContentResolver(),
                Settings.Secure.ENABLED_INPUT_METHODS,
                flatASKComponent);
        contentObservers.iterator().next().dispatchChange(false, Settings.Secure.CONTENT_URI);
        ensureAllScheduledJobsAreDone();
        Robolectric.getForegroundThreadScheduler().setIdleState(Scheduler.IdleState.PAUSED);
        // notifying about the change.
        getFragmentController().start().resume();
        ensureAllScheduledJobsAreDone();

        // now at page three - activate keyboard
        Assert.assertEquals(2, pager.getCurrentItem());

        Robolectric.getForegroundThreadScheduler().setIdleState(Scheduler.IdleState.UNPAUSED);

        getFragmentController().pause();
        Settings.Secure.putString(
                fragment.getActivity().getContentResolver(),
                Settings.Secure.DEFAULT_INPUT_METHOD,
                flatASKComponent);
        // notifying about the change.
        contentObservers.iterator().next().dispatchChange(false, Settings.Secure.CONTENT_URI);
        ensureAllScheduledJobsAreDone();
        Robolectric.getForegroundThreadScheduler().setIdleState(Scheduler.IdleState.PAUSED);

        getFragmentController().resume();
        ensureAllScheduledJobsAreDone();
        // now at page four - more settings.
        Assert.assertEquals(3, pager.getCurrentItem());

        // destroying the fragment should unregister from Secure content provider
        getFragmentController().stop().pause().destroy();
        Assert.assertEquals(
                0, shadowContentResolver.getContentObservers(Settings.Secure.CONTENT_URI).size());
    }
}
