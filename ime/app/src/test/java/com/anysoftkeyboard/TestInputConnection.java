package com.anysoftkeyboard;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.UnderlineSpan;
import android.view.KeyEvent;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.CorrectionInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.common.base.Preconditions;
import org.junit.Assert;
import org.robolectric.Shadows;

public class TestInputConnection extends BaseInputConnection {
    private static final int DELAYED_SELECTION_UPDATE_MSG_ID = 88;

    @NonNull private final AnySoftKeyboard mIme;
    @NonNull private final UnderlineSpan mCurrentComposingSpan = new UnderlineSpan();
    @Nullable private SelectionUpdateData mEditModeInitialState = null;
    @Nullable private SelectionUpdateData mEditModeLatestState = null;
    private int mCursorPosition = 0;
    private int mSelectionEndPosition = 0;
    private int mLastEditorAction = 0;
    private final SpannableStringBuilder mInputText = new SpannableStringBuilder();
    private String mLastCommitCorrection = "";

    private long mDelayedSelectionUpdate = 1L;
    private final Handler mDelayer;

    public TestInputConnection(@NonNull AnySoftKeyboard ime) {
        super(new TextView(ime.getApplicationContext()), false);
        mIme = ime;
        mDelayer =
                new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage(@NonNull Message msg) {
                        if (msg.what == DELAYED_SELECTION_UPDATE_MSG_ID) {
                            final SelectionUpdateData data = (SelectionUpdateData) msg.obj;
                            mIme.onUpdateSelection(
                                    data.oldSelStart,
                                    data.oldSelEnd,
                                    data.newSelStart,
                                    data.newSelEnd,
                                    data.candidatesStart,
                                    data.candidatesEnd);
                        } else {
                            super.handleMessage(msg);
                        }
                    }
                };
    }

    /**
     * Sets the delay of the onUpdateSelection notifications. To simulate actual device behavior, we
     * perform onUpdateSelection events in a MessageHandler.
     *
     * @param delay milliseconds. Must be 1 or larger value.
     */
    public void setUpdateSelectionDelay(long delay) {
        if (delay < 1L) throw new IllegalArgumentException("Delay must be larger than zero.");
        mDelayedSelectionUpdate = delay;
    }

    public void executeOnSelectionUpdateEvent() {
        Shadows.shadowOf(mDelayer.getLooper()).runOneTask();
    }

    @Override
    public CharSequence getTextBeforeCursor(int n, int flags) {
        String unspanned = mInputText.toString();
        int start = Math.max(0, mCursorPosition - n);
        int end = Math.min(mInputText.length(), mCursorPosition);
        return unspanned.substring(start, end);
    }

    @Override
    public CharSequence getTextAfterCursor(int n, int flags) {
        String unspanned = mInputText.toString();
        int start = Math.max(0, mCursorPosition);
        int end = Math.min(mInputText.length(), Math.max(mCursorPosition, mCursorPosition + n));
        return unspanned.substring(start, end);
    }

    @Override
    public CharSequence getSelectedText(int flags) {
        return mInputText.subSequence(mCursorPosition, mSelectionEndPosition);
    }

    @Override
    public int getCursorCapsMode(int reqModes) {
        return 0;
    }

    @Override
    public ExtractedText getExtractedText(ExtractedTextRequest request, int flags) {
        ExtractedText extracted = new ExtractedText();
        extracted.startOffset = 0;
        extracted.text = mInputText.subSequence(0, mInputText.length());
        extracted.selectionStart = mCursorPosition;
        extracted.selectionEnd = mSelectionEndPosition;

        return extracted;
    }

    @Override
    public boolean deleteSurroundingText(int beforeLength, int afterLength) {
        if (beforeLength == 0 && afterLength == 0) return true;

        final int deleteStart = Math.max(mCursorPosition - beforeLength, 0);
        final int deleteEnd =
                Math.max(0, Math.min(mCursorPosition + afterLength, mInputText.length()));
        mInputText.delete(deleteStart, deleteEnd);
        final int cursorDelta = mCursorPosition - deleteStart;
        notifyTextChange(-cursorDelta);
        return true;
    }

    private void notifyTextChange(int cursorDelta) {
        final int oldPosition = mCursorPosition;
        final int oldEndSelection = mSelectionEndPosition;
        mCursorPosition += cursorDelta;
        // cursor moved? so selection is cleared
        mSelectionEndPosition = cursorDelta == 0 ? mSelectionEndPosition : mCursorPosition;
        notifyTextChanged(oldPosition, oldEndSelection, mCursorPosition, mSelectionEndPosition);
    }

    private void notifyTextChanged(int oldStart, int oldEnd, int newStart, int newEnd) {
        Assert.assertTrue(oldStart >= 0);
        Assert.assertTrue(oldEnd >= 0);
        Assert.assertTrue(oldEnd >= oldStart);
        Assert.assertTrue(newStart >= 0);
        Assert.assertTrue(newEnd >= 0);
        Assert.assertTrue(newEnd >= newStart);
        int[] composedTextRange = findComposedText();
        final SelectionUpdateData data =
                new SelectionUpdateData(
                        oldStart,
                        oldEnd,
                        newStart,
                        newEnd,
                        composedTextRange[0],
                        composedTextRange[1]);
        if (mEditModeInitialState != null) {
            mEditModeLatestState = data;
        } else {
            mDelayer.sendMessageDelayed(
                    mDelayer.obtainMessage(DELAYED_SELECTION_UPDATE_MSG_ID, data),
                    mDelayedSelectionUpdate);
        }
    }

    @Override
    public boolean setComposingText(CharSequence text, int newCursorPosition) {
        commitTextAs(text, true, newCursorPosition);
        return true;
    }

    private void commitTextAs(
            final CharSequence text, final boolean asComposing, final int newCursorPosition) {
        Preconditions.checkNotNull(text);
        int[] composedTextRange;
        if (mCursorPosition != mSelectionEndPosition) {
            composedTextRange = new int[] {mCursorPosition, mSelectionEndPosition};
        } else {
            composedTextRange = Preconditions.checkNotNull(findComposedText());
        }

        final int cursorPositionAfterText;
        if (newCursorPosition <= 0) {
            cursorPositionAfterText = composedTextRange[0] + newCursorPosition;
        } else {
            cursorPositionAfterText = composedTextRange[0] + text.length() + newCursorPosition - 1;
        }

        mInputText.delete(composedTextRange[0], composedTextRange[1]);
        mInputText.clearSpans();
        mInputText.insert(composedTextRange[0], asComposing ? asComposeText(text) : text);

        notifyTextChange(cursorPositionAfterText - mCursorPosition);
    }

    private int[] findComposedText() {
        int start = mInputText.getSpanStart(mCurrentComposingSpan);
        int end = mInputText.getSpanEnd(mCurrentComposingSpan);
        if (start == -1) {
            return new int[] {mCursorPosition, mCursorPosition};
        } else {
            return new int[] {start, end};
        }
    }

    private CharSequence asComposeText(CharSequence text) {
        SpannableString composed = new SpannableString(text);
        composed.setSpan(mCurrentComposingSpan, 0, text.length(), 0);
        return composed;
    }

    @Override
    public boolean setComposingRegion(int start, int end) {
        mInputText.clearSpans();
        mInputText.setSpan(mCurrentComposingSpan, start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        notifyTextChange(0);
        return true;
    }

    @Override
    public boolean finishComposingText() {
        mInputText.clearSpans();
        notifyTextChange(0);
        return true;
    }

    @Override
    public boolean commitText(CharSequence text, int newCursorPosition) {
        commitTextAs(text, false, newCursorPosition);
        return true;
    }

    @Override
    public boolean commitCompletion(CompletionInfo text) {
        return false;
    }

    @Override
    public boolean commitCorrection(CorrectionInfo correctionInfo) {
        mLastCommitCorrection = correctionInfo.getNewText().toString();
        return true;
    }

    public String getLastCommitCorrection() {
        return mLastCommitCorrection;
    }

    @Override
    public boolean setSelection(int start, int end) {
        if (start == end && start == mCursorPosition) return true;

        final int len = mInputText.length();
        if (start < 0 || end < 0 || start > len || end > len) return true;

        int oldStart = mCursorPosition;
        int oldEnd = mSelectionEndPosition;
        mCursorPosition = start;
        mSelectionEndPosition = Math.min(end, mInputText.length());
        notifyTextChanged(oldStart, oldEnd, mCursorPosition, mSelectionEndPosition);
        return true;
    }

    @Override
    public boolean performEditorAction(int editorAction) {
        mLastEditorAction = editorAction;
        return false;
    }

    public int getLastEditorAction() {
        return mLastEditorAction;
    }

    @Override
    public boolean performContextMenuAction(int id) {
        return false;
    }

    @Override
    public boolean beginBatchEdit() {
        if (mEditModeInitialState == null) {
            int[] composedTextRange = findComposedText();
            mEditModeInitialState =
                    new SelectionUpdateData(
                            mCursorPosition,
                            mSelectionEndPosition,
                            mCursorPosition,
                            mSelectionEndPosition,
                            composedTextRange[0],
                            composedTextRange[1]);
            mEditModeLatestState = mEditModeInitialState;
        }
        return true;
    }

    @Override
    public boolean endBatchEdit() {
        final SelectionUpdateData initialState = mEditModeInitialState;
        final SelectionUpdateData finalState = mEditModeLatestState;
        mEditModeInitialState = null;
        mEditModeLatestState = null;
        if (initialState != null) {
            if (!initialState.equals(finalState)) {
                notifyTextChanged(
                        initialState.oldSelStart,
                        initialState.oldSelEnd,
                        finalState.newSelStart,
                        finalState.newSelEnd);
            }
        }
        return true;
    }

    @Override
    public boolean sendKeyEvent(KeyEvent event) {
        /*
        ic.sendKeyEvent(new KeyEvent(eventTime, eventTime,
                KeyEvent.ACTION_DOWN, keyEventCode, 0, 0, KeyCharacterMap.VIRTUAL_KEYBOARD, 0,
                KeyEvent.FLAG_SOFT_KEYBOARD|KeyEvent.FLAG_KEEP_TOUCH_MODE));
        ic.sendKeyEvent(new KeyEvent(eventTime, SystemClock.uptimeMillis(),
                KeyEvent.ACTION_UP, keyEventCode, 0, 0, KeyCharacterMap.VIRTUAL_KEYBOARD, 0,
                KeyEvent.FLAG_SOFT_KEYBOARD|KeyEvent.FLAG_KEEP_TOUCH_MODE));
         */
        boolean handled = false;
        if (event.getAction() == KeyEvent.ACTION_UP) {
            // only handling UP events
            if (event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
                if (mSelectionEndPosition == mCursorPosition) {
                    handled = true;
                    deleteSurroundingText(1, 0);
                } else {
                    handled = true;
                    mInputText.delete(mCursorPosition, mSelectionEndPosition);
                    notifyTextChange(0);
                }
            } else if (event.getKeyCode() == KeyEvent.KEYCODE_FORWARD_DEL) {
                if (mSelectionEndPosition == mCursorPosition) {
                    handled = true;
                    deleteSurroundingText(0, 1);
                } else {
                    handled = true;
                    mInputText.delete(mCursorPosition, mSelectionEndPosition);
                    notifyTextChange(0);
                }
            } else if (event.getKeyCode() == KeyEvent.KEYCODE_SPACE) {
                handled = true;
                commitText(" ", 1);
            } else if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                handled = true;
                commitText("\n", 1);
            } else if (event.getKeyCode() >= KeyEvent.KEYCODE_0
                    || event.getKeyCode() <= KeyEvent.KEYCODE_9) {
                handled = true;
                commitText(Integer.toString(event.getKeyCode() - KeyEvent.KEYCODE_0), 1);
            } else if (event.getKeyCode() >= KeyEvent.KEYCODE_A
                    || event.getKeyCode() <= KeyEvent.KEYCODE_Z) {
                handled = true;
                commitText("" + (char) (event.getKeyCode() - KeyEvent.KEYCODE_A + 'a'), 1);
            }
        }

        if (!handled) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                mIme.onKeyDown(event.getKeyCode(), event);
            } else {
                mIme.onKeyUp(event.getKeyCode(), event);
            }
        }
        return true;
    }

    @Override
    public boolean clearMetaKeyStates(int states) {
        return true;
    }

    @Override
    public boolean reportFullscreenMode(boolean enabled) {
        return false;
    }

    @Override
    public boolean performPrivateCommand(String action, Bundle data) {
        return false;
    }

    @Override
    public boolean requestCursorUpdates(int cursorUpdateMode) {
        return false;
    }

    @NonNull
    public String getCurrentTextInInputConnection() {
        return mInputText.toString();
    }

    public int getCurrentStartPosition() {
        return mCursorPosition;
    }

    private static class SelectionUpdateData {
        final int oldSelStart;
        final int oldSelEnd;
        final int newSelStart;
        final int newSelEnd;
        final int candidatesStart;
        final int candidatesEnd;

        private SelectionUpdateData(
                int oldSelStart,
                int oldSelEnd,
                int newSelStart,
                int newSelEnd,
                int candidatesStart,
                int candidatesEnd) {
            this.oldSelStart = oldSelStart;
            this.oldSelEnd = oldSelEnd;
            this.newSelStart = newSelStart;
            this.newSelEnd = newSelEnd;
            this.candidatesStart = candidatesStart;
            this.candidatesEnd = candidatesEnd;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SelectionUpdateData)) return false;

            SelectionUpdateData that = (SelectionUpdateData) o;

            if (oldSelStart != that.oldSelStart) return false;
            if (oldSelEnd != that.oldSelEnd) return false;
            if (newSelStart != that.newSelStart) return false;
            if (newSelEnd != that.newSelEnd) return false;
            if (candidatesStart != that.candidatesStart) return false;
            return candidatesEnd == that.candidatesEnd;
        }

        @Override
        public int hashCode() {
            int result = oldSelStart;
            result = 31 * result + oldSelEnd;
            result = 31 * result + newSelStart;
            result = 31 * result + newSelEnd;
            result = 31 * result + candidatesStart;
            result = 31 * result + candidatesEnd;
            return result;
        }
    }
}
