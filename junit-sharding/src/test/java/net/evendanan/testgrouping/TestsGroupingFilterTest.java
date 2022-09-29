package net.evendanan.testgrouping;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class TestsGroupingFilterTest {

    @After
    public void tearDown() {
        System.clearProperty(TestsGroupingFilter.TEST_GROUPS_COUNT_SYSTEM_PROPERTY_KEY);
        System.clearProperty(TestsGroupingFilter.TEST_GROUP_TO_EXECUTE_SYSTEM_PROPERTY_KEY);
    }

    @Test
    public void addTestsGroupingFilterWithSystemPropertiesData() throws Exception {
        System.setProperty(TestsGroupingFilter.TEST_GROUPS_COUNT_SYSTEM_PROPERTY_KEY, "2");
        System.setProperty(TestsGroupingFilter.TEST_GROUP_TO_EXECUTE_SYSTEM_PROPERTY_KEY, "1");

        Filterable runner = Mockito.mock(Filterable.class);

        TestsGroupingFilter.addTestsGroupingFilterWithSystemPropertiesData(runner, true);

        Mockito.verify(runner).filter(Mockito.notNull(TestsGroupingFilter.class));
    }

    @Test(expected = IllegalStateException.class)
    public void addTestsGroupingFilterWithSystemPropertiesDataThrowsExceptionIfNoData_1()
            throws Exception {
        Filterable runner = Mockito.mock(Filterable.class);

        TestsGroupingFilter.addTestsGroupingFilterWithSystemPropertiesData(runner, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addTestsGroupingFilterWithSystemPropertiesDataThrowsExceptionIfNoStrategy()
            throws Exception {
        Filterable runner = Mockito.mock(Filterable.class);

        TestsGroupingFilter.addTestsGroupingFilterToRunner(runner, null, 1, 0);
    }

    @Test(expected = IllegalStateException.class)
    public void addTestsGroupingFilterWithSystemPropertiesDataThrowsExceptionIfNoData_NullStrategy()
            throws Exception {
        Filterable runner = Mockito.mock(Filterable.class);

        TestsGroupingFilter.addTestsGroupingFilterWithSystemPropertiesData(runner, null, true);
    }

    @Test(expected = IllegalStateException.class)
    public void addTestsGroupingFilterWithSystemPropertiesDataThrowsExceptionIfNoData_2()
            throws Exception {
        System.setProperty(TestsGroupingFilter.TEST_GROUPS_COUNT_SYSTEM_PROPERTY_KEY, "2");

        Filterable runner = Mockito.mock(Filterable.class);

        TestsGroupingFilter.addTestsGroupingFilterWithSystemPropertiesData(runner, true);
    }

    @Test(expected = IllegalStateException.class)
    public void addTestsGroupingFilterWithSystemPropertiesDataThrowsExceptionIfNoData_3()
            throws Exception {
        System.setProperty(TestsGroupingFilter.TEST_GROUP_TO_EXECUTE_SYSTEM_PROPERTY_KEY, "1");

        Filterable runner = Mockito.mock(Filterable.class);

        TestsGroupingFilter.addTestsGroupingFilterWithSystemPropertiesData(runner, true);
    }

    @Test(expected = NumberFormatException.class)
    public void addTestsGroupingFilterWithSystemPropertiesDataThrowsExceptionIfNotNumber()
            throws Exception {
        System.setProperty(
                TestsGroupingFilter.TEST_GROUPS_COUNT_SYSTEM_PROPERTY_KEY, "NOT_A_NUMBER");
        System.setProperty(TestsGroupingFilter.TEST_GROUP_TO_EXECUTE_SYSTEM_PROPERTY_KEY, "1");

        Filterable runner = Mockito.mock(Filterable.class);

        TestsGroupingFilter.addTestsGroupingFilterWithSystemPropertiesData(runner, true);
    }

    @Test
    public void
            addTestsGroupingFilterWithSystemPropertiesDataDoesNotThrowExceptionIfNoDataAndFalsePassed()
                    throws Exception {
        Filterable runner = Mockito.mock(Filterable.class);

        TestsGroupingFilter.addTestsGroupingFilterWithSystemPropertiesData(runner, false);

        Mockito.verifyZeroInteractions(runner);
    }

    @Test
    public void addTestsGroupingFilterToRunner() throws Exception {
        Filterable runner = Mockito.mock(Filterable.class);

        TestsGroupingFilter.addTestsGroupingFilterToRunner(runner, 1, 0);

        Mockito.verify(runner).filter(Mockito.notNull(TestsGroupingFilter.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void addTestsGroupingFilterToRunnerFailsWithNegativeGroup() throws Exception {
        Filterable runner = Mockito.mock(Filterable.class);

        TestsGroupingFilter.addTestsGroupingFilterToRunner(runner, 1, -1);

        Mockito.verify(runner).filter(Mockito.notNull(TestsGroupingFilter.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void addTestsGroupingFilterToRunnerFailsWithGroupOutOfRange() throws Exception {
        Filterable runner = Mockito.mock(Filterable.class);

        TestsGroupingFilter.addTestsGroupingFilterToRunner(runner, 1, 1);

        Mockito.verify(runner).filter(Mockito.notNull(TestsGroupingFilter.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void addTestsGroupingFilterToRunnerFailsWithGroupsCountNegative() throws Exception {
        Filterable runner = Mockito.mock(Filterable.class);

        TestsGroupingFilter.addTestsGroupingFilterToRunner(runner, -1, 1);

        Mockito.verify(runner).filter(Mockito.notNull(TestsGroupingFilter.class));
    }

    @Test
    public void addTestsGroupingFilterToRunnerSwallowsNoTestsRemainException() throws Exception {
        Filterable runner = Mockito.mock(Filterable.class);
        Mockito.doAnswer(
                        new Answer() {
                            @Override
                            public Object answer(InvocationOnMock invocation) throws Throwable {
                                throw new NoTestsRemainException();
                            }
                        })
                .when(runner)
                .filter(Mockito.any(Filter.class));

        TestsGroupingFilter.addTestsGroupingFilterToRunner(runner, 1, 0);

        Mockito.verify(runner).filter(Mockito.notNull(TestsGroupingFilter.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void addTestsGroupingFilterToRunnerDoesNotSwallowsNonNoTestsRemainException()
            throws Exception {
        Filterable runner = Mockito.mock(Filterable.class);
        Mockito.doAnswer(
                        new Answer() {
                            @Override
                            public Object answer(InvocationOnMock invocation) throws Throwable {
                                throw new IllegalArgumentException();
                            }
                        })
                .when(runner)
                .filter(Mockito.any(Filter.class));

        TestsGroupingFilter.addTestsGroupingFilterToRunner(runner, 1, 0);
    }

    private static int[] generateCountsForGroupCount(
            final int groupCount, final int testIterationLow, final int testIterationHigh) {
        final Filter[] filters = new Filter[groupCount];
        for (int groupIndex = 0; groupIndex < groupCount; groupIndex++) {
            filters[groupIndex] = new TestableTestsGroupingFilter(groupCount, groupIndex);
        }

        final int[] counts = new int[groupCount];

        for (int testIteration = testIterationLow;
                testIteration < testIterationHigh;
                testIteration++) {
            Description description =
                    TestableTestsGroupingFilter.mockDescriptionWithHashcode(testIteration);
            for (int testGroup = 0; testGroup < groupCount; testGroup++) {
                Filter filter = filters[testGroup];
                if (filter.shouldRun(description)) counts[testGroup]++;
            }
        }

        return counts;
    }

    @Test
    public void shouldRunWithOneGroup() throws Exception {
        int[] count = generateCountsForGroupCount(1, -100, 100);
        Assert.assertEquals(1, count.length);
        Assert.assertEquals(200, count[0]);
    }

    @Test
    public void shouldRunWithTwoGroups() throws Exception {
        int[] count = generateCountsForGroupCount(2, -100, 100);
        Assert.assertEquals(2, count.length);
        Assert.assertEquals(100, count[0]);
        Assert.assertEquals(100, count[1]);
    }

    @Test
    public void shouldRunWithThreeGroups() throws Exception {
        int[] count = generateCountsForGroupCount(3, -102, 198);
        Assert.assertEquals(3, count.length);
        Assert.assertEquals(100, count[0]);
        Assert.assertEquals(100, count[1]);
        Assert.assertEquals(100, count[2]);
    }

    @Test
    public void defaultHashcodeIsStableFromClassName() throws Exception {
        final TestsGroupingFilter filterFirst = new TestsGroupingFilter(2, 0);
        final TestsGroupingFilter filterSecond = new TestsGroupingFilter(2, 1);

        final Description description = Mockito.mock(Description.class);

        Mockito.doReturn("a").when(description).getClassName();

        Assert.assertFalse(filterFirst.shouldRun(description));
        Assert.assertTrue(filterSecond.shouldRun(description));

        Mockito.doReturn("c").when(description).getClassName();

        Assert.assertFalse(filterFirst.shouldRun(description));
        Assert.assertTrue(filterSecond.shouldRun(description));

        Mockito.doReturn("b").when(description).getClassName();

        Assert.assertTrue(filterFirst.shouldRun(description));
        Assert.assertFalse(filterSecond.shouldRun(description));

        Mockito.doReturn("d").when(description).getClassName();

        Assert.assertTrue(filterFirst.shouldRun(description));
        Assert.assertFalse(filterSecond.shouldRun(description));
    }

    @Test
    public void describe() throws Exception {
        Assert.assertEquals(
                "Execute tests from group 1 (out of 2)", new TestsGroupingFilter(2, 1).describe());
    }

    public static class TestableTestsGroupingFilter extends TestsGroupingFilter {

        public TestableTestsGroupingFilter(int groupCount, int groupToExecute) {
            super(new TestCountStrategy(), groupCount, groupToExecute);
        }

        static Description mockDescriptionWithHashcode(int hashcode) {
            Description description = Mockito.mock(Description.class);
            // using testCount here since it is the only thing I can mock with Mockito.
            Mockito.when(description.testCount()).thenReturn(hashcode);

            return description;
        }

        private static class TestCountStrategy extends SimpleHashingStrategyBase {
            @Override
            public int calculateHashFromDescription(Description description) {
                return description.testCount();
            }
        }
    }
}
