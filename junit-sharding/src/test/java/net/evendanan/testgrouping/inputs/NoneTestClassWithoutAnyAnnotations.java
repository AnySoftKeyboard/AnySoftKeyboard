package net.evendanan.testgrouping.inputs;

public class NoneTestClassWithoutAnyAnnotations {

    public void noneTestMethod() {
        throw new IllegalStateException("this should not run");
    }
}
