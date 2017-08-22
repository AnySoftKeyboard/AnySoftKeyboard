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

package com.anysoftkeyboard.devicespecific;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.StrictMode;
import android.view.GestureDetector;
import android.view.inputmethod.InputMethodSubtype;

@TargetApi(19)
public class DeviceSpecificV19 extends DeviceSpecificV14 {
    @Override
    public String getApiLevel() {
        return "DeviceSpecificV19";
    }

    @Override
    public GestureDetector createGestureDetector(Context appContext, AskOnGestureListener listener) {
        return new AskV19GestureDetector(appContext, listener);
    }

    protected InputMethodSubtype createSubtype(String locale, CharSequence keyboardId) {
        return new InputMethodSubtype.InputMethodSubtypeBuilder()
                .setIsAsciiCapable(true)
                .setSubtypeLocale(locale)
                .setSubtypeExtraValue(keyboardId.toString())
                .build();
    }

    @Override
    public void setupStrictMode() {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .penaltyFlashScreen()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build());
    }
}
