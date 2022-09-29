package net.evendanan.testgrouping;

import org.junit.runner.Description;
import org.junit.runner.RunWith;

@RunWith(ShardingSuite.class)
@ShardingSuite.ShardUsing(TestsSuite.SkipTestInputClasses.class)
public class TestsSuite {
    public static class SkipTestInputClasses extends TestClassHashingStrategy {

        @Override
        public int calculateHashFromDescription(
                final Description description, final int groupsCount) {
            if (description.getTestClass().getName().contains(".inputs."))
                return -1; // this means the test will not be executed.
            return super.calculateHashFromDescription(description, groupsCount);
        }
    }
}
