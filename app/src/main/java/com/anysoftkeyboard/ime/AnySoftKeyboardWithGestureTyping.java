package com.anysoftkeyboard.ime;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.view.inputmethod.InputConnection;

import com.anysoftkeyboard.dictionaries.TextEntryState;
import com.anysoftkeyboard.gesturetyping.GestureTypingDetector;
import com.menny.android.anysoftkeyboard.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AnySoftKeyboardWithGestureTyping extends AnySoftKeyboardWithQuickText {

    private boolean mGestureTypingEnabled;
    private GestureTypingDetector mGestureTypingDetector;

    @Override
    protected void onLoadSettingsRequired(SharedPreferences sharedPreferences) {
        super.onLoadSettingsRequired(sharedPreferences);
        mGestureTypingEnabled = sharedPreferences.getBoolean(getString(R.string.settings_key_gesture_typing),
                getResources().getBoolean(R.bool.settings_default_gesture_typing));
    }

    protected abstract void commitWordToInput(@NonNull CharSequence wordToCommit, boolean correcting);

    public abstract void setSuggestions(List<? extends CharSequence> suggestions,
                                        boolean completions, boolean typedWordValid,
                                        boolean haveMinimalSuggestion);

    @Override
    public boolean isValidGestureTypingStart(int x, int y) {
        if (mGestureTypingDetector == null) {
            mGestureTypingDetector = new GestureTypingDetector(getCurrentAlphabetKeyboard().getKeys(), this);
        }
        return mGestureTypingDetector.isValidStartTouch(x,y);
    }

    @Override
    public void onGestureTypingInputStart(int x, int y, long eventTime) {
        mGestureTypingDetector.clearGesture();
        mGestureTypingDetector.addPoint(x,y,eventTime);
    }

    @Override
    public void onGestureTypingInput(int x, int y, long eventTime) {
        mGestureTypingDetector.addPoint(x,y,eventTime);
    }

    @Override
    public void onGestureTypingInputDone() {
        InputConnection ic = getCurrentInputConnection();

        if (mGestureTypingEnabled && ic != null) {
            ArrayList<String> gestureTypingPossibilities = mGestureTypingDetector.getCandidates();

            final boolean isShifted = mShiftKeyState.isActive();
            final boolean isCapsLocked = mShiftKeyState.isLocked();

            if (gestureTypingPossibilities.size() > 0) {
                ic.beginBatchEdit();
                final boolean alsoAddSpace = TextEntryState.getState() == TextEntryState.State.PERFORMED_GESTURE;
                abortCorrectionAndResetPredictionState(false);

                if (alsoAddSpace) {
                    //adding space automatically
                    ic.commitText(" ", 1);
                }

                CharSequence word = gestureTypingPossibilities.get(0);

                // This is used when correcting
                mWord.reset();
                mWord.setAutoCapitalized(isShifted || isCapsLocked);
                mWord.simulateTypedWord(word);

                commitWordToInput(mWord.getTypedWord(), false);

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

            mGestureTypingDetector.clearGesture();
        }
    }
}
