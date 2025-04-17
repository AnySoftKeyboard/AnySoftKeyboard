package net.evendanan.testgrouping;

import org.junit.runner.Description;

/** Defines the interface for a strategy used to calculate the test's execution group. */
public interface HashingStrategy {

  /**
   * Should return an integer (non-negative) that used to describes the execution group.
   *
   * @param description the test method {@link Description} instance.
   * @param groupsCount the total number of groups.
   * @return the hashing value of for the given description. It should be between [0..groupsCount).
   *     If it returns anything out of that range the described test-unit will not be executed. In
   *     some cases, that might be okay, say you want to filter certain tests completely from
   *     running.
   */
  int calculateHashFromDescription(Description description, int groupsCount);
}
