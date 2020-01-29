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
import com.anysoftkeyboard.prefs.RxSharedPrefs;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;
import java.util.ArrayList;
import java.util.List;

@TargetApi(11)
public class ClipboardV11 implements Clipboard {
    private static final int MAX_ENTRIES_INDEX = 15;

    private final List<CharSequence> mEntries = new ArrayList<>(16);

    private final ClipboardManager mClipboardManager;
    private final RxSharedPrefs mPrefs;

    public ClipboardV11(Context context) {
        mPrefs = AnyApplication.prefs(context);
        mClipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);

        mClipboardManager.addPrimaryClipChangedListener(this::onPrimaryClipChanged);

        onPrimaryClipChanged();
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

    private void onPrimaryClipChanged() {
        if (!mPrefs.getBoolean(
                        R.string.settings_key_os_clipboard_sync,
                        R.bool.settings_default_os_clipboard_sync)
                .get()) return;

        ClipData cp = mClipboardManager.getPrimaryClip();
        if (cp != null) {
            for (int entryIndex = 0; entryIndex < cp.getItemCount(); entryIndex++) {
                while (mEntries.size() > MAX_ENTRIES_INDEX) {
                    mEntries.remove(MAX_ENTRIES_INDEX);
                }

                mEntries.add(0, cp.getItemAt(entryIndex).getText());
            }
        }
    }
}
