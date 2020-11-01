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
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.XmlRes;
import com.anysoftkeyboard.addons.AddOnImpl;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class KeyboardExtension extends AddOnImpl {
    public static final int TYPE_BOTTOM = 1;
    public static final int TYPE_TOP = 2;
    public static final int TYPE_EXTENSION = 3;
    @XmlRes private final int mKeyboardResId;
    @KeyboardExtensionType private final int mExtensionType;

    public KeyboardExtension(
            @NonNull Context askContext,
            @NonNull Context packageContext,
            int apiVersion,
            @NonNull CharSequence id,
            CharSequence name,
            @XmlRes int keyboardResId,
            @KeyboardExtensionType int type,
            @NonNull CharSequence description,
            boolean isHidden,
            int sortIndex) {
        super(askContext, packageContext, apiVersion, id, name, description, isHidden, sortIndex);
        mKeyboardResId = keyboardResId;
        mExtensionType = type;
    }

    @KeyboardExtensionType
    public static int ensureValidType(final int keyboardExtensionType) {
        switch (keyboardExtensionType) {
            case TYPE_BOTTOM:
            case TYPE_TOP:
            case TYPE_EXTENSION:
                return keyboardExtensionType;
            default:
                throw new RuntimeException(
                        "Invalid keyboard-extension-type " + keyboardExtensionType);
        }
    }

    @XmlRes
    public int getKeyboardResId() {
        return mKeyboardResId;
    }

    @KeyboardExtensionType
    public int getExtensionType() {
        return mExtensionType;
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({TYPE_BOTTOM, TYPE_TOP, TYPE_EXTENSION})
    public @interface KeyboardExtensionType {}
}
