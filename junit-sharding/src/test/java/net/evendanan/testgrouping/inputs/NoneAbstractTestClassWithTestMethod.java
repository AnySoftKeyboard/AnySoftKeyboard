package net.evendanan.testgrouping.inputs;

import org.junit.Test;

public abstract class NoneAbstractTestClassWithTestMethod {

  @Test
  public void testMethod() {
    throw new IllegalStateException("this should not run");
  }
}
