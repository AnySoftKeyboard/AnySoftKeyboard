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

package com.anysoftkeyboard.devicespecific;

import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.ClipData.Item;
import android.content.ClipboardManager;
import android.content.Context;

import com.anysoftkeyboard.IndirectlyInstantiated;

@TargetApi(11)
@IndirectlyInstantiated
final class ClipboardV11 implements Clipboard {
    private final ClipboardManager cbV11;
    private final Context mAppContext;

    ClipboardV11(ClipboardDiagram diagram) {
        mAppContext = diagram.getContext();
        cbV11 = (ClipboardManager) mAppContext
                .getSystemService(Context.CLIPBOARD_SERVICE);
    }

    public void setText(CharSequence text) {
        cbV11.setPrimaryClip(ClipData.newPlainText("Styled Text", text));
    }

    public CharSequence getText() {
        ClipData cp = cbV11.getPrimaryClip();
        if (cp != null) {
            if (cp.getItemCount() > 0) {
                Item cpi = cp.getItemAt(0);
                return cpi.coerceToText(mAppContext);
            }
        }

        return null;
    }

}