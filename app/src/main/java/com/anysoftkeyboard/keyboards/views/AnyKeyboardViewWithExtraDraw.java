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

package com.anysoftkeyboard.keyboards.views;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

import com.anysoftkeyboard.keyboards.views.extradraw.ExtraDraw;
import com.anysoftkeyboard.prefs.AnimationsLevel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class AnyKeyboardViewWithExtraDraw extends AnyKeyboardViewWithMiniKeyboard {
    private final List<ExtraDraw> mExtraDraws = new ArrayList<>();

    private AnimationsLevel mCurrentAnimationLevel;

    public AnyKeyboardViewWithExtraDraw(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AnyKeyboardViewWithExtraDraw(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mDisposables.add(mAnimationLevelSubject.subscribe(value -> mCurrentAnimationLevel = value));

    }

    public void addExtraDraw(ExtraDraw extraDraw) {
        if (!mAlwaysUseDrawText) {
            return; // not doing it with StaticLayout
        }

        if (mCurrentAnimationLevel == AnimationsLevel.None) {
            return; //no animations requested.
        }

        mExtraDraws.add(extraDraw);
        // it is ok to wait for the next loop.
        postInvalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!mExtraDraws.isEmpty()) {
            Iterator<ExtraDraw> extraDrawListIterator = mExtraDraws.iterator();
            while (extraDrawListIterator.hasNext()) {
                ExtraDraw extraDraw = extraDrawListIterator.next();
                if (!extraDraw.onDraw(canvas, mPaint, this)) {
                    extraDrawListIterator.remove();
                }
            }

            if (!mExtraDraws.isEmpty()) {
                //requesting another re-draw since we have more items waiting to be drawn
                // next frame
                postInvalidateDelayed(1000 / 60);// doing 60 frames per second;
            }
        }
    }
}
