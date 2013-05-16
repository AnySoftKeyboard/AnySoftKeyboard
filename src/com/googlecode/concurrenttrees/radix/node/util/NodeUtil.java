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
package com.googlecode.concurrenttrees.radix.node.util;

import com.googlecode.concurrenttrees.radix.node.Node;

import java.util.*;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * Static utility methods useful when implementing {@link com.googlecode.concurrenttrees.radix.node.Node}s.
 *
 * @author Niall Gallagher
 */
public class NodeUtil {

    /**
     * Private constructor, not used.
     */
    NodeUtil() {
    }

    private static final Comparator<NodeCharacterProvider> NODE_COMPARATOR = new NodeCharacterComparator();

    /**
     * Returns the index of the node in the given {@link AtomicReferenceArray} whose edge starts with the given
     * first character.
     * <p/>
     * This method expects that some constraints are enforced on the {@link AtomicReferenceArray}:
     * <ul>
     *     <li>
     *         The array must already be in ascending sorted order of the first character of the edge for each node
     *     </li>
     *     <li>
     *         No entries in the array can be null
     *     </li>
     *     <li>
     *         Any existing node in the array cannot be swapped concurrently for another unless the edge associated
     *         with the other node also starts with the same first character
     *     </li>
     * </ul>
     * If these constraints are enforced as expected, then this method will have deterministic behaviour even in the
     * face of concurrent modification.
     *
     * @param childNodes An {@link AtomicReferenceArray} of {@link com.googlecode.concurrenttrees.radix.node.Node} objects, which is used in accordance with
     * the constraints documented in this method
     *
     * @param edgeFirstCharacter The first character of the edge for which the associated node is required
     * @return The index of the node representing the indicated edge, or a value < 0 if no such node exists in the
     * array
     */
    public static int binarySearchForEdge(AtomicReferenceArray<Node> childNodes, Character edgeFirstCharacter) {
        List<? extends NodeCharacterProvider> childNodesList = new AtomicReferenceArrayListAdapter<Node>(childNodes);
        NodeCharacterProvider searchKey = new NodeCharacterKey(edgeFirstCharacter);
        return Collections.binarySearch(childNodesList, searchKey, NODE_COMPARATOR);
    }

    /**
     * Throws an exception if any nodes in the given list represent edges having the same first character.
     *
     * @param nodes The list of nodes to validate
     * @throws IllegalStateException If a duplicate edge is detected
     */
    public static void ensureNoDuplicateEdges(List<Node> nodes) {
        // Sanity check that no two nodes specify an edge with the same first character...
        Set<Character> uniqueChars = new HashSet<Character>(nodes.size());
        for (Node node : nodes) {
            uniqueChars.add(node.getIncomingEdgeFirstCharacter());
        }
        if (nodes.size() != uniqueChars.size()) {
            throw new IllegalStateException("Duplicate edge detected in list of nodes supplied: " + nodes);
        }
    }
}
