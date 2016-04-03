package com.anysoftkeyboard;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.UnderlineSpan;
import android.view.KeyEvent;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.CorrectionInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;

public class TestInputConnection implements InputConnection {

    @NonNull
    private UnderlineSpan mCurrentComposingSpan = new UnderlineSpan();
    private boolean mSendUpdates = true;
    private boolean mInEditMode = false;
    private boolean mChangesWhileInEdit = false;

    private int mCursorPosition = 0;
    private int mSelectionEndPosition = 0;

    private SpannableStringBuilder mInputText = new SpannableStringBuilder();
    @NonNull
    private final AnySoftKeyboard mIme;

    private String mLastCommitCorrection = "";

    public TestInputConnection(@NonNull AnySoftKeyboard ime) {
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
        extracted.selectionStart = mCursorPosition;
        extracted.selectionEnd = mCursorPosition;

        return extracted;
    }

    @Override
    public boolean deleteSurroundingText(int beforeLength, int afterLength) {
        if (beforeLength == 0 && afterLength == 0) return true;

        final int deleteStart = Math.max(mCursorPosition - beforeLength, 0);
        final int deleteEnd = Math.min(mCursorPosition + afterLength, mInputText.length());
        mInputText.delete(deleteStart, deleteEnd);
        final int cursorDelta = mCursorPosition - deleteStart;
        notifyTextChange(-cursorDelta);
        return true;
    }

    private void notifyTextChange(int cursorDelta) {
        final int oldPosition = mCursorPosition;
        mCursorPosition += cursorDelta;
        mSelectionEndPosition = mCursorPosition;
        if (mInEditMode) {
            mChangesWhileInEdit = true;
        } else {
            int[] composedTextRange = findComposedText();
            if (mSendUpdates) mIme.onUpdateSelection(oldPosition, oldPosition, mCursorPosition, mCursorPosition, composedTextRange[0], composedTextRange[1]);
        }
    }

    public void setSendUpdates(boolean sendUpdates) {
        mSendUpdates = sendUpdates;
    }

    public void sendUpdateNow() {
        final boolean originalSendState = mSendUpdates;
        mSendUpdates = true;
        notifyTextChange(0);
        mSendUpdates = originalSendState;
    }

    @Override
    public boolean setComposingText(CharSequence text, int newCursorPosition) {
        commitTextAs(text, true);
        return true;
    }

    private void commitTextAs(CharSequence text, boolean asComposing) {
        int[] composedTextRange = findComposedText();
        mInputText.delete(composedTextRange[0], composedTextRange[1]);
        final int textRemoved = (composedTextRange[1] - composedTextRange[0]);
        mInputText.append(asComposing? asComposeText(text) : text);
        notifyTextChange(text.length() - textRemoved);
    }

    private int[] findComposedText() {
        int start = mInputText.getSpanStart(mCurrentComposingSpan);
        int end = mInputText.getSpanEnd(mCurrentComposingSpan);
        if (start == -1) return new int[] {mCursorPosition, mCursorPosition};
        else return new int[] {start, end};
    }

    private CharSequence asComposeText(CharSequence text) {
        mCurrentComposingSpan = new UnderlineSpan();
        SpannableString composed = new SpannableString(text);
        composed.setSpan(mCurrentComposingSpan, 0, text.length(), 0);
        return composed;
    }

    @Override
    public boolean setComposingRegion(int start, int end) {
        return false;
    }

    @Override
    public boolean finishComposingText() {
        mInputText.clearSpans();
        return true;
    }

    @Override
    public boolean commitText(CharSequence text, int newCursorPosition) {
        commitTextAs(text, false);
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
        if (start == end && start == mCursorPosition) return true;

        final int len = mInputText.length();
        if (start < 0 || end < 0 || start > len || end > len) return true;//ignoring

        notifyTextChange(start - mCursorPosition);

        mSelectionEndPosition = Math.min(end, mInputText.length());

        return true;
    }

    @Override
    public boolean performEditorAction(int editorAction) {
        return false;
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
        /*
        ic.sendKeyEvent(new KeyEvent(eventTime, eventTime,
                KeyEvent.ACTION_DOWN, keyEventCode, 0, 0, KeyCharacterMap.VIRTUAL_KEYBOARD, 0,
                KeyEvent.FLAG_SOFT_KEYBOARD|KeyEvent.FLAG_KEEP_TOUCH_MODE));
        ic.sendKeyEvent(new KeyEvent(eventTime, SystemClock.uptimeMillis(),
                KeyEvent.ACTION_UP, keyEventCode, 0, 0, KeyCharacterMap.VIRTUAL_KEYBOARD, 0,
                KeyEvent.FLAG_SOFT_KEYBOARD|KeyEvent.FLAG_KEEP_TOUCH_MODE));
         */
        if (event.getAction() == KeyEvent.ACTION_UP) {
            //only handling UP events
            if (event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
                deleteSurroundingText(1, 0);
            } else if (event.getKeyCode() == KeyEvent.KEYCODE_SPACE) {
                commitText(" ", 1);
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
}
