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
public class ClipboardV11 implements Clipboard {
    private final ClipboardManager mClipboardManager;
    private final Context mAppContext;

    public ClipboardV11(ClipboardDiagram diagram) {
        mAppContext = diagram.getContext();
        mClipboardManager = (ClipboardManager) mAppContext.getSystemService(Context.CLIPBOARD_SERVICE);
    }

    @Override
    public void setText(CharSequence text) {
        ClipData newClipData = ClipData.newPlainText("Styled Text", text);
        ClipData oldClipData = mClipboardManager.getPrimaryClip();
        if (oldClipData != null) {
            //we have previous data, we would like to add all the previous
            //text into the new clip-data
            for (int oldClipDataItemIndex = 0; oldClipDataItemIndex < oldClipData.getItemCount(); oldClipDataItemIndex++) {
                newClipData.addItem(oldClipData.getItemAt(oldClipDataItemIndex));
            }
        }
        mClipboardManager.setPrimaryClip(newClipData);
    }

    @Override
    public CharSequence getText(int entryIndex) {
        ClipData cp = mClipboardManager.getPrimaryClip();
        if (cp != null) {
            if (cp.getItemCount() > 0) {
                Item cpi = cp.getItemAt(entryIndex);
                return cpi.coerceToText(mAppContext);
            }
        }

        return null;
    }

    @Override
    public int getClipboardEntriesCount() {
        ClipData cp = mClipboardManager.getPrimaryClip();
        if (cp != null) return cp.getItemCount();
        return 0;
    }
}