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
package com.googlecode.concurrenttrees.radixinverted;

import com.googlecode.concurrenttrees.common.KeyValuePair;

/**
 * API of an inverted radix tree, that is a radix tree which is set up to scan external documents for keys previously
 * added to the tree, rather than for data contained in the tree itself.
 * <p/>
 * This method of scanning external documents for keys (or keywords) can be <u>significantly faster</u> than repeatedly
 * scanning the same document for each and every keyword using {@link String#contains(CharSequence)} on the document.
 * <p/>
 * Time complexity is O(d) rather than O(dn), where d=length of document and n=number of keys/keywords.
 * <p/>
 * See documentation on each method for details.
 *
 * @param <O> The type of the values associated with keys in the tree
 *
 * @author Niall Gallagher
 */
public interface InvertedRadixTree<O> {

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
     * Returns a lazy iterable which returns the set of keys in the tree which are contained in the given document.
     *
     * @param document A document to be scanned for keys contained in the tree
     * @return The set of keys in the tree which are contained in the given document
     */
    Iterable<CharSequence> getKeysContainedIn(CharSequence document);

    /**
     * Returns a lazy iterable which returns the set of values associated with keys in the tree which are contained
     * in the given document.
     *
     * @param document A document to be scanned for keys contained in the tree
     * @return The set of values associated with keys in the tree which are contained in the given document
     */
    Iterable<O> getValuesForKeysContainedIn(CharSequence document);

    /**
     * Returns a lazy iterable which returns the set of {@link KeyValuePair}s for keys in the tree which are contained
     * in the given document.
     *
     * @param document A document to be scanned for keys contained in the tree
     * @return The set of {@link KeyValuePair}s for keys in the tree which are contained in the given document
     */
    Iterable<KeyValuePair<O>> getKeyValuePairsForKeysContainedIn(CharSequence document);
}
