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

import android.content.Context;
import android.graphics.Canvas;
import android.view.GestureDetector;
import android.view.inputmethod.InputConnection;
import android.widget.AbsListView;

import com.anysoftkeyboard.WordComposer;

public interface DeviceSpecific {

    public String getApiLevel();

    public MultiTouchSupportLevel getMultiTouchSupportLevel(Context appContext);

    public GestureDetector createGestureDetector(Context appContext, AskOnGestureListener listener);

    public void commitCorrectionToInputConnection(InputConnection ic, WordComposer word);

    public void performListScrollToPosition(AbsListView listView, int position);

    public boolean isHardwareAcceleratedCanvas(Canvas canvas);
}
