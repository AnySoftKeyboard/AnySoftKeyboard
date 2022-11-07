package net.evendanan.testgrouping.inputs;

import net.evendanan.testgrouping.ShardingSuite;
import org.junit.runner.RunWith;

@RunWith(ShardingSuite.class)
@ShardingSuite.ShardUsing(TestableHashingStrategy.class)
public class SuiteToTest {}
