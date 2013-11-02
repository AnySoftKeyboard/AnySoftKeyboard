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

import android.view.MotionEvent;

public class WMotionEventV3 implements WMotionEvent {
    protected MotionEvent mNativeMotionEvent;

    public final void setNativeMotionEvent(MotionEvent nativeMotionEvent) {
        mNativeMotionEvent = nativeMotionEvent;
    }

    public int getActionMasked() {
        return mNativeMotionEvent.getAction();
    }

    public int getPointerCount() {
        return 1;
    }

    public long getEventTime() {
        return mNativeMotionEvent.getEventTime();
    }

    public int getActionIndex() {
        return 1;
    }

    public int getPointerId(int index) {
        return 1;
    }

    public float getX(int index) {
        return mNativeMotionEvent.getX();
    }

    public float getY(int index) {
        return mNativeMotionEvent.getY();
    }

    public int findPointerIndex(int pointerId) {
        return 1;
    }
}
