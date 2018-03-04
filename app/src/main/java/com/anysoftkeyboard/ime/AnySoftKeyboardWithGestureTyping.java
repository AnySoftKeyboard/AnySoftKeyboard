package com.anysoftkeyboard.ime;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
    @Nullable
    private GestureTypingDetector mGestureTypingDetector;

    @Override
    public void onCreate() {
        super.onCreate();
        addDisposable(prefs().getBoolean(R.string.settings_key_gesture_typing, R.bool.settings_default_gesture_typing)
                .asObservable().subscribe(enabled -> {
                    mGestureTypingEnabled = enabled;
                    if (mGestureTypingDetector == null && mGestureTypingEnabled) {
                        mGestureTypingDetector = new GestureTypingDetector();
                        mGestureTypingDetector.loadResources(this);
                    } else if (mGestureTypingDetector != null && !mGestureTypingEnabled) {
                        mGestureTypingDetector.destroy();
                        mGestureTypingDetector = null;
                    }
                }));
    }

    /**
     * FIXME:we only need gesture-typing enabled at alphabet mode.
     */
    private boolean getGestureTypingEnabled() {
        return mGestureTypingEnabled && isInAlphabetKeyboardMode();
    }


    /**
     * When alphabet keyboard loaded, we start loading our getsture-typing word corners data.
     * It is earlier than the first time we click on the keyboard.
     */
    @Override
    public void onAlphabetKeyboardSet(@NonNull AnyKeyboard keyboard) {
        super.onAlphabetKeyboardSet(keyboard);

        if (mGestureTypingEnabled && mGestureTypingDetector != null) {
            mGestureTypingDetector.setKeys(keyboard.getKeys(), this, keyboard.getMinWidth(), keyboard.getHeight());
        }
    }

    public abstract void setSuggestions(List<? extends CharSequence> suggestions,
                                        boolean completions, boolean typedWordValid,
                                        boolean haveMinimalSuggestion);

    @Override
    public boolean isValidGestureTypingStart(int x, int y) {
        if (!getGestureTypingEnabled()) return false;
        mGestureTypingDetector.setKeys(getCurrentAlphabetKeyboard().getKeys(), this,
                getCurrentAlphabetKeyboard().getMinWidth(), getCurrentAlphabetKeyboard().getHeight());

        return mGestureTypingDetector.isValidStartTouch(x, y);
    }

    @Override
    public void onGestureTypingInputStart(int x, int y, long eventTime) {
        if (!getGestureTypingEnabled()) return;
        mGestureTypingDetector.clearGesture();
        mGestureTypingDetector.addPoint(x, y, eventTime);
    }

    @Override
    public void onGestureTypingInput(int x, int y, long eventTime) {
        if (!getGestureTypingEnabled()) return;
        mGestureTypingDetector.addPoint(x, y, eventTime);
    }

    @Override
    public void onGestureTypingInputDone() {
        if (!getGestureTypingEnabled()) return;
        InputConnection ic = getCurrentInputConnection();

        if (getGestureTypingEnabled() && ic != null) {
            ArrayList<String> gestureTypingPossibilities = mGestureTypingDetector.getCandidates();

            final boolean isShifted = mShiftKeyState.isActive();
            final boolean isCapsLocked = mShiftKeyState.isLocked();

            if (isShifted || isCapsLocked) {
                for (int i=0; i<gestureTypingPossibilities.size(); ++i) {
                    String capitalized = gestureTypingPossibilities.get(i).substring(0,1).toUpperCase()
                            + gestureTypingPossibilities.get(i).substring(1);
                    gestureTypingPossibilities.set(i, capitalized);
                }
            }

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
                    setSuggestions(Collections.emptyList(), false, false, false);
                }

                ic.endBatchEdit();
            }

            mGestureTypingDetector.clearGesture();
        }
    }
}
