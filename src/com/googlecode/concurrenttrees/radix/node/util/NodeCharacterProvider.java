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

/**
 * A super-interface of both {@link com.googlecode.concurrenttrees.radix.node.Node} and {@link NodeCharacterKey}
 * which, by sharing this common interface, enables binary search of nodes via
 * {@link java.util.Collections#binarySearch(java.util.List, Object, java.util.Comparator)}.
 *
 * @see NodeCharacterComparator
 * @see NodeCharacterKey
 *
 * @author Niall Gallagher
 */
public interface NodeCharacterProvider {

    Character getIncomingEdgeFirstCharacter();
}
