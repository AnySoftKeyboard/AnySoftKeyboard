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
package com.googlecode.concurrenttrees.radix.node.concrete.voidvalue;

import java.io.Serializable;

/**
 * A dummy object which if supplied as a value for an entry in a tree, will not actually be stored in the tree by
 * {@link com.googlecode.concurrenttrees.radix.node.concrete.DefaultCharArrayNodeFactory} or
 * {@link com.googlecode.concurrenttrees.radix.node.concrete.DefaultCharSequenceNodeFactory} to save memory.
 *
 * @author Niall Gallagher
 */
public class VoidValue implements Serializable {

    @Override
    public int hashCode() {
        return 1;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof VoidValue;
    }

    @Override
    public String toString() {
        return "-";
    }

    VoidValue() {
    }

    public static final VoidValue SINGLETON = new VoidValue();
}
