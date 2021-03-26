package com.anysoftkeyboard;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import com.anysoftkeyboard.rx.TestRxSchedulers;
import com.anysoftkeyboard.ui.settings.MainSettingsActivity;
import com.menny.android.anysoftkeyboard.R;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;
import org.robolectric.shadows.support.v4.SupportFragmentController;

/** Driver for a Fragment unit-tests */
@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
@LooperMode(LooperMode.Mode.LEGACY)
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
        TestMainSettingsActivity.CREATED_FRAGMENT = fragment;

        mFragmentController =
                SupportFragmentController.of(fragment, TestMainSettingsActivity.class)
                        .create(R.id.main_ui_content, state)
                        .start()
                        .postCreate(state)
                        .resume()
                        .visible();

        ensureAllScheduledJobsAreDone();

        return mFragmentController.get();
    }

    protected SupportFragmentController<T> getFragmentController() {
        return mFragmentController;
    }

    protected void ensureAllScheduledJobsAreDone() {
        TestRxSchedulers.drainAllTasks();
    }
    /*Ahead are some basic tests we can run regardless*/

    @Test
    public void testEnsurePortraitFragmentHandlesHappyPathLifecycle() {
        startFragment();

        mFragmentController.pause().stop().destroy();
        ensureAllScheduledJobsAreDone();
    }

    @Test
    @Config(qualifiers = "w480dp-h800dp-land-mdpi")
    public void testEnsureLandscapeFragmentHandlesHappyPathLifecycle() {
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

    public static class TestMainSettingsActivity extends MainSettingsActivity {
        private static Fragment CREATED_FRAGMENT;

        @NonNull
        @Override
        protected Fragment createRootFragmentInstance() {
            return CREATED_FRAGMENT;
        }
    }
}
