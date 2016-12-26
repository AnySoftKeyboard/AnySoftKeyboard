package com.anysoftkeyboard.ime;

import android.content.SharedPreferences;

import com.anysoftkeyboard.gesturetyping.GestureTypingDetector;
import com.anysoftkeyboard.utils.Logger;
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

    @Override
    public void onGestureTypingInput(final int[] keyCodesInPath, final int pathLength) {
        if (mGestureTypingEnabled) {
            if (pathLength > 1) {
                List<CharSequence> gestureTypingPossibilities = GestureTypingDetector.getGestureWords(keyCodesInPath, pathLength, this);
                if (gestureTypingPossibilities.size() == 1) {
                    //single possibility, outputting it
                    onText(null/*it's fine, I know this key will not be used*/, gestureTypingPossibilities.get(0));
                } else if (gestureTypingPossibilities.size() > 1){
                    //TODO: show suggestions
                    onText(null/*it's fine, I know this key will not be used*/, gestureTypingPossibilities.get(0));
                }
            }
        }
    }
}
