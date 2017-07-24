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

package com.anysoftkeyboard.dictionaries;

import com.anysoftkeyboard.utils.Logger;
import com.menny.android.anysoftkeyboard.BuildConfig;

public class TextEntryState {

    private static final boolean DBG = BuildConfig.DEBUG;

    private static final String TAG = "TextEntryState";

    public enum State {
        UNKNOWN,
        START,
        IN_WORD,
        ACCEPTED_DEFAULT,
        PICKED_SUGGESTION,
        PUNCTUATION_AFTER_PICKED,
        PUNCTUATION_AFTER_ACCEPTED,
        SPACE_AFTER_ACCEPTED,
        SPACE_AFTER_PICKED,
        UNDO_COMMIT,
        PICKED_CORRECTION,
        PICKED_TYPED_ADDED_TO_DICTIONARY,
        PERFORMED_GESTURE,
    }

    private static State sState = State.UNKNOWN;
    private static boolean sPredictionOn;

    public static void newSession(boolean withPrediction) {
        restartSession();
        sPredictionOn = withPrediction;
    }

    public static void acceptedDefault(CharSequence typedWord) {
        if (typedWord == null) return;
        sState = State.ACCEPTED_DEFAULT;
        displayState();
    }

    public static void acceptedTyped() {
        sState = State.PICKED_SUGGESTION;
        displayState();
    }

    public static void acceptedSuggestion(CharSequence typedWord, CharSequence actualWord) {
        if (typedWord.equals(actualWord)) {
            acceptedTyped();
        } else {
            sState = State.PICKED_SUGGESTION;
        }
        displayState();
    }

    public static void typedCharacter(char c, boolean isSeparator) {
        final boolean isSpace = c == ' ';

        switch (sState) {
            case IN_WORD:
                if (isSpace || isSeparator) {
                    sState = State.START;
                }/* else State hasn't changed.*/
                break;
            case ACCEPTED_DEFAULT:
                if (isSpace) {
                    sState = State.SPACE_AFTER_ACCEPTED;
                } else if (isSeparator) {
                    sState = State.PUNCTUATION_AFTER_ACCEPTED;
                } else {
                    sState = State.IN_WORD;
                }
                break;
            case PERFORMED_GESTURE:
            case PICKED_SUGGESTION:
            case PICKED_TYPED_ADDED_TO_DICTIONARY:
                if (isSpace) {
                    sState = State.SPACE_AFTER_PICKED;
                } else if (isSeparator) {
                    sState = State.PUNCTUATION_AFTER_PICKED;
                } else {
                    sState = State.IN_WORD;
                }
                break;
            case START:
            case UNKNOWN:
            case SPACE_AFTER_ACCEPTED:
            case SPACE_AFTER_PICKED:
            case PUNCTUATION_AFTER_ACCEPTED:
            case PUNCTUATION_AFTER_PICKED:
                if (!isSpace && !isSeparator) {
                    sState = State.IN_WORD;
                } else {
                    sState = State.START;
                }
                break;
            case UNDO_COMMIT:
                if (isSpace || isSeparator) {
                    sState = State.ACCEPTED_DEFAULT;
                } else {
                    sState = State.IN_WORD;
                }
                break;
        }
        displayState();
    }

    public static boolean willUndoCommitOnBackspace() {
        return getNextStateOnBackSpace(sState).equals(State.UNDO_COMMIT);
    }

    private static State getNextStateOnBackSpace(State currentState) {
        switch (currentState) {
            case PERFORMED_GESTURE:
            case ACCEPTED_DEFAULT:
            case SPACE_AFTER_ACCEPTED:
            case PUNCTUATION_AFTER_ACCEPTED:
                return State.UNDO_COMMIT;
            case PICKED_TYPED_ADDED_TO_DICTIONARY:
            case SPACE_AFTER_PICKED:
            case PICKED_SUGGESTION:
            case PUNCTUATION_AFTER_PICKED:
                return State.UNKNOWN;
            case UNDO_COMMIT:
                return State.IN_WORD;
            default:
                return currentState;
        }
    }

    public static void backspace() {
        sState = getNextStateOnBackSpace(sState);
        displayState();
    }

    public static void performedGesture() {
        sState = State.PERFORMED_GESTURE;
        displayState();
    }

    public static void acceptedSuggestionAddedToDictionary() {
        if (BuildConfig.TESTING_BUILD) {
            if (sState != State.PICKED_SUGGESTION)
                Logger.wtf(TAG, "acceptedSuggestionAddedToDictionary should only be called in a PICKED_SUGGESTION state!");
        }
        sState = State.PICKED_TYPED_ADDED_TO_DICTIONARY;
    }

    public static void restartSession() {
        //prediction flag should stay the same
        sState = State.START;
        displayState();
    }

    public static State getState() {
        if (DBG) {
            Logger.d(TAG, "Returning state = " + sState);
        }
        return sState;
    }

    private static void displayState() {
        if (DBG) {
            Logger.d(TAG, "State = " + sState);
        }
    }

    public static boolean isPredicting() {
        return sPredictionOn && sState == State.IN_WORD;
    }

    public static boolean isReadyToPredict() {
        return sPredictionOn && !isPredicting();
    }
}

