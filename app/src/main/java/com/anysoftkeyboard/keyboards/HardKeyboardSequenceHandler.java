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

import android.view.KeyEvent;

import com.anysoftkeyboard.AnySoftKeyboard;
import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.keyboards.KeyEventStateMachine.State;
import com.menny.android.anysoftkeyboard.AnyApplication;

import java.security.InvalidParameterException;

public class HardKeyboardSequenceHandler {
    private static final int[] msQwerty = new int[]{
            KeyEvent.KEYCODE_Q, KeyEvent.KEYCODE_W, KeyEvent.KEYCODE_E, KeyEvent.KEYCODE_R, KeyEvent.KEYCODE_T, KeyEvent.KEYCODE_Y, KeyEvent.KEYCODE_U, KeyEvent.KEYCODE_I, KeyEvent.KEYCODE_O, KeyEvent.KEYCODE_P,
            KeyEvent.KEYCODE_A, KeyEvent.KEYCODE_S, KeyEvent.KEYCODE_D, KeyEvent.KEYCODE_F, KeyEvent.KEYCODE_G, KeyEvent.KEYCODE_H, KeyEvent.KEYCODE_J, KeyEvent.KEYCODE_K, KeyEvent.KEYCODE_L,
            KeyEvent.KEYCODE_Z, KeyEvent.KEYCODE_X, KeyEvent.KEYCODE_C, KeyEvent.KEYCODE_V, KeyEvent.KEYCODE_B, KeyEvent.KEYCODE_N, KeyEvent.KEYCODE_M
    };

    //See 'getSequenceCharacter' function for usage for msSequenceLivingTime and mLastTypedKeyEventTime.
    //private static final long msSequenceLivingTime = 600;
    private long mLastTypedKeyEventTime;
    private final KeyEventStateMachine mCurrentSequence;

    public HardKeyboardSequenceHandler() {
        mCurrentSequence = new KeyEventStateMachine();
        mLastTypedKeyEventTime = System.currentTimeMillis();
    }

    public void addQwertyTranslation(String targetCharacters) {
        if (msQwerty.length != targetCharacters.length())
            throw new InvalidParameterException("'targetCharacters' should be the same length as the latin QWERTY keys strings: " + msQwerty.length);
        for (int qwertyIndex = 0; qwertyIndex < msQwerty.length; qwertyIndex++) {
            char latinCharacter = (char) msQwerty[qwertyIndex];
            char otherCharacter = targetCharacters.charAt(qwertyIndex);
            if (otherCharacter > 0) {
                this.addSequence(new int[]{latinCharacter}, otherCharacter);
                this.addSequence(new int[]{KeyCodes.SHIFT, latinCharacter}, Character.toUpperCase(otherCharacter));
            }
        }
    }

    public void addSequence(int[] sequence, int result) {
        this.mCurrentSequence.addSequence(sequence, result);
    }

    public void addShiftSequence(int[] sequence, int result) {
        this.mCurrentSequence.addSpecialKeySequence(sequence, KeyCodes.SHIFT, result);
    }

    public void addAltSequence(int[] sequence, int result) {
        this.mCurrentSequence.addSpecialKeySequence(sequence, KeyCodes.ALT, result);
    }


    private State addNewKey(int currentKeyEvent) {
        //sequence does not live forever!
        //I say, let it live for msSequenceLivingTime milliseconds.
        long currentTime = System.currentTimeMillis();
        if ((currentTime - mLastTypedKeyEventTime) >= AnyApplication.getConfig().getMultiTapTimeout())
            mCurrentSequence.reset();
        mLastTypedKeyEventTime = currentTime;
        return mCurrentSequence.addKeyCode(currentKeyEvent);
    }

    public boolean addSpecialKey(int currentKeyEvent) {
        State result = this.addNewKey(currentKeyEvent);
        return (result != State.RESET);
    }


    public int getCurrentCharacter(int currentKeyEvent, AnySoftKeyboard inputHandler) {
        State result = this.addNewKey(currentKeyEvent);
        if (result == State.FULL_MATCH || result == State.PART_MATCH) {
            int mappedChar = mCurrentSequence.getCharacter();
            final int charactersToDelete = mCurrentSequence.getSequenceLength() - 1;

            if (charactersToDelete > 0)
                inputHandler.deleteLastCharactersFromInput(charactersToDelete);
            return mappedChar;
        }
        return 0;
    }

}
