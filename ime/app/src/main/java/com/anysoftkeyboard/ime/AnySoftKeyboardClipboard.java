package com.anysoftkeyboard.ime;

import android.content.Context;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.VisibleForTesting;
import android.text.InputType;
import android.text.TextUtils;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.TextView;
import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.devicespecific.Clipboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.views.KeyboardViewContainerView;
import com.anysoftkeyboard.rx.GenericOnError;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

public abstract class AnySoftKeyboardClipboard extends AnySoftKeyboardSwipeListener {

    private boolean mArrowSelectionState;
    private Clipboard mClipboard;
    protected static final int MAX_CHARS_PER_CODE_POINT = 2;
    private static final long MAX_TIME_TO_SHOW_SYNCED_CLIPBOARD_ENTRY = 15 * 1000;
    private long mLastSyncedClipboardEntryTime = Long.MIN_VALUE;
    @Nullable private CharSequence mLastSyncedClipboardEntry;

    @VisibleForTesting
    protected interface ClipboardActionOwner {
        @NonNull
        Context getContext();

        void outputClipboardText(@NonNull CharSequence text);
    }

    @VisibleForTesting
    protected final ClipboardActionOwner mClipboardActionOwnerImpl =
            new ClipboardActionOwner() {
                @NonNull
                @Override
                public Context getContext() {
                    return AnySoftKeyboardClipboard.this;
                }

                @Override
                public void outputClipboardText(@NonNull CharSequence text) {
                    AnySoftKeyboardClipboard.this.onText(null, text);
                    AnySoftKeyboardClipboard.this
                            .getInputViewContainer()
                            .removeStripAction(mSuggestionClipboardEntry);
                }
            };

    @VisibleForTesting
    protected static class ClipboardStripActionProvider
            implements KeyboardViewContainerView.StripActionProvider {
        private final ClipboardActionOwner mOwner;
        @Nullable private CharSequence mEntryText;
        @Nullable private TextView mClipboardText;

        ClipboardStripActionProvider(@NonNull ClipboardActionOwner owner) {
            mOwner = owner;
        }

        @Override
        public View inflateActionView(ViewGroup parent) {
            final View rootView =
                    LayoutInflater.from(mOwner.getContext())
                            .inflate(R.layout.clipboard_suggestion_action, parent, false);
            mClipboardText = rootView.findViewById(R.id.clipboard_suggestion_text);
            rootView.setOnClickListener(
                    view -> {
                        final TextView clipboardText = mClipboardText;
                        if (clipboardText != null) {
                            mOwner.outputClipboardText(mEntryText);
                        }
                    });

            return rootView;
        }

        @Override
        public void onRemoved() {
            mClipboardText = null;
        }

        boolean isVisible() {
            return mClipboardText != null;
        }

        void setClipboardText(CharSequence text, boolean isSecured) {
            mEntryText = text;
            mClipboardText.setSelected(true);
            if (isSecured) mClipboardText.setText("**********");
            else mClipboardText.setText(text);
        }
    }

    @VisibleForTesting protected ClipboardStripActionProvider mSuggestionClipboardEntry;

    @Override
    public void onCreate() {
        super.onCreate();
        mClipboard = AnyApplication.getDeviceSpecific().createClipboard(getApplicationContext());
        mSuggestionClipboardEntry = new ClipboardStripActionProvider(mClipboardActionOwnerImpl);
        addDisposable(
                prefs().getBoolean(
                                R.string.settings_key_os_clipboard_sync,
                                R.bool.settings_default_os_clipboard_sync)
                        .asObservable()
                        .subscribe(
                                syncClipboard -> {
                                    mLastSyncedClipboardEntryTime = Long.MIN_VALUE;
                                    mClipboard.setClipboardUpdatedListener(
                                            syncClipboard ? this::onClipboardEntryAdded : null);
                                },
                                GenericOnError.onError("settings_key_os_clipboard_sync")));
    }

    private void onClipboardEntryAdded(CharSequence clipboardEntry) {
        mLastSyncedClipboardEntry = clipboardEntry;
        mLastSyncedClipboardEntryTime = SystemClock.uptimeMillis();
    }

    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
        super.onStartInputView(info, restarting);
        if (mLastSyncedClipboardEntryTime + MAX_TIME_TO_SHOW_SYNCED_CLIPBOARD_ENTRY
                        >= SystemClock.uptimeMillis()
                && !TextUtils.isEmpty(mLastSyncedClipboardEntry)) {
            getInputViewContainer().addStripAction(mSuggestionClipboardEntry);
            getInputViewContainer().setActionsStripVisibility(true);
            final int variation = (info.inputType & InputType.TYPE_MASK_VARIATION);

            mSuggestionClipboardEntry.setClipboardText(
                    mLastSyncedClipboardEntry,
                    (variation & InputType.TYPE_TEXT_VARIATION_PASSWORD)
                                    == InputType.TYPE_TEXT_VARIATION_PASSWORD
                            || (variation & InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
                                    == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                            ||
                            // InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD
                            (variation & 0x000000e0) == 0x000000e0);
        }
    }

    @Override
    public void onKey(
            int primaryCode,
            Keyboard.Key key,
            int multiTapIndex,
            int[] nearByKeyCodes,
            boolean fromUI) {
        if (mSuggestionClipboardEntry.isVisible()) {
            getInputViewContainer().removeStripAction(mSuggestionClipboardEntry);
        }
        super.onKey(primaryCode, key, multiTapIndex, nearByKeyCodes, fromUI);
    }

