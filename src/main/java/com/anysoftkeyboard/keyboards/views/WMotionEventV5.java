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

import android.annotation.TargetApi;
import android.view.MotionEvent;

@TargetApi(5)
public class WMotionEventV5 extends WMotionEventV3 {

    public int getActionMasked() {
        return mNativeMotionEvent.getAction() & MotionEvent.ACTION_MASK;
    }

    public int getPointerCount() {
        return mNativeMotionEvent.getPointerCount();
    }

    public int getActionIndex() {
        return (mNativeMotionEvent.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
    }

    public int getPointerId(int index) {
        return mNativeMotionEvent.getPointerId(index);
    }

    public float getX(int index) {
        return mNativeMotionEvent.getX(index);
    }

    public float getY(int index) {
        return mNativeMotionEvent.getY(index);
    }

    public int findPointerIndex(int pointerId) {
        return mNativeMotionEvent.findPointerIndex(pointerId);
    }

}
