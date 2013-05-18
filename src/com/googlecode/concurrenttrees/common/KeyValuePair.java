/**
 * Copyright 2012-2013 Niall Gallagher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.googlecode.concurrenttrees.common;

import java.io.Serializable;

/**
 * Encapsulates a key and a value.
 *
 * @param <O> The type of the value
 * @author Niall Gallagher
 */
public interface KeyValuePair<O> {

    /**
     * Returns the key with which the value is associated
     * @return The key with which the value is associated
     */
    CharSequence getKey();

    /**
     * Returns the value associated with the key
     * @return The value associated with the key
     */
    O getValue();

    /**
     * Compares this {@link KeyValuePair} object with another for equality.
     * <p/>
     * This is implemented based on equality of the keys.
     *
     * @param o The other object to compare
     * @return True if the other object is also a {@link KeyValuePair} and is equal to this one as specified above
     */
    @Override
    boolean equals(Object o);

    /**
     * Returns a hash code for this object.
     */
    @Override
    int hashCode();

    /**
     * Returns a string representation as {@code (key, value)}.
     * @return A string representation as {@code (key, value)}
     */
    @Override
    String toString();
}