package com.anysoftkeyboard;

import net.evendanan.testgrouping.HashingStrategy;
import net.evendanan.testgrouping.TestClassHashingStrategy;
import net.evendanan.testgrouping.TestsGroupingFilter;

import org.junit.runner.Description;
import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Just a way to add general things on-top RobolectricTestRunner.
 */

public class AnySoftKeyboardTestRunner extends RobolectricTestRunner {
    public AnySoftKeyboardTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
        TestsGroupingFilter.addTestsGroupingFilterWithSystemPropertiesData(this, new TestWeightHashingStrategy(), false/*so running from AS will work*/);
    }

    private static class TestWeightHashingStrategy implements HashingStrategy {
        private final TestClassHashingStrategy mFallbackHashing = new TestClassHashingStrategy();

        @Override
        public int calculateHashFromDescription(Description description, int groupsCount) {
            if (groupsCount < 2) {
                return 0;
            }

            if (description.getAnnotation(Config.class) != null || description.getTestClass().getAnnotation(Config.class) != null) {
                return groupsCount - 1;
            } else {
                return mFallbackHashing.calculateHashFromDescription(description, groupsCount - 1);
            }
        }
    }
}
