package com.anysoftkeyboard.ime;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AlertDialog;
import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.devicespecific.Clipboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.views.KeyboardViewContainerView;
import com.anysoftkeyboard.rx.GenericOnError;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;
import java.util.ArrayList;
import java.util.List;
import net.evendanan.pixel.GeneralDialogController;

public abstract class AnySoftKeyboardClipboard extends AnySoftKeyboardSwipeListener {

  private boolean mArrowSelectionState;
  private Clipboard mClipboard;
  protected static final int MAX_CHARS_PER_CODE_POINT = 2;
  private static final long MAX_TIME_TO_SHOW_SYNCED_CLIPBOARD_ENTRY = 15 * 1000;
  private static final long MAX_TIME_TO_SHOW_SYNCED_CLIPBOARD_HINT = 120 * 1000;
  private long mLastSyncedClipboardEntryTime = Long.MIN_VALUE;
  private final Clipboard.ClipboardUpdatedListener mClipboardUpdatedListener =
      new Clipboard.ClipboardUpdatedListener() {
        @Override
        public void onClipboardEntryAdded(@NonNull CharSequence text) {
          AnySoftKeyboardClipboard.this.onClipboardEntryAdded(text);
        }

        @Override
        public void onClipboardCleared() {
          AnySoftKeyboardClipboard.this.onClipboardEntryAdded(null);
        }
      };

  @Nullable private CharSequence mLastSyncedClipboardEntry;
  private boolean mLastSyncedClipboardEntryInSecureInput;

  @VisibleForTesting
  protected interface ClipboardActionOwner {
    @NonNull Context getContext();

    void outputClipboardText();

    void showAllClipboardOptions();
  }

  @VisibleForTesting
  protected final ClipboardActionOwner mClipboardActionOwnerImpl =
      new ClipboardActionOwner() {
        @NonNull @Override
        public Context getContext() {
          return AnySoftKeyboardClipboard.this;
        }

        @Override
        public void outputClipboardText() {
          AnySoftKeyboardClipboard.this.performPaste();
          mSuggestionClipboardEntry.setAsHint(false);
        }

        @Override
        public void showAllClipboardOptions() {
          AnySoftKeyboardClipboard.this.showAllClipboardEntries(null);
          mSuggestionClipboardEntry.setAsHint(false);
        }
      };

  @VisibleForTesting
  protected static class ClipboardStripActionProvider
      implements KeyboardViewContainerView.StripActionProvider {
    private final ClipboardActionOwner mOwner;
    private View mRootView;
    private ViewGroup mParentView;
    private TextView mClipboardText;
    private Animator mHideClipboardTextAnimator;

    ClipboardStripActionProvider(@NonNull ClipboardActionOwner owner) {
      mOwner = owner;
    }

    @Override
    public @NonNull View inflateActionView(@NonNull ViewGroup parent) {
      mParentView = parent;
      mRootView =
          LayoutInflater.from(mOwner.getContext())
              .inflate(R.layout.clipboard_suggestion_action, mParentView, false);
      mHideClipboardTextAnimator =
          AnimatorInflater.loadAnimator(parent.getContext(), R.animator.clipboard_text_to_gone);
      mClipboardText = mRootView.findViewById(R.id.clipboard_suggestion_text);
      mHideClipboardTextAnimator.addListener(
          new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
              super.onAnimationEnd(animation);
              final TextView textView = mClipboardText;
              if (textView != null) {
                textView.setVisibility(View.GONE);
              }
            }
          });
      mRootView.setOnClickListener(view -> mOwner.outputClipboardText());
      mRootView.setOnLongClickListener(
          v -> {
            mOwner.showAllClipboardOptions();
            return true;
          });

