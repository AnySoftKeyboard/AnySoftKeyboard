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

package com.anysoftkeyboard.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.anysoftkeyboard.AnySoftKeyboard;
import com.anysoftkeyboard.base.utils.Logger;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.BuildConfig;

public class PackagesChangedReceiver extends BroadcastReceiver {

    private static final String TAG = "ASK PkgChanged";

    private final AnySoftKeyboard mIme;
    private final StringBuilder mStringBuffer = new StringBuilder();

    public PackagesChangedReceiver(AnySoftKeyboard ime) {
        mIme = ime;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getData() == null || context == null)
            return;

        if (BuildConfig.TESTING_BUILD) {
            mStringBuffer.setLength(0);
            String text = mStringBuffer.append("Package '").append(intent.getData()).append("' have been changed.").toString();
            Logger.d(TAG, text);
        }
        try {
            ((AnyApplication) mIme.getApplicationContext()).onPackageChanged(intent, mIme);
        } catch (Exception e) {
            Logger.e(TAG, "Failed to parse changed package. Ignoring.", e);
        }
    }

    public IntentFilter createIntentFilter() {
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
