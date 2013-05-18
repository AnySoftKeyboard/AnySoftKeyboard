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
package com.googlecode.concurrenttrees.radix;

import com.googlecode.concurrenttrees.common.KeyValuePair;
import com.googlecode.concurrenttrees.common.LazyIterator;
import com.googlecode.concurrenttrees.radix.node.Node;
import com.googlecode.concurrenttrees.radix.node.NodeFactory;
import com.googlecode.concurrenttrees.common.CharSequences;
import com.googlecode.concurrenttrees.radix.node.util.PrettyPrintable;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import static com.googlecode.concurrenttrees.radix.ConcurrentRadixTree.SearchResult.Classification;

/**
 * An implementation of {@link RadixTree} which supports lock-free concurrent reads, and allows items to be added to and
 * to be removed from the tree <i>atomically</i> by background thread(s), without blocking reads.
 * <p/>
 * Unlike reads, writes require locking of the tree (locking out other writing threads only; reading threads are never
 * blocked). Currently write locks are coarse-grained; in fact they are tree-level. In future branch-level write locks
 * might be added, but the current implementation is targeted at high concurrency read-mostly use cases.
 *
 * @author Niall Gallagher
 */
public class ConcurrentRadixTree<O> implements RadixTree<O>, PrettyPrintable,  Serializable  {
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 4169547300692751673L;

	private final NodeFactory nodeFactory;

    protected volatile Node root;

    // Write operations acquire write lock.
    // Read operations are lock-free by default, but can be forced to acquire read locks via constructor flag...
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    // If true, force reading threads to acquire read lock (they will block on writes).
    private final boolean restrictConcurrency;

    /**
     * Creates a new {@link ConcurrentRadixTree} which will use the given {@link NodeFactory} to create nodes.
     * 
     * @param nodeFactory An object which creates {@link Node} objects on-demand, and which might return node 
     * implementations optimized for storing the values supplied to it for the creation of each node
     */
    public ConcurrentRadixTree(NodeFactory nodeFactory) {
        this(nodeFactory, false);
    }

    /**
     * Creates a new {@link ConcurrentRadixTree} which will use the given {@link NodeFactory} to create nodes.
     * 
     * @param nodeFactory An object which creates {@link Node} objects on-demand, and which might return node 
     * implementations optimized for storing the values supplied to it for the creation of each node
     * @param restrictConcurrency If true, configures use of a {@link java.util.concurrent.locks.ReadWriteLock} allowing
     * concurrent reads, except when writes are being performed by other threads, in which case writes block all reads;
     * if false, configures lock-free reads; allows concurrent non-blocking reads, even if writes are being performed
     * by other threads
     */
    public ConcurrentRadixTree(NodeFactory nodeFactory, boolean restrictConcurrency) {
        this.nodeFactory = nodeFactory;
        this.restrictConcurrency = restrictConcurrency;
        @SuppressWarnings({"NullableProblems", "UnnecessaryLocalVariable"})
        Node rootNode = nodeFactory.createNode("", null, Collections.<Node>emptyList(), true);
        this.root = rootNode;
    }

    // ------------- Helper methods for serializing writes -------------

    protected void acquireWriteLock() {
        //readWriteLock.writeLock().lock();
    }

    protected void releaseWriteLock() {
       // readWriteLock.writeLock().unlock();
    }

    // Temporary helper methods for read locks, read locks will be removed in future...

    protected void acquireReadLockIfNecessary() {
        if (restrictConcurrency) { // restrictConcurrency is final
           // readWriteLock.readLock().lock();
        }
    }

    protected void releaseReadLockIfNecessary() {
        if (restrictConcurrency) {  // restrictConcurrency is final
            //readWriteLock.readLock().unlock();
        }
    }

    // ------------- Public API methods -------------


