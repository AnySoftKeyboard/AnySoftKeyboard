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
import android.os.Build;
import android.view.GestureDetector;

import com.anysoftkeyboard.backup.CloudBackupRequester;
import com.anysoftkeyboard.backup.CloudBackupRequesterApi8;

@TargetApi(Build.VERSION_CODES.FROYO)
public class DeviceSpecificV8 extends DeviceSpecificV3 {
    @Override
    public String getApiLevel() {
        return "DeviceSpecificV8";
    }

    @Override
    public GestureDetector createGestureDetector(Context appContext, AskOnGestureListener listener) {
        return new AskV8GestureDetector(appContext, listener);
    }

    @Override
    public CloudBackupRequester createCloudBackupRequester(Context appContext) {
        return new CloudBackupRequesterApi8(appContext);
    }
}