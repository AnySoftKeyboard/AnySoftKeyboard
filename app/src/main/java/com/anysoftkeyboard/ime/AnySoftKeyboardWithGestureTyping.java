package com.anysoftkeyboard.ime;

import android.content.SharedPreferences;

import com.anysoftkeyboard.gesturetyping.GestureTypingDebugUtils;
import com.anysoftkeyboard.gesturetyping.GestureTypingDetector;
import com.anysoftkeyboard.gesturetyping.Point;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.menny.android.anysoftkeyboard.R;

import java.util.ArrayList;
import java.util.List;

public abstract class AnySoftKeyboardWithGestureTyping extends AnySoftKeyboardWithQuickText {

    private boolean mGestureTypingEnabled;
    private static final int[] SINGLE_CODE_ARRAY = new int[1];

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
    public void onGestureTypingInput(final List<Point> gestureInput, final Keyboard.Key[] keys) {
        if (mGestureTypingEnabled) {
            if (gestureInput.size() > 1) {
                List<? extends CharSequence> gestureTypingPossibilities
                        = GestureTypingDetector.getGestureWords(gestureInput, this, keys);
                if (gestureTypingPossibilities.size() > 0) {
                    final CharSequence firstWord = gestureTypingPossibilities.get(0);
                    for (int i=0; i<firstWord.length(); i++) {
                        SINGLE_CODE_ARRAY[0] = firstWord.charAt(i);
                        onKey(SINGLE_CODE_ARRAY[0], null, 0, SINGLE_CODE_ARRAY, true);
                    }
                    if (gestureTypingPossibilities.size() == 1) {
                        //single possibility, outputting it
                        pickSuggestionManually(0, firstWord);
                    } else {
                        setSuggestions(gestureTypingPossibilities, false, true, true);
                    }
                }

                if (GestureTypingDebugUtils.DEBUG) {
                    if (!gestureTypingPossibilities.isEmpty())
                        GestureTypingDebugUtils.DEBUG_WORD = gestureTypingPossibilities.get(0);
                    else
                        GestureTypingDebugUtils.DEBUG_WORD = "";

                    GestureTypingDebugUtils.DEBUG_INPUT = new ArrayList<>(gestureInput);
                    GestureTypingDebugUtils.DEBUG_KEYS = keys;
                }
            }
        }
    }
}
