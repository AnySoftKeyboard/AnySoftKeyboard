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
import android.content.ClipboardManager;
import android.content.Context;
import android.text.TextUtils;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@TargetApi(11)
public class ClipboardV11 implements Clipboard {
    private static final int MAX_ENTRIES_INDEX = 15;

    protected final List<CharSequence> mEntries = new ArrayList<>(16);
    protected final ClipboardManager mClipboardManager;
    @Nullable private ClipboardUpdatedListener mClipboardEntryAddedListener;
    private final ClipboardManager.OnPrimaryClipChangedListener mOsClipboardChangedListener =
            this::onPrimaryClipChanged;

    ClipboardV11(Context context) {
        mClipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
    }

    @Override
    public void setClipboardUpdatedListener(@Nullable ClipboardUpdatedListener listener) {
        if (mClipboardEntryAddedListener != listener) {
            mEntries.clear();
        }
        mClipboardEntryAddedListener = listener;
        if (listener == null) {
            mClipboardManager.removePrimaryClipChangedListener(mOsClipboardChangedListener);
        } else {
            mClipboardManager.addPrimaryClipChangedListener(mOsClipboardChangedListener);
        }
    }

    @Override
    public void setText(CharSequence text) {
        mClipboardManager.setPrimaryClip(ClipData.newPlainText("Styled Text", text));
    }

    @Override
    public CharSequence getText(int entryIndex) {
        return mEntries.get(entryIndex);
    }

    @Override
    public int getClipboardEntriesCount() {
        return mEntries.size();
    }

    @Override
    public void deleteEntry(int entryIndex) {
        mEntries.remove(entryIndex);
        if (entryIndex == 0) {
            // also remove from clipboard
            setText("");
        }
    }

    private void onPrimaryClipChanged() {
        final ClipboardUpdatedListener addedListener = mClipboardEntryAddedListener;
        if (addedListener != null) {
            ClipData cp = mClipboardManager.getPrimaryClip();
            if (cp != null) {
                for (int entryIndex = 0; entryIndex < cp.getItemCount(); entryIndex++) {
                    final CharSequence text = cp.getItemAt(entryIndex).getText();
                    if (TextUtils.isEmpty(text)) continue;
                    mEntries.add(0, text);

                    while (mEntries.size() > MAX_ENTRIES_INDEX) {
                        mEntries.remove(MAX_ENTRIES_INDEX);
                    }

                    addedListener.onClipboardEntryAdded(text);
                }
            }
        }
    }
}
