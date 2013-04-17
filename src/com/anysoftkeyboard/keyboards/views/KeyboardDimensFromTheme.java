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

import com.anysoftkeyboard.keyboards.KeyboardDimens;

class KeyboardDimensFromTheme implements KeyboardDimens {

    private int mMaxKeyboardWidth;
    private float mKeyHorizontalGap;
    private float mRowVerticalGap;
    private int mNormalKeyHeight;
    private int mSmallKeyHeight;
    private int mLargeKeyHeight;
    private int mMaxKeyWidth = Integer.MAX_VALUE;

    KeyboardDimensFromTheme() {
    }

    public int getKeyboardMaxWidth() {
        return mMaxKeyboardWidth;
    }

    public int getKeyMaxWidth() {
        return mMaxKeyWidth;
    }

    public float getKeyHorizontalGap() {
        return mKeyHorizontalGap;
    }

    public float getRowVerticalGap() {
        return mRowVerticalGap;
    }

    public int getNormalKeyHeight() {
        return mNormalKeyHeight;
    }

    public int getSmallKeyHeight() {
        return mSmallKeyHeight;
    }

    public int getLargeKeyHeight() {
        return mLargeKeyHeight;
    }

    void setKeyboardMaxWidth(int maxKeyboardWidth) {
        mMaxKeyboardWidth = maxKeyboardWidth;
    }

    void setHorizontalKeyGap(float themeHorizotalKeyGap) {
        mKeyHorizontalGap = themeHorizotalKeyGap;
    }

    void setVerticalRowGap(float themeVerticalRowGap) {
        mRowVerticalGap = themeVerticalRowGap;
    }

    void setNormalKeyHeight(float themeNormalKeyHeight) {
        mNormalKeyHeight = (int) themeNormalKeyHeight;
    }

    void setLargeKeyHeight(float themeLargeKeyHeight) {
        mLargeKeyHeight = (int) themeLargeKeyHeight;
    }

    void setSmallKeyHeight(float themeSmallKeyHeight) {
        mSmallKeyHeight = (int) themeSmallKeyHeight;
    }

    void setKeyMaxWidth(int keyMaxWidth) {
        mMaxKeyWidth = keyMaxWidth;
    }

}
