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

package com.anysoftkeyboard.keyboards.physical;

import android.text.method.MetaKeyKeyListener;
import android.view.KeyEvent;

import com.anysoftkeyboard.keyboards.AnyKeyboard.HardKeyboardAction;

public class HardKeyboardActionImpl implements HardKeyboardAction {
    private int mKeyCode = 0;
    private boolean mChanged = false;
    private long mMetaState;

    private final int META_ACTIVE_ALT = (MetaKeyKeyListener.META_ALT_ON | MetaKeyKeyListener.META_ALT_LOCKED);
    private final int META_ACTIVE_SHIFT = (MetaKeyKeyListener.META_SHIFT_ON | MetaKeyKeyListener.META_CAP_LOCKED);

    public void initializeAction(KeyEvent event, long metaState) {
        mChanged = false;
        mKeyCode = event.getKeyCode();
        mMetaState = metaState;
    }

    public int getKeyCode() {
        return mKeyCode;
    }

    public boolean isAltActive() {
        return (MetaKeyKeyListener.getMetaState(mMetaState) & META_ACTIVE_ALT) != 0;
    }

    public boolean isShiftActive() {
        return (MetaKeyKeyListener.getMetaState(mMetaState) & META_ACTIVE_SHIFT) != 0;
    }

    public void setNewKeyCode(int keyCode) {
        mChanged = true;
        mKeyCode = keyCode;
    }

    public boolean getKeyCodeWasChanged() {
        return mChanged;
    }
}

