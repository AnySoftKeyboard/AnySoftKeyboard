package com.anysoftkeyboard;

import static android.text.TextUtils.CAP_MODE_CHARACTERS;
import static android.text.TextUtils.CAP_MODE_SENTENCES;
import static android.text.TextUtils.CAP_MODE_WORDS;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Assert;
import org.robolectric.Shadows;

public class TestInputConnection extends BaseInputConnection {
  private static final int DELAYED_SELECTION_UPDATE_MSG_ID = 88;

  @NonNull private final AnySoftKeyboard mIme;
  @NonNull private final UnderlineSpan mCurrentComposingSpan = new UnderlineSpan();
  private final SpannableStringBuilder mInputText = new SpannableStringBuilder();
  private final Handler mDelayer;
  private final List<Long> mNextMessageTime = new ArrayList<>();
  @Nullable private SelectionUpdateData mEditModeInitialState = null;
  @Nullable private SelectionUpdateData mEditModeLatestState = null;
  private final AtomicInteger mSelectionDataNests = new AtomicInteger(0);
  private int mCursorPosition = 0;
  private int mSelectionEndPosition = 0;
  private int mLastEditorAction = 0;
  private String mLastCommitCorrection = "";
  private long mDelayedSelectionUpdate = 1L;
  private boolean mRealCapsMode = false;
  private final List<KeyEvent> mKeyEvents = new ArrayList<>();

