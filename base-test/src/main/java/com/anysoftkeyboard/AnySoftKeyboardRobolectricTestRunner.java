package com.anysoftkeyboard;

import android.os.Looper;

import com.anysoftkeyboard.rx.TestRxSchedulers;

import net.evendanan.testgrouping.TestClassHashingStrategy;
import net.evendanan.testgrouping.TestsGroupingFilter;

import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runners.model.InitializationError;
import org.robolectric.DefaultTestLifecycle;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.TestLifecycle;
import org.robolectric.android.util.concurrent.RoboExecutorService;

import java.lang.reflect.Method;

import javax.annotation.Nonnull;

/**
 * Just a way to add general things on-top RobolectricTestRunner.
 */

public class AnySoftKeyboardRobolectricTestRunner extends RobolectricTestRunner {
    public AnySoftKeyboardRobolectricTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
        try {
            filter(new HeavyTestsFilter());
        } catch (NoTestsRemainException e) {
            //No tests are okay.
        }
        TestsGroupingFilter.addTestsGroupingFilterWithSystemPropertiesData(this, new TestClassHashingStrategy(), false/*so running from AS will work*/);

    }

    @Nonnull
    @Override
    protected Class<? extends TestLifecycle> getTestLifecycleClass() {
        return AnySoftKeyboardRobolectricTestLifeCycle.class;
    }

    public static class AnySoftKeyboardRobolectricTestLifeCycle extends DefaultTestLifecycle {
        @Override
        public void beforeTest(Method method) {
            TestRxSchedulers.setSchedulers(Looper.getMainLooper(), new RoboExecutorService());
            super.beforeTest(method);
        }
    }
}
