package com.anysoftkeyboard;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.anysoftkeyboard.ui.settings.MainSettingsActivity;
import com.menny.android.anysoftkeyboard.AskGradleTestRunner;
import com.menny.android.anysoftkeyboard.R;

import net.evendanan.chauffeur.lib.FragmentChauffeurActivity;
import net.evendanan.chauffeur.lib.experiences.TransitionExperiences;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.ActivityController;

/**
 * Driver for a Fragment unit-tests
 */
@RunWith(AskGradleTestRunner.class)
public abstract class RobolectricFragmentTestCase<T extends Fragment> {

    private Intent mStartFragmentIntent;
    private T mFragment;
    private ActivityController<MainSettingsActivity> mActivityController;

    @NonNull
    protected abstract T createFragment();

    @NonNull
    protected final T startFragment() {
        return startFragmentWithState(null);
    }

    @NonNull
    protected final T startFragmentWithState(@Nullable Bundle state) {
        mActivityController = Robolectric.buildActivity(MainSettingsActivity.class);
        Fragment prototypeFragment = createFragment();
        Assert.assertNotNull(prototypeFragment);
        Intent startFragmentIntent = FragmentChauffeurActivity.createStartActivityIntentForAddingFragmentToUi(RuntimeEnvironment.application, MainSettingsActivity.class, prototypeFragment, TransitionExperiences.ROOT_FRAGMENT_EXPERIENCE_TRANSITION);

        mActivityController.withIntent(startFragmentIntent);

        mActivityController.attach().create(state).postCreate(state).start();
        if (state != null) mActivityController.restoreInstanceState(state);
        mActivityController.resume().postResume();

        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();

        MainSettingsActivity mainSettingsActivity = mActivityController.get();
        Fragment actualFragment = mainSettingsActivity.getSupportFragmentManager().findFragmentById(R.id.main_ui_content);
        Assert.assertNotNull(actualFragment);
        Assert.assertEquals(prototypeFragment.getClass(), actualFragment.getClass());
        mFragment = (T)actualFragment;
        return mFragment;
    }

    /*Ahead are some basic tests we can run regardless*/

    @Test
    public void testEnsureFragmentHandlesHappyPathLifecycle() {
        startFragment();

        mActivityController.pause().stop().destroy();
        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();
    }

    @Test
    public void testEnsureFragmentHandlesHappyPathLifecycleWithResume() {
        startFragment();

        mActivityController.pause().stop();
        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();
        mActivityController.restart().start().resume();
        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();

        mActivityController.pause().stop().destroy();
        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();
    }

    @Test
    public void testEnsureFragmentHandlesRecreate() {
        startFragment();

        Bundle state = new Bundle();
        mActivityController.saveInstanceState(state).pause().stop().destroy();
        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();

        startFragmentWithState(state);

        state = new Bundle();
        mActivityController.saveInstanceState(state).pause().stop().destroy();
        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();
    }
}
