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
package com.googlecode.concurrenttrees.radix.node;

import java.util.List;

/**
 * An interface for a factory which creates new {@link Node} objects on demand, to encapsulate specified variables.
 * Factory objects can choose to return implementations of the {@link Node} interface which are memory-optimized for
 * storing only the given variables, potentially further optimized based on variable values.
 *
 * @author Niall Gallagher
 */
public interface NodeFactory {

    /**
     * Returns a new {@link Node} object which encapsulates the arguments supplied, optionally returning implementations
     * of the {@link Node} interface which are memory-optimized for storing only the supplied combination of variables,
     * potentially further optimized based on variable values.
     *
     * @param edgeCharacters Provides edge characters to be stored in the node. This is never null. In the case of
     * (re-)constructing the root node, this will contain zero characters, otherwise will always contain one or more
     * characters
     *
     * @param value An arbitrary object to associate with the node. This can be null, but it will not be null if
     * dealing with a leaf node (when childNodes will be empty)
     *
     * @param childNodes A list of child nodes to store in the node. This will never be null, but may be empty when
     * building a leaf node
     *
     * @param isRoot Indicates if this will be the root node, in which case edge characters will be non-null but empty,
     * value will be null, and child nodes will be non-null but may be empty
     *
     * @return An object implementing the {@link Node} interface which stores the given variables
     */
    Node createNode(CharSequence edgeCharacters, Object value, List<Node> childNodes, boolean isRoot);

}
