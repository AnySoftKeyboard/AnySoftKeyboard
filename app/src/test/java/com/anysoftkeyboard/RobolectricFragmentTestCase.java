package com.anysoftkeyboard;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.anysoftkeyboard.ui.settings.MainSettingsActivity;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.support.v4.SupportFragmentController;

/**
 * Driver for a Fragment unit-tests
 */
@RunWith(RobolectricTestRunner.class)
public abstract class RobolectricFragmentTestCase<T extends Fragment> {

    private SupportFragmentController<T> mFragmentController;

    @NonNull
    protected abstract T createFragment();

    @NonNull
    protected final T startFragment() {
        return startFragmentWithState(null);
    }

    @NonNull
    protected final T startFragmentWithState(@Nullable Bundle state) {
        T fragment = createFragment();

        mFragmentController = SupportFragmentController.of(fragment, MainSettingsActivity.class).attach();

        mFragmentController.create(R.id.main_ui_content, state);
        //if (state != null) mFragmentController.get().onViewStateRestored(state);
        mFragmentController.start().resume().visible();

        ensureAllScheduledJobsAreDone();

        return fragment;
    }

    protected SupportFragmentController<T> getFragmentController() {
        return mFragmentController;
    }

    protected void ensureAllScheduledJobsAreDone() {
        while (Robolectric.getForegroundThreadScheduler().size() > 0 || Robolectric.getBackgroundThreadScheduler().size() > 0) {
            Robolectric.flushBackgroundThreadScheduler();
            Robolectric.flushForegroundThreadScheduler();
        }
    }
    /*Ahead are some basic tests we can run regardless*/

    @Test
    public void testEnsureFragmentHandlesHappyPathLifecycle() {
        startFragment();

        mFragmentController.pause().stop().destroy();
        ensureAllScheduledJobsAreDone();
    }

    @Test
    public void testEnsureFragmentHandlesHappyPathLifecycleWithResume() {
        startFragment();

        mFragmentController.pause().stop();
        ensureAllScheduledJobsAreDone();

        mFragmentController.start().resume();
        ensureAllScheduledJobsAreDone();

        mFragmentController.pause().stop().destroy();
        ensureAllScheduledJobsAreDone();
    }

    @Test
    public void testEnsureFragmentHandlesRecreateWithInstanceState() {
        startFragment();

        mFragmentController.pause().stop();
        Bundle state = new Bundle();
        mFragmentController.get().onSaveInstanceState(state);
        mFragmentController.destroy();

        ensureAllScheduledJobsAreDone();

        startFragmentWithState(state);
    }
}
