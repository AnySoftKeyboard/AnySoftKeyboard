/*
 * Copyright (c) 2013 Menny Even-Danan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.anysoftkeyboard.keyboards;

import java.util.ArrayList;
import java.util.List;

class KeyEventStateMachine {

    static final int KEYCODE_FIRST_CHAR = -4097;
    private static final int MAX_NFA_DIVIDES = 30;
    private KeyEventState mStart;
    private RingBuffer mWalker;
    private RingBuffer mWalkerHelper;
    private RingBuffer mWalkerUnused;
    private int mSequenceLength;
    private int mResultChar;

    KeyEventStateMachine() {
        this.mStart = new KeyEventState();
        this.mWalker = new RingBuffer();
        this.mWalker.putItem(new NFAPart());

        this.mWalkerUnused = new RingBuffer();
        for (int i = 1; i < MAX_NFA_DIVIDES; i++)
            this.mWalkerUnused.putItem(new NFAPart());

        this.mWalkerHelper = new RingBuffer();
    }

    private static KeyEventState addNextState(KeyEventState current, int keyCode) {
        KeyEventState next = current.getNext(keyCode);
        if (next != null)
            return next;
        next = new KeyEventState();
        current.addNextState(keyCode, next);
        return next;
    }

    public void addSequence(int[] sequence, int result) {
        addSpecialKeySequence(sequence, 0/*no special key*/, result);
    }

    public void addSpecialKeySequence(int[] sequence, int specialKey, int result) {
        KeyEventState c = this.mStart;

        for (int aSequence : sequence) {
            if (specialKey != 0) {
                //special key first
                c = addNextState(c, specialKey);
            }
            //the sequence second
            c = addNextState(c, aSequence);
        }
        c.setCharacter(result);
    }

    public State addKeyCode(int keyCode) {
        this.mSequenceLength = 0;
        this.mResultChar = 0;

        NFAPart found = null;
        State resultstate = State.RESET;

        if (!this.mWalker.hasItem()) {
            NFAPart part = this.mWalkerUnused.getItem();
            part.reset();
            this.mWalker.putItem(part);
        }

        while (this.mWalker.hasItem()) {
            NFAPart cWalker = this.mWalker.getItem();

            State result = cWalker.addKeyCode(keyCode);
            if (result == State.REWIND) {
                if (this.mWalkerUnused.hasItem()) {
                    NFAPart newwalker = this.mWalkerUnused.getItem();
                    newwalker.reset(cWalker);
                    this.mWalkerHelper.putItem(newwalker);
                }
                cWalker.returnToFirst(keyCode);
                result = cWalker.addKeyCode(keyCode);
            }

            if (result == State.FULL_MATCH) {
                if (found == null) {
                    this.mWalkerHelper.putItem(cWalker);
                    resultstate = result;
                    found = cWalker;
                    break;
                }
            }

            if (result == State.PART_MATCH || result == State.NO_MATCH) {
                if (resultstate == State.RESET)
                    resultstate = result;
                this.mWalkerHelper.putItem(cWalker);
            } else {
                this.mWalkerUnused.putItem(cWalker);
            }
            if (result == State.PART_MATCH) {
                if (this.mWalkerUnused.hasItem()) {
                    NFAPart newwalker = this.mWalkerUnused.getItem();
                    newwalker.reset();
                    this.mWalkerHelper.putItem(newwalker);
                }
            }
            if (result == State.PART_MATCH) {
                if ((found == null) || (found.mSequenceLength < cWalker.mSequenceLength)) {
                    found = cWalker;
                    resultstate = result;
                }
            }
        }
        while (this.mWalker.hasItem())
            this.mWalkerUnused.putItem(this.mWalker.getItem());

        final RingBuffer switchWalkerarrays = this.mWalkerHelper;
        this.mWalkerHelper = this.mWalker;
        this.mWalker = switchWalkerarrays;

        if (found != null) {
            this.mSequenceLength = found.mVisibleSequenceLength;
            this.mResultChar = found.mResultChar;

            int i = 0;
            final int count = this.mWalker.getCount();
            while (i < count) {
                NFAPart part = this.mWalker.getItem();
                this.mWalker.putItem(part);
                i++;
                if (part == found && resultstate == State.FULL_MATCH)
                    break;

                if (found.mVisibleSequenceLength > 1) {
                    part.iVisibleSequenceLength -= found.mVisibleSequenceLength - 1;
                }

                if (part == found)
                    break;
            }
            while (i++ < count) {
                this.mWalker.putItem(this.mWalker.getItem());
            }
        }
        return resultstate;
    }

    public int getCharacter() {
        return this.mResultChar;
    }

    public int getSequenceLength() {
        return this.mSequenceLength;
    }

    public void reset() {
        while (this.mWalker.hasItem())
            this.mWalkerUnused.putItem(this.mWalker.getItem());
        NFAPart first = this.mWalkerUnused.getItem();
        first.reset();
        this.mWalker.putItem(first);
    }

    public enum State {RESET, REWIND, NO_MATCH, PART_MATCH, FULL_MATCH}

    private static final class KeyEventTransition {

        private KeyEventState mNext;
        private int mKeyCode;

        KeyEventTransition(int keyCode, KeyEventState next) {
            this.mNext = next;
            this.mKeyCode = keyCode;
        }

    }

    private static final class KeyEventState {

        private List<KeyEventTransition> mTransitions;
        private int mResult;

        KeyEventState() {
            this.mResult = 0;
        }

        public KeyEventState getNext(int keyCode) {
            if (this.mTransitions == null)
                return null;
            for (KeyEventTransition transition : this.mTransitions) {
                if (transition.mKeyCode == keyCode) {
                    return transition.mNext;
                }
            }
            return null;
        }

        public void addNextState(int keyCode, KeyEventState next) {
            if (this.mTransitions == null)
                this.mTransitions = new ArrayList<>();
            this.mTransitions.add(new KeyEventTransition(keyCode, next));
        }

        public void setCharacter(int result) {
            this.mResult = result;
        }

        public boolean hasNext() {
            return (this.mTransitions != null);
        }

    }

    private class NFAPart {

        KeyEventState state;
        int iVisibleSequenceLength;
        int iSequenceLength;
        private int mResultChar;
        private int mSequenceLength;
        private int mVisibleSequenceLength;

        NFAPart() {
            this.reset();
        }

        void reset() {
            this.state = KeyEventStateMachine.this.mStart;
            this.iSequenceLength = 0;
            this.iVisibleSequenceLength = 0;
        }

        void reset(NFAPart part) {
            this.state = part.state;
            this.iSequenceLength = part.iSequenceLength;
            this.iVisibleSequenceLength = part.iVisibleSequenceLength;
        }


        private void returnToFirst(int keyCode) {
            this.state = KeyEventStateMachine.this.mStart;
            if (keyCode > 0)
                this.iVisibleSequenceLength--;
            this.iSequenceLength--;
        }

        private State addKeyCode(int keyCode) {
            this.state = this.state.getNext(keyCode);
            if (this.state == null) {
                this.reset();
                return State.RESET;
            }
            if (keyCode > 0)
                this.iVisibleSequenceLength++;
            this.iSequenceLength++;

            if (this.state.mResult != 0) {
                this.mResultChar = this.state.mResult;
                this.mSequenceLength = this.iSequenceLength;
                this.mVisibleSequenceLength = this.iVisibleSequenceLength;

                if (this.mResultChar == KEYCODE_FIRST_CHAR) {
                    return State.REWIND;
                }

                if (!this.state.hasNext()) {
                    this.reset();
                    return State.FULL_MATCH;
                }
                return State.PART_MATCH;
            }
            return State.NO_MATCH;
        }
    }

    static class RingBuffer {

        private NFAPart[] mBuffer;
        private int mStart;
        private int mEnd;
        private int mCount;

        RingBuffer() {
            this.mBuffer = new NFAPart[MAX_NFA_DIVIDES];
            this.mStart = 0;
            this.mEnd = 0;
            this.mCount = 0;
        }

        boolean hasItem() {
            return this.mCount > 0;
        }

        NFAPart getItem() {
            NFAPart result = this.mBuffer[this.mStart];
            this.mBuffer[this.mStart] = null;
            this.mStart = (this.mStart + 1) % MAX_NFA_DIVIDES;
            this.mCount--;
            return result;
        }

        void putItem(NFAPart item) {
            this.mBuffer[this.mEnd] = item;
            this.mEnd = (this.mEnd + 1) % MAX_NFA_DIVIDES;
            this.mCount++;
        }

        int getCount() {
            return this.mCount;
        }

    }

}