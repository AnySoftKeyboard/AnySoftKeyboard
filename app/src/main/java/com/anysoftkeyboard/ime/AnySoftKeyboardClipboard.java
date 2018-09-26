package com.anysoftkeyboard.ime;

import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.inputmethod.InputConnection;

import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.devicespecific.Clipboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.f2prateek.rx.preferences2.Preference;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

public abstract class AnySoftKeyboardClipboard extends AnySoftKeyboardSwipeListener {

    private static final int MAX_TIMES_TO_SHOW_LONG_PRESS_TIP = 5;

    private boolean mArrowSelectionState;
    private Preference<Integer> mLongPressPref;
    private Clipboard mClipboard;

    @Override
    public void onCreate() {
        super.onCreate();
        mLongPressPref = prefs().getInteger(R.string.settings_key_clipboard_tip_for_long_press, R.integer.settings_default_zero_value);
        mClipboard = AnyApplication.getDeviceSpecific().createClipboard(getApplicationContext());
    }

    private void showAllClipboardEntries(final Keyboard.Key key) {
        if (mClipboard.getClipboardEntriesCount() == 0) {
            showToastMessage(R.string.clipboard_is_empty_toast, true);
        } else {
            final CharSequence[] entries = new CharSequence[mClipboard.getClipboardEntriesCount()];
            for (int entryIndex = 0; entryIndex < entries.length; entryIndex++) {
                entries[entryIndex] = mClipboard.getText(entryIndex);
            }
            showOptionsDialogWithData(getText(R.string.clipboard_paste_entries_title), R.drawable.ic_clipboard_paste_light,
                    entries, (dialog, which) -> onText(key, entries[which]));
        }
    }

    protected void handleClipboardOperation(final Keyboard.Key key, final int primaryCode, InputConnection ic) {
        switch (primaryCode) {
            case KeyCodes.CLIPBOARD_PASTE:
                CharSequence clipboardText = mClipboard.getClipboardEntriesCount() > 0? mClipboard.getText(0/*last entry paste*/) : "";
                if (!TextUtils.isEmpty(clipboardText)) {
                    onText(key, clipboardText);
                } else {
                    showToastMessage(R.string.clipboard_is_empty_toast, true);
                }
                break;
            case KeyCodes.CLIPBOARD_CUT:
            case KeyCodes.CLIPBOARD_COPY:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && ic != null) {
                    CharSequence selectedText = ic.getSelectedText(InputConnection.GET_TEXT_WITH_STYLES);
                    if (!TextUtils.isEmpty(selectedText)) {
                        mClipboard.setText(selectedText);
                        if (primaryCode == KeyCodes.CLIPBOARD_CUT) {
                            //sending a DEL key will delete the selected text
                            sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
                        } else {
                            //showing toast, since there isn't any other UI feedback
                            final int toastTextToShow;
                            final int timesTipShown = mLongPressPref.get();
                            if (timesTipShown < MAX_TIMES_TO_SHOW_LONG_PRESS_TIP) {
                                toastTextToShow = R.string.clipboard_copy_done_toast_with_long_press_tip;
                                mLongPressPref.set(timesTipShown + 1);
                            } else {
                                toastTextToShow = R.string.clipboard_copy_done_toast;
                            }
                            showToastMessage(toastTextToShow, true);
                        }
                    }
                }
                break;
            case KeyCodes.CLIPBOARD_SELECT_ALL:
                final CharSequence toLeft = ic.getTextBeforeCursor(10240, 0);
                final CharSequence toRight = ic.getTextAfterCursor(10240, 0);
                final int leftLength = toLeft == null ? 0 : toLeft.length();
                final int rightLength = toRight == null ? 0 : toRight.length();
                if (leftLength != 0 || rightLength != 0) {
                    ic.setSelection(0, leftLength + rightLength);
                }
                break;
            case KeyCodes.CLIPBOARD_PASTE_POPUP:
                showAllClipboardEntries(key);
                break;
            case KeyCodes.CLIPBOARD_SELECT:
                mArrowSelectionState = !mArrowSelectionState;
                if (mArrowSelectionState)
                    showToastMessage(R.string.clipboard_fine_select_enabled_toast, true);
                //okay, so they know how to do it...
                mLongPressPref.set(MAX_TIMES_TO_SHOW_LONG_PRESS_TIP);
                break;
            case KeyCodes.UNDO:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    sendDownUpKeyEvents(KeyEvent.KEYCODE_Z, KeyEvent.META_CTRL_ON);
                }
                break;
            case KeyCodes.REDO:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    sendDownUpKeyEvents(KeyEvent.KEYCODE_Z, KeyEvent.META_CTRL_ON | KeyEvent.META_SHIFT_ON);
                }
                break;
            default:
                throw new IllegalArgumentException("The keycode " + primaryCode + " is not covered by handleClipboardOperation!");
        }
    }

    protected boolean handleSelectionExpending(int keyEventKeyCode, InputConnection ic, int globalSelectionStartPosition, int globalCursorPosition) {
        if (mArrowSelectionState && ic != null) {
            switch (keyEventKeyCode) {
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    ic.setSelection(Math.max(0, globalSelectionStartPosition - 1), globalCursorPosition);
                    return true;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    ic.setSelection(globalSelectionStartPosition, globalCursorPosition + 1);
                    return true;
                default:
                    mArrowSelectionState = false;
            }
        }
        return false;
    }

    @RequiresApi(Build.VERSION_CODES.HONEYCOMB)
    public void sendDownUpKeyEvents(int keyEventCode, int metaState) {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;
        long eventTime = SystemClock.uptimeMillis();
        ic.sendKeyEvent(new KeyEvent(eventTime, eventTime,
                KeyEvent.ACTION_DOWN, keyEventCode, 0, metaState, KeyCharacterMap.VIRTUAL_KEYBOARD, 0,
                KeyEvent.FLAG_SOFT_KEYBOARD | KeyEvent.FLAG_KEEP_TOUCH_MODE));
        ic.sendKeyEvent(new KeyEvent(eventTime, SystemClock.uptimeMillis(),
                KeyEvent.ACTION_UP, keyEventCode, 0, metaState, KeyCharacterMap.VIRTUAL_KEYBOARD, 0,
                KeyEvent.FLAG_SOFT_KEYBOARD | KeyEvent.FLAG_KEEP_TOUCH_MODE));
    }

    @Override
    public void onPress(int primaryCode) {
        if (mArrowSelectionState && (primaryCode != KeyCodes.ARROW_LEFT && primaryCode != KeyCodes.ARROW_RIGHT)) {
            mArrowSelectionState = false;
        }
    }
}
