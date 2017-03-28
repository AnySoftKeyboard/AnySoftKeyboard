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
import android.support.annotation.Nullable;

import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard.Key;

import java.util.Arrays;

public abstract class KeyDetector {
    @Nullable
    protected AnyKeyboard mKeyboard;

    private final int[] mNearByCodes;
    @NonNull
    private Key[] mKeys = new Key[0];

    private int mCorrectionX;

    private int mCorrectionY;

    protected boolean mProximityCorrectOn;

    protected int mProximityThresholdSquare;
    @Nullable
    private Key mShiftKey;

    protected KeyDetector() {
        mNearByCodes = new int[getMaxNearbyKeys()];
    }

    public Key[] setKeyboard(AnyKeyboard keyboard, @Nullable Key shiftKey) {
        mShiftKey = shiftKey;
        mKeyboard = keyboard;

        if (keyboard == null) return mKeys = new Key[0];
        return mKeys = mKeyboard.getKeys().toArray(new Key[mKeyboard.getKeys().size()]);
    }

    public void setCorrection(float correctionX, float correctionY) {
        mCorrectionX = (int) correctionX;
        mCorrectionY = (int) correctionY;
    }

    protected int getTouchX(int x) {
        return x + mCorrectionX;
    }

    protected int getTouchY(int y) {
        return y + mCorrectionY;
    }

    protected Key[] getKeys() {
        return mKeys;
    }

    public void setProximityCorrectionEnabled(boolean enabled) {
        mProximityCorrectOn = enabled;
    }

    public void setProximityThreshold(int threshold) {
        mProximityThresholdSquare = threshold * threshold;
    }

    /**
     * Allocates array that can hold all key indices returned by {@link #getKeyIndexAndNearbyCodes}
     * method. The maximum size of the array should be computed by {@link #getMaxNearbyKeys}.
     *
     * @return Allocates and returns an array that can hold all key indices returned by
     * {@link #getKeyIndexAndNearbyCodes} method. All elements in the returned array are
     * initialized by {@link AnyKeyboardViewBase#NOT_A_KEY}
     * value.
     */
    public int[] newCodeArray() {
        Arrays.fill(mNearByCodes, AnyKeyboardViewBase.NOT_A_KEY);
        return mNearByCodes;
    }

    /**
     * Computes maximum size of the array that can contain all nearby key indices returned by
     * {@link #getKeyIndexAndNearbyCodes}.
     *
     * @return Returns maximum size of the array that can contain all nearby key indices returned
     * by {@link #getKeyIndexAndNearbyCodes}.
     */
    protected abstract int getMaxNearbyKeys();

    /**
     * Finds all possible nearby key indices around a touch event point and returns the nearest key
     * index. The algorithm to determine the nearby keys depends on the threshold set by
     * {@link #setProximityThreshold(int)} and the mode set by
     * {@link #setProximityCorrectionEnabled(boolean)}.
     *
     * @param x       The x-coordinate of a touch point
     * @param y       The y-coordinate of a touch point
     * @param allKeys All nearby key indices are returned in this array
     * @return The nearest key index
     */
    public abstract int getKeyIndexAndNearbyCodes(int x, int y, int[] allKeys);

    public boolean isKeyShifted(Key key) {
        if (mKeyboard == null) return false;
        AnyKeyboard.AnyKey anyKey = (AnyKeyboard.AnyKey) key;
        return mKeyboard.keyboardSupportShift() &&
                ((mShiftKey != null && mShiftKey.pressed) || (anyKey.isShiftCodesAlways() && mKeyboard.isShifted()));
    }
}
