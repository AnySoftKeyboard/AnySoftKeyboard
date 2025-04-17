package net.evendanan.testgrouping.inputs;

import net.evendanan.testgrouping.HashingStrategy;
import org.junit.runner.Description;

public class TestableHashingStrategy implements HashingStrategy {

  @Override
  public int calculateHashFromDescription(final Description description, final int groupsCount) {
    if (description.getTestClass().getName().contains("TestClassWithTestMethodToSkip")) {
      return -1;
    }

    if (groupsCount == 2) {
      if (description.getTestClass().getName().contains("TestClassWithRunnerAnnotation")) {
        return 0;
      } else if (description.getTestClass().getName().contains("TestClassWithTestMethod")) {
        return 1;
      } else {
        return -1;
      }
    }

    return 0;
  }
}
