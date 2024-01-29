/*
 * Copyright (c) 2013 Menny Even-Danan
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

package com.anysoftkeyboard.keyboards.views;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.anysoftkeyboard.keyboards.AnyKeyboard.AnyKey;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.views.AnyKeyboardViewBase.KeyPressTimingHandler;
import java.util.Locale;

class PointerTracker {
  static class SharedPointerTrackersData {
    int lastSentKeyIndex = NOT_A_KEY;

    int delayBeforeKeyRepeatStart;
    int longPressKeyTimeout;
    int multiTapKeyTimeout;
  }

  interface UIProxy {
    boolean isAtTwoFingersState();

    void invalidateKey(Keyboard.Key key);

    void showPreview(int keyIndex, PointerTracker tracker);

    void hidePreview(int keyIndex, PointerTracker tracker);
  }

  final int mPointerId;

  // Miscellaneous constants
  private static final int NOT_A_KEY = AnyKeyboardViewBase.NOT_A_KEY;

  private final UIProxy mProxy;
  private final KeyPressTimingHandler mHandler;
  private final KeyDetector mKeyDetector;
  private OnKeyboardActionListener mListener;

  private Keyboard.Key[] mKeys;
  private final int mKeyHysteresisDistanceSquared;

  private final KeyState mKeyState;

  private int mKeyCodesInPathLength = -1;

  // true if keyboard layout has been changed.
  private boolean mKeyboardLayoutHasBeenChanged;

  // true if event is already translated to a key action (long press or mini-keyboard)
  private boolean mKeyAlreadyProcessed;

  // true if this pointer is repeatable key
  private boolean mIsRepeatableKey;

  // For multi-tap
  private final SharedPointerTrackersData mSharedPointerTrackersData;
  private int mTapCount;
  private long mLastTapTime;
  private boolean mInMultiTap;

  // pressed key
  private int mPreviousKey = NOT_A_KEY;

  // This class keeps track of a key index and a position where this pointer is.
  private static class KeyState {
    private final KeyDetector mKeyDetector;

    // The current key index where this pointer is.
    private int mKeyIndex = NOT_A_KEY;
    // The position where mKeyIndex was recognized for the first time.
    private int mKeyX;
    private int mKeyY;

    // Last pointer position.
    private int mLastX;
    private int mLastY;

    KeyState(KeyDetector keyDetector) {
      mKeyDetector = keyDetector;
    }

    int getKeyIndex() {
      return mKeyIndex;
    }

    int getKeyX() {
      return mKeyX;
    }

    int getKeyY() {
      return mKeyY;
    }

    int getLastX() {
      return mLastX;
    }

    int getLastY() {
      return mLastY;
    }

    int onDownKey(int x, int y) {
      return onMoveToNewKey(onMoveKeyInternal(x, y), x, y);
    }

    private int onMoveKeyInternal(int x, int y) {
      mLastX = x;
      mLastY = y;
      return mKeyDetector.getKeyIndexAndNearbyCodes(x, y, null);
    }

    int onMoveKey(int x, int y) {
      return onMoveKeyInternal(x, y);
    }

    int onMoveToNewKey(int keyIndex, int x, int y) {
      mKeyIndex = keyIndex;
      mKeyX = x;
      mKeyY = y;
      return keyIndex;
    }

    int onUpKey(int x, int y) {
      return onMoveKeyInternal(x, y);
    }
  }

  PointerTracker(
      int id,
      KeyPressTimingHandler handler,
      KeyDetector keyDetector,
      UIProxy proxy,
      int keyHysteresisDistance,
      @NonNull SharedPointerTrackersData sharedPointerTrackersData) {
    if (proxy == null || handler == null || keyDetector == null) {
      throw new NullPointerException();
    }
    mKeyHysteresisDistanceSquared = keyHysteresisDistance * keyHysteresisDistance;
    mSharedPointerTrackersData = sharedPointerTrackersData;
    mPointerId = id;
    mProxy = proxy;
    mHandler = handler;
    mKeyDetector = keyDetector;
    mKeyState = new KeyState(keyDetector);
    resetMultiTap();
  }

  void setOnKeyboardActionListener(OnKeyboardActionListener listener) {
    mListener = listener;
  }

  public void setKeyboard(@NonNull Keyboard.Key[] keys) {
    mKeys = keys;
    // Mark that keyboard layout has been changed.
    mKeyboardLayoutHasBeenChanged = true;
  }

  private boolean isValidKeyIndex(int keyIndex) {
    return keyIndex >= 0 && keyIndex < mKeys.length;
  }

  @Nullable public Keyboard.Key getKey(int keyIndex) {
    return isValidKeyIndex(keyIndex) ? mKeys[keyIndex] : null;
  }

  private boolean isModifierInternal(int keyIndex) {
    Keyboard.Key key = getKey(keyIndex);
    return key != null && key.modifier;
  }

  public boolean isModifier() {
    return isModifierInternal(mKeyState.getKeyIndex());
  }

  boolean isOnModifierKey(int x, int y) {
    return isModifierInternal(mKeyDetector.getKeyIndexAndNearbyCodes(x, y, null));
  }

  private void updateKey(int keyIndex) {
    if (mKeyAlreadyProcessed) {
      return;
    }
    int oldKeyIndex = mPreviousKey;
    mPreviousKey = keyIndex;
    if (keyIndex != oldKeyIndex) {
      if (isValidKeyIndex(oldKeyIndex)) {
        // if new key index is not a key, old key was just released inside of the key.
        mKeys[oldKeyIndex].onReleased();
        mProxy.invalidateKey(mKeys[oldKeyIndex]);
      }
      if (isValidKeyIndex(keyIndex)) {
        mKeys[keyIndex].onPressed();
        mProxy.invalidateKey(mKeys[keyIndex]);
      }
    }
  }

  void setAlreadyProcessed() {
    mKeyAlreadyProcessed = true;
  }

  //    public void onTouchEvent(int action, int x, int y, long eventTime) {
  //        switch (action) {
  //            case MotionEvent.ACTION_MOVE:
  //                onMoveEvent(x, y, eventTime);
  //                break;
  //            case MotionEvent.ACTION_DOWN:
  //            case MotionEvent.ACTION_POINTER_DOWN:
  //                onDownEvent(x, y, eventTime);
  //                break;
  //            case MotionEvent.ACTION_UP:
  //            case MotionEvent.ACTION_POINTER_UP:
  //                onUpEvent(x, y, eventTime);
  //                break;
  //            case MotionEvent.ACTION_CANCEL:
  //                onCancelEvent();
  //                break;
  //            default:
  //                break;
  //        }
  //    }

  void onDownEvent(int x, int y, long eventTime) {
    int keyIndex = mKeyState.onDownKey(x, y);
    mKeyboardLayoutHasBeenChanged = false;
    mKeyAlreadyProcessed = false;
    mIsRepeatableKey = false;
    mKeyCodesInPathLength = -1;
    checkMultiTap(eventTime, keyIndex);
    if (mListener != null && isValidKeyIndex(keyIndex)) {
      AnyKey key = (AnyKey) mKeys[keyIndex];
      final int codeAtIndex = key.getCodeAtIndex(0, mKeyDetector.isKeyShifted(key));

      if (!mProxy.isAtTwoFingersState()
          && mListener.onGestureTypingInputStart(x, y, key, eventTime)) {
        mKeyCodesInPathLength = 1;
      }

      if (codeAtIndex != 0) {
        mListener.onPress(codeAtIndex);
        // also notifying about first down
        mListener.onFirstDownKey(codeAtIndex);
      }
      // This onPress call may have changed keyboard layout. Those cases are detected at
      // {@link #setKeyboard}. In those cases, we should update keyIndex according to the
      // new keyboard layout.
      if (mKeyboardLayoutHasBeenChanged) {
        mKeyboardLayoutHasBeenChanged = false;
        keyIndex = mKeyState.onDownKey(x, y);
      }
    }
    if (isValidKeyIndex(keyIndex)) {
      if (mKeys[keyIndex].repeatable) {
        repeatKey(keyIndex);
        mHandler.startKeyRepeatTimer(
            mSharedPointerTrackersData.delayBeforeKeyRepeatStart, keyIndex, this);
        mIsRepeatableKey = true;
      }
      startLongPressTimer(keyIndex);
    }
    showKeyPreviewAndUpdateKey(keyIndex);
  }

  void onMoveEvent(int x, int y, long eventTime) {
    if (mProxy.isAtTwoFingersState()) {
      mKeyCodesInPathLength = -1;
    } else if (canDoGestureTyping()) {
      mListener.onGestureTypingInput(x, y, eventTime);
    }

    if (mKeyAlreadyProcessed) {
      return;
    }
    final KeyState keyState = mKeyState;
    final int oldKeyIndex = keyState.getKeyIndex();
    int keyIndex = keyState.onMoveKey(x, y);
    final Keyboard.Key oldKey = getKey(oldKeyIndex);

    if (isValidKeyIndex(keyIndex)) {
      if (oldKey == null) {
        // The pointer has been slid in to the new key, but the finger was not on any keys.
        // In this case, we must call onPress() to notify that the new key is being pressed.
        if (mListener != null) {
          Keyboard.Key key = getKey(keyIndex);
          mListener.onPress(key.getCodeAtIndex(0, mKeyDetector.isKeyShifted(key)));
          // This onPress call may have changed keyboard layout. Those cases are detected
          // at {@link #setKeyboard}. In those cases, we should update keyIndex according
          // to the new keyboard layout.
          if (mKeyboardLayoutHasBeenChanged) {
            mKeyboardLayoutHasBeenChanged = false;
            keyIndex = keyState.onMoveKey(x, y);
          }
        }
        keyState.onMoveToNewKey(keyIndex, x, y);
        startLongPressTimer(keyIndex);
      } else if (!isMinorMoveBounce(x, y, keyIndex)) {
        // The pointer has been slid in to the new key from the previous key, we must call
        // onRelease() first to notify that the previous key has been released, then call
        // onPress() to notify that the new key is being pressed.
        if (mListener != null && !isInGestureTyping()) {
          mListener.onRelease(oldKey.getCodeAtIndex(0, mKeyDetector.isKeyShifted(oldKey)));
        }
        resetMultiTap();
        if (mListener != null) {
          Keyboard.Key key = getKey(keyIndex);
          if (canDoGestureTyping()) {
            mKeyCodesInPathLength++;
          } else {
            mListener.onPress(key.getCodeAtIndex(0, mKeyDetector.isKeyShifted(key)));
          }
          // This onPress call may have changed keyboard layout. Those cases are detected
          // at {@link #setKeyboard}. In those cases, we should update keyIndex according
          // to the new keyboard layout.
          if (mKeyboardLayoutHasBeenChanged) {
            mKeyboardLayoutHasBeenChanged = false;
            keyIndex = keyState.onMoveKey(x, y);
          }
        }
        keyState.onMoveToNewKey(keyIndex, x, y);
        startLongPressTimer(keyIndex);
        if (oldKeyIndex != keyIndex) {
          mProxy.hidePreview(oldKeyIndex, this);
        }
      }
    } else {
      if (oldKey != null && !isMinorMoveBounce(x, y, keyIndex)) {
        // The pointer has been slid out from the previous key, we must call onRelease() to
        // notify that the previous key has been released.
        if (mListener != null) {
          mListener.onRelease(oldKey.getCodeAtIndex(0, mKeyDetector.isKeyShifted(oldKey)));
        }
        resetMultiTap();
        keyState.onMoveToNewKey(keyIndex, x, y);
        mHandler.cancelLongPressTimer();
        if (oldKeyIndex != keyIndex) {
          mProxy.hidePreview(oldKeyIndex, this);
        }
      }
    }
    showKeyPreviewAndUpdateKey(keyState.getKeyIndex());
  }

  void onUpEvent(int x, int y, long eventTime) {
    final OnKeyboardActionListener listener = mListener;
    mHandler.cancelAllMessages();
    mProxy.hidePreview(mKeyState.getKeyIndex(), this);
    showKeyPreviewAndUpdateKey(NOT_A_KEY);
    if (mKeyAlreadyProcessed) {
      return;
    }
    int keyIndex = mKeyState.onUpKey(x, y);
    if (isMinorMoveBounce(x, y, keyIndex)) {
      // Use previous fixed key index and coordinates.
      keyIndex = mKeyState.getKeyIndex();
      x = mKeyState.getKeyX();
      y = mKeyState.getKeyY();
    }
    if (mIsRepeatableKey) {
      // we just need to report up
      final Keyboard.Key key = getKey(keyIndex);

      if (key != null) {
        if (listener != null) {
          listener.onRelease(key.getPrimaryCode());
        }
      }
    } else {
      boolean notHandled = true;
      if (isInGestureTyping()) {
        mKeyCodesInPathLength = -1;
        notHandled = !listener.onGestureTypingInputDone();
      }
      if (notHandled) detectAndSendKey(keyIndex, x, y, eventTime, true);
    }

    if (isValidKeyIndex(keyIndex)) {
      mProxy.invalidateKey(mKeys[keyIndex]);
    }
  }

  void onCancelEvent() {
    mKeyCodesInPathLength = -1;
    mHandler.cancelAllMessages();
    int keyIndex = mKeyState.getKeyIndex();
    mProxy.hidePreview(keyIndex, this);
    showKeyPreviewAndUpdateKey(NOT_A_KEY);
    if (isValidKeyIndex(keyIndex)) mProxy.invalidateKey(mKeys[keyIndex]);
    setAlreadyProcessed();
  }

  void repeatKey(int keyIndex) {
    Keyboard.Key key = getKey(keyIndex);
    if (key != null) {
      // While key is repeating, because there is no need to handle multi-tap key, we can
      // pass -1 as eventTime argument.
      detectAndSendKey(keyIndex, key.x, key.y, -1, false);
    }
  }

  int getLastX() {
    return mKeyState.getLastX();
  }

  int getLastY() {
    return mKeyState.getLastY();
  }

  private boolean isMinorMoveBounce(int x, int y, int newKey) {
    int curKey = mKeyState.getKeyIndex();
    if (newKey == curKey) {
      return true;
    } else if (isValidKeyIndex(curKey)) {
      return getSquareDistanceToKeyEdge(x, y, mKeys[curKey]) < mKeyHysteresisDistanceSquared;
    } else {
      return false;
    }
  }

  private static int getSquareDistanceToKeyEdge(int x, int y, Keyboard.Key key) {
    final int left = key.x;
    final int right = key.x + key.width;
    final int top = key.y;
    final int bottom = key.y + key.height;
    final int edgeX = x < left ? left : Math.min(x, right);
    final int edgeY = y < top ? top : Math.min(y, bottom);
    final int dx = x - edgeX;
    final int dy = y - edgeY;
    return dx * dx + dy * dy;
  }

  private void showKeyPreviewAndUpdateKey(int keyIndex) {
    updateKey(keyIndex);
    if (!isInGestureTyping()) mProxy.showPreview(keyIndex, this);
  }

  private void startLongPressTimer(int keyIndex) {
    // in gesture typing we do not do long-pressing.
    if (isInGestureTyping()) {
      mHandler.cancelLongPressTimer();
    } else {
      Keyboard.Key key = mKeys[keyIndex];
      final int delay =
          shouldLongPressQuickly(key) ? 1 : mSharedPointerTrackersData.longPressKeyTimeout;
      mHandler.startLongPressTimer(delay, keyIndex, this);
    }
  }

  private boolean shouldLongPressQuickly(Keyboard.Key key) {
    return key.getCodesCount() == 0 && key.popupResId != 0 && TextUtils.isEmpty(key.text);
  }

  private void detectAndSendKey(int index, int x, int y, long eventTime, boolean withRelease) {
    final OnKeyboardActionListener listener = mListener;
    final Keyboard.Key key = getKey(index);

    if (key == null) {
      if (listener != null) {
        listener.onCancel();
      }
    } else {
      boolean isShifted = mKeyDetector.isKeyShifted(key);

      if ((key.typedText != null && !isShifted) || (key.shiftedTypedText != null && isShifted)) {
        if (listener != null) {
          mTapCount = 0;

          final CharSequence text;
          if (isShifted) {
            text = key.shiftedTypedText;
          } else {
            text = key.typedText;
          }
          listener.onText(key, text);
          if (withRelease) listener.onRelease(key.getPrimaryCode());
        }
      } else if ((key.text != null && !isShifted) || (key.shiftedText != null && isShifted)) {
        if (listener != null) {
          mTapCount = 0;

          final CharSequence text;
          if (isShifted) {
            text = key.shiftedText;
          } else {
            text = key.text;
          }
          listener.onText(key, text);
          if (withRelease) listener.onRelease(key.getPrimaryCode());
        }
      } else {
        final int code;
        int[] nearByKeyCodes = mKeyDetector.newCodeArray();
        mKeyDetector.getKeyIndexAndNearbyCodes(x, y, nearByKeyCodes);
        boolean multiTapStarted = false;
        // Multi-tap
        if (mInMultiTap) {
          if (mTapCount != -1) {
            multiTapStarted = true;
            mListener.onMultiTapStarted();
          } else {
            mTapCount = 0;
          }
          code = key.getMultiTapCode(mTapCount);
        } else {
          code = key.getCodeAtIndex(0, mKeyDetector.isKeyShifted(key));
        }
        /*
         * Swap the first and second values in the mCodes array if the primary code is not
         * the first value but the second value in the array. This happens when key
         * debouncing is in effect.
         */
        if (nearByKeyCodes.length >= 2 && nearByKeyCodes[0] != code && nearByKeyCodes[1] == code) {
          nearByKeyCodes[1] = nearByKeyCodes[0];
          nearByKeyCodes[0] = code;
        }
        if (listener != null) {
          listener.onKey(code, key, mTapCount, nearByKeyCodes, x >= 0 || y >= 0);
          if (withRelease) listener.onRelease(code);

          if (multiTapStarted) {
            mListener.onMultiTapEnded();
          }
        }
      }
      mSharedPointerTrackersData.lastSentKeyIndex = index;
      mLastTapTime = eventTime;
    }
  }

  /** Handle multi-tap keys by producing the key label for the current multi-tap state. */
  CharSequence getPreviewText(Keyboard.Key key) {
    boolean isShifted = mKeyDetector.isKeyShifted(key);
    AnyKey anyKey = (AnyKey) key;
    if (isShifted && !TextUtils.isEmpty(anyKey.shiftedKeyLabel)) {
      return anyKey.shiftedKeyLabel;
    } else if (!TextUtils.isEmpty(anyKey.label)) {
      return isShifted ? anyKey.label.toString().toUpperCase(Locale.getDefault()) : anyKey.label;
    } else {
      int multiTapCode = key.getMultiTapCode(mTapCount);
      // The following line became necessary when we stopped casting multiTapCode to char
      if (multiTapCode < 32) {
        multiTapCode = 32;
      }
      // because, if multiTapCode happened to be negative, this would fail:
      return new String(new int[] {multiTapCode}, 0, 1);
    }
  }

  private void resetMultiTap() {
    mSharedPointerTrackersData.lastSentKeyIndex = NOT_A_KEY;
    mTapCount = 0;
    mLastTapTime = -1;
    mInMultiTap = false;
  }

  private void checkMultiTap(long eventTime, int keyIndex) {
    Keyboard.Key key = getKey(keyIndex);
    if (key == null) {
      return;
    }

    final boolean isMultiTap =
        (eventTime < mLastTapTime + mSharedPointerTrackersData.multiTapKeyTimeout
            && keyIndex == mSharedPointerTrackersData.lastSentKeyIndex);
    if (key.getCodesCount() > 1) {
      mInMultiTap = true;
      if (isMultiTap) {
        mTapCount++;
        return;
      } else {
        mTapCount = -1;
        return;
      }
    }
    if (!isMultiTap) {
      resetMultiTap();
    }
  }

  boolean isInGestureTyping() {
    return mKeyCodesInPathLength > 1;
  }

  boolean canDoGestureTyping() {
    return mKeyCodesInPathLength >= 1;
  }
}
