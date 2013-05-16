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

import com.googlecode.concurrenttrees.radix.node.util.NodeCharacterProvider;

import java.util.List;

/**
 * Specifies the methods that nodes must implement.
 * <p/>
 * The main function of a node is to represent an "edge" in the tree. An edge is a connection from a parent node to a
 * child node which represents a sequence of characters. For practical reasons we store these characters in the child
 * node, to avoid needing separate Edge objects. All nodes except the root encode at least one character for an edge.
 * <p/>
 * Nodes contain several fields, but not all nodes will actually need to store values in every field. Therefore
 * some specialized implementations of this interface are possible, optimized for storing various combinations of
 * data items in reduced numbers of fields, to reduce memory overhead.
 * <p/>
 * Nodes are <u><i>partially immutable</i></u>:
 * <ul>
 *     <li>
 *         The <i>characters of an "edge"</i> encoded in within a node are <u>immutable</u> (these characters belong to
 *         the edge arriving at the current node from a parent node)
 *     </li>
 *     <li>
 *         The <i>number of outgoing edges</i> from a node (references to child nodes), and the <i>first characters of
 *         those edges</i> are <u>immutable</u>
 *     </li>
 *     <li>
 *         The <i>references to child nodes for existing edges</i> (as identified by their first characters) are
 *         <u>mutable with constraints</u>; the reference to a child node for an existing edge may be updated to point
 *         to a different child node as long as the new edge starts with the same first character
 *     </li>
 *     <li>
 *         If a node stores a value, the reference to the value is <u>immutable</u> (values can be changed but it
 *         requires recreating the node with the new value - this is to account for specialized node implementations
 *         omitting a field for the value when not required)
 *     </li>
 * </ul>
 * These constraints exist allow concurrent traversal and modifications to the tree. Nodes are required to implement
 * some operations <u>atomically</u>, see documentation on each method in this interface for details.
 * <p/>
 * Hints for specialized implementations of this Node interface:
 * <ul>
 *     <li>
 *         Leaf nodes do not need to store references to child nodes; a specialized node implementation
 *         could eliminate a field and associated data structure for child node references
 *     </li>
 *     <li>
 *         All leaf nodes store values
 *     </li>
 *     <li>
 *         Some non-leaf nodes store values, some do not
 *     </li>
 *     <li>
 *         Edge character data can be encoded using implementation-specific methods.
 *         <p/>
 *         Nodes are not required to store a {@link CharSequence} object verbatim, or use a particular implementation of
 *         {@link CharSequence}, the only requirement is that they provide a {@link CharSequence} <i>view</i> onto
 *         the character data.
 *         <p/>
 *         Character data can optionally be stored outside of the tree.  {@link CharSequence}s can encode a start and
 *         end offset (or length) as a view onto a larger string (possibly a view onto the original key inserted).
 *         Furthermore end offset could be stored as length, relative to the start offset with variable length encoding
 *         to avoid storing 4 bytes for the length. This option would have consequences for
 *         garbage collection of large string keys however, therefore would mostly suit immutable data sets.
 *         <p/>
 *         Character data can be compressed. {@link CharSequence}s are free to store character data within the tree but
 *         in a size-reduced encoding such as UTF-8
 *     </li>
 * </ul>
 *
 * @author Niall Gallagher
 */
public interface Node extends NodeCharacterProvider {

    /**
     * Returns the first character of the "edge" encoded in this node, belonging to the connection from a parent node to
     * this node.
     * <p/>
     *
     * @return The first character of the "edge" encoded in this node
     */
    Character getIncomingEdgeFirstCharacter();

    /**
     * Returns all characters of the "edge" encoded in this node, belonging to the connection from a parent node to this
     * node.
     *
     * @return All characters of the "edge" encoded in this node
     */
    CharSequence getIncomingEdge();

    /**
     * Returns a value object which has been associated with a key and which is stored in this node, or returns
     * <code>null</code> if no value is stored in this node.
     *
     * @return A value object which has been associated with a key and which is stored in this node, or returns
     * <code>null</code> if no value is stored in this node
     */
    Object getValue();


    /**
     * Returns the child of this node whose edge starts with the given first character.
     * <p/>
     * This <i>read</i> must be performed <b><u>atomically</u></b>, in relation to writes made via
     * {@link #updateOutgoingEdge(Node)}.
     *
     * @param edgeFirstCharacter The first character of the edge for which the associated child node is required
     * @return The child of this node whose edge starts with the given first character, or <code>null</code> if this
     * node has no such outgoing edge
     */
    Node getOutgoingEdge(Character edgeFirstCharacter);

    /**
     * Updates the child node reference for a given edge (identified by its first character) to point to a different
     * child node.
     * <p/>
     * The first character of the given child node's edge must match the first character of an existing outgoing
     * edge from this node.
     * <p/>
     * This <i>write</i> must be performed <b><u>atomically</u></b>, in relation to reads made via
     * {@link #getOutgoingEdge(Character)}.
     *
     * @param childNode The new child node to associated with this edge
     */
    void updateOutgoingEdge(Node childNode);

    /**
     * Returns a read-only list of the child nodes to which this node has outgoing edges, i.e. child nodes which have
     * incoming edges from this node.
     * <p/>
     * It is intended that this method will be used for copying/cloning nodes.
     *
     * @return A read-only list of the child nodes to which this node has outgoing edges
     */
    List<Node> getOutgoingEdges();
}
