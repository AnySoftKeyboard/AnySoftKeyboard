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

import android.content.Context;
import android.text.format.DateFormat;

import com.anysoftkeyboard.keyboards.Keyboard.Key;
import com.anysoftkeyboard.utils.Log;
import com.menny.android.anysoftkeyboard.BuildConfig;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

public class TextEntryState {

    private static final boolean DBG = BuildConfig.DEBUG;

    private static final String TAG = "TextEntryState";

    private static int sBackspaceCount = 0;

    private static int sAutoSuggestCount = 0;

    private static int sAutoSuggestUndoneCount = 0;

    private static int sManualSuggestCount = 0;

    private static int sWordNotInDictionaryCount = 0;

    private static int sSessionCount = 0;

    private static int sTypedChars;

    private static int sActualChars;

    public enum State {
        UNKNOWN,
        START,
        IN_WORD,
        ACCEPTED_DEFAULT,
        PICKED_SUGGESTION,
        PUNCTUATION_AFTER_WORD,
        PUNCTUATION_AFTER_ACCEPTED,
        SPACE_AFTER_ACCEPTED,
        SPACE_AFTER_PICKED,
        UNDO_COMMIT,
        CORRECTING,
        PICKED_CORRECTION,
        PICKED_TYPED_ADDED_TO_DICTIONARY,
    }

    private static State sState = State.UNKNOWN;

    private static FileOutputStream sKeyLocationFile;
    private static FileOutputStream sUserActionFile;

    public static void newSession(Context context) {
        sSessionCount++;
        sAutoSuggestCount = 0;
        sBackspaceCount = 0;
        sAutoSuggestUndoneCount = 0;
        sManualSuggestCount = 0;
        sWordNotInDictionaryCount = 0;
        sTypedChars = 0;
        sActualChars = 0;
        sState = State.START;

        if (DBG) {
            try {
                endSession();//closing any still(?) open session
                sKeyLocationFile = context.openFileOutput("key.txt", Context.MODE_APPEND);
                sUserActionFile = context.openFileOutput("action.txt", Context.MODE_APPEND);
            } catch (IOException ioe) {
                Log.e("TextEntryState", "Couldn't open file for output: " + ioe);
            }
        }
    }

    public static void endSession() {
        if (sKeyLocationFile == null) {
            return;
        }
        try {
            sKeyLocationFile.close();
            // Write to log file            
            // Write timestamp, settings,
            String out = DateFormat.format("MM:dd hh:mm:ss", Calendar.getInstance().getTime())
                    .toString()
                    + " BS: " + sBackspaceCount
                    + " auto: " + sAutoSuggestCount
                    + " manual: " + sManualSuggestCount
                    + " typed: " + sWordNotInDictionaryCount
                    + " undone: " + sAutoSuggestUndoneCount
                    + " saved: " + ((float) (sActualChars - sTypedChars) / sActualChars)
                    + "\n";
            sUserActionFile.write(out.getBytes());
            sUserActionFile.close();
            sKeyLocationFile = null;
            sUserActionFile = null;
        } catch (IOException ioe) {

        }
    }

    public static void acceptedDefault(CharSequence typedWord, CharSequence actualWord) {
        if (typedWord == null) return;
        if (!typedWord.equals(actualWord)) {
            sAutoSuggestCount++;
        }
        sTypedChars += typedWord.length();
        sActualChars += actualWord.length();
        sState = State.ACCEPTED_DEFAULT;
        //LatinImeLogger.logOnAutoSuggestion(typedWord.toString(), actualWord.toString());
        displayState();
    }

    public static void acceptedTyped(CharSequence typedWord) {
        sWordNotInDictionaryCount++;
        sState = State.PICKED_SUGGESTION;
        displayState();
    }

    public static void acceptedSuggestion(CharSequence typedWord, CharSequence actualWord) {
        sManualSuggestCount++;
        State oldState = sState;
        if (typedWord.equals(actualWord)) {
            acceptedTyped(typedWord);
        }
        if (oldState == State.CORRECTING || oldState == State.PICKED_CORRECTION) {
            sState = State.PICKED_CORRECTION;
        } else {
            sState = State.PICKED_SUGGESTION;
        }
        displayState();
    }

