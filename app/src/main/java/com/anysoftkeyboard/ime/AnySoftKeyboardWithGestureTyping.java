package com.anysoftkeyboard.ime;

import android.content.SharedPreferences;
import android.view.inputmethod.InputConnection;

import com.anysoftkeyboard.dictionaries.TextEntryState;
import com.anysoftkeyboard.gesturetyping.GestureTypingDebugUtils;
import com.anysoftkeyboard.gesturetyping.GestureTypingDetector;
import com.anysoftkeyboard.gesturetyping.Point;
import com.menny.android.anysoftkeyboard.R;

import java.util.List;

public abstract class AnySoftKeyboardWithGestureTyping extends AnySoftKeyboardWithQuickText {

    private boolean mGestureTypingEnabled;

    @Override
    protected void onLoadSettingsRequired(SharedPreferences sharedPreferences) {
        super.onLoadSettingsRequired(sharedPreferences);
        mGestureTypingEnabled = sharedPreferences.getBoolean(getString(R.string.settings_key_gesture_typing),
                getResources().getBoolean(R.bool.settings_default_gesture_typing));
    }

    public abstract void pickSuggestionManually(int index, CharSequence suggestion);

    public abstract void setSuggestions(List<? extends CharSequence> suggestions,
                                        boolean completions, boolean typedWordValid,
                                        boolean haveMinimalSuggestion);

    @Override
    public void onGestureTypingInput(final List<Point> gestureInput, final int[] keyCodesInPath, final int keyCodesInPathLength) {
        InputConnection ic = getCurrentInputConnection();

        if (mGestureTypingEnabled && ic != null) {
            if (gestureInput.size() > 1) {

                if (GestureTypingDebugUtils.DEBUG) {
                    GestureTypingDebugUtils.DEBUG_INPUT.clear();
                    // Avoid introducing referencing bugs
                    for (Point p : gestureInput) GestureTypingDebugUtils.DEBUG_INPUT.add(new Point(p.x, p.y));
                }

                final boolean isShifted = mShiftKeyState.isActive();
                final boolean isCapsLocked = mShiftKeyState.isLocked();

                List<CharSequence> wordsInPath = mSuggest.getWordsForPath(isShifted, isCapsLocked, keyCodesInPath, keyCodesInPathLength);
                List<String> gestureTypingPossibilities = GestureTypingDetector.getGestureWords(gestureInput, wordsInPath, getCurrentAlphabetKeyboard().getKeys());
                if (gestureTypingPossibilities.size() > 0) {
                    ic.finishComposingText();
                    CharSequence before = ic.getTextBeforeCursor(1, 0);
                    if (before.length() == 1 && before.charAt(0) != ' ') {
                        ic.commitText(" ", 1);
                    }

                    CharSequence word = gestureTypingPossibilities.get(0);
                    if (isShifted) {
                        word = Character.toUpperCase(word.charAt(0)) + "" + word.subSequence(1, word.length());

                        for (int i=0; i<gestureTypingPossibilities.size(); i++) {
                            String w = gestureTypingPossibilities.get(i);
                            w = Character.toUpperCase(w.charAt(0))
                                    + "" + w.subSequence(1, w.length());
                            gestureTypingPossibilities.set(i, w);
                        }
                    }

                    mWord.reset();
                    mWord.setTypedWord(word);
                    mWord.setPreferredWord(word);
                    mWord.setAutoCapitalized(isShifted);
                    mWord.setCursorPosition(mWord.length());
                    ic.setComposingText(mWord.getTypedWord(), 1);

                    if (gestureTypingPossibilities.size() > 1) {
                        setCandidatesViewShown(true);
                        setSuggestions(gestureTypingPossibilities, false, true, true);
                    }
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
}
