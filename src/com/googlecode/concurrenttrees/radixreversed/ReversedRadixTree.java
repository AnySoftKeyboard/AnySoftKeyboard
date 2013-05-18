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
package com.googlecode.concurrenttrees.radixreversed;

import com.googlecode.concurrenttrees.common.KeyValuePair;

/**
 * API of a reversed radix tree, that is a tree which allows values to be looked up based on <i>suffixes</i> of the keys
 * with which they were associated, as well as based on exact matches for keys. A reverse radix tree essentially allows
 * <i><u>"equals"</u></i> and <i><u>"ends with"</u></i> lookup.
 * <p/>
 * This has the same structure as a radix tree, but this tree transparently reverses characters in the keys added to the
 * tree, and reverses characters supplied for queries. This is <i><u>not the same as a suffix tree</u></i> - a suffix
 * tree stores <i>all possible</i> suffix substrings of keys (supporting <i>"contains"</i> lookup); this tree only
 * stores the original key in reverse character order.
 * <p/>
 * See documentation on each method for details.
 *
 * @param <O> The type of the values associated with keys in the tree
 *
 * @see com.googlecode.concurrenttrees.radix.RadixTree
 * @author Niall Gallagher
 */
public interface ReversedRadixTree<O> {

    /**
     * Associates the given value with the given key; replacing any previous value associated with the key.
     * Returns the previous value associated with the key, if any.
     * <p/>
     * This operation is performed atomically.
     *
     * @param key The key with which the specified value should be associated
     * @param value The value to associate with the key, which cannot be null
     * @return The previous value associated with the key, if there was one, otherwise null
     */
    O put(CharSequence key, O value);

    /**
     * If a value is not already associated with the given key in the tree, associates the given value with the
     * key; otherwise if an existing value is already associated, returns the existing value and does not overwrite it.
     * <p/>
     * This operation is performed atomically.
     *
     * @param key The key with which the specified value should be associated
     * @param value The value to associate with the key, which cannot be null
     * @return The existing value associated with the key, if there was one; otherwise null in which case the new
     * value was successfully associated
     */
    O putIfAbsent(CharSequence key, O value);

    /**
     * Removes the value associated with the given key (exact match).
     * If no value is associated with the key, does nothing.
     *
     * @param key The key for which an associated value should be removed
     * @return True if a value was removed (and therefore was associated with the key), false if no value was
     * associated/removed
     */
    boolean remove(CharSequence key);

    /**
     * Returns the value associated with the given key (exact match), or returns null if no such value
     * is associated with the key.
     *
     * @param key The key with which a sought value might be associated
     * @return The value associated with the given key (exact match), or null if no value was associated with the key
     */
    O getValueForExactKey(CharSequence key);

    /**
     * Returns a lazy iterable which returns the set of keys in the tree which end with the given suffix.
     * <p/>
     * This is <i>inclusive</i> - if the given suffix is an exact match for a key in the tree, that key is also
     * returned.
     *
     * @param suffix A suffix of sought keys in the tree
     * @return The set of keys in the tree which end with the given suffix, inclusive
     */
    Iterable<CharSequence> getKeysEndingWith(CharSequence suffix);

    /**
     * Returns a lazy iterable which returns the set of values associated with keys in the tree which end with the
     * given suffix.
     * <p/>
     * This is <i>inclusive</i> - if the given suffix is an exact match for a key in the tree, the value associated
     * with that key is also returned.
     * <p/>
     * Note that although the same value might originally have been associated with multiple keys, the set returned
     * does not contain duplicates (as determined by the value objects' implementation of {@link #equals(Object)}).
     *
     * @param suffix A suffix of keys in the tree for which associated values are sought
     * @return The set of values associated with keys in the tree which end with the given suffix, inclusive
     */
    Iterable<O> getValuesForKeysEndingWith(CharSequence suffix);

    /**
     * Returns a lazy iterable which returns the set of {@link KeyValuePair}s for keys and their associated values
     * in the tree, where the keys end with the given suffix.
     * <p/>
     * This is <i>inclusive</i> - if the given suffix is an exact match for a key in the tree, the {@link KeyValuePair}
     * for that key is also returned.
     *
     * @param suffix A suffix of keys in the tree for which associated {@link KeyValuePair}s are sought
     * @return The set of {@link KeyValuePair}s for keys in the tree which end with the given suffix, inclusive
     */
    Iterable<KeyValuePair<O>> getKeyValuePairsForKeysEndingWith(CharSequence suffix);
}
