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

package com.anysoftkeyboard.keyboardextensions;

import android.content.Context;
import com.anysoftkeyboard.addons.AddOnImpl;

public class KeyboardExtension extends AddOnImpl {

    public static final int TYPE_BOTTOM = 1;
    public static final int TYPE_TOP = 2;
    public static final int TYPE_EXTENSION = 3;
    public static final int TYPE_HIDDEN_BOTTOM = 4;

    private final int mKeyboardResId;
    private final int mExtensionType;

    public KeyboardExtension(Context askContext, Context packageContext, String id, int nameResId, int keyboardResId, int type,
                             String description, int sortIndex) {
        super(askContext, packageContext, id, nameResId, description, sortIndex);
        mKeyboardResId = keyboardResId;
        mExtensionType = type;
    }

    public int getKeyboardResId() {
        return mKeyboardResId;
    }

    public int getExtensionType() {
        return mExtensionType;
    }
}