  public TestInputConnection(@NonNull AnySoftKeyboard ime) {
    super(new TextView(ime.getApplicationContext()), false);
    mIme = ime;
    mDelayer =
        new Handler(Looper.getMainLooper()) {
          @Override
          public void handleMessage(@NonNull Message msg) {
            if (msg.what == DELAYED_SELECTION_UPDATE_MSG_ID) {
              final SelectionUpdateData data = (SelectionUpdateData) msg.obj;
              final long now = SystemClock.uptimeMillis();
              mNextMessageTime.removeIf(time -> time <= now);
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

  public void setRealCapsMode(boolean real) {
    mRealCapsMode = real;
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
    final long forTime = mNextMessageTime.remove(0) - SystemClock.uptimeMillis();
    Shadows.shadowOf(mDelayer.getLooper()).idleFor(forTime, TimeUnit.MILLISECONDS);
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
    if (mRealCapsMode) {
      return latestGetCursorCapsMode(getCurrentTextInInputConnection(), mCursorPosition, reqModes);
    } else {
      return 0;
    }
  }

  private static int latestGetCursorCapsMode(CharSequence cs, int off, int reqModes) {
    if (off < 0) {
      return 0;
    }

    int i;
    char c;
    int mode = 0;

    if ((reqModes & CAP_MODE_CHARACTERS) != 0) {
      mode |= CAP_MODE_CHARACTERS;
    }
    if ((reqModes & (CAP_MODE_WORDS | CAP_MODE_SENTENCES)) == 0) {
      return mode;
    }

    // Back over allowed opening punctuation.

    for (i = off; i > 0; i--) {
      c = cs.charAt(i - 1);

      if (c != '"' && c != '\'' && Character.getType(c) != Character.START_PUNCTUATION) {
        break;
      }
    }

    // Start of paragraph, with optional whitespace.

    int j = i;
    while (j > 0 && ((c = cs.charAt(j - 1)) == ' ' || c == '\t')) {
      j--;
    }
    if (j == 0 || cs.charAt(j - 1) == '\n') {
      return mode | CAP_MODE_WORDS;
    }

    // Or start of word if we are that style.

    if ((reqModes & CAP_MODE_SENTENCES) == 0) {
      if (i != j) mode |= CAP_MODE_WORDS;
      return mode;
    }

    // There must be a space if not the start of paragraph.

    if (i == j) {
      return mode;
    }

    // Back over allowed closing punctuation.

    for (; j > 0; j--) {
      c = cs.charAt(j - 1);

      if (c != '"' && c != '\'' && Character.getType(c) != Character.END_PUNCTUATION) {
        break;
      }
    }

    if (j > 0) {
      c = cs.charAt(j - 1);

      if (c == '.' || c == '?' || c == '!') {
        // Do not capitalize if the word ends with a period but
        // also contains a period, in which case it is an abbreviation.

        if (c == '.') {
          for (int k = j - 2; k >= 0; k--) {
            c = cs.charAt(k);

            if (c == '.') {
              return mode;
            }

            if (!Character.isLetter(c)) {
              break;
            }
          }
        }

        return mode | CAP_MODE_SENTENCES;
      }
    }

    return mode;
  }

  @Override
  public ExtractedText getExtractedText(ExtractedTextRequest request, int flags) {
    return getCurrentState();
  }

  public CompleteState getCurrentState() {
    final var extracted = new CompleteState();
    extracted.startOffset = 0;
    extracted.text = mInputText.subSequence(0, mInputText.length());
    extracted.selectionStart = mCursorPosition;
    extracted.selectionEnd = mSelectionEndPosition;
    final var compositeRange = findComposedText();
    extracted.candidateStart = compositeRange[0];
    extracted.candidateEnd = compositeRange[1];

    return extracted;
  }

  public static class CompleteState extends ExtractedText {
    public int candidateStart;
    public int candidateEnd;
  }

  @Override
  public boolean deleteSurroundingText(int beforeLength, int afterLength) {
    if (beforeLength == 0 && afterLength == 0) return true;

    final int deleteStart = Math.max(mCursorPosition - beforeLength, 0);
    final int deleteEnd = Math.max(0, Math.min(mCursorPosition + afterLength, mInputText.length()));
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
            oldStart, oldEnd, newStart, newEnd, composedTextRange[0], composedTextRange[1]);
    if (mEditModeLatestState != null) {
      mEditModeLatestState = data;
    } else {
      mNextMessageTime.add(mDelayedSelectionUpdate + SystemClock.uptimeMillis());
      mDelayer.sendMessageDelayed(
          mDelayer.obtainMessage(DELAYED_SELECTION_UPDATE_MSG_ID, data), mDelayedSelectionUpdate);
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
    final int nests = mSelectionDataNests.getAndIncrement();
    int[] composedTextRange = findComposedText();
    mEditModeLatestState =
        new SelectionUpdateData(
            mCursorPosition,
            mSelectionEndPosition,
            mCursorPosition,
            mSelectionEndPosition,
            composedTextRange[0],
            composedTextRange[1]);
    if (nests == 0) {
      mEditModeInitialState = mEditModeLatestState;
    }
    return true;
  }

  @Override
  public boolean endBatchEdit() {
    Assert.assertNotNull(mEditModeLatestState);
    int nests = mSelectionDataNests.decrementAndGet();
    Assert.assertTrue(nests >= 0);
    if (nests == 0) {
      final SelectionUpdateData initialState = mEditModeInitialState;
      final SelectionUpdateData finalState = mEditModeLatestState;
      mEditModeLatestState = null;
      notifyTextChanged(
          initialState.oldSelStart,
          initialState.oldSelEnd,
          finalState.newSelStart,
          finalState.newSelEnd);
    }
    return true;
  }

  public List<KeyEvent> getSentKeyEvents() {
    return Collections.unmodifiableList(mKeyEvents);
  }

  @Override
  public boolean sendKeyEvent(KeyEvent event) {
    mKeyEvents.add(event);
    /*
    ic.sendKeyEvent(new KeyEvent(eventTime, eventTime,
            KeyEvent.ACTION_DOWN, keyEventCode, 0, 0, KeyCharacterMap.VIRTUAL_KEYBOARD, 0,
            KeyEvent.FLAG_SOFT_KEYBOARD|KeyEvent.FLAG_KEEP_TOUCH_MODE));
    ic.sendKeyEvent(new KeyEvent(eventTime, SystemClock.uptimeMillis(),
            KeyEvent.ACTION_UP, keyEventCode, 0, 0, KeyCharacterMap.VIRTUAL_KEYBOARD, 0,
            KeyEvent.FLAG_SOFT_KEYBOARD|KeyEvent.FLAG_KEEP_TOUCH_MODE));
     */
    boolean handled = false;
    final var isUp = event.getAction() == KeyEvent.ACTION_UP;
    // only handling UP events
    if (event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
      if (mSelectionEndPosition == mCursorPosition) {
        handled = true;
        if (isUp) deleteSurroundingText(1, 0);
      } else {
        handled = true;
        if (isUp) {
          mInputText.delete(mCursorPosition, mSelectionEndPosition);
          notifyTextChange(0);
        }
      }
    } else if (event.getKeyCode() == KeyEvent.KEYCODE_FORWARD_DEL) {
      if (mSelectionEndPosition == mCursorPosition) {
        handled = true;
        if (isUp) deleteSurroundingText(0, 1);
      } else {
        handled = true;
        if (isUp) {
          mInputText.delete(mCursorPosition, mSelectionEndPosition);
          notifyTextChange(0);
        }
      }
    } else if (event.getKeyCode() == KeyEvent.KEYCODE_SPACE) {
      handled = true;
      if (isUp) commitText(" ", 1);
    } else if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
      handled = true;
      if (isUp) commitText("\n", 1);
    } else if (event.getKeyCode() == KeyEvent.KEYCODE_C && event.isCtrlPressed()) {
      handled = true;
      if (isUp) {
        var clipboard = (ClipboardManager) mIme.getSystemService(Context.CLIPBOARD_SERVICE);
        final CharSequence selectedText = getSelectedText(0);
        ClipData clipData = ClipData.newPlainText(selectedText, selectedText);
        clipboard.setPrimaryClip(clipData);
      }
    } else if (event.getKeyCode() == KeyEvent.KEYCODE_X && event.isCtrlPressed()) {
      handled = true;
      if (isUp) {
        var clipboard = (ClipboardManager) mIme.getSystemService(Context.CLIPBOARD_SERVICE);
        final CharSequence selectedText = getSelectedText(0);
        ClipData clipData = ClipData.newPlainText(selectedText, selectedText);
        clipboard.setPrimaryClip(clipData);
        mInputText.delete(mCursorPosition, mSelectionEndPosition);
        notifyTextChange(0);
      }
    } else if (event.getKeyCode() == KeyEvent.KEYCODE_V && event.isCtrlPressed()) {
      handled = true;
      if (isUp) {
        var clipboard = (ClipboardManager) mIme.getSystemService(Context.CLIPBOARD_SERVICE);
        var primaryClip = clipboard.getPrimaryClip();
        if (primaryClip != null && primaryClip.getItemCount() > 0) {
          var clipboardText = primaryClip.getItemAt(0).coerceToStyledText(mIme);
          commitTextAs(clipboardText, false, 1);
        }
      }
    } else if (event.getKeyCode() >= KeyEvent.KEYCODE_0
        && event.getKeyCode() <= KeyEvent.KEYCODE_9) {
      handled = true;
      if (isUp) commitText(Integer.toString(event.getKeyCode() - KeyEvent.KEYCODE_0), 1);
    } else if (event.getKeyCode() >= KeyEvent.KEYCODE_A
        && event.getKeyCode() <= KeyEvent.KEYCODE_Z) {
      handled = true;
      final char baseChar = event.isShiftPressed() || event.isCapsLockOn() ? 'A' : 'a';
      if (isUp) commitText("" + (char) (event.getKeyCode() - KeyEvent.KEYCODE_A + baseChar), 1);
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
