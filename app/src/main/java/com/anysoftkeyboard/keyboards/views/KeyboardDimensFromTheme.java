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

public class KeyboardDimensFromTheme implements KeyboardDimens {

    private int mMaxKeyboardWidth;
    private float mKeyHorizontalGap;
    private float mRowVerticalGap;
    private int mNormalKeyHeight;
    private int mSmallKeyHeight;
    private int mLargeKeyHeight;

    KeyboardDimensFromTheme() {
    }

    @Override
    public int getKeyboardMaxWidth() {
        return mMaxKeyboardWidth;
    }

    @Override
    public float getKeyHorizontalGap() {
        return mKeyHorizontalGap;
    }

    @Override
    public float getRowVerticalGap() {
        return mRowVerticalGap;
    }

    @Override
    public int getNormalKeyHeight() {
        return mNormalKeyHeight;
    }

    @Override
    public int getSmallKeyHeight() {
        return mSmallKeyHeight;
    }

    @Override
    public int getLargeKeyHeight() {
        return mLargeKeyHeight;
    }

    void setKeyboardMaxWidth(int maxKeyboardWidth) {
        mMaxKeyboardWidth = maxKeyboardWidth;
    }

    void setHorizontalKeyGap(float themeHorizontalKeyGap) {
        mKeyHorizontalGap = themeHorizontalKeyGap;
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

}
