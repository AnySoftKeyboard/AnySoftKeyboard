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
 * A lightweight object which simply wraps a {@link Character} and implements {@link NodeCharacterProvider}, which
 * can be used as a key to locate a node having the same edge first character in a list of nodes using binary search.
 *
 * @author Niall Gallagher
 */
public class NodeCharacterKey implements NodeCharacterProvider {

    private final Character character;

    public NodeCharacterKey(Character character) {
        this.character = character;
    }

    @Override
    public Character getIncomingEdgeFirstCharacter() {
        return character;
    }
}
