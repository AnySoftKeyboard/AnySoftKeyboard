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
package com.googlecode.concurrenttrees.solver;

import com.googlecode.concurrenttrees.common.CharSequences;
import com.googlecode.concurrenttrees.radix.ConcurrentRadixTree;
import com.googlecode.concurrenttrees.radix.node.Node;
import com.googlecode.concurrenttrees.radix.node.NodeFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Finds the longest common substring in a collection of documents.
 * See <a href="http://en.wikipedia.org/wiki/Longest_common_substring_problem">Longest common substring problem</a>.
 * <p/>
 * This class internally extends {@link ConcurrentRadixTree} and combines it with elements from
 * {@link com.googlecode.concurrenttrees.suffix.ConcurrentSuffixTree}, but implements its own traversal algorithm.
 *
 * @author Niall Gallagher
 */
public class LCSubstringSolver {

    class ConcurrentSuffixTreeImpl<V> extends ConcurrentRadixTree<V> {

        public ConcurrentSuffixTreeImpl(NodeFactory nodeFactory) {
            super(nodeFactory);
        }

        public ConcurrentSuffixTreeImpl(NodeFactory nodeFactory, boolean restrictConcurrency) {
            super(nodeFactory, restrictConcurrency);
        }

        @Override
        protected void acquireWriteLock() {
            super.acquireWriteLock();
        }

        @Override
        protected void releaseWriteLock() {
            super.releaseWriteLock();
        }

        // Override to make accessible to outer class...
        @Override
        protected Iterable<NodeKeyPair> lazyTraverseDescendants(CharSequence startKey, Node startNode) {
            return super.lazyTraverseDescendants(startKey, startNode);
        }

        /**
         * The main algorithm to find the longest common substring.
         * <ol>
         *     <li>
         *         Traverses all nodes in the suffix tree
         *     </li>
         *     <li>
         *         For each node checks if the path from the root via edges to that node is
         *         longer than the longest common substring encountered so far (and so is a candidate)
         *     </li>
         *     <li>
         *         Calls helper method {@link #subTreeReferencesAllOriginalDocuments(CharSequence, Node)},
         *         supplying the candidate node. That method returns true if nodes in the sub-tree descending from
         *         that node collectively references all of the original documents added to the solver
         *     </li>
         *     <li>
         *         If the nodes in the sub-tree do collectively reference all documents, then
         *         the path from root to the current node is a substring of all documents
         *     </li>
         *     <li>
         *         Updates the longest common substring encountered so far if the conditions above hold for the
         *         current node
         *     </li>
         *     <li>
         *         Continues traversing the tree until all nodes have been checked
         *     </li>
         * </ol>
         * Implementation note: Method {@link #subTreeReferencesAllOriginalDocuments(CharSequence, Node)} will
         * stop traversal early if it finds all original documents early. This method currently does not apply a similar
         * optimization, and will actually descend into and apply the same tests to branches which the helper method
         * already indicated are dead-ends(!). Future work might be to use this knowledge, skip dead-end branches, but
         * it would involve not using any of the traversal logic from superclasses and overriding it all here for this
         * one use case.
         *
         * @return The longest common substring
         */
        CharSequence getLongestCommonSubstring() {
            Node root = suffixTree.getNode();
            final CharSequence[] longestCommonSubstringSoFar = new CharSequence[] {""};
            final int[] longestCommonSubstringSoFarLength = new int[] {0};

            for (NodeKeyPair nodeKeyPair : lazyTraverseDescendants("", root)) {
                if (nodeKeyPair.key.length() > longestCommonSubstringSoFarLength[0]
                    && subTreeReferencesAllOriginalDocuments(nodeKeyPair.key, nodeKeyPair.node)) {
                    longestCommonSubstringSoFarLength[0] = nodeKeyPair.key.length();
                    longestCommonSubstringSoFar[0] = nodeKeyPair.key;
                }
            }
            return longestCommonSubstringSoFar[0];
        }

