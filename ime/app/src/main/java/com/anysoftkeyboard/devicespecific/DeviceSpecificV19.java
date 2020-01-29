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
public class DeviceSpecificV19 extends DeviceSpecificV16 {
    @Override
    public String getApiLevel() {
        return "DeviceSpecificV19";
    }

    @Override
    public GestureDetector createGestureDetector(
            Context appContext, AskOnGestureListener listener) {
        return new AskV19GestureDetector(appContext, listener);
    }

    @Override
    protected InputMethodSubtype createSubtype(String locale, CharSequence keyboardId) {
        return buildAndFillSubtypeBuilder(locale, keyboardId).build();
    }

    protected InputMethodSubtype.InputMethodSubtypeBuilder buildAndFillSubtypeBuilder(
            String locale, CharSequence keyboardId) {
        return new InputMethodSubtype.InputMethodSubtypeBuilder()
                .setSubtypeNameResId(0)
                .setSubtypeId(calculateSubtypeIdFromKeyboardId(keyboardId))
                .setIsAsciiCapable(true)
                .setSubtypeLocale(locale)
                .setSubtypeMode("keyboard")
                .setSubtypeExtraValue(keyboardId.toString());
    }

    private static int calculateSubtypeIdFromKeyboardId(CharSequence keyboardId) {
        long hash = 0;
        for (int i = 0; i < keyboardId.length(); i++) {
            hash = hash * 31L + keyboardId.charAt(i);
        }

        return (int) (hash ^ (hash >>> 32));
    }

    @Override
    public void setupStrictMode() {
        StrictMode.setThreadPolicy(
                new StrictMode.ThreadPolicy.Builder()
                        .detectAll()
                        .penaltyLog()
                        .penaltyFlashScreen()
                        .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build());
    }
}
