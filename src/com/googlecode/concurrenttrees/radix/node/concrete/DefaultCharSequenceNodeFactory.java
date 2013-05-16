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
package com.googlecode.concurrenttrees.radix.node.concrete;

import com.googlecode.concurrenttrees.common.CharSequences;
import com.googlecode.concurrenttrees.radix.node.Node;
import com.googlecode.concurrenttrees.radix.node.NodeFactory;
import com.googlecode.concurrenttrees.radix.node.concrete.charsequence.*;
import com.googlecode.concurrenttrees.radix.node.concrete.voidvalue.VoidValue;
import com.googlecode.concurrenttrees.radix.node.util.NodeUtil;

import java.io.Serializable;
import java.util.List;

/**
 * A {@link NodeFactory} which creates various implementations of {@link Node} objects all of which store incoming
 * edge characters as a {@link CharSequence} (a <i>view</i> onto the original key) rather than copying the edge into a
 * character array.
 * <p/>
 * Returns an optimal node implementation depending on arguments supplied, which will be one of:
 * <ul>
 *     <li>{@link CharSequenceNodeDefault} - contains all possible fields</li>
 *     <li>{@link CharSequenceNodeNonLeafNullValue} - does not store a value, returns {@code null} for value</li>
 *     <li>{@link CharSequenceNodeNonLeafVoidValue} - does not store a value, returns {@link VoidValue} for value</li>
 *     <li>{@link CharSequenceNodeLeafVoidValue} - does not store child edges or a value, returns {@link VoidValue} for value</li>
 *     <li>{@link CharSequenceNodeLeafWithValue} - does not store child edges, but does store a value</li>
 * </ul>
 * <p/>
 * When the application supplies {@link VoidValue} for a value, this factory will omit actually storing that value
 * in the tree and will return one of the Void-optimized nodes above which can reduce memory usage.
 *
 * @author Niall Gallagher
 */
public class DefaultCharSequenceNodeFactory implements NodeFactory,  Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 4847556568042758719L;

	@Override
    public Node createNode(CharSequence edgeCharacters, Object value, List<Node> childNodes, boolean isRoot) {
        if (edgeCharacters == null) {
            throw new IllegalStateException("The edgeCharacters argument was null");
        }
        if (!isRoot && edgeCharacters.length() == 0) {
            throw new IllegalStateException("Invalid edge characters for non-root node: " + CharSequences.toString(edgeCharacters));
        }
        if (childNodes == null) {
            throw new IllegalStateException("The childNodes argument was null");
        }
        NodeUtil.ensureNoDuplicateEdges(childNodes);


        if (childNodes.isEmpty()) {
            // Leaf node...
            if (value instanceof VoidValue) {
                return new CharSequenceNodeLeafVoidValue(edgeCharacters);
            }
            else if (value != null) {
                return new CharSequenceNodeLeafWithValue(edgeCharacters, value);
            }
            else {
                return new CharSequenceNodeLeafNullValue(edgeCharacters);
            }
        }
        else {
            // Non-leaf node...
            if (value instanceof VoidValue) {
                return new CharSequenceNodeNonLeafVoidValue(edgeCharacters, childNodes);
            }
            else if (value == null) {
                return new CharSequenceNodeNonLeafNullValue(edgeCharacters, childNodes);
            }
            else {
                return new CharSequenceNodeDefault(edgeCharacters, value, childNodes);
            }
        }
    }

}
