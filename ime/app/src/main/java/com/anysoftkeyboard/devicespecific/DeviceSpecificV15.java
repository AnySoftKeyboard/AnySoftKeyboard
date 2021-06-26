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
import android.database.ContentObserver;
import android.os.Build;
import android.os.IBinder;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.inputmethod.CorrectionInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.dictionaries.BTreeDictionary;
import com.anysoftkeyboard.dictionaries.DictionaryContentObserver;
import com.anysoftkeyboard.keyboards.KeyboardAddOnAndBuilder;
import java.util.ArrayList;
import java.util.List;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
public class DeviceSpecificV15 implements DeviceSpecific {
    @Override
    public String getApiLevel() {
        return "DeviceSpecificV15";
    }

    @Override
    public GestureDetector createGestureDetector(
            Context appContext, AskOnGestureListener listener) {
        return new AskV8GestureDetector(appContext, listener);
    }

    @Override
    public void commitCorrectionToInputConnection(
            InputConnection ic, int wordOffsetInInput, CharSequence oldWord, CharSequence newWord) {
        ic.commitText(newWord, 1);
        CorrectionInfo correctionInfo = new CorrectionInfo(wordOffsetInInput, oldWord, newWord);
        ic.commitCorrection(correctionInfo);
    }

    @Override
    public final void reportInputMethodSubtypes(
            @NonNull InputMethodManager inputMethodManager,
            @NonNull String imeId,
            @NonNull List<KeyboardAddOnAndBuilder> builders) {
        List<InputMethodSubtype> subtypes = new ArrayList<>();
        for (KeyboardAddOnAndBuilder builder : builders) {
            Logger.d(
                    "reportInputMethodSubtypes",
                    "reportInputMethodSubtypes for %s with locale %s",
                    builder.getId(),
                    builder.getKeyboardLocale());
            final String locale = builder.getKeyboardLocale();
            if (TextUtils.isEmpty(locale)) continue;
            InputMethodSubtype subtype = createSubtype(locale, builder.getId());
            Logger.d(
                    "reportInputMethodSubtypes",
                    "created subtype for %s with hash %s",
                    builder.getId(),
                    subtype);
            subtypes.add(subtype);
        }
        inputMethodManager.setAdditionalInputMethodSubtypes(
                imeId, subtypes.toArray(new InputMethodSubtype[0]));
    }

    @Override
    public void reportCurrentInputMethodSubtypes(
            @NonNull InputMethodManager inputMethodManager,
            @NonNull String imeId,
            @NonNull IBinder token,
            @Nullable String keyboardLocale,
            @NonNull CharSequence keyboardId) {
        if (keyboardLocale != null)
            inputMethodManager.setInputMethodAndSubtype(
                    token, imeId, createSubtype(keyboardLocale, keyboardId));
    }

    @Override
    public ContentObserver createDictionaryContentObserver(@NonNull BTreeDictionary dictionary) {
        return new DictionaryContentObserver(dictionary);
    }

    @Override
    public Clipboard createClipboard(@NonNull Context applicationContext) {
        return new ClipboardV11(applicationContext);
    }

    protected InputMethodSubtype createSubtype(String locale, CharSequence keyboardId) {
        return new InputMethodSubtype(0, 0, locale, "", keyboardId.toString(), false, false);
    }

    @Override
    public PressVibrator createPressVibrator(@NonNull Vibrator vibe) {
        return new PressVibratorV1(vibe);
    }
}
