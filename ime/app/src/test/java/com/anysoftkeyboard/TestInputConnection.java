package com.anysoftkeyboard;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;

public class TestInputConnection extends BaseInputConnection {

    @NonNull private final AnySoftKeyboard mIme;
    @NonNull private final UnderlineSpan mCurrentComposingSpan = new UnderlineSpan();
    private boolean mSendUpdates = true;
    private boolean mCongested = false;
    private final List<Runnable> mCongestedActions = new ArrayList<>();
    private boolean mInEditMode = false;
    private boolean mChangesWhileInEdit = false;
    private int mCursorPosition = 0;
    private int mSelectionEndPosition = 0;
    private int mLastEditorAction = 0;
    private final SpannableStringBuilder mInputText = new SpannableStringBuilder();
    private String mLastCommitCorrection = "";

    public TestInputConnection(@NonNull AnySoftKeyboard ime) {
        super(new TextView(ime.getApplicationContext()), false);
        mIme = ime;
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
        mCursorPosition += cursorDelta;
        notifyTextChanged(oldPosition, mSelectionEndPosition, mCursorPosition, mCursorPosition);
        mSelectionEndPosition = mCursorPosition;
    }

    private void notifyTextChanged(int oldStart, int oldEnd, int newStart, int newEnd) {
        if (mInEditMode) {
            mChangesWhileInEdit = true;
        } else {
            int[] composedTextRange = findComposedText();
            if (mSendUpdates) {
                mIme.onUpdateSelection(
                        oldStart,
                        oldEnd,
                        newStart,
                        newEnd,
                        composedTextRange[0],
                        composedTextRange[1]);
            }
        }
    }

    public void setSendUpdates(boolean sendUpdates) {
        mSendUpdates = sendUpdates;
    }

    public void setCongested(boolean congested) {
        mCongested = congested;
        if (!mCongested) {
            while (!mCongestedActions.isEmpty()) mCongestedActions.remove(0).run();
        }
    }

    public void popCongestedAction() {
        if (mCongested) {
            mCongestedActions.remove(0).run();
        } else {
            throw new IllegalStateException("called popCongestedAction when not congested");
        }
    }

    public void sendUpdateNow() {
        final boolean originalSendState = mSendUpdates;
        mSendUpdates = true;
        notifyTextChange(0);
        mSendUpdates = originalSendState;
    }

    @Override
    public boolean setComposingText(CharSequence text, int newCursorPosition) {
        commitTextAs(text, true, newCursorPosition);
        return true;
    }

    private void commitTextAs(
            final CharSequence text, final boolean asComposing, final int newCursorPosition) {
        if (mCongested) {
            final String queuedText = text.toString();
            mCongestedActions.add(
                    () -> internalCommitTextAs(queuedText, asComposing, newCursorPosition));
        } else {
            internalCommitTextAs(text, asComposing, newCursorPosition);
        }
    }

    private void internalCommitTextAs(
            CharSequence text, boolean asComposing, int newCursorPosition) {
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
        return true;
    }

    @Override
    public boolean finishComposingText() {
        mInputText.clearSpans();
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

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
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
        if (mCongested) {
            mCongestedActions.add(() -> internalSetSelection(start, end));
        } else {
            internalSetSelection(start, end);
        }
        return true;
    }

    private void internalSetSelection(int start, int end) {
        if (start == end && start == mCursorPosition) return;

        final int len = mInputText.length();
        if (start < 0 || end < 0 || start > len || end > len) return; // ignoring

        int oldStart = mCursorPosition;
        int oldEnd = mSelectionEndPosition;
        mCursorPosition = start;
        mSelectionEndPosition = Math.min(end, mInputText.length());
        notifyTextChanged(oldStart, oldEnd, mCursorPosition, mSelectionEndPosition);
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
        mInEditMode = true;
        return true;
    }

    @Override
    public boolean endBatchEdit() {
        mInEditMode = false;
        if (mChangesWhileInEdit) sendUpdateNow();
        mChangesWhileInEdit = false;
        return true;
    }

    @Override
    public boolean sendKeyEvent(KeyEvent event) {
        if (mCongested) {
            final KeyEvent queuedEvent = new KeyEvent(event);
            mCongestedActions.add(() -> internalSendKeyEvent(queuedEvent));
        } else {
            /*
            ic.sendKeyEvent(new KeyEvent(eventTime, eventTime,
                    KeyEvent.ACTION_DOWN, keyEventCode, 0, 0, KeyCharacterMap.VIRTUAL_KEYBOARD, 0,
                    KeyEvent.FLAG_SOFT_KEYBOARD|KeyEvent.FLAG_KEEP_TOUCH_MODE));
            ic.sendKeyEvent(new KeyEvent(eventTime, SystemClock.uptimeMillis(),
                    KeyEvent.ACTION_UP, keyEventCode, 0, 0, KeyCharacterMap.VIRTUAL_KEYBOARD, 0,
                    KeyEvent.FLAG_SOFT_KEYBOARD|KeyEvent.FLAG_KEEP_TOUCH_MODE));
             */
            internalSendKeyEvent(event);
        }
        return true;
    }

    private void internalSendKeyEvent(KeyEvent event) {
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
}
