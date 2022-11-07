package net.evendanan.testgrouping;

import org.junit.runner.Description;

/** Groups tests by their class name. */
public class TestClassHashingStrategy extends SimpleHashingStrategyBase {

    @Override
    protected int calculateHashFromDescription(final Description description) {
        return stableStringHashcode(description.getClassName());
    }

    /**
     * This is a stable (known) implementation of calculating a hash-code for the specified {@link
     * String}. This is here to ensure that you get the same hashcode for a String no matter which
     * JDK version or OS you are using.
     *
     * <p>Note: This hash function is in no way cryptographically impressive. But we can assume that
     * over a large number of tests, this should have normal distribution.
     */
    public static int stableStringHashcode(String string) {
        int hash = 0;
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            hash += c;
        }

        return hash;
    }
}
