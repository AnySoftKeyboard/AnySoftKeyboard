package com.anysoftkeyboard;

import net.evendanan.testgrouping.TestClassHashingStrategy;
import net.evendanan.testgrouping.TestsGroupingFilter;

import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Just a way to add general things on-top RobolectricTestRunner.
 */

public class AnySoftKeyboardTestRunner extends RobolectricTestRunner {
    public AnySoftKeyboardTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
        try {
            filter(new HeavyTestsFilter());
        } catch (NoTestsRemainException e) {
            //No tests are okay.
        }
        TestsGroupingFilter.addTestsGroupingFilterWithSystemPropertiesData(this, new TestClassHashingStrategy(), false/*so running from AS will work*/);
    }

    private static class HeavyTestsFilter extends org.junit.runner.manipulation.Filter {
        private final boolean mDoNotFilter = System.getenv("ONLY_HEAVY_TESTS") == null;
        private final boolean mOnlyHeavy = "true".equals(System.getenv("ONLY_HEAVY_TESTS"));

        @Override
        public boolean shouldRun(Description description) {
            if (mDoNotFilter) return true;

            final RunWith runWithAnnotation = description.getTestClass().getAnnotation(RunWith.class);

            //HEAVY is:
            //a AnySoftKeyboardTestRunner tester
            //has a Config annotation
            final boolean isHeavy =
                    (runWithAnnotation != null && AnySoftKeyboardTestRunner.class.equals(runWithAnnotation.value())) &&
                            (description.getAnnotation(Config.class) != null || description.getTestClass().getAnnotation(Config.class) != null);

            if (mOnlyHeavy) {
                return isHeavy;
            } else {
                return !isHeavy;
            }
        }

        @Override
        public String describe() {
            return "Filter Heavy Tests " + mOnlyHeavy;
        }
    }
}
