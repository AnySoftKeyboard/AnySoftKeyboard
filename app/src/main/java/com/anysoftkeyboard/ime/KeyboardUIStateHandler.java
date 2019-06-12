package com.anysoftkeyboard.ime;

import android.os.Handler;
import android.os.Message;
import android.view.inputmethod.InputConnection;

import com.menny.android.anysoftkeyboard.R;

import java.lang.ref.WeakReference;

/**
 * handles all kind of UI thread related operations.
 */
public final class KeyboardUIStateHandler extends Handler {
    public static final int MSG_UPDATE_SUGGESTIONS = R.id.keyboard_ui_handler_MSG_UPDATE_SUGGESTIONS;
    public static final int MSG_RESTART_NEW_WORD_SUGGESTIONS = R.id.keyboard_ui_handler_MSG_RESTART_NEW_WORD_SUGGESTIONS;
    public static final int MSG_CLOSE_DICTIONARIES = R.id.keyboard_ui_handler_MSG_CLOSE_DICTIONARIES;

    private final WeakReference<AnySoftKeyboardSuggestions> mKeyboard;

    public KeyboardUIStateHandler(AnySoftKeyboardSuggestions keyboard) {
        mKeyboard = new WeakReference<>(keyboard);
    }

    public void removeAllSuggestionMessages() {
        removeMessages(MSG_UPDATE_SUGGESTIONS);
        removeMessages(MSG_RESTART_NEW_WORD_SUGGESTIONS);
    }

    public void removeAllMessages() {
        removeAllSuggestionMessages();
        removeMessages(MSG_CLOSE_DICTIONARIES);
    }

    @Override
    public void handleMessage(Message msg) {
        AnySoftKeyboardSuggestions ask = mKeyboard.get();

        if (ask == null) {
            // delayed posts and such may result in the reference gone
            return;
        }
        final InputConnection ic = ask.getCurrentInputConnection();

        switch (msg.what) {
            case MSG_UPDATE_SUGGESTIONS:
                ask.performUpdateSuggestions();
                break;
            case MSG_RESTART_NEW_WORD_SUGGESTIONS:
                ask.performRestartWordSuggestion(ic);
                break;
            case MSG_CLOSE_DICTIONARIES:
                ask.closeDictionaries();
                break;
            default:
                super.handleMessage(msg);
        }
    }
}
