/**
 * Copyright 2012-2013 Niall Gallagher
 * Copyright 2007 The Guava Authors
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

import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An unmodifiable iterator which computes the next element to return only when it is requested.
 * <p/>
 * This class is inspired by com.google.common.collect.AbstractIterator in Google Guava,
 * which was written by the Google Guava Authors, in particular by Kevin Bourrillion.
 *
 * @author Niall Gallagher
 */
public abstract class LazyIterator<T> implements Iterator<T>,  Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = -1248910982407487043L;
	T next = null;

    enum State { READY, NOT_READY, DONE, FAILED }

    State state = State.NOT_READY;

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Iterator.remove() is not supported");
    }

    @Override
    public final boolean hasNext() {
        if (state == State.FAILED) {
            throw new IllegalStateException("This iterator is in an inconsistent state, and can no longer be used, " +
                    "due to an exception previously thrown by the computeNext() method");
        }
        switch (state) {
            case DONE:
                return false;
            case READY:
                return true;
        }
        return tryToComputeNext();
    }

    boolean tryToComputeNext() {
        state = State.FAILED; // temporary pessimism
        next = computeNext();
        if (state != State.DONE) {
            state = State.READY;
            return true;
        }
        return false;
    }

    @Override
    public final T next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        state = State.NOT_READY;
        return next;
    }

    /**
     *
     * @return a dummy value which if returned by the <code>computeNext()</code> method, signals that there are no more
     * elements to return
     */
    protected final T endOfData() {
        state = State.DONE;
        return null;
    }

    /**
     * @return The next element which the iterator should return, or the result of calling <code>endOfData()</code>
     * if there are no more elements to return
     */
    protected abstract T computeNext();
}
