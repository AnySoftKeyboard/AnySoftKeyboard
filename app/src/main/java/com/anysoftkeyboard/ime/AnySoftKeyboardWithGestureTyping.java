package com.anysoftkeyboard.ime;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.view.inputmethod.InputConnection;

import com.anysoftkeyboard.dictionaries.TextEntryState;
import com.anysoftkeyboard.gesturetyping.GestureTypingDebugUtils;
import com.anysoftkeyboard.gesturetyping.GestureTypingDetector;
import com.anysoftkeyboard.gesturetyping.Point;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.menny.android.anysoftkeyboard.R;

import java.util.Collections;
import java.util.List;

public abstract class AnySoftKeyboardWithGestureTyping extends AnySoftKeyboardWithQuickText {

    private boolean mGestureTypingEnabled;

    @Override
    protected void onLoadSettingsRequired(SharedPreferences sharedPreferences) {
        super.onLoadSettingsRequired(sharedPreferences);
        mGestureTypingEnabled = sharedPreferences.getBoolean(getString(R.string.settings_key_gesture_typing),
                getResources().getBoolean(R.bool.settings_default_gesture_typing));
    }

    /**
     * Commits the chosen word to the text field and saves it for later
     * retrieval.
     *
     * @param wordToCommit the suggestion picked by the user to be committed to the text
     *                   field
     * @param correcting this is a correction commit
     */
    protected abstract void commitWordToInput(@NonNull CharSequence wordToCommit, boolean correcting);

    public abstract void setSuggestions(List<? extends CharSequence> suggestions,
                                        boolean completions, boolean typedWordValid,
                                        boolean haveMinimalSuggestion);


    @Override
    public void onGestureTypingInput(final List<Point> gestureInput, final int[] keyCodesInPath, final int keyCodesInPathLength) {
        InputConnection ic = getCurrentInputConnection();

        if (mGestureTypingEnabled && ic != null) {
            if (GestureTypingDebugUtils.DEBUG) {
                GestureTypingDebugUtils.keyCodesInPath = keyCodesInPath;
                GestureTypingDebugUtils.keyCodesInPathLength = keyCodesInPathLength;
                GestureTypingDebugUtils.DEBUG_INPUT.clear();
                // Avoid introducing referencing bugs
                for (Point p : gestureInput)
                    GestureTypingDebugUtils.DEBUG_INPUT.add(new Point(p.x, p.y));
            }

            final boolean isShifted = mShiftKeyState.isActive();
            final boolean isCapsLocked = mShiftKeyState.isLocked();

            List<Keyboard.Key> keys = getCurrentAlphabetKeyboard().getKeys();
            List<CharSequence> wordsInPath = mSuggest.getWordsForPath(isShifted, isCapsLocked,
                    keyCodesInPath, keyCodesInPathLength,
                    GestureTypingDetector.nearbyKeys(keys, gestureInput.get(0)),
                    GestureTypingDetector.nearbyKeys(keys, gestureInput.get(gestureInput.size() - 1)), keys);
            List<Integer> frequenciesInPath = mSuggest.getFrequenciesForPath();
            List<CharSequence> gestureTypingPossibilities = GestureTypingDetector.getGestureWords(gestureInput,
                    wordsInPath, frequenciesInPath, keys);

            if (gestureTypingPossibilities.size() > 0) {
                ic.beginBatchEdit();
                final boolean alsoAddSpace = TextEntryState.getState() == TextEntryState.State.PERFORMED_GESTURE;
                abortCorrectionAndResetPredictionState(false);

                if (alsoAddSpace) {
                    //adding space automatically
                    ic.commitText(" ", 1);
                }

                CharSequence word = gestureTypingPossibilities.get(0);

                mWord.reset();
                mWord.setAutoCapitalized(isShifted);
                mWord.simulateTypedWord(word);
                commitWordToInput(word, false);

                TextEntryState.performedGesture();

                if (gestureTypingPossibilities.size() > 1) {
                    setCandidatesViewShown(true);
                    setSuggestions(gestureTypingPossibilities, false, true, true);
                } else {
                    //clearing any suggestion shown
                    setSuggestions(Collections.<CharSequence>emptyList(), false, false, false);
                }

                ic.endBatchEdit();
            }

            if (GestureTypingDebugUtils.DEBUG) {
                if (!gestureTypingPossibilities.isEmpty())
                    GestureTypingDebugUtils.DEBUG_WORD = gestureTypingPossibilities.get(0);
                else
                    GestureTypingDebugUtils.DEBUG_WORD = "";

                GestureTypingDebugUtils.DEBUG_KEYS = getCurrentAlphabetKeyboard().getKeys();
            }
        }
    }
}
