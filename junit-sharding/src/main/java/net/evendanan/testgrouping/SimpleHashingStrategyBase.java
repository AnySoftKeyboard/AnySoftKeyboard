package net.evendanan.testgrouping;

import org.junit.runner.Description;

/**
 * A simple implementation of {@link HashingStrategy} which allows you to just implement a simple,
 * normally-distributed, hashing function for the given {@link Description}.
 */
public abstract class SimpleHashingStrategyBase implements HashingStrategy {

    @Override
    public int calculateHashFromDescription(final Description description, final int groupsCount) {
        return Math.abs(calculateHashFromDescription(description)) % groupsCount;
    }

    /** calculates a hashing value for the given description. */
    protected abstract int calculateHashFromDescription(Description description);
}