    public static void typedCharacter(char c, boolean isSeparator) {
        boolean isSpace = c == ' ';
        switch (sState) {
            case IN_WORD:
                if (isSpace || isSeparator) {
                    sState = State.START;
                }/* else State hasn't changed.*/
                break;
            case ACCEPTED_DEFAULT:
            case PICKED_SUGGESTION:
            case PICKED_CORRECTION:
            case PICKED_TYPED_ADDED_TO_DICTIONARY:
                if (isSpace) {
                    if (sState == State.ACCEPTED_DEFAULT || sState == State.SPACE_AFTER_PICKED) {
                        sState = State.SPACE_AFTER_ACCEPTED;
                    } else {
                        sState = State.SPACE_AFTER_PICKED;
                    }
                } else if (isSeparator) {
                    sState = State.PUNCTUATION_AFTER_ACCEPTED;
                } else {
                    sState = State.IN_WORD;
                }
                break;
            case START:
            case UNKNOWN:
            case SPACE_AFTER_ACCEPTED:
            case SPACE_AFTER_PICKED:
            case PUNCTUATION_AFTER_ACCEPTED:
            case PUNCTUATION_AFTER_WORD:
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
            case CORRECTING:
                sState = State.START;
                break;
        }
        displayState();
    }

    public static boolean willUndoCommitOnBackspace() {
        switch (sState) {
            case ACCEPTED_DEFAULT:
            case SPACE_AFTER_ACCEPTED:
            case PUNCTUATION_AFTER_ACCEPTED:
            case PICKED_TYPED_ADDED_TO_DICTIONARY:
                return true;
            default:
                return false;
        }
    }

    public static void backspace() {
        switch (sState) {
            case ACCEPTED_DEFAULT:
            case SPACE_AFTER_ACCEPTED:
            case PUNCTUATION_AFTER_ACCEPTED:
                sState = State.UNDO_COMMIT;
                sAutoSuggestUndoneCount++;
                break;
            case PICKED_TYPED_ADDED_TO_DICTIONARY:
                sState = State.UNDO_COMMIT;
                break;
            case SPACE_AFTER_PICKED:
            case PICKED_SUGGESTION:
                sState = State.UNKNOWN;
                break;
            case UNDO_COMMIT:
                sState = State.IN_WORD;
                break;
        }
        sBackspaceCount++;
        displayState();
    }

    public static void acceptedSuggestionAddedToDictionary() {
        if (BuildConfig.TESTING_BUILD) {
            if (sState != State.PICKED_SUGGESTION) Log.wtf(TAG, "acceptedSuggestionAddedToDictionary should only be called in a PICKED_SUGGESTION state!");
        }
        sState = State.PICKED_TYPED_ADDED_TO_DICTIONARY;
    }

    public static void reset() {
        sState = State.START;
        displayState();
    }

    public static State getState() {
        if (DBG) {
            Log.d(TAG, "Returning state = " + sState);
        }
        return sState;
    }

    public static boolean isCorrecting() {
        return sState == State.CORRECTING || sState == State.PICKED_CORRECTION;
    }

    public static void keyPressedAt(Key key, int x, int y) {
        if (DBG && sKeyLocationFile != null && key.getCodeAtIndex(0, false) >= 32) {
            String out =
                    "KEY: " + (char) key.getCodeAtIndex(0, false)
                            + " X: " + x
                            + " Y: " + y
                            + " MX: " + (key.x + key.width / 2)
                            + " MY: " + (key.y + key.height / 2)
                            + "\n";
            try {
                sKeyLocationFile.write(out.getBytes());
            } catch (IOException ioe) {
                // TODO: May run out of space
            }
        }
    }

    private static void displayState() {
        if (DBG) {
            Log.d(TAG, "State = " + sState);
        }
    }
}

