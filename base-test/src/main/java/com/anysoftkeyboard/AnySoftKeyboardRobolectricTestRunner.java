package com.anysoftkeyboard;

import net.evendanan.testgrouping.TestClassHashingStrategy;
import net.evendanan.testgrouping.TestsGroupingFilter;

import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricTestRunner;

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
}
