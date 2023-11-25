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
import android.content.Context;

@TargetApi(28)
public class ClipboardV28 extends ClipboardV16 {

  ClipboardV28(Context context) {
    super(context);
  }

  @Override
  public void deleteEntry(int entryIndex) {
    mEntries.remove(entryIndex);
    if (entryIndex == 0) {
      // actually removing from clipboard
      mClipboardManager.clearPrimaryClip();
    }
  }

  @Override
  public void deleteAllEntries() {
    mEntries.clear();
    mClipboardManager.clearPrimaryClip();
  }
}
