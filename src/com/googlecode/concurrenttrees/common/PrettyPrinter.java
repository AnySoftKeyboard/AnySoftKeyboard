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

import com.googlecode.concurrenttrees.radix.node.Node;
import com.googlecode.concurrenttrees.radix.node.util.PrettyPrintable;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/**
 * Utility methods to generate semi-graphical string representations of trees.
 *
 * @author Niall Gallagher
 */
public class PrettyPrinter implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 9014541865668738764L;

	/**
     * Private constructor, not used.
     */
    PrettyPrinter() {
    }

    /**
     * Generates a semi-graphical string representation of a given tree.
     * <p/>
     * Example output:<br/>
     * <pre>
     * ○
     * └── ○ B (1)
     *     └── ○ A (2)
     *         └── ○ N (3)
     *             ├── ○ AN (5)
     *             │   └── ○ A (6)
     *             └── ○ DANA (4)
     * </pre>
     *
     * @param tree The tree for which the semi-graphical representation should be generated
     * @return A semi-graphical string representation of the tree
     */
    public static String prettyPrint(PrettyPrintable tree) {
        StringBuilder sb = new StringBuilder();
        prettyPrint(tree.getNode(), sb, "", true, true);
        return sb.toString();
    }

    /**
     * Generates a semi-graphical string representation of a given tree, writing it to a given {@link Appendable}.
     * <p/>
     * Example output:<br/>
     * <pre>
     * ○
     * └── ○ B (1)
     *     └── ○ A (2)
     *         └── ○ N (3)
     *             ├── ○ AN (5)
     *             │   └── ○ A (6)
     *             └── ○ DANA (4)
     * </pre>
     *
     * @param tree The tree for which the semi-graphical representation should be generated
     * @param appendable The object ot which the tree should be written
     */
    public static void prettyPrint(PrettyPrintable tree, Appendable appendable) {
        prettyPrint(tree.getNode(), appendable, "", true, true);
    }

    static void prettyPrint(Node node, Appendable sb, String prefix, boolean isTail, boolean isRoot) {
        try {
            StringBuilder label = new StringBuilder();
            if (isRoot) {
                label.append("○");
                if (node.getIncomingEdge().length() > 0) {
                    label.append(" ");
                }
            }
            label.append(node.getIncomingEdge());
            if (node.getValue() != null) {
                label.append(" (").append(node.getValue()).append(")");
            }
            sb.append(prefix).append(isTail ? isRoot ? "" : "└── ○ " : "├── ○ ").append(label).append("\n");
            List<Node> children = node.getOutgoingEdges();
            for (int i = 0; i < children.size() - 1; i++) {
                prettyPrint(children.get(i), sb, prefix + (isTail ? isRoot ? "" : "    " : "│   "), false, false);
            }
            if (!children.isEmpty()) {
                prettyPrint(children.get(children.size() - 1), sb, prefix + (isTail ? isRoot ? "" : "    " : "│   "), true, false);
            }
        }
        catch (IOException ioException) {
            // Rethrow the checked exception as a runtime exception...
            throw new IllegalStateException(ioException);
        }
    }
}
