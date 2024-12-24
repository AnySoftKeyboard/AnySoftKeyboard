package net.evendanan.testgrouping.inputs;

import org.junit.Test;

public class TestClassWithTestMethodToSkip {

  @Test
  public void testMethod() {
    throw new IllegalStateException("this should not run");
  }
}
