package net.evendanan.testgrouping.inputs;

import org.junit.Test;

class NonePackageTestClassWithTestMethod {

    @Test
    public void testMethod() {
        throw new IllegalStateException("this should not run");
    }
}
