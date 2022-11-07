package net.evendanan.testgrouping.inputs;

import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@SuppressWarnings("JUnit4TestNotRun")
@RunWith(JUnit4.class)
public class TestClassWithRunnerAnnotation {

    public void testMethod() {
        throw new IllegalStateException("this should not run");
    }
}
