package com.anysoftkeyboard.ui.settings.setup;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.database.ContentObserver;
import android.os.Build;
import android.provider.Settings;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.viewpager.widget.ViewPager;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.rx.TestRxSchedulers;
import com.menny.android.anysoftkeyboard.BuildConfig;
import com.menny.android.anysoftkeyboard.R;
import com.menny.android.anysoftkeyboard.SoftKeyboard;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;
import org.robolectric.shadows.ShadowContentResolver;
import org.robolectric.util.Scheduler;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
@LooperMode(LooperMode.Mode.LEGACY)
public class SetUpKeyboardWizardTest {

    @Rule
    public ActivityScenarioRule<SetupWizardActivity> mActivityScenarioRule =
            new ActivityScenarioRule<>(SetupWizardActivity.class);

    @TargetApi(Build.VERSION_CODES.M)
    @Test
    @Config(sdk = Build.VERSION_CODES.M)
    public void testHasPermissionsPage() {
        mActivityScenarioRule.getScenario().moveToState(Lifecycle.State.STARTED);
        mActivityScenarioRule
                .getScenario()
                .onActivity(
                        activity -> {
                            // ensuring we registered for Secure settings observing
                            final Collection<ContentObserver> contentObservers =
                                    Shadows.shadowOf(activity.getContentResolver())
                                            .getContentObservers(Settings.Secure.CONTENT_URI);
                            Assert.assertEquals(1, contentObservers.size());

                            final ViewPager pager = activity.findViewById(R.id.wizard_pages_pager);
                            Assert.assertNotNull(pager);
                            Assert.assertEquals(5, pager.getAdapter().getCount());
                            Assert.assertTrue(
                                    ((FragmentPagerAdapter) pager.getAdapter()).getItem(3)
                                            instanceof WizardPermissionsFragment);
                        });
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Test
    /*I don't want to also verify the permissions page too*/
    @Config(sdk = Build.VERSION_CODES.LOLLIPOP)
    public void testHappyPath() {
        mActivityScenarioRule.getScenario().moveToState(Lifecycle.State.STARTED);
        mActivityScenarioRule
                .getScenario()
                .onActivity(
                        activity -> {
                            // ensuring we registered for Secure settings observing
                            final ShadowContentResolver shadowContentResolver =
                                    Shadows.shadowOf(activity.getContentResolver());
                            final Collection<ContentObserver> contentObservers =
                                    shadowContentResolver.getContentObservers(
                                            Settings.Secure.CONTENT_URI);
                            Assert.assertEquals(1, contentObservers.size());

                            final ViewPager pager = activity.findViewById(R.id.wizard_pages_pager);
                            Assert.assertNotNull(pager);
                            Assert.assertEquals(4, pager.getAdapter().getCount());
                            // starts at page one - welcome keyboard
                            Assert.assertEquals(0, pager.getCurrentItem());
                            Assert.assertTrue(
                                    ((FragmentPagerAdapter) pager.getAdapter()).getItem(0)
                                            instanceof WizardPageWelcomeFragment);
                            Robolectric.getForegroundThreadScheduler()
                                    .setIdleState(Scheduler.IdleState.PAUSED);
                            ((FragmentPagerAdapter) pager.getAdapter())
                                    .getItem(0)
                                    .getView()
                                    .findViewById(R.id.go_to_start_setup)
                                    .performClick();
                            TestRxSchedulers.foregroundAdvanceBy(1000 /*after the animation*/);
                            Robolectric.getForegroundThreadScheduler()
                                    .setIdleState(Scheduler.IdleState.UNPAUSED);
                            // TestRxSchedulers.foregroundFlushAllJobs();

                            // page two - enable ASK
                            Assert.assertEquals(1, pager.getCurrentItem());
                        });

        // now, lets say that ASK was enabled.
        // moving back to state CREATED (basically, pause then stop).
        mActivityScenarioRule.getScenario().moveToState(Lifecycle.State.CREATED);
        TestRxSchedulers.drainAllTasks();

        final String flatASKComponent =
                new ComponentName(BuildConfig.APPLICATION_ID, SoftKeyboard.class.getName())
                        .flattenToString();
        final ContentResolver contentResolver =
                ApplicationProvider.getApplicationContext().getContentResolver();
        Settings.Secure.putString(
                contentResolver, Settings.Secure.ENABLED_INPUT_METHODS, flatASKComponent);
        final Collection<ContentObserver> contentObservers =
                Shadows.shadowOf(contentResolver).getContentObservers(Settings.Secure.CONTENT_URI);
        contentObservers.iterator().next().dispatchChange(false, Settings.Secure.CONTENT_URI);
        TestRxSchedulers.drainAllTasks();
        Robolectric.getForegroundThreadScheduler().setIdleState(Scheduler.IdleState.PAUSED);
        // notifying about the change.
        mActivityScenarioRule.getScenario().moveToState(Lifecycle.State.RESUMED);
        TestRxSchedulers.foregroundAdvanceBy(1000 /*after the animation*/);

        // now at page three - activate keyboard
        mActivityScenarioRule
                .getScenario()
                .onActivity(
                        activity -> {
                            final ViewPager pager = activity.findViewById(R.id.wizard_pages_pager);
                            Assert.assertNotNull(pager);
                            Assert.assertEquals(2, pager.getCurrentItem());

                            Robolectric.getForegroundThreadScheduler()
                                    .setIdleState(Scheduler.IdleState.UNPAUSED);
                        });

        mActivityScenarioRule.getScenario().moveToState(Lifecycle.State.STARTED);
        mActivityScenarioRule
                .getScenario()
                .onActivity(
                        activity -> {
                            Settings.Secure.putString(
                                    activity.getContentResolver(),
                                    Settings.Secure.DEFAULT_INPUT_METHOD,
                                    flatASKComponent);
                            // notifying about the change.
                            contentObservers
                                    .iterator()
                                    .next()
                                    .dispatchChange(false, Settings.Secure.CONTENT_URI);
                            TestRxSchedulers.drainAllTasks();
                            Robolectric.getForegroundThreadScheduler()
                                    .setIdleState(Scheduler.IdleState.PAUSED);
                        });
        mActivityScenarioRule.getScenario().moveToState(Lifecycle.State.RESUMED);
        mActivityScenarioRule
                .getScenario()
                .onActivity(
                        activity -> {
                            TestRxSchedulers.foregroundAdvanceBy(1000 /*after the animation*/);
                            // now at page four - more settings.
                            final ViewPager pager = activity.findViewById(R.id.wizard_pages_pager);
                            Assert.assertNotNull(pager);
                            Assert.assertEquals(3, pager.getCurrentItem());
                        });

        // destroying the fragment should unregister from Secure content provider
        mActivityScenarioRule.getScenario().moveToState(Lifecycle.State.DESTROYED);
        Assert.assertEquals(
                0,
                Shadows.shadowOf(contentResolver)
                        .getContentObservers(Settings.Secure.CONTENT_URI)
                        .size());
    }
}
