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
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;

import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.KeyboardAddOnAndBuilder;

import java.util.ArrayList;
import java.util.List;

@TargetApi(14)
public class DeviceSpecific_V14 extends DeviceSpecific_V11 {
    @Override
    public String getApiLevel() {
        return "DeviceSpecific_V14";
    }

    @Override
    public final void reportInputMethodSubtypes(@NonNull InputMethodManager inputMethodManager, @NonNull String imeId, @NonNull List<KeyboardAddOnAndBuilder> builders) {
        List<InputMethodSubtype> subtypes = new ArrayList<>();
        for (KeyboardAddOnAndBuilder builder : builders) {
            final String locale = builder.getKeyboardLocale();
            if (TextUtils.isEmpty(locale)) continue;
            subtypes.add(createSubtype(locale, builder.getId()));
        }
        inputMethodManager.setAdditionalInputMethodSubtypes(imeId, subtypes.toArray(new InputMethodSubtype[subtypes.size()]));
    }

    @Override
    public void reportCurrentInputMethodSubtypes(@NonNull InputMethodManager inputMethodManager, @NonNull String imeId, @NonNull IBinder token, @NonNull AnyKeyboard keyboard) {
        if (keyboard.getLocale() != null)
            inputMethodManager.setInputMethodAndSubtype(token, imeId, createSubtype(keyboard.getLocale().toString(), keyboard.getKeyboardPrefId()));
    }

    protected InputMethodSubtype createSubtype(String locale, String keyboardId) {
        //noinspection deprecation
        return new InputMethodSubtype(0, 0, locale, "", keyboardId, false, false);
    }
}