        /**
         * Returns true if the given node and its descendants collectively reference all original documents added to
         * the solver.
         * <p/>
         * This method will traverse the entire sub-tree until it has encountered all of the original documents. If
         * it encounters all of the original documents early, before exhausting all nodes, returns early.
         *
         * @param startKey The key associated with the start node (concatenation of edges from root leading to it)
         * @param startNode The root of the sub-tree to traverse
         * @return True if the given node and its descendants collectively reference all original documents added to
         * the solver, false if the sub-tree does not reference all documents added to the solver
         */
        boolean subTreeReferencesAllOriginalDocuments(CharSequence startKey, Node startNode) {
            final Set<String> documentsEncounteredSoFar = new HashSet<String>(originalDocuments.size());
            final boolean[] result = new boolean[] {false};

            for (NodeKeyPair nodeKeyPair : lazyTraverseDescendants(startKey, startNode)) {
                @SuppressWarnings({"unchecked"})
                Set<String> documentsReferencedByThisNode = (Set<String>) nodeKeyPair.node.getValue();
                if (documentsReferencedByThisNode != null) {
                    documentsEncounteredSoFar.addAll(documentsReferencedByThisNode);
                    if (documentsEncounteredSoFar.equals(originalDocuments)) {
                        // We have now found all of the original documents
                        // referenced from descendants of the start node...
                        result[0] = true;
                        // Stop traversal...
                        break;
                    }
                }
            }
            return result[0];
        }
    }

    final ConcurrentSuffixTreeImpl<Set<String>> suffixTree;
    final Set<String> originalDocuments;

    /**
     * Creates a new {@link LCSubstringSolver} which will use the given {@link NodeFactory} to create nodes.
     *
     * @param nodeFactory An object which creates {@link com.googlecode.concurrenttrees.radix.node.Node} objects
     * on-demand, and which might return node implementations optimized for storing the values supplied to it for
     * the creation of each node
     */
    public LCSubstringSolver(NodeFactory nodeFactory) {
        this.suffixTree = new ConcurrentSuffixTreeImpl<Set<String>>(nodeFactory);
        this.originalDocuments = createSetForOriginalKeys();
    }

    /**
     * Creates a new {@link LCSubstringSolver} which will use the given {@link NodeFactory} to create nodes.
     *
     * @param nodeFactory An object which creates {@link com.googlecode.concurrenttrees.radix.node.Node} objects
     * on-demand, and which might return node implementations optimized for storing the values supplied to it for the
     * creation of each node
     * @param restrictConcurrency If true, configures use of a {@link java.util.concurrent.locks.ReadWriteLock} allowing
     * concurrent reads, except when writes are being performed by other threads, in which case writes block all reads;
     * if false, configures lock-free reads; allows concurrent non-blocking reads, even if writes are being performed
     * by other threads
     */
    public LCSubstringSolver(NodeFactory nodeFactory, boolean restrictConcurrency) {
        this.suffixTree = new ConcurrentSuffixTreeImpl<Set<String>>(nodeFactory, restrictConcurrency);
        this.originalDocuments = createSetForOriginalKeys();
    }

    /**
     * Adds a {@link CharSequence} document to the solver.
     *
     * @param document The {@link CharSequence} to add to the solver
     * @return True if the document was added, false if it was not because it had been added previously
     */
    public boolean add(CharSequence document) {
        if (document == null) {
            throw new IllegalArgumentException("The document argument was null");
        }
        if (document.length() == 0) {
            throw new IllegalArgumentException("The document argument was zero-length");
        }
        suffixTree.acquireWriteLock();
        try {
            // We convert to string (for now) due to lack of equals() and hashCode() support in CharSequence...
            String documentString = CharSequences.toString(document);

            // Put/replace value in set before we add suffixes to the tree...
            boolean addedNew = originalDocuments.add(documentString);
            if (!addedNew) {
                // Key was not added as was already contained, no need to do anything, return false...
                return false;
            }
            // Kew was added to set, now add to tree...
            addSuffixesToRadixTree(documentString);
            return true;
        }
        finally {
            suffixTree.releaseWriteLock();
        }
    }

    void addSuffixesToRadixTree(String keyAsString) {
        Iterable<CharSequence> suffixes = CharSequences.generateSuffixes(keyAsString);
        for (CharSequence suffix : suffixes) {
            Set<String> originalKeyRefs = suffixTree.getValueForExactKey(suffix);
            if (originalKeyRefs == null) {
                originalKeyRefs = createSetForOriginalKeys();
                suffixTree.put(suffix, originalKeyRefs);
            }
            originalKeyRefs.add(keyAsString);
        }
    }

    /**
     * Finds the longest common substring in the documents added to the solver so far.
     *
     * @return The longest common substring
     */
    public CharSequence getLongestCommonSubstring() {
        return suffixTree.getLongestCommonSubstring();
    }

    protected Set<String> createSetForOriginalKeys() {
        return Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
    }
}
