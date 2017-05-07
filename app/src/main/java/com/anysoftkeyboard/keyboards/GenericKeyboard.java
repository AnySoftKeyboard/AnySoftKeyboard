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

package com.anysoftkeyboard.keyboards;

import android.content.Context;
import android.support.annotation.NonNull;

import com.anysoftkeyboard.addons.AddOn;

public class GenericKeyboard extends ExternalAnyKeyboard {
    private final boolean mDisableKeyPreviews;
    private final String mKeyboardId;

    public GenericKeyboard(@NonNull AddOn keyboardAddOn, Context askContext, int xmlLayoutResId, int xmlLandscapeLayoutResId, String name, String prefKeyId, @KeyboardRowModeId int mode, boolean disableKeyPreviews) {
        super(keyboardAddOn, askContext, askContext, xmlLayoutResId, xmlLandscapeLayoutResId, name, AddOn.INVALID_RES_ID, AddOn.INVALID_RES_ID, null, null, "", filterPasswordMode(mode));
        mKeyboardId = prefKeyId;
        setExtensionLayout(null);
        mDisableKeyPreviews = disableKeyPreviews;
    }

    /**
     * This will ensure that password extra rows are not shown over a symbols keyboard.
     */
    @KeyboardRowModeId
    private static int filterPasswordMode(@KeyboardRowModeId int mode) {
        if (mode == KEYBOARD_ROW_MODE_PASSWORD) return KEYBOARD_ROW_MODE_NORMAL;
        else return mode;
    }

    public boolean disableKeyPreviews() {
        return mDisableKeyPreviews;
    }

    @NonNull
    @Override
    public CharSequence getKeyboardId() {
        return mKeyboardId;
    }
}
