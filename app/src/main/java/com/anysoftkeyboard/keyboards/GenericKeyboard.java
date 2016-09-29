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

    public GenericKeyboard(@NonNull AddOn keyboardAddOn, Context askContext, int xmlLayoutResId, int xmlLandscapeLayoutResId, String name, String prefKeyId, @KeyboardRowModeId int mode, boolean disableKeyPreviews) {
        super(keyboardAddOn, askContext, askContext, xmlLayoutResId, xmlLandscapeLayoutResId, prefKeyId, name, AddOn.INVALID_RES_ID, AddOn.INVALID_RES_ID, null, null, "", mode);
        setExtensionLayout(null);
        mDisableKeyPreviews = disableKeyPreviews;
    }

    public boolean disableKeyPreviews() {
        return mDisableKeyPreviews;
    }
}
