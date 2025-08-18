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

package com.anysoftkeyboard.addons;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import androidx.core.util.Consumer;
import com.anysoftkeyboard.base.utils.Logger;

public class PackagesChangedReceiver extends BroadcastReceiver {

  private static final String TAG = "ASKPkgChanged";

  private final Consumer<Intent> mOnPackageChanged;

  public PackagesChangedReceiver(Consumer<Intent> onPackageChanged) {
    this.mOnPackageChanged = onPackageChanged;
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    if (intent == null || intent.getData() == null || context == null) return;
    Logger.d(TAG, "Package changed: %s", intent.getData());
    mOnPackageChanged.accept(intent);
  }

  public static IntentFilter createIntentFilter() {
    /*
    receiver android:name="com.anysoftkeyboard.receivers.PackagesChangedReceiver">
        <intent-filter>
            <category android:name="android.intent.category.DEFAULT" />
            <action android:name="android.intent.action.PACKAGE_CHANGED"/>
            <action android:name="android.intent.action.PACKAGE_REMOVED"/>
            <action android:name="android.intent.action.PACKAGE_ADDED"/>
            <action android:name="android.intent.action.PACKAGE_INSTALL"/>
            <action android:name="android.intent.action.PACKAGE_REPLACED"/>
            <data android:scheme="package" />
        </intent-filter>
    </receiver>
     */
    IntentFilter filter = new IntentFilter();
    filter.addCategory(Intent.CATEGORY_DEFAULT);

    filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
    filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
    filter.addAction(Intent.ACTION_PACKAGE_ADDED);
    filter.addAction(Intent.ACTION_PACKAGE_REPLACED);

    filter.addDataScheme("package");

    return filter;
  }
}
