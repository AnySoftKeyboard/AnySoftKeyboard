package com.anysoftkeyboard.gesturetyping;

import com.anysoftkeyboard.keyboards.Keyboard;

public class GestureTypingUtils {

    /**
     * Did we come close enough to a normal (alphabet) character for this
     * to be considered the start of a gesture?
     */
    public static boolean isValidStartTouch(Keyboard.Key key) {
        return key != null && key.getCodesCount() > 0 && Character.isLetter(key.getPrimaryCode());
    }
}
