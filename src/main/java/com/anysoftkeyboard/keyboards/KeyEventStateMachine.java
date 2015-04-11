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

import java.util.LinkedList;

public class KeyEventStateMachine {

    public static final int KEYCODE_FIRST_CHAR = -4097;

    private static final class KeyEventTransition {

        private KeyEventState next;
        private int keyCode;

        KeyEventTransition(int keyCode, KeyEventState next) {
            this.next = next;
            this.keyCode = keyCode;
        }

    }

    private static final class KeyEventState {

        private LinkedList<KeyEventTransition> transitions;
        private int result;

        KeyEventState() {
            this.result = 0;
        }

        public KeyEventState getNext(int keyCode) {
            if (this.transitions == null)
                return null;
            for (KeyEventTransition transition : this.transitions) {
                if (transition.keyCode == keyCode) {
                    return transition.next;
                }
            }
            return null;
        }

        public void addNextState(int keyCode, KeyEventState next) {
            if (this.transitions == null)
                this.transitions = new LinkedList<>();
            this.transitions.add(new KeyEventTransition(keyCode, next));
        }

        public void setCharacter(int result) {
            this.result = result;
        }

        public boolean hasNext() {
            return (this.transitions != null);
        }

    }


    private KeyEventState start;

    public static enum State {RESET, REWIND, NO_MATCH, PART_MATCH, FULL_MATCH}

    private class NFAPart {

        KeyEventState state;
        int iVisibleSequenceLength;
        int iSequenceLength;
        private int resultChar;
        private int sequenceLength;
        private int visibleSequenceLength;

        NFAPart() {
            this.reset();
        }

        void reset() {
            this.state = KeyEventStateMachine.this.start;
            this.iSequenceLength = 0;
            this.iVisibleSequenceLength = 0;
        }

        void reset(NFAPart part) {
            this.state = part.state;
            this.iSequenceLength = part.iSequenceLength;
            this.iVisibleSequenceLength = part.iVisibleSequenceLength;
        }


        private void returnToFirst(int keyCode) {
            this.state = KeyEventStateMachine.this.start;
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

            if (this.state.result != 0) {
                this.resultChar = this.state.result;
                this.sequenceLength = this.iSequenceLength;
                this.visibleSequenceLength = this.iVisibleSequenceLength;

                if (this.resultChar == KEYCODE_FIRST_CHAR) {
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

    private static final int MAX_NFA_DIVIDES = 30;

    class RingBuffer {

        private NFAPart[] buffer;
        private int start;
        private int end;
        private int count;

        RingBuffer() {
            this.buffer = new NFAPart[MAX_NFA_DIVIDES];
            this.start = 0;
            this.end = 0;
            this.count = 0;
        }

        boolean hasItem() {
            return this.count > 0;
        }

        NFAPart getItem() {
            assert (this.count > 0);
            NFAPart result = this.buffer[this.start];
            this.buffer[this.start] = null;
            this.start = (this.start + 1) % MAX_NFA_DIVIDES;
            this.count--;
            return result;
        }

        void putItem(NFAPart item) {
            assert (this.count < MAX_NFA_DIVIDES);
            this.buffer[this.end] = item;
            this.end = (this.end + 1) % MAX_NFA_DIVIDES;
            this.count++;
        }

        int getCount() {
            return this.count;
        }

    }


    private RingBuffer walker;
    private RingBuffer walkerhelper;
    private RingBuffer walkerunused;


    private int sequenceLength;
    private int resultChar;

    public KeyEventStateMachine() {
        this.start = new KeyEventState();
        this.walker = new RingBuffer();
        this.walker.putItem(new NFAPart());

        this.walkerunused = new RingBuffer();
        for (int i = 1; i < MAX_NFA_DIVIDES; i++)
            this.walkerunused.putItem(new NFAPart());

        this.walkerhelper = new RingBuffer();
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
        KeyEventState c = this.start;

        for (int i = 0; i < sequence.length; i++) {
            if (specialKey != 0) {
                //special key first
                c = addNextState(c, specialKey);
            }
            //the sequence second
            c = addNextState(c, sequence[i]);
        }
        c.setCharacter(result);
    }

    public State addKeyCode(int keyCode) {
        this.sequenceLength = 0;
        this.resultChar = 0;

        NFAPart found = null;
        State resultstate = State.RESET;

        if (!this.walker.hasItem()) {
            NFAPart part = this.walkerunused.getItem();
            part.reset();
            this.walker.putItem(part);
        }

        while (this.walker.hasItem()) {
            NFAPart cWalker = this.walker.getItem();

            State result = cWalker.addKeyCode(keyCode);
            if (result == State.REWIND) {
                if (this.walkerunused.hasItem()) {
                    NFAPart newwalker = this.walkerunused.getItem();
                    newwalker.reset(cWalker);
                    this.walkerhelper.putItem(newwalker);
                }
                cWalker.returnToFirst(keyCode);
                result = cWalker.addKeyCode(keyCode);
            }

            if (result == State.FULL_MATCH) {
                if (found == null) {
                    this.walkerhelper.putItem(cWalker);
                    resultstate = result;
                    found = cWalker;
                    break;
                }
            }

            if (result == State.PART_MATCH || result == State.NO_MATCH) {
                if (resultstate == State.RESET)
                    resultstate = result;
                this.walkerhelper.putItem(cWalker);
            } else {
                this.walkerunused.putItem(cWalker);
            }
            if (result == State.PART_MATCH) {
                if (this.walkerunused.hasItem()) {
                    NFAPart newwalker = this.walkerunused.getItem();
                    newwalker.reset();
                    this.walkerhelper.putItem(newwalker);
                }
            }
            if (result == State.PART_MATCH) {
                if ((found == null) || (found.sequenceLength < cWalker.sequenceLength)) {
                    found = cWalker;
                    resultstate = result;
                }
            }
        }
        while (this.walker.hasItem())
            this.walkerunused.putItem(this.walker.getItem());

        final RingBuffer switchWalkerarrays = this.walkerhelper;
        this.walkerhelper = this.walker;
        this.walker = switchWalkerarrays;

        if (found != null) {
            this.sequenceLength = found.visibleSequenceLength;
            this.resultChar = found.resultChar;

            int i = 0;
            final int count = this.walker.getCount();
            while (i < count) {
                NFAPart part = this.walker.getItem();
                this.walker.putItem(part);
                i++;
                if (part == found && resultstate == State.FULL_MATCH)
                    break;

                if (found.visibleSequenceLength > 1) {
                    part.iVisibleSequenceLength -= found.visibleSequenceLength - 1;
                }

                if (part == found)
                    break;
            }
            while (i++ < count) {
                this.walker.putItem(this.walker.getItem());
            }
        }
        return resultstate;
    }

    public int getCharacter() {
        return this.resultChar;
    }

    public int getSequenceLength() {
        return this.sequenceLength;
    }

    public void reset() {
        while (this.walker.hasItem())
            this.walkerunused.putItem(this.walker.getItem());
        NFAPart first = this.walkerunused.getItem();
        first.reset();
        this.walker.putItem(first);
    }

}