    /**
     * {@inheritDoc}
     */
    @Override
    public O put(CharSequence key, O value) {
        @SuppressWarnings({"unchecked", "UnnecessaryLocalVariable"})
        O existingValue = (O) putInternal(key, value, true);  // putInternal acquires write lock
        return existingValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public O putIfAbsent(CharSequence key, O value) {
        @SuppressWarnings({"unchecked", "UnnecessaryLocalVariable"})
        O existingValue = (O) putInternal(key, value, false); // putInternal acquires write lock
        return existingValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public O getValueForExactKey(CharSequence key) {
        acquireReadLockIfNecessary();
        try {
            SearchResult searchResult = searchTree(key);
            if (searchResult.classification.equals(SearchResult.Classification.EXACT_MATCH)) {
                @SuppressWarnings({"unchecked", "UnnecessaryLocalVariable"})
                O value = (O) searchResult.nodeFound.getValue();
                return value;
            }
            return null;
        }
        finally {
            releaseReadLockIfNecessary();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<CharSequence> getKeysStartingWith(CharSequence prefix) {
        acquireReadLockIfNecessary();
        try {
            SearchResult searchResult = searchTree(prefix);
            Classification classification = searchResult.classification;
            switch (classification) {
                case EXACT_MATCH: {
                    return getDescendantKeys(prefix, searchResult.nodeFound);
                }
                case KEY_ENDS_MID_EDGE: {
                    // Append the remaining characters of the edge to the key.
                    // For example if we searched for CO, but first matching node was COFFEE,
                    // the key associated with the first node should be COFFEE...
                    CharSequence edgeSuffix = CharSequences.getSuffix(searchResult.nodeFound.getIncomingEdge(), searchResult.charsMatchedInNodeFound);
                    prefix = CharSequences.concatenate(prefix, edgeSuffix);
                    return getDescendantKeys(prefix, searchResult.nodeFound);
                }
                default: {
                    // Incomplete match means key is not a prefix of any node...
                    return Collections.emptySet();
                }
            }
        }
        finally {
            releaseReadLockIfNecessary();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<O> getValuesForKeysStartingWith(CharSequence prefix) {
        acquireReadLockIfNecessary();
        try {
            SearchResult searchResult = searchTree(prefix);
            Classification classification = searchResult.classification;
            switch (classification) {
                case EXACT_MATCH: {
                    return getDescendantValues(prefix, searchResult.nodeFound);
                }
                case KEY_ENDS_MID_EDGE: {
                    // Append the remaining characters of the edge to the key.
                    // For example if we searched for CO, but first matching node was COFFEE,
                    // the key associated with the first node should be COFFEE...
                    CharSequence edgeSuffix = CharSequences.getSuffix(searchResult.nodeFound.getIncomingEdge(), searchResult.charsMatchedInNodeFound);
                    prefix = CharSequences.concatenate(prefix, edgeSuffix);
                    return getDescendantValues(prefix, searchResult.nodeFound);
                }
                default: {
                    // Incomplete match means key is not a prefix of any node...
                    return Collections.emptySet();
                }
            }
        }
        finally {
            releaseReadLockIfNecessary();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<KeyValuePair<O>> getKeyValuePairsForKeysStartingWith(CharSequence prefix) {
        acquireReadLockIfNecessary();
        try {
            SearchResult searchResult = searchTree(prefix);
            Classification classification = searchResult.classification;
            switch (classification) {
                case EXACT_MATCH: {
                    return getDescendantKeyValuePairs(prefix, searchResult.nodeFound);
                }
                case KEY_ENDS_MID_EDGE: {
                    // Append the remaining characters of the edge to the key.
                    // For example if we searched for CO, but first matching node was COFFEE,
                    // the key associated with the first node should be COFFEE...
                    CharSequence edgeSuffix = CharSequences.getSuffix(searchResult.nodeFound.getIncomingEdge(), searchResult.charsMatchedInNodeFound);
                    prefix = CharSequences.concatenate(prefix, edgeSuffix);
                    return getDescendantKeyValuePairs(prefix, searchResult.nodeFound);
                }
                default: {
                    // Incomplete match means key is not a prefix of any node...
                    return Collections.emptySet();
                }
            }
        }
        finally {
            releaseReadLockIfNecessary();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean remove(CharSequence key) {
        if (key == null) {
            throw new IllegalArgumentException("The key argument was null");
        }
        acquireWriteLock();
        try {
            SearchResult searchResult = searchTree(key);
            SearchResult.Classification classification = searchResult.classification;
            switch (classification) {
                case EXACT_MATCH: {
                    if (searchResult.nodeFound.getValue() == null) {
                        // This node was created automatically as a split between two branches (implicit node).
                        // No need to remove it...
                        return false;
                    }

                    // Proceed with deleting the node...
                    List<Node> childEdges = searchResult.nodeFound.getOutgoingEdges();
                    if (childEdges.size() > 1) {
                        // This node has more than one child, so if we delete the value from this node, we still need
                        // to leave a similar node in place to act as the split between the child edges.
                        // Just delete the value associated with this node.
                        // -> Clone this node without its value, preserving its child nodes...
                        @SuppressWarnings({"NullableProblems"})
                        Node cloned = nodeFactory.createNode(searchResult.nodeFound.getIncomingEdge(), null, searchResult.nodeFound.getOutgoingEdges(), false);
                        // Re-add the replacement node to the parent...
                        searchResult.parentNode.updateOutgoingEdge(cloned);
                    }
                    else if (childEdges.size() == 1) {
                        // Node has one child edge.
                        // Create a new node which is the concatenation of the edges from this node and its child,
                        // and which has the outgoing edges of the child and the value from the child.
                        Node child = childEdges.get(0);
                        CharSequence concatenatedEdges = CharSequences.concatenate(searchResult.nodeFound.getIncomingEdge(), child.getIncomingEdge());
                        Node mergedNode = nodeFactory.createNode(concatenatedEdges, child.getValue(), child.getOutgoingEdges(), false);
                        // Re-add the merged node to the parent...
                        searchResult.parentNode.updateOutgoingEdge(mergedNode);
                    }
                    else {
                        // Node has no children. Delete this node from its parent,
                        // which involves re-creating the parent rather than simply updating its child edge
                        // (this is why we need parentNodesParent).
                        // However if this would leave the parent with only one remaining child edge,
                        // and the parent itself has no value (is a split node), and the parent is not the root node
                        // (a special case which we never merge), then we also need to merge the parent with its
                        // remaining child.

                        List<Node> currentEdgesFromParent = searchResult.parentNode.getOutgoingEdges();
                        // Create a list of the outgoing edges of the parent which will remain
                        // if we remove this child...
                        // Use a non-resizable list, as a sanity check to force ArrayIndexOutOfBounds...
                        List<Node> newEdgesOfParent = Arrays.asList(new Node[searchResult.parentNode.getOutgoingEdges().size() - 1]);
                        for (int i = 0, added = 0, numParentEdges = currentEdgesFromParent.size(); i < numParentEdges; i++) {
                            Node node = currentEdgesFromParent.get(i);
                            if (node != searchResult.nodeFound) {
                                newEdgesOfParent.set(added++, node);
                            }
                        }

                        // Note the parent might actually be the root node (which we should never merge)...
                        boolean parentIsRoot = (searchResult.parentNode == root);
                        Node newParent;
                        if (newEdgesOfParent.size() == 1 && searchResult.parentNode.getValue() == null && !parentIsRoot) {
                            // Parent is a non-root split node with only one remaining child, which can now be merged.
                            Node parentsRemainingChild = newEdgesOfParent.get(0);
                            // Merge the parent with its only remaining child...
                            CharSequence concatenatedEdges = CharSequences.concatenate(searchResult.parentNode.getIncomingEdge(), parentsRemainingChild.getIncomingEdge());
                            newParent = nodeFactory.createNode(concatenatedEdges, parentsRemainingChild.getValue(), parentsRemainingChild.getOutgoingEdges(), parentIsRoot);
                        }
                        else {
                            // Parent is a node which either has a value of its own, has more than one remaining
                            // child, or is actually the root node (we never merge the root node).
                            // Create new parent node which is the same as is currently just without the edge to the
                            // node being deleted...
                            newParent = nodeFactory.createNode(searchResult.parentNode.getIncomingEdge(), searchResult.parentNode.getValue(), newEdgesOfParent, parentIsRoot);
                        }
                        // Re-add the parent node to its parent...
                        if (parentIsRoot) {
                            // Replace the root node...
                            this.root = newParent;
                        }
                        else {
                            // Re-add the parent node to its parent...
                            searchResult.parentNodesParent.updateOutgoingEdge(newParent);
                        }
                    }
                    return true;
                }
                default: {
                    return false;
                }
            }
        }
        finally {
            releaseWriteLock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<CharSequence> getClosestKeys(CharSequence candidate) {
        acquireReadLockIfNecessary();
        try {
            SearchResult searchResult = searchTree(candidate);
            Classification classification = searchResult.classification;
            switch (classification) {
                case EXACT_MATCH: {
                    return getDescendantKeys(candidate, searchResult.nodeFound);
                }
                case KEY_ENDS_MID_EDGE: {
                    // Append the remaining characters of the edge to the key.
                    // For example if we searched for CO, but first matching node was COFFEE,
                    // the key associated with the first node should be COFFEE...
                    CharSequence edgeSuffix = CharSequences.getSuffix(searchResult.nodeFound.getIncomingEdge(), searchResult.charsMatchedInNodeFound);
                    candidate = CharSequences.concatenate(candidate, edgeSuffix);
                    return getDescendantKeys(candidate, searchResult.nodeFound);
                }
                case INCOMPLETE_MATCH_TO_MIDDLE_OF_EDGE: {
                    // Example: if we searched for CX, but deepest matching node was CO,
                    // the results should include node CO and its descendants...
                    CharSequence keyOfParentNode = CharSequences.getPrefix(candidate, searchResult.charsMatched - searchResult.charsMatchedInNodeFound);
                    CharSequence keyOfNodeFound = CharSequences.concatenate(keyOfParentNode, searchResult.nodeFound.getIncomingEdge());
                    return getDescendantKeys(keyOfNodeFound, searchResult.nodeFound);
                }
                case INCOMPLETE_MATCH_TO_END_OF_EDGE: {
                    if (searchResult.charsMatched == 0) {
                        // Closest match is the root node, we don't consider this a match for anything...
                        break;
                    }
                    // Example: if we searched for COFFEE, but deepest matching node was CO,
                    // the results should include node CO and its descendants...
                    CharSequence keyOfNodeFound = CharSequences.getPrefix(candidate, searchResult.charsMatched);
                    return getDescendantKeys(keyOfNodeFound, searchResult.nodeFound);
                }
            }
            return Collections.emptySet();
        }
        finally {
            releaseReadLockIfNecessary();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<O> getValuesForClosestKeys(CharSequence candidate) {
        acquireReadLockIfNecessary();
        try {
            SearchResult searchResult = searchTree(candidate);
            Classification classification = searchResult.classification;
            switch (classification) {
                case EXACT_MATCH: {
                    return getDescendantValues(candidate, searchResult.nodeFound);
                }
                case KEY_ENDS_MID_EDGE: {
                    // Append the remaining characters of the edge to the key.
                    // For example if we searched for CO, but first matching node was COFFEE,
                    // the key associated with the first node should be COFFEE...
                    CharSequence edgeSuffix = CharSequences.getSuffix(searchResult.nodeFound.getIncomingEdge(), searchResult.charsMatchedInNodeFound);
                    candidate = CharSequences.concatenate(candidate, edgeSuffix);
                    return getDescendantValues(candidate, searchResult.nodeFound);
                }
                case INCOMPLETE_MATCH_TO_MIDDLE_OF_EDGE: {
                    // Example: if we searched for CX, but deepest matching node was CO,
                    // the results should include node CO and its descendants...
                    CharSequence keyOfParentNode = CharSequences.getPrefix(candidate, searchResult.charsMatched - searchResult.charsMatchedInNodeFound);
                    CharSequence keyOfNodeFound = CharSequences.concatenate(keyOfParentNode, searchResult.nodeFound.getIncomingEdge());
                    return getDescendantValues(keyOfNodeFound, searchResult.nodeFound);
                }
                case INCOMPLETE_MATCH_TO_END_OF_EDGE: {
                    if (searchResult.charsMatched == 0) {
                        // Closest match is the root node, we don't consider this a match for anything...
                        break;
                    }
                    // Example: if we searched for COFFEE, but deepest matching node was CO,
                    // the results should include node CO and its descendants...
                    CharSequence keyOfNodeFound = CharSequences.getPrefix(candidate, searchResult.charsMatched);
                    return getDescendantValues(keyOfNodeFound, searchResult.nodeFound);
                }
            }
            return Collections.emptySet();
        }
        finally {
            releaseReadLockIfNecessary();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<KeyValuePair<O>> getKeyValuePairsForClosestKeys(CharSequence candidate) {
        acquireReadLockIfNecessary();
        try {
            SearchResult searchResult = searchTree(candidate);
            Classification classification = searchResult.classification;
            switch (classification) {
                case EXACT_MATCH: {
                    return getDescendantKeyValuePairs(candidate, searchResult.nodeFound);
                }
                case KEY_ENDS_MID_EDGE: {
                    // Append the remaining characters of the edge to the key.
                    // For example if we searched for CO, but first matching node was COFFEE,
                    // the key associated with the first node should be COFFEE...
                    CharSequence edgeSuffix = CharSequences.getSuffix(searchResult.nodeFound.getIncomingEdge(), searchResult.charsMatchedInNodeFound);
                    candidate = CharSequences.concatenate(candidate, edgeSuffix);
                    return getDescendantKeyValuePairs(candidate, searchResult.nodeFound);
                }
                case INCOMPLETE_MATCH_TO_MIDDLE_OF_EDGE: {
                    // Example: if we searched for CX, but deepest matching node was CO,
                    // the results should include node CO and its descendants...
                    CharSequence keyOfParentNode = CharSequences.getPrefix(candidate, searchResult.charsMatched - searchResult.charsMatchedInNodeFound);
                    CharSequence keyOfNodeFound = CharSequences.concatenate(keyOfParentNode, searchResult.nodeFound.getIncomingEdge());
                    return getDescendantKeyValuePairs(keyOfNodeFound, searchResult.nodeFound);
                }
                case INCOMPLETE_MATCH_TO_END_OF_EDGE: {
                    if (searchResult.charsMatched == 0) {
                        // Closest match is the root node, we don't consider this a match for anything...
                        break;
                    }
                    // Example: if we searched for COFFEE, but deepest matching node was CO,
                    // the results should include node CO and its descendants...
                    CharSequence keyOfNodeFound = CharSequences.getPrefix(candidate, searchResult.charsMatched);
                    return getDescendantKeyValuePairs(keyOfNodeFound, searchResult.nodeFound);
                }
            }
            return Collections.emptySet();
        }
        finally {
            releaseReadLockIfNecessary();
        }
    }

    // ------------- Helper method for put() -------------

    /**
     * Atomically adds the given value to the tree, creating a node for the value as necessary. If the value is already
     * stored for the same key, either overwrites the existing value, or simply returns the existing value, depending
     * on the given value of the <code>overwrite</code> flag.
     *
     * @param key The key against which the value should be stored
     * @param value The value to store against the key
     * @param overwrite If true, should replace any existing value, if false should not replace any existing value
     * @return The existing value for this key, if there was one, otherwise null
     */
    Object putInternal(CharSequence key, Object value, boolean overwrite) {
        if (key == null) {
            throw new IllegalArgumentException("The key argument was null");
        }
        if (key.length() == 0) {
            throw new IllegalArgumentException("The key argument was zero-length");
        }
        if (value == null) {
            throw new IllegalArgumentException("The value argument was null");
        }
        acquireWriteLock();
        try {
            // Note we search the tree here after we have acquired the write lock...
            SearchResult searchResult = searchTree(key);
            SearchResult.Classification classification = searchResult.classification;

            switch (classification) {
                case EXACT_MATCH: {
                    // Search found an exact match for all edges leading to this node.
                    // -> Add or update the value in the node found, by replacing
                    // the existing node with a new node containing the value...

                    // First check if existing node has a value, and if we are allowed to overwrite it.
                    // Return early without overwriting if necessary...
                    Object existingValue = searchResult.nodeFound.getValue();
                    if (!overwrite && existingValue != null) {
                        return existingValue;
                    }
                    // Create a replacement for the existing node containing the new value...
                    Node replacementNode = nodeFactory.createNode(searchResult.nodeFound.getIncomingEdge(), value, searchResult.nodeFound.getOutgoingEdges(), false);
                    searchResult.parentNode.updateOutgoingEdge(replacementNode);
                    // Return the existing value...
                    return existingValue;
                }
                case KEY_ENDS_MID_EDGE: {
                    // Search ran out of characters from the key while in the middle of an edge in the node.
                    // -> Split the node in two: Create a new parent node storing the new value,
                    // and a new child node holding the original value and edges from the existing node...
                    CharSequence keyCharsFromStartOfNodeFound = key.subSequence(searchResult.charsMatched - searchResult.charsMatchedInNodeFound, key.length());
                    CharSequence commonPrefix = CharSequences.getCommonPrefix(keyCharsFromStartOfNodeFound, searchResult.nodeFound.getIncomingEdge());
                    CharSequence suffixFromExistingEdge = CharSequences.subtractPrefix(searchResult.nodeFound.getIncomingEdge(), commonPrefix);

                    // Create new nodes...
                    Node newChild = nodeFactory.createNode(suffixFromExistingEdge, searchResult.nodeFound.getValue(), searchResult.nodeFound.getOutgoingEdges(), false);
                    Node newParent = nodeFactory.createNode(commonPrefix, value, Arrays.asList(newChild), false);

                    // Add the new parent to the parent of the node being replaced (replacing the existing node)...
                    searchResult.parentNode.updateOutgoingEdge(newParent);

                    // Return null for the existing value...
                    return null;
                }
                case INCOMPLETE_MATCH_TO_END_OF_EDGE: {
                    // Search found a difference in characters between the key and the start of all child edges leaving the
                    // node, the key still has trailing unmatched characters.
                    // -> Add a new child to the node, containing the trailing characters from the key.

                    // NOTE: this is the only branch which allows an edge to be added to the root.
                    // (Root node's own edge is "" empty string, so is considered a prefixing edge of every key)

                    // Create a new child node containing the trailing characters...
                    CharSequence keySuffix = key.subSequence(searchResult.charsMatched, key.length());
                    Node newChild = nodeFactory.createNode(keySuffix, value, Collections.<Node>emptyList(), false);

                    // Clone the current node adding the new child...
                    List<Node> edges = new ArrayList<Node>(searchResult.nodeFound.getOutgoingEdges().size() + 1);
                    edges.addAll(searchResult.nodeFound.getOutgoingEdges());
                    edges.add(newChild);
                    Node clonedNode = nodeFactory.createNode(searchResult.nodeFound.getIncomingEdge(), searchResult.nodeFound.getValue(), edges, searchResult.nodeFound == root);

                    // Re-add the cloned node to its parent node...
                    if (searchResult.nodeFound == root) {
                        this.root = clonedNode;
                    }
                    else {
                        searchResult.parentNode.updateOutgoingEdge(clonedNode);
                    }

                    // Return null for the existing value...
                    return null;
                }
                case INCOMPLETE_MATCH_TO_MIDDLE_OF_EDGE: {
                    // Search found a difference in characters between the key and the characters in the middle of the
                    // edge in the current node, and the key still has trailing unmatched characters.
                    // -> Split the node in three:
                    // Let's call node found: NF
                    // (1) Create a new node N1 containing the unmatched characters from the rest of the key, and the
                    // value supplied to this method
                    // (2) Create a new node N2 containing the unmatched characters from the rest of the edge in NF, and
                    // copy the original edges and the value from NF unmodified into N2
                    // (3) Create a new node N3, which will be the split node, containing the matched characters from
                    // the key and the edge, and add N1 and N2 as child nodes of N3
                    // (4) Re-add N3 to the parent node of NF, effectively replacing NF in the tree

                    CharSequence keyCharsFromStartOfNodeFound = key.subSequence(searchResult.charsMatched - searchResult.charsMatchedInNodeFound, key.length());
                    CharSequence commonPrefix = CharSequences.getCommonPrefix(keyCharsFromStartOfNodeFound, searchResult.nodeFound.getIncomingEdge());
                    CharSequence suffixFromExistingEdge = CharSequences.subtractPrefix(searchResult.nodeFound.getIncomingEdge(), commonPrefix);
                    CharSequence suffixFromKey = key.subSequence(searchResult.charsMatched, key.length());

                    // Create new nodes...
                    Node n1 = nodeFactory.createNode(suffixFromKey, value, Collections.<Node>emptyList(), false);
                    Node n2 = nodeFactory.createNode(suffixFromExistingEdge, searchResult.nodeFound.getValue(), searchResult.nodeFound.getOutgoingEdges(), false);
                    @SuppressWarnings({"NullableProblems"})
                    Node n3 = nodeFactory.createNode(commonPrefix, null, Arrays.asList(n1, n2), false);

                    searchResult.parentNode.updateOutgoingEdge(n3);

                    // Return null for the existing value...
                    return null;
                }
                default: {
                    // This is a safeguard against a new enum constant being added in future.
                    throw new IllegalStateException("Unexpected classification for search result: " + searchResult);
                }
            }
        }
        finally {
            releaseWriteLock();
        }
    }

    // ------------- Helper method for finding descendants of a given node -------------

    /**
     * Returns a lazy iterable which will return {@link CharSequence} keys for which the given key is a prefix.
     * The results inherently will not contain duplicates (duplicate keys cannot exist in the tree).
     * <p/>
     * Note that this method internally converts {@link CharSequence}s to {@link String}s, to avoid set equality issues,
     * because equals() and hashCode() are not specified by the CharSequence API contract.
     */
    @SuppressWarnings({"JavaDoc"})
    Iterable<CharSequence> getDescendantKeys(final CharSequence startKey, final Node startNode) {
        return new Iterable<CharSequence> () {
            @Override
            public Iterator<CharSequence> iterator() {
                return new LazyIterator<CharSequence>() {
                    Iterator<NodeKeyPair> descendantNodes = lazyTraverseDescendants(startKey, startNode).iterator();

                    @Override
                    protected CharSequence computeNext() {
                        // Traverse to the next matching node in the tree and return its key and value...
                        while (descendantNodes.hasNext()) {
                            NodeKeyPair nodeKeyPair = descendantNodes.next();
                            Object value = nodeKeyPair.node.getValue();
                            if (value != null) {
                                // Dealing with a node explicitly added to tree (rather than an automatically-added split node).

                                // Call the transformKeyForResult method to allow key to be transformed before returning to client.
                                // Used by subclasses such as ReversedRadixTree implementations...
                                CharSequence optionallyTransformedKey = transformKeyForResult(nodeKeyPair.key);

                                // -> Convert the CharSequence to a String before returning, to avoid set equality issues,
                                // because equals() and hashCode() is not specified by the CharSequence API contract...
                                return CharSequences.toString(optionallyTransformedKey);
                            }
                        }
                        // Finished traversing the tree, no more matching nodes to return...
                        return endOfData();
                    }
                };
            }
        };
    }

    /**
     * Returns a lazy iterable which will return values which are associated with keys in the tree for which
     * the given key is a prefix.
     */
    @SuppressWarnings({"JavaDoc"})
    <O> Iterable<O> getDescendantValues(final CharSequence startKey, final Node startNode) {
        return new Iterable<O> () {
            @Override
            public Iterator<O> iterator() {
                return new LazyIterator<O>() {
                    Iterator<NodeKeyPair> descendantNodes = lazyTraverseDescendants(startKey, startNode).iterator();

                    @Override
                    protected O computeNext() {
                        // Traverse to the next matching node in the tree and return its key and value...
                        while (descendantNodes.hasNext()) {
                            NodeKeyPair nodeKeyPair = descendantNodes.next();
                            Object value = nodeKeyPair.node.getValue();
                            if (value != null) {
                                // Dealing with a node explicitly added to tree (rather than an automatically-added split node).

                                // We have to cast to generic type here, because Node objects are not generically typed.
                                // Background: Node objects are not generically typed, because arrays can't be generically typed,
                                // and we use arrays in nodes. We choose to cast here (in wrapper logic around the tree) rather than
                                // pollute the already-complex tree manipulation logic with casts.
                                @SuppressWarnings({"unchecked", "UnnecessaryLocalVariable"})
                                O valueTyped = (O)value;
                                return valueTyped;
                            }
                        }
                        // Finished traversing the tree, no more matching nodes to return...
                        return endOfData();
                    }
                };
            }
        };
    }

    /**
     * Returns a lazy iterable which will return {@link KeyValuePair} objects each containing a key and a value,
     * for which the given key is a prefix of the key in the {@link KeyValuePair}. These results inherently will not
     * contain duplicates (duplicate keys cannot exist in the tree).
     * <p/>
     * Note that this method internally converts {@link CharSequence}s to {@link String}s, to avoid set equality issues,
     * because equals() and hashCode() are not specified by the CharSequence API contract.
     */
    @SuppressWarnings({"JavaDoc"})
    <O> Iterable<KeyValuePair<O>> getDescendantKeyValuePairs(final CharSequence startKey, final Node startNode) {
        return new Iterable<KeyValuePair<O>> () {
            @Override
            public Iterator<KeyValuePair<O>> iterator() {
                return new LazyIterator<KeyValuePair<O>>() {
                    Iterator<NodeKeyPair> descendantNodes = lazyTraverseDescendants(startKey, startNode).iterator();

                    @Override
                    protected KeyValuePair<O> computeNext() {
                        // Traverse to the next matching node in the tree and return its key and value...
                        while (descendantNodes.hasNext()) {
                            NodeKeyPair nodeKeyPair = descendantNodes.next();
                            Object value = nodeKeyPair.node.getValue();
                            if (value != null) {
                                // Dealing with a node explicitly added to tree (rather than an automatically-added split node).

                                // Call the transformKeyForResult method to allow key to be transformed before returning to client.
                                // Used by subclasses such as ReversedRadixTree implementations...
                                CharSequence optionallyTransformedKey = transformKeyForResult(nodeKeyPair.key);

                                // -> Convert the CharSequence to a String before returning, to avoid set equality issues,
                                // because equals() and hashCode() is not specified by the CharSequence API contract...
                                String keyString = CharSequences.toString(optionallyTransformedKey);
                                return new KeyValuePairImpl<O>(keyString, value);
                            }
                        }
                        // Finished traversing the tree, no more matching nodes to return...
                        return endOfData();
                    }
                };
            }
        };
    }

    /**
     * Implementation of the {@link KeyValuePair} interface.
     */
    public static class KeyValuePairImpl<O> implements KeyValuePair<O> {

        final String key;
        final O value;

        /**
         * Constructor.
         *
         * Implementation node: This constructor currently requires the key to be supplied as a {@link String}
         * - this is to allow reliable testing of object equality; the alternative {@link CharSequence}
         * does not specify a contract for {@link Object#equals(Object)}.
         *
         * @param key The key as a string
         * @param value The value
         */
        public KeyValuePairImpl(String key, Object value) {
            this.key = key;
            // We have to cast to generic type here, because Node objects are not generically typed.
            // Background: Node objects are not generically typed, because arrays can't be generically typed,
            // and we use arrays in nodes. We choose to cast here (in wrapper logic around the tree) rather than
            // pollute the already-complex tree manipulation logic with casts.
            @SuppressWarnings({"unchecked", "UnnecessaryLocalVariable"})
            O valueTyped = (O)value;
            this.value = valueTyped;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public CharSequence getKey() {
            return key;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public O getValue() {
            return value;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            KeyValuePairImpl that = (KeyValuePairImpl) o;

            return key.equals(that.key);

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return key.hashCode();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "(" + key + ", " + value + ")";
        }
    }

    /**
     * Traverses the tree using depth-first, preordered traversal, starting at the given node, using lazy evaluation
     * such that the next node is only determined when next() is called on the iterator returned.
     * The traversal algorithm uses iteration instead of recursion to allow deep trees to be traversed without
     * requiring large JVM stack sizes.
     * <p/>
     * Each node that is encountered is returned from the iterator along with a key associated with that node,
     * in a NodeKeyPair object. The key will be prefixed by the given start key, and will be generated by appending
     * to the start key the edges traversed along the path to that node from the start node.
     *
     * @param startKey The key which matches the given start node
     * @param startNode The start node
     * @return An iterator which when iterated traverses the tree using depth-first, preordered traversal,
     * starting at the given start node
     */
    protected Iterable<NodeKeyPair> lazyTraverseDescendants(final CharSequence startKey, final Node startNode) {
        return new Iterable<NodeKeyPair>() {
            @Override
            public Iterator<NodeKeyPair> iterator() {
                return new LazyIterator<NodeKeyPair>() {

                    Deque<NodeKeyPair> stack = new LinkedList<NodeKeyPair>();
                    {
                        stack.push(new NodeKeyPair(startNode, startKey));
                    }

                    @Override
                    protected NodeKeyPair computeNext() {
                        if (stack.isEmpty()) {
                            return endOfData();
                        }
                        NodeKeyPair current = stack.pop();
                        List<Node> childNodes = current.node.getOutgoingEdges();

                        // -> Iterate child nodes in reverse order and so push them onto the stack in reverse order,
                        // to counteract that pushing them onto the stack alone would otherwise reverse their processing order.
                        // This ensures that we actually process nodes in ascending alphabetical order.
                        for (int i = childNodes.size(); i > 0; i--) {
                            Node child = childNodes.get(i - 1);
                            stack.push(new NodeKeyPair(child, CharSequences.concatenate(current.key, child.getIncomingEdge())));
                        }
                        return current;
                    }
                };
            }
        };
    }


    /**
     * Encapsulates a node and its associated key. Used internally by {@link #lazyTraverseDescendants}.
     */
    protected static class NodeKeyPair {
        public final Node node;
        public final CharSequence key;

        public NodeKeyPair(Node node, CharSequence key) {
            this.node = node;
            this.key = key;
        }
    }

    /**
     * A hook method which may be overridden by subclasses, to transform a key just before it is returned to
     * the application, for example by the {@link #getKeysStartingWith(CharSequence)} or the
     * {@link #getKeyValuePairsForKeysStartingWith(CharSequence)} methods.
     * <p/>
     * This hook is expected to be used by  {@link com.googlecode.concurrenttrees.radixreversed.ReversedRadixTree}
     * implementations, where keys are stored in the tree in reverse order but results should be returned in normal
     * order.
     * <p/>
     * <b>This default implementation simply returns the given key unmodified.</b>
     *
     * @param rawKey The raw key as stored in the tree
     * @return A transformed version of the key
     */
    protected CharSequence transformKeyForResult(CharSequence rawKey) {
        return rawKey;
    }


    // ------------- Helper method for searching the tree and associated SearchResult object -------------

    /**
     * Traverses the tree and finds the node which matches the longest prefix of the given key.
     * <p/>
     * The node returned might be an <u>exact match</u> for the key, in which case {@link SearchResult#charsMatched}
     * will equal the length of the key.
     * <p/>
     * The node returned might be an <u>inexact match</u> for the key, in which case {@link SearchResult#charsMatched}
     * will be less than the length of the key.
     * <p/>
     * There are two types of inexact match:
     * <ul>
     *     <li>
     *         An inexact match which ends evenly at the boundary between a node and its children (the rest of the key
     *         not matching any children at all). In this case if we we wanted to add nodes to the tree to represent the
     *         rest of the key, we could simply add child nodes to the node found.
     *     </li>
     *     <li>
     *         An inexact match which ends in the middle of a the characters for an edge stored in a node (the key
     *         matching only the first few characters of the edge). In this case if we we wanted to add nodes to the
     *         tree to represent the rest of the key, we would have to split the node (let's call this node found: NF):
     *         <ol>
     *             <li>
     *                 Create a new node (N1) which will be the split node, containing the matched characters from the
     *                 start of the edge in NF
     *             </li>
     *             <li>
     *                 Create a new node (N2) which will contain the unmatched characters from the rest of the edge
     *                 in NF, and copy the original edges from NF unmodified into N2
     *             </li>
     *             <li>
     *                 Create a new node (N3) which will be the new branch, containing the unmatched characters from
     *                 the rest of the key
     *             </li>
     *             <li>
     *                 Add N2 as a child of N1
     *             </li>
     *             <li>
     *                 Add N3 as a child of N1
     *             </li>
     *             <li>
     *                 In the <b>parent node of NF</b>, replace the edge pointing to NF with an edge pointing instead
     *                 to N1. If we do this step atomically, reading threads are guaranteed to never see "invalid"
     *                 data, only either the old data or the new data
     *             </li>
     *         </ol>
     *     </li>
     * </ul>
     * The {@link SearchResult#classification} is an enum value based on its classification of the
     * match according to the descriptions above.
     *
     * @param key a key for which the node matching the longest prefix of the key is required
     * @return A {@link SearchResult} object which contains the node matching the longest prefix of the key, its
     * parent node, the number of characters of the key which were matched in total and within the edge of the
     * matched node, and a {@link SearchResult#classification} of the match as described above
     */
    SearchResult searchTree(CharSequence key) {
        Node parentNodesParent = null;
        Node parentNode = null;
        Node currentNode = root;
        int charsMatched = 0, charsMatchedInNodeFound = 0;

        final int keyLength = key.length();
        outer_loop: while (charsMatched < keyLength) {
            Node nextNode = currentNode.getOutgoingEdge(key.charAt(charsMatched));
            if (nextNode == null) {
                // Next node is a dead end...
                //noinspection UnnecessaryLabelOnBreakStatement
                break outer_loop;
            }

            parentNodesParent = parentNode;
            parentNode = currentNode;
            currentNode = nextNode;
            charsMatchedInNodeFound = 0;
            CharSequence currentNodeEdgeCharacters = currentNode.getIncomingEdge();
            for (int i = 0, numEdgeChars = currentNodeEdgeCharacters.length(); i < numEdgeChars && charsMatched < keyLength; i++) {
                if (currentNodeEdgeCharacters.charAt(i) != key.charAt(charsMatched)) {
                    // Found a difference in chars between character in key and a character in current node.
                    // Current node is the deepest match (inexact match)....
                    break outer_loop;
                }
                charsMatched++;
                charsMatchedInNodeFound++;
            }
        }
        return new SearchResult(key, currentNode, charsMatched, charsMatchedInNodeFound, parentNode, parentNodesParent);
    }

    /**
     * Encapsulates results of searching the tree for a node for which a given key is a prefix. Encapsulates the node
     * found, its parent node, its parent's parent node, and the number of characters matched in the current node and
     * in total.
     * <p/>
     * Also classifies the search result so that algorithms in methods which use this SearchResult, when adding nodes
     * and removing nodes from the tree, can select appropriate strategies based on the classification.
     */
    static class SearchResult {
        final CharSequence key;
        final Node nodeFound;
        final int charsMatched;
        final int charsMatchedInNodeFound;
        final Node parentNode;
        final Node parentNodesParent;
        final Classification classification;

        enum Classification {
            EXACT_MATCH,
            INCOMPLETE_MATCH_TO_END_OF_EDGE,
            INCOMPLETE_MATCH_TO_MIDDLE_OF_EDGE,
            KEY_ENDS_MID_EDGE,
            INVALID // INVALID is never used, except in unit testing
        }

        SearchResult(CharSequence key, Node nodeFound, int charsMatched, int charsMatchedInNodeFound, Node parentNode, Node parentNodesParent) {
            this.key = key;
            this.nodeFound = nodeFound;
            this.charsMatched = charsMatched;
            this.charsMatchedInNodeFound = charsMatchedInNodeFound;
            this.parentNode = parentNode;
            this.parentNodesParent = parentNodesParent;

            // Classify this search result...
            this.classification = classify(key, nodeFound, charsMatched, charsMatchedInNodeFound);
        }

        protected Classification classify(CharSequence key, Node nodeFound, int charsMatched, int charsMatchedInNodeFound) {
            if (charsMatched == key.length()) {
                if (charsMatchedInNodeFound == nodeFound.getIncomingEdge().length()) {
                    return Classification.EXACT_MATCH;
                }
                else if (charsMatchedInNodeFound < nodeFound.getIncomingEdge().length()) {
                    return Classification.KEY_ENDS_MID_EDGE;
                }
            }
            else if (charsMatched < key.length()) {
                if (charsMatchedInNodeFound == nodeFound.getIncomingEdge().length()) {
                    return Classification.INCOMPLETE_MATCH_TO_END_OF_EDGE;
                }
                else if (charsMatchedInNodeFound < nodeFound.getIncomingEdge().length()) {
                    return Classification.INCOMPLETE_MATCH_TO_MIDDLE_OF_EDGE;
                }
            }
            throw new IllegalStateException("Unexpected failure to classify SearchResult: " + this);
        }

        @Override
        public String toString() {
            return "SearchResult{" +
                    "key=" + key +
                    ", nodeFound=" + nodeFound +
                    ", charsMatched=" + charsMatched +
                    ", charsMatchedInNodeFound=" + charsMatchedInNodeFound +
                    ", parentNode=" + parentNode +
                    ", parentNodesParent=" + parentNodesParent +
                    ", classification=" + classification +
                    '}';
        }
    }

    // ------------- Helper method for pretty-printing tree (not public API) -------------

    @Override
    public Node getNode() {
        return root;
    }

}
