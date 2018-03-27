package com.anysoftkeyboard.ime;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.view.inputmethod.InputConnection;

import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.dictionaries.TextEntryState;
import com.anysoftkeyboard.gesturetyping.GestureTypingDetector;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
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
                        mGestureTypingDetector = createGestureTypingDetector();
                        mGestureTypingDetector.loadResources(this);

                        final AnyKeyboard currentAlphabetKeyboard = getCurrentAlphabetKeyboard();
                        //it might be null if the IME service started with enabled flag set to true. In that case
                        //the keyboard object will not be ready yet.
                        if (currentAlphabetKeyboard != null) {
                            mGestureTypingDetector.setKeys(currentAlphabetKeyboard.getKeys(),
                                    currentAlphabetKeyboard.getMinWidth(), currentAlphabetKeyboard.getHeight());
                        }
                    } else if (mGestureTypingDetector != null && !mGestureTypingEnabled) {
                        mGestureTypingDetector.destroy();
                        mGestureTypingDetector = null;
                    }
                }));
    }

    @NonNull
    @VisibleForTesting
    protected GestureTypingDetector createGestureTypingDetector() {
        return new GestureTypingDetector();
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
            mGestureTypingDetector.setKeys(keyboard.getKeys(), keyboard.getMinWidth(), keyboard.getHeight());
        }
    }

    public abstract void setSuggestions(List<? extends CharSequence> suggestions,
            boolean completions, boolean typedWordValid,
            boolean haveMinimalSuggestion);

    @Override
    public boolean isValidGestureTypingStart(int x, int y) {
        if (!getGestureTypingEnabled()) return false;

        return mGestureTypingDetector.isValidStartTouch(x, y);
    }

    @Override
    public void onGestureTypingInputStart(int x, int y, long eventTime) {
        if (!getGestureTypingEnabled()) return;
        //we can call this as many times as we want, it has a short-circuit check.
        setCandidatesViewShown(true/*we need candidates-view to be shown, since we are going to show suggestions*/);

        confirmLastGesture(mPrefsAutoSpace);

        mGestureTypingDetector.clearGesture();
        mGestureTypingDetector.addPoint(x, y, eventTime);
    }

    @Override
    public void onGestureTypingInput(int x, int y, long eventTime) {
        if (!getGestureTypingEnabled()) return;
        mGestureTypingDetector.addPoint(x, y, eventTime);
    }

    @Override
    public void onKey(int primaryCode, Keyboard.Key key, int multiTapIndex, int[] nearByKeyCodes, boolean fromUI) {
        if (getGestureTypingEnabled() && TextEntryState.getState() == TextEntryState.State.PERFORMED_GESTURE) {
            if (primaryCode > 0 /*printable character*/) {
                confirmLastGesture(primaryCode != KeyCodes.SPACE && mPrefsAutoSpace);
            }
        }

        super.onKey(primaryCode, key, multiTapIndex, nearByKeyCodes, fromUI);
    }

    private void confirmLastGesture(boolean withAutoSpace) {
        if (TextEntryState.getState() == TextEntryState.State.PERFORMED_GESTURE) {
            pickSuggestionManually(0, mWord.getTypedWord(), withAutoSpace);
        }
    }

    @Override
    public void onGestureTypingInputDone() {
        if (!getGestureTypingEnabled()) return;

        InputConnection ic = getCurrentInputConnection();

        if (ic != null) {
            ArrayList<CharSequence> gestureTypingPossibilities = mGestureTypingDetector.getCandidates();

            if (!gestureTypingPossibilities.isEmpty()) {
                final boolean isShifted = mShiftKeyState.isActive();
                final boolean isCapsLocked = mShiftKeyState.isLocked();

                final Locale locale = getCurrentAlphabetKeyboard().getLocale();
                if (locale != null && (isShifted || isCapsLocked)) {

                    StringBuilder builder = new StringBuilder();
                    for (int i = 0; i < gestureTypingPossibilities.size(); ++i) {
                        final CharSequence word = gestureTypingPossibilities.get(i);
                        if (isCapsLocked) {
                            gestureTypingPossibilities.set(i, word.toString().toUpperCase(locale));
                        } else {
                            builder.append(word.subSequence(0, 1).toString().toUpperCase(locale));
                            builder.append(word.subSequence(1, word.length()));
                            gestureTypingPossibilities.set(i, builder.toString());
                            builder.setLength(0);
                        }
                    }
                }

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
