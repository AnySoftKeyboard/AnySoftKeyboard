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

import com.anysoftkeyboard.keyboards.Keyboard.Key;

class MiniKeyboardKeyDetector extends KeyDetector {
    private static final int MAX_NEARBY_KEYS = 1;

    private final int mSlideAllowanceSquare;
    private final int mSlideAllowanceSquareTop;

    public MiniKeyboardKeyDetector(float slideAllowance) {
        super();
        mSlideAllowanceSquare = (int) (slideAllowance * slideAllowance);
        // Top slide allowance is slightly longer (sqrt(2) times) than other edges.
        mSlideAllowanceSquareTop = mSlideAllowanceSquare * 2;
    }

    @Override
    protected int getMaxNearbyKeys() {
        return MAX_NEARBY_KEYS;
    }

    @Override
    public int getKeyIndexAndNearbyCodes(int x, int y, int[] allKeys) {
        final Key[] keys = getKeys();
        final int touchX = getTouchX(x);
        final int touchY = getTouchY(y);
        int closestKeyIndex = AnyKeyboardViewBase.NOT_A_KEY;
        int closestKeyDist = (y < 0) ? mSlideAllowanceSquareTop : mSlideAllowanceSquare;
        final int keyCount = keys.length;
        for (int i = 0; i < keyCount; i++) {
            final Key key = keys[i];
            int dist = key.squaredDistanceFrom(touchX, touchY);
            if (dist < closestKeyDist) {
                closestKeyIndex = i;
                closestKeyDist = dist;
            }
        }
        if (allKeys != null && closestKeyIndex != AnyKeyboardViewBase.NOT_A_KEY) {
            final Key key = keys[closestKeyIndex];
            allKeys[0] = key.getCodeAtIndex(0, isKeyShifted(key));
        }
        return closestKeyIndex;
    }

    @Override
    public boolean isKeyShifted(Key key) {
        //in the mini-keyboard we want to shift the keys depending on the state of the parent keyboard.
        return mKeyboard.isShifted();
    }
}
