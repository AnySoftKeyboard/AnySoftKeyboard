package com.anysoftkeyboard.ime;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.view.inputmethod.InputConnection;

import com.anysoftkeyboard.dictionaries.TextEntryState;
import com.anysoftkeyboard.gesturetyping.GestureTypingDetector;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.menny.android.anysoftkeyboard.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AnySoftKeyboardWithGestureTyping extends AnySoftKeyboardWithQuickText {

    private boolean mGestureTypingEnabled;
    private GestureTypingDetector mGestureTypingDetector;

    /**
     * FIXME:we only need gesture-typing enabled at alphabet mode.
     */
    private boolean getGestureTypeingEnabled() {
        return mGestureTypingEnabled && isInAlphabetKeyboardMode();
    }


    /**
     * When alphabet keyboard loaded, we start loading our getsture-typing word corners data.
     * It is earlier than the first time we click on the keyboard.
     * @param keyboard
     */
    @Override
    public void onAlphabetKeyboardSet(@NonNull AnyKeyboard keyboard) {
        super.onAlphabetKeyboardSet(keyboard);

        if (mGestureTypingDetector == null && mGestureTypingEnabled) {
            mGestureTypingDetector = new GestureTypingDetector();
            mGestureTypingDetector.loadResources(this);
        }

        if (mGestureTypingEnabled)
            mGestureTypingDetector.setKeys(getCurrentAlphabetKeyboard().getKeys(), this,
                    getCurrentAlphabetKeyboard().getMinWidth(), getCurrentAlphabetKeyboard().getHeight());
    }

    @Override
    protected void onLoadSettingsRequired(SharedPreferences sharedPreferences) {
        super.onLoadSettingsRequired(sharedPreferences);
        mGestureTypingEnabled = sharedPreferences.getBoolean(getString(R.string.settings_key_gesture_typing),
                getResources().getBoolean(R.bool.settings_default_gesture_typing));

        if (mGestureTypingDetector == null && mGestureTypingEnabled) {
            mGestureTypingDetector = new GestureTypingDetector();
            mGestureTypingDetector.loadResources(this);
        }
    }

    protected abstract void commitWordToInput(@NonNull CharSequence wordToCommit, boolean correcting);

    public abstract void setSuggestions(List<? extends CharSequence> suggestions,
                                        boolean completions, boolean typedWordValid,
                                        boolean haveMinimalSuggestion);

    @Override
    public boolean isValidGestureTypingStart(int x, int y) {
        if (!getGestureTypeingEnabled()) return false;
        mGestureTypingDetector.setKeys(getCurrentAlphabetKeyboard().getKeys(), this,
                getCurrentAlphabetKeyboard().getMinWidth(), getCurrentAlphabetKeyboard().getHeight());

        return mGestureTypingDetector.isValidStartTouch(x, y);
    }

    @Override
    public void onGestureTypingInputStart(int x, int y, long eventTime) {
        if (!getGestureTypeingEnabled()) return;
        mGestureTypingDetector.clearGesture();
        mGestureTypingDetector.addPoint(x, y, eventTime);
    }

    @Override
    public void onGestureTypingInput(int x, int y, long eventTime) {
        if (!getGestureTypeingEnabled()) return;
        mGestureTypingDetector.addPoint(x, y, eventTime);
    }

    @Override
    public void onGestureTypingInputDone() {
        if (!getGestureTypeingEnabled()) return;
        InputConnection ic = getCurrentInputConnection();

        if (getGestureTypeingEnabled() && ic != null) {
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
