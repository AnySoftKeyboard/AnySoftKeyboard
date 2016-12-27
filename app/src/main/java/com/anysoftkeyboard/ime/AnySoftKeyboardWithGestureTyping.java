package com.anysoftkeyboard.ime;

import android.content.SharedPreferences;
import android.graphics.Canvas;

import com.anysoftkeyboard.gesturetyping.GestureTypingDebugUtils;
import com.anysoftkeyboard.gesturetyping.GestureTypingDetector;
import com.anysoftkeyboard.gesturetyping.Point;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.utils.Logger;
import com.menny.android.anysoftkeyboard.R;

import java.util.ArrayList;
import java.util.List;

public abstract class AnySoftKeyboardWithGestureTyping extends AnySoftKeyboardWithQuickText {

    private boolean mGestureTypingEnabled;

    @Override
    protected void onLoadSettingsRequired(SharedPreferences sharedPreferences) {
        super.onLoadSettingsRequired(sharedPreferences);
        mGestureTypingEnabled = sharedPreferences.getBoolean(getString(R.string.settings_key_gesture_typing),
                getResources().getBoolean(R.bool.settings_default_gesture_typing));
    }

    @Override
    public void onGestureTypingInput(final List<Point> gestureInput, final Keyboard.Key[] keys) {
        if (mGestureTypingEnabled) {
            if (gestureInput.size() > 1) {
                List<? extends CharSequence> gestureTypingPossibilities
                        = GestureTypingDetector.getGestureWords(gestureInput, this, keys);
                if (gestureTypingPossibilities.size() == 1) {
                    //single possibility, outputting it
                    onText(null/*it's fine, I know this key will not be used*/, gestureTypingPossibilities.get(0));
                } else if (gestureTypingPossibilities.size() > 1){
                    //TODO: show suggestions
                    onText(null/*it's fine, I know this key will not be used*/, gestureTypingPossibilities.get(0));
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
