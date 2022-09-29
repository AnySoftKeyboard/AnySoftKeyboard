package net.evendanan.testgrouping.inputs;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
public class NoneTestClassWithSuiteAnnotation {

    public void testMethod() {
        throw new IllegalStateException("this should not run");
    }
}
