package com.anysoftkeyboard.ime;

import android.content.DialogInterface;
import android.os.Build;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.inputmethod.InputConnection;

import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.devicespecific.Clipboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

public abstract class AnySoftKeyboardClipboard extends AnySoftKeyboardKeyboardSwitchedListener {
    protected void showAllClipboardEntries(final Keyboard.Key key) {
        Clipboard clipboard = AnyApplication.getFrankenRobot().embody(new Clipboard.ClipboardDiagram(getApplicationContext()));
        if (clipboard.getClipboardEntriesCount() == 0) {
            showToastMessage(R.string.clipboard_is_empty_toast, true);
        } else {
            final CharSequence[] entries = new CharSequence[clipboard.getClipboardEntriesCount()];
            for (int entryIndex = 0; entryIndex < entries.length; entryIndex++) {
                entries[entryIndex] = clipboard.getText(entryIndex);
            }
            showOptionsDialogWithData(getText(R.string.clipboard_paste_entries_title), R.drawable.ic_clipboard_paste_light,
                    entries, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            onText(key, entries[which]);
                        }
                    });
        }
    }

    protected void handleClipboardOperation(final Keyboard.Key key, final int primaryCode) {
        Clipboard clipboard = AnyApplication.getFrankenRobot().embody(new Clipboard.ClipboardDiagram(getApplicationContext()));
        switch (primaryCode) {
            case KeyCodes.CLIPBOARD_PASTE:
                CharSequence clipboardText = clipboard.getText(0/*last entry paste*/);
                if (!TextUtils.isEmpty(clipboardText)) {
                    onText(key, clipboardText);
                } else {
                    showToastMessage(R.string.clipboard_is_empty_toast, true);
                }
                break;
            case KeyCodes.CLIPBOARD_CUT:
            case KeyCodes.CLIPBOARD_COPY:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                    InputConnection ic = getCurrentInputConnection();
                    if (ic != null) {
                        CharSequence selectedText = ic.getSelectedText(InputConnection.GET_TEXT_WITH_STYLES);
                        if (!TextUtils.isEmpty(selectedText)) {
                            clipboard.setText(selectedText);
                            if (primaryCode == KeyCodes.CLIPBOARD_CUT) {
                                //sending a DEL key will delete the selected text
                                sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
                            } else {
                                //showing toast, since there isn't any other UI feedback
                                showToastMessage(R.string.clipboard_copy_done_toast, true);
                            }
                        }
                    }
                }
                break;
        }
    }
}
