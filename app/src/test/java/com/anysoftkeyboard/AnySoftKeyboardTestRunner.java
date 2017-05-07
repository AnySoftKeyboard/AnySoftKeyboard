package com.anysoftkeyboard;

import net.evendanan.testgrouping.TestsGroupingFilter;

import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricTestRunner;

/**
 * Just a way to add general things on-top RobolectricTestRunner.
 */

public class AnySoftKeyboardTestRunner extends RobolectricTestRunner {
    public AnySoftKeyboardTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
        TestsGroupingFilter.addTestsGroupingFilterWithSystemPropertiesData(this, false/*so running from AS will work*/);
    }
}
