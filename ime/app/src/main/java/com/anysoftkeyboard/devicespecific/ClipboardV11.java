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

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.TextUtils;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ClipboardV11 implements Clipboard {
  private static final int MAX_ENTRIES_INDEX = 15;

  protected final List<CharSequence> mEntries = new ArrayList<>(16);
  protected final ClipboardManager mClipboardManager;
  protected final Context mContext;
  @Nullable private ClipboardUpdatedListener mClipboardEntryAddedListener;
  private final ClipboardManager.OnPrimaryClipChangedListener mOsClipboardChangedListener =
      this::onPrimaryClipChanged;

  ClipboardV11(Context context) {
    mContext = context;
    mClipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
  }

  @Override
  public void setClipboardUpdatedListener(@Nullable ClipboardUpdatedListener listener) {
    if (mClipboardEntryAddedListener != listener) {
      mEntries.clear();
    }
    if (listener == null) {
      mClipboardManager.removePrimaryClipChangedListener(mOsClipboardChangedListener);
    } else if (mClipboardEntryAddedListener != listener) {
      mClipboardManager.addPrimaryClipChangedListener(mOsClipboardChangedListener);
    }
    mClipboardEntryAddedListener = listener;
  }

  @Override
  public CharSequence getText(int entryIndex) {
    return mEntries.get(entryIndex);
  }

  @Override
  public boolean isOsClipboardEmpty() {
    return !mClipboardManager.hasPrimaryClip();
  }

  @Override
  public int getClipboardEntriesCount() {
    return mEntries.size();
  }

  @Override
  public void deleteEntry(int entryIndex) {
    mEntries.remove(entryIndex);
    if (entryIndex == 0) {
      clearPrimaryClip();
    }
  }

  protected void clearPrimaryClip() {
    mClipboardManager.setPrimaryClip(ClipData.newPlainText("", ""));
  }

  @Override
  public void deleteAllEntries() {
    mEntries.clear();
    clearPrimaryClip();
  }

  protected CharSequence getTextFromClipItem(ClipData.Item item) {
    return item.getText();
  }

  private void onPrimaryClipChanged() {
    // this is call on every change in the clip:
    // new item, clear clip, updated clip, etc.
    // we will only notify the listener if the clip is new or cleared.
    final var addedListener = mClipboardEntryAddedListener;
    if (addedListener != null) {
      var cp = mClipboardManager.hasPrimaryClip() ? mClipboardManager.getPrimaryClip() : null;
      if (cp != null && cp.getItemCount() > 0) {
        // we're only taking the first item!
        // complex clips (multiple items) means that it was a multi-selection
        // copy. This only works if we have the OS doing the paste.
        final var text = getTextFromClipItem(cp.getItemAt(0));
        if (!TextUtils.isEmpty(text)) {
          if (!alreadyKnownText(text)) {
            mEntries.add(0, text);

            while (mEntries.size() > MAX_ENTRIES_INDEX) {
              mEntries.remove(MAX_ENTRIES_INDEX);
            }

            addedListener.onClipboardEntryAdded(text);
          }
        }
      } else {
        addedListener.onClipboardCleared();
      }
    }
  }

  private boolean alreadyKnownText(CharSequence text) {
    if (mEntries.size() > 0) {
      return TextUtils.equals(mEntries.get(0), text);
    }

    return false;
  }
}
