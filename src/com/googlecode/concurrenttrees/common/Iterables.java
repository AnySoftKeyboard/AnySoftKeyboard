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
import java.util.*;

/**
 * Provides methods to convert {@link Iterable}s to {@link List}s and {@link Set}s.
 * <p/>
 * This class is mostly provided for backwards compatibility in applications which were programmed against
 * concurrent-trees 1.0.0, in which the tree APIs returned lists and sets instead of lazily-evaluated iterables.
 * <p/>
 * Note that in applications which would have simply iterated through the lists and sets returned by the old APIs,
 * the new approach of returning lazy iterables is more efficient. Applications can iterate the iterables returned
 * in exactly the same manner, and results will be the same.
 * <p/>
 * These methods are provided for convenience in applications which <i>actually</i> relied on List and Set-specific
 * features in the objects which were returned.
 * <p/>
 * Most methods in this class are somewhat similar to utilities in Google Guava; but are provided here to avoid a
 * dependency on Guava. Applications could use either these methods or Guava.
 *
 * @author Niall Gallagher
 */
public class Iterables implements Serializable{

    // Most methods in this class are somewhat similar to utilities in Google Guava; but are provided here
    // to avoid a dependency on Guava. Applications could use either these methods or Guava.

    /**
	 * 
	 */
	private static final long serialVersionUID = 4624060691749070061L;

	/**
     * Copies elements from the given {@link Iterable} into a new {@link List}.
     * <p/>
     * The iteration order of the list returned, will be the same as that of the iterable.
     * <p/>
     * Be aware of the memory implications of copying objects from a lazy iterable into a collection;
     * usually it's better to just work with the iterable directly (i.e. by iterating it).
     *
     * @param iterable Provides elements to be copied into a new list
     * @param <T> The type of elements returned by the iterable
     * @return A new {@link List} which contains the elements which were returned by the iterable
     */
    public static <T> List<T> toList(Iterable<T> iterable) {
        if (iterable instanceof Collection) {
            return new ArrayList<T>((Collection<T>)iterable);
        }
        else {
            List<T> list = new LinkedList<T>();
            for (T element : iterable) {
                list.add(element);
            }
            return list;
        }
    }

    /**
     * Copies elements from the given {@link Iterable} into a new {@link Set}.
     * <p/>
     * The iteration order of the set returned, will be the same as that of the iterable.
     * <p/>
     * Be aware of the memory implications of copying objects from a lazy iterable into a collection;
     * usually it's better to just work with the iterable directly (i.e. by iterating it).
     *
     * @param iterable Provides elements to be copied into a new set
     * @param <T> The type of elements returned by the iterable
     * @return A new {@link Set} which contains the elements which were returned by the iterable
     */
    public static <T> Set<T> toSet(Iterable<T> iterable) {
        if (iterable instanceof Collection) {
            // Return a LinkedHashSet instead of HashSet, to preserve iteration order...
            return new LinkedHashSet<T>((Collection<T>)iterable);
        }
        else {
            Set<T> list = new LinkedHashSet<T>();
            for (T element : iterable) {
                list.add(element);
            }
            return list;
        }
    }

    /**
     * Returns a string representation of elements returned by the given {@link Iterable}.
     *
     * @param iterable Provides elements whose <code>toString</code> representations should be included in the string
     * @return A string representation of elements returned by the given {@link Iterable}
     */
    public static String toString(Iterable<?> iterable) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (Iterator<?> i = iterable.iterator(); i.hasNext();) {
            sb.append(i.next());
            if (i.hasNext()) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Counts the number of elements returned by the given {@link Iterable}.
     *
     * @param iterable Provides elements to be counted
     * @return The number of elements returned by the iterable
     */
    public static int count(Iterable<?> iterable) {
        int count = 0;
        //noinspection UnusedDeclaration
        for (Object next : iterable) {
            count++;
        }
        return count;
    }

    /**
     * Private constructor, not used.
     */
    Iterables() {
    }
}
