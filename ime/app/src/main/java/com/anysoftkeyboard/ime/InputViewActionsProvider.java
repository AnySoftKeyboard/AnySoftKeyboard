package com.anysoftkeyboard.ime;

import com.anysoftkeyboard.keyboards.views.OnKeyboardActionListener;

public interface InputViewActionsProvider {

    /** Sets the listener of actions taken on this {@link InputViewActionsProvider}. */
    void setOnKeyboardActionListener(OnKeyboardActionListener keyboardActionListener);
}
