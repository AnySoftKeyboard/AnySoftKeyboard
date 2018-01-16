package com.anysoftkeyboard;

import net.evendanan.testgrouping.TestClassHashingStrategy;
import net.evendanan.testgrouping.TestsGroupingFilter;

import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

/**
 * Just a way to add general things on-top RobolectricTestRunner.
 */

public class AnySoftKeyboardPlainTestRunner extends BlockJUnit4ClassRunner {
    public AnySoftKeyboardPlainTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
        try {
            filter(new HeavyTestsFilter());
        } catch (NoTestsRemainException e) {
            //No tests are okay.
        }
        TestsGroupingFilter.addTestsGroupingFilterWithSystemPropertiesData(this, new TestClassHashingStrategy(), false/*so running from AS will work*/);
    }

}
