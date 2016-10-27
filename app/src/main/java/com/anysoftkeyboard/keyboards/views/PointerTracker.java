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

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.MotionEvent;

import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.keyboards.AnyKeyboard.AnyKey;
import com.anysoftkeyboard.keyboards.Keyboard.Key;
import com.anysoftkeyboard.keyboards.views.AnyKeyboardViewBase.KeyPressTimingHandler;
import com.menny.android.anysoftkeyboard.AnyApplication;

import java.util.Locale;

class PointerTracker {
    static class SharedPointerTrackersData {
        int lastSentKeyIndex = NOT_A_KEY;
    }

    interface UIProxy {
        void invalidateKey(Key key);

        void showPreview(int keyIndex, PointerTracker tracker);

        void hidePreview(int keyIndex, PointerTracker tracker);
    }

    final int mPointerId;

    // Timing constants
    private final int mDelayBeforeKeyRepeatStart;
    private final int mLongPressKeyTimeout;
    private final int mMultiTapKeyTimeout;

    // Miscellaneous constants
    private static final int NOT_A_KEY = AnyKeyboardViewBase.NOT_A_KEY;

    private final UIProxy mProxy;
    private final KeyPressTimingHandler mHandler;
    private final KeyDetector mKeyDetector;
    private OnKeyboardActionListener mListener;

    private Key[] mKeys;
    private int mKeyHysteresisDistanceSquared = -1;

    private final KeyState mKeyState;

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
    //private final StringBuilder mPreviewLabel = new StringBuilder(1);

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

    PointerTracker(int id, KeyPressTimingHandler handler, KeyDetector keyDetector, UIProxy proxy, @NonNull SharedPointerTrackersData sharedPointerTrackersData) {
        if (proxy == null || handler == null || keyDetector == null)
            throw new NullPointerException();
        mSharedPointerTrackersData = sharedPointerTrackersData;
        mPointerId = id;
        mProxy = proxy;
        mHandler = handler;
        mKeyDetector = keyDetector;
        mKeyState = new KeyState(keyDetector);
        mDelayBeforeKeyRepeatStart = AnyApplication.getConfig().getLongPressTimeout();//350
        mLongPressKeyTimeout = AnyApplication.getConfig().getLongPressTimeout();//350
        mMultiTapKeyTimeout = AnyApplication.getConfig().getMultiTapTimeout();//350
        resetMultiTap();
    }

    void setOnKeyboardActionListener(OnKeyboardActionListener listener) {
        mListener = listener;
    }

    public void setKeyboard(Key[] keys, float keyHysteresisDistance) {
        if (keys == null || keyHysteresisDistance < 0) throw new IllegalArgumentException();

        mKeys = keys;
        mKeyHysteresisDistanceSquared = (int) (keyHysteresisDistance * keyHysteresisDistance);
        // Mark that keyboard layout has been changed.
        mKeyboardLayoutHasBeenChanged = true;
    }

    private boolean isValidKeyIndex(int keyIndex) {
        return keyIndex >= 0 && keyIndex < mKeys.length;
    }

    public Key getKey(int keyIndex) {
        return isValidKeyIndex(keyIndex) ? mKeys[keyIndex] : null;
    }

    private boolean isModifierInternal(int keyIndex) {
        Key key = getKey(keyIndex);
        return key != null && key.modifier;
    }

    public boolean isModifier() {
        return isModifierInternal(mKeyState.getKeyIndex());
    }

    boolean isOnModifierKey(int x, int y) {
        return isModifierInternal(mKeyDetector.getKeyIndexAndNearbyCodes(x, y, null));
    }