    @Override
    public void onFinishInputView(boolean finishingInput) {
        super.onFinishInputView(finishingInput);
        getInputViewContainer().removeStripAction(mSuggestionClipboardEntry);
    }

    private void showAllClipboardEntries(Keyboard.Key key) {
        if (mClipboard.getClipboardEntriesCount() == 0) {
            showToastMessage(R.string.clipboard_is_empty_toast, true);
        } else {
            final CharSequence[] entries = new CharSequence[mClipboard.getClipboardEntriesCount()];
            for (int entryIndex = 0; entryIndex < entries.length; entryIndex++) {
                entries[entryIndex] = mClipboard.getText(entryIndex);
            }
            showOptionsDialogWithData(
                    R.string.clipboard_paste_entries_title,
                    R.drawable.ic_clipboard_paste_light,
                    entries,
                    (dialog, which) -> onText(key, entries[which]));
        }
    }

    protected void handleClipboardOperation(
            final Keyboard.Key key, final int primaryCode, InputConnection ic) {
        switch (primaryCode) {
            case KeyCodes.CLIPBOARD_PASTE:
                CharSequence clipboardText =
                        mClipboard.getClipboardEntriesCount() > 0
                                ? mClipboard.getText(0 /*last entry paste*/)
                                : "";
                if (!TextUtils.isEmpty(clipboardText)) {
                    onText(null, clipboardText);
                } else {
                    showToastMessage(R.string.clipboard_is_empty_toast, true);
                }
                break;
            case KeyCodes.CLIPBOARD_CUT:
            case KeyCodes.CLIPBOARD_COPY:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && ic != null) {
                    CharSequence selectedText =
                            ic.getSelectedText(InputConnection.GET_TEXT_WITH_STYLES);
                    if (!TextUtils.isEmpty(selectedText)) {
                        mClipboard.setText(selectedText);
                        if (primaryCode == KeyCodes.CLIPBOARD_CUT) {
                            // sending a DEL key will delete the selected text
                            sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
                        } else {
                            // showing toast, since there isn't any other UI feedback
                            showToastMessage(R.string.clipboard_copy_done_toast, true);
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
                if (mArrowSelectionState) {
                    showToastMessage(R.string.clipboard_fine_select_enabled_toast, true);
                }
                break;
            case KeyCodes.UNDO:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    sendDownUpKeyEvents(KeyEvent.KEYCODE_Z, KeyEvent.META_CTRL_ON);
                }
                break;
            case KeyCodes.REDO:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    sendDownUpKeyEvents(
                            KeyEvent.KEYCODE_Z, KeyEvent.META_CTRL_ON | KeyEvent.META_SHIFT_ON);
                }
                break;
            default:
                throw new IllegalArgumentException(
                        "The keycode "
                                + primaryCode
                                + " is not covered by handleClipboardOperation!");
        }
    }

    protected boolean handleSelectionExpending(
            int keyEventKeyCode,
            InputConnection ic,
            int globalSelectionStartPosition,
            int globalCursorPosition) {
        if (mArrowSelectionState && ic != null) {
            switch (keyEventKeyCode) {
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    // A Unicode codepoint can be made up of two Java chars.
                    // We check if that's what happening before the cursor:
                    final String toLeft =
                            ic.getTextBeforeCursor(MAX_CHARS_PER_CODE_POINT, 0).toString();
                    if (toLeft.length() == 0) {
                        ic.setSelection(globalSelectionStartPosition, globalCursorPosition);
                    } else {
                        ic.setSelection(
                                globalSelectionStartPosition
                                        - Character.charCount(
                                                toLeft.codePointBefore(toLeft.length())),
                                globalCursorPosition);
                    }
                    return true;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    final String toRight =
                            ic.getTextAfterCursor(MAX_CHARS_PER_CODE_POINT, 0).toString();
                    if (toRight.length() == 0) {
                        ic.setSelection(globalSelectionStartPosition, globalCursorPosition);
                    } else {
                        ic.setSelection(
                                globalSelectionStartPosition,
                                globalCursorPosition + Character.charCount(toRight.codePointAt(0)));
                    }
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
        ic.sendKeyEvent(
                new KeyEvent(
                        eventTime,
                        eventTime,
                        KeyEvent.ACTION_DOWN,
                        keyEventCode,
                        0,
                        metaState,
                        KeyCharacterMap.VIRTUAL_KEYBOARD,
                        0,
                        KeyEvent.FLAG_SOFT_KEYBOARD | KeyEvent.FLAG_KEEP_TOUCH_MODE));
        ic.sendKeyEvent(
                new KeyEvent(
                        eventTime,
                        SystemClock.uptimeMillis(),
                        KeyEvent.ACTION_UP,
                        keyEventCode,
                        0,
                        metaState,
                        KeyCharacterMap.VIRTUAL_KEYBOARD,
                        0,
                        KeyEvent.FLAG_SOFT_KEYBOARD | KeyEvent.FLAG_KEEP_TOUCH_MODE));
    }

    @Override
    public void onPress(int primaryCode) {
        if (mArrowSelectionState
                && (primaryCode != KeyCodes.ARROW_LEFT && primaryCode != KeyCodes.ARROW_RIGHT)) {
            mArrowSelectionState = false;
        }
    }
}
