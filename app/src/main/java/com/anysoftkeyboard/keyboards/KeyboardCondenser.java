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

import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.keyboards.Keyboard.Key;
import com.menny.android.anysoftkeyboard.R;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class KeyboardCondenser {

    private static class KeySize {
        final int width;
        final int height;
        final int X;
        final int Y;

        public KeySize(int w, int h, int x, int y) {
            width = w;
            height = h;
            X = x;
            Y = y;
        }
    }

    private CondenseType mKeyboardCondenseType = CondenseType.None;
    private List<KeySize> mKeySizesMap = null;//it is usually not used, so I'll create an instance when first needed.
    private final AnyKeyboard mKeyboard;
    private final float mCondensingFullFactor;
    private final float mCondensingEdgeFactor;

    public KeyboardCondenser(Context askContext, AnyKeyboard keyboard) {
        mKeyboard = keyboard;
        mCondensingFullFactor = ((float) askContext.getResources()
                .getInteger(R.integer.condensing_precentage)) / 100f;
        mCondensingEdgeFactor = ((float) askContext.getResources()
                .getInteger(R.integer.condensing_precentage_edge)) / 100f;
    }

    public boolean setCondensedKeys(CondenseType condenseType) {
        if (mKeyboardCondenseType.equals(condenseType))
            return false;//not changed

        final float condensingFactor;
        switch (condenseType) {
            case CompactToLeft:
            case CompactToRight:
                condensingFactor = mCondensingEdgeFactor;
                break;
            default:
                condensingFactor = mCondensingFullFactor;
                break;
        }

        if (!condenseType.equals(CondenseType.None) && condensingFactor > 0.97f)
            return false;

        List<Key> keys = mKeyboard.getKeys();

        if (mKeySizesMap == null)
            mKeySizesMap = new ArrayList<>(keys.size());

        //restoring sizes
        List<KeySize> stashedKeySizes = mKeySizesMap;
        if (stashedKeySizes.size() > 0) {
            //we have condensed before
            if (stashedKeySizes.size() != keys.size())
                throw new IllegalStateException("The size of the stashed keys and the actual keyboard keys is not the same!");
            for (int i = 0; i < stashedKeySizes.size(); i++) {
                Key k = keys.get(i);
                KeySize originalSize = mKeySizesMap.get(i);
                k.width = originalSize.width;
                k.height = originalSize.height;
                k.x = originalSize.X;
                k.y = originalSize.Y;
            }
        }
        //back to original state, no need to keep those key-size data anymore
        mKeySizesMap.clear();

        final int keyboardWidth = mKeyboard.getMinWidth();
        switch (condenseType) {
            case Split:
                splitKeys(keyboardWidth, keyboardWidth / 2, condensingFactor);
                break;
            case CompactToLeft:
                splitKeys(keyboardWidth, keyboardWidth, condensingFactor);
                break;
            case CompactToRight:
                splitKeys(keyboardWidth, 0, condensingFactor);
                break;
            case None:
                // keys already restored
                break;
            default:
                throw new IllegalArgumentException("Unknown condensing type given: " + condenseType);
        }

        mKeyboardCondenseType = condenseType;
        //changed
        return true;
    }

    private void splitKeys(final int keyboardWidth, final int watershedLineX, final float condensingFactor) {
        int currentLeftX = 0;
        int currentRightX = keyboardWidth;
        int currentY = 0;
        Deque<Key> rightKeys = new ArrayDeque<>();
        boolean flipSideLeft = true;
        Key spaceKey = null;
        for (Key k : mKeyboard.getKeys()) {
            // first, store the original values
            mKeySizesMap.add(new KeySize(k.width, k.height, k.x, k.y));

            if (currentY != k.y)// on new line, we want to handle the left
            // side of the keyboard
            {
                flipSideLeft = !flipSideLeft;

                condenseRightSide(condensingFactor, keyboardWidth,
                        currentRightX, rightKeys, spaceKey);

                currentLeftX = 0;
                currentRightX = keyboardWidth;
                currentY = k.y;
                rightKeys.clear();
            }

            int targetWidth = (int) (k.width * condensingFactor);
            int keyMidPoint = (k.gap + k.x + (k.width / 2));
            if (k.getPrimaryCode() == KeyCodes.SPACE &&
                    (k.gap + k.x) < watershedLineX &&//one side is to the left,
                    (k.gap + k.x + k.width) > watershedLineX) { //the other side of the key is to the right of the watershed-line
                // space is a special case, I want to make it as wide as
                // possible (since it is a space-bar in the middle of the screen
                spaceKey = k;
                currentLeftX = condenseLeftSide(condensingFactor,
                        currentLeftX, k, targetWidth);
            } else if (keyMidPoint < (watershedLineX - 5)) {
                currentLeftX = condenseLeftSide(condensingFactor,
                        currentLeftX, k, targetWidth);
            } else if (keyMidPoint > (watershedLineX + 5)) {
                // to handle later. I need to find the last gap
                currentRightX = stackRightSideKeyForLater(rightKeys, k,
                        targetWidth);
            } else {
                if (flipSideLeft) {
                    currentLeftX = condenseLeftSide(condensingFactor,
                            currentLeftX, k, targetWidth);
                } else {
                    currentRightX = stackRightSideKeyForLater(rightKeys, k,
                            targetWidth);
                }
            }
        }
        // now to condense the last row
        condenseRightSide(condensingFactor, keyboardWidth, currentRightX, rightKeys, spaceKey);
    }

    private int stackRightSideKeyForLater(Deque<Key> rightKeys, Key k, int targetWidth) {
        int currentRightX;
        rightKeys.push(k);
        currentRightX = k.x + k.width;
        k.width = targetWidth;
        return currentRightX;
    }

    private int condenseLeftSide(final float condensingFactor, int currentLeftX,
                                 Key k, int targetWidth) {
        currentLeftX = (int) (currentLeftX + (k.gap * condensingFactor));
        k.x = currentLeftX;
        k.width = targetWidth;
        currentLeftX += k.width;
        return currentLeftX;
    }

    private void condenseRightSide(final float condensingFactor,
                                   final int keyboardWidth, int currentRightX, Deque<Key> rightKeys,
                                   Key spaceKey) {
        // currentRightX holds the rightest x+width point. condensing a bit
        currentRightX = (int) (keyboardWidth - ((keyboardWidth - currentRightX) * condensingFactor));
        while (!rightKeys.isEmpty()) {
            Key rightKey = rightKeys.pop();

            currentRightX -= rightKey.width;// already holds the new width
            rightKey.x = currentRightX;
            currentRightX = (int) (currentRightX - (rightKey.gap * condensingFactor));
        }
        // now to handle the space, which will hold as much as possible
        if (spaceKey != null) {
            spaceKey.width = currentRightX - spaceKey.x;
        }
    }
}
