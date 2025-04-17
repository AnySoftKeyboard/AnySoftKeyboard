/*
 * Copyright (c) 2016 Menny Even-Danan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.anysoftkeyboard.ime;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.InputMethodService;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.anysoftkeyboard.base.utils.GCUtils;
import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.keyboards.views.KeyboardViewContainerView;
import com.anysoftkeyboard.keyboards.views.OnKeyboardActionListener;
import com.anysoftkeyboard.ui.dev.DeveloperUtils;
import com.anysoftkeyboard.utils.ModifierKeyState;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.BuildConfig;
import com.menny.android.anysoftkeyboard.R;
import io.reactivex.disposables.CompositeDisposable;
import java.util.List;

public abstract class AnySoftKeyboardBase extends InputMethodService
    implements OnKeyboardActionListener {
  protected static final String TAG = "ASK";

  protected static final long ONE_FRAME_DELAY = 1000L / 60L;

  private static final ExtractedTextRequest EXTRACTED_TEXT_REQUEST = new ExtractedTextRequest();

  private KeyboardViewContainerView mInputViewContainer;
  private InputViewBinder mInputView;
  private InputMethodManager mInputMethodManager;

  // NOTE: These two are dangerous to use, as they may point to
  // an inaccurate position (in cases where onSelectionUpdate is delayed).
  protected int mGlobalCursorPositionDangerous = 0;
  protected int mGlobalSelectionStartPositionDangerous = 0;
  protected int mGlobalCandidateStartPositionDangerous = 0;
  protected int mGlobalCandidateEndPositionDangerous = 0;

  protected final ModifierKeyState mShiftKeyState =
      new ModifierKeyState(true /*supports locked state*/);
  protected final ModifierKeyState mControlKeyState =
      new ModifierKeyState(false /*does not support locked state*/);

  @NonNull protected final CompositeDisposable mInputSessionDisposables = new CompositeDisposable();
  private int mOrientation;

  @Override
  @CallSuper
  public void onCreate() {
    Logger.i(
        TAG,
        "****** AnySoftKeyboard v%s (%d) service started.",
        BuildConfig.VERSION_NAME,
        BuildConfig.VERSION_CODE);
    super.onCreate();
    mOrientation = getResources().getConfiguration().orientation;
    if (!BuildConfig.DEBUG && DeveloperUtils.hasTracingRequested(getApplicationContext())) {
      try {
        DeveloperUtils.startTracing();
        Toast.makeText(getApplicationContext(), R.string.debug_tracing_starting, Toast.LENGTH_SHORT)
            .show();
      } catch (Exception e) {
        // see issue https://github.com/AnySoftKeyboard/AnySoftKeyboard/issues/105
        // I might get a "Permission denied" error.
        e.printStackTrace();
        Toast.makeText(
                getApplicationContext(), R.string.debug_tracing_starting_failed, Toast.LENGTH_LONG)
            .show();
      }
    }

    mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
  }

  @Nullable
  public final InputViewBinder getInputView() {
    return mInputView;
  }

  @Nullable
  public KeyboardViewContainerView getInputViewContainer() {
    return mInputViewContainer;
  }

  protected abstract String getSettingsInputMethodId();

  protected InputMethodManager getInputMethodManager() {
    return mInputMethodManager;
  }

  @Override
  public void onComputeInsets(@NonNull Insets outInsets) {
    super.onComputeInsets(outInsets);
    if (!isFullscreenMode()) {
      outInsets.contentTopInsets = outInsets.visibleTopInsets;
    }
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

  public abstract void deleteLastCharactersFromInput(int countToDelete);

  @CallSuper
  public void onAddOnsCriticalChange() {
    hideWindow();
  }

  @Override
  public View onCreateInputView() {
    if (mInputView != null) mInputView.onViewNotRequired();
    mInputView = null;

    GCUtils.getInstance()
        .performOperationWithMemRetry(
            TAG,
            () -> {
              mInputViewContainer = createInputViewContainer();
              mInputViewContainer.setBackgroundResource(R.drawable.ask_wallpaper);
            });

    mInputView = mInputViewContainer.getStandardKeyboardView();
    mInputViewContainer.setOnKeyboardActionListener(this);
    setupInputViewWatermark();

    return mInputViewContainer;
  }

  @Override
  public void setInputView(View view) {
    super.setInputView(view);
    updateSoftInputWindowLayoutParameters();
  }

  @Override
  public void updateFullscreenMode() {
    super.updateFullscreenMode();
    updateSoftInputWindowLayoutParameters();
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    ((AnyApplication) getApplication()).setNewConfigurationToAllAddOns(newConfig);
    super.onConfigurationChanged(newConfig);
    if (newConfig.orientation != mOrientation) {
      var lastOrientation = mOrientation;
      mOrientation = newConfig.orientation;
      onOrientationChanged(lastOrientation, mOrientation);
    }
  }

  protected int getCurrentOrientation() {
    // must use the current configuration, since mOrientation may lag a bit.
    return getResources().getConfiguration().orientation;
  }

  @CallSuper
  protected void onOrientationChanged(int oldOrientation, int newOrientation) {}

  private void updateSoftInputWindowLayoutParameters() {
    final Window window = getWindow().getWindow();
    // Override layout parameters to expand {@link SoftInputWindow} to the entire screen.
    // See {@link InputMethodService#setinputView(View)} and
    // {@link SoftInputWindow#updateWidthHeight(WindowManager.LayoutParams)}.
    updateLayoutHeightOf(window, ViewGroup.LayoutParams.MATCH_PARENT);
    // This method may be called before {@link #setInputView(View)}.
    if (mInputViewContainer != null) {
      // In non-fullscreen mode, {@link InputView} and its parent inputArea should expand to
      // the entire screen and be placed at the bottom of {@link SoftInputWindow}.
      // In fullscreen mode, these shouldn't expand to the entire screen and should be
      // coexistent with {@link #mExtractedArea} above.
      // See {@link InputMethodService#setInputView(View) and
      // com.android.internal.R.layout.input_method.xml.
      final View inputArea = window.findViewById(android.R.id.inputArea);

      updateLayoutHeightOf(
          (View) inputArea.getParent(),
          isFullscreenMode()
              ? ViewGroup.LayoutParams.MATCH_PARENT
              : ViewGroup.LayoutParams.WRAP_CONTENT);
      updateLayoutGravityOf((View) inputArea.getParent(), Gravity.BOTTOM);
    }
  }

  private static void updateLayoutHeightOf(final Window window, final int layoutHeight) {
    final WindowManager.LayoutParams params = window.getAttributes();
    if (params != null && params.height != layoutHeight) {
      params.height = layoutHeight;
      window.setAttributes(params);
    }
  }

  private static void updateLayoutHeightOf(final View view, final int layoutHeight) {
    final ViewGroup.LayoutParams params = view.getLayoutParams();
    if (params != null && params.height != layoutHeight) {
      params.height = layoutHeight;
      view.setLayoutParams(params);
    }
  }

  private static void updateLayoutGravityOf(final View view, final int layoutGravity) {
    final ViewGroup.LayoutParams lp = view.getLayoutParams();
    if (lp instanceof LinearLayout.LayoutParams) {
      final LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) lp;
      if (params.gravity != layoutGravity) {
        params.gravity = layoutGravity;
        view.setLayoutParams(params);
      }
    } else if (lp instanceof FrameLayout.LayoutParams) {
      final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) lp;
      if (params.gravity != layoutGravity) {
        params.gravity = layoutGravity;
        view.setLayoutParams(params);
      }
    } else {
      throw new IllegalArgumentException(
          "Layout parameter doesn't have gravity: " + lp.getClass().getName());
    }
  }

  @CallSuper
  @NonNull
  protected List<Drawable> generateWatermark() {
    return ((AnyApplication) getApplication()).getInitialWatermarksList();
  }

  protected final void setupInputViewWatermark() {
    final InputViewBinder inputView = getInputView();
    if (inputView != null) {
      inputView.setWatermark(generateWatermark());
    }
  }

  @SuppressLint("InflateParams")
  protected KeyboardViewContainerView createInputViewContainer() {
    return (KeyboardViewContainerView)
        getLayoutInflater().inflate(R.layout.main_keyboard_layout, null);
  }

  @CallSuper
  protected boolean handleCloseRequest() {
    // meaning, I didn't do anything with this request.
    return false;
  }

  /** This will ask the OS to hide all views of AnySoftKeyboard. */
  @Override
  public void hideWindow() {
    while (handleCloseRequest()) {
      Logger.i(TAG, "Still have stuff to close. Trying handleCloseRequest again.");
    }
    super.hideWindow();
  }

  @Override
  public void onDestroy() {
    mInputSessionDisposables.dispose();
    if (getInputView() != null) getInputView().onViewNotRequired();
    mInputView = null;

    super.onDestroy();
  }

  @Override
  @CallSuper
  public void onFinishInput() {
    super.onFinishInput();
    mInputSessionDisposables.clear();
    mGlobalCursorPositionDangerous = 0;
    mGlobalSelectionStartPositionDangerous = 0;
    mGlobalCandidateStartPositionDangerous = 0;
    mGlobalCandidateEndPositionDangerous = 0;
  }

  protected abstract boolean isSelectionUpdateDelayed();

  @Nullable
  protected ExtractedText getExtractedText() {
    final InputConnection connection = getCurrentInputConnection();
    if (connection == null) {
      return null;
    }
    return connection.getExtractedText(EXTRACTED_TEXT_REQUEST, 0);
  }

  // TODO SHOULD NOT USE THIS METHOD AT ALL!
  protected int getCursorPosition() {
    if (isSelectionUpdateDelayed()) {
      ExtractedText extracted = getExtractedText();
      if (extracted == null) {
        return 0;
      }
      mGlobalCursorPositionDangerous = extracted.startOffset + extracted.selectionEnd;
      mGlobalSelectionStartPositionDangerous = extracted.startOffset + extracted.selectionStart;
    }
    return mGlobalCursorPositionDangerous;
  }

  @Override
  public void onUpdateSelection(
      int oldSelStart,
      int oldSelEnd,
      int newSelStart,
      int newSelEnd,
      int candidatesStart,
      int candidatesEnd) {
    if (BuildConfig.DEBUG) {
      Logger.d(
          TAG,
          "onUpdateSelection: oss=%d, ose=%d, nss=%d, nse=%d, cs=%d, ce=%d",
          oldSelStart,
          oldSelEnd,
          newSelStart,
          newSelEnd,
          candidatesStart,
          candidatesEnd);
    }
    mGlobalCursorPositionDangerous = newSelEnd;
    mGlobalSelectionStartPositionDangerous = newSelStart;
    mGlobalCandidateStartPositionDangerous = candidatesStart;
    mGlobalCandidateEndPositionDangerous = candidatesEnd;
  }

  @Override
  public void onCancel() {
    // the user released their finger outside of any key... okay. I have nothing to do about
    // that.
  }
}