    private void updateKey(int keyIndex) {
        if (mKeyAlreadyProcessed)
            return;
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

    public void onTouchEvent(int action, int x, int y, long eventTime) {
        switch (action) {
            case MotionEvent.ACTION_MOVE:
                onMoveEvent(x, y);
                break;
            case MotionEvent.ACTION_DOWN:
            case 0x00000005://MotionEvent.ACTION_POINTER_DOWN:
                onDownEvent(x, y, eventTime);
                break;
            case MotionEvent.ACTION_UP:
            case 0x00000006://MotionEvent.ACTION_POINTER_UP:
                onUpEvent(x, y, eventTime);
                break;
            case MotionEvent.ACTION_CANCEL:
                onCancelEvent();
                break;
        }
    }

    void onDownEvent(int x, int y, long eventTime) {
        int keyIndex = mKeyState.onDownKey(x, y);
        mKeyboardLayoutHasBeenChanged = false;
        mKeyAlreadyProcessed = false;
        mIsRepeatableKey = false;
        checkMultiTap(eventTime, keyIndex);
        if (mListener != null) {
            if (isValidKeyIndex(keyIndex)) {
                Key key = mKeys[keyIndex];
                final int codeAtIndex = key.getCodeAtIndex(0, mKeyDetector.isKeyShifted(key));
                mListener.onPress(codeAtIndex);
                //also notifying about first down
                mListener.onFirstDownKey(codeAtIndex);
                // This onPress call may have changed keyboard layout. Those cases are detected at
                // {@link #setKeyboard}. In those cases, we should update keyIndex according to the
                // new keyboard layout.
                if (mKeyboardLayoutHasBeenChanged) {
                    mKeyboardLayoutHasBeenChanged = false;
                    keyIndex = mKeyState.onDownKey(x, y);
                }
            }
        }
        if (isValidKeyIndex(keyIndex)) {
            if (mKeys[keyIndex].repeatable) {
                repeatKey(keyIndex);
                mHandler.startKeyRepeatTimer(mDelayBeforeKeyRepeatStart, keyIndex, this);
                mIsRepeatableKey = true;
            }
            startLongPressTimer(keyIndex);
        }
        showKeyPreviewAndUpdateKey(keyIndex);
    }

    void onMoveEvent(int x, int y) {
        if (mKeyAlreadyProcessed)
            return;
        final KeyState keyState = mKeyState;
        final int oldKeyIndex = keyState.getKeyIndex();
        int keyIndex = keyState.onMoveKey(x, y);
        final Key oldKey = getKey(oldKeyIndex);
        if (isValidKeyIndex(keyIndex)) {
            if (oldKey == null) {
                // The pointer has been slid in to the new key, but the finger was not on any keys.
                // In this case, we must call onPress() to notify that the new key is being pressed.
                if (mListener != null) {
                    Key key = getKey(keyIndex);
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
                if (mListener != null)
                    mListener.onRelease(oldKey.getCodeAtIndex(0, mKeyDetector.isKeyShifted(oldKey)));
                resetMultiTap();
                if (mListener != null) {
                    Key key = getKey(keyIndex);
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
                if (oldKeyIndex != keyIndex) {
                    mProxy.hidePreview(oldKeyIndex, this);
                }
            }
        } else {
            if (oldKey != null && !isMinorMoveBounce(x, y, keyIndex)) {
                // The pointer has been slid out from the previous key, we must call onRelease() to
                // notify that the previous key has been released.
                if (mListener != null)
                    mListener.onRelease(oldKey.getCodeAtIndex(0, mKeyDetector.isKeyShifted(oldKey)));
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
        mHandler.cancelAllMessages();
        mProxy.hidePreview(mKeyState.getKeyIndex(), this);
        showKeyPreviewAndUpdateKey(NOT_A_KEY);
        if (mKeyAlreadyProcessed)
            return;
        int keyIndex = mKeyState.onUpKey(x, y);
        if (isMinorMoveBounce(x, y, keyIndex)) {
            // Use previous fixed key index and coordinates.
            keyIndex = mKeyState.getKeyIndex();
            x = mKeyState.getKeyX();
            y = mKeyState.getKeyY();
        }
        if (!mIsRepeatableKey) {
            detectAndSendKey(keyIndex, x, y, eventTime);
        }

        if (isValidKeyIndex(keyIndex))
            mProxy.invalidateKey(mKeys[keyIndex]);
    }

    void onCancelEvent() {
        mHandler.cancelAllMessages();
        int keyIndex = mKeyState.getKeyIndex();
        mProxy.hidePreview(keyIndex, this);
        showKeyPreviewAndUpdateKey(NOT_A_KEY);
        if (isValidKeyIndex(keyIndex)) mProxy.invalidateKey(mKeys[keyIndex]);
        setAlreadyProcessed();
    }

    void repeatKey(int keyIndex) {
        Key key = getKey(keyIndex);
        if (key != null) {
            // While key is repeating, because there is no need to handle multi-tap key, we can
            // pass -1 as eventTime argument.
            detectAndSendKey(keyIndex, key.x, key.y, -1);
        }
    }

    int getLastX() {
        return mKeyState.getLastX();
    }

    int getLastY() {
        return mKeyState.getLastY();
    }

    private boolean isMinorMoveBounce(int x, int y, int newKey) {
        if (mKeys == null || mKeyHysteresisDistanceSquared < 0)
            throw new IllegalStateException("keyboard and/or hysteresis not set");
        int curKey = mKeyState.getKeyIndex();
        if (newKey == curKey) {
            return true;
        } else if (isValidKeyIndex(curKey)) {
            return getSquareDistanceToKeyEdge(x, y, mKeys[curKey]) < mKeyHysteresisDistanceSquared;
        } else {
            return false;
        }
    }

    private static int getSquareDistanceToKeyEdge(int x, int y, Key key) {
        final int left = key.x;
        final int right = key.x + key.width;
        final int top = key.y;
        final int bottom = key.y + key.height;
        final int edgeX = x < left ? left : (x > right ? right : x);
        final int edgeY = y < top ? top : (y > bottom ? bottom : y);
        final int dx = x - edgeX;
        final int dy = y - edgeY;
        return dx * dx + dy * dy;
    }

    private void showKeyPreviewAndUpdateKey(int keyIndex) {
        updateKey(keyIndex);
        mProxy.showPreview(keyIndex, this);
    }

    private void startLongPressTimer(int keyIndex) {
        mHandler.startLongPressTimer(mLongPressKeyTimeout, keyIndex, this);
    }

    private void detectAndSendKey(int index, int x, int y, long eventTime) {
        final OnKeyboardActionListener listener = mListener;
        final Key key = getKey(index);

        if (key == null) {
            if (listener != null)
                listener.onCancel();
        } else {
            if (key.text != null) {
                if (listener != null) {
                    listener.onText(key, key.text);
                    listener.onRelease(0); // dummy key code
                }
            } else {
                int code = key.getCodeAtIndex(0, mKeyDetector.isKeyShifted(key));
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
                    code = getMultiTapCode(key);
                }
                /*
                 * Swap the first and second values in the codes array if the primary code is not
                 * the first value but the second value in the array. This happens when key
                 * debouncing is in effect.
                 */
                if (nearByKeyCodes.length >= 2 && nearByKeyCodes[0] != code && nearByKeyCodes[1] == code) {
                    nearByKeyCodes[1] = nearByKeyCodes[0];
                    nearByKeyCodes[0] = code;
                }
                if (listener != null) {
                    listener.onKey(code, key, mTapCount, nearByKeyCodes, x >= 0 || y >= 0);
                    listener.onRelease(code);
                    if (multiTapStarted)
                        mListener.onMultiTapEnded();
                }
            }
            mSharedPointerTrackersData.lastSentKeyIndex = index;
            mLastTapTime = eventTime;
        }
    }

    /**
     * Handle multi-tap keys by producing the key label for the current multi-tap state.
     */
    CharSequence getPreviewText(Key key) {
        boolean isShifted = mKeyDetector.isKeyShifted(key);
        AnyKey anyKey = (AnyKey) key;
        if (isShifted && !TextUtils.isEmpty(anyKey.shiftedKeyLabel)) {
            return anyKey.shiftedKeyLabel;
        } else if (!TextUtils.isEmpty(anyKey.label)) {
            return isShifted ? anyKey.label.toString().toUpperCase(Locale.getDefault()) : anyKey.label;
        } else {
            return Character.toString(getMultiTapCode(key));
        }
    }

    private char getMultiTapCode(final Key key) {
        final int codesCount = key.getCodesCount();
        if (codesCount == 0) return KeyCodes.SPACE;//space is good for nothing
        int safeMultiTapIndex = mTapCount < 0 ? 0 : mTapCount % codesCount;
        return (char) key.getCodeAtIndex(safeMultiTapIndex, mKeyDetector.isKeyShifted(key));
    }

    private void resetMultiTap() {
        mSharedPointerTrackersData.lastSentKeyIndex = NOT_A_KEY;
        mTapCount = 0;
        mLastTapTime = -1;
        mInMultiTap = false;
    }

    private void checkMultiTap(long eventTime, int keyIndex) {
        Key key = getKey(keyIndex);
        if (key == null)
            return;

        final boolean isMultiTap =
                (eventTime < mLastTapTime + mMultiTapKeyTimeout && keyIndex == mSharedPointerTrackersData.lastSentKeyIndex);
        if (key.getCodesCount() > 1) {
            mInMultiTap = true;
            if (isMultiTap) {
                mTapCount = (mTapCount + 1) % key.getCodesCount();
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
}
