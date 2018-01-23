package com.anysoftkeyboard;

import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

class HeavyTestsFilter extends org.junit.runner.manipulation.Filter {
    private final boolean mDoNotFilter = System.getenv("ONLY_HEAVY_TESTS") == null;
    private final boolean mOnlyHeavy = "true".equals(System.getenv("ONLY_HEAVY_TESTS"));

    @Override
    public boolean shouldRun(Description description) {
        if (mDoNotFilter) return true;

        final RunWith runWithAnnotation = description.getTestClass().getAnnotation(RunWith.class);

        //HEAVY is:
        //a AnySoftKeyboardRobolectricTestRunner tester
        //has a Config annotation
        final boolean isHeavy =
                (runWithAnnotation != null && AnySoftKeyboardRobolectricTestRunner.class.equals(runWithAnnotation.value())) &&
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
