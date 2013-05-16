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

import com.googlecode.concurrenttrees.radix.node.Node;
import com.googlecode.concurrenttrees.radix.node.NodeFactory;
import com.googlecode.concurrenttrees.radix.node.concrete.chararray.*;
import com.googlecode.concurrenttrees.radix.node.concrete.voidvalue.VoidValue;
import com.googlecode.concurrenttrees.radix.node.util.NodeUtil;
import com.googlecode.concurrenttrees.common.CharSequences;

import java.io.Serializable;
import java.util.List;

/**
 * A {@link NodeFactory} which creates various implementations of {@link Node} objects all of which store incoming
 * edge characters as a character array inside the node.
 * <p/>
 * Returns an optimal node implementation depending on arguments supplied, which will be one of:
 * <ul>
 *     <li>{@link CharArrayNodeDefault} - contains all possible fields</li>
 *     <li>{@link CharArrayNodeNonLeafNullValue} - does not store a value, returns {@code null} for value</li>
 *     <li>{@link CharArrayNodeNonLeafVoidValue} - does not store a value, returns {@link VoidValue} for value</li>
 *     <li>{@link CharArrayNodeLeafVoidValue} - does not store child edges or a value, returns {@link VoidValue} for value</li>
 *     <li>{@link CharArrayNodeLeafWithValue} - does not store child edges, but does store a value</li>
 * </ul>
 * <p/>
 * When the application supplies {@link VoidValue} for a value, this factory will omit actually storing that value
 * in the tree and will return one of the VoidValue-optimized nodes above which can reduce memory usage.
 *
 * @author Niall Gallagher
 */
public class DefaultCharArrayNodeFactory implements NodeFactory, Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 2244370851498317602L;

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
                return new CharArrayNodeLeafVoidValue(edgeCharacters);
            }
            else if (value != null) {
                return new CharArrayNodeLeafWithValue(edgeCharacters, value);
            }
            else {
                return new CharArrayNodeLeafNullValue(edgeCharacters);
            }
        }
        else {
            // Non-leaf node...
            if (value instanceof VoidValue) {
                return new CharArrayNodeNonLeafVoidValue(edgeCharacters, childNodes);
            }
            else if (value == null) {
                return new CharArrayNodeNonLeafNullValue(edgeCharacters, childNodes);
            }
            else {
                return new CharArrayNodeDefault(edgeCharacters, value, childNodes);
            }
        }
    }

}
