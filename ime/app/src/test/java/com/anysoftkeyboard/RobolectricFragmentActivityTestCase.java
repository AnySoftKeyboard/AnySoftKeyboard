package com.anysoftkeyboard;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.rx.TestRxSchedulers;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;

/** Driver for a Fragment unit-tests */
@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
@LooperMode(LooperMode.Mode.LEGACY)
public abstract class RobolectricFragmentActivityTestCase<
        A extends AppCompatActivity, F extends Fragment> {

    private ActivityController<A> mActivityController;

    @After
    public void afterRobolectricFragmentActivityTestCase() {
        try {
            mActivityController.destroy();
        } catch (Throwable e) {
            Logger.i(
                    "RobolectricFragmentActivityTestCase",
                    "Failed to destroy the host activity in After. That's okay, I guess.");
        }
    }

    @NonNull
    protected abstract F createFragment();

    @NonNull
    protected final F startFragment() {
        return startFragmentWithState(null);
    }

    @NonNull
    protected final F startFragmentWithState(@Nullable Bundle state) {
        F fragment = createFragment();
        mActivityController = createActivityController(fragment);

        mActivityController.create(state).start().postCreate(state).resume().visible();

        ensureAllScheduledJobsAreDone();

        return fragment;
    }

    protected abstract ActivityController<A> createActivityController(F fragment);

    protected ActivityController<A> getActivityController() {
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
}
