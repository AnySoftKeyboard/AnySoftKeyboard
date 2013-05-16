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

/**
 * An internal interface implemented by trees, which allows internal details of trees to be accessed by
 * {@link com.googlecode.concurrenttrees.common.PrettyPrinter}.
 *
 * @author Niall Gallagher
 */
public interface PrettyPrintable {
    public Node getNode();
}
