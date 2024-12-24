package net.evendanan.testgrouping;

import java.util.Locale;
import java.util.Properties;
import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.NoTestsRemainException;

/** Will filter out tests that are not in the split group. */
public class TestsGroupingFilter extends Filter {

  public static final String TEST_GROUPS_COUNT_SYSTEM_PROPERTY_KEY =
      "TestsGroupingFilter_TEST_GROUPS_COUNT_SYSTEM_PROPERTY_KEY";
  public static final String TEST_GROUP_TO_EXECUTE_SYSTEM_PROPERTY_KEY =
      "TestsGroupingFilter_TEST_GROUP_TO_EXECUTE_SYSTEM_PROPERTY_KEY";

  /**
   * Adds a {@link TestsGroupingFilter} to the runner with {@link TestClassHashingStrategy} hashing
   * strategy.
   */
  public static void addTestsGroupingFilterWithSystemPropertiesData(
      Filterable testRunner, boolean failIfDataMissing) {
    addTestsGroupingFilterWithSystemPropertiesData(
        testRunner, new TestClassHashingStrategy(), failIfDataMissing);
  }

  public static void addTestsGroupingFilterWithSystemPropertiesData(
      Filterable testRunner, HashingStrategy hashingStrategy, boolean failIfDataMissing) {
    Properties systemProperties = System.getProperties();
    if (systemProperties.containsKey(TEST_GROUPS_COUNT_SYSTEM_PROPERTY_KEY)
        && systemProperties.containsKey(TEST_GROUP_TO_EXECUTE_SYSTEM_PROPERTY_KEY)) {
      int groupCount =
          Integer.parseInt(systemProperties.getProperty(TEST_GROUPS_COUNT_SYSTEM_PROPERTY_KEY));
      int groupToExecute =
          Integer.parseInt(systemProperties.getProperty(TEST_GROUP_TO_EXECUTE_SYSTEM_PROPERTY_KEY));
      addTestsGroupingFilterToRunner(testRunner, hashingStrategy, groupCount, groupToExecute);
    } else if (failIfDataMissing) {
      throw new IllegalStateException(
          String.format(
              Locale.US,
              "Could not find '%s' and '%s' in System.properties!",
              TEST_GROUPS_COUNT_SYSTEM_PROPERTY_KEY,
              TEST_GROUP_TO_EXECUTE_SYSTEM_PROPERTY_KEY));
    }
  }

  /**
   * Adds a {@link TestsGroupingFilter} to the runner with {@link TestClassHashingStrategy} hashing
   * strategy.
   */
  public static void addTestsGroupingFilterToRunner(
      Filterable testRunner, int groupCount, int groupToExecute) {
    addTestsGroupingFilterToRunner(
        testRunner, new TestClassHashingStrategy(), groupCount, groupToExecute);
  }

  public static void addTestsGroupingFilterToRunner(
      Filterable testRunner, HashingStrategy hashingStrategy, int groupCount, int groupToExecute) {
    try {
      testRunner.filter(new TestsGroupingFilter(hashingStrategy, groupCount, groupToExecute));
    } catch (NoTestsRemainException e) {
      // swallow.
      // I know what I'm doing
    }
  }

  private final int mGroupToExecute;
  private final int mGroupCount;
  private final HashingStrategy mHashingStrategy;

  /**
   * Creates a TestsGroupingFilter with {@link TestClassHashingStrategy}.
   *
   * @param groupCount total number of text groups.
   * @param groupToExecute current execution group index.
   */
  public TestsGroupingFilter(int groupCount, int groupToExecute) {
    this(new TestClassHashingStrategy(), groupCount, groupToExecute);
  }

  /**
   * Creates a TestsGroupingFilter.
   *
   * @param hashingStrategy strategy used to determine test-group hash.
   * @param groupCount total number of text groups.
   * @param groupToExecute current execution group index.
   */
  public TestsGroupingFilter(HashingStrategy hashingStrategy, int groupCount, int groupToExecute) {
    if (hashingStrategy == null) {
      throw new IllegalArgumentException("hashingStrategy can not be null");
    }
    if (groupCount <= 0) {
      throw new IllegalArgumentException("groupCount should be greater than zero.");
    }
    if (groupToExecute < 0) {
      throw new IllegalArgumentException("groupToExecute should be a non-negative number.");
    }
    if (groupToExecute >= groupCount) {
      throw new IllegalArgumentException("groupToExecute should less than groupCount.");
    }

    mHashingStrategy = hashingStrategy;
    mGroupToExecute = groupToExecute;
    mGroupCount = groupCount;
  }

  @Override
  public boolean shouldRun(Description description) {
    return getGroupNumberFor(description, mGroupCount) == mGroupToExecute;
  }

  private int getGroupNumberFor(Description description, int groupCount) {
    return mHashingStrategy.calculateHashFromDescription(description, groupCount);
  }

  @Override
  public String describe() {
    return String.format(
        Locale.US, "Execute tests from group %d (out of %d)", mGroupToExecute, mGroupCount);
  }
}
