package com.anysoftkeyboard;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.rx.TestRxSchedulers;
import com.anysoftkeyboard.ui.settings.MainSettingsActivity;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;

/** Driver for a Fragment unit-tests */
@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
@LooperMode(LooperMode.Mode.LEGACY)
public abstract class RobolectricFragmentTestCase<T extends Fragment> {

    private ActivityController<TestMainSettingsActivity> mActivityController;

    @After
    public void afterRobolectricFragmentTestCase() {
        TestMainSettingsActivity.CREATED_FRAGMENT = null;
        try {
            mActivityController.destroy();
        } catch (Throwable e) {
            Logger.i(
                    "RobolectricFragmentTestCase",
                    "Failed to destroy the host activity in After. That's okay, I guess.");
        }
    }

    @NonNull
    protected abstract T createFragment();

    @NonNull
    protected final T startFragment() {
        return startFragmentWithState(null);
    }

    @NonNull
    protected final T startFragmentWithState(@Nullable Bundle state) {
        T fragment = createFragment();
        TestMainSettingsActivity.CREATED_FRAGMENT = fragment;

        mActivityController =
                ActivityController.of(new TestMainSettingsActivity())
                        .create(state)
                        .start()
                        .postCreate(state)
                        .resume()
                        .visible();

        ensureAllScheduledJobsAreDone();

        return fragment;
    }

    protected ActivityController<TestMainSettingsActivity> getActivityController() {
        return mActivityController;
    }

    protected void ensureAllScheduledJobsAreDone() {
        TestRxSchedulers.drainAllTasks();
    }
    /*Ahead are some basic tests we can run regardless*/

    @Test
    public void testEnsurePortraitFragmentHandlesHappyPathLifecycle() {
        startFragment();

        mActivityController.pause().stop().destroy();
        ensureAllScheduledJobsAreDone();
    }

    @Test
    @Config(qualifiers = "w480dp-h800dp-land-mdpi")
    public void testEnsureLandscapeFragmentHandlesHappyPathLifecycle() {
        startFragment();

        mActivityController.pause().stop().destroy();

        ensureAllScheduledJobsAreDone();
    }

    @Test
    public void testEnsureFragmentHandlesHappyPathLifecycleWithResume() {
        startFragment();

        mActivityController.pause().stop();
        ensureAllScheduledJobsAreDone();

        mActivityController.start().resume();
        ensureAllScheduledJobsAreDone();

        mActivityController.pause().stop().destroy();
        ensureAllScheduledJobsAreDone();
    }

    @Test
    public void testEnsureFragmentHandlesRecreateWithInstanceState() {
        startFragment();

        mActivityController.pause().stop();
        Bundle state = new Bundle();
        mActivityController.saveInstanceState(state);
        mActivityController.destroy();

        ensureAllScheduledJobsAreDone();

        startFragmentWithState(state);
    }

    public static class TestMainSettingsActivity extends MainSettingsActivity {
        private static Fragment CREATED_FRAGMENT;

        @NonNull
        @Override
        protected Fragment createRootFragmentInstance() {
            return CREATED_FRAGMENT;
        }
    }
}
