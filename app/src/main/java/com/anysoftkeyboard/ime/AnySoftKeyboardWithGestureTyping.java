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
import java.util.Locale;

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

    public abstract void pickLastSuggestion();

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
        pickLastSuggestion();

        InputConnection ic = getCurrentInputConnection();

        if (getGestureTypingEnabled() && ic != null) {
            ArrayList<String> gestureTypingPossibilities = mGestureTypingDetector.getCandidates();

            final boolean isShifted = mShiftKeyState.isActive();
            final boolean isCapsLocked = mShiftKeyState.isLocked();

            final Locale locale = getCurrentAlphabetKeyboard().getLocale();
            if (locale != null && (isShifted || isCapsLocked)) {

                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < gestureTypingPossibilities.size(); ++i) {
                    final String word = gestureTypingPossibilities.get(i);
                    if (isCapsLocked) {
                        gestureTypingPossibilities.set(i, word.toUpperCase(locale));
                    } else {
                        builder.append(word.substring(0, 1).toUpperCase(locale));
                        builder.append(word.substring(1));
                        gestureTypingPossibilities.set(i, builder.toString());
                        builder.setLength(0);
                    }
                }
            }

            if (gestureTypingPossibilities.size() > 0) {
                ic.beginBatchEdit();
                abortCorrectionAndResetPredictionState(false);

                CharSequence word = gestureTypingPossibilities.get(0);

                // This is used when correcting
                mWord.reset();
                mWord.setAutoCapitalized(isShifted || isCapsLocked);
                mWord.simulateTypedWord(word);

                mWord.setPreferredWord(mWord.getTypedWord());
                ic.setComposingText(mWord.getTypedWord(), 1);

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

    @Override
    public boolean isPerformingGesture() {
        return getGestureTypingEnabled() && mGestureTypingDetector.isPerformingGesture();

    }
}
