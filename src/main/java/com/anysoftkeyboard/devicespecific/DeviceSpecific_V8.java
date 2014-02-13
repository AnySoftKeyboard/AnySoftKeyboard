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
import android.content.pm.PackageManager;
import android.view.GestureDetector;
import android.widget.AbsListView;

@TargetApi(8)
public class DeviceSpecific_V8 extends DeviceSpecific_V7 {
    @Override
    public String getApiLevel() {
        return "DeviceSpecific_V8";
    }

    @Override
    public MultiTouchSupportLevel getMultiTouchSupportLevel(Context appContext) {
        PackageManager pkg = appContext.getPackageManager();
        boolean hasDistintMultitouch = pkg
                .hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH_DISTINCT);
        boolean hasMultitouch = pkg
                .hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH);

        if (hasDistintMultitouch)
            return MultiTouchSupportLevel.Distinct;
        else if (hasMultitouch)
            return MultiTouchSupportLevel.Basic;
        else
            return MultiTouchSupportLevel.None;
    }

    @Override
    public GestureDetector createGestureDetector(Context appContext,
                                                 AskOnGestureListener listener) {
        return new AskV8GestureDetector(appContext, listener, null, true/*ignore multi-touch*/);
    }

    @Override
    public void performListScrollToPosition(AbsListView listView, int position) {
        listView.smoothScrollToPosition(position);
    }
}