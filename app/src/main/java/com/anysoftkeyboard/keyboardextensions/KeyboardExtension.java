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

package com.anysoftkeyboard.keyboardextensions;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.annotation.XmlRes;

import com.anysoftkeyboard.addons.AddOnImpl;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class KeyboardExtension extends AddOnImpl {
    public static final int TYPE_BOTTOM = 1;
    public static final int TYPE_TOP = 2;
    public static final int TYPE_EXTENSION = 3;
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({TYPE_BOTTOM, TYPE_TOP, TYPE_EXTENSION})
    public @interface KeyboardExtensionType {}

    @KeyboardExtensionType
    public static int ensureValidType(final int keyboardExtensionType) {
        switch (keyboardExtensionType) {
            case TYPE_BOTTOM:
            case TYPE_TOP:
            case TYPE_EXTENSION:
                return keyboardExtensionType;
            default:
                throw new RuntimeException("Invalid keyboard-extension-type "+keyboardExtensionType);
        }
    }

    @XmlRes
    private final int mKeyboardResId;
    @KeyboardExtensionType
    private final int mExtensionType;

    public KeyboardExtension(@NonNull Context askContext, @NonNull Context packageContext, @NonNull String id, @StringRes int nameResId, @XmlRes int keyboardResId, @KeyboardExtensionType int type, @NonNull String description, int sortIndex) {
        super(askContext, packageContext, id, nameResId, description, sortIndex);
        mKeyboardResId = keyboardResId;
        mExtensionType = type;
    }

    @XmlRes
    public int getKeyboardResId() {
        return mKeyboardResId;
    }

    @KeyboardExtensionType
    public int getExtensionType() {
        return mExtensionType;
    }
}