      return mRootView;
    }

    @Override
    public void onRemoved() {
      if (mHideClipboardTextAnimator != null) mHideClipboardTextAnimator.cancel();
      mClipboardText = null;
      mRootView = null;
    }

    boolean isVisible() {
      return mRootView != null;
    }

    boolean isFullyVisible() {
      return mClipboardText != null && mClipboardText.getVisibility() == View.VISIBLE;
    }

    void setAsHint(boolean now) {
      if (now) {
        mClipboardText.setVisibility(View.GONE);
      } else if (mClipboardText.getVisibility() != View.GONE
          && !mHideClipboardTextAnimator.isStarted()) {
        mClipboardText.setPivotX(mClipboardText.getWidth());
        mClipboardText.setPivotY(mClipboardText.getHeight() / 2f);
        mHideClipboardTextAnimator.setTarget(mClipboardText);
        mHideClipboardTextAnimator.start();
      }
      mParentView.requestLayout();
    }

    void setClipboardText(CharSequence text, boolean isSecured) {
      mHideClipboardTextAnimator.cancel();
      mClipboardText.setVisibility(View.VISIBLE);
      mClipboardText.setScaleX(1f);
      mClipboardText.setScaleY(1f);
      mClipboardText.setAlpha(1f);
      mClipboardText.setSelected(true);
      if (isSecured) mClipboardText.setText("**********");
      else mClipboardText.setText(text);
      mParentView.requestLayout();
    }
  }

  @VisibleForTesting protected ClipboardStripActionProvider mSuggestionClipboardEntry;

  @Override
  public void onCreate() {
    super.onCreate();
    mClipboard = AnyApplication.getDeviceSpecific().createClipboard(getApplicationContext());
    mSuggestionClipboardEntry = new ClipboardStripActionProvider(mClipboardActionOwnerImpl);
    addDisposable(
        prefs()
            .getBoolean(
                R.string.settings_key_os_clipboard_sync, R.bool.settings_default_os_clipboard_sync)
            .asObservable()
            .distinctUntilChanged()
            .subscribe(
                syncClipboard -> {
                  mLastSyncedClipboardEntryTime = Long.MIN_VALUE;
                  mClipboard.setClipboardUpdatedListener(
                      syncClipboard ? mClipboardUpdatedListener : null);
                },
                GenericOnError.onError("settings_key_os_clipboard_sync")));
  }

  private void onClipboardEntryAdded(CharSequence clipboardEntry) {
    if (TextUtils.isEmpty(clipboardEntry)) {
      mLastSyncedClipboardEntry = null;
      mLastSyncedClipboardEntryTime = Long.MIN_VALUE;
      // this method could be called before the IM view was created, but the
      // service already alive.
      var inputViewContainer = getInputViewContainer();
      if (inputViewContainer != null) {
        inputViewContainer.removeStripAction(mSuggestionClipboardEntry);
      }
    } else {
      mLastSyncedClipboardEntry = clipboardEntry;
      EditorInfo currentInputEditorInfo = getCurrentInputEditorInfo();
      mLastSyncedClipboardEntryInSecureInput = isTextPassword(currentInputEditorInfo);
      mLastSyncedClipboardEntryTime = SystemClock.uptimeMillis();
      // if we already showing the view, we want to update it contents
      if (isInputViewShown()) {
        showClipboardActionIcon(currentInputEditorInfo);
      }
    }
  }

  private void showClipboardActionIcon(EditorInfo info) {
    getInputViewContainer().addStripAction(mSuggestionClipboardEntry, true);
    getInputViewContainer().setActionsStripVisibility(true);

    mSuggestionClipboardEntry.setClipboardText(
        mLastSyncedClipboardEntry, mLastSyncedClipboardEntryInSecureInput || isTextPassword(info));
  }

  @Override
  public void onStartInputView(EditorInfo info, boolean restarting) {
    super.onStartInputView(info, restarting);
    final long now = SystemClock.uptimeMillis();
    final long startTime = mLastSyncedClipboardEntryTime;
    if (startTime + MAX_TIME_TO_SHOW_SYNCED_CLIPBOARD_HINT > now
        && !TextUtils.isEmpty(mLastSyncedClipboardEntry)) {
      showClipboardActionIcon(info);
      if (startTime + MAX_TIME_TO_SHOW_SYNCED_CLIPBOARD_ENTRY <= now && !restarting) {
        mSuggestionClipboardEntry.setAsHint(true);
      }
    }
  }

  protected static boolean isTextPassword(@Nullable EditorInfo info) {
    if (info == null) return false;
    if ((info.inputType & EditorInfo.TYPE_CLASS_TEXT) == 0) return false;
    switch (info.inputType & EditorInfo.TYPE_MASK_VARIATION) {
      case EditorInfo.TYPE_TEXT_VARIATION_PASSWORD:
      case EditorInfo.TYPE_TEXT_VARIATION_WEB_PASSWORD:
      case EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD:
        return true;
      default:
        return false;
    }
  }

  @Override
  public void onKey(
      int primaryCode, Keyboard.Key key, int multiTapIndex, int[] nearByKeyCodes, boolean fromUI) {
    if (mSuggestionClipboardEntry.isVisible()) {
      final long now = SystemClock.uptimeMillis();
      if (mLastSyncedClipboardEntryTime + MAX_TIME_TO_SHOW_SYNCED_CLIPBOARD_HINT <= now) {
        getInputViewContainer().removeStripAction(mSuggestionClipboardEntry);
      } else {
        mSuggestionClipboardEntry.setAsHint(false);
      }
    }
    super.onKey(primaryCode, key, multiTapIndex, nearByKeyCodes, fromUI);
  }

  @Override
  public void onFinishInputView(boolean finishingInput) {
    super.onFinishInputView(finishingInput);
    getInputViewContainer().removeStripAction(mSuggestionClipboardEntry);
  }

  private void showAllClipboardEntries(Keyboard.Key key) {
    int entriesCount = mClipboard.getClipboardEntriesCount();
    if (entriesCount == 0) {
      showToastMessage(R.string.clipboard_is_empty_toast, true);
    } else {
      final List<CharSequence> nonEmpties = new ArrayList<>(entriesCount);
      for (int entryIndex = 0; entryIndex < entriesCount; entryIndex++) {
        nonEmpties.add(mClipboard.getText(entryIndex));
      }
      final CharSequence[] entries = nonEmpties.toArray(new CharSequence[0]);
      DialogInterface.OnClickListener onClickListener =
          (dialog, which) -> {
            if (which == 0) {
              performPaste();
            } else {
              onText(key, entries[which]);
            }
          };
      showOptionsDialogWithData(
          R.string.clipboard_paste_entries_title,
          R.drawable.ic_clipboard_paste_in_app,
          new CharSequence[0],
          onClickListener,
          new GeneralDialogController.DialogPresenter() {
            @Override
            public void beforeDialogShown(@NonNull AlertDialog dialog, @Nullable Object data) {}

            @Override
            public void onSetupDialogRequired(
                Context context, AlertDialog.Builder builder, int optionId, @Nullable Object data) {
              builder.setNeutralButton(
                  R.string.delete_all_clipboard_entries,
                  (dialog, which) -> {
                    mClipboard.deleteAllEntries();
                    dialog.dismiss();
                  });
              builder.setAdapter(new ClipboardEntriesAdapter(context, entries), onClickListener);
            }
          });
    }
  }

  private void performPaste() {
    CharSequence clipboardText =
        mClipboard.getClipboardEntriesCount() > 0 ? mClipboard.getText(0 /*last entry paste*/) : "";
    if (!TextUtils.isEmpty(clipboardText)) {
      sendDownUpKeyEvents(KeyEvent.KEYCODE_V, KeyEvent.META_CTRL_ON);
    } else {
      showToastMessage(R.string.clipboard_is_empty_toast, true);
    }
  }

  private void performCopy(boolean alsoCut) {
    if (alsoCut) {
      sendDownUpKeyEvents(KeyEvent.KEYCODE_X, KeyEvent.META_CTRL_ON);
    } else {
      sendDownUpKeyEvents(KeyEvent.KEYCODE_C, KeyEvent.META_CTRL_ON);
      // showing toast, since there isn't any other UI feedback
      showToastMessage(R.string.clipboard_copy_done_toast, true);
    }
  }

  protected void handleClipboardOperation(
      final Keyboard.Key key, final int primaryCode, InputConnection ic) {
    abortCorrectionAndResetPredictionState(false);
    switch (primaryCode) {
      case KeyCodes.CLIPBOARD_PASTE:
        performPaste();
        break;
      case KeyCodes.CLIPBOARD_CUT:
      case KeyCodes.CLIPBOARD_COPY:
        performCopy(primaryCode == KeyCodes.CLIPBOARD_CUT);
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
        sendDownUpKeyEvents(KeyEvent.KEYCODE_Z, KeyEvent.META_CTRL_ON);
        break;
      case KeyCodes.REDO:
        sendDownUpKeyEvents(KeyEvent.KEYCODE_Z, KeyEvent.META_CTRL_ON | KeyEvent.META_SHIFT_ON);
        break;
      default:
        throw new IllegalArgumentException(
            "The keycode " + primaryCode + " is not covered by handleClipboardOperation!");
    }
  }

  protected boolean handleSelectionExpending(int keyEventKeyCode, InputConnection ic) {
    if (mArrowSelectionState && ic != null) {
      final int selectionEnd = getCursorPosition();
      final int selectionStart = mGlobalSelectionStartPositionDangerous;
      markExpectingSelectionUpdate();
      switch (keyEventKeyCode) {
        case KeyEvent.KEYCODE_DPAD_LEFT:
          // A Unicode code-point can be made up of two Java chars.
          // We check if that's what happening before the cursor:
          final String toLeft = ic.getTextBeforeCursor(MAX_CHARS_PER_CODE_POINT, 0).toString();
          if (toLeft.length() == 0) {
            ic.setSelection(selectionStart, selectionEnd);
          } else {
            ic.setSelection(
                selectionStart - Character.charCount(toLeft.codePointBefore(toLeft.length())),
                selectionEnd);
          }
          return true;
        case KeyEvent.KEYCODE_DPAD_RIGHT:
          final String toRight = ic.getTextAfterCursor(MAX_CHARS_PER_CODE_POINT, 0).toString();
          if (toRight.length() == 0) {
            ic.setSelection(selectionStart, selectionEnd);
          } else {
            ic.setSelection(
                selectionStart, selectionEnd + Character.charCount(toRight.codePointAt(0)));
          }
          return true;
        default:
          mArrowSelectionState = false;
      }
    }
    return false;
  }

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

  private class ClipboardEntriesAdapter extends ArrayAdapter<CharSequence> {
    public ClipboardEntriesAdapter(@NonNull Context context, CharSequence[] items) {
      super(context, R.layout.clipboard_dialog_entry, R.id.clipboard_entry_text, items);
    }

    @NonNull @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
      View view = super.getView(position, convertView, parent);
      View deleteView = view.findViewById(R.id.clipboard_entry_delete);
      deleteView.setTag(R.id.clipboard_entry_delete, position);
      deleteView.setOnClickListener(this::onItemDeleteClicked);

      return view;
    }

    private void onItemDeleteClicked(View view) {
      int position = (int) view.getTag(R.id.clipboard_entry_delete);
      mClipboard.deleteEntry(position);
      closeGeneralOptionsDialog();
    }
  }
}
