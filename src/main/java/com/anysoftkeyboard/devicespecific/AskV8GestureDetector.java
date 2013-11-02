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
import android.os.Handler;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import com.anysoftkeyboard.utils.Log;

@TargetApi(8)
public class AskV8GestureDetector extends GestureDetector {
    private static final String TAG = "AskV8GestureDetector";

    private final ScaleGestureDetector mScaleGestureDetector;
    private final AskOnGestureListener mListener;
    private boolean mScaleEventHandled = false;

    public AskV8GestureDetector(Context context, AskOnGestureListener listener,
                                Handler handler, boolean ignoreMultitouch) {
        super(context, listener, handler, ignoreMultitouch);

        mListener = listener;

        mScaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            public void onScaleEnd(ScaleGestureDetector detector) {
                final float factor = detector.getScaleFactor();
                Log.d(TAG, "onScaleEnd factor " + factor);

                if (factor > 1.1)
                    mScaleEventHandled = mListener.onSeparate(factor);
                else if (factor < 0.9)
                    mScaleEventHandled = mListener.onPinch(factor);
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        try {
            //https://github.com/AnySoftKeyboard/AnySoftKeyboard/issues/26
            mScaleGestureDetector.onTouchEvent(ev);
        } catch (IllegalArgumentException e) {
            //I have nothing I can do here.
        } catch (ArrayIndexOutOfBoundsException e) {
            //I have nothing I can do here.
        }
        final boolean scaleEventHandled = mScaleEventHandled;
        mScaleEventHandled = false;
        return super.onTouchEvent(ev) || scaleEventHandled;
    }

}
