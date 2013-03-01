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

package com.anysoftkeyboard.keyboards;

import android.content.Context;
import android.util.SparseArray;
import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.keyboards.Keyboard.Key;
import com.menny.android.anysoftkeyboard.R;

import java.util.Stack;

public class KeyboardCondensor {

    private static class KeySize {
        public final int width;
        public final int height;
        public final int X;
        public final int Y;

        public KeySize(int w, int h, int x, int y) {
            width = w;
            height = h;
            X = x;
            Y = y;
        }
    }

    private static final String TAG = "ASK - KeyboardCondensor";

    private boolean mKeyboardCondensed = false;
    private final SparseArray<KeySize> mKeySizesMap = new SparseArray<KeySize>();
    private final AnyKeyboard mKeyboard;
    private final float mCondensingFactor;

    public KeyboardCondensor(Context askContext, AnyKeyboard keyboard) {
        mKeyboard = keyboard;
        mCondensingFactor = ((float) askContext.getResources()
                .getInteger(R.integer.condensing_precentage)) / 100f;
    }

    public void setCondensedKeys(boolean condensed) {
        if (condensed == mKeyboardCondensed)
            return;

        if (condensed) {
            mKeySizesMap.clear();
            if (mCondensingFactor > 0.97f)
                return;

            // now to determine the watershed line: keys will be align to the
            // edges
            final int keyboardWidth = mKeyboard.getMinWidth();
            final int watershedLineX = keyboardWidth / 2;

            int currentLeftX = 0;
            int currentRightX = keyboardWidth;
            int currentY = 0;
            Stack<Key> rightKeys = new Stack<Key>();
            boolean flipSideLeft = true;
            int i = 0;
            Key spaceKey = null;
            for (Key k : mKeyboard.getKeys()) {
                // first, store the original values
                mKeySizesMap.put(i, new KeySize(k.width, k.height,
                        k.x, k.y));
                i++;

                if (currentY != k.y)// on new line, we want to handle the left
                // side of the keyboard
                {
                    flipSideLeft = !flipSideLeft;

                    condenseRightSide(mCondensingFactor, keyboardWidth,
                            currentRightX, rightKeys, spaceKey);

                    currentLeftX = 0;
                    currentRightX = keyboardWidth;
                    currentY = k.y;
                    rightKeys.clear();
                }

                int targetWidth = (int) (k.width * mCondensingFactor);
                int keyMidPoint = (k.gap + k.x + (k.width / 2));
                if ((k.gap + k.x) < watershedLineX
                        && k.codes[0] == KeyCodes.SPACE) {
                    // space is a special case, I want to make it as wide as
                    // possible
                    spaceKey = k;
                    currentLeftX = condenseLeftSide(mCondensingFactor,
                            currentLeftX, k, targetWidth);
                } else if (keyMidPoint < (watershedLineX - 5)) {
                    currentLeftX = condenseLeftSide(mCondensingFactor,
                            currentLeftX, k, targetWidth);
                } else if (keyMidPoint > (watershedLineX + 5)) {
                    // to handle later. I need to find the last gap
                    currentRightX = stackRightSideKeyForLater(rightKeys, k,
                            targetWidth);
                } else {
                    if (flipSideLeft) {
                        currentLeftX = condenseLeftSide(mCondensingFactor,
                                currentLeftX, k, targetWidth);
                    } else {
                        currentRightX = stackRightSideKeyForLater(rightKeys, k,
                                targetWidth);
                    }
                }
            }
            // now to condense the last row
            condenseRightSide(mCondensingFactor, keyboardWidth, currentRightX,
                    rightKeys, spaceKey);
        } else {
            // restoring sizes
            int i = 0;
            for (Key k : mKeyboard.getKeys()) {
                KeySize originalSize = mKeySizesMap.get(i);
                k.width = originalSize.width;
                k.height = originalSize.height;
                k.x = originalSize.X;
                k.y = originalSize.Y;
                i++;
            }
        }

        mKeyboardCondensed = condensed;
    }

    int stackRightSideKeyForLater(Stack<Key> rightKeys, Key k, int targetWidth) {
        int currentRightX;
        rightKeys.push(k);
        currentRightX = k.x + k.width;
        k.width = targetWidth;
        return currentRightX;
    }

    int condenseLeftSide(final float CONDENSING_FACTOR, int currentLeftX,
                         Key k, int targetWidth) {
        currentLeftX += (k.gap * CONDENSING_FACTOR);
        k.x = currentLeftX;
        k.width = targetWidth;
        currentLeftX += k.width;
        return currentLeftX;
    }

    void condenseRightSide(final float CONDENSING_FACTOR,
                           final int keyboardWidth, int currentRightX, Stack<Key> rightKeys,
                           Key spaceKey) {
        // currentRightX holds the rightest x+width point. condensing a bit
        currentRightX = (int) (keyboardWidth - ((keyboardWidth - currentRightX) * CONDENSING_FACTOR));
        while (!rightKeys.isEmpty()) {
            Key rightKey = rightKeys.pop();

            currentRightX -= rightKey.width;// already holds the new width
            rightKey.x = currentRightX;
            currentRightX -= (rightKey.gap * CONDENSING_FACTOR);
        }
        // now to handle the space, which will hold as much as possible
        if (spaceKey != null) {
            spaceKey.width = currentRightX - spaceKey.x;
        }
    }
}
