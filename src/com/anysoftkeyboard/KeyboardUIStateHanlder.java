package com.anysoftkeyboard;

import android.os.Handler;
import android.os.Message;
import android.view.inputmethod.InputConnection;

import java.lang.ref.WeakReference;

/**
 * handles all kind of UI thread related operations.
*/
final class KeyboardUIStateHanlder extends Handler {
    public static final int MSG_UPDATE_SUGGESTIONS = 0;
    public static final int MSG_RESTART_NEW_WORD_SUGGESTIONS = 1;
    public static final int MSG_UPDATE_SHIFT_STATE = 3;

    private final WeakReference<AnySoftKeyboard> mKeyboard;

    public KeyboardUIStateHanlder(AnySoftKeyboard keyboard) {
        mKeyboard = new WeakReference<AnySoftKeyboard>(keyboard);
    }

    @Override
    public void handleMessage(Message msg) {
        AnySoftKeyboard ask = mKeyboard.get();
        if (ask == null)// delayed posts and such may result in the
            // reference gone
            return;

        switch (msg.what) {
            case MSG_UPDATE_SUGGESTIONS:
                ask.performUpdateSuggestions();
                break;
            case MSG_RESTART_NEW_WORD_SUGGESTIONS:
                final InputConnection ic = ask.getCurrentInputConnection();
                ask.performRestartWordSuggestion(ic);
                break;
            case MSG_UPDATE_SHIFT_STATE:
                ask.updateShiftKeyState(ask.getCurrentInputEditorInfo());
                break;
            default:
                super.handleMessage(msg);
        }
    }
}
