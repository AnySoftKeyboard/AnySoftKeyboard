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

package com.anysoftkeyboard.devicespecific;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import androidx.annotation.NonNull;
import androidx.core.view.MotionEventCompat;

@TargetApi(Build.VERSION_CODES.FROYO)
public class AskV8GestureDetector extends GestureDetector {
    private static final int NOT_A_POINTER_ID = -1;

    protected final ScaleGestureDetector mScaleGestureDetector;
    private final AskOnGestureListener mListener;

    private int mSingleFingerEventPointerId = NOT_A_POINTER_ID;

    public AskV8GestureDetector(Context context, AskOnGestureListener listener) {
        super(context, listener, null, true /*ignore multi-touch*/);

        mListener = listener;

        mScaleGestureDetector =
                new ScaleGestureDetector(
                        context,
                        new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                            @Override
                            public boolean onScale(ScaleGestureDetector detector) {
                                final float factor = detector.getScaleFactor();
                                if (factor > 1.1) return mListener.onSeparate(factor);
                                else if (factor < 0.9) return mListener.onPinch(factor);

                                return false;
                            }
                        });
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent ev) {
        int singleFingerEventPointerId = mSingleFingerEventPointerId;

        // I want to keep track on the first finger
        // (https://github.com/AnySoftKeyboard/AnySoftKeyboard/issues/300)
        // CHECKSTYLE:OFF: missingswitchdefault
        switch (MotionEventCompat.getActionMasked(ev)) {
            case MotionEvent.ACTION_DOWN:
                if (ev.getPointerCount() == 1) {
                    mSingleFingerEventPointerId = ev.getPointerId(0);
                    singleFingerEventPointerId = mSingleFingerEventPointerId;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (ev.getPointerCount() == 1) mSingleFingerEventPointerId = NOT_A_POINTER_ID;
                break;
        }
        // CHECKSTYLE:ON: missingswitchdefault
        try {
            // https://github.com/AnySoftKeyboard/AnySoftKeyboard/issues/26
            mScaleGestureDetector.onTouchEvent(ev);
        } catch (IllegalArgumentException e) {
            // I have nothing I can do here.
        } catch (ArrayIndexOutOfBoundsException e) {
            // I have nothing I can do here.
        }
        // I'm going to pass the event to the super, only if it is a single touch, and the event is
        // for the first finger
        // https://github.com/AnySoftKeyboard/AnySoftKeyboard/issues/300
        if (ev.getPointerCount() == 1 && ev.getPointerId(0) == singleFingerEventPointerId)
            return super.onTouchEvent(ev);
        else return false;
    }
}
