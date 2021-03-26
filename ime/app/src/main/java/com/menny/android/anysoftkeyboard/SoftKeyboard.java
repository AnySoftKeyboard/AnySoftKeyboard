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

package com.menny.android.anysoftkeyboard;

import android.content.ComponentName;
import android.os.Handler;
import android.os.Looper;
import com.anysoftkeyboard.saywhat.PublicNotices;

/*
 * Why is this class exists?
 * Well, I first released ASK as SoftKeyboard main class, and then renamed the class, but I can't do that
 * and still support upgrade... so SoftKeyboard inherits from the actual class
 */
public class SoftKeyboard extends PublicNotices {

    /* DEVELOPERS NOTICE:
    This TURNED-OFF code is used to simulate
    a very slow InputConnection updates:
    On some devices and apps, the onUpdateSelection callback will be
    very delayed, and may get com.anysoftkeyboard.ime.AnySoftKeyboardSuggestions.mGlobalCursorPosition
    out-of-sync.
     */
    private static final boolean DELAY_SELECTION_UPDATES = false;
    private Handler mDelayer = null;

    @Override
    public void onCreate() {
        super.onCreate();
        if (DELAY_SELECTION_UPDATES) mDelayer = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onUpdateSelection(
            int oldSelStart,
            int oldSelEnd,
            int newSelStart,
            int newSelEnd,
            int candidatesStart,
            int candidatesEnd) {
        if (DELAY_SELECTION_UPDATES) {
            mDelayer.postDelayed(
                    () ->
                            SoftKeyboard.super.onUpdateSelection(
                                    oldSelStart,
                                    oldSelEnd,
                                    newSelStart,
                                    newSelEnd,
                                    candidatesStart,
                                    candidatesEnd),
                    1025);
        } else {
            super.onUpdateSelection(
                    oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd);
        }
    }

    @Override
    protected String getSettingsInputMethodId() {
        return new ComponentName(getApplication(), SoftKeyboard.class).flattenToShortString();
    }
}
